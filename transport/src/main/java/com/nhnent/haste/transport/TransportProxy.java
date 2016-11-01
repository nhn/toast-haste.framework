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

import java.io.IOException;

/**
 * Handling packet periodically in event loop.
 */
public interface TransportProxy {
    /**
     * Perform when SelectionKey was {@code OP_ACCEPT}
     *
     * @param currentTime Current time.
     */
    void accept(long currentTime);

    /**
     * Perform when SelectionKey was {@code OP_READ}
     *
     * @param currentTime Current time.
     */
    void read(long currentTime);

    /**
     * Perform when SelectionKey was {@code OP_WRITE}
     *
     * @param currentTime Current time.
     */
    void write(long currentTime);

    /**
     * Perform periodically for sending data.
     *
     * @param currentTime Current time.
     */
    void send(long currentTime);

    /**
     * Closes all resources included {@link java.nio.channels.DatagramChannel}
     *
     * @throws IOException If an I/O error occurs
     */
    void close() throws IOException;
}
