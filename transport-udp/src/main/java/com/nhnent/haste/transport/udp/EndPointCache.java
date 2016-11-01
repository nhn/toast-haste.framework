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

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

final class EndPointCache {
    private static EndPointCache instance = new EndPointCache();

    private final int CONNECTION_TIMEOUT = 2000;

    private final int CLEANUP_INTERVAL = 1000;

    private AtomicLong lastCleanUpTime = new AtomicLong(0);

    private Map<SocketAddress, EndPoint> connectingList = new HashMap<>();

    private EndPointCache() {
    }

    public static EndPointCache getInstance() {
        return instance;
    }

    public void addEndPoint(SocketAddress sa, long currentTime, ConnectionInfo connectionInfo) {
        EndPoint ep = new EndPoint(currentTime, connectionInfo);

        synchronized (connectingList) {
            connectingList.put(sa, ep);
        }
    }

    public ConnectionInfo getInfo(SocketAddress sa) {
        EndPoint ep;

        synchronized (connectingList) {
            ep = connectingList.get(sa);
        }

        if (ep == null)
            return null;

        return ep.getInfo();
    }

    private class EndPoint {
        private final long time;
        private final ConnectionInfo info;

        EndPoint(long currentTime, ConnectionInfo info) {
            this.time = currentTime;
            this.info = info;
        }

        long getTime() {
            return this.time;
        }

        ConnectionInfo getInfo() {
            return info;
        }
    }

    boolean exist(long currentTime, SocketAddress sa) {
        boolean cleanUp = false;

        if ((lastCleanUpTime.get() + CLEANUP_INTERVAL) > currentTime) {
            cleanUp = true;
            lastCleanUpTime.set(currentTime);
        }


        long threshold = currentTime - CONNECTION_TIMEOUT;

        synchronized (connectingList) {

            if (cleanUp) {
                List<SocketAddress> removeList = new LinkedList<>();

                for (Map.Entry<SocketAddress, EndPoint> pair : connectingList.entrySet()) {
                    EndPoint endPoint = pair.getValue();

                    if (endPoint.getTime() < threshold)
                        removeList.add(pair.getKey());
                }

                for (SocketAddress elem : removeList) {
                    connectingList.remove(elem);
                }

            }

            EndPoint endPoint = connectingList.get(sa);

            if (endPoint == null || endPoint.getTime() < threshold) {
                return false;
            }
        }

        return true;
    }
}
