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

package org.xwiki.crypto.signer.internal.cms;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.AbstractPKIXTest;
import org.xwiki.crypto.AsymmetricKeyFactory;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcDSAKeyFactory;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA1DigestFactory;
import org.xwiki.crypto.internal.digest.factory.DefaultDigestFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.params.cipher.asymmetric.PrivateKeyParameters;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.CertifyingSigner;
import org.xwiki.crypto.pkix.internal.BcStoreX509CertificateProvider;
import org.xwiki.crypto.pkix.internal.BcX509CertificateChainBuilder;
import org.xwiki.crypto.pkix.internal.BcX509CertificateFactory;
import org.xwiki.crypto.pkix.params.CertifiedKeyPair;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.SignerFactory;
import org.xwiki.crypto.signer.internal.DefaultBcContentVerifierProviderBuilder;
import org.xwiki.crypto.signer.internal.factory.BcDSAwithSHA1SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSHA1withRsaSignerFactory;
import org.xwiki.crypto.signer.internal.factory.DefaultSignerFactory;
import org.xwiki.crypto.signer.param.CMSSignedDataGeneratorParameters;
import org.xwiki.crypto.signer.param.CMSSignedDataVerified;
import org.xwiki.crypto.signer.param.CMSSignerVerifiedInformation;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
@ComponentList({ Base64BinaryStringEncoder.class, BcRSAKeyFactory.class, BcDSAKeyFactory.class,
    DefaultDigestFactory.class, BcSHA1DigestFactory.class, BcSHA1withRsaSignerFactory.class,
    BcDSAwithSHA1SignerFactory.class, DefaultSignerFactory.class, BcX509CertificateFactory.class,
    DefaultBcContentVerifierProviderBuilder.class, BcStoreX509CertificateProvider.class,
    BcX509CertificateChainBuilder.class })
class DefaultCMSSignedDataTest extends AbstractPKIXTest
{
    @InjectMockComponents
    private DefaultCMSSignedDataGenerator generator;

    @InjectMockComponents
    private DefaultCMSSignedDataVerifier verifier;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @SuppressWarnings("unused")
    private static SignerFactory rsaSignerFactory;

    private static SignerFactory dsaSignerFactory;

    private static PrivateKeyParameters rsaPrivateKey;

    private static PrivateKeyParameters dsaPrivateKey;

    private static CertifiedPublicKey v3CaCert;

    private static CertifiedPublicKey v3InterCaCert;

    private static CertifiedPublicKey v3Cert;

    protected static byte[] text;

    @BeforeEach
    void configure() throws Exception
    {
        // Decode keys once for all tests.
        if (rsaPrivateKey == null) {
            BinaryStringEncoder base64encoder = this.componentManager.getInstance(BinaryStringEncoder.class, "Base64");
            AsymmetricKeyFactory rsaKeyFactory = this.componentManager.getInstance(AsymmetricKeyFactory.class, "RSA");
            AsymmetricKeyFactory dsaKeyFactory = this.componentManager.getInstance(AsymmetricKeyFactory.class, "DSA");
            CertificateFactory certFactory = this.componentManager.getInstance(CertificateFactory.class, "X509");
            rsaPrivateKey = rsaKeyFactory.fromPKCS8(base64encoder.decode(RSA_PRIVATE_KEY));
            dsaPrivateKey = dsaKeyFactory.fromPKCS8(base64encoder.decode(DSA_PRIVATE_KEY));
            v3CaCert = certFactory.decode(base64encoder.decode(V3_CA_CERT));
            v3InterCaCert = certFactory.decode(base64encoder.decode(V3_ITERCA_CERT));
            v3Cert = certFactory.decode(base64encoder.decode(V3_CERT));
            text = TEXT.getBytes(StandardCharsets.UTF_8);
            rsaSignerFactory = this.componentManager.getInstance(SignerFactory.class, "SHA1withRSAEncryption");
            dsaSignerFactory = this.componentManager.getInstance(SignerFactory.class, "DSAwithSHA1");
        }
    }

