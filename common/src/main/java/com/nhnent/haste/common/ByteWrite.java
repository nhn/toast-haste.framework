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

package com.nhnent.haste.common;

public final class ByteWrite {
    private ByteWrite() {
    }

    public static int set(byte value, byte[] dst, int startIndex) {
        return setByte(value, dst, startIndex);
    }

    public static int set(boolean value, byte[] dst, int startIndex) {
        return setBoolean(value, dst, startIndex);
    }

    public static int set(short value, byte[] dst, int startIndex) {
        return setShort(value, dst, startIndex);
    }

    public static int set(int value, byte[] dst, int startIndex) {
        return setInt(value, dst, startIndex);
    }

    public static int set(long value, byte[] dst, int startIndex) {
        return setLong(value, dst, startIndex);
    }

    public static int set(byte[] src, int srcStartIndex, int length, byte[] dst, int dstStartIndex) {
        return setBytes(src, srcStartIndex, length, dst, dstStartIndex);
    }

    public static int setByte(byte value, byte[] dst, int startIndex) {
        Check.EnsureIndex(dst, startIndex, 1);
        dst[startIndex] = value;
        return startIndex + 1;
    }

    public static int setBoolean(boolean value, byte[] dst, int startIndex) {
        return setByte((byte) (value ? 1 : 0), dst, startIndex);
    }

    public static int setShort(short value, byte[] dst, int startIndex) {
        Check.EnsureIndex(dst, startIndex, 2);
        dst[startIndex] = (byte) ((value & 0xFF00) >> 8);
        dst[startIndex + 1] = (byte) (value & 0xFF);
        return startIndex + 2;
    }

    public static int setInt(int value, byte[] dst, int startIndex) {
        Check.EnsureIndex(dst, startIndex, 4);
        setShort((short) ((value & 0xFFFF0000) >> 16), dst, startIndex);
        setShort((short) (value & 0xFFFF), dst, startIndex + 2);
        return startIndex + 4;
    }

    public static int setLong(long value, byte[] dst, int startIndex) {
        Check.EnsureIndex(dst, startIndex, 8);
        setInt((int) ((value & 0xFFFFFFFF00000000L) >> 32), dst, startIndex);
        setInt((int) (value & 0xFFFFFFFFL), dst, startIndex + 4);
        return startIndex + 8;
    }

    public static int setBytes(byte[] src, int srcStartIndex, int length, byte[] dst, int dstStartIndex) {
        Check.EnsureIndex(src, srcStartIndex, length);
        Check.EnsureIndex(dst, dstStartIndex, length);

        System.arraycopy(src, srcStartIndex, dst, dstStartIndex, length);

        return dstStartIndex + length;
    }
}
