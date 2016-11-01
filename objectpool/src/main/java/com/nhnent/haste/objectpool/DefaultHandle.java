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

package com.nhnent.haste.objectpool;

import java.util.Map;

public class DefaultHandle<T> implements Handle {
    Stack dstStack;
    T object;

    DefaultHandle(Stack dstStack) {
        this.dstStack = dstStack;
    }

    @Override
    public void release() {
        Thread currentThread = Thread.currentThread();
        if (dstStack.getOwnerThread() == currentThread) {
            if (dstStack != null) {
                dstStack.tryPush(this);
            }
        } else {
            // TODO If released thread is not same owner thread
            Map<Stack, DelayedPool> delayedPoolMap = ObjectPool.delayedPooMap.get();
            DelayedPool delayedPool = delayedPoolMap.get(dstStack);
            if (delayedPool == null) {
                delayedPoolMap.put(dstStack, (delayedPool = new DelayedPool(currentThread, dstStack)));
            }
            delayedPool.add(this);
        }
    }
}
