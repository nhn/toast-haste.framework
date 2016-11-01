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
import com.nhnent.haste.common.ByteWrite;

import java.util.Arrays;

public class ByteWrapper {
    private byte[] src;
    private int readIndex;
    private int writeIndex;

    public ByteWrapper(int capacity) {
        this(new byte[capacity]);
    }

    public ByteWrapper(byte[] src) {
        this.src = src;
        this.readIndex = 0;
        this.writeIndex = 0;
    }

    public byte[] toArray() {
        byte[] srcBytes = this.src;
        return Arrays.copyOfRange(srcBytes, 0, writeIndex);
    }

    public byte readByte() {
        byte result = ByteRead.getByte(this.src, this.readIndex);
        this.readIndex += 1;
        return result;
    }

    public boolean readBoolean() {
        boolean result = ByteRead.getBoolean(this.src, this.readIndex);
        this.readIndex += 1;
        return result;
    }

    public short readUnsignedByte() {
        short result = ByteRead.getUnsignedByte(this.src, this.readIndex);
        this.readIndex += 1;
        return result;
    }

    public short readShort() {
        short result = ByteRead.getShort(this.src, this.readIndex);
        this.readIndex += 2;
        return result;
    }

    public int readInt() {
        int result = ByteRead.getInt(this.src, this.readIndex);
        this.readIndex += 4;
        return result;
    }

    public long readLong() {
        long result = ByteRead.getLong(this.src, this.readIndex);
        this.readIndex += 8;
        return result;
    }

    public float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }

    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }

    public byte[] readBytes(int size) {
        byte[] result = ByteRead.getBytes(this.src, this.readIndex, size);
        this.readIndex += result.length;
        return result;
    }

    public void writeByte(byte value) {
        this.writeIndex = ByteWrite.setByte(value, this.src, this.writeIndex);
    }

    public void writeBoolean(boolean value) {
        this.writeIndex = ByteWrite.setBoolean(value, this.src, this.writeIndex);
    }

    public void writeShort(short value) {
        this.writeIndex = ByteWrite.setShort(value, this.src, this.writeIndex);
    }

    public void writeInt(int value) {
        this.writeIndex = ByteWrite.setInt(value, this.src, this.writeIndex);
    }

    public void writeLong(long value) {
        this.writeIndex = ByteWrite.setLong(value, this.src, this.writeIndex);
    }

    public void writeFloat(float value) {
        this.writeIndex = ByteWrite.setInt(Float.floatToIntBits(value), this.src, this.writeIndex);
    }

    public void writeDouble(double value) {
        this.writeIndex = ByteWrite.setLong(Double.doubleToLongBits(value), this.src, this.writeIndex);
    }

    public void writeBytes(byte[] value) {
        this.writeIndex = ByteWrite.setBytes(value, 0, value.length, this.src, this.writeIndex);
    }
}