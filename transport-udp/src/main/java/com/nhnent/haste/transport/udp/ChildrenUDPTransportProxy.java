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

import com.nhnent.haste.transport.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class ChildrenUDPTransportProxy extends AbstractUDPTransportProxy {
    private static final Logger logger = LoggerFactory.getLogger(ChildrenUDPTransportProxy.class);

    final Map<Integer, TransportPeer> peers = new HashMap<>();

    private final int SEND_RATE = 30;

    private long lastSentTime = 0;

    public ChildrenUDPTransportProxy(UDPTransport transport, DatagramChannel ch, int port) {
        super(transport, ch, port);
        readByteBuffer.order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public void read(long currentTime) {
        try {
            SocketAddress socketAddress = ch.receive(readByteBuffer);

            int transferred_bytes = readByteBuffer.position();
            readByteBuffer.position(0);

            if (transferred_bytes > 0)
                onReceive(currentTime, socketAddress, readByteBuffer, transferred_bytes);

        } catch (IOException e) {
            logger.error("Failed to read data in ChildrenUDPTransportProxy", e);
        } finally {
            readByteBuffer.clear();
        }
    }

    private void onReceive(long currentTime, SocketAddress socketAddress, ByteBuffer byteBuffer, int transferred_bytes) {
        byte ct = byteBuffer.get();           //1

        if (CommandType.MESSAGES.isNotEqual(ct))
            return;

        int peerID = byteBuffer.getInt();    //4

        TransportPeer peer = getPeer(peerID);

        if (peer == null) {
            return;
        }

        peer.onReceive(currentTime, socketAddress, byteBuffer, transferred_bytes);
    }

    @Override
    public void send(long currentTime) {
        if (lastSentTime + SEND_RATE > currentTime)
            return;

        lastSentTime = currentTime;

        List<TransportPeer> disconnectedPeers = new LinkedList<>();

        synchronized (peers) {
            for (TransportPeer peer : peers.values()) {
                try {
                    peer.send(ch, currentTime);
                } catch (Exception e) {
                    logger.error("Raised exception when sending", e);
                }

                if (peer.getConnectionState().isEqual(ConnectionState.DISCONNECTED)) {
                    disconnectedPeers.add(peer);
                }
            }

            for (TransportPeer peer : disconnectedPeers) {
                peer.dispose();
            }
        }
    }

    public void addPeer(TransportPeer peer) {
        int peerID = peer.getPeerID();

        peer.addDisposeEvent(new TransportPeer.Disposable() {
            private int peerID;

            @Override
            public void dispose() {
                removePeer(peerID);
            }

            public TransportPeer.Disposable build(int peerID) {
                this.peerID = peerID;
                return this;
            }
        }.build(peerID));

        synchronized (peers) {
            peers.put(peerID, peer);
        }
    }

    private void removePeer(int peerID) {
        synchronized (peers) {
            peers.remove(peerID);
        }
    }

    private TransportPeer getPeer(int peerID) {
        synchronized (peers) {
            return peers.get(peerID);
        }
    }
}
