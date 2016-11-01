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

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Arrays;

class Stack {
    private ObjectPool parent;
    DefaultHandle<?>[] elements;
    private WeakReference<Thread> ownerThread;

    int head;
    int currentCapacity;
    int maxCapacity;

    private final int INITIAL_MIN_CAPACITY = 256;

    private volatile DelayedPool headDelayedPool;
    private DelayedPool current, previous;

    Stack(ObjectPool parent, Thread ownerThread, int maxCapacity, boolean isPreCreate, int preCreateCount) {
        this.parent = parent;
        this.ownerThread = new WeakReference<>(ownerThread);
        this.maxCapacity = maxCapacity;
        this.currentCapacity = Math.min(INITIAL_MIN_CAPACITY, maxCapacity);
        this.elements = new DefaultHandle[currentCapacity];
        this.head = -1;

        if (isPreCreate) {
            for (int i = 0; i < preCreateCount; i++) {
                this.tryPush(parent.createNewHandle(this));
            }
        }
    }

    int increaseCapacity() {
        int newCapacity = this.currentCapacity << 1;
        if (newCapacity > maxCapacity) {
            return this.currentCapacity;
        }

        this.currentCapacity = newCapacity;
        elements = Arrays.copyOf(elements, newCapacity);

        return newCapacity;
    }

    boolean tryPush(DefaultHandle<?> itemHandle) {
        if (itemHandle == null)
            throw new NullPointerException("itemHandle");

        if (head >= elements.length - 1) {
            int prevCapacity = this.currentCapacity;
            int newSize = increaseCapacity();
            if (prevCapacity == newSize) {
                return false;
            }
        }
        head++;
        this.elements[head] = itemHandle;
        return true;
    }

    DefaultHandle<?> pop() {
        if (this.head < 0) {
            if (!scavengeAll()) {
                this.current = headDelayedPool;
                this.previous = null;
                return null;
            }
        }

        DefaultHandle popHandle = this.elements[head];
        this.elements[head] = null;
        head--;
        return popHandle;
    }

    boolean scavengeAll() {
        DelayedPool current = this.current;
        DelayedPool previous = this.previous;

        if (current == null) {
            if (this.headDelayedPool == null) {
                return false;
            }
            current = this.headDelayedPool;
            previous = null;
        }

        boolean success = false;
        int loopCount = 1;
        do {
            if (current.transfer(this)) {
                success = true;
                break;
            }

            // If the thread of current delayed pool were deallocated,
            // fetch all elements in current delayed pool.
            if (current.ownerThread.get() == null) {
                while (!current.isEmpty()) {
                    if (current.transfer()) {
                        success = true;
                    } else {
                        break;
                    }
                }

                if (previous != null) {
                    previous.next = current.next;
                } else {
                    synchronized (this) {
                        this.headDelayedPool = this.current;
                    }
                }
            } else {
                previous = current;
            }
            current = current.next;

            if (current == null && !success && loopCount > 0) {
                current = this.headDelayedPool;
                loopCount--;
            }
        } while (current != null && !success);

        this.previous = previous;
        this.current = current;

        return success;
    }

    void setHeadDelayedPool(DelayedPool pool) {
        if (pool == null)
            throw new NullPointerException("pool");

        synchronized (this) {
            pool.next = this.headDelayedPool;
            this.headDelayedPool = pool;
        }
    }

    int count() {
        return head + 1;
    }

    Thread getOwnerThread() {
        return this.ownerThread.get();
    }

    ObjectPool<?> getParent() {
        return this.parent;
    }

    @Override
    public String toString() {
        DelayedPool pool = this.headDelayedPool;
        StringBuilder builder = new StringBuilder();
        Thread owner = ownerThread.get();
        long ownerId = -1;
        if (owner != null) {
            ownerId = owner.getId();
        }
        while (pool != null) {
            builder.append(pool.toString() + ",");
            pool = pool.next;
        }

        return MessageFormat.format("[tid={0,number,00},stackCnt={1,number,0000},link='{'{2}'}']", ownerId, head + 1, builder.toString());
    }
}