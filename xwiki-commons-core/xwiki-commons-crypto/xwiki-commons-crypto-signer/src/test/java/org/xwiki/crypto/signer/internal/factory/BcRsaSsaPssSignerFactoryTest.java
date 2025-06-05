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
package org.xwiki.crypto.signer.internal.factory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA1DigestFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA224DigestFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA256DigestFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA384DigestFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA512DigestFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.signer.Signer;
import org.xwiki.crypto.signer.params.PssSignerParameters;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.assertSignatureVerification;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.privateKey;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.publicKey;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.setupTest;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.text;

@ComponentList({
    Base64BinaryStringEncoder.class,
    BcRSAKeyFactory.class,
    BcSHA1DigestFactory.class,
    BcSHA224DigestFactory.class,
    BcSHA256DigestFactory.class,
    BcSHA384DigestFactory.class,
    BcSHA512DigestFactory.class
})
@ComponentTest
class BcRsaSsaPssSignerFactoryTest
{
    @InjectMockComponents
    private BcRsaSsaPssSignerFactory factory;

    @InjectComponentManager
    private ComponentManager componentManager;

    @BeforeEach
    void setUp() throws Exception
    {
        setupTest(this.componentManager);
    }

    @Test
    void defaultSignatureVerification() throws Exception
    {
        assertSignatureVerification(
            this.factory.getInstance(true, privateKey),
            this.factory.getInstance(false, publicKey)
        );
    }

    @Test
    void sha224SignatureVerification() throws Exception
    {
        assertSignatureVerification(
            this.factory.getInstance(true, new PssSignerParameters(privateKey, "SHA-224", -1)),
            this.factory.getInstance(false, new PssSignerParameters(publicKey, "SHA-224", -1))
        );
    }

    @Test
    void sha256SignatureVerification() throws Exception
    {
        assertSignatureVerification(
            this.factory.getInstance(true, new PssSignerParameters(privateKey, "SHA-256", -1)),
            this.factory.getInstance(false, new PssSignerParameters(publicKey, "SHA-256", -1))
        );
    }

    @Test
    void sha384SignatureVerification() throws Exception
    {
        assertSignatureVerification(
            this.factory.getInstance(true, new PssSignerParameters(privateKey, "SHA-384", -1)),
            this.factory.getInstance(false, new PssSignerParameters(publicKey, "SHA-384", -1))
        );
    }

    @Test
    void sha512SignatureVerification() throws Exception
    {
        assertSignatureVerification(
            this.factory.getInstance(true, new PssSignerParameters(privateKey, "SHA-512", -1)),
            this.factory.getInstance(false, new PssSignerParameters(publicKey, "SHA-512", -1))
        );
    }

    @Test
    void encodedDefaultSignatureVerification() throws Exception
    {
        Signer signer = this.factory.getInstance(true, privateKey);
        Signer verifier = this.factory.getInstance(false, publicKey, signer.getEncoded());

        assertSignatureVerification(signer, verifier);
    }

    @ParameterizedTest
    @CsvSource({
        "SHA-224",
        "SHA-256",
        "SHA-384",
        "SHA-512"
    })
    void encodedSha224SignatureVerification(String hashAlgorithm) throws Exception
    {
        Signer signer = this.factory.getInstance(true, new PssSignerParameters(privateKey, hashAlgorithm, -1));
        Signer verifier = this.factory.getInstance(false, publicKey, signer.getEncoded());

        assertSignatureVerification(signer, verifier);
    }

    private void progressiveUpdateSignature(Signer signer, byte[] bytes, int blockSize) throws Exception
    {
        signer.update(bytes, 0, blockSize + 1);
        signer.update(bytes, blockSize + 1, blockSize - 1);
        signer.update(bytes, blockSize * 2, 1);
        signer.update(bytes[(blockSize * 2) + 1]);
        signer.update(bytes, ((blockSize * 2) + 2), bytes.length - ((blockSize * 2) + 2));
    }

    @Test
    void progressiveSignatureVerification() throws Exception
    {
        Signer signer = this.factory.getInstance(true, privateKey);
        progressiveUpdateSignature(signer, text, 17);

        byte[] signature = signer.generate();

        Signer verifier = this.factory.getInstance(true, publicKey);
        progressiveUpdateSignature(verifier, text, 15);

        assertTrue(verifier.verify(signature));
    }

    @Test
    void partialBufferVerification() throws Exception
    {
        Signer signer = this.factory.getInstance(true, privateKey);

        byte[] source = new byte[text.length * 2];
        System.arraycopy(text, 0, source, text.length - 10, text.length);

        byte[] signature = signer.generate(source, text.length - 10, text.length);

        byte[] sign = new byte[signature.length * 2];
        System.arraycopy(signature, 0, sign, signature.length - 5, signature.length);

        Signer verifier = this.factory.getInstance(true, publicKey);
        assertTrue(
            verifier.verify(sign, signature.length - 5, signature.length, source, text.length - 10, text.length));
    }

    private int readAll(InputStream decis, byte[] out) throws IOException
    {
        int readLen = 0, len = 0;
        while ((readLen = decis.read(out, len, Math.min(15, out.length - len))) > 0) {
            len += readLen;
        }
        decis.close();
        return len;
    }

    @Test
    void streamSignatureVerification() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(text);

        Signer signer = this.factory.getInstance(true, privateKey);
        InputStream input = signer.getInputStream(bais);

        byte[] buf = new byte[text.length];
        assertThat(readAll(input, buf), equalTo(text.length));
        assertThat(buf, equalTo(text));

        byte[] signature = signer.generate();

        Signer verifier = this.factory.getInstance(false, publicKey);
        OutputStream output = verifier.getOutputStream();

        output.write(text);
        assertTrue(verifier.verify(signature));
    }
}
