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

class DelayedPool {
    static final int LINK_CAPACITY = 16;

    static class Link {
        DefaultHandle[] elements;
        volatile int writeIndex;
        int readIndex;
        Link next;

        Link() {
            elements = new DefaultHandle[LINK_CAPACITY];
            writeIndex = 0;
            readIndex = 0;
            next = null;
        }

        void addLast(DefaultHandle item) {
            elements[writeIndex] = item;
            writeIndex++;
        }
    }

    WeakReference<Thread> ownerThread;
    Stack ownerStack;
    volatile Link head;
    Link tail;

    DelayedPool next;

    DelayedPool(Thread ownerThread, Stack ownerStack) {
        if (ownerThread == null)
            throw new NullPointerException("ownerThread");
        if (ownerStack == null)
            throw new NullPointerException("ownerStack");

        this.ownerThread = new WeakReference<>(ownerThread);
        this.ownerStack = ownerStack;
        this.head = this.tail = new Link();
        ownerStack.setHeadDelayedPool(this);
    }

    void add(DefaultHandle item) {
        Link tail = this.tail;
        if (this.tail.writeIndex >= LINK_CAPACITY) {
            Link newLink = new Link();
            tail = this.tail = this.tail.next = newLink;
        }

        tail.addLast(item);
    }

    boolean isEmpty() {
        // tail is always not null.
        return tail.readIndex == tail.writeIndex;
    }

    boolean transfer() {
        return transfer(this.ownerStack);
    }

    boolean transfer(Stack dstStack) {
        // This method is certainly called by owner thread.
        if (dstStack == null)
            throw new NullPointerException("dstStack");

        if (this.head == null) {
            return false;
        }

        Link head = this.head;

        if (head.readIndex == LINK_CAPACITY) {
            if (head.next == null) {
                return false;
            }
            this.head = head = head.next;
        }

        if (head.readIndex >= head.writeIndex) {
            return false;
        }

        int srcStart = head.readIndex;
        int srcEnd = head.writeIndex;
        int srcMovableSize = srcEnd - srcStart;

        int dstStart = dstStack.head;
        int dstCapacity = dstStack.currentCapacity;
        int dstAvailableSize = dstCapacity - dstStart;

        if (dstAvailableSize < srcMovableSize) {
            int newDstCapacity = dstStack.increaseCapacity();
            if (newDstCapacity == dstCapacity) {
                srcEnd = srcStart + dstAvailableSize;
            }
        }

        if (srcStart != srcEnd) {
            for (int i = srcStart; i < srcEnd; i++) {
                DefaultHandle<?> element = head.elements[i];
                if (!dstStack.tryPush(element)) {
                    srcEnd = i;
                    break;
                }
                head.elements[i] = null;
            }

            if (srcEnd == LINK_CAPACITY && head.next != null) {
                this.head = head.next;
            }

            head.readIndex = srcEnd;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        Link link = this.head;
        int count = 0;
        Thread owner = ownerThread.get();
        long ownerId = -1;
        if (owner != null) {
            ownerId = owner.getId();
        }
        while (link != null) {
            count += (link.writeIndex - link.readIndex);
            link = link.next;
        }
        return MessageFormat.format("[tid={0,number,00},linkCnt={1,number,0000}]", ownerId, count);
    }
}
