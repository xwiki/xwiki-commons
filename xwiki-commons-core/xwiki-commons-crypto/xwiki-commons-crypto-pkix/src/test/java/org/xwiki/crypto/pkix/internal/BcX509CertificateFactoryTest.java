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

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.AbstractPKIXTest;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcDSAKeyFactory;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA1DigestFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.extension.ExtendedKeyUsages;
import org.xwiki.crypto.pkix.params.x509certificate.extension.KeyUsage;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509Extensions;
import org.xwiki.crypto.signer.internal.factory.BcDSAwithSHA1SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSHA1withRsaSignerFactory;
import org.xwiki.crypto.signer.internal.factory.DefaultSignerFactory;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
@ComponentList({ Base64BinaryStringEncoder.class, BcRSAKeyFactory.class, BcDSAKeyFactory.class,
    BcSHA1DigestFactory.class, BcSHA1withRsaSignerFactory.class,
    DefaultSignerFactory.class, BcDSAwithSHA1SignerFactory.class })
public class BcX509CertificateFactoryTest extends AbstractPKIXTest
{
    @InjectMockComponents
    private BcX509CertificateFactory factory;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private static byte[] v1CaCert;

    private static byte[] v1Cert;

    private static byte[] v3CaCert;

    private static byte[] v3InterCaCert;

    private static byte[] v3Cert;

    @BeforeEach
    public void setupTest() throws Exception
    {
        // Decode keys once for all tests.
        if (v1CaCert == null) {
            BinaryStringEncoder base64encoder = componentManager.getInstance(BinaryStringEncoder.class, "Base64");
            v1CaCert = base64encoder.decode(V1_CA_CERT);
            v1Cert = base64encoder.decode(V1_CERT);
            v3CaCert = base64encoder.decode(V3_CA_CERT);
            v3InterCaCert = base64encoder.decode(V3_ITERCA_CERT);
            v3Cert = base64encoder.decode(V3_CERT);
        }
    }

    @Test
    public void v1CaCert() throws Exception
    {
        CertifiedPublicKey certificate = factory.decode(v1CaCert);

        assertTrue(certificate.isSignedBy(certificate.getPublicKeyParameters()), "CA should verify itself.");

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(1));
        assertThat(cert.isRootCA(), equalTo(true));
    }

    @Test
    public void v1Cert() throws Exception
    {
        CertifiedPublicKey caCert = factory.decode(v1CaCert);
        CertifiedPublicKey certificate = factory.decode(v1Cert);

        assertTrue(certificate.isSignedBy(caCert.getPublicKeyParameters()),
            "End certificate should be verified by CA.");

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(1));
    }

    @Test
    public void v3CaCert() throws Exception
    {
        CertifiedPublicKey certificate = factory.decode(v3CaCert);

        assertTrue(certificate.isSignedBy(certificate.getPublicKeyParameters()), "CA should verify itself.");

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(3));

        assertTrue(cert.getExtensions().isCritical(X509Extensions.BASIC_CONSTRAINTS_OID),
            "Basic constraints should be critical.");
        assertTrue(cert.getExtensions().hasCertificateAuthorityBasicConstraints(),
            "Basic constraints should be set to CA.");
        assertTrue(cert.getExtensions().isCritical(KeyUsage.OID),
            "KeyUsage extension should be critical.");
        assertThat(cert.getExtensions().getKeyUsage(), equalTo(EnumSet.of(KeyUsage.keyCertSign,
            KeyUsage.cRLSign)));
        assertThat(cert.getAuthorityKeyIdentifier(), notNullValue());
        assertThat(cert.getAuthorityKeyIdentifier(),
            equalTo(cert.getSubjectKeyIdentifier()));
        assertThat(cert.isRootCA(), equalTo(true));
    }

    @Test
    public void v3InterCACert() throws Exception
    {
        CertifiedPublicKey caCert = factory.decode(v3CaCert);
        CertifiedPublicKey interCaCert = factory.decode(v3InterCaCert);

        assertTrue(interCaCert.isSignedBy(caCert.getPublicKeyParameters()),
            "Intermediate CA certificate should be verified by CA.");

        assertThat(interCaCert, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) interCaCert;
        assertThat(cert.getVersionNumber(), equalTo(3));

        assertTrue(cert.getExtensions().isCritical(X509Extensions.BASIC_CONSTRAINTS_OID),
            "Basic constraints should be critical.");
        assertTrue(cert.getExtensions().hasCertificateAuthorityBasicConstraints(),
            "Basic constraints should be set to CA.");
        assertThat(cert.getExtensions().getBasicConstraintsPathLen(), equalTo(0));
        assertTrue(cert.getExtensions().isCritical(KeyUsage.OID), "KeyUsage extension should be critical.");
        assertThat(cert.getExtensions().getKeyUsage(), equalTo(EnumSet.of(KeyUsage.keyCertSign,
            KeyUsage.cRLSign)));

        assertThat(cert.getAuthorityKeyIdentifier(),
            equalTo(((X509CertifiedPublicKey) caCert).getSubjectKeyIdentifier()));
        assertThat(cert.isRootCA(), equalTo(false));
    }

    @Test
    public void v3Cert() throws Exception
    {
        CertifiedPublicKey interCaCert = factory.decode(v3InterCaCert);
        CertifiedPublicKey certificate = factory.decode(v3Cert);

        assertTrue(certificate.isSignedBy(interCaCert.getPublicKeyParameters()),
            "End certificate should be verified by CA.");

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(3));

        assertTrue(cert.getExtensions().isCritical(KeyUsage.OID), "KeyUsage extension should be critical.");
        assertThat(cert.getExtensions().getKeyUsage(), equalTo(EnumSet.of(KeyUsage.digitalSignature,
            KeyUsage.dataEncipherment)));
        assertFalse(cert.getExtensions().isCritical(ExtendedKeyUsages.OID),
            "ExtendedKeyUsage extension should be non critical.");
        assertThat(cert.getExtensions().getExtendedKeyUsage().getAll().toArray(new String[0]), equalTo(
            new String[]{ ExtendedKeyUsages.EMAIL_PROTECTION }));
        assertTrue(cert.getExtensions().getExtendedKeyUsage().hasUsage(ExtendedKeyUsages.EMAIL_PROTECTION),
            "Email data protection extended usage should be set.");

        assertThat(cert.getAuthorityKeyIdentifier(),
            equalTo(((X509CertifiedPublicKey) interCaCert).getSubjectKeyIdentifier()));
        assertThat(cert.isRootCA(), equalTo(false));
    }
}
