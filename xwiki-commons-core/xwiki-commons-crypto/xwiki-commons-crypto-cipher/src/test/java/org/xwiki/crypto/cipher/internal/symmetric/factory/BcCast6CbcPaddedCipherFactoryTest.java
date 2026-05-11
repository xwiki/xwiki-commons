/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.crypto.cipher.internal.symmetric.factory;

import org.xwiki.crypto.cipher.Cipher;
import org.xwiki.crypto.cipher.CipherFactory;
import org.xwiki.crypto.params.cipher.symmetric.KeyWithIVParameters;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

@ComponentTest
class BcCast6CbcPaddedCipherFactoryTest extends AbstractSymmetricCipherFactoryTest
{
    @InjectMockComponents
    private BcCast6CbcPaddedCipherFactory factory;

    {
        this.cipherAlgo = "CAST6/CBC/PKCS7Padding";
        this.blockSize = 16;
        this.keySize = 32;
        this.supportedKeySize = new int[] { 16, 20, 24, 28, 32 };

        this.bytesEncryptedSize = ((BYTES.length / this.blockSize) * this.blockSize) + this.blockSize;
        this.anotherBytesEncryptedSize = ((ANOTHER_BYTES.length / this.blockSize) * this.blockSize) + this.blockSize;
    }

    @Override
    protected CipherFactory getFactory()
    {
        return this.factory;
    }

    @Override
    Cipher getCipherInstance(boolean forEncryption)
    {
        return this.factory.getInstance(forEncryption, new KeyWithIVParameters(KEY32, IV16));
    }
}
