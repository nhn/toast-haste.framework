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

/**
 * Key-Value container for containing data. Key is {@link Byte}, Value is {@link DataWrapper}.
 *
 * @see DataWrapper
 */
public class DataObject {
    private static final int DEFAULT_CAPACITY = 2048;
    Map<Byte, DataWrapper> dataMap;

    /**
     * Default constructor.
     */
    public DataObject() {
        dataMap = new HashMap<>();
    }

    /**
     * Checks whether this key exists or not.
     *
     * @param key The key that wants to find.
     * @return {@code true} if this key exists {@code false} otherwise.
     */
    public boolean contains(byte key) {
        return dataMap.containsKey(key);
    }

    /**
     * Get the value that mapped this key. This method returns {@link DataWrapper}.
     * If wants to get a real value, uses {@link DataWrapper#value}
     *
     * @param key The key that wants to find.
     * @return The wrapper wrapping the real value.
     */
    public DataWrapper get(byte key) {
        return dataMap.get(key);
    }

    /**
     * Set this value that mapped this key.
     *
     * @param key   The key that wants to find.
     * @param value The value that wants to put.
     * @return A reference of this {@link DataObject} object for method chaining.
     */
    public DataObject set(byte key, Object value) {
        DataWrapper wrapper = new DataWrapper(DataObjectUtil.getType(value), value);
        dataMap.put(key, wrapper);
        return this;
    }

    /**
     * Clear all keys and values.
     */
    public void clear() {
        dataMap.clear();
    }

    /**
     * Serialize from {@link DataObject} to byte array.
     * (Use the default capacity : {@link DataObject#DEFAULT_CAPACITY})
     *
     * @return Serialized byte array.
     */
    public byte[] serialize() {
        return this.serialize(DEFAULT_CAPACITY);
    }

    /**
     * Serialize from {@link DataObject} to byte array.
     *
     * @return Serialized byte array.
     */
    public byte[] serialize(int capacity) {
        ByteWrapper byteWrapper = new ByteWrapper(capacity);
        byteWrapper.writeInt(dataMap.size());
        for (byte key : dataMap.keySet()) {
            DataWrapper data = dataMap.get(key);
            byteWrapper.writeByte(key);
            DataObjectUtil.writeData(data, byteWrapper);
        }
        return byteWrapper.toArray();
    }

    /**
     * Convert to a {@link DataObject} from a byte array.
     *
     * @param src The byte array that wants to convert to {@link DataObject}.
     * @return The {@link DataObject} that converted from a byte array.
     */
    public static DataObject toDataObject(byte[] src) {
        ByteWrapper byteWrapper = new ByteWrapper(src);
        int count = byteWrapper.readInt();

        DataObject obj = new DataObject();
        for (int i = 0; i < count; i++) {
            byte key = byteWrapper.readByte();
            obj.dataMap.put(key, DataObjectUtil.readData(byteWrapper));
        }

        return count > 0 ? obj : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (byte key : dataMap.keySet()) {
            builder.append(key).append(":");
            builder.append(dataMap.get(key).toString()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1).append("]");
        return builder.toString();
    }
}
