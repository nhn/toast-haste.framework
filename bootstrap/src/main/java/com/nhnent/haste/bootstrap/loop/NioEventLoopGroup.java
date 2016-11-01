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

package com.nhnent.haste.bootstrap.loop;

import com.nhnent.haste.bootstrap.threading.DefaultThreadFactory;
import com.nhnent.haste.bootstrap.threading.ThreadPerTaskExecutor;
import com.nhnent.haste.transport.EventExecutor;
import com.nhnent.haste.transport.EventExecutorGroup;
import com.nhnent.haste.transport.Transport;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public class NioEventLoopGroup implements EventExecutorGroup, EventLoopGroup {
    private final int THREAD_COUNT;

    private final EventLoop listener;

    private final EventLoop[] children;

    private final Transport transport;

    public NioEventLoopGroup(Transport transport) {
        this(transport, null);
    }

    public NioEventLoopGroup(Transport transport, ThreadFactory threadFactory) {
        this.transport = transport;

        THREAD_COUNT = transport.threadCount();

        if (threadFactory == null)
            threadFactory = new DefaultThreadFactory();

        ExecutorService executorService = new ThreadPerTaskExecutor(threadFactory);

        int threadCount = THREAD_COUNT <= 1 ? 1 : THREAD_COUNT - 1;
        children = new NioEventLoop[threadCount];

        listener = newEventLoop(executorService);

        for (int i = 0; i < children.length; i++) {
            children[i] = newEventLoop(executorService);
        }

        transport.register(this);
    }

    protected NioEventLoop newEventLoop(ExecutorService executorService) {
        try {
            return new NioEventLoop(this, executorService);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void start() {
        for (EventLoop child : children) {
            child.start();
        }
        listener.start();
    }

    @Override
    public void close() throws IOException {
        for (EventLoop child : children) {
            child.close();
        }
        listener.close();
    }

    public Transport transport() {
        return transport;
    }

    @Override
    public EventExecutor listenerExecutor() {
        return listener;
    }

    @Override
    public EventExecutor[] childExecutors() {
        return children;
    }
}

