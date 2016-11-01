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

import java.text.MessageFormat;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ObjectPoolAdvancedTest extends ObjectPoolTestBase {
    @Test
    public void testPreCreateEachThread() throws InterruptedException {
        class TestRunnable implements Runnable {
            int expectedCapacity = -1;
            int currentStackCount = -2;

            @Override
            public void run() {
                ObjectPool pool = TestObject.pool;

                this.expectedCapacity = TestObject.PRE_CREATE_COUNT;
                this.currentStackCount = pool.getStackCount();
            }
        }

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        TestRunnable[] runnables = new TestRunnable[threadCount];

        for (int i = 0; i < threadCount; i++) {
            executor.execute((runnables[i] = new TestRunnable()));
        }

        executor.shutdown();
        executor.awaitTermination(100, TimeUnit.SECONDS);

        for (int i = 0; i < threadCount; i++) {
            Assert.assertEquals(runnables[i].expectedCapacity, runnables[i].currentStackCount);
        }
    }

    @Test
    public void testOneReleaseAnotherThread() throws InterruptedException {
        Assert.assertEquals(TestObject.PRE_CREATE_COUNT, TestObject.pool.getStackCount());

        final TestObject testObject = TestObject.pool.take();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                testObject.release();
            }
        });
        thread.start();
        thread.join();

        // Although testObject was released, it didn't be released.
        // Because this object was released in another thread.
        Assert.assertEquals(TestObject.PRE_CREATE_COUNT - 1, TestObject.pool.getStackCount());

        Assert.assertTrue(TestObject.pool.scavengeAll());
        Assert.assertEquals(TestObject.PRE_CREATE_COUNT, TestObject.pool.getStackCount());
    }

    @Test
    public void testManyReleaseAnotherThread() throws InterruptedException {
        Assert.assertEquals(TestObject.PRE_CREATE_COUNT, TestObject.pool.getStackCount());

        final int count = TestObject.PRE_CREATE_COUNT;
        final TestObject[] testObjects = new TestObject[count];
        for (int i = 0; i < count; i++) {
            testObjects[i] = TestObject.pool.take();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        for (int i = 0; i < count; i++) {
            executor.execute(new Runnable() {
                int index;

                @Override
                public void run() {
                    testObjects[index].release();
                }

                public Runnable setIndex(int index) {
                    this.index = index;
                    return this;
                }
            }.setIndex(i));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Assert.assertEquals(0, TestObject.pool.getStackCount());

        // After scavenging, the count of objects in pool is same to link capacity.
        Assert.assertTrue(TestObject.pool.scavengeAll());
        Assert.assertEquals(DelayedPool.LINK_CAPACITY, TestObject.pool.getStackCount());

        Assert.assertTrue(TestObject.pool.scavengeAll());
    }

    @Test
    public void testUnnecessaryCreateInstance() throws InterruptedException, ExecutionException {
        final int testCount = 1000;

        TestObject.pool = new ObjectPool<TestObject>() {
            @Override
            protected TestObject newInstance(Handle handle) {
                return new TestObject(handle);
            }
        };

        TestObject prevObject = TestObject.pool.take();
        prevObject.release();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        final Random rnd = new Random();
        for (int i = 0; i < testCount; i++) {
            final TestObject testObject = TestObject.pool.take();

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.currentThread().sleep(rnd.nextInt(3));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        testObject.release();
                    }
                }
            });
            Thread.currentThread().sleep(rnd.nextInt(3));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        while (TestObject.pool.scavengeAll()) {
        }

        int stackCount = TestObject.pool.getStackCount();
        String failMsg = MessageFormat.format("stack count is {0}", stackCount);

        System.out.println(TestObject.pool.getStackCount());
        Assert.assertTrue(failMsg, stackCount > 1);
    }
}