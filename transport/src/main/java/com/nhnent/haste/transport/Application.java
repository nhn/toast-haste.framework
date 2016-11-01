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
 * Will receive life-cycle event from {@link Transport} or {@link NetworkPeer}.
 */
public interface Application {
    /**
     * Called when the application was started.
     */
    void onStart();

    /**
     * Called when the {@link NetworkPeer} was disconnected.
     * @param networkPeer The disconnected peer.
     */
    void onDisconnect(NetworkPeer networkPeer);

    /**
     * Called when the {@link NetworkPeer} was connected.
     * @param networkPeer The connected peer.
     * @param payload The received payload when the peer was connected.
     * @param channel The channel that received connect payload.
     */
    void onConnect(NetworkPeer networkPeer, Payload payload, byte channel);

    /**
     * Called when the application was stopped.
     */
    void onStop();
}
