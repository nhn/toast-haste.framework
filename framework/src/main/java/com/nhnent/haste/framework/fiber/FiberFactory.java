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

package com.nhnent.haste.framework.fiber;

import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class FiberFactory {

    private FiberFactory() {
    }

    private static ExecutorService pool = null;

    private static PoolFiberFactory fiberFactory;

    public static boolean init(int threadCount, ThreadFactory threadFactory) {
        if (pool != null)
            return false;

        pool = Executors.newFixedThreadPool(threadCount, threadFactory);
        createFiberFactory();

        return true;
    }

    public static boolean init(ThreadFactory threadFactory) {
        if (pool != null)
            return false;

        pool = Executors.newCachedThreadPool(threadFactory);
        createFiberFactory();

        return true;
    }

    private static void createFiberFactory() {
        fiberFactory = new PoolFiberFactory(pool);
    }

    public static Fiber newFiber() {
        return fiberFactory.create();
    }
}
