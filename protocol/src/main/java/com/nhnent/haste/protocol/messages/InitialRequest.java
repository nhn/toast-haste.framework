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

import com.nhnent.haste.common.Version;
import com.nhnent.haste.protocol.Protocol;

import java.util.Arrays;

public class InitialRequest {
    private Protocol protocol;
    private Version sdkVersion;
    private Version clientVersion;
    private byte[] customData;

    private String errorMessage;

    public InitialRequest protocol(Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public InitialRequest sdkVersion(Version sdkVersion) {
        this.sdkVersion = sdkVersion;
        return this;
    }

    public InitialRequest clientVersion(Version clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public InitialRequest customData(byte[] customData) {
        this.customData = customData;
        return this;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Version getSDKVersion() {
        return sdkVersion;
    }

    public Version getClientVersion() {
        return clientVersion;
    }

    public byte[] getCustomData() {
        return customData;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "InitialRequest{" +
                "protocol=" + protocol +
                ", sdkVersion=" + sdkVersion +
                ", clientVersion=" + clientVersion +
                ", customData=" + Arrays.toString(customData) +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
