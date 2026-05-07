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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.cipher.Cipher;
import org.xwiki.crypto.cipher.CipherFactory;
import org.xwiki.crypto.params.cipher.asymmetric.AsymmetricCipherParameters;
import org.xwiki.crypto.params.cipher.symmetric.SymmetricCipherParameters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base class for cipher tests.
 *
 * @version $Id$
 */
public abstract class AbstractSymmetricCipherFactoryTest
{
    /** Length = 272 byte = 17 * 16 */
    private static final String TEXT = "Congress shall make no law respecting an establishment of religion, or "
        + "prohibiting the free exercise thereof; or abridging the freedom of speech, "
        + "or of the press; or the right of the people peaceably to assemble, and to "
        + "petition the Government for a redress of grievances.";

    protected static final byte[] BYTES = TEXT.getBytes();

    /** Length = 113 byte = 7 * 16 + 1 */
    private static final String ANOTHER_TEXT = "The length of this text is 113 byte. This is 1 byte more "
        + "than a multiple of block size for 128 bit block ciphers.";

    protected static final byte[] ANOTHER_BYTES = ANOTHER_TEXT.getBytes();

    /** A poor 8bits key. */
    protected static final byte[] KEY8 = {
        0x58, 0x57, 0x69, 0x6b, 0x69, 0x20, 0x69, 0x73 };

    /** A poor 16bits key. */
    protected static final byte[] KEY16 = {
        0x58, 0x57, 0x69, 0x6b, 0x69, 0x20, 0x69, 0x73,
        0x20, 0x74, 0x68, 0x65, 0x20, 0x6b, 0x65, 0x79 };

    /** A poor 32bits key. */
    protected static final byte[] KEY32 = {
        0x58, 0x57, 0x69, 0x6b, 0x69, 0x20, 0x69, 0x73,
        0x20, 0x74, 0x68, 0x65, 0x20, 0x6b, 0x65, 0x79,
        0x79, 0x65, 0x6b, 0x20, 0x65, 0x68, 0x74, 0x20,
        0x73, 0x69, 0x20, 0x69, 0x6b, 0x69, 0x57, 0x58 };

    /** A poor 8bits IV. */
    protected static final byte[] IV8 = { 0x12, 0x34, 0x56, 0x78, 0x78, 0x56, 0x34, 0x12 };

    /** A poor 16bits IV. */
    protected static final byte[] IV16 = {
        0x12, 0x34, 0x56, 0x78, 0x78, 0x56, 0x34, 0x12,
        0x12, 0x34, 0x56, 0x78, 0x78, 0x56, 0x34, 0x12 };

    abstract protected CipherFactory getFactory();

    protected String cipherAlgo;
    protected int blockSize;
    protected int keySize;
    protected int[] supportedKeySize;

    protected int bytesEncryptedSize;
    protected int anotherBytesEncryptedSize;

    @Test
    public void fetCipherFactoryProperties()
    {
        assertEquals(this.cipherAlgo, getFactory().getCipherAlgorithmName());
        assertEquals(this.blockSize, getFactory().getIVSize());
        assertEquals(this.keySize, getFactory().getKeySize());
        assertArrayEquals(this.supportedKeySize, getFactory().getSupportedKeySizes());
    }

    @Test
    public void fetCipherProperties()
    {
        Cipher cipher = getCipher(true);

        assertEquals(this.cipherAlgo, cipher.getAlgorithmName());
        assertEquals(this.blockSize, cipher.getOutputBlockSize());
        assertTrue(cipher.isForEncryption());

        cipher = getCipher(false);
        assertEquals(this.cipherAlgo, cipher.getAlgorithmName());
        assertEquals(this.blockSize, cipher.getOutputBlockSize());
        assertFalse(cipher.isForEncryption());
    }

    private static Cipher encryptCipher;
    private static Cipher decryptCipher;

    protected static byte[] encrypted;
    protected static byte[] anotherEncrypted;

