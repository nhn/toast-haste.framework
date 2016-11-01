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

package com.nhnent.haste.protocol;

public enum ReturnCode {
    UNKNOWN_ERROR(-1),
    FAILED_TO_CREATE_CLIENT_PEER(-2),
    OK(1);

    private short value;

    ReturnCode(int value) {
        this.value = (short) value;
    }

    public Short getShort() {
        return value;
    }

    public static ReturnCode getCode(short code) {
        switch (code) {
            case -1:
                return UNKNOWN_ERROR;
            case -2:
                return FAILED_TO_CREATE_CLIENT_PEER;
            case 1:
                return OK;
            default:
                return UNKNOWN_ERROR;
        }
    }
}
