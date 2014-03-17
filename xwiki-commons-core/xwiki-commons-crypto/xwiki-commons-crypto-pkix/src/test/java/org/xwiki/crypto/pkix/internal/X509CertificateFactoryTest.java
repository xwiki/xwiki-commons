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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcDSAKeyFactory;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA1DigestFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.extension.ExtendedKeyUsages;
import org.xwiki.crypto.pkix.params.x509certificate.extension.KeyUsage;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509Extensions;
import org.xwiki.crypto.signer.internal.factory.BcRsaSsaPssSignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSHA1withRsaSignerFactory;
import org.xwiki.crypto.signer.internal.factory.DefaultSignerFactory;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ComponentList({Base64BinaryStringEncoder.class, BcRSAKeyFactory.class, BcDSAKeyFactory.class,
    BcSHA1DigestFactory.class, BcSHA1withRsaSignerFactory.class, BcRsaSsaPssSignerFactory.class,
    DefaultSignerFactory.class})
public class X509CertificateFactoryTest
{
    @Rule
    public final MockitoComponentMockingRule<CertificateFactory> mocker =
        new MockitoComponentMockingRule<CertificateFactory>(X509CertificateFactory.class);

    private static final String V1_CA_CERT = "MIICpzCCAY8CEBySdlSTKgwuylJNlQxTMNIwDQYJKoZIhvcNAQEFBQAwEjEQMA4G"
        + "A1UEAwwHVGVzdCBDQTAeFw0xNDAyMDMxMTAwMDBaFw0xNTA2MTgxMDAwMDBaMBIx"
        + "EDAOBgNVBAMMB1Rlc3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB"
        + "AQDCmjim/3likJ4VF564UyygqPjIX/z090AImLl0fDLUkIyCVTSd18wJ3axr1qjL"
        + "tSgNPWet0puSxOFH0AzFKRCJOjUkQRU8iAkz64MLAf9xrx4nBECciqeB941s01kL"
        + "tG8C/UqC3O9SwHSdhtUpUU8V/91SiD09yNJsnODi3WqM3oLg1QYzKhoaD2mVo2xJ"
        + "LQ/QXqr2XIc5i2Mlpfq6S5JNbFD/I+UFhBUlBNuDOEV7ttIt2eFMEUsfkCestGo0"
        + "YoQYOpTLPcPGRS7MnSY1CLWGUYqaMSnes0nS8ke2PPD4Q0suAZz4msnhNufanscs"
        + "tM8tcNtsZF6hj0JvbZok89szAgMBAAEwDQYJKoZIhvcNAQEFBQADggEBAB2M29kY"
        + "IwXRNpqM/CnRhc8MFCKO5XDQI35CrECFFXOyfGJsWb2W/O2FQFpi3bNHdKgt5BN6"
        + "ZVjTdr8veHPr5bQ9IrZgoAAL41xwMThQjGDvomiZri0WtulP8VfX0axFGhdde4mg"
        + "iYpYyCLYvTg5Mp8FuEW9XPtgJSumKYTNhk0prKyN7UfLxrhdI1sG3Y1/2/a8Bz3m"
        + "xzPB6DMYMNPD1rB6R/mU+QUBPCPlUSCm+zQf+gTL0Uu2r4jlUiHSVywAPcEWfGFP"
        + "/qb05hjvU8mYDbwPd3kX/mKHBUYKVqGemz9UPJqF0Yg9y7qtlivdiv7o7VaoykdK"
        + "mDzNKbH1jnI/azc=";

