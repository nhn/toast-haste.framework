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

public class UDPOption<T> extends Constant {

    private static final ConstantOptions<UDPOption<Object>> pool;

    static {
        pool = new ConstantOptions<UDPOption<Object>>() {
            @Override
            protected UDPOption<Object> newConstant(int id, String name) {
                return new UDPOption<Object>(id, name);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> UDPOption<T> valueOf(String name) {
        return (UDPOption<T>) pool.valueOf(name);
    }

    private UDPOption(int id, String name) {
        super(id, name);
    }

    public static final UDPOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");
    public static final UDPOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");
    public static final UDPOption<Integer> LISTEN_PORT = valueOf("LISTEN_PORT");
    public static final UDPOption<Integer> ClIENT_PORT = valueOf("CLIENT_PORT");
    public static final UDPOption<Integer> THREAD_COUNT = valueOf("THREAD_COUNT");
    public static final UDPOption<Integer> MSS_SIZE = valueOf("MSS_SIZE");
    public static final UDPOption<Integer> MAX_CONNNECTION = valueOf("MAX_CONNNECTION");

}
