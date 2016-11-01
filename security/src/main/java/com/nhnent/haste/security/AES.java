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

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;

public class AES {
    /*
        Java supported follow AES.
            AES/CBC/NoPadding (128)
            AES/CBC/PKCS5Padding (128)
            AES/ECB/NoPadding (128)
            AES/ECB/PKCS5Padding (128)
    */
    public enum BlockCipherMode {
        ECB("ECB"),
        CBC("CBC");

        private String cipherMode;

        BlockCipherMode(String cipherMode) {
            this.cipherMode = cipherMode;
        }

        @Override
        public String toString() {
            return this.cipherMode;
        }
    }

    public enum Padding {
        NO("NoPadding"),
        PKCS5("PKCS5Padding");

        private String padding;

        Padding(String padding) {
            this.padding = padding;
        }

        @Override
        public String toString() {
            return this.padding;
        }
    }

    private BlockCipherMode cipherMode = DEFAULT_CIPHER_MODE;
    private Padding padding = DEFAULT_PADDING;
    private byte[] key;

    private String cipherString = null;

    private Cipher encryptCipher;
    private Cipher decryptCipher;

    private static final String CIPHER_SIG = "AES";
    public static final BlockCipherMode DEFAULT_CIPHER_MODE = BlockCipherMode.CBC;
    public static final Padding DEFAULT_PADDING = Padding.PKCS5;
    public static final byte[] DEFAULT_IV = new byte[16]; // IV is 16 bit

    private AES() {
    }

    public static class Builder {
        private BlockCipherMode cipherMode = DEFAULT_CIPHER_MODE;
        private Padding padding = DEFAULT_PADDING;
        private byte[] key;

        public Builder cipherMode(BlockCipherMode cipherMode) {
            this.cipherMode = cipherMode;
            return this;
        }

        public Builder padding(Padding padding) {
            this.padding = padding;
            return this;
        }

        public Builder key(byte[] key) {
            this.key = key;
            return this;
        }

        public AES build() throws GeneralSecurityException {
            AES aes = new AES();
            aes.cipherMode = this.cipherMode;
            aes.padding = this.padding;
            aes.key = this.key;

            aes.cipherString = MessageFormat.format("{0}/{1}/{2}", CIPHER_SIG, cipherMode.toString(), padding.toString());

            aes.init();

            return aes;
        }
    }

    private void init() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        SecretKey secureKey = new SecretKeySpec(key, CIPHER_SIG);

        encryptCipher = Cipher.getInstance(this.cipherString);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(DEFAULT_IV));

        decryptCipher = Cipher.getInstance(this.cipherString);
        decryptCipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(DEFAULT_IV));
    }

    public byte[] encrypt(byte[] value) throws Exception {
        return aes(Cipher.ENCRYPT_MODE, key, value, 0, value.length);
    }

    public byte[] decrypt(byte[] value) throws Exception {
        return aes(Cipher.DECRYPT_MODE, key, value, 0, value.length);
    }

    public byte[] encrypt(byte[] value, int start, int length) throws Exception {
        return aes(Cipher.ENCRYPT_MODE, key, value, start, length);
    }

    public byte[] decrypt(byte[] value, int start, int length) throws Exception {
        return aes(Cipher.DECRYPT_MODE, key, value, start, length);
    }

    private byte[] aes(int encryptionMode, byte[] key, byte[] value, int valueOffset, int valueLength) throws BadPaddingException, IllegalBlockSizeException {
        if (this.key == null) {
            throw new NullPointerException("key is null!");
        }

        Cipher c = encryptionMode == Cipher.ENCRYPT_MODE ? encryptCipher : decryptCipher;
        return c.doFinal(value, valueOffset, valueLength);
    }

    @Override
    public String toString() {
        return "AES{" +
                "cipherMode=" + cipherMode +
                ", padding=" + padding +
                ", key=" + Arrays.toString(key) +
                '}';
    }
}
