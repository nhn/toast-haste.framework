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

package com.nhnent.haste.transport.state;

import com.nhnent.haste.transport.AbstractNetworkPeer;

public class Connecting extends ConnectionState {
    public final static Connecting instance = new Connecting();

    private Connecting() {
    }

    @Override
    public int value() {
        return CONNECTING;
    }

    @Override
    public boolean transitDisconnecting(AbstractNetworkPeer peer) {
        return peer.transitConnectionState(Disconnecting.instance, this);
    }


    @Override
    public boolean transitDisconnected(AbstractNetworkPeer peer) {
        return peer.transitConnectionState(Disconnected.instance, this);
    }

    @Override
    public boolean transitConnected(AbstractNetworkPeer peer) {
        return peer.transitConnectionState(Connected.instance, this);
    }

    @Override
    public String toString() {
        return "Connecting";
    }
}
