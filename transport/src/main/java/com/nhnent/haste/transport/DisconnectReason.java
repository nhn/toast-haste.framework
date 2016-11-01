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

public enum DisconnectReason {
    TIMEOUT_DISCONNECT(1),
    CLIENT_DISCONNECT(2),
    SERVER_USER_LIMIT(3),
    SERVER_DISCONNECT(4),
    QUEUE_OVERFLOW(5),
    INVALID_CONNECTION(6),
    INVALID_ENCRYPTION(7),
    INVALID_DATA_FORMAT(8),
    UNKNOWN_ERROR(9),
    APPLICATION_STOP(12),
    CONNECTION_FAILED(13);

    private int value;

    DisconnectReason(int value) {
        this.value = value;
    }

    public int getInt() {
        return value;
    }

    public static DisconnectReason getReason(int value) {
        DisconnectReason[] reasons = DisconnectReason.values();
        for (int i = 0; i < reasons.length; i++) {
            if (reasons[i].getInt() == value)
                return reasons[i];
        }
        return UNKNOWN_ERROR;
    }
}
