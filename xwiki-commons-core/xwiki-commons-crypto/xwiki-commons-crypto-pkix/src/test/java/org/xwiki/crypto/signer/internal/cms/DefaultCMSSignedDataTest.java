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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
import org.xwiki.crypto.signer.CMSSignedDataGenerator;
import org.xwiki.crypto.signer.CMSSignedDataVerifier;
import org.xwiki.crypto.signer.SignerFactory;
import org.xwiki.crypto.signer.internal.DefaultBcContentVerifierProviderBuilder;
import org.xwiki.crypto.signer.internal.factory.BcDSAwithSHA1SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSHA1withRsaSignerFactory;
import org.xwiki.crypto.signer.internal.factory.DefaultSignerFactory;
import org.xwiki.crypto.signer.param.CMSSignedDataGeneratorParameters;
import org.xwiki.crypto.signer.param.CMSSignedDataVerified;
import org.xwiki.crypto.signer.param.CMSSignerVerifiedInformation;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ComponentList({Base64BinaryStringEncoder.class, BcRSAKeyFactory.class, BcDSAKeyFactory.class,
    DefaultDigestFactory.class, BcSHA1DigestFactory.class, BcSHA1withRsaSignerFactory.class,
    BcDSAwithSHA1SignerFactory.class, DefaultSignerFactory.class, BcX509CertificateFactory.class,
    DefaultBcContentVerifierProviderBuilder.class, BcStoreX509CertificateProvider.class,
    BcX509CertificateChainBuilder.class})
public class DefaultCMSSignedDataTest extends AbstractPKIXTest
{
    @Rule
    public final MockitoComponentMockingRule<CMSSignedDataGenerator> generatorMocker =
        new MockitoComponentMockingRule<>(DefaultCMSSignedDataGenerator.class);

    @Rule
    public final MockitoComponentMockingRule<CMSSignedDataVerifier> verifierMocker =
        new MockitoComponentMockingRule<>(DefaultCMSSignedDataVerifier.class);

    private CMSSignedDataGenerator generator;
    private CMSSignedDataVerifier verifier;
    @SuppressWarnings("unused")
    private static SignerFactory rsaSignerFactory;
    private static SignerFactory dsaSignerFactory;
    private static PrivateKeyParameters rsaPrivateKey;
    private static PrivateKeyParameters dsaPrivateKey;
    private static CertifiedPublicKey v3CaCert;
    private static CertifiedPublicKey v3InterCaCert;
    private static CertifiedPublicKey v3Cert;
    protected static byte[] text;

    public void setupTest(MockitoComponentMockingRule<CMSSignedDataGenerator> mocker) throws Exception
    {
        // Decode keys once for all tests.
        if (rsaPrivateKey == null) {
            BinaryStringEncoder base64encoder = mocker.getInstance(BinaryStringEncoder.class, "Base64");
            AsymmetricKeyFactory rsaKeyFactory = mocker.getInstance(AsymmetricKeyFactory.class, "RSA");
            AsymmetricKeyFactory dsaKeyFactory = mocker.getInstance(AsymmetricKeyFactory.class, "DSA");
            CertificateFactory certFactory = mocker.getInstance(CertificateFactory.class, "X509");
            rsaPrivateKey = rsaKeyFactory.fromPKCS8(base64encoder.decode(RSA_PRIVATE_KEY));
            dsaPrivateKey = dsaKeyFactory.fromPKCS8(base64encoder.decode(DSA_PRIVATE_KEY));
            v3CaCert = certFactory.decode(base64encoder.decode(V3_CA_CERT));
            v3InterCaCert = certFactory.decode(base64encoder.decode(V3_ITERCA_CERT));
            v3Cert = certFactory.decode(base64encoder.decode(V3_CERT));
            text = TEXT.getBytes("UTF-8");
            rsaSignerFactory = mocker.getInstance(SignerFactory.class, "SHA1withRSAEncryption");
            dsaSignerFactory = mocker.getInstance(SignerFactory.class, "DSAwithSHA1");
        }
    }

    @Before
    public void configure() throws Exception
    {
        generator = generatorMocker.getComponentUnderTest();
        verifier = verifierMocker.getComponentUnderTest();
        setupTest(generatorMocker);
    }

    @Test
    public void testDSASignatureAllEmbedded() throws Exception
    {
        byte[] signature = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
                .addCertificate(v3Cert)
                .addCertificate(v3InterCaCert)
                .addCertificate(v3CaCert), true);

