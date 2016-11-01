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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ObjectPoolBasicTest extends ObjectPoolTestBase {
    @Test
    public void testTakeOne() {
        TestObject testObject = TestObject.pool.take();
        Assert.assertNotNull(testObject);
    }

    @Test
    public void testTakeOneAnotherThread() throws InterruptedException, ExecutionException {
        final long currentThreadId = Thread.currentThread().getId();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<TestObject> future = executor.submit(new Callable<TestObject>() {
            @Override
            public TestObject call() throws Exception {
                Assert.assertNotEquals(currentThreadId, Thread.currentThread().getId());
                return TestObject.pool.take();
            }
        });

        TestObject testObject = future.get();
        Assert.assertNotNull(testObject);

        boolean result = TestObject.pool.release(testObject, testObject.getHandle());
        Assert.assertTrue(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTakeManyAnotherThread() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<TestObject>[] futures = new Future[TestObject.PRE_CREATE_COUNT];
        for (int i = 0; i < TestObject.PRE_CREATE_COUNT; i++) {
            futures[i] = executor.submit(new Callable<TestObject>() {
                @Override
                public TestObject call() throws Exception {
                    return TestObject.pool.take();
                }
            });
        }

        for (int i = 0; i < TestObject.PRE_CREATE_COUNT; i++) {
            TestObject testObject = futures[i].get();
            Assert.assertNotNull(testObject);

            boolean releaseResult = TestObject.pool.release(testObject, testObject.getHandle());
            Assert.assertTrue(releaseResult);
        }

        // A stack of pool is thread local.
        // So the count of stack must be gotten in produced thread using executor(single thread executor).
        Future<Integer> result = executor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return TestObject.pool.getStackCount();
            }
        });

        // Because objects were released in another thread,
        // Stack must be empty.
        Assert.assertEquals(0, result.get().intValue());
    }

    @Test
    public void testRelease() {
        TestObject testObject = TestObject.pool.take();
        Assert.assertNotNull(testObject);

        boolean result = TestObject.pool.release(testObject, testObject.getHandle());
        Assert.assertTrue(result);
    }

    @Test
    public void testProcessStack() {
        TestObject prevObject = TestObject.pool.take();
        prevObject.release();

        try (TestObject testObject = TestObject.pool.take()) {
            // Current object equals previous object,
            // because object pool is implemented using stack.
            if (testObject != prevObject) {
                Assert.fail("invalid operation as stack");
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testIncreaseCapacity() throws InterruptedException {
        int testCapacity = 1024;

        List<TestObject> objectList = new ArrayList<>(testCapacity);
        for (int i = 0; i < testCapacity; i++) {
            objectList.add(TestObject.pool.take());
        }

        for (int i = 0; i < objectList.size(); i++) {
            objectList.get(i).release();
        }

        Assert.assertEquals(testCapacity, TestObject.pool.getStackCapacity());
    }
}