    @Test
    void testDSASignatureAllEmbedded() throws Exception
    {
        byte[] signature = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
                .addCertificate(v3Cert)
                .addCertificate(v3InterCaCert)
                .addCertificate(v3CaCert), true);

        CMSSignedDataVerified result = this.verifier.verify(signature);

        assertTrue(result.isVerified());
        assertTrue(result.getCertificates().containsAll(java.util.Arrays.asList(v3CaCert, v3InterCaCert, v3Cert)) &&
            result.getCertificates().size() == 3);
        assertArrayEquals(text, result.getContent());
        assertEquals("1.2.840.113549.1.7.1", result.getContentType());
        assertEquals(1, result.getSignatures().size());

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertTrue(signerInfo.isVerified());
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    void testDSASignatureWithExternalCerts() throws Exception
    {
        byte[] signature = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory)), true);

        CMSSignedDataVerified result = this.verifier.verify(signature, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        assertTrue(result.isVerified());
        assertTrue(result.getCertificates().isEmpty());
        assertArrayEquals(text, result.getContent());
        assertEquals("1.2.840.113549.1.7.1", result.getContentType());
        assertEquals(1, result.getSignatures().size());

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertTrue(signerInfo.isVerified());
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    void testDSADetachedSignatureWithEmbeddedCerts() throws Exception
    {
        byte[] signature = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
                .addCertificate(v3Cert)
                .addCertificate(v3InterCaCert)
                .addCertificate(v3CaCert));

        CMSSignedDataVerified result = this.verifier.verify(signature, text);

        assertTrue(result.isVerified());
        assertThat(result.getCertificates(), containsInAnyOrder(v3CaCert, v3InterCaCert, v3Cert));
        assertArrayEquals(text, result.getContent());
        assertEquals("1.2.840.113549.1.7.1", result.getContentType());
        assertEquals(1, result.getSignatures().size());

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertTrue(signerInfo.isVerified());
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    void testDSADetachedSignatureWithExternalCerts() throws Exception
    {
        byte[] signature = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory)));

        CMSSignedDataVerified result =
            this.verifier.verify(signature, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        assertTrue(result.isVerified());
        assertTrue(result.getCertificates().isEmpty());
        assertArrayEquals(text, result.getContent());
        assertEquals("1.2.840.113549.1.7.1", result.getContentType());
        assertEquals(1, result.getSignatures().size());

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertTrue(signerInfo.isVerified());
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    void testDSADetachedSignatureWitMixedCerts() throws Exception
    {
        byte[] signature = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
                .addCertificate(v3Cert));

        CMSSignedDataVerified result = this.verifier.verify(signature, text, Arrays.asList(v3InterCaCert, v3CaCert));

        assertTrue(result.isVerified());
        assertEquals(1, result.getCertificates().size());
        assertThat(result.getCertificates(), containsInAnyOrder(v3Cert));
        assertArrayEquals(text, result.getContent());
        assertEquals("1.2.840.113549.1.7.1", result.getContentType());
        assertEquals(1, result.getSignatures().size());

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertTrue(signerInfo.isVerified());
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    void testPreCalculatedSignature() throws Exception
    {
        byte[] signature = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
        );

        CMSSignedDataVerified result =
            this.verifier.verify(signature, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        byte[] signature2 = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSignature(result.getSignatures().iterator().next())
        );

        result = this.verifier.verify(signature2, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        assertArrayEquals(signature, signature2);
    }

    @Test
    void testAddingCertificatesToSignature() throws Exception
    {
        byte[] signature = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
        );

        CMSSignedDataVerified result =
            this.verifier.verify(signature, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        byte[] signature2 = this.generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSignature(result.getSignatures().iterator().next())
                .addCertificates(result.getSignatures().iterator().next().getCertificateChain())
        );

        result = this.verifier.verify(signature2, text);

        assertTrue(result.isVerified());
        assertThat(result.getCertificates(), containsInAnyOrder(v3CaCert, v3InterCaCert, v3Cert));
        assertEquals(1, result.getSignatures().size());

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertTrue(signerInfo.isVerified());
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }
}
