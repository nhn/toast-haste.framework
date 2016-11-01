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

package com.nhnent.haste.protocol.data;

import com.nhnent.haste.common.ByteRead;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SerializeTest {
    @Test
    public void testByte() {
        final byte TEST_CASE = 111;
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer = buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(TEST_CASE);

        byte readByte = ByteRead.getByte(buffer.array(), 0);
        Assert.assertEquals(TEST_CASE, readByte);
    }

    @Test
    public void testUnsignedByte() {
        final byte TEST_CASE = (byte) 0xFD;
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer = buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(TEST_CASE);

        short readUbyte = ByteRead.getUnsignedByte(buffer.array(), 0);
        Assert.assertEquals((short) (TEST_CASE & 0xFF), readUbyte);
    }

    @Test
    public void testShort() {
        final short TEST_CASE = (short) 62510;
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer = buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort(TEST_CASE);

        short readShort = ByteRead.getShort(buffer.array(), 0);
        Assert.assertEquals(TEST_CASE, readShort);
    }

    @Test
    public void testInt() {
        final int TEST_CASE = 164214632;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer = buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(TEST_CASE);

        int readInt = ByteRead.getInt(buffer.array(), 0);
        Assert.assertEquals(TEST_CASE, readInt);
    }

    @Test
    public void testLong() {
        final long TEST_CASE = 1642146535132L;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer = buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(TEST_CASE);

        long readLong = ByteRead.getLong(buffer.array(), 0);
        Assert.assertEquals(TEST_CASE, readLong);
    }

    @Test
    public void testSetObject() {
        DataObject dataObject = new DataObject();
        dataObject.set((byte) 0, (byte) 7);
        dataObject.set((byte) 1, true);
        dataObject.set((byte) 2, (short) 17);
        dataObject.set((byte) 3, "테스트");
        dataObject.set((byte) 4, 23);
        dataObject.set((byte) 5, 97472L);
        dataObject.set((byte) 6, "Test!");
        byte[] bs = dataObject.serialize();

        DataObject dataObject2 = DataObject.toDataObject(bs);
        Assert.assertEquals((byte) 7, dataObject2.get((byte) 0).value);
        Assert.assertEquals(true, dataObject2.get((byte) 1).value);
        Assert.assertEquals((short) 17, dataObject2.get((byte) 2).value);
        Assert.assertEquals("테스트", dataObject2.get((byte) 3).value);
        Assert.assertEquals(23, dataObject2.get((byte) 4).value);
        Assert.assertEquals(97472L, dataObject2.get((byte) 5).value);
        Assert.assertEquals("Test!", dataObject2.get((byte) 6).value);
    }
}