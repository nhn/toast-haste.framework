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
 * The listener for receiving a network metric.
 * If this listener was registered in transport, can receive a network metric periodically.
 * <strong>For performance testing purposes only!</strong>
 */
public interface MetricListener {
    /**
     * Get the period(milliseconds) which will be received a network metric.
     * @return The period which will be received a network metric.
     */
    long periodMilliseconds();

    /**
     * The callback which will be received a round trip time periodically.
     * @param peerCount The total count of current connected peers.
     * @param meanOfRoundTripTime The mean of round trip time.
     * @param meanOfRoundTripTimeDeviation The mean of round trip time deviation.
     */
    void onReceive(int peerCount, double meanOfRoundTripTime, double meanOfRoundTripTimeDeviation);
}
