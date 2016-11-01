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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The object pool based on thread-local. (inspired by Recycler of Netty)
 * @param <T> the type of a pooled target object
 */
public abstract class ObjectPool<T extends Poolable> {
    private static final Logger logger = LoggerFactory.getLogger(ObjectPool.class);

    // TODO Use lambda when upgrading java 1.8
    public interface Initializer<PooledType extends Poolable> {
        void initialize(PooledType target, Object... parameters);
    }

    private ThreadLocal<Stack> internalStack;
    static ThreadLocal<Map<Stack, DelayedPool>> delayedPooMap = new ThreadLocal<Map<Stack, DelayedPool>>() {
        @Override
        protected Map<Stack, DelayedPool> initialValue() {
            // TODO Consider to change WeakHashMap.
            return new WeakHashMap<>();
        }
    };

    static final int DEFAULT_MAX_CAPACITY = 262144;

    public ObjectPool() {
        this(DEFAULT_MAX_CAPACITY, false, 0);
    }

    public ObjectPool(final boolean isPreCreate, final int preCreatedCount) {
        this(DEFAULT_MAX_CAPACITY, isPreCreate, preCreatedCount);
    }

    public ObjectPool(final int maxCapacity, final boolean isPreCreate, final int preCreatedCount) {
        final ObjectPool<T> thisObject = this;
        internalStack = new ThreadLocal<Stack>() {
            @Override
            protected Stack initialValue() {
                int maxStackcapacity = Math.min(maxCapacity, DEFAULT_MAX_CAPACITY);
                return new Stack(thisObject, Thread.currentThread(), maxStackcapacity, isPreCreate, preCreatedCount);
            }
        };
    }

    DefaultHandle<T> createNewHandle(Stack dstStack) {
        DefaultHandle<T> newHandle = new DefaultHandle<>(dstStack);
        newHandle.object = newInstance(newHandle);
        return newHandle;
    }

    @SuppressWarnings("unchecked")
    public T take() {
        return take(null);
    }

    @SuppressWarnings("unchecked")
    public T take(Initializer<T> initializer, Object... parameters) {
        Stack stack = internalStack.get();
        if (stack != null) {
            DefaultHandle<?> handle = stack.pop();
            if (handle == null) {
                handle = createNewHandle(stack);
            }

            if (initializer != null) {
                initializer.initialize((T) handle.object, parameters);
            }
            return (T) handle.object;
        }
        return null;
    }

    public boolean release(T item, Handle handle) {
        if (item == null)
            throw new NullPointerException("item");

        if (handle == null)
            throw new NullPointerException("handle");

        Stack stack = internalStack.get();
        if (stack == null) {
            return false;
        }

        if (stack.getParent() != this) {
            return false;
        }

        handle.release();
        return true;
    }

    boolean scavengeAll() {
        Stack stack = internalStack.get();
        if (stack == null) {
            return false;
        }
        return stack.scavengeAll();
    }

    int getStackCount() {
        Stack stack = internalStack.get();
        return stack.count();
    }

    int getStackCapacity() {
        Stack stack = internalStack.get();
        return stack.currentCapacity;
    }

    protected abstract T newInstance(Handle handle);

    @Override
    public String toString() {
        Stack stack = internalStack.get();
        return stack.toString();
    }
}