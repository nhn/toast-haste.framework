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

import java.util.zip.CRC32;

public final class CRC {
    public final static int CRC_LENGTH = 8;

    public static boolean Check(CRC32 crc32, byte[] buf, int length, int offset) {
        long crc = ByteRead.getLong(buf, offset);

        crc32.reset();
        ByteWrite.setLong(0, buf, offset);
        crc32.update(buf, 0, length);

        return crc == crc32.getValue();
    }

    public static void Write(CRC32 crc32, byte[] buf, int length, int offset) {
        ByteWrite.setLong(0, buf, offset);

        crc32.reset();
        crc32.update(buf, 0, length);
        long crc = crc32.getValue();

        ByteWrite.setLong(crc, buf, offset);
    }
}
