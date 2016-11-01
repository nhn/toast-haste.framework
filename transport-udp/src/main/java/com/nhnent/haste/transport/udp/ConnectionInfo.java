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

import com.nhnent.haste.security.BigInteger;

final class ConnectionInfo {
    private final int peerID;
    private final BigInteger serverKeyNumber;
    private final int port;

    private ConnectionInfo(int peerID, BigInteger serverKey, int port) {
        this.peerID = peerID;
        this.serverKeyNumber = serverKey;
        this.port = port;
    }

    public static ConnectionInfo newConnectionInfo(int peerID, BigInteger serverKeyNumber, int port) {
        return new ConnectionInfo(peerID, serverKeyNumber, port);
    }

    public int getPeerID() {
        return peerID;
    }

    public BigInteger getServerKeyNumber() {
        return serverKeyNumber;
    }

    public int getPort() {
        return port;
    }
}