    private static final String V1_CERT = "MIIDRDCCAiwCEGTodh45ecrZyNhs/OCWTMQwDQYJKoZIhvcNAQEFBQAwEjEQMA4G"
        + "A1UEAwwHVGVzdCBDQTAeFw0xNDAzMTcxMTAwMDBaFw0xNTA3MzAxMDAwMDBaMBox"
        + "GDAWBgNVBAMMD1Rlc3QgRW5kIEVudGl0eTCCAbcwggEsBgcqhkjOOAQBMIIBHwKB"
        + "gQDUPTmtY/bFgIXVzcqs/By/2wP3ndlawcOkz5IDMldVKYsefEUozhzEDa4Hpkhp"
        + "G/NHUXC6lQ2GyNzm5yCYfN2ces3mk6b1KIRi8k2tBe1GNeS5Gyacs/FhSWlcpB/O"
        + "Z892aOjHfWeF/ecrxdfofamLmpI1nB1t9fqTeLt3CTYORQIVAI/fOnFRvTYFoFsx"
        + "VUi2wfaRD2xVAoGBAJkK2RR9zF+Qi1/+bw78ck3nkT2Ry1nNIpNxsKyCRMblazpL"
        + "hSJ6cdGlW7iU8axJ2Nn7nP/gco/xeegwzt9fqj7u2cW0r7w99zjpPHhRH5/h4mt0"
        + "JgPnRq4VKqDKXw3+PxgYCYZ8kLRczHXR5mAdEJ0vcl9Z+gzlo/wkl9D765TGA4GE"
        + "AAKBgCb57k5vKCP0USNrYmR7T5L0kGgNxT+D0bFces9aCBoPTgMa7Ukv2okGvh1h"
        + "YO34x5TCd9FYdMqre4oI+9I2ixsg769zDw8ojvvu9iIhlJbnOsh4OpvXEOd3nibf"
        + "YuOCyX0zsNUt2DFNUk+2paJEpyUUyAqeVgctJtbHMIXrk6NBMA0GCSqGSIb3DQEB"
        + "BQUAA4IBAQC8bGVK2vFXZGGVOSxvvXXkDNFL9H8cW4i4efrBJ8xGVgtRRm211Mkn"
        + "uziXcHi2pAPdDnmdrgz0xnj+RxTJDLltYbRgh6R7QCywKH+Wqsqq6D+p/D9wm5FI"
        + "BYSjS3UMjB9ALSKC4x09Plp+6ooU9tpKGHlmkNCCH9tkkK2p2GcjbOv9o+tM5BWa"
        + "nRyNfdeKrfWSPVxchHovIEG2ytfBVsd8UNMgIIOM8QBFHqzbGjT2jTcIWoAlwaRL"
        + "wJLu0jsLxRl+pRqced1xaz42KYHwZceBIVjg1SP6goowbwfgaQKfteGcpZk27KME"
        + "x3zaPAb+IcpeOs3gOMfdiAk77nlo3mxo";

    private static final String V3_CA_CERT = "MIIDETCCAfmgAwIBAgIQcGoZ+d8mnKUSOv270U6ZrTANBgkqhkiG9w0BAQUFADAS"
        + "MRAwDgYDVQQDDAdUZXN0IENBMB4XDTE0MDIwMzExMDAwMFoXDTE1MDYxODEwMDAw"
        + "MFowEjEQMA4GA1UEAwwHVGVzdCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC"
        + "AQoCggEBAMKaOKb/eWKQnhUXnrhTLKCo+Mhf/PT3QAiYuXR8MtSQjIJVNJ3XzAnd"
        + "rGvWqMu1KA09Z63Sm5LE4UfQDMUpEIk6NSRBFTyICTPrgwsB/3GvHicEQJyKp4H3"
        + "jWzTWQu0bwL9SoLc71LAdJ2G1SlRTxX/3VKIPT3I0myc4OLdaozeguDVBjMqGhoP"
        + "aZWjbEktD9BeqvZchzmLYyWl+rpLkk1sUP8j5QWEFSUE24M4RXu20i3Z4UwRSx+Q"
        + "J6y0ajRihBg6lMs9w8ZFLsydJjUItYZRipoxKd6zSdLyR7Y88PhDSy4BnPiayeE2"
        + "59qexyy0zy1w22xkXqGPQm9tmiTz2zMCAwEAAaNjMGEwHwYDVR0jBBgwFoAUiBZ2"
        + "8+5dImSVCTC89MiGmBPOFWEwHQYDVR0OBBYEFIgWdvPuXSJklQkwvPTIhpgTzhVh"
        + "MA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBBQUA"
        + "A4IBAQCou90IDsVku2cVEUguedn1J5hFYlYXZBhK4/6QNwS8cfPetqJJywwRClnH"
        + "mJWHgc0alswX5MJ184z5IM3G9NTmqzHjplpNiw6HZ1N0/8cCYIIng3kSrK4Y6w9F"
        + "FVH2HVYy+3EspWHcwPdSr2Kcs4dFD/w4J4cjcp8UILSQyTlKpv+Dh8XHKZFv6f1U"
        + "vy+gIYdrxnmApcOcnX4vAPVwbVMd+he9HpjHxf4zsdXrtSdLM7RP6VeeFouxoox/"
        + "GZKuV59XK0DWMcGya8JAScedfCEQL6AbMs/CZBL4Hx/Y1/bnsdncmclcWPgmp0KD"
        + "i5ZotvGKE+kkN1srsfPZLx8FrU9v";

