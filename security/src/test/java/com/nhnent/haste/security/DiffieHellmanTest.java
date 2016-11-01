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

import java.text.MessageFormat;

public class DiffieHellmanTest {
    private void testDH(BigInteger clientPrivateKey, BigInteger clientPublicKey, BigInteger serverPrivateKey, BigInteger serverPublicKey) {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();

        BigInteger serverSecretKey = DiffieHellman.generateSecretKey(clientPublicKey, serverPrivateKey);
        BigInteger clientSecretKey = serverPublicKey.modPow(clientPrivateKey, OakleyGroup1.primeNumber);

        byte[] serverSecretkeyMD5 = MD5.hash(serverSecretKey);
        byte[] clientSecretkeyMD5 = MD5.hash(clientSecretKey);

        Assert.assertEquals(clientSecretKey, serverSecretKey);
        Assert.assertArrayEquals(SHA256.hash(clientSecretKey), SHA256.hash(serverSecretKey));
        Assert.assertArrayEquals(MD5.hash(clientSecretKey), MD5.hash(serverSecretKey));

        System.out.println("============================================");
        System.out.println(MessageFormat.format("[{0}]", stacks[2].getMethodName()));
        System.out.println("ClientPrivateKey : " + TestUtil.toString(clientPrivateKey));
        System.out.println("ClientPublicKey : " + TestUtil.toString(clientPublicKey));
        System.out.println("ServerPrivateKey : " + TestUtil.toString(serverPrivateKey));
        System.out.println("ServerPublicKey : " + TestUtil.toString(serverPublicKey));
        System.out.println("ServerSecretKey : " + TestUtil.toString(serverSecretKey));
        System.out.println("ClientSecretKey : " + TestUtil.toString(clientSecretKey));
        System.out.println("============================================");
        System.out.println("ServerSecretKey(MD5) : " + TestUtil.toString(serverSecretkeyMD5));
        System.out.println("ClientSecretKey(MD5) : " + TestUtil.toString(clientSecretkeyMD5));
        System.out.println("============================================");
    }

    @Test
    public void testStaticKey() {
        BigInteger clientPrivateKey = new BigInteger("1234567891433", 10);
        BigInteger clientPublicKey = OakleyGroup1.generator.modPow(clientPrivateKey, OakleyGroup1.primeNumber);

        BigInteger serverPrivateKey = new BigInteger("3234567891283", 10);
        BigInteger serverPublicKey = DiffieHellman.generatePublicKey(serverPrivateKey);

        testDH(clientPrivateKey, clientPublicKey, serverPrivateKey, serverPublicKey);
    }

    @Test
    public void testGeneratedClientPublicKey() {
        BigInteger clientPrivateKey = new BigInteger("1234567891433", 10);
        BigInteger clientPublicKey = new BigInteger(new byte[]{
                (byte) 0x0, (byte) 0xA5, (byte) 0x36, (byte) 0x5C, (byte) 0xAB, (byte) 0x4B, (byte) 0x89, (byte) 0x4F, (byte) 0xF5, (byte) 0x9C, (byte) 0xA3, (byte) 0x98, (byte) 0xA0, (byte) 0x45, (byte) 0xF5, (byte) 0xF8, (byte) 0xFD, (byte) 0x5, (byte) 0x30, (byte) 0xA4, (byte) 0xDE, (byte) 0xA7, (byte) 0xC3, (byte) 0x22, (byte) 0xCE, (byte) 0x96, (byte) 0x4D, (byte) 0x16, (byte) 0xFF, (byte) 0xAA, (byte) 0x9, (byte) 0x64, (byte) 0xDA, (byte) 0xCE, (byte) 0xCE, (byte) 0xAC, (byte) 0x60, (byte) 0x36, (byte) 0x2F, (byte) 0xF6, (byte) 0xEA, (byte) 0x6A, (byte) 0xF9, (byte) 0xD1, (byte) 0x22, (byte) 0x94, (byte) 0x56, (byte) 0x1A, (byte) 0xEB, (byte) 0xA7, (byte) 0xC9, (byte) 0x7E, (byte) 0x27, (byte) 0xE2, (byte) 0x67, (byte) 0x85, (byte) 0xE7, (byte) 0xB1, (byte) 0xAB, (byte) 0x71, (byte) 0x5A, (byte) 0x56, (byte) 0x46, (byte) 0xBD, (byte) 0x87, (byte) 0x98, (byte) 0x6A, (byte) 0x1, (byte) 0xD3, (byte) 0x1A, (byte) 0xA5, (byte) 0x8C, (byte) 0x10, (byte) 0x39, (byte) 0x8A, (byte) 0x83, (byte) 0x68, (byte) 0x6B, (byte) 0x72, (byte) 0xEC, (byte) 0xCB, (byte) 0xF7, (byte) 0x74, (byte) 0x6E, (byte) 0x1A, (byte) 0x29, (byte) 0x6C, (byte) 0xC6, (byte) 0xDA, (byte) 0x96, (byte) 0xE7, (byte) 0x7E, (byte) 0x47, (byte) 0xBF, (byte) 0x9A, (byte) 0xF3, (byte) 0xA
        });
        BigInteger serverPrivateKey = new BigInteger("3234567891283", 10);
        BigInteger serverPublicKey = DiffieHellman.generatePublicKey(serverPrivateKey);

        testDH(clientPrivateKey, clientPublicKey, serverPrivateKey, serverPublicKey);
    }

    @Test
    public void testGenerateSecretKey() {
        BigInteger clientPrivateKey = DiffieHellman.generatePrivateKey(160);
        BigInteger clientPublicKey = DiffieHellman.generatePublicKey(clientPrivateKey);

        BigInteger serverPrivateKey = DiffieHellman.generatePrivateKey(160);
        BigInteger serverPublicKey = DiffieHellman.generatePublicKey(serverPrivateKey);

        testDH(clientPrivateKey, clientPublicKey, serverPrivateKey, serverPublicKey);
    }

    @Test
    public void testGenerateSecretKeyFromClientData() {
        BigInteger clientPrivateKey = new BigInteger(TestUtil.toByteArray(new int[]{
                0, 255, 255, 255, 255, 11, 89, 11, 124, 248, 75, 231, 221, 125, 34, 76, 198, 27, 49, 255, 146, 218, 223, 125, 111, 196, 155, 49, 217, 203, 5, 212, 205
        }));
        BigInteger clientPublicKey = new BigInteger(TestUtil.toByteArray(new int[]{
                0, 213, 99, 198, 57, 153, 233, 59, 184, 161, 120, 118, 171, 173, 189, 14, 42, 173, 193, 161, 134, 97, 8, 64, 32, 104, 88, 27, 87, 190, 38, 18, 64, 90, 61, 157, 219, 122, 255, 36, 214, 251, 151, 136, 184, 192, 110, 216, 137, 229, 133, 178, 200, 97, 207, 139, 41, 200, 244, 243, 32, 178, 212, 226, 196, 215, 157, 9, 165, 169, 210, 255, 137, 63, 3, 68, 78, 133, 216, 16, 83, 187, 247, 195, 125, 35, 202, 233, 222, 29, 150, 196, 67, 34, 217, 161, 174
        }));

        BigInteger serverPrivateKey = DiffieHellman.generatePrivateKey(160);
        BigInteger serverPublicKey = DiffieHellman.generatePublicKey(serverPrivateKey);

        testDH(clientPrivateKey, clientPublicKey, serverPrivateKey, serverPublicKey);
    }
}