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

/**
 * Will receive a payload and disconnect message from {@link NetworkPeer}.
 */
public interface ApplicationPeer {
    /**
     * The callback that would receive a payload.
     * @param data The received data.
     * @param dataLength The length of received data.
     * @param channel The channel that received a payload.
     * @param isEncrypted {@code true} if a payload was encrypted {@code false} otherwise.
     * @param qos The received {@link QoS}
     */
    void internal_onReceive(byte[] data, int dataLength,
                            byte channel,
                            boolean isEncrypted,
                            QoS qos);

    /**
     * The callback that would receive a disconnect reason, and detail message.
     * @param reason The disconnect reason.
     * @param detail The detail message.
     */
    void internal_onDisconnect(DisconnectReason reason, String detail);
}