    @BeforeAll
    public static void cleanUpCaches() {
        encryptCipher = null;
        decryptCipher = null;
        encrypted = null;
        anotherEncrypted = null;
    }

    /**
     * Cache the cipher to avoid recreating it between test uselessly since cypher setup may be long.
     * @return a cipher for encryption or decryption using KEY32 and IV16.
     */
    protected Cipher getCipher(boolean forEncryption)
    {
        return getCipher(forEncryption, false);
    }

    abstract Cipher getCipherInstance(boolean forEncryption);

    private Cipher getCipher(boolean forEncryption, boolean reset)
    {
        Cipher cipher = (forEncryption) ? encryptCipher : decryptCipher;

        if (reset || cipher == null) {
            cipher = getCipherInstance(forEncryption);
            if (forEncryption) {
                encryptCipher = cipher;
            } else {
                decryptCipher = cipher;
            }
        }
        return cipher;
    }

    /**
     * Cache the encrypted results for BYTES, for faster comparison, or use initialized constant.
     * @return the encrypted bytes of BYTES.
     */
    protected byte[] getEncrypted() throws Exception
    {
        if (encrypted == null) {
            encrypted = getCipher(true).doFinal(BYTES);
        }
        return encrypted;
    }

    /**
     * Cache the encrypted results for ANOTHER_BYTES, for faster comparison, or use initialized constant.
     * @return the encrypted bytes of ANOTHER_BYTES.
     */
    protected byte[] getAnotherEncrypted() throws Exception
    {
        if (anotherEncrypted == null) {
            anotherEncrypted = getCipher(true).doFinal(ANOTHER_BYTES);
        }
        return anotherEncrypted;
    }

    @Test
    public void cipherOneShotEncryption() throws Exception
    {
        Cipher cipher = getCipher(true);

        assertEquals(this.bytesEncryptedSize, getEncrypted().length);
        assertArrayEquals(getEncrypted(), cipher.doFinal(BYTES));

        assertEquals(this.anotherBytesEncryptedSize, getAnotherEncrypted().length);
        assertArrayEquals(getAnotherEncrypted(), cipher.doFinal(ANOTHER_BYTES));
    }

    @Test
    public void cipherOneShotDecryption() throws Exception
    {
        Cipher cipher = getCipher(false);

        byte[] result = cipher.doFinal(getEncrypted());
        assertEquals(BYTES.length, result.length);
        assertArrayEquals(BYTES, result);

        result = cipher.doFinal(getAnotherEncrypted());
        assertEquals(ANOTHER_BYTES.length, result.length);
        assertArrayEquals(ANOTHER_BYTES, result);
    }

    private byte[] getProgressive(boolean forEncryption, byte[] bytes, int size) throws Exception
    {
        Cipher cipher = getCipher(forEncryption);
        byte[] result = new byte[size];
        byte[] tmp;
        int len = 0;

        tmp = cipher.update(bytes, 0, this.blockSize + 1);
        assertNotNull(tmp);
        System.arraycopy(tmp, 0, result, 0, len = tmp.length);

        assertNull(cipher.update(bytes, this.blockSize + 1, this.blockSize - 1));

        tmp = cipher.update(bytes, this.blockSize * 2, 1);
        assertNotNull(tmp);
        System.arraycopy(tmp, 0, result, len, tmp.length);
        len += tmp.length;

        tmp = cipher.update(bytes, ((this.blockSize * 2) + 1), bytes.length - ((this.blockSize * 2) + 1));
        assertNotNull(tmp);
        System.arraycopy(tmp, 0, result, len, tmp.length);
        len += tmp.length;

        tmp = cipher.doFinal();
        if (forEncryption || tmp != null) {
            assertNotNull(tmp);
            System.arraycopy(tmp, 0, result, len, tmp.length);
            len += tmp.length;
        }

        return result;
    }