        CMSSignedDataVerified result = verifier.verify(signature);

        assertThat(result.isVerified(), equalTo(true));
        assertThat(result.getCertificates(), containsInAnyOrder(v3CaCert, v3InterCaCert, v3Cert));
        assertThat(result.getContent(), equalTo(text));
        assertThat(result.getContentType(), equalTo("1.2.840.113549.1.7.1"));
        assertThat(result.getSignatures().size(),equalTo(1));

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertThat(signerInfo.isVerified(), equalTo(true));
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    public void testDSASignatureWithExternalCerts() throws Exception
    {
        byte[] signature = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory)), true);

        CMSSignedDataVerified result = verifier.verify(signature, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        assertThat(result.isVerified(), equalTo(true));
        assertThat(result.getCertificates().isEmpty(), equalTo(true));
        assertThat(result.getContent(), equalTo(text));
        assertThat(result.getContentType(), equalTo("1.2.840.113549.1.7.1"));
        assertThat(result.getSignatures().size(),equalTo(1));

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertThat(signerInfo.isVerified(), equalTo(true));
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    public void testDSADetachedSignatureWithEmbeddedCerts() throws Exception
    {
        byte[] signature = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
                .addCertificate(v3Cert)
                .addCertificate(v3InterCaCert)
                .addCertificate(v3CaCert));

        CMSSignedDataVerified result = verifier.verify(signature, text);

        assertThat(result.isVerified(), equalTo(true));
        assertThat(result.getCertificates(), containsInAnyOrder(v3CaCert, v3InterCaCert, v3Cert));
        assertThat(result.getContent(), equalTo(text));
        assertThat(result.getContentType(), equalTo("1.2.840.113549.1.7.1"));
        assertThat(result.getSignatures().size(),equalTo(1));

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertThat(signerInfo.isVerified(), equalTo(true));
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    public void testDSADetachedSignatureWithExternalCerts() throws Exception
    {
        byte[] signature = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory)));

        CMSSignedDataVerified result = verifier.verify(signature, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        assertThat(result.isVerified(), equalTo(true));
        assertThat(result.getCertificates().isEmpty(), equalTo(true));
        assertThat(result.getContent(), equalTo(text));
        assertThat(result.getContentType(), equalTo("1.2.840.113549.1.7.1"));
        assertThat(result.getSignatures().size(),equalTo(1));

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertThat(signerInfo.isVerified(), equalTo(true));
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    public void testDSADetachedSignatureWitMixedCerts() throws Exception
    {
        byte[] signature = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
                .addCertificate(v3Cert));

        CMSSignedDataVerified result = verifier.verify(signature, text, Arrays.asList(v3InterCaCert, v3CaCert));

        assertThat(result.isVerified(), equalTo(true));
        assertThat(result.getCertificates(), containsInAnyOrder(v3Cert));
        assertThat(result.getContent(), equalTo(text));
        assertThat(result.getContentType(), equalTo("1.2.840.113549.1.7.1"));
        assertThat(result.getSignatures().size(),equalTo(1));

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertThat(signerInfo.isVerified(), equalTo(true));
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    public void testPreCalculatedSignature() throws Exception
    {
        byte[] signature = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
        );

        CMSSignedDataVerified result = verifier.verify(signature, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        byte[] signature2 = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSignature(result.getSignatures().iterator().next())
        );

        result = verifier.verify(signature2, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        assertThat(signature2, equalTo(signature));
    }

    @Test
    public void testAddingCertificatesToSignature() throws Exception
    {
        byte[] signature = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSigner(CertifyingSigner.getInstance(true,
                    new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory))
        );

        CMSSignedDataVerified result = verifier.verify(signature, text, Arrays.asList(v3Cert, v3InterCaCert, v3CaCert));

        byte[] signature2 = generator.generate(text,
            new CMSSignedDataGeneratorParameters()
                .addSignature(result.getSignatures().iterator().next())
                .addCertificates(result.getSignatures().iterator().next().getCertificateChain())
        );

        result = verifier.verify(signature2, text);

        assertThat(result.isVerified(), equalTo(true));
        assertThat(result.getCertificates(), containsInAnyOrder(v3CaCert, v3InterCaCert, v3Cert));
        assertThat(result.getSignatures().size(),equalTo(1));

        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();

        assertThat(signerInfo.isVerified(), equalTo(true));
        assertThat(signerInfo.getCertificateChain(), contains(v3CaCert, v3InterCaCert, v3Cert));
    }
}
