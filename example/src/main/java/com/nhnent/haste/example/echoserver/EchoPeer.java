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

package com.nhnent.haste.example.echoserver;

import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.nhnent.haste.protocol.messages.ResponseMessage;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.NetworkPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class EchoPeer extends ClientPeer {
    private static final Logger logger = LoggerFactory.getLogger(EchoPeer.class);

    public EchoPeer(InitialRequest initialRequest, NetworkPeer networkPeer) {
        super(initialRequest, networkPeer);
    }

    @Override
    protected void onReceive(RequestMessage request, SendOptions options) {
        EchoMessage message = new EchoMessage(request);

        logger.info(MessageFormat.format("Client message is \"{0}\"", message.message));

        ResponseMessage response = message.toResponse();
        this.send(response, options);
    }

    @Override
    protected void onDisconnect(DisconnectReason reason, String detail) {
        logger.error("Disconnect : {}:{}", reason, detail);
    }
}
