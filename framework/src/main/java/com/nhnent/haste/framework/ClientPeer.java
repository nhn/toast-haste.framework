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

package com.nhnent.haste.framework;

import com.nhnent.haste.common.Check;
import com.nhnent.haste.framework.fiber.FiberFactory;
import com.nhnent.haste.framework.security.AesCryptoProvider;
import com.nhnent.haste.framework.security.CryptoProvider;
import com.nhnent.haste.protocol.Protocol;
import com.nhnent.haste.protocol.ReturnCode;
import com.nhnent.haste.protocol.messages.*;
import com.nhnent.haste.transport.ApplicationPeer;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import com.nhnent.haste.transport.QoS;
import com.nhnent.haste.transport.state.ConnectionState;
import org.jetlang.fibers.Fiber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for receiving a payload and disconnect message from {@link NetworkPeer}.
 */
public abstract class ClientPeer implements ApplicationPeer {
    private static final Logger logger = LoggerFactory.getLogger(ClientPeer.class);

    private class ReceiveRunnable implements Runnable {
        private byte[] receivedData;
        private int receivedDataLength;
        private byte channel;
        private boolean isEncrypted;
        private QoS qos;

        public ReceiveRunnable(byte[] data, int dataLength, byte channel, boolean isEncrypted, QoS qos) {
            this.receivedData = data;
            this.receivedDataLength = dataLength;
            this.channel = channel;
            this.isEncrypted = isEncrypted;
            this.qos = qos;
        }

        @Override
        public void run() {
            if (networkPeer == null) {
                logger.warn("networkPeer is null");
                return;
            }

            if (networkPeer.getConnectionState().isNotEqual(ConnectionState.CONNECTED)) {
                logger.warn("networkPeer is not connected");
                return;
            }

            if (receivedData == null) {
                logger.warn("receivedData is null when receiving");
                return;
            }

            ClientPeer.this.onReceived(receivedData, receivedDataLength, channel, isEncrypted, qos);
        }
    }

    private class OnDisconnectRunnable implements Runnable {
        private DisconnectReason reason;
        private String detail;

        private OnDisconnectRunnable(DisconnectReason reason, String detail) {
            this.reason = reason;
            this.detail = detail;
        }

        @Override
        public void run() {
            ClientPeer.this.onDisconnect(reason, detail);
        }
    }

    private CryptoProvider cryptoProvider;

    private NetworkPeer networkPeer;

    private Fiber fiber = FiberFactory.newFiber();

    protected Protocol protocol;

    public ClientPeer(InitialRequest initialRequest, NetworkPeer networkPeer) {
        this.networkPeer = networkPeer;
        this.protocol = initialRequest.getProtocol();
        this.cryptoProvider = new AesCryptoProvider(networkPeer.getSecretKey());

        networkPeer.setApplicationPeer(this);

        fiber.start();
    }

    @Override
    public void internal_onReceive(byte[] data, int dataLength, byte channel, boolean isEncrypted, QoS qos) {
        if (data.length < dataLength)
            return;

        ReceiveRunnable runnable = new ReceiveRunnable(data, dataLength, channel, isEncrypted, qos);
        fiber.execute(runnable);
    }

    @Override
    public void internal_onDisconnect(DisconnectReason reason, String detail) {
        fiber.execute(new OnDisconnectRunnable(reason, detail));
    }

    public void disconnect(DisconnectReason reason, String detail) {
        networkPeer.disconnect(reason, detail);
    }

    private void onReceived(byte[] payloadBytes, int payloadLength, byte channel, boolean isEncrypted, QoS qos) {
        if (isEncrypted) {
            byte[] decryptedPayload = this.cryptoProvider.decrypt(payloadBytes, 0, payloadLength);
            if (decryptedPayload == null) {
                if (logger.isWarnEnabled())
                    logger.warn("Failed Encrypted Data : length[{}]", payloadLength);
                return;
            }
            payloadBytes = decryptedPayload;
        }

        MessageType type = this.protocol.getMessageType(payloadBytes);
        switch (type) {
            case INITIAL_REQUEST:
                // TODO Validate InitialRequest
                InitialResponse initialResponse = new InitialResponse(ReturnCode.OK, "success");
                send(initialResponse, channel, false, QoS.RELIABLE_SEQUENCED);
                break;
            case INITIAL_RESPONSE:
                break;
            case REQUEST_MESSAGE:
            case RESPONSE_MESSAGE:
            case EVENT_MESSAGE:
                Message message = this.protocol.deserializeMessage(payloadBytes);
                if (message != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("onReceived message : {}", message.toString());
                    }

                    if (message instanceof RequestMessage) {
                        onReceive((RequestMessage) message, SendOptions.take(channel, isEncrypted, qos));
                    }
                }
                break;
            case NONE:
                logger.debug("Invalid message code!");
                break;
        }
    }

    protected boolean send(ResponseMessage response, SendOptions options) {
        return send(response, options.getChannel(), options.isEncrypt(), options.getQos());
    }

    protected boolean send(ResponseMessage response, byte channel, boolean encrypt, QoS qos) {
        return send((Message) response, channel, encrypt, qos);
    }

    public boolean send(Message message, SendOptions options) {
        return send(message, options.getChannel(), options.isEncrypt(), options.getQos());
    }

    protected boolean send(Message message, byte channel, boolean encrypt, QoS qos) {
        Check.NotNull(message, "message");

        byte[] payload = protocol.serialize(message);

        if (encrypt) {
            payload = this.cryptoProvider.encrypt(payload);
        }

        return send(payload, payload.length, channel, encrypt, qos);
    }

    protected boolean send(InitialResponse initialResponse, byte channel, boolean encrypt, QoS qos) {
        Check.NotNull(initialResponse, "initialResponse");

        byte[] payload = protocol.serialize(initialResponse);

        if (encrypt) {
            payload = this.cryptoProvider.encrypt(payload);
        }

        return send(payload, payload.length, channel, encrypt, qos);
    }

    protected boolean send(byte[] payload, int payloadLength, byte channel, boolean encrypt, QoS qos) {

        if (networkPeer == null) {
            return false;
        }

        if (networkPeer.getConnectionState().isEqual(ConnectionState.DISCONNECTED)) {
            return false;
        }

        boolean result = networkPeer.enqueueOutgoingCommand(payload, payloadLength, channel, encrypt, qos);

        if (result) {
            onSent(payload, channel, encrypt, qos);
        } else {
            onFailedToSend(payload, channel, encrypt, qos);
        }

        return result;
    }

    protected void onFailedToSend(byte[] payload, byte channel, boolean encrypt, QoS qos) {
        logger.error("ch[{}] Failed to send command!!", channel);
    }

    protected void onSent(byte[] payload, byte channel, boolean encrypt, QoS qos) {
    }

    public long getCurrentRTT() {
        return networkPeer.getMeanOfRoundTripTime();
    }

    public long getCurrentRTTDeviation() {
        return networkPeer.getMeanOfRoundTripTimeDeviation();
    }

    public long getLowestRoundTripTime() {
        return networkPeer.getLowestRoundTripTime();
    }

    public long getHighestRoundTripTimeDeviation() {
        return networkPeer.getHighestRoundTripTimeDeviation();
    }

    /**
     * Called when this peer was received a message from {@link NetworkPeer}
     * @param request A request message.
     * @param options A option about networking.
     */
    protected abstract void onReceive(RequestMessage request, SendOptions options);

    /**
     * Called when this peer was disconnected.
     * @param reason The reason of disconnection.
     * @param detail The detail message of disconnection.
     */
    protected abstract void onDisconnect(DisconnectReason reason, String detail);
}