    @Test
    public void cipherProgressiveEncryption() throws Exception
    {
        assertArrayEquals(getEncrypted(), getProgressive(true, BYTES, this.bytesEncryptedSize));
        assertArrayEquals(getAnotherEncrypted(), getProgressive(true, ANOTHER_BYTES, this.anotherBytesEncryptedSize));
    }

    @Test
    public void cipherProgressiveDecryption() throws Exception
    {
        assertArrayEquals(BYTES, getProgressive(false, getEncrypted(), BYTES.length));
        assertArrayEquals(ANOTHER_BYTES, getProgressive(false, getAnotherEncrypted(), ANOTHER_BYTES.length));
    }

    @Test
    public void cipherOutputStreamEncryption() throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(this.bytesEncryptedSize);
        OutputStream encos = getCipher(true).getOutputStream(baos);
        encos.write(BYTES);
        encos.close();

        assertArrayEquals(getEncrypted(), baos.toByteArray());

        baos = new ByteArrayOutputStream(this.anotherBytesEncryptedSize);
        encos = getCipher(true).getOutputStream(baos);
        encos.write(ANOTHER_BYTES);
        encos.close();

        assertArrayEquals(getAnotherEncrypted(), baos.toByteArray());
    }

    @Test
    public void cipherOutputStreamDecryption() throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTES.length);
        OutputStream encos = getCipher(false).getOutputStream(baos);
        encos.write(getEncrypted());
        encos.close();

        assertArrayEquals(BYTES, baos.toByteArray());

        baos = new ByteArrayOutputStream(ANOTHER_BYTES.length);
        encos = getCipher(false).getOutputStream(baos);
        encos.write(getAnotherEncrypted());
        encos.close();

        assertArrayEquals(ANOTHER_BYTES, baos.toByteArray());
    }

    private int readAll(InputStream decis, byte[] out) throws IOException
    {
        int readLen = 0, len = 0;
        while( (readLen = decis.read(out, len, this.blockSize + 1)) > 0 ) {
            len += readLen;
        }
        decis.close();
        return len;
    }

    @Test
    public void cipheInputStreamEncryption() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(BYTES);
        InputStream decis = getCipher(true).getInputStream(bais);
        byte[] buf = new byte[this.bytesEncryptedSize];
        assertEquals(this.bytesEncryptedSize, readAll(decis, buf));
        assertArrayEquals(getEncrypted(), buf);

        bais = new ByteArrayInputStream(ANOTHER_BYTES);
        decis = getCipher(true).getInputStream(bais);
        buf = new byte[this.anotherBytesEncryptedSize];
        assertEquals(this.anotherBytesEncryptedSize, readAll(decis, buf));
        assertArrayEquals(getAnotherEncrypted(), buf);
    }

    @Test
    public void cipheInputStreamDecryption() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(getEncrypted());
        InputStream decis = getCipher(false).getInputStream(bais);
        byte[] buf = new byte[BYTES.length];
        assertEquals(BYTES.length, readAll(decis, buf));
        assertArrayEquals(BYTES, buf);

        bais = new ByteArrayInputStream(getAnotherEncrypted());
        decis = getCipher(false).getInputStream(bais);
        buf = new byte[ANOTHER_BYTES.length];
        assertEquals(ANOTHER_BYTES.length, readAll(decis, buf));
        assertArrayEquals(ANOTHER_BYTES, buf);
    }

    class WrongParameters implements SymmetricCipherParameters
    { }

    @Test
    public void cipherWithWrongParameters()
    {
        WrongParameters parameters = new WrongParameters();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> getFactory().getInstance(true, parameters));
        assertEquals("Invalid parameters for cipher: " + WrongParameters.class.getName(), ex.getMessage());
    }

    class AsymmetricParameters implements AsymmetricCipherParameters
    { }

    @Test
    public void cipherWithAsymmetricParameters()
    {
        AsymmetricParameters parameters = new AsymmetricParameters();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> getFactory().getInstance(true, parameters));
        assertEquals("Unexpected parameters received for a symmetric cipher: " + AsymmetricParameters.class.getName(),
            ex.getMessage());
    }
}
