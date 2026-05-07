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
package org.xwiki.crypto.pkix.internal;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.bouncycastle.util.IPAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.AbstractPKIXTest;
import org.xwiki.crypto.AsymmetricKeyFactory;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.DefaultSecureRandomProvider;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcDSAKeyFactory;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA1DigestFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.params.cipher.asymmetric.PrivateKeyParameters;
import org.xwiki.crypto.params.cipher.asymmetric.PublicKeyParameters;
import org.xwiki.crypto.pkix.CertificateGenerator;
import org.xwiki.crypto.pkix.CertifyingSigner;
import org.xwiki.crypto.pkix.X509ExtensionBuilder;
import org.xwiki.crypto.pkix.internal.extension.DefaultX509ExtensionBuilder;
import org.xwiki.crypto.pkix.params.CertifiedKeyPair;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertificateGenerationParameters;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertificateParameters;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.extension.ExtendedKeyUsages;
import org.xwiki.crypto.pkix.params.x509certificate.extension.KeyUsage;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509DirectoryName;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509DnsName;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509Extensions;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509GeneralName;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509IpAddress;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509Rfc822Name;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509StringGeneralName;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509URI;
import org.xwiki.crypto.signer.SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcDSAwithSHA1SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSHA1withRsaSignerFactory;
import org.xwiki.crypto.signer.internal.factory.DefaultSignerFactory;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ComponentTest
// @formatter:off
@ComponentList({
    Base64BinaryStringEncoder.class,
    DefaultSecureRandomProvider.class,
    BcRSAKeyFactory.class,
    BcDSAKeyFactory.class,
    BcSHA1DigestFactory.class,
    BcSHA1withRsaSignerFactory.class,
    DefaultSignerFactory.class,
    BcX509CertificateFactory.class,
    BcDSAwithSHA1SignerFactory.class
})
// @formatter:on
class BcX509CertificateGeneratorFactoryTest extends AbstractPKIXTest
{
    @InjectMockComponents
    private BcX509CertificateGeneratorFactory factory;

    @InjectMockComponents
    private DefaultX509ExtensionBuilder builder;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private SignerFactory signerFactory;

    private static PrivateKeyParameters rsaPrivateKey;

    private static PublicKeyParameters rsaPublicKey;

    private static PrivateKeyParameters interCaDsaPrivateKey;

    private static PublicKeyParameters interCaDsaPublicKey;

    @SuppressWarnings("unused")
    private static PrivateKeyParameters dsaPrivateKey;

    private static PublicKeyParameters dsaPublicKey;

    public void setupTest() throws Exception
    {
        // Decode keys once for all tests.
        if (rsaPrivateKey == null) {
            BinaryStringEncoder base64encoder = this.componentManager.getInstance(BinaryStringEncoder.class, "Base64");
            AsymmetricKeyFactory keyFactory = this.componentManager.getInstance(AsymmetricKeyFactory.class, "RSA");
            rsaPrivateKey = keyFactory.fromPKCS8(base64encoder.decode(RSA_PRIVATE_KEY));
            rsaPublicKey = keyFactory.fromX509(base64encoder.decode(RSA_PUBLIC_KEY));
            keyFactory = this.componentManager.getInstance(AsymmetricKeyFactory.class, "DSA");
            interCaDsaPrivateKey = keyFactory.fromPKCS8(base64encoder.decode(INTERCA_DSA_PRIVATE_KEY));
            interCaDsaPublicKey = keyFactory.fromX509(base64encoder.decode(INTERCA_DSA_PUBLIC_KEY));
            dsaPrivateKey = keyFactory.fromPKCS8(base64encoder.decode(DSA_PRIVATE_KEY));
            dsaPublicKey = keyFactory.fromX509(base64encoder.decode(DSA_PUBLIC_KEY));
        }
    }

    @BeforeEach
    void configure() throws Exception
    {
        this.signerFactory = this.componentManager.getInstance(SignerFactory.class, "SHA1withRSAEncryption");
        setupTest();
    }

/*
    private void dumpCert(CertifiedPublicKey certificate) throws Exception {
        BinaryStringEncoder base64encoder = mocker.getInstance(BinaryStringEncoder.class, "Base64");
        System.out.println("-----BEGIN CERTIFICATE-----");
        System.out.println(base64encoder.encode(certificate.getEncoded(), 64));
        System.out.println("-----END CERTIFICATE-----");
    }
*/

