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

import java.util.HashMap;
import java.util.Map;

public enum DataType {
    NONE((byte) 0),
    BYTE((byte) 1),
    BOOL((byte) 2),
    INT16((byte) 3),
    INT32((byte) 4),
    INT64((byte) 5),
    FLOAT((byte) 6),
    DOUBLE((byte) 7),
    STRING((byte) 8),

    BYTE_ARRAY((byte) 9),
    BOOL_ARRAY((byte) 10),
    INT16_ARRAY((byte) 11),
    INT32_ARRAY((byte) 12),
    INT64_ARRAY((byte) 13),
    FLOAT_ARRAY((byte) 14),
    DOUBLE_ARRAY((byte) 15),
    STRING_ARRAY((byte) 16),

    DataObject((byte) 17);

    private static final Map<Byte, DataType> typeMap;

    static {
        DataType[] dataTypes = DataType.values();
        typeMap = new HashMap<>(dataTypes.length);

        for (int i = 0; i < dataTypes.length; i++) {
            typeMap.put(dataTypes[i].getByte(), dataTypes[i]);
        }
    }

    private byte value;

    DataType(byte value) {
        this.value = value;
    }

    public byte getByte() {
        return this.value;
    }

    public static DataType getDataType(byte typeNum) {
        return typeMap.get(typeNum);
    }
}
