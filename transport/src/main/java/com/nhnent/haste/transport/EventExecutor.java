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

import java.nio.channels.Selector;

/**
 * Control information about the network.
 * For instance, {@link Selector}, {@link TransportProxy}, and timeout, etc.
 */
public interface EventExecutor {
    /**
     * Return a selector.
     */
    Selector selector();

    /**
     * Register proxy that processed event when received data from a socket.
     */
    void registerProxy(TransportProxy transportProxy);

    /**
     * Set a timeout of selector.
     */
    void setSelectorTimeout(long milliseconds);
}
