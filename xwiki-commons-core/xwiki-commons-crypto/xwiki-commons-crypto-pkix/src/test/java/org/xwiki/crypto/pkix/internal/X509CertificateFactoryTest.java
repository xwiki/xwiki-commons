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
import org.xwiki.crypto.signer.internal.factory.BcDSAwithSHA1SignerFactory;
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
    BcSHA1DigestFactory.class, BcSHA1withRsaSignerFactory.class,
    DefaultSignerFactory.class, BcDSAwithSHA1SignerFactory.class})
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

    private static final String V3_CA_CERT = "MIIDEjCCAfqgAwIBAgIRANKASb61Xw7K0Oz9c034VPswDQYJKoZIhvcNAQEFBQAw"
        + "EjEQMA4GA1UEAwwHVGVzdCBDQTAeFw0xNDAzMTcyMzAwMDBaFw0xNTA3MzAyMjAw"
        + "MDBaMBIxEDAOBgNVBAMMB1Rlc3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw"
        + "ggEKAoIBAQDCmjim/3likJ4VF564UyygqPjIX/z090AImLl0fDLUkIyCVTSd18wJ"
        + "3axr1qjLtSgNPWet0puSxOFH0AzFKRCJOjUkQRU8iAkz64MLAf9xrx4nBECciqeB"
        + "941s01kLtG8C/UqC3O9SwHSdhtUpUU8V/91SiD09yNJsnODi3WqM3oLg1QYzKhoa"
        + "D2mVo2xJLQ/QXqr2XIc5i2Mlpfq6S5JNbFD/I+UFhBUlBNuDOEV7ttIt2eFMEUsf"
        + "kCestGo0YoQYOpTLPcPGRS7MnSY1CLWGUYqaMSnes0nS8ke2PPD4Q0suAZz4msnh"
        + "NufanscstM8tcNtsZF6hj0JvbZok89szAgMBAAGjYzBhMB8GA1UdIwQYMBaAFIgW"
        + "dvPuXSJklQkwvPTIhpgTzhVhMB0GA1UdDgQWBBSIFnbz7l0iZJUJMLz0yIaYE84V"
        + "YTAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjANBgkqhkiG9w0BAQUF"
        + "AAOCAQEAs8QhTcl1gIczPhb0JFFB44Uvv5PSGzmnZev9PRstdHzyElCXngM3DVDc"
        + "RfoukW0FUcStw7r7XrVKxMAQPHiFmmfESr8M5uX/RNvp9Vm/x3NsCZfYXFsj7P89"
        + "kRfZOrld+IEVlKa/OhMiG9uOvT3ZZQIUON7pLZEfiVgMTnzKKAyJ1uE0cD18wXrZ"
        + "XkzvVwyBX61ALnLA49ZvE9IWpzJEk9F+hgFnJox1GNuQn6JC9ibdEW+FQVuThCVI"
        + "axcICn4Ek9fPos990Ehd9EdMx+tWgz+6URtkpRYLPGCFZ3ygejGvCh6FqRfLMFoc"
        + "DdEVSTgcutG5PEOLctckxeHsS6yJkg==";

    private static final String V3_ITERCA_CERT = "MIID4zCCAsugAwIBAgIQTAHcJ2NjsKKdlkNAWCJaxTANBgkqhkiG9w0BAQUFADAS"
        + "MRAwDgYDVQQDDAdUZXN0IENBMB4XDTE0MDMxNzIzMDAwMFoXDTE1MDczMDIyMDAw"
        + "MFowHzEdMBsGA1UEAwwUVGVzdCBJbnRlcm1lZGlhdGUgQ0EwggG3MIIBLAYHKoZI"
        + "zjgEATCCAR8CgYEAuMeV+akqPwGIR96SItOdth1mWjkfPoTZ0CPH6vKq/eN4Zhs9"
        + "PGH6nGsPQwghQoX7ejxj5MHAGlsqedn8bZP8/pBezz6XsftDgJQkpKF1D4+Gkrwl"
        + "dhogI3hUC0qw/g9WEExYvD/dUA2HrskPBnPU5rmyhyLd0BVANY3yZzg91uECFQCh"
        + "at9GPXgBRBPvn1P8x6fV1D2jQQKBgQCBY+URaSm+B+QK/IExGJFZyFLSy45RQvlH"
        + "KpgqZUWkMTZopEwwU7mjCWW4nWWbnt/jNp11jzMCxPva2/S1PHPucUAA+hfhA85x"
        + "LFhhqm8QZFmSUlFTiqutc4+VdOJrqOxaWCOcxonjfMIFe14rYB1nYMXtiv1Q9G42"
        + "6QjNJhzilwOBhAACgYAQf9xfhq5hiNqnA++Km6WZQ8s8H0ZM9xl9x58tgfgWUCz9"
        + "bX+996vWQZkv/66tK77kP8Wic4Y0T1VGcuFU7hLI6t6Z8lM7RksDvDlyJDBxBHHB"
        + "dfe+9jPG2IUDYfsbtIdVyDwYPPKKQd49CfIPCOBd/YLx3FzJqxQDkUlvrZgqjaOB"
        + "kjCBjzBKBgNVHSMEQzBBgBSIFnbz7l0iZJUJMLz0yIaYE84VYaEWpBQwEjEQMA4G"
        + "A1UEAwwHVGVzdCBDQYIRANKASb61Xw7K0Oz9c034VPswHQYDVR0OBBYEFG663aJM"
        + "aoS1UB532ZKStTze5FG6MBIGA1UdEwEB/wQIMAYBAf8CAQAwDgYDVR0PAQH/BAQD"
        + "AgEGMA0GCSqGSIb3DQEBBQUAA4IBAQAfcpvFEzTGdqcdg/3XUS7PDnuP40O+Alli"
        + "5Vqt77Ldh3a11+g6HBxSrk+MrqB/0gC2SQ1id0FCyJxfXAameZpqqXt2DnjwOWX/"
        + "cWcDBws0VbZDisq37eg/LwPAAxnJvF9ap625Vwmr+Gr7B/zehegdOajYj5Iufk0Z"
        + "72ZJEXs39KX9g4+95YxRX8wRgASv3qSl1rtdR7jEo8T2+Ca5CjueNtk7uGxzPzUV"
        + "93dfky41j1UKXlCndj0vqTvlRe7pJ9OPFR5I9P2afQWOKELVR67yDsQnD9MRblkF"
        + "/zEecnWSgPNAJi/YywA3ImvwlVJADrIOsAwbAOm6SHADgHgNAJ2n";

    private static final String V3_CERT = "MIIDMTCCAu+gAwIBAgIRANthik4fzttcoxdetKvv3g0wCwYHKoZIzjgEAwUAMB8x"
        + "HTAbBgNVBAMMFFRlc3QgSW50ZXJtZWRpYXRlIENBMB4XDTE0MDMxNzIzMDAwMFoX"
        + "DTE1MDczMDIyMDAwMFowGjEYMBYGA1UEAwwPVGVzdCBFbmQgRW50aXR5MIIBtzCC"
        + "ASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/ed2VrBw6TPkgMy"
        + "V1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6zeaTpvUohGLyTa0F"
        + "7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuakjWcHW31+pN4u3cJ"
        + "Ng5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3MX5CLX/5vDvxyTeeR"
        + "PZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+Byj/F56DDO31+qPu7Z"
        + "xbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhnyQtFzMddHmYB0QnS9y"
        + "X1n6DOWj/CSX0PvrlMYDgYQAAoGAJvnuTm8oI/RRI2tiZHtPkvSQaA3FP4PRsVx6"
        + "z1oIGg9OAxrtSS/aiQa+HWFg7fjHlMJ30Vh0yqt7igj70jaLGyDvr3MPDyiO++72"
        + "IiGUluc6yHg6m9cQ53eeJt9i44LJfTOw1S3YMU1ST7alokSnJRTICp5WBy0m1scw"
        + "heuTo0Gjga8wgawwSQYDVR0jBEIwQIAUbrrdokxqhLVQHnfZkpK1PN7kUbqhFqQU"
        + "MBIxEDAOBgNVBAMMB1Rlc3QgQ0GCEEwB3CdjY7CinZZDQFgiWsUwHQYDVR0OBBYE"
        + "FJ0i7GBYsbjkyTTVEdohFS7ZFp0eMA4GA1UdDwEB/wQEAwIEkDATBgNVHSUEDDAK"
        + "BggrBgEFBQcDBDAbBgNVHREEFDASgRB0ZXN0QGV4YW1wbGUuY29tMAsGByqGSM44"
        + "BAMFAAMvADAsAhQJWXbMhGtsgc8EbXoXWWlk+QNuoQIURvSp4HxJ8oFD8RbmiRsc"
        + "4jiu93Q=";

    private CertificateFactory factory;
    private static byte[] v1CaCert;
    private static byte[] v1Cert;
    private static byte[] v3CaCert;
    private static byte[] v3InterCaCert;
    private static byte[] v3Cert;

    public void setupTest(MockitoComponentMockingRule<CertificateFactory> mocker) throws Exception
    {
        // Decode keys once for all tests.
        if (v1CaCert == null) {
            BinaryStringEncoder base64encoder = mocker.getInstance(BinaryStringEncoder.class, "Base64");
            v1CaCert = base64encoder.decode(V1_CA_CERT);
            v1Cert = base64encoder.decode(V1_CERT);
            v3CaCert = base64encoder.decode(V3_CA_CERT);
            v3InterCaCert = base64encoder.decode(V3_ITERCA_CERT);
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
    public void testV3InterCACert() throws Exception
    {
        CertifiedPublicKey caCert = factory.decode(v3CaCert);
        CertifiedPublicKey interCaCert = factory.decode(v3InterCaCert);

        assertTrue("Intermediate CA certificate should be verified by CA.",
            interCaCert.isSignedBy(caCert.getPublicKeyParameters()));

        assertThat(interCaCert, instanceOf(X509CertifiedPublicKey.class));
        X509CertifiedPublicKey cert = (X509CertifiedPublicKey) interCaCert;
        assertThat(cert.getVersionNumber(), equalTo(3));

        assertTrue("Basic constraints should be critical.",
            cert.getExtensions().isCritical(X509Extensions.BASIC_CONSTRAINTS_OID));
        assertTrue("Basic constraints should be set to CA.",
            cert.getExtensions().hasCertificateAuthorityBasicConstraints());
        assertThat(cert.getExtensions().getBasicConstraintsPathLen(), equalTo(0));
        assertTrue("KeyUsage extension should be critical.", cert.getExtensions().isCritical(KeyUsage.OID));
        assertThat(cert.getExtensions().getKeyUsage(), equalTo(EnumSet.of(KeyUsage.keyCertSign,
            KeyUsage.cRLSign)));

        assertThat(cert.getExtensions().getAuthorityKeyIdentifier(),
            equalTo(((X509CertifiedPublicKey) caCert).getExtensions().getSubjectKeyIdentifier()));
    }

    @Test
    public void testV3Cert() throws Exception
    {
        CertifiedPublicKey interCaCert = factory.decode(v3InterCaCert);
        CertifiedPublicKey certificate = factory.decode(v3Cert);

        assertTrue("End certificate should be verified by CA.",
            certificate.isSignedBy(interCaCert.getPublicKeyParameters()));

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

        assertThat(cert.getExtensions().getAuthorityKeyIdentifier(),
            equalTo(((X509CertifiedPublicKey) interCaCert).getExtensions().getSubjectKeyIdentifier()));
    }

}
