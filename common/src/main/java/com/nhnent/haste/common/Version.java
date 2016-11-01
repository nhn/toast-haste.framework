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

package com.nhnent.haste.common;

public class Version {
    private final int build;
    private final int major;
    private final int minor;

    private Version(int major, int minor, int build) {
        if (major < 0)
            throw new IllegalArgumentException("major is out of range");

        if (minor < 0)
            throw new IllegalArgumentException("minor is out of range");

        if (build < 0)
            throw new IllegalArgumentException("build is out of range");

        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public int getBuild() {
        return build;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public static class Builder {
        private int major;
        private int minor;

        public Builder major(int major) {
            this.major = major;
            return this;
        }

        public Builder minor(int minor) {
            this.minor = minor;
            return this;
        }

        public Version build(int build) {
            return new Version(major, minor, build);
        }
    }

    @Override
    public boolean equals(Object obj) {
        Version v = null;

        try {
            v = (Version) obj;
        } catch (ClassCastException e) {
            return false;
        }

        if (this == obj) {
            return false;
        }

        return !((major != v.major) ||
                (minor != v.minor) ||
                (build != v.build));
    }
}