    private static final String V3_CERT = "MIIE6DCCA9CgAwIBAgIQFCr1Ht5IaKU11uZMok9AYDANBgkqhkiG9w0BAQUFADAS"
        + "MRAwDgYDVQQDDAdUZXN0IENBMB4XDTE0MDMxNzExMDAwMFoXDTE1MDczMDEwMDAw"
        + "MFowGjEYMBYGA1UEAwwPVGVzdCBFbmQgRW50aXR5MIIBtzCCASwGByqGSM44BAEw"
        + "ggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/ed2VrBw6TPkgMyV1Upix58RSjOHMQN"
        + "rgemSGkb80dRcLqVDYbI3ObnIJh83Zx6zeaTpvUohGLyTa0F7UY15LkbJpyz8WFJ"
        + "aVykH85nz3Zo6Md9Z4X95yvF1+h9qYuakjWcHW31+pN4u3cJNg5FAhUAj986cVG9"
        + "NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3MX5CLX/5vDvxyTeeRPZHLWc0ik3GwrIJE"
        + "xuVrOkuFInpx0aVbuJTxrEnY2fuc/+Byj/F56DDO31+qPu7ZxbSvvD33OOk8eFEf"
        + "n+Hia3QmA+dGrhUqoMpfDf4/GBgJhnyQtFzMddHmYB0QnS9yX1n6DOWj/CSX0Pvr"
        + "lMYDgYQAAoGAJvnuTm8oI/RRI2tiZHtPkvSQaA3FP4PRsVx6z1oIGg9OAxrtSS/a"
        + "iQa+HWFg7fjHlMJ30Vh0yqt7igj70jaLGyDvr3MPDyiO++72IiGUluc6yHg6m9cQ"
        + "53eeJt9i44LJfTOw1S3YMU1ST7alokSnJRTICp5WBy0m1scwheuTo0GjggGbMIIB"
        + "lzBJBgNVHSMEQjBAgBSIFnbz7l0iZJUJMLz0yIaYE84VYaEWpBQwEjEQMA4GA1UE"
        + "AwwHVGVzdCBDQYIQAng5x/oSenY8eZP1JohJcTAdBgNVHQ4EFgQUnSLsYFixuOTJ"
        + "NNUR2iEVLtkWnR4wDgYDVR0PAQH/BAQDAgSQMBMGA1UdJQQMMAoGCCsGAQUFBwME"
        + "MIIBBAYDVR0RBIH8MIH5gRB0ZXN0QGV4YW1wbGUuY29tgQ10ZXN0QHRlc3QuY29t"
        + "ggtleGFtcGxlLmNvbaQRMA8xDTALBgNVBAMMBFRlc3SHBMCoAQGHCMCoAgD///8A"
        + "hwjAqAMA////AIcEwKgEAYcIwKgFAP///wCHECABDbgAAIWjAAAAAKwfgAGHICAB"
        + "DbgfiQAAAAAAAAAAAAD///////8AAAAAAAAAAAAAhxAgAQ24AACFowAAAACsH4AB"
        + "hyAgAQ24H4kAAAAAAAAAAAAA////////AAAAAAAAAAAAAIYQaHR0cDovL3h3aWtp"
        + "Lm9yZ4YSaHR0cDovL215eHdpa2kub3JnMA0GCSqGSIb3DQEBBQUAA4IBAQCYWr/B"
        + "zRQc598MPGWrqRvhos23RBlF8CUIPWGSB0o67gGcKbReLfEIEtJCRZq4sLMeDHMi"
        + "thUx1gkj7gtBkIV9ZE7Tr2h93WkEcr+JeK+9fIvX5erPvdH6MgmIOI3etoBvSmPz"
        + "pys0N/nK6M2+YLCJM7ZntEkZ5INlhyEIKDxDb4JHnvhTA4unF0bN/rrdnIOzBYEc"
        + "fmho35HLN3x3wHJGnAwnw/fbApyUcuqUbkxjX7JMZ7bvuPDZBxoVZj0wJNu9EXbV"
        + "v7jGvdkFZjAy09wXTX+V1veJNu/LPtl6I6MUM4n+CJKrucP/FJ/MQUMV2ufH4crv"
        + "DnmN4S10tHJsLSoa";

