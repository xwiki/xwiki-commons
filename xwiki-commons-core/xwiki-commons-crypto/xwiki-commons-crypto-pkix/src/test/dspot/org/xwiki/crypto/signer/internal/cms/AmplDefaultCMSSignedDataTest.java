package org.xwiki.crypto.signer.internal.cms;


import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ComponentList({ Base64BinaryStringEncoder.class, BcRSAKeyFactory.class, BcDSAKeyFactory.class, DefaultDigestFactory.class, BcSHA1DigestFactory.class, BcSHA1withRsaSignerFactory.class, BcDSAwithSHA1SignerFactory.class, DefaultSignerFactory.class, BcX509CertificateFactory.class, DefaultBcContentVerifierProviderBuilder.class, BcStoreX509CertificateProvider.class, BcX509CertificateChainBuilder.class })
public class AmplDefaultCMSSignedDataTest extends AbstractPKIXTest {
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

    public void setupTest(MockitoComponentMockingRule<CMSSignedDataGenerator> mocker) throws Exception {
        if ((rsaPrivateKey) == null) {
            BinaryStringEncoder base64encoder = mocker.getInstance(BinaryStringEncoder.class, "Base64");
            AsymmetricKeyFactory rsaKeyFactory = mocker.getInstance(AsymmetricKeyFactory.class, "RSA");
            AsymmetricKeyFactory dsaKeyFactory = mocker.getInstance(AsymmetricKeyFactory.class, "DSA");
            CertificateFactory certFactory = mocker.getInstance(CertificateFactory.class, "X509");
            rsaPrivateKey = rsaKeyFactory.fromPKCS8(base64encoder.decode(AbstractPKIXTest.RSA_PRIVATE_KEY));
            dsaPrivateKey = dsaKeyFactory.fromPKCS8(base64encoder.decode(AbstractPKIXTest.DSA_PRIVATE_KEY));
            v3CaCert = certFactory.decode(base64encoder.decode(AbstractPKIXTest.V3_CA_CERT));
            v3InterCaCert = certFactory.decode(base64encoder.decode(AbstractPKIXTest.V3_ITERCA_CERT));
            v3Cert = certFactory.decode(base64encoder.decode(AbstractPKIXTest.V3_CERT));
            text = AbstractPKIXTest.TEXT.getBytes("UTF-8");
            rsaSignerFactory = mocker.getInstance(SignerFactory.class, "SHA1withRSAEncryption");
            dsaSignerFactory = mocker.getInstance(SignerFactory.class, "DSAwithSHA1");
        }
    }

    @Before
    public void configure() throws Exception {
        generator = generatorMocker.getComponentUnderTest();
        verifier = verifierMocker.getComponentUnderTest();
        setupTest(generatorMocker);
    }

    @Test(timeout = 10000)
    public void testDSADetachedSignatureWithEmbeddedCerts() throws Exception {
        CMSSignedDataVerified result = verifier.verify(generator.generate(text, new CMSSignedDataGeneratorParameters().addSigner(CertifyingSigner.getInstance(true, new CertifiedKeyPair(dsaPrivateKey, v3Cert), dsaSignerFactory)).addCertificate(v3Cert).addCertificate(v3InterCaCert).addCertificate(v3CaCert)), text);
        assertTrue(result.isVerified());
        result.getCertificates();
        Matchers.containsInAnyOrder(v3CaCert, v3InterCaCert, v3Cert);
        result.getContent();
        Matcher<byte[]> o_testDSADetachedSignatureWithEmbeddedCerts__17 = Matchers.equalTo(new byte[] {67, 111, 110, 103, 114, 101, 115, 115, 32, 115, 104, 97, 108, 108, 32, 109, 97, 107, 101, 32, 110, 111, 32, 108, 97, 119, 32, 114, 101, 115, 112, 101, 99, 116, 105, 110, 103, 32, 97, 110, 32, 101, 115, 116, 97, 98, 108, 105, 115, 104, 109, 101, 110, 116, 32, 111, 102, 32, 114, 101, 108, 105, 103, 105, 111, 110, 44, 32, 111, 114, 32, 112, 114, 111, 104, 105, 98, 105, 116, 105, 110, 103, 32, 116, 104, 101, 32, 102, 114, 101, 101, 32, 101, 120, 101, 114, 99, 105, 115, 101, 32, 116, 104, 101, 114, 101, 111, 102, 59, 32, 111, 114, 32, 97, 98, 114, 105, 100, 103, 105, 110, 103, 32, 116, 104, 101, 32, 102, 114, 101, 101, 100, 111, 109, 32, 111, 102, 32, 115, 112, 101, 101, 99, 104, 44, 32, 111, 114, 32, 111, 102, 32, 116, 104, 101, 32, 112, 114, 101, 115, 115, 59, 32, 111, 114, 32, 116, 104, 101, 32, 114, 105, 103, 104, 116, 32, 111, 102, 32, 116, 104, 101, 32, 112, 101, 111, 112, 108, 101, 32, 112, 101, 97, 99, 101, 97, 98, 108, 121, 32, 116, 111, 32, 97, 115, 115, 101, 109, 98, 108, 101, 44, 32, 97, 110, 100, 32, 116, 111, 32, 112, 101, 116, 105, 116, 105, 111, 110, 32, 116, 104, 101, 32, 71, 111, 118, 101, 114, 110, 109, 101, 110, 116, 32, 102, 111, 114, 32, 97, 32, 114, 101, 100, 114, 101, 115, 115, 32, 111, 102, 32, 103, 114, 105, 101, 118, 97, 110, 99, 101, 115, 46});
        MatcherAssert.assertThat(text, o_testDSADetachedSignatureWithEmbeddedCerts__17);
        assertEquals("1.2.840.113549.1.7.1", result.getContentType());
        int o_testDSADetachedSignatureWithEmbeddedCerts__20 = result.getSignatures().size();
        Assert.assertEquals(1, ((int) (o_testDSADetachedSignatureWithEmbeddedCerts__20)));
        Matcher<Integer> o_testDSADetachedSignatureWithEmbeddedCerts__22 = Matchers.equalTo(1);
        Assert.assertEquals("<1>", ((IsEqual) (o_testDSADetachedSignatureWithEmbeddedCerts__22)).toString());
        CMSSignerVerifiedInformation signerInfo = result.getSignatures().iterator().next();
        signerInfo.isVerified();
        Matcher<Boolean> o_testDSADetachedSignatureWithEmbeddedCerts__28 = Matchers.equalTo(true);
        Assert.assertEquals("<true>", ((IsEqual) (o_testDSADetachedSignatureWithEmbeddedCerts__28)).toString());
        signerInfo.getCertificateChain();
        Matchers.contains(v3CaCert, v3InterCaCert, v3Cert);
    }
}

