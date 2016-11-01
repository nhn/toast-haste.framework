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

package com.nhnent.haste.transport;

import com.nhnent.haste.objectpool.Handle;
import com.nhnent.haste.objectpool.ObjectPool;
import com.nhnent.haste.objectpool.Poolable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Wrapped a byte array which received through network in order to recycle object.
 */
public class Payload implements Poolable {
    private static final Logger logger = LoggerFactory.getLogger(Payload.class);

    private static final int PAYLOAD_CAPACITY = 2048;
    private static final int INITIAL_CREATE_COUNT = 16;
    private static final ObjectPool<Payload> pool = new ObjectPool<Payload>(true, INITIAL_CREATE_COUNT) {
        @Override
        protected Payload newInstance(Handle handle) {
            return new Payload(handle, PAYLOAD_CAPACITY);
        }
    };

    private Handle handle;
    private byte[] bytes;
    private int length;

    private Payload(Handle handle, int capacity) {
        this.handle = handle;
        this.bytes = new byte[capacity];
    }

    /**
     * Take {@link Payload} object in pool.
     *
     * @return A payload object
     */
    public static Payload take() {
        return pool.take();
    }

    private void clear() {
        Arrays.fill(this.bytes, (byte) 0);
    }

    /**
     * Return a reference of a byte array in payload.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Set a reference of a byte array in payload.
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Copy the byte array from {@code src} to this payload.
     *
     * @param src    A target byte array.
     * @param srcPos A position of src array.
     * @param srcLen A length of src array.
     */
    public void copyBytes(byte[] src, int srcPos, int srcLen) {
        copyBytes(src, srcPos, srcLen, 0);
    }

    /**
     * Copy the byte array from {@code src} to this payload.
     *
     * @param src    A target byte array.
     * @param srcPos A position of src array.
     * @param srcLen A length of src array.
     * @param dstPos A position of this payload array.
     */
    public void copyBytes(byte[] src, int srcPos, int srcLen, int dstPos) {
        if (this.bytes.length < dstPos + srcLen) {
            this.bytes = Arrays.copyOf(this.bytes, dstPos + srcLen);
        }
        System.arraycopy(src, srcPos, this.bytes, dstPos, srcLen);
    }

    /**
     * Return a copy of the data in payload.
     *
     * @return a copy of the data in payload.
     */
    public byte[] copyOf() {
        byte[] copy = new byte[this.length];
        System.arraycopy(this.bytes, 0, copy, 0, this.length);
        return copy;
    }

    /**
     * Return a length of byte array in this payload.
     */
    public int getLength() {
        return length;
    }

    /**
     * Set a length of a read byte array.
     *
     * @param length A length of a read byte array.
     */
    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public void release() {
        if (this.handle != null) {
            this.handle.release();
            clear();
        }
    }
}
