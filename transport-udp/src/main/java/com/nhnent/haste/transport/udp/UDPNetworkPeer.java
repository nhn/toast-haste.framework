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

import com.nhnent.haste.common.*;
import com.nhnent.haste.security.BigInteger;
import com.nhnent.haste.transport.AbstractNetworkPeer;
import com.nhnent.haste.transport.DisconnectReason;
import com.nhnent.haste.transport.QoS;
import com.nhnent.haste.transport.state.ConnectionState;
import com.nhnent.haste.transport.state.Disconnected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.CRC32;

public final class UDPNetworkPeer extends AbstractNetworkPeer implements TransportPeer {
    private static final Logger logger = LoggerFactory.getLogger(UDPNetworkPeer.class);

    private final byte[] writeBuf = new byte[2048];
    private final ByteBuffer writeByteBuffer = ByteBuffer.wrap(writeBuf);

    private int udpBufferIndex = 0;
    private short udpCommandCount = 0;

    private static final int MAX_COMMANDS_ON_MTU = 100;
    private static final int WARNING_SIZE = 100;
    private static final int SENT_COUNT_ALLOWANCE = 3;

    private SocketAddress socketAddress;

    private final int MTU;
    private final int DISCONNECT_TIMEOUT;
    private final int CHANNEL_COUNT;
    private final byte LAST_CHANNEL;

    private final boolean isCRCEnabled;

    private int packetLossByCRC = 0;
    private CRC32 crc32 = new CRC32();

    private long lastSentData = 0;

    private long SEND_INTERVAL_MILLISECOUNDS = 20;

    private List<Disposable> cleanUpEvents = Collections.synchronizedList(new ArrayList<Disposable>());

    private final List<IncomingCommand> receivedCommands = new Vector<>(MAX_COMMANDS_ON_MTU);

    private final List<OutgoingCommand> outgoingAckList = new Vector<>(MAX_COMMANDS_ON_MTU);

    private final Map<Byte, Channel> channels;

    private UDPNetworkPeer(Builder builder) {
        super(builder, EnvironmentTimer.currentTimeMillis());

        this.MTU = builder.Mtu;
        this.DISCONNECT_TIMEOUT = builder.disconnectionTimeout;
        this.CHANNEL_COUNT = builder.channelCount;

        this.isCRCEnabled = builder.enableCRC;
        this.socketAddress = builder.socketAddress;

        channels = new LinkedHashMap<>(CHANNEL_COUNT);

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            channels.put((byte) i, new Channel(i));
        }

