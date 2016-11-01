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

/**
 * Reference to <a href="https://tools.ietf.org/html/rfc2409#page-21">RFC-2409</a>
 */
class OakleyGroup1 {
    static final BigInteger generator = BigInteger.valueOf(2);
    private static final byte[] oakley768 = new byte[]{
            (byte) 0x00, // the most significant byte is in the zeroth element.
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xC9,
            (byte) 0x0F,
            (byte) 0xDA,
            (byte) 0xA2,
            (byte) 0x21,
            (byte) 0x68,
            (byte) 0xC2,
            (byte) 0x34,
            (byte) 0xC4,
            (byte) 0xC6,
            (byte) 0x62,
            (byte) 0x8B,
            (byte) 0x80,
            (byte) 0xDC,
            (byte) 0x1C,
            (byte) 0xD1,
            (byte) 0x29,
            (byte) 0x02,
            (byte) 0x4E,
            (byte) 0x08,
            (byte) 0x8A,
            (byte) 0x67,
            (byte) 0xCC,
            (byte) 0x74,
            (byte) 0x02,
            (byte) 0x0B,
            (byte) 0xBE,
            (byte) 0xA6,
            (byte) 0x3B,
            (byte) 0x13,
            (byte) 0x9B,
            (byte) 0x22,
            (byte) 0x51,
            (byte) 0x4A,
            (byte) 0x08,
            (byte) 0x79,
            (byte) 0x8E,
            (byte) 0x34,
            (byte) 0x04,
            (byte) 0xDD,
            (byte) 0xEF,
            (byte) 0x95,
            (byte) 0x19,
            (byte) 0xB3,
            (byte) 0xCD,
            (byte) 0x3A,
            (byte) 0x43,
            (byte) 0x1B,
            (byte) 0x30,
            (byte) 0x2B,
            (byte) 0x0A,
            (byte) 0x6D,
            (byte) 0xF2,
            (byte) 0x5F,
            (byte) 0x14,
            (byte) 0x37,
            (byte) 0x4F,
            (byte) 0xE1,
            (byte) 0x35,
            (byte) 0x6D,
            (byte) 0x6D,
            (byte) 0x51,
            (byte) 0xC2,
            (byte) 0x45,
            (byte) 0xE4,
            (byte) 0x85,
            (byte) 0xB5,
            (byte) 0x76,
            (byte) 0x62,
            (byte) 0x5E,
            (byte) 0x7E,
            (byte) 0xC6,
            (byte) 0xF4,
            (byte) 0x4C,
            (byte) 0x42,
            (byte) 0xE9,
            (byte) 0xA6,
            (byte) 0x3A,
            (byte) 0x36,
            (byte) 0x20,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF
    };
    static final BigInteger primeNumber = new BigInteger(oakley768);
}
