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

package com.nhnent.haste.common;

public final class Check {
    private Check() {
    }

    public static <T> void NotNull(T param, String message) {
        if (param == null) {
            throw new NullPointerException(message + " is null.");
        }
    }

    public static void NotEmpty(String param, String message) {
        NotNull(param, message);

        if (param.isEmpty()) {
            throw new IllegalArgumentException(message + " is empty.");
        }
    }

    public static void EnsureIndex(byte[] target, int startIndex, int size) {
        if (startIndex < 0) {
            throw new IndexOutOfBoundsException("startIndex should be larger than zero");
        }

        if (target.length < (startIndex + size)) {
            throw new IndexOutOfBoundsException("target array is out of range");
        }
    }
}
