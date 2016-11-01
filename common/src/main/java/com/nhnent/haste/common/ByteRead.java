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

public final class ByteRead {
    private static final int MASK_BYTE = 0x000000FF;
    private static final int MASK_SHORT = 0x0000FFFF;
    private static final long MASK_INT = 0x00000000FFFFFFFFL;

    private ByteRead() {
    }

    public static byte getByte(byte[] src, int startIndex) {
        Check.EnsureIndex(src, startIndex, 1);
        return (byte) (src[startIndex] & MASK_BYTE);
    }

    public static boolean getBoolean(byte[] src, int startIndex) {
        byte boolData = getByte(src, startIndex);
        return boolData != 0;
    }

    public static short getUnsignedByte(byte[] src, int startIndex) {
        Check.EnsureIndex(src, startIndex, 1);
        return (short) (src[startIndex] & MASK_BYTE);
    }

    public static short getShort(byte[] src, int startIndex) {
        Check.EnsureIndex(src, startIndex, 2);

        int value1 = (src[startIndex] & MASK_BYTE) << 8;
        int value2 = src[startIndex + 1] & MASK_BYTE;

        return (short) (value1 | value2);
    }

    public static int getInt(byte[] src, int startIndex) {
        Check.EnsureIndex(src, startIndex, 4);

        int value1 = (getShort(src, startIndex) & MASK_SHORT) << 16;
        int value2 = (getShort(src, startIndex + 2) & MASK_SHORT);
        return value1 | value2;
    }

    public static long getLong(byte[] src, int startIndex) {
        Check.EnsureIndex(src, startIndex, 8);

        long value1 = (getInt(src, startIndex) & MASK_INT) << 32;
        long value2 = (getInt(src, startIndex + 4) & MASK_INT);
        return value1 | value2;
    }

    public static byte[] getBytes(byte[] src, int startIndex) {
        return getBytes(src, startIndex, src.length - startIndex);
    }

    public static byte[] getBytes(byte[] src, int startIndex, int size) {
        byte[] newBytes = new byte[size];
        System.arraycopy(src, startIndex, newBytes, 0, size);
        return newBytes;
    }
}
