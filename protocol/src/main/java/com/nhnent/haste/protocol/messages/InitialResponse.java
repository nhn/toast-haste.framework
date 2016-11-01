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

import com.nhnent.haste.protocol.ReturnCode;

public class InitialResponse {
    private final ReturnCode resultCode;
    private final short messageLength;
    private final String message;

    public InitialResponse(ReturnCode resultCode, String message) {
        this.resultCode = resultCode;
        this.messageLength = (short) message.length();
        this.message = message;
    }

    public short getResultCode() {
        return resultCode.getShort();
    }

    public short getMessageLength() {
        return messageLength;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "InitialResponse{" +
                "resultCode=" + resultCode +
                ", messageLength=" + messageLength +
                ", message='" + message + '\'' +
                '}';
    }
}