    private X509CertifiedPublicKey checkSelfSigned(CertifiedPublicKey certificate, int version) throws Exception
    {
        assertEquals(new DistinguishedName("CN=Test"), certificate.getIssuer());
        assertEquals(new DistinguishedName("CN=Test"), certificate.getSubject());
        assertEquals(certificate.getSubject(), certificate.getIssuer());
        assertTrue(certificate.isSignedBy(rsaPublicKey), "Signature should match used private key.");
        assertTrue(certificate.isSignedBy(certificate.getPublicKeyParameters()),
            "Signature should match subject public key.");

        assertInstanceOf(X509CertifiedPublicKey.class, certificate);
        X509CertifiedPublicKey x509cert = (X509CertifiedPublicKey) certificate;

        //dumpCert(certificate);

        assertEquals(version, x509cert.getVersionNumber());

        Date yesterday = new Date(System.currentTimeMillis() - 86400000);
        Date inMoreThan500Days = new Date(System.currentTimeMillis() + 43286400000L);

        assertTrue(x509cert.isValidOn(new Date()), "Certificate should be valid today.");
        assertThat(x509cert.getNotBefore().getTime(), greaterThan(yesterday.getTime()));
        assertThat(x509cert.getNotAfter().getTime(), lessThan(inMoreThan500Days.getTime()));
        assertFalse(x509cert.isValidOn(yesterday), "Certificate should not be valid yesterday.");
        assertFalse(x509cert.isValidOn(inMoreThan500Days), "Certificate should not be valid in more than 500 days.");
        assertTrue(x509cert.isRootCA());

        return x509cert;
    }

    private X509CertifiedPublicKey checkRootSigned(CertifiedPublicKey certificate, int version) throws Exception
    {
        assertEquals(new DistinguishedName("CN=Test CA"), certificate.getIssuer());
        assertEquals(new DistinguishedName("CN=Test End Entity"), certificate.getSubject());
        assertTrue(certificate.isSignedBy(rsaPublicKey), "Signature should match used private key.");

        assertInstanceOf(X509CertifiedPublicKey.class, certificate);
        X509CertifiedPublicKey x509cert = (X509CertifiedPublicKey) certificate;

        //dumpCert(certificate);

        assertEquals(version, x509cert.getVersionNumber());

        Date yesterday = new Date(System.currentTimeMillis() - 86400000);
        Date inMoreThan500Days = new Date(System.currentTimeMillis() + 43286400000L);

        assertTrue(x509cert.isValidOn(new Date()), "Certificate should be valid today.");
        assertThat(x509cert.getNotBefore().getTime(), greaterThan(yesterday.getTime()));
        assertThat(x509cert.getNotAfter().getTime(), lessThan(inMoreThan500Days.getTime()));
        assertFalse(x509cert.isValidOn(yesterday), "Certificate should not be valid yesterday.");
        assertFalse(x509cert.isValidOn(inMoreThan500Days), "Certificate should not be valid in more than 500 days.");
        assertFalse(x509cert.isRootCA());

        return x509cert;
    }

    @Test
    void generateSelfSignedCertificateVersion1() throws Exception
    {
        CertifiedPublicKey certificate =
            this.factory.getInstance(this.signerFactory.getInstance(true, rsaPrivateKey),
                    new X509CertificateGenerationParameters())
                .generate(new DistinguishedName("CN=Test"), rsaPublicKey,
                    new X509CertificateParameters());

        checkSelfSigned(certificate, 1);
    }

    @Test
    void generateSelfSignedCertificateVersion3WithoutExtension() throws Exception
    {
        CertifiedPublicKey certificate =
            this.factory.getInstance(this.signerFactory.getInstance(true, rsaPrivateKey),
                    new X509CertificateGenerationParameters(null))
                .generate(new DistinguishedName("CN=Test"), rsaPublicKey,
                    new X509CertificateParameters());

        checkSelfSigned(certificate, 3);
    }

