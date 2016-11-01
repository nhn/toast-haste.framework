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
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.nhnent.haste.transport.udp;

import com.nhnent.haste.transport.TransportProxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

abstract class AbstractUDPTransportProxy implements TransportProxy {
    private final byte[] readBuf = new byte[2048];

    private final byte[] writeBuf = new byte[2048];

    private final int port;

    protected final UDPTransport transport;

    protected final DatagramChannel ch;

    protected final ByteBuffer readByteBuffer = ByteBuffer.wrap(readBuf);

    protected final ByteBuffer writeByteBuffer = ByteBuffer.wrap(writeBuf);

    public AbstractUDPTransportProxy(UDPTransport transport, DatagramChannel ch, int port) {
        this.transport = transport;
        this.ch = ch;
        this.port = port;
    }

    @Override
    public void accept(long currentTime) {
    }

    @Override
    public void read(long currentTime) {
    }

    @Override
    public void write(long currentTime) {
    }

    @Override
    public void send(long currentTime) {
    }

    @Override
    public void close() throws IOException {
        this.ch.close();
    }

    public int port() {
        return port;
    }
}