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

import com.nhnent.haste.common.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * {@link DataSchema} is a helper class in order to easy to convert the {@link DataObject}, and also vice versa.
 * In constructor, convert to fields that were annotated {@link FieldParameter} in this object from {@link DataObject}.
 * </p>
 * <br/>
 * <p><b>Object to DataObject</b></p>
 * <p>Reference to {@link DataSchema#toDataObject()}</p>
 * <br/>
 * <p><b>DataObject to Object</b></p>
 * <p>Reference to {@link DataSchema#DataSchema(DataObject)} constructor</p>
 * <br/>
 * <p><b>For example,</b></p>
 * <pre>
 * public class CustomData extends DataSchema {
 *      public CustomData(DataObject data) {
 *          super(data);
 *      }
 *
 *      <b>@FieldParameter(Code = 0)</b>
 *      public String message;
 * }
 * </pre>
 *
 * @see DataObject
 */
public abstract class DataSchema {
    private static final Logger logger = LoggerFactory.getLogger(DataSchema.class);

    private static final Map<Class, List<Field>> fieldMap = new ConcurrentHashMap<>();
    private static final Map<Field, FieldParameter> fieldParameterMap = new ConcurrentHashMap<>();

    private boolean isValid;

    protected DataSchema() {
        this.isValid = true;
    }

    protected DataSchema(DataObject data) {
        Check.NotNull(data, "data");
        setDataToFields(cachedFields(this.getClass()), data);
    }

    /**
     * Get cached fields that were attached {@link FieldParameter}.
     * If have not cached fields ever, cache fields of a class.
     *
     * @param clazz A target class.
     * @return A field list.
     */
    private List<Field> cachedFields(Class clazz) {
        if (!fieldMap.containsKey(clazz)) {
            List<Field> fieldList = new ArrayList<>();

            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                FieldParameter fieldParameter = field.getAnnotation(FieldParameter.class);
                if (fieldParameter == null) {
                    continue;
                }

                fieldList.add(field);
                fieldParameterMap.put(field, fieldParameter);
            }

            fieldMap.put(clazz, fieldList);
        }

        return fieldMap.get(clazz);
    }

    private void setDataToFields(List<Field> fields, DataObject data) {
        for (Field field : fields) {
            try {
                if (!field.isAccessible()) {
                    // If this field couldn't access, enforce to access.
                    field.setAccessible(true);
                }
                this.isValid = trySetField(field, data);
                if (!this.isValid) {
                    break;
                }
            } catch (Throwable e) {
                logger.error("Wrong protocol", e);
            }
        }
    }

    private boolean trySetField(Field field, DataObject dataObject) throws IllegalAccessException {
        FieldParameter fieldParameter = fieldParameterMap.get(field);
        if (fieldParameter == null) {
            return true;
        }

        byte code = fieldParameter.Code();
        boolean isOptional = fieldParameter.IsOptional();

        DataWrapper dataWrapper = dataObject.get(code);

        if (dataWrapper != null && dataWrapper.value != null) {
            if (dataWrapper.isValidType(field.getType())) {
                field.set(this, dataWrapper.value);
                return true;
            }
        }

        return isOptional;
    }

    /**
     * Validate this object.
     *
     * @return {@code true} if this object was valid {@code false} otherwise.
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Convert {@link DataObject} from this object.
     *
     * @return A data object that converted.
     */
    public DataObject toDataObject() {
        DataObject dataObject = new DataObject();

        List<Field> fields = cachedFields(this.getClass());
        for (int i = 0; i < fields.size(); i++) {
            FieldParameter fieldParameter = fieldParameterMap.get(fields.get(i));
            if (fieldParameter != null) {
                try {
                    if (!fields.get(i).isAccessible()) {
                        fields.get(i).setAccessible(true);
                    }
                    Object value = fields.get(i).get(this);
                    if (value != null) {
                        dataObject.set(fieldParameter.Code(), value);
                    }
                } catch (IllegalAccessException e) {
                    logger.error("Failed to parse DataSchema", e);
                }
            }
        }
        return dataObject;
    }
}