    @Test
    void generateSelfSignedCertificateVersion3RootCa() throws Exception
    {
        CertifiedPublicKey certificate =
            this.factory.getInstance(this.signerFactory.getInstance(true, rsaPrivateKey),
                    new X509CertificateGenerationParameters(
                        this.builder.addBasicConstraints(true)
                            .addKeyUsage(true, EnumSet.of(KeyUsage.keyCertSign,
                                KeyUsage.cRLSign))
                            .build()))
                .generate(new DistinguishedName("CN=Test"), rsaPublicKey,
                    new X509CertificateParameters());

        X509CertifiedPublicKey cert = checkSelfSigned(certificate, 3);

        assertTrue(cert.getExtensions().isCritical(X509Extensions.BASIC_CONSTRAINTS_OID),
            "Basic constraints should be critical.");
        assertTrue(cert.getExtensions().hasCertificateAuthorityBasicConstraints(),
            "Basic constraints should be set to CA.");
        assertTrue(cert.getExtensions().isCritical(KeyUsage.OID), "KeyUsage extension should be critical.");
        assertEquals(EnumSet.of(KeyUsage.keyCertSign, KeyUsage.cRLSign), cert.getExtensions().getKeyUsage());
        assertNotNull(cert.getAuthorityKeyIdentifier());
        assertArrayEquals(cert.getSubjectKeyIdentifier(), cert.getAuthorityKeyIdentifier());
    }

    @Test
    void generateEndEntitySignedCertificateVersion1() throws Exception
    {
        CertifiedPublicKey caCertificate =
            this.factory.getInstance(this.signerFactory.getInstance(true, rsaPrivateKey),
                    new X509CertificateGenerationParameters(
                        //<validity in days>
                    ))
                .generate(new DistinguishedName("CN=Test CA"), rsaPublicKey,
                    new X509CertificateParameters());

        //dumpCert(caCertificate);

        CertifiedPublicKey certificate =
            this.factory.getInstance(
                CertifyingSigner.getInstance(true, new CertifiedKeyPair(rsaPrivateKey, caCertificate),
                    this.signerFactory),
                new X509CertificateGenerationParameters(
                    //<validity in days>
                )
            ).generate(new DistinguishedName("CN=Test End Entity"), dsaPublicKey,
                new X509CertificateParameters());

        checkRootSigned(certificate, 1);
    }

