/*
* Copyright 2016 NHN Entertainment Corp.
*
* NHN Entertainment Corp. licenses this file to you under the Apache License,
* version 2.0 (the "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at:
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.nhnent.haste.transport.udp;

import com.nhnent.haste.common.CRC;
import com.nhnent.haste.common.Check;
import com.nhnent.haste.security.BigInteger;
import com.nhnent.haste.security.DiffieHellman;
import com.nhnent.haste.transport.AbstractTransport;
import com.nhnent.haste.transport.EventExecutor;
import com.nhnent.haste.transport.EventExecutorGroup;
import com.nhnent.haste.transport.MetricListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

public final class UDPTransport extends AbstractTransport {
    private static final Logger logger = LoggerFactory.getLogger(UDPTransport.class);

    private static final int PROTOCOL_VERSION = 0x02;

    private AtomicInteger connectionCount = new AtomicInteger(0);

    private EventExecutorGroup executorGroup;

    AtomicInteger childIndex = new AtomicInteger();

    private ListenerUDPTransportProxy listenerTransportProxy;
    private ChildrenUDPTransportProxy[] childTransportProxies;

    private final int clientStartPort;

    private final CRC32 crc32 = new CRC32();

    private final Queue<Integer> peerIDs = new ConcurrentLinkedQueue<>();

    private Timer metricTimer;

    private UDPTransport(Builder builder) {
        super(builder);

        this.clientStartPort = builder.clientStartPort;

        //prepare peerIDs
        int max = MAX_CONNECTION * 2;

        for (int i = 1; i < max; i++) {
            peerIDs.add(i);
        }
    }

    public static class Builder extends AbstractTransport.Builder<Builder, UDPTransport> {
        private int clientStartPort;

        public Builder clientStartPort(int clientStartPort) {
            this.clientStartPort = clientStartPort;
            return this;
        }

        @Override
        public UDPTransport build() {
            return new UDPTransport(this);
        }
    }

    /**
     * The callback which is called by a metric timer periodically.
     * <strong>run</strong> method in {@link MetricTask} is called in a timer thread.
     */
    private class MetricTask extends TimerTask {
        MetricListener listener;

        MetricTask(MetricListener listener) {
            this.listener = listener;
        }

        private double calculateMean(double prevMean, int count, long currentValue) {
            return (prevMean + ((currentValue - prevMean) / count));
        }

        @Override
        public void run() {
            // Although proxies was modified or removed by a network thread, this task must run periodically.
            if (this.listener != null) {
                int index = 1;
                double meanOfRoundTripTime = 0.0;
                double meanOfRoundTripTimeDeviation = 0.0;

                try {
                    int proxiesCount = childTransportProxies.length;
                    for (int i = 0; i < proxiesCount; i++) {
                        ChildrenUDPTransportProxy proxy = childTransportProxies[i];
                        if (proxy == null) continue;

                        Map<Integer, TransportPeer> peerMap = childTransportProxies[i].peers;
                        if (peerMap == null) continue;

                        Collection<TransportPeer> peers = peerMap.values();
                        for (TransportPeer peer : peers) {
                            if (peer == null) continue;
                            if (!(peer instanceof UDPNetworkPeer)) continue;

                            UDPNetworkPeer np = (UDPNetworkPeer) peer;
                            long rtt = np.getMeanOfRoundTripTime();
                            long rttDeviation = np.getMeanOfRoundTripTimeDeviation();

                            if (rtt <= 0 || rttDeviation <= 0) continue;

                            meanOfRoundTripTime = calculateMean(meanOfRoundTripTime, index, rtt);
                            meanOfRoundTripTimeDeviation = calculateMean(meanOfRoundTripTimeDeviation, index, rttDeviation);

                            index++;
                        }
                    }
                } catch (Exception ex) {
                    // Do nothing because this task must not interrupt the main method.
                }
                this.listener.onReceive(index - 1, meanOfRoundTripTime, meanOfRoundTripTimeDeviation);
            }
        }
    }

    @Override
    public boolean register(EventExecutorGroup executorGroup) {
        Check.NotNull(executorGroup, "executeGroup");

        application.onStart();

        this.executorGroup = executorGroup;

        try {
            EventExecutor listenerExecutor = executorGroup.listenerExecutor();
            DatagramChannel listener = DatagramChannel.open();

            listener.setOption(StandardSocketOptions.SO_SNDBUF, SEND_BUF_SIZE);
            listener.setOption(StandardSocketOptions.SO_RCVBUF, RECV_BUF_SIZE);

            logger.info("UDP Listen Port: {}", LISTEN_PORT);

            bind(listener, listenerExecutor.selector(), LISTEN_PORT);
            listenerTransportProxy = new ListenerUDPTransportProxy(this, listener, LISTEN_PORT);
            listenerExecutor.registerProxy(listenerTransportProxy);
            listenerExecutor.setSelectorTimeout(0);

            EventExecutor[] childExecutors = executorGroup.childExecutors();

            DatagramChannel[] children = new DatagramChannel[childExecutors.length];
            childTransportProxies = new ChildrenUDPTransportProxy[childExecutors.length];

            for (int i = 0; i < childExecutors.length; i++) {
                children[i] = DatagramChannel.open();
                children[i].setOption(StandardSocketOptions.SO_SNDBUF, SEND_BUF_SIZE);
                children[i].setOption(StandardSocketOptions.SO_RCVBUF, RECV_BUF_SIZE);

                int port = i + clientStartPort;
                bind(children[i], childExecutors[i].selector(), port);
                childTransportProxies[i] = new ChildrenUDPTransportProxy(this, children[i], port);
                childExecutors[i].registerProxy(childTransportProxies[i]);
                childExecutors[i].setSelectorTimeout(1);
            }

            // If exists a metric listener, start a metric collecting task.
            if (this.metricListener != null) {
                if (this.metricTimer != null) {
                    this.metricTimer.cancel();
                }
                long period = this.metricListener.periodMilliseconds();
                this.metricTimer = new Timer("metricTimer", true);
                this.metricTimer.schedule(new MetricTask(this.metricListener), period, period);
            }

            return true;
        } catch (IOException e) {
            logger.error("Failed to register in transport", e);
        }

        return false;
    }

    private void bind(DatagramChannel ch, Selector selector, int port) throws IOException {
        Check.NotNull(ch, "channel");
        Check.NotNull(selector, "selector");

        ch.socket().bind(new InetSocketAddress(port));
        ch.configureBlocking(false);

        ch.register(selector, SelectionKey.OP_READ);
    }

    ConnectionInfo accept(long currentTime, SocketAddress socketAddress, ByteBuffer byteBuffer) {
        if (connectionCount.get() >= MAX_CONNECTION)
            return null;

        int transferred_bytes = byteBuffer.position();
        byteBuffer.position(0);

        if (CommandType.CONNECT.isNotEqual(byteBuffer.get()))  //1
            return null;

        int version = byteBuffer.getInt();             //4

        if (!validateProtocolVersion(version)) {
            logger.error("Invalid connection protocol version : {}", version);
            return null;
        }

        long sendingTime = byteBuffer.getLong();        //8
        short channelCount = byteBuffer.getShort();     //2
        short mtu = byteBuffer.getShort();
        int disconnectionTimeout = byteBuffer.getInt(); //4

        short enableCrc = byteBuffer.getShort();        //2

        byte clientPublicKeyLength = byteBuffer.get();
        if (transferred_bytes - byteBuffer.position() < clientPublicKeyLength) {
            logger.error("Invalid connection packet");
            return null;
        }

        byte[] clientPublicKey = new byte[clientPublicKeyLength];
        byteBuffer = byteBuffer.get(clientPublicKey, 0, clientPublicKeyLength);

        boolean isCrcEnabled = enableCrc > 0;

        if (isCrcEnabled) {
            if (!CRC.Check(crc32, byteBuffer.array(), transferred_bytes, byteBuffer.position())) {
                logger.error("CRC Error");
                return null;
            }
        }

        byteBuffer.position(byteBuffer.position() + CRC.CRC_LENGTH);

        // TODO Re-Implement Diffie-Hellman
        BigInteger clientPublicKeyNumber = new BigInteger(clientPublicKey);
        BigInteger privateKeyNumber = DiffieHellman.generatePrivateKey(160);
        BigInteger serverKeyNumber = DiffieHellman.generatePublicKey(privateKeyNumber);
        BigInteger secretNumber = DiffieHellman.generateSecretKey(clientPublicKeyNumber, privateKeyNumber);

        ChildrenUDPTransportProxy childrenTransportProxy = getClientProxy();

        UDPNetworkPeer networkPeer = new UDPNetworkPeer.Builder()
                .application(application)
                .socketAddress(socketAddress)
                .diffieHellman(secretNumber, serverKeyNumber)
                .peerId(getPeerID())
                .disconnectTimeout(disconnectionTimeout)
                .channelCount(channelCount)
                .enableCRC(isCrcEnabled)
                .mtu(mtu)
                .build();

        connectionCount.getAndIncrement();

        networkPeer.addDisposeEvent(new TransportPeer.Disposable() {

            private int peerID;

            @Override
            public void dispose() {
                releasePeerID(peerID);
                connectionCount.decrementAndGet();
            }

            public TransportPeer.Disposable build(int peerID) {
                this.peerID = peerID;
                return this;
            }
        }.build(networkPeer.getPeerID()));

        childrenTransportProxy.addPeer(networkPeer);

        ConnectionInfo connectionInfo = ConnectionInfo.newConnectionInfo(networkPeer.getPeerID(), networkPeer.getServerKey(), childrenTransportProxy.port());

        listenerTransportProxy.sendConnectionResponse(socketAddress,
                connectionInfo,
                sendingTime,
                currentTime);

        return connectionInfo;
    }

    boolean validateProtocolVersion(int version) {
        return PROTOCOL_VERSION == version;
    }

    private ChildrenUDPTransportProxy getClientProxy() {
        return childTransportProxies[Math.abs(childIndex.getAndIncrement() % childTransportProxies.length)];
    }

    private int getPeerID() {
        Integer peerID = peerIDs.poll();
        return peerID == null ? -1 : peerID;
    }

    private void releasePeerID(int peerID) {
        peerIDs.offer(peerID);
    }
}