    private CertificateFactory factory;
    private static byte[] v1CaCert;
    private static byte[] v1Cert;
    private static byte[] v3CaCert;
    private static byte[] v3Cert;

    public void setupTest(MockitoComponentMockingRule<CertificateFactory> mocker) throws Exception
    {
        // Decode keys once for all tests.
        if (v1CaCert == null) {
            BinaryStringEncoder base64encoder = mocker.getInstance(BinaryStringEncoder.class, "Base64");
            v1CaCert = base64encoder.decode(V1_CA_CERT);
            v1Cert = base64encoder.decode(V1_CERT);
            v3CaCert = base64encoder.decode(V3_CA_CERT);
            v3Cert = base64encoder.decode(V3_CERT);
        }
    }

    @Before
    public void configure() throws Exception
    {
        factory = mocker.getComponentUnderTest();
        setupTest(mocker);
    }

    @Test
    public void testV1CaCert() throws Exception
    {
        CertifiedPublicKey certificate = factory.decode(v1CaCert);

        assertTrue("CA should verify itself.", certificate.isSignedBy(certificate.getPublicKeyParameters()));

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(1));
    }

    @Test
    public void testV1Cert() throws Exception
    {
        CertifiedPublicKey caCert = factory.decode(v1CaCert);
        CertifiedPublicKey certificate = factory.decode(v1Cert);

        assertTrue("End certificate should be verified by CA.", certificate.isSignedBy(caCert.getPublicKeyParameters()));

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(1));
    }

    @Test
    public void testV3CaCert() throws Exception
    {
        CertifiedPublicKey certificate = factory.decode(v3CaCert);

        assertTrue("CA should verify itself.", certificate.isSignedBy(certificate.getPublicKeyParameters()));

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(3));

        assertTrue("Basic constraints should be critical.", cert.getExtensions().isCritical(X509Extensions.BASIC_CONSTRAINTS_OID));
        assertTrue("Basic constraints should be set to CA.", cert.getExtensions().hasCertificateAuthorityBasicConstraints());
        assertTrue("KeyUsage extension should be critical.", cert.getExtensions().isCritical(KeyUsage.OID));
        assertThat(cert.getExtensions().getKeyUsage(), equalTo(EnumSet.of(KeyUsage.keyCertSign,
            KeyUsage.cRLSign)));
        assertThat(cert.getExtensions().getAuthorityKeyIdentifier(), notNullValue());
        assertThat(cert.getExtensions().getAuthorityKeyIdentifier(),
            equalTo(cert.getExtensions().getSubjectKeyIdentifier()));

    }

    @Test
    public void testV3Cert() throws Exception
    {
        CertifiedPublicKey caCert = factory.decode(v3CaCert);
        CertifiedPublicKey certificate = factory.decode(v3Cert);

        assertTrue("End certificate should be verified by CA.", certificate.isSignedBy(caCert.getPublicKeyParameters()));

        assertThat(certificate, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) certificate;
        assertThat(cert.getVersionNumber(), equalTo(3));

        assertTrue("KeyUsage extension should be critical.", cert.getExtensions().isCritical(KeyUsage.OID));
        assertThat(cert.getExtensions().getKeyUsage(), equalTo(EnumSet.of(KeyUsage.digitalSignature,
            KeyUsage.dataEncipherment)));
        assertFalse("ExtendedKeyUsage extension should be non critical.",
            cert.getExtensions().isCritical(ExtendedKeyUsages.OID));
        assertThat(cert.getExtensions().getExtendedKeyUsage().getAll().toArray(new String[0]), equalTo(
            new String[]{ExtendedKeyUsages.EMAIL_PROTECTION}));
        assertTrue("Email data protection extended usage should be set.",
            cert.getExtensions().getExtendedKeyUsage().hasUsage(ExtendedKeyUsages.EMAIL_PROTECTION));
    }

}
