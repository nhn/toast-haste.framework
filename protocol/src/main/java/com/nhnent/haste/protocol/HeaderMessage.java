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

package com.nhnent.haste.protocol;

import com.nhnent.haste.protocol.messages.MessageType;

public class HeaderMessage {
    public static final int HEADER_SIZE = 2;

    byte protocolVersion;
    byte messageType;

    public HeaderMessage(byte protocolVersion, byte messageType) {
        this.protocolVersion = protocolVersion;
        this.messageType = messageType;
    }

    public byte getProtocolVersion() {
        return this.protocolVersion;
    }

    public MessageType getMessageType() {
        return MessageType.getType(this.messageType);
    }
}
