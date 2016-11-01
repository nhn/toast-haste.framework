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

import com.nhnent.haste.transport.state.ConnectionState;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * The peer which was managed in low-level layer({@link com.nhnent.haste.transport.Transport}).
 */
interface TransportPeer {
    /**
     * Return the identifier of this peer.
     */
    int getPeerID();

    /**
     * Return connection state of this peer.
     */
    ConnectionState getConnectionState();

    /**
     * Add dispose event which is invoked when this peer is disconnected.
     */
    void addDisposeEvent(Disposable disposeEvent);

    /**
     * Send a data through {@link DatagramChannel}.
     */
    void send(DatagramChannel channel, long currentTime);

    /**
     * The callback which was received data from socket.
     */
    void onReceive(long currentTime, SocketAddress socketAddress, ByteBuffer byteBuffer, int transferredBytes);

    /**
     * Release resources which need to release when this peer is disconnected.
     */
    void dispose();

    interface Disposable {
        void dispose();
    }
}
