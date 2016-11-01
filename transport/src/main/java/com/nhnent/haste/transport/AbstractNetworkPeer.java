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

package com.nhnent.haste.transport;

import com.nhnent.haste.security.BigInteger;
import com.nhnent.haste.security.SHA256;
import com.nhnent.haste.transport.state.ConnectionState;
import com.nhnent.haste.transport.state.Disconnected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractNetworkPeer implements NetworkPeer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractNetworkPeer.class);

    private AtomicReference<ConnectionState> connectionState = new AtomicReference<ConnectionState>(Disconnected.instance);

    private final Application application;

    protected final int peerID;

    protected final BigInteger secretNumber;
    protected final BigInteger serverKeyNumber;

    private final byte[] secretKey;

    // TODO Improve to process disconnect.
    protected DisconnectReason disconnectReason = DisconnectReason.UNKNOWN_ERROR;
    protected String disconnectDetail = "";

    protected AbstractNetworkPeer(AbstractBuilder builder, long currentTime) {
        this.peerID = builder.peerID;
        this.application = builder.application;

        this.secretNumber = builder.secretNumber;
        this.serverKeyNumber = builder.serverKeyNumber;
        this.secretKey = SHA256.hash(this.secretNumber);

        connectionState.get().transitConnecting(this);

        timestampOfLastReceive = currentTime;
    }

    private ApplicationPeer applicationPeer;

    public abstract static class AbstractBuilder<T extends AbstractBuilder> {
        private Application application;
        private BigInteger secretNumber;
        private BigInteger serverKeyNumber;
        private int peerID = -1;

        @SuppressWarnings("unchecked")
        public T application(Application application) {
            this.application = application;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T diffieHellman(BigInteger secretNumber, BigInteger serverKeyNumber) {
            this.secretNumber = secretNumber;
            this.serverKeyNumber = serverKeyNumber;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T peerId(int peerID) {
            this.peerID = peerID;
            return (T) this;
        }
    }

    private final long INITIAL_RTT = 300;

    protected volatile long timestampOfLastReceive;

    private volatile long meanOfRoundTripTime = INITIAL_RTT;

    private volatile long meanOfRoundTripTimeDeviation = 1;

    private volatile long lowestRoundTripTime = INITIAL_RTT;

    private volatile long highestRoundTripTimeDeviation = 0;

    public int getPeerID() {
        return peerID;
    }

    @Override
    public long getMeanOfRoundTripTime() {
        return meanOfRoundTripTime;
    }

    @Override
    public long getMeanOfRoundTripTimeDeviation() {
        return meanOfRoundTripTimeDeviation;
    }

    @Override
    public long getLowestRoundTripTime() {
        return lowestRoundTripTime;
    }

    @Override
    public long getHighestRoundTripTimeDeviation() {
        return highestRoundTripTimeDeviation;
    }

    @Override
    public void setApplicationPeer(ApplicationPeer applicationPeer) {
        connectionState.get().transitConnected(this);
        this.applicationPeer = applicationPeer;
    }

    protected void UpdateRoundTripTimeAndVariance(long lastRoundTripTime) {
        long currentRTT = meanOfRoundTripTime;
        long nextRTT = currentRTT;
        long nextDeviation = meanOfRoundTripTimeDeviation;

        nextRTT -= (nextRTT >> 3);
        nextDeviation -= (nextDeviation >> 3);

        long rtt = lastRoundTripTime >> 3;
        long var = Math.abs(lastRoundTripTime - currentRTT) >> 3;

        nextRTT += rtt;
        nextDeviation += var;

        meanOfRoundTripTime = nextRTT;
        meanOfRoundTripTimeDeviation = nextDeviation;

        if (meanOfRoundTripTime < lowestRoundTripTime) {
            lowestRoundTripTime = meanOfRoundTripTime;
        }

        if (meanOfRoundTripTimeDeviation > highestRoundTripTimeDeviation) {
            highestRoundTripTimeDeviation = meanOfRoundTripTimeDeviation;
        }
    }

    protected void getClientPeer(Payload payload, byte channel) {
        application.onConnect(this, payload, channel);
    }

    protected void onDisconnected(DisconnectReason reason, String detail) {
        application.onDisconnect(this);

        if (applicationPeer != null) {
            applicationPeer.internal_onDisconnect(reason, detail);
            applicationPeer = null;
        }
    }

    protected boolean isClientPeerNull() {
        return applicationPeer == null;
    }

    protected boolean sendDataToClientPeer(Payload payload, byte channel, boolean isEncrypted, QoS qos) {
        if (applicationPeer == null)
            return false;

        byte[] data = payload.copyOf();
        int dataLength = payload.getLength();

        applicationPeer.internal_onReceive(data, dataLength, channel, isEncrypted, qos);

        return true;
    }

    public boolean transitConnectionState(ConnectionState newState, ConnectionState oldState) {
        if (connectionState.get() == newState)
            return false;

        connectionState.set(newState);

        if (logger.isTraceEnabled()) {
            logger.trace("PeerID[{}] changed state from {} to {}", peerID, oldState.toString(), newState);
        }

        return true;
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState.get();
    }

    public byte[] getSecretKey() {
        return this.secretKey;
    }
}
