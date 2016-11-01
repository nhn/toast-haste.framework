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

package com.nhnent.haste.bootstrap.threading;

import com.nhnent.haste.bootstrap.loop.EventLoop;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ExecutorService} for running a work in a new thread, for example {@link EventLoop}.
 * If calls the {@link ThreadPerTaskExecutor#execute(Runnable)}, a thread factory creates a new thread and executes command.
 */
public class ThreadPerTaskExecutor extends AbstractExecutorService {
    private final ThreadFactory threadFactory;

    private Thread thread;
    private Runnable command;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        this.command = command;
        this.thread = threadFactory.newThread(command);
        this.thread.start();
    }

    @Override
    public void shutdown() {
        if (!this.thread.isInterrupted()) {
            this.thread.interrupt();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.singletonList(command);
    }

    @Override
    public boolean isShutdown() {
        return !this.thread.isAlive();
    }

    @Override
    public boolean isTerminated() {
        return !this.thread.isAlive();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long start = System.currentTimeMillis();
        for (; ; ) {
            if (isTerminated())
                return true;
            long current = System.currentTimeMillis();
            if (current - start >= unit.toMillis(timeout)) {
                return false;
            }
        }
    }
}
