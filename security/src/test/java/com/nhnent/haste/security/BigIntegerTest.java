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

import org.junit.Assert;
import org.junit.Test;

public class BigIntegerTest {
    @Test
    public void testFromByteArrayToBigInteger() {
        // This byte array means 1234567891433.
        byte[] bs = new byte[]{1, 31, 113, (byte) 251, 9, (byte) 233};

        BigInteger integer = new BigInteger(bs);
        BigInteger stringInteger = new BigInteger("1234567891433");

        Assert.assertEquals(integer, stringInteger);
    }
}