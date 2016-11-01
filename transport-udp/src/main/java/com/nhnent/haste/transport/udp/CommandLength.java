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

public enum CommandLength {
    ACK_PAYLOAD(16),
    CONNECT_PAYLOAD(14),
    MIN_SIZE(13),
    DISCONNECT_HEADER_SIZE(MIN_SIZE.getLength()),
    //PING_SIZE(MIN_SIZE.getLength()),
    RELIABLE_HEADER_SIZE(MIN_SIZE.getLength()),
    ACK_HEADER_SIZE(MIN_SIZE.getLength()),
    UNRELIABLE_HEADER_SIZE(MIN_SIZE.getLength() + 8),
    FRAGMENTED_HEADER_SIZE(MIN_SIZE.getLength() + 20),
    UDP_MTU_HEADER_LENGTH(23);

    private int value;

    CommandLength(int value) {
        this.value = value;
    }

    public int getLength() {
        return value;
    }
}
