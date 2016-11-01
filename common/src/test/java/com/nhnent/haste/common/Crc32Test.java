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

import org.junit.Assert;
import org.junit.Test;

import java.util.zip.CRC32;

public class Crc32Test {
    private static final byte[] data = new byte[]{
            92, 48, 1, 4, 85, 34, 87, 41, 25, 21, 75, 3, 8
    };

    @Test
    public void testCrc32() {
        int offset = data.length - 8;

        ByteWrite.setLong(0, data, offset);

        CRC32 crc32 = new CRC32();
        crc32.reset();
        crc32.update(data);
        ByteWrite.setLong(crc32.getValue(), data, offset);

        Assert.assertTrue(CRC.Check(crc32, data, data.length, offset));
    }
}
