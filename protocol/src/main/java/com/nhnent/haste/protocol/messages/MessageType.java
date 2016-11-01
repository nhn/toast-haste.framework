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

package com.nhnent.haste.protocol.messages;

public enum MessageType {
    NONE((byte) 0),
    INITIAL_REQUEST((byte) 1),
    INITIAL_RESPONSE((byte) 2),
    REQUEST_MESSAGE((byte) 3),
    RESPONSE_MESSAGE((byte) 4),
    EVENT_MESSAGE((byte) 5);

    private byte value;

    MessageType(byte value) {
        this.value = value;
    }

    public static MessageType getType(byte value) {
        switch (value) {
            case 1:
                return MessageType.INITIAL_REQUEST;
            case 2:
                return MessageType.INITIAL_RESPONSE;
            case 3:
                return MessageType.REQUEST_MESSAGE;
            case 4:
                return MessageType.RESPONSE_MESSAGE;
            case 5:
                return MessageType.EVENT_MESSAGE;
            default:
                return MessageType.NONE;
        }
    }

    public byte getByte() {
        return value;
    }
}
