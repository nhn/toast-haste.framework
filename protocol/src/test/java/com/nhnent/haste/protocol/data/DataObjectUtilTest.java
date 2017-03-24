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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DataObjectUtilTest {
    private static final byte BYTE_DATA = 12;
    private static final boolean BOOLEAN_DATA = true;
    private static final short SHORT_DATA = 34;
    private static final int INT_DATA = 56;
    private static final long LONG_DATA = 78;
    private static final float FLOAT_DATA = 9.10f;
    private static final double DOUBLE_DATA = 11.12;
    private static final String STRING_DATA = "test";

    private static final byte[] BYTE_ARRAY_DATA = {1, 2};
    private static final boolean[] BOOLEAN_ARRAY_DATA = {true, false};
    private static final short[] SHORT_ARRAY_DATA = {3, 4};
    private static final int[] INT_ARRAY_DATA = {5, 6};
    private static final long[] LONG_ARRAY_DATA = {7L, 8L};
    private static final float[] FLOAT_ARRAY_DATA = {9.10f, 10.11f};
    private static final double[] DOUBLE_ARRAY_DATA = {11.12, 12.13};

    private static final Byte[] BYTE_OBJECT_ARRAY_DATA = {1, 2};
    private static final Boolean[] BOOLEAN_OBJECT_ARRAY_DATA = {true};
    private static final Short[] SHORT_OBJECT_ARRAY_DATA = {3, 4};
    private static final Integer[] INT_OBJECT_ARRAY_DATA = {5, 6};
    private static final Long[] LONG_OBJECT_ARRAY_DATA = {7L, 8L};
    private static final Float[] FLOAT_OBJECT_ARRAY_DATA = {9.10f, 10.11f};
    private static final Double[] DOUBLE_OBJECT_ARRAY_DATA = {11.12, 12.13};
    private static final String[] STRING_OBJECT_ARRAY_DATA = {"test", "test2"};

    @Test
    public void testGetType() throws Exception {
        Assert.assertEquals(DataType.BYTE, DataObjectUtil.getType(BYTE_DATA));
        Assert.assertEquals(DataType.BOOL, DataObjectUtil.getType(BOOLEAN_DATA));
        Assert.assertEquals(DataType.INT16, DataObjectUtil.getType(SHORT_DATA));
        Assert.assertEquals(DataType.INT32, DataObjectUtil.getType(INT_DATA));
        Assert.assertEquals(DataType.INT64, DataObjectUtil.getType(LONG_DATA));
        Assert.assertEquals(DataType.FLOAT, DataObjectUtil.getType(FLOAT_DATA));
        Assert.assertEquals(DataType.DOUBLE, DataObjectUtil.getType(DOUBLE_DATA));
        Assert.assertEquals(DataType.STRING, DataObjectUtil.getType(STRING_DATA));

        Assert.assertEquals(DataType.BYTE_ARRAY, DataObjectUtil.getType(BYTE_OBJECT_ARRAY_DATA));
        Assert.assertEquals(DataType.BOOL_ARRAY, DataObjectUtil.getType(BOOLEAN_OBJECT_ARRAY_DATA));
        Assert.assertEquals(DataType.INT16_ARRAY, DataObjectUtil.getType(SHORT_OBJECT_ARRAY_DATA));
        Assert.assertEquals(DataType.INT32_ARRAY, DataObjectUtil.getType(INT_OBJECT_ARRAY_DATA));
        Assert.assertEquals(DataType.INT64_ARRAY, DataObjectUtil.getType(LONG_OBJECT_ARRAY_DATA));
        Assert.assertEquals(DataType.FLOAT_ARRAY, DataObjectUtil.getType(FLOAT_OBJECT_ARRAY_DATA));
        Assert.assertEquals(DataType.DOUBLE_ARRAY, DataObjectUtil.getType(DOUBLE_OBJECT_ARRAY_DATA));
        Assert.assertEquals(DataType.STRING_ARRAY, DataObjectUtil.getType(STRING_OBJECT_ARRAY_DATA));

        Assert.assertEquals(DataType.BYTE_ARRAY, DataObjectUtil.getType(BYTE_ARRAY_DATA));
        Assert.assertEquals(DataType.BOOL_ARRAY, DataObjectUtil.getType(BOOLEAN_ARRAY_DATA));
        Assert.assertEquals(DataType.INT16_ARRAY, DataObjectUtil.getType(SHORT_ARRAY_DATA));
        Assert.assertEquals(DataType.INT32_ARRAY, DataObjectUtil.getType(INT_ARRAY_DATA));
        Assert.assertEquals(DataType.INT64_ARRAY, DataObjectUtil.getType(LONG_ARRAY_DATA));
        Assert.assertEquals(DataType.FLOAT_ARRAY, DataObjectUtil.getType(FLOAT_ARRAY_DATA));
        Assert.assertEquals(DataType.DOUBLE_ARRAY, DataObjectUtil.getType(DOUBLE_ARRAY_DATA));
    }

    @Test
    public void testWriteAndReadPrimitiveType() {
        ByteWrapper byteWrapper = new ByteWrapper(1024);
        DataObjectUtil.writeData(new DataWrapper(DataType.BYTE, BYTE_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.BOOL, BOOLEAN_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT16, SHORT_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT32, INT_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT64, LONG_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.FLOAT, FLOAT_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.DOUBLE, DOUBLE_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.STRING, STRING_DATA), byteWrapper);

        DataWrapper byteDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper booleanDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper shortDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper intDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper longDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper floatDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper doubleDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper stringDataWrapper = DataObjectUtil.readData(byteWrapper);

        Assert.assertEquals(DataType.BYTE, byteDataWrapper.type);
        Assert.assertEquals(DataType.BOOL, booleanDataWrapper.type);
        Assert.assertEquals(DataType.INT16, shortDataWrapper.type);
        Assert.assertEquals(DataType.INT32, intDataWrapper.type);
        Assert.assertEquals(DataType.INT64, longDataWrapper.type);
        Assert.assertEquals(DataType.FLOAT, floatDataWrapper.type);
        Assert.assertEquals(DataType.DOUBLE, doubleDataWrapper.type);
        Assert.assertEquals(DataType.STRING, stringDataWrapper.type);

        Assert.assertEquals(BYTE_DATA, byteDataWrapper.value);
        Assert.assertEquals(BOOLEAN_DATA, booleanDataWrapper.value);
        Assert.assertEquals(SHORT_DATA, shortDataWrapper.value);
        Assert.assertEquals(INT_DATA, intDataWrapper.value);
        Assert.assertEquals(LONG_DATA, longDataWrapper.value);
        Assert.assertEquals(FLOAT_DATA, floatDataWrapper.value);
        Assert.assertEquals(DOUBLE_DATA, doubleDataWrapper.value);
        Assert.assertEquals(STRING_DATA, stringDataWrapper.value);
    }

    @Test
    public void testWriteAndReadPrimitiveArrayType() {
        ByteWrapper byteWrapper = new ByteWrapper(1024);
        DataObjectUtil.writeData(new DataWrapper(DataType.BYTE_ARRAY, BYTE_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.BOOL_ARRAY, BOOLEAN_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT16_ARRAY, SHORT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT32_ARRAY, INT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT64_ARRAY, LONG_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.FLOAT_ARRAY, FLOAT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.DOUBLE_ARRAY, DOUBLE_ARRAY_DATA), byteWrapper);

        DataWrapper byteDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper booleanDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper shortDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper intDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper longDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper floatDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper doubleDataWrapper = DataObjectUtil.readData(byteWrapper);

        // Cast to primitive array type because Assert::assertArrayEquals can't pass Object parameter
        byte[] byteArrayData = (byte[]) byteDataWrapper.value;
        boolean[] booleanArrayData = (boolean[]) booleanDataWrapper.value;
        short[] shortArrayData = (short[]) shortDataWrapper.value;
        int[] intArrayData = (int[]) intDataWrapper.value;
        long[] longArrayData = (long[]) longDataWrapper.value;
        float[] floatArrayData = (float[]) floatDataWrapper.value;
        double[] doubleArrayData = (double[]) doubleDataWrapper.value;

        Assert.assertArrayEquals(BYTE_ARRAY_DATA, byteArrayData);
        Assert.assertArrayEquals(BOOLEAN_ARRAY_DATA, booleanArrayData);
        Assert.assertArrayEquals(SHORT_ARRAY_DATA, shortArrayData);
        Assert.assertArrayEquals(INT_ARRAY_DATA, intArrayData);
        Assert.assertArrayEquals(LONG_ARRAY_DATA, longArrayData);
        Assert.assertArrayEquals(FLOAT_ARRAY_DATA, floatArrayData, 0.0001f);
        Assert.assertArrayEquals(DOUBLE_ARRAY_DATA, doubleArrayData, 0.0001);
    }

    @Test
    public void testWriteAndReadObjectArrayType() {
        ByteWrapper byteWrapper = new ByteWrapper(1024);
        DataObjectUtil.writeData(new DataWrapper(DataType.BYTE_ARRAY, BYTE_OBJECT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.BOOL_ARRAY, BOOLEAN_OBJECT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT16_ARRAY, SHORT_OBJECT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT32_ARRAY, INT_OBJECT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.INT64_ARRAY, LONG_OBJECT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.FLOAT_ARRAY, FLOAT_OBJECT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.DOUBLE_ARRAY, DOUBLE_OBJECT_ARRAY_DATA), byteWrapper);
        DataObjectUtil.writeData(new DataWrapper(DataType.STRING_ARRAY, STRING_OBJECT_ARRAY_DATA), byteWrapper);

        DataWrapper byteDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper booleanDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper shortDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper intDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper longDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper floatDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper doubleDataWrapper = DataObjectUtil.readData(byteWrapper);
        DataWrapper stringDataWrapper = DataObjectUtil.readData(byteWrapper);

        // DataObjectUtil::readData returns primitive array type although written data was object array type
        byte[] byteArrayData = (byte[]) byteDataWrapper.value;
        boolean[] booleanArrayData = (boolean[]) booleanDataWrapper.value;
        short[] shortArrayData = (short[]) shortDataWrapper.value;
        int[] intArrayData = (int[]) intDataWrapper.value;
        long[] longArrayData = (long[]) longDataWrapper.value;
        float[] floatArrayData = (float[]) floatDataWrapper.value;
        double[] doubleArrayData = (double[]) doubleDataWrapper.value;
        String[] stringArrayData = (String[]) stringDataWrapper.value;

        Assert.assertArrayEquals(BYTE_OBJECT_ARRAY_DATA, Util.toObjectArray(byteArrayData));
        Assert.assertArrayEquals(BOOLEAN_OBJECT_ARRAY_DATA, Util.toObjectArray(booleanArrayData));
        Assert.assertArrayEquals(SHORT_OBJECT_ARRAY_DATA, Util.toObjectArray(shortArrayData));
        Assert.assertArrayEquals(INT_OBJECT_ARRAY_DATA, Util.toObjectArray(intArrayData));
        Assert.assertArrayEquals(LONG_OBJECT_ARRAY_DATA, Util.toObjectArray(longArrayData));
        Assert.assertArrayEquals(FLOAT_OBJECT_ARRAY_DATA, Util.toObjectArray(floatArrayData));
        Assert.assertArrayEquals(DOUBLE_OBJECT_ARRAY_DATA, Util.toObjectArray(doubleArrayData));
        Assert.assertArrayEquals(STRING_OBJECT_ARRAY_DATA, stringArrayData);
    }

    static class Util {
        static Byte[] toObjectArray(byte[] src) {
            Byte[] dst = new Byte[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = src[i];
            return dst;
        }

        static Boolean[] toObjectArray(boolean[] src) {
            Boolean[] dst = new Boolean[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = src[i];
            return dst;
        }

        static Short[] toObjectArray(short[] src) {
            Short[] dst = new Short[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = src[i];
            return dst;
        }

        static Integer[] toObjectArray(int[] src) {
            Integer[] dst = new Integer[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = src[i];
            return dst;
        }

        static Long[] toObjectArray(long[] src) {
            Long[] dst = new Long[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = src[i];
            return dst;
        }

        static Float[] toObjectArray(float[] src) {
            Float[] dst = new Float[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = src[i];
            return dst;
        }

        static Double[] toObjectArray(double[] src) {
            Double[] dst = new Double[src.length];
            for (int i = 0; i < src.length; i++)
                dst[i] = src[i];
            return dst;
        }
    }
}