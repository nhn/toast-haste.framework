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

package com.nhnent.haste.protocol.data;

import org.junit.Assert;
import org.junit.Test;

public class DataSchemaTest {
    private final int ID = 255;
    private final String NAME = "Paul";

    private final int ID2 = 1659592;
    private final String NAME2 = "Johns";

    static class TestMessage extends DataSchema {
        TestMessage() {
        }

        TestMessage(DataObject object) {
            super(object);
        }

        @FieldParameter(Code = 0)
        public int id;

        @FieldParameter(Code = 1)
        public String name;

        @FieldParameter(Code = 2)
        public DataObject packet;
    }

    @Test
    public void testGetAnnotation() {
        DataObject dataObject = new DataObject();
        dataObject.set((byte) 0, ID);
        dataObject.set((byte) 1, NAME);

        TestMessage testMessage = new TestMessage(dataObject);
        Assert.assertEquals(ID, testMessage.id);
        Assert.assertEquals(NAME, testMessage.name);
    }

    @Test
    public void testGetAnnotation2() {
        testGetAnnotation();

        DataObject dataObject2 = new DataObject();
        dataObject2.set((byte) 0, ID2);
        dataObject2.set((byte) 1, NAME2);

        TestMessage testMessage2 = new TestMessage(dataObject2);
        Assert.assertEquals(ID2, testMessage2.id);
        Assert.assertEquals(NAME2, testMessage2.name);
    }

    @Test
    public void testToDataObject() {
        TestMessage testMessage = new TestMessage();
        testMessage.id = ID;
        testMessage.name = NAME;

        DataObject dataObject = testMessage.toDataObject();
        Assert.assertEquals(ID, dataObject.get((byte)0).value);
        Assert.assertEquals(NAME, dataObject.get((byte)1).value);

        TestMessage testMessage2 = new TestMessage();
        testMessage2.id = ID2;
        testMessage2.name = NAME2;

        DataObject dataObject2 = testMessage2.toDataObject();
        Assert.assertEquals(ID2, dataObject2.get((byte)0).value);
        Assert.assertEquals(NAME2, dataObject2.get((byte)1).value);
    }

    @Test
    public void testNestedDataObject() {
        DataObject dataObject = new DataObject();
        dataObject.set((byte) 0, ID);
        dataObject.set((byte) 1, NAME);
        DataObject inData = new DataObject();
        inData.set((byte) 0, ID);
        inData.set((byte) 1, NAME);
        dataObject.set((byte) 2, inData);

        TestMessage testMessage = new TestMessage(dataObject);
        Assert.assertEquals(ID, testMessage.id);
        Assert.assertEquals(NAME, testMessage.name);

        DataObject packet = testMessage.packet;
        Assert.assertEquals(ID, packet.get((byte) 0).value);
        Assert.assertEquals(NAME, packet.get((byte) 1).value);
    }
}
