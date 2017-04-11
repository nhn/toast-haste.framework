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

class ArrayUtils {
    static byte[] toByteArray(Object value) {
        if (value instanceof byte[])
            return (byte[]) value;

        Byte[] src = (Byte[]) value;
        byte[] dst = new byte[src.length];
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
        return dst;
    }

    static boolean[] toBooleanArray(Object value) {
        if (value instanceof boolean[])
            return (boolean[]) value;

        Boolean[] src = (Boolean[]) value;
        boolean[] dst = new boolean[src.length];
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
        return dst;
    }

    static short[] toShortArray(Object value) {
        if (value instanceof short[])
            return (short[]) value;

        Short[] src = (Short[]) value;
        short[] dst = new short[src.length];
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
        return dst;
    }

    static int[] toIntArray(Object value) {
        if (value instanceof int[])
            return (int[]) value;

        Integer[] src = (Integer[]) value;
        int[] dst = new int[src.length];
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
        return dst;
    }

    static long[] toLongArray(Object value) {
        if (value instanceof long[])
            return (long[]) value;

        Long[] src = (Long[]) value;
        long[] dst = new long[src.length];
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
        return dst;
    }

    static float[] toFloatArray(Object value) {
        if (value instanceof float[])
            return (float[]) value;

        Float[] src = (Float[]) value;
        float[] dst = new float[src.length];
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
        return dst;
    }

    static double[] toDoubleArray(Object value) {
        if (value instanceof double[])
            return (double[]) value;

        Double[] src = (Double[]) value;
        double[] dst = new double[src.length];
        for (int i = 0; i < src.length; i++)
            dst[i] = src[i];
        return dst;
    }
}
