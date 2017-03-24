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

package com.nhnent.haste.framework.security;

import com.nhnent.haste.security.AES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;

public class AesCryptoProvider implements CryptoProvider {
    private static final Logger logger = LoggerFactory.getLogger(AesCryptoProvider.class);

    private AES aes;

    public AesCryptoProvider(byte[] secretKey) {
        byte[] copiedSecretKey = new byte[secretKey.length];
        System.arraycopy(secretKey, 0, copiedSecretKey, 0, secretKey.length);

        try {
            aes = new AES.Builder().cipherMode(AES.BlockCipherMode.CBC).key(copiedSecretKey).padding(AES.Padding.PKCS5).build();
        } catch (GeneralSecurityException e) {
            logger.error("Failed to load AES instance : exception[{}], cause[{}]", e.getClass().getSimpleName(), e.getCause());
        }
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return this.encrypt(data, 0, data.length);
    }

    @Override
    public byte[] encrypt(byte[] data, int dataOffset, int dataLength) {
        try {
            return aes.encrypt(data, dataOffset, dataLength);
        } catch (Exception ex) {
            logger.error("Invalid encrypt operation", ex);
            return null;
        }
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return decrypt(data, 0, data.length);
    }

    @Override
    public byte[] decrypt(byte[] data, int dataOffset, int dataLength) {
        try {
            return aes.decrypt(data, dataOffset, dataLength);
        } catch (Exception ex) {
            logger.error("Invalid decrypt operation", ex);
            return null;
        }
    }
}
