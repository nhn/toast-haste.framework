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

import com.nhnent.haste.common.Check;
import com.nhnent.haste.common.EnvironmentTimer;
import com.nhnent.haste.transport.EventExecutorGroup;
import com.nhnent.haste.transport.TransportProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class NioEventLoop implements EventLoop {
    private static final Logger logger = LoggerFactory.getLogger(NioEventLoop.class);

    private static final int CLOSE_WAIT_MS = 500;

    private boolean isRunning = true;

    private final EventExecutorGroup parent;
    private final ExecutorService executorService;
    private final Selector selector;
    private SelectedSelectionKeySet selectedKeys;

    private TransportProxy transportProxy;

    private long selectorTimeout = 0;

    public NioEventLoop(EventExecutorGroup parent, ExecutorService executorService) throws IOException {
        Check.NotNull(parent, "parent");
        Check.NotNull(executorService, "executorService");

        this.parent = parent;
        this.executorService = executorService;
        selector = SelectorProvider.provider().openSelector();

        try {
            SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

            Class<?> selectorImplClass = Class.forName("sun.nio.ch.SelectorImpl", false, getSystemClassLoader());

            // Ensure the current selector implementation is what we can instrument.
            if (!selectorImplClass.isAssignableFrom(selector.getClass())) {
                return;
            }

            Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
            Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

            selectedKeysField.setAccessible(true);
            publicSelectedKeysField.setAccessible(true);

            selectedKeysField.set(selector, selectedKeySet);
            publicSelectedKeysField.set(selector, selectedKeySet);

            selectedKeys = selectedKeySet;
        } catch (Throwable t) {
            selectedKeys = null;
        }
    }

    static ClassLoader getSystemClassLoader() {
        if (System.getSecurityManager() == null) {
            return ClassLoader.getSystemClassLoader();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return ClassLoader.getSystemClassLoader();
                }
            });
        }
    }

    public EventExecutorGroup parent() {
        return parent;
    }

    private void startThread() {
        isRunning = true;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                NioEventLoop.this.run();
            }
        });
    }

    private void run() {
        while (isRunning) {
            if (Thread.currentThread().isInterrupted())
                break;

            select(selectorTimeout);

            processKeys();
            processSend();
        }
    }

    private void select(long timeoutMills) {
        try {
            selector.select(timeoutMills);
        } catch (IOException e) {
            logger.error("Failed to select", e);
        }
    }

    private void processKeys() {
        processKeys(selectedKeys.flip(), EnvironmentTimer.currentTimeMillis());
    }

    private void processKeys(SelectionKey[] selectedKeys, long currentTime) {
        for (int i = 0; ; i++) {
            final SelectionKey k = selectedKeys[i];
            if (k == null) {
                break;
            }

            selectedKeys[i] = null;
            processKey(k, currentTime);
        }
    }

    private void processSend() {
        transportProxy.send(EnvironmentTimer.currentTimeMillis());
    }

    private void processKey(SelectionKey key, long currentTime) {
        if (!key.isValid())
            return;

        int readyOps = key.readyOps();

        switch (readyOps) {
            case SelectionKey.OP_READ:
                transportProxy.read(currentTime);
                break;
            case SelectionKey.OP_ACCEPT:
                transportProxy.accept(currentTime);
                break;
            case SelectionKey.OP_WRITE:
                transportProxy.write(currentTime);
                break;
            default:
                break;
        }
    }

    @Override
    public void start() {
        startThread();
    }

    @Override
    public void close() throws IOException {
        isRunning = false;

        try {
            executorService.shutdown();
            executorService.awaitTermination(CLOSE_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

        transportProxy.close();
        selector.close();
    }

    @Override
    public Selector selector() {
        return selector;
    }

    @Override
    public void registerProxy(TransportProxy transportProxy) {
        this.transportProxy = transportProxy;
    }

    @Override
    public void setSelectorTimeout(long milliseconds) {
        this.selectorTimeout = milliseconds;
    }
}