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

import com.nhnent.haste.objectpool.Handle;
import com.nhnent.haste.objectpool.ObjectPool;
import com.nhnent.haste.objectpool.Poolable;
import com.nhnent.haste.transport.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public final class IncomingCommand extends UDPCommand<IncomingCommand> implements Poolable {
    private static final Logger logger = LoggerFactory.getLogger(IncomingCommand.class);

    private static final int INITIAL_CREATE_COUNT = 1024;
    private Handle handle;

    private static final ObjectPool<IncomingCommand> pool = new ObjectPool<IncomingCommand>(true, INITIAL_CREATE_COUNT) {
        @Override
        protected IncomingCommand newInstance(Handle handle) {
            return new IncomingCommand(handle);
        }
    };

    private Payload payload;

    private long ackReceivedReliableSeq;

    private long ackReceivedSentTime;

    private final short MAX_COMMAND_BYTES = 1350;

    private final short MIN_COMMAND_BYTES = (short) CommandLength.MIN_SIZE.getLength();

    private long receivedTime;

    private IncomingCommand(Handle handle) {
        this.handle = handle;
    }

    private void init(long receivedTime, ByteBuffer byteBuffer) {
        this.payload = Payload.take();
        if (this.payload == null) {
            this.valid = false;
            return;
        }

        this.receivedTime = receivedTime;
        this.valid = true;

        byte ct = byteBuffer.get();
        this.commandType = CommandType.convert(ct);

        if (commandType == null) {
            logger.error("Invalid CommandType : {}", ct);
            return;
        }

        byte channel = byteBuffer.get();
        byte flag = byteBuffer.get();

        super.init(channel, flag);

        short commandBufferSize = byteBuffer.getShort();

        if (commandBufferSize > MAX_COMMAND_BYTES || commandBufferSize < MIN_COMMAND_BYTES) {
            logger.error("Invalid command bytes length : {}", commandBufferSize);
            valid = false;
            return;
        }

        reliableSeqNum = byteBuffer.getLong();

        switch (commandType) {
            case ACK: {
                ackReceivedReliableSeq = byteBuffer.getLong();
                ackReceivedSentTime = byteBuffer.getLong();
                break;
            }
            case DISCONNECT:
            case RELIABLE: {
                this.payload.setLength(commandBufferSize - CommandLength.RELIABLE_HEADER_SIZE.getLength());
                int offset = byteBuffer.position();
                payload.copyBytes(byteBuffer.array(), offset, this.payload.getLength());
                byteBuffer.position(offset + this.payload.getLength());
                break;
            }
            case UNRELIABLE: {
                unreliableSeqNum = byteBuffer.getLong();
                this.payload.setLength(commandBufferSize - CommandLength.UNRELIABLE_HEADER_SIZE.getLength());
                int offset = byteBuffer.position();
                payload.copyBytes(byteBuffer.array(), offset, this.payload.getLength());
                byteBuffer.position(offset + this.payload.getLength());
                break;
            }
            case FRAGMENT: {
                startSeqNum = byteBuffer.getLong();
                fragmentCount = byteBuffer.getShort();
                fragmentNum = byteBuffer.getShort();
                fragmentPayloadTotalLength = byteBuffer.getInt();
                fragmentOffset = byteBuffer.getInt();

                fragmentList.clear();

                for (int i = 0; i < fragmentCount; i++) {
                    fragmentList.set(i, false);
                }

                fragmentList.set(fragmentNum, true);

                this.payload.setLength(commandBufferSize - CommandLength.FRAGMENTED_HEADER_SIZE.getLength());
                int offset = byteBuffer.position();
                payload.copyBytes(byteBuffer.array(), offset, this.payload.getLength());

                break;
            }
            case PING:
            case EG_SERVER_TIME:
                break;
            default:
                valid = false;
                logger.error("Invalid CommandType");
                break;
        }
    }

    public static IncomingCommand newIncomingCommand(long receivedTime, ByteBuffer byteBuffer) {
        IncomingCommand command = pool.take();
        command.init(receivedTime, byteBuffer);
        return command;
    }

    public OutgoingCommand createAck(long serverSentTime) {
        byte[] payload = new byte[CommandLength.ACK_PAYLOAD.getLength()];
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
        byteBuffer.putLong(reliableSeqNum);
        byteBuffer.putLong(serverSentTime);

        return OutgoingCommand.newOutgoingCommand(CommandType.ACK, payload, payload.length, false, getChannelIndex());
    }

    public long getReceiveTime() {
        return receivedTime;
    }

    public long getAckReceivedReliableSeq() {
        assert commandType == CommandType.ACK;
        return ackReceivedReliableSeq;
    }

    public long getAckReceivedSentTime() {
        return ackReceivedSentTime;
    }

    public long getRoundTripTime() {
        assert commandType == CommandType.ACK;
        return receivedTime - ackReceivedSentTime;
    }

    public void setPayloadBuf(byte[] payload, int length) {
        assert commandType == CommandType.FRAGMENT;
        this.payload.setBytes(payload);
        this.payload.setLength(length);
    }

    public Payload getPayload() {
        return payload;
    }

    @Override
    public void release() {
        if (this.handle != null) {
            this.payload.release();
            this.handle.release();
        }
    }
}
