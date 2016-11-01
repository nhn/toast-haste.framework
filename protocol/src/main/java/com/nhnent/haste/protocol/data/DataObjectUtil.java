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

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;

class DataObjectUtil {
    static final Charset UTF8 = Charset.forName("UTF-8");

    static DataType getType(Object object) {
        if (object instanceof Byte) {
            return DataType.BYTE;
        } else if (object instanceof Boolean) {
            return DataType.BOOL;
        } else if (object instanceof Short) {
            return DataType.INT16;
        } else if (object instanceof Integer) {
            return DataType.INT32;
        } else if (object instanceof Long) {
            return DataType.INT64;
        } else if (object instanceof Float) {
            return DataType.FLOAT;
        } else if (object instanceof Double) {
            return DataType.DOUBLE;
        } else if (object instanceof String) {
            return DataType.STRING;
        } else if (object instanceof Byte[]) {
            return DataType.BYTE_ARRAY;
        } else if (object instanceof Boolean[]) {
            return DataType.BOOL_ARRAY;
        } else if (object instanceof Short[]) {
            return DataType.INT16_ARRAY;
        } else if (object instanceof Integer[]) {
            return DataType.INT32_ARRAY;
        } else if (object instanceof Long[]) {
            return DataType.INT64_ARRAY;
        } else if (object instanceof Float[]) {
            return DataType.FLOAT_ARRAY;
        } else if (object instanceof Double[]) {
            return DataType.DOUBLE_ARRAY;
        } else if (object instanceof String[]) {
            return DataType.STRING_ARRAY;
        } else if (object instanceof DataObject) {
            return DataType.DataObject;
        } else {
            return DataType.NONE;
        }
    }

    private static Object readPrimitiveData(DataType dataType, ByteWrapper byteWrapper) {
        switch (dataType) {
            case BYTE:
                return byteWrapper.readByte();
            case BOOL:
                return byteWrapper.readBoolean();
            case INT16:
                return byteWrapper.readShort();
            case INT32:
                return byteWrapper.readInt();
            case INT64:
                return byteWrapper.readLong();
            case FLOAT:
                return byteWrapper.readFloat();
            case DOUBLE:
                return byteWrapper.readDouble();
            case STRING: {
                int length = byteWrapper.readInt();
                byte[] bytes = byteWrapper.readBytes(length);
                return new String(bytes, UTF8);
            }
            default:
                break;
        }
        return null;
    }

    private static Object readArrayData(DataType dataType, ByteWrapper byteWrapper) {
        int length = byteWrapper.readInt();
        switch (dataType) {
            case BYTE_ARRAY: {
                byte[] values = new byte[length];
                for (int i = 0; i < length; i++) {
                    values[i] = byteWrapper.readByte();
                }
                return values;
            }
            case BOOL_ARRAY: {
                boolean[] values = new boolean[length];
                for (int i = 0; i < length; i++) {
                    values[i] = byteWrapper.readBoolean();
                }
                return values;
            }
            case INT16_ARRAY: {
                short[] values = new short[length];
                for (int i = 0; i < length; i++) {
                    values[i] = byteWrapper.readShort();
                }
                return values;
            }
            case INT32_ARRAY: {
                int[] values = new int[length];
                for (int i = 0; i < length; i++) {
                    values[i] = byteWrapper.readInt();
                }
                return values;
            }
            case INT64_ARRAY: {
                long[] values = new long[length];
                for (int i = 0; i < length; i++) {
                    values[i] = byteWrapper.readLong();
                }
                return values;
            }
            case FLOAT_ARRAY: {
                float[] values = new float[length];
                for (int i = 0; i < length; i++) {
                    values[i] = byteWrapper.readFloat();
                }
                return values;
            }
            case DOUBLE_ARRAY: {
                double[] values = new double[length];
                for (int i = 0; i < length; i++) {
                    values[i] = byteWrapper.readDouble();
                }
                return values;
            }
            case STRING_ARRAY: {
                String[] values = new String[length];
                for (int i = 0; i < length; i++) {
                    int strLen = byteWrapper.readInt();
                    byte[] strBytes = byteWrapper.readBytes(strLen);

                    values[i] = new String(strBytes, UTF8);
                }
                return values;
            }
            default:
                break;
        }
        return null;
    }

    private static Object readDataObject(ByteWrapper byteWrapper) {
        DataObject dataObject = new DataObject();
        int count = byteWrapper.readInt();
        for (int i = 0; i < count; i++) {
            byte key = byteWrapper.readByte();
            dataObject.dataMap.put(key, readData(byteWrapper));
        }
        return dataObject;
    }

