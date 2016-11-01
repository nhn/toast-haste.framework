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

package com.nhnent.haste.bootstrap;

import com.nhnent.haste.bootstrap.loop.EventLoopGroup;
import com.nhnent.haste.bootstrap.loop.NioEventLoopGroup;
import com.nhnent.haste.bootstrap.options.TCPConfig;
import com.nhnent.haste.bootstrap.options.TCPOption;
import com.nhnent.haste.bootstrap.options.UDPConfig;
import com.nhnent.haste.bootstrap.options.UDPOption;
import com.nhnent.haste.common.Check;
import com.nhnent.haste.transport.Application;
import com.nhnent.haste.transport.MetricListener;
import com.nhnent.haste.transport.tcp.TCPTransport;
import com.nhnent.haste.transport.udp.UDPTransport;

import java.io.IOException;

/**
 * {@link GameServerBootstrap} is a helper class in order to easy to start server application.
 */
public class GameServerBootstrap {
    private final UDPConfig udpConfig;
    private final TCPConfig tcpConfig;

    private Application application;
    private UDPTransport udpTransport;
    private TCPTransport tcpTransport;

    private EventLoopGroup udpEventGroup;
    private EventLoopGroup tcpEvenGroup;

    private MetricListener metricListener;

    /**
     * Default constructor of {@link GameServerBootstrap}. Configurations of transports are filled with default values.
     */
    public GameServerBootstrap() {
        this(new UDPConfig(), new TCPConfig());
    }

    /**
     * A constructor of {@link GameServerBootstrap}. Configurations of transports are filled with parameterized values.
     *
     * @param udpConfig A configuration of the UDP transport.
     * @param tcpConfig A configuration of the TCP transport.
     */
    public GameServerBootstrap(UDPConfig udpConfig, TCPConfig tcpConfig) {
        this.udpConfig = udpConfig;
        this.tcpConfig = tcpConfig;
    }

    /**
     * Put a UDP option.
     *
     * @param option The key of option. Reference to static fields of {@link UDPOption}
     * @param value  The value of option.
     * @param <T>    The type of option.
     * @return A reference of this {@link GameServerBootstrap} object for method chaining.
     * @see UDPOption
     */
    public <T> GameServerBootstrap option(UDPOption<T> option, T value) {
        udpConfig.option(option, value);
        return this;
    }

    /**
     * Put a TCP option.
     *
     * @param option The key of option. Reference to static fields of {@link TCPOption}
     * @param value  The value of option.
     * @param <T>    The type of option.
     * @return A reference of this {@link GameServerBootstrap} object for method chaining.
     */
    public <T> GameServerBootstrap option(TCPOption<T> option, T value) {
        tcpConfig.option(option, value);
        return this;
    }

    /**
     * Set a application.
     *
     * @param application A implemented application.
     * @return A reference of this {@link GameServerBootstrap} object for method chaining.
     */
    public GameServerBootstrap application(Application application) {
        this.application = application;
        return this;
    }

    /**
     * Set a listen port.
     *
     * @param inetPort The listen port of binding.
     * @return A reference of this {@link GameServerBootstrap} object for method chaining.
     */
    public GameServerBootstrap bind(int inetPort) {
        udpConfig.option(UDPOption.LISTEN_PORT, inetPort);
        tcpConfig.option(TCPOption.LISTEN_PORT, inetPort);
        return this;
    }

    /**
     * Set a metric listener for receiving metrics.
     *
     * @param metricListener A implemented metric listener.
     * @return A reference of this {@link GameServerBootstrap} object for method chaining.
     */
    public GameServerBootstrap metricListener(MetricListener metricListener) {
        this.metricListener = metricListener;
        return this;
    }

    /**
     * Start a game server.
     *
     * @return {@code true} if it was success to start server {@code false} otherwise.
     */
    public boolean start() {
        Check.NotNull(application, "application");

        startUDP();
        //startTCP();

        return true;
    }

    /**
     * Closes all resources, and stop event loops.
     *
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException {
        udpEventGroup.close();
    }

    //private void startTCP() {
    //}

    private void startUDP() {
        UDPTransport.Builder builder = new UDPTransport.Builder()
                .application(application)
                .listenPort(udpConfig.option(UDPOption.LISTEN_PORT))
                .clientStartPort(udpConfig.option(UDPOption.ClIENT_PORT))
                .sendBufSize(udpConfig.option(UDPOption.SO_SNDBUF))
                .recvBufSize(udpConfig.option(UDPOption.SO_RCVBUF))
                .maxConnnection(udpConfig.option(UDPOption.MAX_CONNNECTION))
                .threadCount(udpConfig.option(UDPOption.THREAD_COUNT));

        if (this.metricListener != null) {
            builder.metricListener(this.metricListener);
        }

        udpTransport = builder.build();

        udpEventGroup = new NioEventLoopGroup(udpTransport);

        udpEventGroup.start();
    }
}
