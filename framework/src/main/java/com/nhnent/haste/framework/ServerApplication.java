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

import com.nhnent.haste.framework.fiber.FiberFactory;
import com.nhnent.haste.framework.fiber.FiberThreadFactory;
import com.nhnent.haste.protocol.Protocol;
import com.nhnent.haste.protocol.ProtocolChooser;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for receiving life-cycle event from {@link Transport} or {@link NetworkPeer}.
 */
public abstract class ServerApplication implements Application {
    private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);

    private final int FIBER_THREAD_COUNT = 4;

    @Override
    public void onStart() {
        FiberFactory.init(FIBER_THREAD_COUNT, new FiberThreadFactory());
        setup();
    }

    @Override
    public void onStop() {
        tearDown();
    }

    @Override
    public void onDisconnect(NetworkPeer networkPeer) {
    }

    @Override
    public void onConnect(NetworkPeer networkPeer, Payload payload, byte channel) {
        byte[] payloadBytes = payload.getBytes();

        // TODO Improve to get protocol version. Because this method is different by protocol.
        Protocol protocol = ProtocolChooser.getProtocol(payloadBytes[0]);
        if (protocol == null) {
            networkPeer.disconnect(DisconnectReason.INVALID_DATA_FORMAT, "Invalid protocol version");
            logger.error("Failed to get protocol");
            return;
        }

        InitialRequest initialRequest = protocol.deserializeInitialRequest(payloadBytes);

        if (initialRequest == null) {
            networkPeer.disconnect(DisconnectReason.INVALID_DATA_FORMAT, "Invalid Data format");
            logger.error("Failed to get initialRequest");
            return;
        }

        ClientPeer clientPeer = createPeer(initialRequest, networkPeer);

        if (clientPeer == null) {
            networkPeer.disconnect(DisconnectReason.CLIENT_DISCONNECT, initialRequest.getErrorMessage());
            logger.error("Failed to create client peer");
        }
    }

    /**
     * Called when this application was started.
     */
    protected abstract void setup();

    /**
     * Called when this application was terminated.
     */
    protected abstract void tearDown();

    /**
     * Called when {@link NetworkPeer} was connected to this application.
     * @param initialRequest The initial request that was contained information about connection.
     * @param networkPeer {@link NetworkPeer}
     * @return
     */
    protected abstract ClientPeer createPeer(InitialRequest initialRequest, NetworkPeer networkPeer);
}
