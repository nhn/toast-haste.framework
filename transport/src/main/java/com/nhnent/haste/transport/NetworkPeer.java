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

import com.nhnent.haste.transport.state.ConnectionState;

/**
 * Performs linkages between {@link Transport} and {@link ApplicationPeer}.
 */
public interface NetworkPeer {
    /**
     * Return the mean of round-trip time.
     */
    long getMeanOfRoundTripTime();

    /**
     * Return the deviation of the mean of round-trip time.
     */
    long getMeanOfRoundTripTimeDeviation();

    /**
     * Return the lowest round-trip time.
     */
    long getLowestRoundTripTime();

    /**
     * Return the highest deviation of the mean of round-trip time.
     */
    long getHighestRoundTripTimeDeviation();

    /**
     * Return secret key for cryptography.
     */
    byte[] getSecretKey();

    /**
     * Return current connection state.
     */
    ConnectionState getConnectionState();

    /**
     * Enqueue a command which received data to a queue of channel.
     * @return {@code true} if enqueuing was success {@code false} otherwise.
     */
    boolean enqueueOutgoingCommand(byte[] payload, int payloadLength, byte channelIndex, boolean encrypt, QoS qos);

    /**
     * Set {@link ApplicationPeer} in order to link between NetworkPeer and ApplicationPeer.
     */
    void setApplicationPeer(ApplicationPeer applicationPeer);

    /**
     * Disconnect to this peer.
     * @param reason The disconnect reason.
     * @param detail The detail message.
     */
    void disconnect(DisconnectReason reason, String detail);
}
