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

package com.nhnent.haste.example.echoserver;

import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.ServerApplication;
import com.nhnent.haste.protocol.messages.InitialRequest;
import com.nhnent.haste.transport.NetworkPeer;

public class EchoServerApplication extends ServerApplication {
    @Override
    protected void setup() {
    }

    @Override
    protected void tearDown() {
    }

    @Override
    protected ClientPeer createPeer(InitialRequest initialRequest, NetworkPeer networkPeer) {
        return new EchoPeer(initialRequest, networkPeer);
    }
}