    @Test
    void generateEndEntitySignedCertificateVersion3() throws Exception
    {
        CertifiedPublicKey caCertificate =
            this.factory.getInstance(this.signerFactory.getInstance(true, rsaPrivateKey),
                    new X509CertificateGenerationParameters(
                        this.builder.addBasicConstraints(true)
                            .addKeyUsage(true, EnumSet.of(KeyUsage.keyCertSign,
                                KeyUsage.cRLSign))
                            .build()))
                .generate(new DistinguishedName("CN=Test CA"), rsaPublicKey,
                    new X509CertificateParameters());

        this.builder = this.componentManager.getInstance(X509ExtensionBuilder.class);

        CertificateGenerator generator = this.factory.getInstance(
            CertifyingSigner.getInstance(true, new CertifiedKeyPair(rsaPrivateKey, caCertificate), this.signerFactory),
            new X509CertificateGenerationParameters(
                this.builder.addKeyUsage(EnumSet.of(KeyUsage.digitalSignature,
                        KeyUsage.dataEncipherment))
                    .addExtendedKeyUsage(false,
                        new ExtendedKeyUsages(new String[] { ExtendedKeyUsages.EMAIL_PROTECTION }))
                    .build()));

        this.builder = this.componentManager.getInstance(X509ExtensionBuilder.class);

        CertifiedPublicKey certificate =
            generator.generate(new DistinguishedName("CN=Test End Entity"), dsaPublicKey,
                new X509CertificateParameters(
                    this.builder.addSubjectAltName(false,
                            new X509GeneralName[] {
                                new X509Rfc822Name("test@example.com"),
                                new X509Rfc822Name(new InternetAddress("test@test.com")),
                                new X509DnsName("example.com"),
                                new X509DirectoryName("CN=Test"),
                                new X509IpAddress("192.168.1.1"),
                                new X509IpAddress("192.168.2.0/24"),
                                new X509IpAddress("192.168.3.0/255.255.255.0"),
                                new X509IpAddress(InetAddress.getByName("192.168.4.1")),
                                new X509IpAddress(InetAddress.getByName("192.168.5.0"),
                                    InetAddress.getByName("255.255.255.0")),
                                new X509IpAddress("2001:db8:0:85a3::ac1f:8001"),
                                new X509IpAddress("2001:db8:1f89::/48"),
                                new X509IpAddress(InetAddress.getByName("2001:db8:0:85a3::ac1f:8001")),
                                new X509IpAddress(InetAddress.getByName("2001:db8:1f89::"),
                                    InetAddress.getByName("ffff:ffff:ffff::")),
                                new X509URI("http://xwiki.org"),
                                new X509URI(new URL("http://myxwiki.org"))
                            })
                        .build()
                ));

        X509CertifiedPublicKey cert = checkRootSigned(certificate, 3);

        assertArrayEquals(new String[] { "2.5.29.35", "2.5.29.14", "2.5.29.15", "2.5.29.37", "2.5.29.17" },
            cert.getExtensions().getExtensionOID());
        assertArrayEquals(new String[] { "2.5.29.15" }, cert.getExtensions().getCriticalExtensionOID());
        assertArrayEquals(new String[] { "2.5.29.35", "2.5.29.14", "2.5.29.37", "2.5.29.17" },
            cert.getExtensions().getNonCriticalExtensionOID());

        assertTrue(cert.getExtensions().isCritical(KeyUsage.OID), "KeyUsage extension should be critical.");
        assertEquals(EnumSet.of(KeyUsage.digitalSignature,
            KeyUsage.dataEncipherment), cert.getExtensions().getKeyUsage());
        assertFalse(cert.getExtensions().isCritical(ExtendedKeyUsages.OID),
            "ExtendedKeyUsage extension should be non critical.");
        assertArrayEquals(new String[] { ExtendedKeyUsages.EMAIL_PROTECTION },
            cert.getExtensions().getExtendedKeyUsage().getAll().toArray(new String[0]));
        assertTrue(cert.getExtensions().getExtendedKeyUsage().hasUsage(ExtendedKeyUsages.EMAIL_PROTECTION),
            "Email data protection extended usage should be set.");

        List<X509GeneralName> names = cert.getExtensions().getSubjectAltName();

        assertEquals(15, names.size());
        for (X509GeneralName name : names) {
            switch (name) {
                case X509Rfc822Name x509Rfc822Name -> {
                    assertThat(((X509StringGeneralName) name).getName(),
                        anyOf(equalTo("test@example.com"), equalTo("test@test.com")));
                    assertThat(x509Rfc822Name.getAddress(), anyOf(equalTo(new InternetAddress("test@example.com")),
                        equalTo(new InternetAddress("test@test.com"))));
                }
                case X509DnsName x509DnsName -> {
                    assertThat(((X509StringGeneralName) name).getName(), equalTo("example.com"));
                    assertThat(x509DnsName.getDomain(), equalTo("example.com"));
                }
                case X509DirectoryName ignored ->
                    assertThat(((X509StringGeneralName) name).getName(), equalTo("CN=Test"));
                case X509URI x509URI -> {
                    assertThat(((X509StringGeneralName) name).getName(), anyOf(equalTo("http://xwiki.org"),
                        equalTo("http://myxwiki.org")));
                    assertThat(x509URI.getURI(), anyOf(equalTo(new URI("http://xwiki.org")),
                        equalTo(new URI("http://myxwiki.org"))));
                    assertThat(x509URI.getURL(), anyOf(equalTo(new URL("http://xwiki.org")),
                        equalTo(new URL("http://myxwiki.org"))));
                }
                case X509IpAddress ignored1 -> assertTrue(IPAddress.isValid(((X509StringGeneralName) name).getName())
                        || IPAddress.isValidWithNetMask(((X509StringGeneralName) name).getName()),
                    "Invalid IP address: " + ((X509StringGeneralName) name).getName());
                case null, default -> fail("Unexpected SubjectAltName type.");
            }
        }
    }

