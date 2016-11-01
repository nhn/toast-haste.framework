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

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataWrapper {
    private static final ConcurrentMap<Class, Class> primitiveObjectTypeMap = new ConcurrentHashMap<>();

    public DataType type;
    public Object value;

    public DataWrapper() {
    }

    public DataWrapper(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    private boolean isSameForPrimitiveType(Class<?> primitiveType, Class<?> cls) {
        try {
            if (!primitiveObjectTypeMap.containsKey(cls)) {
                Field typeField = cls.getDeclaredField("TYPE");
                Class<?> type = (Class<?>) typeField.get(null);
                primitiveObjectTypeMap.put(cls, type);
            }

            Class<?> clsPrimitiveType = primitiveObjectTypeMap.get(cls);
            return clsPrimitiveType.equals(primitiveType);
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isValidType(Class<?> clazz) {
        Class<?> thisType = this.value.getClass();
        if (thisType.equals(clazz)) {
            return true;
        }

        if (clazz.isPrimitive()) {
            return isSameForPrimitiveType(clazz, thisType);
        }
        return false;
    }

    @Override
    public String toString() {
        return MessageFormat.format("'{'type={0},value={1}'}'", type, value);
    }
}
