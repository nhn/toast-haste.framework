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

package com.nhnent.haste.protocol.messages;

import com.nhnent.haste.protocol.data.DataSchema;
import com.nhnent.haste.protocol.data.DataObject;

public abstract class Message {
    protected short code;

    protected DataObject dataObject;

    protected Message(short code) {
        this(code, new DataObject());
    }

    protected Message(short code, DataObject dataObject) {
        this.code = code;
        this.dataObject = dataObject;
    }

    protected Message(short code, DataSchema dataSchema) {
        this.code = code;
        this.dataObject = dataSchema.toDataObject();
    }

    public short getCode() {
        return code;
    }

    public DataObject getDataObject() {
        return dataObject;
    }

    public abstract MessageType getMessageType();

    public static Message toMessage(MessageType type, short code, DataObject dataObject) {
        Message message = null;
        switch (type) {
            case REQUEST_MESSAGE:
                message = new RequestMessage(code, dataObject);
                break;
            case RESPONSE_MESSAGE:
                message = new ResponseMessage(code, dataObject);
                break;
            case EVENT_MESSAGE:
                message = new EventMessage(code, dataObject);
                break;
        }
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "code=" + code +
                ", dataObject=" + dataObject +
                '}';
    }
}
