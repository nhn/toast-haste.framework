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

package com.nhnent.haste.security;

public class TestUtil {
    public static String toString(BigInteger num) {
        return toString(num.toByteArray());
    }

    public static String toString(byte[] num) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < num.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            short v = (short) (num[i] & 0xFF);
            builder.append(v);
        }
        return builder.append("]").toString();
    }

    public static byte[] toByteArray(int[] input) {
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (byte) (0xFF & input[i]);
        }
        return result;
    }
}
