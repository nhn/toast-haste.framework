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

package com.nhnent.haste.example.echoserver;

import com.nhnent.haste.bootstrap.GameServerBootstrap;
import com.nhnent.haste.bootstrap.options.UDPOption;
import com.nhnent.haste.transport.MetricListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class EchoServer {
    private static final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private static final int PORT = 5056;

    public static void main(String[] args) {
        logger.info("echo server started");

        GameServerBootstrap bootstrap = new GameServerBootstrap();

        bootstrap.application(new EchoServerApplication())
                .option(UDPOption.THREAD_COUNT, 2)
                .option(UDPOption.SO_RCVBUF, 1024)
                .option(UDPOption.SO_SNDBUF, 1024)
                .metricListener(new MetricListener() {
                    @Override
                    public long periodMilliseconds() {
                        return TimeUnit.MINUTES.toMillis(1);
                    }

                    @Override
                    public void onReceive(int peerCount, double meanOfRoundTripTime, double meanOfRoundTripTimeDeviation) {
                        logger.info("Peer : {}, RTT : {}, RTT-D : {}", peerCount, meanOfRoundTripTime, meanOfRoundTripTimeDeviation);
                    }
                }).bind(PORT).start();
    }
}