    private static void writePrimitiveData(DataType type, Object value, ByteWrapper byteWrapper) {
        switch (type) {
            case BYTE:
                byteWrapper.writeByte((byte) value);
                break;
            case BOOL:
                byteWrapper.writeBoolean((boolean) value);
                break;
            case INT16:
                byteWrapper.writeShort((short) value);
                break;
            case INT32:
                byteWrapper.writeInt((int) value);
                break;
            case INT64:
                byteWrapper.writeLong((long) value);
                break;
            case FLOAT:
                byteWrapper.writeFloat((float) value);
                break;
            case DOUBLE:
                byteWrapper.writeDouble((double) value);
                break;
            case STRING: {
                String str = (String) value;
                byte[] strBytes = str.getBytes(UTF8);
                byteWrapper.writeInt(strBytes.length);
                byteWrapper.writeBytes(strBytes);
                break;
            }
            default:
                break;
        }
    }

    private static void writeArrayData(DataType dataType, Object value, ByteWrapper byteWrapper) {
        int length = Array.getLength(value);
        byteWrapper.writeInt(length);
        switch (dataType) {
            case BYTE_ARRAY: {
                byte[] values = (byte[]) value;
                for (int i = 0; i < length; i++) {
                    byteWrapper.writeByte(values[i]);
                }
                break;
            }
            case BOOL_ARRAY: {
                boolean[] values = (boolean[]) value;
                for (int i = 0; i < length; i++) {
                    byteWrapper.writeBoolean(values[i]);
                }
                break;
            }
            case INT16_ARRAY: {
                short[] values = (short[]) value;
                for (int i = 0; i < length; i++) {
                    byteWrapper.writeShort(values[i]);
                }
                break;
            }
            case INT32_ARRAY: {
                int[] values = (int[]) value;
                for (int i = 0; i < length; i++) {
                    byteWrapper.writeInt(values[i]);
                }
                break;
            }
            case INT64_ARRAY: {
                long[] values = (long[]) value;
                for (int i = 0; i < length; i++) {
                    byteWrapper.writeLong(values[i]);
                }
                break;
            }
            case FLOAT_ARRAY: {
                float[] values = (float[]) value;
                for (int i = 0; i < length; i++) {
                    byteWrapper.writeFloat(values[i]);
                }
                break;
            }
            case DOUBLE_ARRAY: {
                double[] values = (double[]) value;
                for (int i = 0; i < length; i++) {
                    byteWrapper.writeDouble(values[i]);
                }
                break;
            }
            case STRING_ARRAY: {
                String[] values = (String[]) value;
                for (int i = 0; i < length; i++) {
                    byte[] strBytes = values[i].getBytes(UTF8);
                    byteWrapper.writeInt(strBytes.length);
                    byteWrapper.writeBytes(strBytes);
                }
                break;
            }
            default:
                break;
        }
    }

    private static void writeDataObject(Object value, ByteWrapper byteWrapper) {
        if (value instanceof DataObject) {
            DataObject dataObject = (DataObject) value;
            int count = dataObject.dataMap.size();
            byteWrapper.writeInt(count);
            for (byte k : dataObject.dataMap.keySet()) {
                DataWrapper v = dataObject.dataMap.get(k);
                byteWrapper.writeByte(k);
                writeData(v, byteWrapper);
            }
        } else {
            throw new InvalidParameterException("value is not dataObject!");
        }
    }

    static DataWrapper readData(ByteWrapper byteWrapper) {
        DataWrapper dataWrapper = new DataWrapper();
        byte typeNum = byteWrapper.readByte();
        dataWrapper.type = DataType.getDataType(typeNum);
        switch (dataWrapper.type) {
            case NONE:
                break;
            case BYTE:
            case BOOL:
            case INT16:
            case INT32:
            case INT64:
            case FLOAT:
            case DOUBLE:
            case STRING:
                dataWrapper.value = readPrimitiveData(dataWrapper.type, byteWrapper);
                break;
            case BYTE_ARRAY:
            case BOOL_ARRAY:
            case INT16_ARRAY:
            case INT32_ARRAY:
            case INT64_ARRAY:
            case FLOAT_ARRAY:
            case DOUBLE_ARRAY:
            case STRING_ARRAY:
                dataWrapper.value = readArrayData(dataWrapper.type, byteWrapper);
                break;
            case DataObject:
                dataWrapper.value = readDataObject(byteWrapper);
                break;
            default:
                break;
        }
        return dataWrapper;
    }

    static void writeData(DataWrapper wrapper, ByteWrapper byteWrapper) {
        DataType type = wrapper.type;
        byteWrapper.writeByte(type.getByte());
        switch (type) {
            case NONE:
                break;
            case BYTE:
            case BOOL:
            case INT16:
            case INT32:
            case INT64:
            case FLOAT:
            case DOUBLE:
            case STRING:
                writePrimitiveData(type, wrapper.value, byteWrapper);
                break;
            case BYTE_ARRAY:
            case BOOL_ARRAY:
            case INT16_ARRAY:
            case INT32_ARRAY:
            case INT64_ARRAY:
            case FLOAT_ARRAY:
            case DOUBLE_ARRAY:
            case STRING_ARRAY:
                writeArrayData(type, wrapper.value, byteWrapper);
                break;
            case DataObject:
                writeDataObject(wrapper.value, byteWrapper);
                break;
            default:
                break;
        }
    }
}
