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

public abstract class AbstractTransport implements Transport {
    protected final Application application;
    protected final int LISTEN_PORT;
    protected final int SEND_BUF_SIZE;
    protected final int RECV_BUF_SIZE;
    protected final int MAX_CONNECTION;
    protected final int THREAD_COUNT;

    protected MetricListener metricListener;

    protected AbstractTransport(final Builder<?, ?> builder) {
        this.application = builder.application;
        this.LISTEN_PORT = builder.listenPort;
        this.SEND_BUF_SIZE = builder.sendBufSize;
        this.RECV_BUF_SIZE = builder.recvBufSize;
        this.MAX_CONNECTION = builder.maxConnection;
        this.THREAD_COUNT = builder.threadCount;

        this.metricListener = builder.metricListener;
    }

    public int threadCount() {
        return THREAD_COUNT;
    }

    public static abstract class Builder<B extends Builder<B, C>, C> {
        private Application application;

        private int listenPort;
        private int sendBufSize;
        private int recvBufSize;
        private int maxConnection;
        private int threadCount;
        private MetricListener metricListener;

        @SuppressWarnings("unchecked")
        public B application(Application application) {
            this.application = application;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B listenPort(int listenPort) {
            this.listenPort = listenPort;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B sendBufSize(int sendBufSize) {
            this.sendBufSize = sendBufSize;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B recvBufSize(int recvBufSize) {
            this.recvBufSize = recvBufSize;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B maxConnnection(int maxConnection) {
            this.maxConnection = maxConnection;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B threadCount(int threadCount) {
            this.threadCount = threadCount;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B metricListener(MetricListener metricListener) {
            this.metricListener = metricListener;
            return (B) this;
        }

        public abstract C build();
    }
}