    @Test
    void generateIntermediateCertificateVersion3() throws Exception
    {
        CertifiedPublicKey caCertificate =
            this.factory.getInstance(this.signerFactory.getInstance(true, rsaPrivateKey),
                    new X509CertificateGenerationParameters(
                        //<validity in days>,
                        this.builder.addBasicConstraints(true)
                            .addKeyUsage(true, EnumSet.of(KeyUsage.keyCertSign,
                                KeyUsage.cRLSign))
                            .build()))
                .generate(new DistinguishedName("CN=Test CA"), rsaPublicKey,
                    new X509CertificateParameters());

        //dumpCert(caCertificate);

        X509CertifiedPublicKey caKey = (X509CertifiedPublicKey) caCertificate;

        this.builder = this.componentManager.getInstance(X509ExtensionBuilder.class);

        CertificateGenerator generator = this.factory.getInstance(
            CertifyingSigner.getInstance(true, new CertifiedKeyPair(rsaPrivateKey, caCertificate), this.signerFactory),
            new X509CertificateGenerationParameters(
                //<validity in days>,
                this.builder.addBasicConstraints(0)
                    .addKeyUsage(EnumSet.of(KeyUsage.keyCertSign,
                        KeyUsage.cRLSign))
                    .build()));

        CertifiedPublicKey interCAcert =
            generator.generate(new DistinguishedName("CN=Test Intermediate CA"), interCaDsaPublicKey,
                new X509CertificateParameters());

        //dumpCert(interCAcert);

        assertTrue(interCAcert.isSignedBy(rsaPublicKey), "Signature should match Root CA key.");
        assertEquals(caCertificate.getSubject(), interCAcert.getIssuer());
        assertEquals(new DistinguishedName("CN=Test Intermediate CA"), interCAcert.getSubject());
        assertInstanceOf(X509CertifiedPublicKey.class, interCAcert);

        X509CertifiedPublicKey interCaKey = (X509CertifiedPublicKey) interCAcert;

        assertEquals(3, interCaKey.getVersionNumber());
        assertTrue(interCaKey.getExtensions().isCritical(X509Extensions.BASIC_CONSTRAINTS_OID),
            "Basic constraints should be critical.");
        assertTrue(interCaKey.getExtensions().hasCertificateAuthorityBasicConstraints(),
            "Basic constraints should be set to CA.");
        assertEquals(0, interCaKey.getExtensions().getBasicConstraintsPathLen());
        assertTrue(interCaKey.getExtensions().isCritical(KeyUsage.OID), "KeyUsage extension should be critical.");
        assertEquals(EnumSet.of(KeyUsage.keyCertSign, KeyUsage.cRLSign), interCaKey.getExtensions().getKeyUsage());
        assertArrayEquals(caKey.getSubjectKeyIdentifier(), interCaKey.getAuthorityKeyIdentifier());

        this.builder = this.componentManager.getInstance(X509ExtensionBuilder.class);

        generator = this.factory.getInstance(
            CertifyingSigner.getInstance(true, new CertifiedKeyPair(interCaDsaPrivateKey, interCAcert),
                this.componentManager.getInstance(SignerFactory.class, "DSAwithSHA1")),
            new X509CertificateGenerationParameters(
                //<validity in days>,
                this.builder.addKeyUsage(EnumSet.of(KeyUsage.digitalSignature,
                        KeyUsage.dataEncipherment))
                    .addExtendedKeyUsage(false,
                        new ExtendedKeyUsages(new String[] { ExtendedKeyUsages.EMAIL_PROTECTION }))
                    .build()));

        this.builder = this.componentManager.getInstance(X509ExtensionBuilder.class);

        CertifiedPublicKey certificate =
            generator.generate(new DistinguishedName("CN=Test End Entity"), dsaPublicKey,
                new X509CertificateParameters(
                    this.builder.addSubjectAltName(false,
                            new X509GeneralName[] {
                                new X509Rfc822Name("test@example.com")
                            })
                        .build()
                ));

        //dumpCert(certificate);

        assertTrue(certificate.isSignedBy(interCaDsaPublicKey), "Signature should match intermediate CA key.");
        assertEquals(interCAcert.getSubject(), certificate.getIssuer());
        assertEquals(new DistinguishedName("CN=Test End Entity"), certificate.getSubject());
        assertInstanceOf(X509CertifiedPublicKey.class, certificate);

        X509CertifiedPublicKey key = (X509CertifiedPublicKey) certificate;
        assertArrayEquals(interCaKey.getSubjectKeyIdentifier(), key.getAuthorityKeyIdentifier());
    }
}
