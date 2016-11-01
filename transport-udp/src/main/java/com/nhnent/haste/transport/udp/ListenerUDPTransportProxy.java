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

import com.nhnent.haste.common.CRC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.zip.CRC32;

final class ListenerUDPTransportProxy extends AbstractUDPTransportProxy {
    private static final Logger logger = LoggerFactory.getLogger(ListenerUDPTransportProxy.class);

    private final CRC32 crc32 = new CRC32();

    private EndPointCache endPointCache = EndPointCache.getInstance();

    public ListenerUDPTransportProxy(UDPTransport transport, DatagramChannel ch, int port) {
        super(transport, ch, port);
        readByteBuffer.order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public void read(long currentTime) {
        try {
            SocketAddress sa = ch.receive(readByteBuffer);

            if (endPointCache.exist(currentTime, sa)) {
                ConnectionInfo info = endPointCache.getInfo(sa);

                if (info == null)
                    return;

                sendResponseDuplicatedRequest(sa, currentTime, info);

            } else {
                ConnectionInfo info = transport.accept(currentTime, sa, readByteBuffer);

                if (info == null)
                    return;

                endPointCache.addEndPoint(sa, currentTime, info);
            }

        } catch (IOException e) {
            logger.error("Failed to read data in ListenerUDPTransportProxy", e);
        } finally {
            readByteBuffer.clear();
        }
    }

    private void sendResponseDuplicatedRequest(SocketAddress sa, long currentTime, ConnectionInfo connectionInfo) {
        readByteBuffer.position(0);

        if (CommandType.CONNECT.isNotEqual(readByteBuffer.get()))  //1
            return;

        int version = readByteBuffer.getInt();             //4

        if (!transport.validateProtocolVersion(version))
            return;

        long sendingTime = readByteBuffer.getLong();        //8

        sendConnectionResponse(sa,
                connectionInfo,
                sendingTime,
                currentTime);
    }

    void sendConnectionResponse(SocketAddress target,
                                        ConnectionInfo connectionInfo,
                                        long sendingTime,
                                        long responseTime) {
        writeByteBuffer.clear();

        byte[] serverKey = connectionInfo.getServerKeyNumber().toByteArray();

        writeByteBuffer.put(CommandType.CONNECT_RESPONSE.getByte());   //1
        writeByteBuffer.putInt(connectionInfo.getPeerID());     //4
        writeByteBuffer.put((byte) serverKey.length);
        writeByteBuffer.put(serverKey);
        writeByteBuffer.putInt(connectionInfo.getPort());       //4
        writeByteBuffer.putLong(sendingTime);                   //8
        writeByteBuffer.putLong(responseTime);                  //8

        CRC.Write(crc32, writeByteBuffer.array(),
                writeByteBuffer.position() + CRC.CRC_LENGTH,
                writeByteBuffer.position());

        writeByteBuffer.position(writeByteBuffer.position() + CRC.CRC_LENGTH);

        try {
            writeByteBuffer.flip();
            ch.send(writeByteBuffer, target);
        } catch (IOException e) {
            logger.error("Failed to send connection response", e);
        }
    }
}