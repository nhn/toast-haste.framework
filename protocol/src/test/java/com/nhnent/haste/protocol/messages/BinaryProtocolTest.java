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

package com.nhnent.haste.protocol.messages;

import com.nhnent.haste.protocol.HeaderMessage;
import com.nhnent.haste.protocol.Protocol;
import com.nhnent.haste.protocol.ReturnCode;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.data.DataSchema;
import com.nhnent.haste.protocol.data.DataType;
import com.nhnent.haste.protocol.data.FieldParameter;
import com.nhnent.haste.protocol.protocols.BinaryProtocol;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class BinaryProtocolTest {
    static class CustomDataSchema extends DataSchema {
        CustomDataSchema() {
            super();
        }

        @FieldParameter(Code = (byte) 0x29)
        public byte id;

        @FieldParameter(Code = (byte) 0x39)
        public long uuid;

        ByteBuffer eventSerialize(byte protocolVersion, short code) {
            ByteBuffer buffer = ByteBuffer.allocate(21);

            // Put header
            buffer.put(protocolVersion); // Serialize version
            buffer.put(MessageType.EVENT_MESSAGE.getByte()); // Message type
            buffer.putShort(code); // Message code
            buffer.putInt(2); // Parameter count

            // Put parameter : id
            buffer.put((byte) 0x29); // Parameter code
            buffer.put(DataType.BYTE.getByte()); // Parameter type
            buffer.put(this.id); // Parameter code

            // Put parameter : uuid
            buffer.put((byte) 0x39); // Parameter code
            buffer.put(DataType.INT64.getByte()); // Parameter type
            buffer.putLong(this.uuid); // Parameter value

            return buffer;
        }
    }

    @Test
    public void testSerializeMessage() {
        Protocol protocol = BinaryProtocol.instance;

        DataObject dataObject = new DataObject();
        dataObject.set((byte) 1, true);
        dataObject.set((byte) 2, (byte) 21);
        ResponseMessage responseMessage = new ResponseMessage((short) 1, dataObject);

        ByteBuffer expected = ByteBuffer.allocate(14);
        expected.put(protocol.getVersion()); // Serialize version
        expected.put(responseMessage.getMessageType().getByte()); // Message type
        expected.putShort((short) 1); // Message code
        expected.putInt(2); // Parameter count

        expected.put((byte) 1); // Parameter code
        expected.put(DataType.BOOL.getByte()); // Parameter type
        expected.put((byte) 1); // Parameter value

        expected.put((byte) 2); // Parameter code
        expected.put(DataType.BYTE.getByte()); // Parameter type
        expected.put((byte) 21); // Parameter value

        byte[] bytes = protocol.serialize(responseMessage);

        Assert.assertArrayEquals(expected.array(), bytes);
    }

    @Test
    public void testSerializeDataSchema() {
        final byte ID = 9;
        final long UUID = 1234567891011L;
        final short CODE = 1;

        Protocol protocol = BinaryProtocol.instance;

        CustomDataSchema data = new CustomDataSchema();
        data.id = ID;
        data.uuid = UUID;

        EventMessage eventMessage = new EventMessage(CODE, data);

        ByteBuffer expected = data.eventSerialize(protocol.getVersion(), CODE);

        byte[] bytes = protocol.serialize(eventMessage);

        Assert.assertArrayEquals(expected.array(), bytes);
    }

    @Test
    public void testDeserializeDataSchema() {
        final byte ID = 9;
        final long UUID = 1234567891011L;
        final short CODE = 1;

        Protocol protocol = BinaryProtocol.instance;

        CustomDataSchema data = new CustomDataSchema();
        data.id = ID;
        data.uuid = UUID;

        ByteBuffer expected = data.eventSerialize(protocol.getVersion(), CODE);

        HeaderMessage header = protocol.deserializeHeader(expected.array());
        Message message = protocol.deserializeMessage(expected.array());

        Assert.assertEquals(MessageType.EVENT_MESSAGE, header.getMessageType());
        Assert.assertEquals(protocol.getVersion(), header.getProtocolVersion());

        Assert.assertEquals(MessageType.EVENT_MESSAGE, message.getMessageType());
        Assert.assertEquals(CODE, message.getCode());
    }

    @Test
    public void testSerializeInitialResponse() {
        Protocol protocol = BinaryProtocol.instance;
        InitialResponse response = new InitialResponse(ReturnCode.OK, "test");

        ByteBuffer expected = ByteBuffer.allocate(10);

        expected.put(protocol.getVersion()); // Serialize version
        expected.put(MessageType.INITIAL_RESPONSE.getByte()); // Message type
        expected.putShort((short) 1); // Message code
        expected.putShort(response.getMessageLength()); // Length of message
        expected.put(response.getMessage().getBytes()); // Message string

        byte[] bytes = protocol.serialize(response);

        Assert.assertArrayEquals(expected.array(), bytes);
    }

    public String toString(byte[] num) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < num.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            short v = (short) (num[i] & 0xFF);
            builder.append(String.format("0x%02X", v));
        }
        return builder.append("]").toString();
    }
}
