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

package com.nhnent.haste.security;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

public class AesTest {
    private static final String failMessage = "If you want to use AES256, you must download JCE!";

    @Test
    public void testAvailableAes256() throws NoSuchAlgorithmException {
        // If this test is passed, this means that you could use AES256.
        Assert.assertEquals(failMessage, Integer.MAX_VALUE, Cipher.getMaxAllowedKeyLength("AES"));
    }

    @Test
    public void testSimpleEncrypt() {
        try {
            AES aes = new AES.Builder().cipherMode(AES.BlockCipherMode.CBC)
                    .padding(AES.Padding.PKCS5)
                    .key(SHA256.hash(TestConstants.key.getBytes())).build();
            byte[] result = aes.encrypt(TestConstants.msg.getBytes());
            String base64Result = Base64.encodeToString(result);
            Assert.assertEquals(base64Result, TestConstants.encryptedBase64Msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
