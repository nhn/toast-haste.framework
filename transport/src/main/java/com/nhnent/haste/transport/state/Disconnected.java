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

public class Disconnected extends ConnectionState {
    public final static Disconnected instance = new Disconnected();

    private Disconnected() {
    }

    @Override
    public int value() {
        return DISCONNECTED;
    }

    @Override
    public boolean transitConnecting(AbstractNetworkPeer peer) {
        return peer.transitConnectionState(Connecting.instance, this);
    }

    @Override
    public String toString() {
        return "Disconnected";
    }
}