        LAST_CHANNEL = (byte) (CHANNEL_COUNT - 1);
    }

    @Override
    public boolean enqueueOutgoingCommand(byte[] payload, int payloadLength, byte channelIndex, boolean encrypt, QoS qos) {
        if (getConnectionState().isNotEqual(ConnectionState.CONNECTED)) {
            if (logger.isDebugEnabled())
                logger.debug("PeerID[{}] is not connected [{}]", peerID, getConnectionState());
            return false;
        }

        CommandType commandType = (qos == QoS.RELIABLE_SEQUENCED) ? CommandType.RELIABLE : CommandType.UNRELIABLE;

        return createAndEnqueueCommand(commandType, payload, payloadLength, channelIndex, encrypt);
    }

    private boolean createAndEnqueueCommand(CommandType commandType, byte[] payload, int payloadLength, byte channelIndex, boolean encrypt) {
        Channel channel = channels.get(channelIndex);

        if (channel == null) {
            if (logger.isDebugEnabled())
                logger.debug("[{}] channel is not found", channelIndex);
            return false;
        }

        int maximumPayloadSize = MTU - (CommandLength.UDP_MTU_HEADER_LENGTH.getLength() + CommandLength.FRAGMENTED_HEADER_SIZE.getLength());

        if (payloadLength < maximumPayloadSize) {
            OutgoingCommand command = OutgoingCommand.newOutgoingCommand(commandType, payload, payloadLength, encrypt, channelIndex);
            return queueOutgoingCommand(channel, command);
        } else { //FRAGMENT
            short fragmentCount = (short) ((payloadLength + maximumPayloadSize - 1) / maximumPayloadSize);
            long startSequenceNumber = channel.getOutgoingReliableSeqNum() + 1;

            short fragmentSeq = 0;

            for (int offset = 0; offset < payloadLength; offset += maximumPayloadSize) {
                if (payloadLength - offset < maximumPayloadSize) {
                    //Specify last fragment's payload
                    maximumPayloadSize = payloadLength - offset;
                }

                byte[] fragment = new byte[maximumPayloadSize];
                System.arraycopy(payload, offset, fragment, 0, maximumPayloadSize);

                OutgoingCommand command = OutgoingCommand.newOutgoingCommand(CommandType.FRAGMENT, fragment, fragment.length, encrypt, channelIndex);

                command.setFragmentNum(fragmentSeq++);
                command.setStartSeqNum(startSequenceNumber);
                command.setFragmentCount(fragmentCount);
                command.setFragmentOffset(offset);
                command.setFragmentPayloadTotalLength(payloadLength);

                boolean result = queueOutgoingCommand(channel, command);

                if (!result)
                    return false;
            }

            return true;
        }
    }

    private boolean queueOutgoingCommand(Channel channel, OutgoingCommand command) {
        assert command.getSentCount() == 0;

        if (command.isReliable()) {

            int queueSize = channel.outgoingReliableCommandCount();

            if (queueSize > Channel.MAX_COMMAND_SIZE) {
                return false;
            }

            command.setReliableSeqNum(channel.increaseOutgoingReliableSeqNum());

            channel.insertOutgoingReliableCommand(command);

        } else {    //CF_UNRELIABLE

            command.setReliableSeqNum(channel.getOutgoingReliableSeqNum());
            command.setUnreliableSeqNum(channel.increaseOutgoingUnreliableSeqNum());

            channel.insertOutgoingUnreliableCommand(command);
        }

        return true;
    }

    @Override
    public int getPeerID() {
        return super.getPeerID();
    }

    private static class ChannelQueue {
        Queue<OutgoingCommand> unreliableCommandQueue;
        Queue<OutgoingCommand> reliableCommandQueue;
        Channel channel;
    }

    @Override
    public void send(DatagramChannel ch, long currentTime) {
        if (getConnectionState().isEqual(ConnectionState.DISCONNECTED))
            return;

        if (lastSentData + SEND_INTERVAL_MILLISECOUNDS > currentTime) {
            return;
        } else {
            lastSentData = currentTime;
        }

        //Check valid connection state.
        if (currentTime > timestampOfLastReceive + DISCONNECT_TIMEOUT) {
            long interval = currentTime - timestampOfLastReceive;
            if (logger.isDebugEnabled())
                logger.debug("PeerID[{}] Disconnect because valid command is not coming from client during [{}]", peerID, interval);
            disconnect(DisconnectReason.TIMEOUT_DISCONNECT, "Cannot receive any command during " + DISCONNECT_TIMEOUT);
        }

        // the resendCommand method must be called before a channelQueueList was made.
        // then, a channel which contains a resend command is added in a channelQueueList.
        resendCommand(currentTime);

        Queue<ChannelQueue> channelQueueList = new ArrayBlockingQueue<>(channels.size());

        for (Channel channel : channels.values()) {
            ChannelQueue queue = new ChannelQueue();

            queue.unreliableCommandQueue = channel.getOutgoingUnreliableCommandQueue();
            queue.reliableCommandQueue = channel.getOutgoingReliableCommandList();
            queue.channel = channel;

            if (queue.unreliableCommandQueue.size() > 0 || queue.reliableCommandQueue.size() > 0)
                channelQueueList.add(queue);
        }

        int commandLeft;

        do {
            commandLeft = 0;

            writeByteBuffer.clear();

            udpBufferIndex = 0;
            udpCommandCount = 0;

            udpBufferIndex = CommandLength.UDP_MTU_HEADER_LENGTH.getLength();

            sendAck();

            while (!channelQueueList.isEmpty()) {

                ChannelQueue channelQueue = channelQueueList.peek();

                commandLeft = serializeToBuffer(currentTime, channelQueue.unreliableCommandQueue, channelQueue.channel);
                commandLeft += serializeToBuffer(currentTime, channelQueue.reliableCommandQueue, channelQueue.channel);

                if (commandLeft > 0) {
                    break;
                } else if (commandLeft == 0) {
                    channelQueueList.poll();
                }
            }

            if (udpCommandCount > 0) {
                goOut(ch, currentTime);
            }

        } while (commandLeft > 0);


        if (getConnectionState().isEqual(ConnectionState.DISCONNECTING)) {
            getConnectionState().transitDisconnected(this);
            onDisconnected(this.disconnectReason, this.disconnectDetail);
        }
    }

    private void goOut(DatagramChannel ch, long currentTime) {
        int offset = 0;

        offset = ByteWrite.setByte(CommandType.MESSAGES.getByte(), writeBuf, offset);
        offset = ByteWrite.setInt(getPeerID(), writeBuf, offset);
        offset = ByteWrite.setLong(currentTime, writeBuf, offset);
        offset = ByteWrite.setShort(udpCommandCount, writeBuf, offset);

        if (isCRCEnabled) {
            CRC.Write(crc32, writeBuf, udpBufferIndex, offset);
        }

        offset += CRC.CRC_LENGTH;

        assert offset == CommandLength.UDP_MTU_HEADER_LENGTH.getLength();

        writeByteBuffer.position(udpBufferIndex);

        try {
            writeByteBuffer.flip();

            int sentBytes = ch.send(writeByteBuffer, socketAddress);

            if (udpBufferIndex != sentBytes) {
                if (logger.isDebugEnabled())
                    logger.debug("sent bytes: {}", sentBytes);
            }

        } catch (IOException e) {
            logger.error("failed to send!", e);
        }
    }

    private int serializeToBuffer(long currentTime, Queue<OutgoingCommand> commandQueue, Channel channel) {
        while (commandQueue.size() > 0) {

            OutgoingCommand outgoingCommand = commandQueue.peek();

            int length = outgoingCommand.getSerializedArrayLength();

            if ((udpBufferIndex + length) > MTU) {
                break;
            } else {
                commandQueue.poll();
            }

            outgoingCommand.serialize();

            udpBufferIndex = ByteWrite.set(outgoingCommand.serialize(), 0, length, writeBuf, udpBufferIndex);
            udpCommandCount++;

            if (outgoingCommand.isReliable()) {
                queueSentReliableCommand(currentTime, channel, outgoingCommand);
            }
        }

        return commandQueue.size();
    }

    private void queueSentReliableCommand(long currentTime, Channel channel, OutgoingCommand outgoingCommand) {
        outgoingCommand.setSentTime(currentTime);
        outgoingCommand.increaseSentCount();

        outgoingCommand.setRetransmissionTimeout(currentTime, getMeanOfRoundTripTime(), getMeanOfRoundTripTimeDeviation());

        if (outgoingCommand.getSentCount() == 1) {
            outgoingCommand.setTimeout(currentTime + DISCONNECT_TIMEOUT);
            channel.addSentReliableCommand(outgoingCommand);
        }
    }

    private void resendCommand(long currentTime) {
        for (Channel channel : channels.values()) {
            if (!channel.resend(currentTime)) {
                disconnect(DisconnectReason.TIMEOUT_DISCONNECT, "timeout");
            }
        }
    }

    public void sendAck() {
        if (0 == outgoingAckList.size())
            return;

        short ackCommandCount = 0;

        List<OutgoingCommand> removeList = new LinkedList<>();
        for (OutgoingCommand command : outgoingAckList) {
            byte[] array = command.serialize();
            int length = command.getSerializedArrayLength();

            if (udpBufferIndex + length >= MTU) {
                break;
            }

            removeList.add(command);
            ackCommandCount++;

            System.arraycopy(array, 0, writeBuf, udpBufferIndex, length);
            udpBufferIndex += length;
        }

        for (int i = 0; i < removeList.size(); i++) {
            if (outgoingAckList.contains(removeList.get(i))) {
                outgoingAckList.remove(removeList.get(i));
            }
        }
        removeList.clear();

        int queueSize = outgoingAckList.size();
        if (queueSize > 0 && (queueSize % WARNING_SIZE) == 0) {
            if (logger.isDebugEnabled())
                logger.debug("[{}] outgoingAckList Count [{}]", peerID, queueSize);
        }

        udpCommandCount += ackCommandCount;
    }

    public static final class Builder extends AbstractNetworkPeer.AbstractBuilder<Builder> {
        private final int CHANNEL_COUNT_MAX = 100;
        private final int CHANNEL_COUNT_MIN = 5;
        private final int MIN_MTU_SIZE = 400;

        private SocketAddress socketAddress = null;
        private int Mtu = MIN_MTU_SIZE;
        private int disconnectionTimeout = -1;
        private boolean enableCRC = false;
        private int channelCount = CHANNEL_COUNT_MIN;

        public Builder() {
        }

        public Builder socketAddress(SocketAddress socketAddress) {
            this.socketAddress = socketAddress;
            return this;
        }

        public Builder mtu(int Mtu) {
            this.Mtu = Mtu;
            return this;
        }

        public Builder disconnectTimeout(int disconnectionTimeout) {
            this.disconnectionTimeout = disconnectionTimeout;
            return this;
        }

        public Builder enableCRC(boolean enableCRC) {
            this.enableCRC = enableCRC;
            return this;
        }

        public Builder channelCount(int channelCount) {
            this.channelCount = channelCount < CHANNEL_COUNT_MIN ? CHANNEL_COUNT_MIN :
                    channelCount > CHANNEL_COUNT_MAX ? CHANNEL_COUNT_MAX : channelCount;
            return this;
        }

        public UDPNetworkPeer build() {
            return new UDPNetworkPeer(this);
        }
    }

    public BigInteger getServerKey() {
        return super.serverKeyNumber;
    }

    @Override
    public void addDisposeEvent(Disposable disposeEvent) {
        cleanUpEvents.add(disposeEvent);
    }

    private final Map<Integer, Channel> temporalChannelList = new HashMap<>();

    @Override
    public void onReceive(long currentTime, SocketAddress socketAddress, ByteBuffer byteBuffer, int transferredBytes) {
        if (Disconnected.instance == getConnectionState()) {
            return;
        }

        if (this.socketAddress != socketAddress) {
            this.socketAddress = socketAddress;
        }

        long serverSentTime = byteBuffer.getLong();     //8
        long commandCount = byteBuffer.getShort();      //2

        if (isCRCEnabled) {
            if (!CRC.Check(crc32, byteBuffer.array(), transferredBytes, byteBuffer.position())) {
                logger.debug("Invalid CRC value");
                packetLossByCRC++;

                if ((packetLossByCRC > 0) && (packetLossByCRC % 100) == 0) {
                    if (logger.isDebugEnabled())
                        logger.debug("[{}] CRC error count[{}] address[{}]", peerID, packetLossByCRC, socketAddress.toString());
                }

                return;
            }
        }

        byteBuffer.position(byteBuffer.position() + CRC.CRC_LENGTH);

        if (commandCount > MAX_COMMANDS_ON_MTU || commandCount <= 0) {
            disconnect(DisconnectReason.INVALID_DATA_FORMAT, "Invalid Command Count : " + commandCount);
        }

        timestampOfLastReceive = currentTime;

        try {
            for (int i = 0; i < commandCount; i++) {
                IncomingCommand command = IncomingCommand.newIncomingCommand(timestampOfLastReceive, byteBuffer);

                if (!command.valid()) {
                    disconnect(DisconnectReason.INVALID_DATA_FORMAT, "Invalid commandType");
                    return;
                }

                if (command.isReliable()) {
                    OutgoingCommand ack = command.createAck(serverSentTime);
                    outgoingAckList.add(ack);
                }

                receivedCommands.add(command);
            }

            for (IncomingCommand command : receivedCommands) {
                Channel channel = chooseChannelToProcess(command);

                if (channel != null)
                    temporalChannelList.put(channel.getChannelNumber(), channel);
            }

            for (Channel channel : temporalChannelList.values()) {
                processChannel(channel);
            }

        } finally {
            receivedCommands.clear();
            temporalChannelList.clear();
        }
    }

    private void processChannel(Channel channel) {
        IncomingCommand targetCommand;

        do {
            targetCommand = null;

            if (channel.incomingUnreliableCommandCount() > 0) {
                long minimumUnreliableSeq = Long.MAX_VALUE;

                Set<Long> seqs = channel.getIncomingUnreliableCommandSeqs();

                for (Long seq : seqs) {
                    IncomingCommand command = channel.getIncomingUnreliableCommand(seq);

                    if (command == null)
                        continue;

                    long reliableSeq = command.getReliableSeqNum();
                    long unreliableSeq = command.getUnreliableSeqNum();

                    if (unreliableSeq < minimumUnreliableSeq && reliableSeq == channel.getIncomingReliableSeqNum()) {
                        minimumUnreliableSeq = unreliableSeq;
                    }
                }

                if (minimumUnreliableSeq < Long.MAX_VALUE) {
                    targetCommand = channel.getIncomingUnreliableCommand(minimumUnreliableSeq);

                    if (targetCommand != null) {
                        channel.removeIncomingUnreliableCommand(targetCommand.getUnreliableSeqNum());
                        channel.setIncomingUnreliableSeqNum(targetCommand.getUnreliableSeqNum());
                    }
                }
            }

            if (targetCommand == null && channel.incomingReliableCommandCount() > 0) {

                long expectedReliableSeq = channel.getIncomingReliableSeqNum() + 1;

                if (!channel.containIncomingReliableSeqNum(expectedReliableSeq)) {
                    //it's not necessary to check because unreliable commands are retrieved already.
                    break;
                }

                targetCommand = channel.getIncomingReliableCommand(expectedReliableSeq);
                if (targetCommand == null) {
                    break;
                }

                switch (targetCommand.getCommandType()) {
                    case PING:
                    case EG_SERVER_TIME:
                    case RELIABLE: {
                        long reliableSeq = targetCommand.getReliableSeqNum();

                        channel.setIncomingReliableSeqNum(reliableSeq);
                        channel.removeIncomingReliableCommand(reliableSeq);
                        break;
                    }
                    case FRAGMENT: {

                        long startSeq = targetCommand.getFragmentStartSeqNum();
                        long endSeq = targetCommand.getFragmentStartSeqNum() + targetCommand.getFragmentCount();

                        for (long seq = startSeq; seq < endSeq; seq++) {
                            if (!channel.containIncomingReliableSeqNum(seq)) {
                                targetCommand = null;
                                break;
                            }
                        }

                        if (targetCommand == null)
                            break;

                        byte[] payload = new byte[targetCommand.getFragmentPayloadTotalLength()];

                        for (long seq = startSeq; seq < endSeq; seq++) {
                            IncomingCommand command = channel.getIncomingReliableCommand(seq);
                            if (command == null)
                                continue;
                            if (command.getPayload() != null)
                                command.getPayload().copyBytes(payload, command.getFragmentOffset(), command.getPayload().getLength());
                            channel.removeIncomingReliableCommand(seq);
                        }

                        targetCommand.setPayloadBuf(payload, targetCommand.getFragmentPayloadTotalLength());
                        channel.setIncomingReliableSeqNum(channel.getIncomingReliableSeqNum() + targetCommand.getFragmentCount());
                        break;
                    }
                }
            }

            if (targetCommand != null) {
                switch (targetCommand.getCommandType()) {
                    case RELIABLE:
                    case UNRELIABLE:
                    case FRAGMENT:
                        forwardToClientPeer(targetCommand);
                        break;
                    case DISCONNECT:
                        processDisconnectCommand(targetCommand);
                        break;
                    default:
                        break;
                }
                targetCommand.release();
            }
        } while (targetCommand != null);
    }

    private void forwardToClientPeer(IncomingCommand targetCommand) {
        if (getConnectionState().isEqual(ConnectionState.CONNECTING)) {
            getClientPeer(targetCommand.getPayload(), targetCommand.getChannelIndex());

            if (isClientPeerNull())
                disconnect(DisconnectReason.SERVER_DISCONNECT, "server logic error");
        }
        sendDataToClientPeer(
                targetCommand.getPayload(),
                targetCommand.getChannelIndex(),
                targetCommand.isEncrypted(),
                targetCommand.isReliable() ? QoS.RELIABLE_SEQUENCED : QoS.UNRELIABLE_SEQUENCED);
    }

    private Channel chooseChannelToProcess(IncomingCommand command) {
        Check.NotNull(command, "command");

        Channel channel = null;

        if (getConnectionState().isEqual(ConnectionState.DISCONNECTED))
            return null;

        switch (command.getCommandType()) {
            case ACK: {
                handleReceivedAck(command);
                command.release();
                break;
            }
            case PING: {
                channel = queueIncomingCommand(command);
                break;
            }
            case EG_SERVER_TIME: {
                channel = queueIncomingCommand(command);
                break;
            }
            case DISCONNECT: {
                getConnectionState().transitDisconnected(this);
                if (logger.isDebugEnabled()) {
                    logger.debug("[{}] Recv Disconnect command from Client", peerID);
                }
                processDisconnectCommand(command);
                command.release();
                break;
            }
            case UNRELIABLE:
            case RELIABLE: {
                if (getConnectionState().isEqual(ConnectionState.DISCONNECTED) ||
                        getConnectionState().isEqual(ConnectionState.DISCONNECTING))
                    return null;

                channel = queueIncomingCommand(command);

                break;
            }
            case FRAGMENT: {
                channel = handleFragmentedCommand(command);
                break;
            }
            default: {
                disconnect(DisconnectReason.INVALID_DATA_FORMAT, "Invalid Command Type");
                break;
            }
        }

        return channel;
    }

    private Channel handleFragmentedCommand(final IncomingCommand command) {
        Channel channel = queueIncomingCommand(command);

        if (channel == null)
            return null;

        int totalCommands = command.getFragmentCount();

        long startSeq = command.getFragmentStartSeqNum();
        long endSeq = startSeq + totalCommands;


        for (long seq = startSeq; seq < endSeq; seq++) {
            if (!channel.containIncomingReliableSeqNum(seq)) {
                return null;
            }
        }

        return channel;
    }

    private Channel queueIncomingCommand(final IncomingCommand command) {
        Check.NotNull(command, "command");

        Channel channel = channels.get(command.getChannelIndex());
        if (channel == null)
            return null;

        long reliableSeq = command.getReliableSeqNum();

        if (command.isReliable()) {
            if (reliableSeq <= channel.getIncomingReliableSeqNum() ||
                    channel.containIncomingReliableSeqNum(reliableSeq)) {
                command.release();
                return null;
            }

            channel.insertIncomingReliableCommand(command);

            int queueSize = channel.incomingReliableCommandCount();

            if (queueSize > 0 && (queueSize % WARNING_SIZE) == 0) {
                if (logger.isDebugEnabled())
                    logger.debug("[{}] ch[{}] incomingReliableCommand Count [{}]",
                            peerID, channel.getChannelNumber(), queueSize);
            }
        } else { //CF_UNRELIABLE
            if (command.getReliableSeqNum() < channel.getIncomingReliableSeqNum() ||
                    command.getUnreliableSeqNum() <= channel.getIncomingUnreliableSeqNum()) {
                command.release();
                return null;
            }

            channel.insertIncomingUnreliableCommand(command);

            int queueSize = channel.incomingUnreliableCommandCount();

            if (queueSize > 0 && (queueSize % WARNING_SIZE) == 0) {
                if (logger.isDebugEnabled())
                    logger.debug("[{}] ch[{}] incomingUnreliableCommand Count [{}]",
                            peerID, channel.getChannelNumber(), queueSize);
            }
        }

        return channel;
    }

    private void handleReceivedAck(IncomingCommand command) {
        byte channelIndex = command.getChannelIndex();

        Channel channel = channels.get(channelIndex);
        if (channel == null)
            return;

        if (channel.removeSentReliableCommand(command.getAckReceivedReliableSeq())) {
            long rtt = command.getRoundTripTime();

            if (logger.isTraceEnabled()) {
                logger.trace("[{}] Received Ack ch[{}] seq[{}] rtt[{}] rev[{}] sent[{}]",
                        peerID,
                        command.getChannelIndex(),
                        command.getAckReceivedReliableSeq(),
                        command.getRoundTripTime(),
                        command.getReceiveTime(),
                        command.getAckReceivedSentTime());
            }

            UpdateRoundTripTimeAndVariance(rtt);
        }
    }

    public void processDisconnectCommand(IncomingCommand command) {
        if (command.getCommandType() != CommandType.DISCONNECT) {
            return;
        }

        byte[] payload = command.getPayload().getBytes();
        int reasonCode = ByteRead.getInt(payload, 0);

        DisconnectReason reason = DisconnectReason.getReason(reasonCode);
        onDisconnected(reason, reason.toString());

        doCleanUp();
    }

    @Override
    public void disconnect(DisconnectReason disconnectReason, String detail) {
        if (getConnectionState().isEqual(ConnectionState.DISCONNECTED) ||
                getConnectionState().isEqual(ConnectionState.DISCONNECTING)) {
            return;
        }

        if (detail.isEmpty()) {
            detail = "unknown";
        }

        int reason = disconnectReason.getInt();
        byte[] message = detail.getBytes();
        int length = message.length;

        byte[] payload = new byte[length + 4 + 4];

        int offset = 0;
        offset = ByteWrite.setInt(reason, payload, offset);
        offset = ByteWrite.setInt(length, payload, offset);
        offset = ByteWrite.setBytes(message, 0, length, payload, offset);

        // TODO Must call onDisconnect when disconnecting by timeout.
        createAndEnqueueCommand(CommandType.DISCONNECT, payload, offset, LAST_CHANNEL, false);

        this.disconnectReason = disconnectReason;
        this.disconnectDetail = detail;
        getConnectionState().transitDisconnecting(this);
    }

    private void doCleanUp() {
        synchronized (cleanUpEvents) {
            for (Disposable disposable : cleanUpEvents) {
                disposable.dispose();
            }
            cleanUpEvents.clear();
        }
    }

    @Override
    public void dispose() {
        if (getConnectionState().isEqual(ConnectionState.DISCONNECTING)) {
            getConnectionState().transitDisconnected(this);
        }
        doCleanUp();
    }
}
