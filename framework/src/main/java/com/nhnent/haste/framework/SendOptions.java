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

package com.nhnent.haste.framework;

import com.nhnent.haste.objectpool.AbstractPoolable;
import com.nhnent.haste.objectpool.Handle;
import com.nhnent.haste.objectpool.ObjectPool;
import com.nhnent.haste.transport.QoS;

public class SendOptions extends AbstractPoolable {
    private final static int PRE_CREATE_OBJECT = 128;
    private final static ObjectPool<SendOptions> pool = new ObjectPool<SendOptions>(true, PRE_CREATE_OBJECT) {
        @Override
        protected SendOptions newInstance(Handle handle) {
            return new SendOptions(handle);
        }
    };

    private static ObjectPool.Initializer<SendOptions> initializer = new ObjectPool.Initializer<SendOptions>() {
        @Override
        public void initialize(SendOptions target, Object... parameters) {
            target.channel = (byte) parameters[0];
            target.encrypt = (boolean) parameters[1];
            target.qos = (QoS) parameters[2];
        }
    };

    public static SendOptions take(byte channel, boolean encrypt, QoS qos) {
        return pool.take(initializer, channel, encrypt, qos);
    }

    private Handle handle;

    private byte channel = 0;
    private boolean encrypt = false;
    private QoS qos = QoS.UNRELIABLE_SEQUENCED;

    public final static SendOptions ReliableSend = new SendOptions((byte) 0, false, QoS.RELIABLE_SEQUENCED);
    public final static SendOptions ReliableSecureSend = new SendOptions((byte) 0, true, QoS.RELIABLE_SEQUENCED);

    private SendOptions(Handle handle) {
        this.handle = handle;
    }

    private SendOptions(byte channel, boolean encrypt, QoS qos) {
        this.channel = channel;
        this.encrypt = encrypt;
        this.qos = qos;
    }

    public byte getChannel() {
        return channel;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public QoS getQos() {
        return qos;
    }

    @Override
    public String toString() {
        return "SendOptions{" +
                "channel=" + channel +
                ", encrypt=" + encrypt +
                ", qos=" + qos +
                '}';
    }

    @Override
    protected Handle getHandle() {
        return this.handle;
    }
}
