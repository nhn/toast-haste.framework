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

package com.nhnent.haste.bootstrap.options;

import com.nhnent.haste.common.Check;

import java.util.LinkedHashMap;
import java.util.Map;

public class UDPConfig {

    protected final Map<UDPOption<?>, Object> options = new LinkedHashMap<>();

    public UDPConfig() {
        option(UDPOption.THREAD_COUNT, Runtime.getRuntime().availableProcessors() * 2);

        option(UDPOption.SO_RCVBUF, 1024 * 256);
        option(UDPOption.SO_SNDBUF, 1024 * 16);

        option(UDPOption.LISTEN_PORT, 10000);
        option(UDPOption.ClIENT_PORT, 10500);

        option(UDPOption.MSS_SIZE, 1300);
        option(UDPOption.MAX_CONNNECTION, 2000);
    }

    public <T> UDPConfig option(UDPOption<T> option, T value) {
        Check.NotNull(option, "option");
        Check.NotNull(value, "value");

        synchronized (options) {
            options.put(option, value);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public final <T> T option(UDPOption<T> option) {
        synchronized (options) {
            return (T) options.get(option);
        }
    }
}
