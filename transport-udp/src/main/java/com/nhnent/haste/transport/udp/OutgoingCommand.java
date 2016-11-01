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

import com.nhnent.haste.common.ByteWrite;

public class OutgoingCommand extends UDPCommand<OutgoingCommand> {
    public final static int MAX_RESEND_COUNT = 3;

    private volatile int sentCount = 0;
    private long rto;

    private boolean serialized = false;
    private byte[] serializedArray = new byte[2048];
    private short serializedArrayLength;

    private long sentTime;

    private int headerLength;
    private long timeout;

    public OutgoingCommand() {
    }

    private void init(CommandType commandType, byte[] payload, int payloadLength, boolean encrypt, byte channel) {
        super.init(commandType, channel, encrypt);

        this.sentCount = 0;
        this.serialized = false;

        headerLength = commandType == CommandType.ACK ? CommandLength.ACK_HEADER_SIZE.getLength() :
                commandType == CommandType.DISCONNECT ? CommandLength.DISCONNECT_HEADER_SIZE.getLength() :
                        commandType == CommandType.RELIABLE ? CommandLength.RELIABLE_HEADER_SIZE.getLength() :
                                commandType == CommandType.UNRELIABLE ? CommandLength.UNRELIABLE_HEADER_SIZE.getLength() :
                                        commandType == CommandType.FRAGMENT ? CommandLength.FRAGMENTED_HEADER_SIZE.getLength() : -1;

        if (headerLength < 0)
            return;

        int length = headerLength + payloadLength;

        if (length > serializedArray.length)
            serializedArray = new byte[length];

        serializedArrayLength = (short) length;

        ByteWrite.set(payload, 0, payloadLength, serializedArray, headerLength);
    }

    public static OutgoingCommand newOutgoingCommand(CommandType commandType, byte[] payload, int payloadLength, boolean encrypt, byte channel) {
        OutgoingCommand command = new OutgoingCommand();
        command.init(commandType, payload, payloadLength, encrypt, channel);
        return command;
    }

    public void setFragmentNum(short fragmentNum) {
        assert commandType == CommandType.FRAGMENT;
        this.fragmentNum = fragmentNum;
    }

    public void setStartSeqNum(long startSeqNum) {
        assert commandType == CommandType.FRAGMENT;
        this.startSeqNum = startSeqNum;
    }

    public void setFragmentCount(short fragmentCount) {
        assert commandType == CommandType.FRAGMENT;
        this.fragmentCount = fragmentCount;
    }

    public void setFragmentPayloadTotalLength(int fragmentPayloadTotalLength) {
        assert commandType == CommandType.FRAGMENT;
        this.fragmentPayloadTotalLength = fragmentPayloadTotalLength;
    }

    public void setFragmentOffset(int fragmentOffset) {
        assert commandType == CommandType.FRAGMENT;
        this.fragmentOffset = fragmentOffset;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void increaseSentCount() {
        sentCount++;
    }

    public short getSerializedArrayLength() {
        return serializedArrayLength;
    }

    public byte[] serialize() {
        if (serialized)
            return serializedArray;

        int offset = 0;

        serializedArray[offset++] = commandType.getByte();
        serializedArray[offset++] = getChannelIndex();
        serializedArray[offset++] = getCommandFlag();

        offset = ByteWrite.set(serializedArrayLength, serializedArray, offset);
        offset = ByteWrite.set(getReliableSeqNum(), serializedArray, offset);

        switch (commandType) {
            case UNRELIABLE: {
                offset = ByteWrite.set(getUnreliableSeqNum(), serializedArray, offset);
                break;
            }
            case FRAGMENT: {
                offset = ByteWrite.set(getFragmentStartSeqNum(), serializedArray, offset);
                offset = ByteWrite.set(getFragmentCount(), serializedArray, offset);
                offset = ByteWrite.set(getFragmentNum(), serializedArray, offset);
                offset = ByteWrite.set(getFragmentPayloadTotalLength(), serializedArray, offset);
                offset = ByteWrite.set(getFragmentOffset(), serializedArray, offset);
                break;
            }
        }

        assert offset == headerLength;

        serialized = true;

        return serializedArray;
    }

    public void setRetransmissionTimeout(long sentTime, long rtt, long deviation) {
        rto = sentTime + (rtt + (deviation << 2)) * sentCount;
    }

    public long getRetransmissionTimeout() {
        return rto;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
