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

import java.util.List;
import java.util.Vector;

public abstract class UDPCommand<T extends UDPCommand> implements Comparable<T> {
    protected CommandType commandType;

    private byte channel;

    protected long reliableSeqNum;

    protected long unreliableSeqNum;

    private byte commandFlag;

    protected long startSeqNum;

    protected short fragmentCount;

    protected short fragmentNum;

    protected int fragmentPayloadTotalLength;

    protected int fragmentOffset;

    protected List<Boolean> fragmentList = new Vector<>();

    protected boolean valid = true;

    final static class CommandFlags {
        public static byte CF_UNRELIABLE = 0x00;
        public static byte CF_RELIABLE = 0x01;
        public static byte CF_ENCRYPT = 0x02;
    }

    protected void init(CommandType commandType, byte channel, boolean encrypt) {
        assert (this instanceof OutgoingCommand);

        this.commandType = commandType;
        this.channel = channel;

        byte flag = (commandType == CommandType.RELIABLE) ? CommandFlags.CF_RELIABLE :
                (commandType == CommandType.FRAGMENT) ? CommandFlags.CF_RELIABLE :
                        (commandType == CommandType.EG_SERVER_TIME) ? CommandFlags.CF_RELIABLE :
                                (commandType == CommandType.PING) ? CommandFlags.CF_RELIABLE : CommandFlags.CF_UNRELIABLE;

        if (encrypt) {
            flag |= CommandFlags.CF_ENCRYPT;
        }

        this.init(channel, flag);
    }

    protected void init(byte channel, byte commandFlag) {
        this.channel = channel;
        this.commandFlag = commandFlag;
    }

    protected void enableReliableFlag() {
    }

    protected void enableEncryptFlag() {
    }

    public boolean isReliable() {
        return (commandFlag & CommandFlags.CF_RELIABLE) == CommandFlags.CF_RELIABLE;
    }

    public boolean isEncrypted() {
        return (commandFlag & CommandFlags.CF_ENCRYPT) == CommandFlags.CF_ENCRYPT;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public byte getChannelIndex() {
        return channel;
    }

    protected byte getCommandFlag() {
        return commandFlag;
    }

    public long getReliableSeqNum() {
        return reliableSeqNum;
    }

    public void setUnreliableSeqNum(long value) {
        unreliableSeqNum = value;
    }

    public void setReliableSeqNum(long value) {
        reliableSeqNum = value;
    }

    public long getUnreliableSeqNum() {
        return unreliableSeqNum;
    }

    public boolean valid() {
        return valid;
    }

    public long getFragmentStartSeqNum() {
        assert commandType == CommandType.FRAGMENT;
        return startSeqNum;
    }

    public short getFragmentCount() {
        assert commandType == CommandType.FRAGMENT;
        return fragmentCount;
    }

    public short getFragmentNum() {
        assert commandType == CommandType.FRAGMENT;
        return fragmentNum;
    }

    public int getFragmentPayloadTotalLength() {
        assert commandType == CommandType.FRAGMENT;
        return fragmentPayloadTotalLength;
    }

    public int getFragmentOffset() {
        assert commandType == CommandType.FRAGMENT;
        return fragmentOffset;
    }

    @Override
    public int compareTo(T other) {
        if (reliableSeqNum == other.reliableSeqNum) {
            if (unreliableSeqNum == other.unreliableSeqNum)
                return 0;
            else if (unreliableSeqNum > other.unreliableSeqNum)
                return 1;
            else
                return -1;

        } else if (reliableSeqNum > other.reliableSeqNum) {
            return 1;
        } else {
            return -1;
        }
    }

}
