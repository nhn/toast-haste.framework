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

public abstract class ConnectionState {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTING = 3;

    protected abstract int value();

    public boolean transitDisconnected(AbstractNetworkPeer peer) {
        throw new UnsupportedOperationException();
    }

    public boolean transitDisconnecting(AbstractNetworkPeer peer) {
        throw new UnsupportedOperationException();
    }

    public boolean transitConnected(AbstractNetworkPeer peer) {
        throw new UnsupportedOperationException();
    }

    public boolean transitConnecting(AbstractNetworkPeer peer) {
        throw new UnsupportedOperationException();
    }

    public boolean isEqual(int state) {
        return value() == state;
    }

    public boolean isNotEqual(int state) {
        return value() != state;
    }
}
