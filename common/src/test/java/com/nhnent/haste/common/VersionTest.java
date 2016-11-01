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

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {
    private static final int MAJOR = 1;
    private static final int MINOR = 2;
    private static final int BUILD = 3;

    @Test
    public void testBuildVersion() {
        Version builtVersion = new Version.Builder().major(MAJOR).minor(MINOR).build(BUILD);

        Assert.assertEquals(MAJOR, builtVersion.getMajor());
        Assert.assertEquals(MINOR, builtVersion.getMinor());
        Assert.assertEquals(BUILD, builtVersion.getBuild());
    }

    @Test
    public void testFailedBuildVersion() {
        Version buildingVersion;

        try{
            buildingVersion = new Version.Builder().major(-1).build(BUILD);
            Assert.fail("major version is negative");
        }catch (IllegalArgumentException iae){
            Assert.assertTrue(true);
        }

        try{
            buildingVersion = new Version.Builder().major(MAJOR).minor(-1).build(BUILD);
            Assert.fail("minor version is negative");
        }catch (IllegalArgumentException iae){
            Assert.assertTrue(true);
        }

        try{
            buildingVersion = new Version.Builder().major(MAJOR).minor(MINOR).build(-1);
            Assert.fail("build version is negative");
        }catch (IllegalArgumentException iae){
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testEquals() {
        Version builtVersion1 = new Version.Builder().major(MAJOR).minor(MINOR).build(BUILD);
        Version builtVersion2 = new Version.Builder().major(MAJOR).minor(MINOR).build(BUILD);

        Assert.assertTrue(builtVersion1.equals(builtVersion2));
    }

    @Test
    public void testNotEquals() {
        Version builtVersion1 = new Version.Builder().major(MAJOR).minor(MINOR).build(BUILD);
        Version builtVersion2 = new Version.Builder().major(BUILD).minor(MAJOR).build(MINOR);

        Assert.assertFalse(builtVersion1.equals(builtVersion2));
    }

    @Test
    public void testEqualsWhenDiffrentClass() {
        Version builtVersion = new Version.Builder().major(MAJOR).minor(MINOR).build(BUILD);
        Assert.assertFalse(builtVersion.equals("test"));
    }

    @Test
    public void testEqualsWhenSameObjects() {
        Version builtVersion = new Version.Builder().major(MAJOR).minor(MINOR).build(BUILD);
        Assert.assertFalse(builtVersion.equals(builtVersion));
    }
}
