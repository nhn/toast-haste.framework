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

package com.nhnent.haste.framework;

import com.nhnent.haste.protocol.data.DataSchema;
import com.nhnent.haste.protocol.data.FieldParameter;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.nhnent.haste.protocol.messages.ResponseMessage;

/**
 * {@link MessageBridge} is a helper class in order to easy to create a message.
 * In constructor, convert to fields that were annotated {@link FieldParameter} in this object from {@link RequestMessage}.
 * <p>For example,</p>
 * <pre>
 * public class CustomMessage extends MessageBridge {
 *      public CustomMessage(RequestMessage request) {
 *          super(request);
 *      }
 *
 *      <b>@FieldParameter(Code = 0)</b>
 *      public String message;
 * }
 * </pre>
 * @see DataSchema
 */
public class MessageBridge extends DataSchema {
    private static final String INVALID_ERROR = "Invalid data!";

    private short code;

    private String errorMessage;

    /**
     * Get the code.
     * @return A code of this message.
     */
    public short getCode() {
        return code;
    }

    /**
     * Get the error message.
     * @return Error message. If successed converting, this message is null.
     * <p>Instead of checking whether this error message is null or not, recommend to use {@link MessageBridge#isValid()}.</p>
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Default constructor of {@link MessageBridge}.
     * Convert to fields that were annotated {@link FieldParameter} in this object from {@link RequestMessage}.
     * If you want to check success or failure, call {@link MessageBridge#isValid()}
     * @param request A request message.
     */
    public MessageBridge(RequestMessage request) {
        super(request.getDataObject());
        this.code = request.getCode();

        if (!isValid()) {
            errorMessage = INVALID_ERROR;
        }
    }

    /**
     * Convert to ResponseMessage from this object. A code of this response message is same to the request message in constructor.
     * @return A response message that converted.
     */
    public ResponseMessage toResponse() {
        return new ResponseMessage(this.code, this.toDataObject());
    }

    @Override
    public String toString() {
        return "MessageBridge{" +
                "code=" + code +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
