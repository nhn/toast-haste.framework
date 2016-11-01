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

package com.nhnent.haste.protocol.protocols;

import com.nhnent.haste.protocol.HeaderMessage;
import com.nhnent.haste.protocol.Protocol;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.protocol.messages.InitialResponse;
import com.nhnent.haste.protocol.messages.Message;
import com.nhnent.haste.protocol.messages.MessageType;

public class JsonProtocol implements Protocol {
    public static JsonProtocol instance = new JsonProtocol();

    private JsonProtocol() {
    }

    public final static byte VERSION = 0x11;

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public MessageType getMessageType(byte[] data) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public byte[] serialize(Message message) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public byte[] serialize(InitialRequest initialRequest) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public byte[] serialize(InitialResponse initialResponse) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public byte[] serializeHeader(HeaderMessage headerMessage) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public Message deserializeMessage(byte[] data) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public InitialRequest deserializeInitialRequest(byte[] data) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public InitialResponse deserializeInitialResponse(byte[] data) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }

    @Override
    public HeaderMessage deserializeHeader(byte[] data) {
        throw new UnsupportedOperationException("JsonProtocol is not supported");
    }
}
