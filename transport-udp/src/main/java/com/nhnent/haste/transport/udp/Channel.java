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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class Channel {
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);

    public final static int MAX_COMMAND_SIZE = 1000;

    private final static int INITIAL_SEQ = 0;
    private final int channelNumber;

    private volatile long incomingReliableSeqNum = INITIAL_SEQ;
    private volatile long incomingUnreliableSeqNum = INITIAL_SEQ;

    private volatile long outgoingReliableSeqNum = INITIAL_SEQ;
    private volatile long outgoingUnreliableSeqNum = INITIAL_SEQ;

    private Map<Long, IncomingCommand> incomingReliableCommandList = new HashMap<>(MAX_COMMAND_SIZE);
    private Map<Long, IncomingCommand> incomingUnreliableCommandList = new HashMap<>(MAX_COMMAND_SIZE);

    private Object syncRoot = new Object();

    private Queue<OutgoingCommand> toPassOutgoingReliableCommandList = new ArrayDeque<>(MAX_COMMAND_SIZE);
    private Queue<OutgoingCommand> toPassOutgoingUnreliableCommandList = new ArrayDeque<>(MAX_COMMAND_SIZE);

    private Queue<OutgoingCommand> outgoingReliableCommandList = new ArrayDeque<>(MAX_COMMAND_SIZE);
    private Queue<OutgoingCommand> outgoingUnreliableCommandList = new ArrayDeque<>(MAX_COMMAND_SIZE);

    private Map<Long, OutgoingCommand> sentReliableCommands = new HashMap<>();

    public Channel(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public long getIncomingReliableSeqNum() {
        return incomingReliableSeqNum;
    }

    public long getIncomingUnreliableSeqNum() {
        return incomingUnreliableSeqNum;
    }

    public boolean containIncomingReliableSeqNum(long reliableSeq) {
        return incomingReliableCommandList.containsKey(reliableSeq);
    }

    public boolean containIncomingUnreliableCommand(IncomingCommand command) {
        return incomingUnreliableCommandList.containsKey(command.getUnreliableSeqNum());
    }

    public void insertIncomingReliableCommand(IncomingCommand command) {
        incomingReliableCommandList.put(command.getReliableSeqNum(), command);
    }

    public void insertIncomingUnreliableCommand(IncomingCommand command) {
        incomingUnreliableCommandList.put(command.getUnreliableSeqNum(), command);
    }

    public int incomingReliableCommandCount() {
        return incomingReliableCommandList.size();
    }

    public int incomingUnreliableCommandCount() {
        return incomingUnreliableCommandList.size();
    }

    public int outgoingReliableCommandCount() {
        return outgoingReliableCommandList.size();
    }

    public int outgoingUnreliableCommandCount() {
        return outgoingUnreliableCommandList.size();
    }

    public Set<Long> getIncomingUnreliableCommandSeqs() {
        return incomingUnreliableCommandList.keySet();
    }

    public IncomingCommand getIncomingReliableCommand(long reliableSeq) {
        return incomingReliableCommandList.get(reliableSeq);
    }

    public IncomingCommand getIncomingUnreliableCommand(long unreliableSeq) {
        return incomingUnreliableCommandList.get(unreliableSeq);
    }

    public void removeIncomingUnreliableCommand(long unreliableSeq) {
        incomingUnreliableCommandList.remove(unreliableSeq);
    }

    public void removeIncomingReliableCommand(long reliableSeq) {
        incomingReliableCommandList.remove(reliableSeq);
    }

    public Queue<OutgoingCommand> getOutgoingReliableCommandList() {
        synchronized (syncRoot) {
            Queue<OutgoingCommand> tmp = outgoingReliableCommandList;
            outgoingReliableCommandList = toPassOutgoingReliableCommandList;
            outgoingReliableCommandList.clear();
            toPassOutgoingReliableCommandList = tmp;
            return tmp;
        }
    }

    public Queue<OutgoingCommand> getOutgoingUnreliableCommandQueue() {
        synchronized (syncRoot) {
            Queue<OutgoingCommand> tmp = outgoingUnreliableCommandList;
            outgoingUnreliableCommandList = toPassOutgoingUnreliableCommandList;
            outgoingUnreliableCommandList.clear();
            toPassOutgoingUnreliableCommandList = tmp;
            return tmp;
        }
    }

    public void insertOutgoingReliableCommand(OutgoingCommand command) {
        synchronized (syncRoot) {
            outgoingReliableCommandList.add(command);
        }
    }

    public void insertOutgoingUnreliableCommand(OutgoingCommand command) {
        synchronized (syncRoot) {
            outgoingUnreliableCommandList.add(command);
        }
    }

    public void setIncomingUnreliableSeqNum(long incomingUnreliableSeqNum) {
        this.incomingUnreliableSeqNum = incomingUnreliableSeqNum;
    }

    public void setIncomingReliableSeqNum(long incomingReliableSeqNum) {
        this.incomingReliableSeqNum = incomingReliableSeqNum;
    }

    public long increaseOutgoingReliableSeqNum() {
        return this.outgoingReliableSeqNum += 1;
    }

    public long increaseOutgoingUnreliableSeqNum() {
        return this.outgoingUnreliableSeqNum += 1;
    }

    public long getOutgoingReliableSeqNum() {
        return outgoingReliableSeqNum;
    }

    public void addSentReliableCommand(OutgoingCommand command) {
        sentReliableCommands.put(command.getReliableSeqNum(), command);
    }

    public boolean removeSentReliableCommand(long reliableSeqNum) {
        return sentReliableCommands.remove(reliableSeqNum) != null;
    }

    public boolean resend(long currentTime) {

        for (OutgoingCommand command : sentReliableCommands.values()) {
            long rto = command.getRetransmissionTimeout();

            if (command.getSentCount() > OutgoingCommand.MAX_RESEND_COUNT) {
                logger.debug("Failed to resend, relSeq[{}] currentTime[{}] rto[{}] sentTime[{}] sentCount[{}]",
                        command.getReliableSeqNum(), currentTime, rto, command.getSentTime(), command.getSentCount());
                return false;
            }

            if (currentTime > rto) {
                insertOutgoingReliableCommand(command);
            }
        }

        return true;
    }
}
