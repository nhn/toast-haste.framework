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

public enum CommandType {
    INVALID(Byte.MIN_VALUE),
    MESSAGES((byte) 0),
    ACK((byte) 1),
    CONNECT((byte) 2),
    CONNECT_RESPONSE((byte) 3),
    DISCONNECT((byte) 4),
    PING((byte) 5),
    RELIABLE((byte) 6),
    UNRELIABLE((byte) 7),
    FRAGMENT((byte) 8),
    EG_SERVER_TIME((byte) 14);

    private final byte value;

    CommandType(byte value) {
        this.value = value;
    }

    public static CommandType convert(byte value) {
        if (RELIABLE.isEqual(value)) {
            return CommandType.RELIABLE;
        } else if (UNRELIABLE.isEqual(value)) {
            return CommandType.UNRELIABLE;
        } else if (PING.isEqual(value)) {
            return CommandType.PING;
        } else if (DISCONNECT.isEqual(value)) {
            return CommandType.DISCONNECT;
        } else if (FRAGMENT.isEqual(value)) {
            return CommandType.FRAGMENT;
        } else if (EG_SERVER_TIME.isEqual(value)) {
            return CommandType.EG_SERVER_TIME;
        } else if (ACK.isEqual(value)) {
            return CommandType.ACK;
        } else {
            return CommandType.INVALID;
        }
    }

    public byte getByte() {
        return value;
    }

    public boolean isEqual(byte value) {
        return this.value == value;
    }

    public boolean isNotEqual(byte value) {
        return this.value != value;
    }
}
