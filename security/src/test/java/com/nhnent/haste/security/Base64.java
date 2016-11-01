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

class Base64 {
    private Base64() {
    }

    private static final char[] toBase64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    private static byte[] encode(byte[] src) {
        byte[] dst = new byte[4 * ((src.length + 2) / 3)];

        int sp = 0;
        final int slen = src.length / 3 * 3;
        int dp = 0;
        while (sp < slen) {
            int sl0 = Math.min(sp + slen, slen);
            for (int sp0 = sp, dp0 = dp; sp0 < sl0; ) {
                int bits = (src[sp0++] & 0xff) << 16 | (src[sp0++] & 0xff) << 8 | (src[sp0++] & 0xff);
                dst[dp0++] = (byte) toBase64[(bits >>> 18) & 0x3f];
                dst[dp0++] = (byte) toBase64[(bits >>> 12) & 0x3f];
                dst[dp0++] = (byte) toBase64[(bits >>> 6) & 0x3f];
                dst[dp0++] = (byte) toBase64[bits & 0x3f];
            }
            int dlen = (sl0 - sp) / 3 * 4;
            dp += dlen;
            sp = sl0;
        }

        if (sp < src.length) {
            int b0 = src[sp++] & 0xff;
            dst[dp++] = (byte) toBase64[b0 >> 2];
            if (sp == src.length) {
                dst[dp++] = (byte) toBase64[(b0 << 4) & 0x3f];
                dst[dp++] = '=';
                dst[dp++] = '=';
            } else {
                int b1 = src[sp++] & 0xff;
                dst[dp++] = (byte) toBase64[(b0 << 4) & 0x3f | (b1 >> 4)];
                dst[dp++] = (byte) toBase64[(b1 << 2) & 0x3f];
                dst[dp++] = '=';
            }
        }

        return dst;
    }

    static String encodeToString(byte[] src) {
        byte[] encoded = encode(src);
        return new String(encoded, 0, 0, encoded.length);
    }
}