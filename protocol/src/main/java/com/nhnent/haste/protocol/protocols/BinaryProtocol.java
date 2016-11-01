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

package com.nhnent.haste.protocol.protocols;

import com.nhnent.haste.common.ByteRead;
import com.nhnent.haste.common.Check;
import com.nhnent.haste.common.Version;
import com.nhnent.haste.protocol.HeaderMessage;
import com.nhnent.haste.protocol.Protocol;
import com.nhnent.haste.protocol.ReturnCode;
import com.nhnent.haste.protocol.data.ByteWrapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.protocol.messages.InitialResponse;
import com.nhnent.haste.protocol.messages.Message;
import com.nhnent.haste.protocol.messages.MessageType;

public class BinaryProtocol implements Protocol {
    public static BinaryProtocol instance = new BinaryProtocol();

    private BinaryProtocol() {
    }

    public final static byte VERSION = 0x02;

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public MessageType getMessageType(byte[] data) {
        if (data == null || data.length < 2) {
            return MessageType.NONE;
        }

        if (data[0] != VERSION) {
            throw new IllegalArgumentException("illegal version : " + data[0]);
        }

        return MessageType.getType(data[1]);
    }

    @Override
    public byte[] serialize(Message message) {
        Check.NotNull(message, "message");

        ByteWrapper wrapper = new ByteWrapper(2048);
        wrapper = serializeHeader(wrapper, VERSION, message.getMessageType());

        wrapper.writeShort(message.getCode());
        byte[] serializedBytes = message.getDataObject().serialize();
        wrapper.writeBytes(serializedBytes);

        return wrapper.toArray();
    }

    @Override
    public byte[] serialize(InitialRequest initialRequest) {
        Check.NotNull(initialRequest, "initialRequest");

        ByteWrapper wrapper = new ByteWrapper(2048);
        wrapper = serializeHeader(wrapper, VERSION, MessageType.INITIAL_REQUEST);

        Version sdkVersion = initialRequest.getSDKVersion();
        Version clientVersion = initialRequest.getClientVersion();

        wrapper.writeShort((short) sdkVersion.getMajor());
        wrapper.writeShort((short) sdkVersion.getMinor());
        wrapper.writeShort((short) sdkVersion.getBuild());

        wrapper.writeShort((short) clientVersion.getMajor());
        wrapper.writeShort((short) clientVersion.getMinor());
        wrapper.writeShort((short) clientVersion.getBuild());

        byte[] customData = initialRequest.getCustomData();
        wrapper.writeShort((short) customData.length);
        wrapper.writeBytes(customData);

        return wrapper.toArray();
    }

    @Override
    public byte[] serialize(InitialResponse initialResponse) {
        Check.NotNull(initialResponse, "initialResponse");

        ByteWrapper wrapper = new ByteWrapper(2048);
        wrapper = serializeHeader(wrapper, VERSION, MessageType.INITIAL_RESPONSE);
        wrapper.writeShort(initialResponse.getResultCode());
        wrapper.writeShort(initialResponse.getMessageLength());
        wrapper.writeBytes(initialResponse.getMessage().getBytes());

        return wrapper.toArray();
    }

    private ByteWrapper serializeHeader(ByteWrapper wrapper, byte version, MessageType type) {
        Check.NotNull(wrapper, "byteWrapper");
        wrapper.writeBytes(serializeHeader(version, type));
        return wrapper;
    }

    private byte[] serializeHeader(byte version, MessageType type) {
        return new byte[]{version, type.getByte()};
    }

    @Override
    public byte[] serializeHeader(HeaderMessage headerMessage) {
        byte version = headerMessage.getProtocolVersion();
        MessageType type = headerMessage.getMessageType();
        return serializeHeader(version, type);
    }

    @Override
    public Message deserializeMessage(byte[] data) {
        Check.NotNull(data, "data");

        int offset = 0;
        byte version = data[offset++];

        if (version != VERSION) {
            throw new IllegalArgumentException("illegal version");
        }

        Message message = null;
        MessageType type = MessageType.getType(data[offset++]);

        short code = ByteRead.getShort(data, offset);
        offset += 2;
        byte[] payload = ByteRead.getBytes(data, offset);
        offset += payload.length;

        return Message.toMessage(type, code, DataObject.toDataObject(payload));
    }

    @Override
    public InitialRequest deserializeInitialRequest(byte[] data) {
        Check.NotNull(data, "data");
        Check.EnsureIndex(data, 0, HeaderMessage.HEADER_SIZE);

        int offset = 0;

        HeaderMessage header = deserializeHeader(data);
        offset += HeaderMessage.HEADER_SIZE;

        if (MessageType.INITIAL_REQUEST != header.getMessageType()) {
            return null;
        }

        short major = ByteRead.getShort(data, offset);
        offset += 2;
        short minor = ByteRead.getShort(data, offset);
        offset += 2;
        short build = ByteRead.getShort(data, offset);
        offset += 2;

        Version sdkVersion = new Version.Builder().major(major).minor(minor).build(build);

        major = ByteRead.getShort(data, offset);
        offset += 2;
        minor = ByteRead.getShort(data, offset);
        offset += 2;
        build = ByteRead.getShort(data, offset);
        offset += 2;

        Version clientVersion = new Version.Builder().major(major).minor(minor).build(build);

        short customDataLength = ByteRead.getShort(data, offset);
        offset += 2;

        byte[] customData = ByteRead.getBytes(data, offset, customDataLength);
        offset += customDataLength;

        return new InitialRequest().protocol(this).sdkVersion(sdkVersion).clientVersion(clientVersion).customData(customData);
    }

    @Override
    public InitialResponse deserializeInitialResponse(byte[] data) {
        Check.NotNull(data, "data");

        int offset = 0;
        byte version = data[offset++];

        if (version != VERSION) {
            throw new IllegalArgumentException("illegal version");
        }

        MessageType type = MessageType.getType(data[offset++]);

        if (type != MessageType.INITIAL_RESPONSE) {
            throw new IllegalArgumentException("invalid operation type!!");
        }

        short returnCode = ByteRead.getShort(data, offset);
        offset += 2;
        short messageLength = ByteRead.getShort(data, offset);
        offset += 2;
        byte[] message = ByteRead.getBytes(data, offset, messageLength);
        offset += messageLength;

        return new InitialResponse(ReturnCode.getCode(returnCode), new String(message));
    }

    @Override
    public HeaderMessage deserializeHeader(byte[] data) {
        return new HeaderMessage(data[0], data[1]);
    }
}
