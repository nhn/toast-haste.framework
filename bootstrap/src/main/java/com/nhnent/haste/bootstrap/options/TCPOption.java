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

public class TCPOption<T> extends Constant {

    private static final ConstantOptions<TCPOption<Object>> pool;

    static {
        pool = new ConstantOptions<TCPOption<Object>>() {
            @Override
            protected TCPOption<Object> newConstant(int id, String name) {
                return new TCPOption<Object>(id, name);
            }
        };
    }

    public static <T> TCPOption<T> valueOf(String name) {
        return (TCPOption<T>) pool.valueOf(name);
    }

    protected TCPOption(int id, String name) {
        super(id, name);
    }

    public static final TCPOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");
    public static final TCPOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");
    public static final TCPOption<Integer> LISTEN_PORT = valueOf("PORT");
    public static final TCPOption<Integer> THREAD_COUNT = valueOf("PORT");
}
