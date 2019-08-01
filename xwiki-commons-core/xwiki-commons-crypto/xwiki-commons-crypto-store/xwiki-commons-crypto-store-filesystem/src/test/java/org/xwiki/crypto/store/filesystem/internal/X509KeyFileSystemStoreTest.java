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


package org.xwiki.crypto.store.filesystem.internal;

import java.io.File;
import java.math.BigInteger;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.AsymmetricKeyFactory;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.params.cipher.asymmetric.PrivateKeyParameters;
import org.xwiki.crypto.password.PrivateKeyPasswordBasedEncryptor;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.params.CertifiedKeyPair;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509Extensions;
import org.xwiki.crypto.store.FileStoreReference;
import org.xwiki.crypto.store.StoreReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit text form {@link org.xwiki.crypto.store.filesystem.internal.X509KeyFileSystemStore}
 *
 * @version $Id$
 * @since 6.1M2
 */
@ComponentTest
public class X509KeyFileSystemStoreTest
{
    private static final byte[] PASSWORD = "password".getBytes();

    private static final byte[] PRIVATEKEY = "privatekey".getBytes();

    private static final String ENCODED_PRIVATEKEY = "encoded_privatekey";

    private static final byte[] ENCRYPTED_PRIVATEKEY = "encrypted_privatekey".getBytes();

    private static final String ENCODED_ENCRYPTED_PRIVATEKEY = "encoded_encrypted_privatekey";

    private static final byte[] CERTIFICATE = "certificate".getBytes();

    private static final String ENCODED_CERTIFICATE = "encoded_certificate";

    private static final byte[] SUBJECT_KEYID = "subjectKeyId".getBytes();

    private static final String ENCODED_SUBJECTKEYID = "encoded_subjectKeyId";

    private static final String HEXENCODED_SUBJECTKEYID = "hex_encoded_subjectKeyId";

    private static final String SUBJECT = "CN=Subject";

    private static final String ISSUER = "CN=Issuer";

    private static final BigInteger SERIAL = new BigInteger("1234567890");

    private static final File TEST_DIR = new File("target/tmp");

    private static final File FILE = new File(TEST_DIR, "my.key");

    private static final File DIRECTORY = new File(TEST_DIR, "keystore");

    private static final File CERT_FILE = new File(DIRECTORY, HEXENCODED_SUBJECTKEYID + ".cert");

    private static final File KEY_FILE = new File(DIRECTORY, HEXENCODED_SUBJECTKEYID + ".key");

    private static final StoreReference SINGLE_STORE_REF = new FileStoreReference(FILE);

    private static final StoreReference MULTI_STORE_REF = new FileStoreReference(DIRECTORY, true);

    private static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n";

    private static final String BEGIN_ENCRYPTED_PRIVATE_KEY = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n";

    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";

    private static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----\n";

    private static final String END_ENCRYPTED_PRIVATE_KEY = "-----END ENCRYPTED PRIVATE KEY-----\n";

    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----\n";

    private static final String NEWLINE = "\n";

    private static final String KEY_FILE_CONTENT = BEGIN_PRIVATE_KEY + ENCODED_PRIVATEKEY + NEWLINE + END_PRIVATE_KEY;

    private static final String ENCRYTEDKEY_FILE_CONTENT = BEGIN_ENCRYPTED_PRIVATE_KEY + ENCODED_ENCRYPTED_PRIVATEKEY
        + NEWLINE + END_ENCRYPTED_PRIVATE_KEY;

    private static final String CERTIFICATE_FILE_CONTENT = BEGIN_CERTIFICATE + ENCODED_CERTIFICATE + NEWLINE
        + END_CERTIFICATE;

    private static final String FILE_CONTENT = KEY_FILE_CONTENT + CERTIFICATE_FILE_CONTENT;

    private static final String ENCRYPTED_FILE_CONTENT = ENCRYTEDKEY_FILE_CONTENT + CERTIFICATE_FILE_CONTENT;

    @InjectMockComponents
    public X509KeyFileSystemStore store;

    @InjectComponentManager
    public MockitoComponentManager componentManager;

    PrivateKeyParameters privateKey;

    X509CertifiedPublicKey certificate;

    CertifiedKeyPair keyPair;

    @BeforeEach
    public void setUp() throws Exception
    {
        BinaryStringEncoder base64Encoder = this.componentManager.getInstance(BinaryStringEncoder.class, "Base64");
        when(base64Encoder.encode(PRIVATEKEY, 64)).thenReturn(ENCODED_PRIVATEKEY);
        when(base64Encoder.decode(ENCODED_PRIVATEKEY)).thenReturn(PRIVATEKEY);
        when(base64Encoder.encode(ENCRYPTED_PRIVATEKEY, 64)).thenReturn(ENCODED_ENCRYPTED_PRIVATEKEY);
        when(base64Encoder.decode(ENCODED_ENCRYPTED_PRIVATEKEY)).thenReturn(ENCRYPTED_PRIVATEKEY);
        when(base64Encoder.encode(CERTIFICATE, 64)).thenReturn(ENCODED_CERTIFICATE);
        when(base64Encoder.decode(ENCODED_CERTIFICATE)).thenReturn(CERTIFICATE);
        when(base64Encoder.encode(SUBJECT_KEYID)).thenReturn(ENCODED_SUBJECTKEYID);
        when(base64Encoder.decode(ENCODED_SUBJECTKEYID)).thenReturn(SUBJECT_KEYID);

        BinaryStringEncoder hexEncoder = this.componentManager.getInstance(BinaryStringEncoder.class, "Hex");
        when(hexEncoder.encode(SUBJECT_KEYID)).thenReturn(HEXENCODED_SUBJECTKEYID);

        privateKey = mock(PrivateKeyParameters.class);
        when(privateKey.getEncoded()).thenReturn(PRIVATEKEY);

        AsymmetricKeyFactory keyFactory = this.componentManager.getInstance(AsymmetricKeyFactory.class);
        when(keyFactory.fromPKCS8(PRIVATEKEY)).thenReturn(privateKey);

        PrivateKeyPasswordBasedEncryptor encryptor =
            this.componentManager.getInstance(PrivateKeyPasswordBasedEncryptor.class);
        when(encryptor.encrypt(PASSWORD, privateKey)).thenReturn(ENCRYPTED_PRIVATEKEY);
        when(encryptor.decrypt(PASSWORD, ENCRYPTED_PRIVATEKEY)).thenReturn(privateKey);

        certificate = mock(X509CertifiedPublicKey.class);
        when(certificate.getSerialNumber()).thenReturn(SERIAL);
        when(certificate.getIssuer()).thenReturn(new DistinguishedName(ISSUER));
        when(certificate.getSubject()).thenReturn(new DistinguishedName(SUBJECT));
        when(certificate.getEncoded()).thenReturn(CERTIFICATE);

        CertificateFactory certificateFactory = this.componentManager.getInstance(CertificateFactory.class, "X509");
        when(certificateFactory.decode(CERTIFICATE)).thenReturn(certificate);

        X509Extensions extensions = mock(X509Extensions.class);
        when(certificate.getExtensions()).thenReturn(extensions);
        when(extensions.getSubjectKeyIdentifier()).thenReturn(SUBJECT_KEYID);
        when(certificate.getSubjectKeyIdentifier()).thenReturn(SUBJECT_KEYID);

        keyPair = new CertifiedKeyPair(privateKey, certificate);

        FileUtils.deleteDirectory(TEST_DIR);
        TEST_DIR.mkdirs();
    }

    @AfterEach
    public void deleteTestFiles() throws Exception
    {
        FileUtils.deleteDirectory(TEST_DIR);
    }

    @Test
    public void storePrivateKeyToFile() throws Exception
    {
        store.store(SINGLE_STORE_REF, keyPair);

        assertThat(FileUtils.readFileToString(FILE), equalTo(FILE_CONTENT));
    }

    @Test
    public void storePrivateKeyToDirectory() throws Exception
    {
        store.store(MULTI_STORE_REF, keyPair);

        assertThat(FileUtils.readFileToString(KEY_FILE),
            equalTo(KEY_FILE_CONTENT));
        assertThat(FileUtils.readFileToString(CERT_FILE),
            equalTo(CERTIFICATE_FILE_CONTENT));
    }

    @Test
    public void storeEncryptedPrivateKeyToFile() throws Exception
    {
        store.store(SINGLE_STORE_REF, keyPair, PASSWORD);

        assertThat(FileUtils.readFileToString(FILE), equalTo(ENCRYPTED_FILE_CONTENT));
    }

    @Test
    public void storeEncryptedPrivateKeyToDirectory() throws Exception
    {
        store.store(MULTI_STORE_REF, keyPair, PASSWORD);

        assertThat(FileUtils.readFileToString(KEY_FILE),
            equalTo(ENCRYTEDKEY_FILE_CONTENT));
        assertThat(FileUtils.readFileToString(CERT_FILE),
            equalTo(CERTIFICATE_FILE_CONTENT));
    }

    @Test
    public void retrievePrivateKeyFromFile() throws Exception
    {
        FileUtils.writeStringToFile(FILE, FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(SINGLE_STORE_REF);
        assertThat(keyPair, notNullValue());
        assertThat(keyPair.getPrivateKey(), equalTo(privateKey));
        assertThat(keyPair.getCertificate(), equalTo((CertifiedPublicKey) certificate));
    }

    @Test
    public void retrieveEncryptedPrivateKeyFromFile() throws Exception
    {
        FileUtils.writeStringToFile(FILE, ENCRYPTED_FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(SINGLE_STORE_REF, PASSWORD);
        assertThat(keyPair, notNullValue());
        assertThat(keyPair.getPrivateKey(), equalTo(privateKey));
        assertThat(keyPair.getCertificate(), equalTo((CertifiedPublicKey) certificate));
    }

    @Test
    public void retrievePrivateKeyFromDirectory() throws Exception
    {
        DIRECTORY.mkdirs();
        FileUtils.writeStringToFile(KEY_FILE, KEY_FILE_CONTENT);
        FileUtils.writeStringToFile(CERT_FILE, CERTIFICATE_FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(MULTI_STORE_REF, certificate);
        assertThat(keyPair, notNullValue());
        assertThat(keyPair.getPrivateKey(), equalTo(privateKey));
        assertThat(keyPair.getCertificate(), equalTo((CertifiedPublicKey) certificate));
    }

    @Test
    public void retrieveEncryptedPrivateKeyFromDirectory() throws Exception
    {
        DIRECTORY.mkdirs();
        FileUtils.writeStringToFile(KEY_FILE, ENCRYTEDKEY_FILE_CONTENT);
        FileUtils.writeStringToFile(CERT_FILE, CERTIFICATE_FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(MULTI_STORE_REF, certificate, PASSWORD);
        assertThat(keyPair, notNullValue());
        assertThat(keyPair.getPrivateKey(), equalTo(privateKey));
        assertThat(keyPair.getCertificate(), equalTo((CertifiedPublicKey) certificate));
    }

    @Test
    public void retrieveMissingPrivateKeyFromFile() throws Exception
    {
        FileUtils.writeStringToFile(FILE, CERTIFICATE_FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(SINGLE_STORE_REF);
        assertThat(keyPair, nullValue());
    }

    @Test
    public void retrieveMissingCertificateFromFile() throws Exception
    {
        FileUtils.writeStringToFile(FILE, KEY_FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(SINGLE_STORE_REF);
        assertThat(keyPair, nullValue());
    }

    @Test
    public void retrieveMissingPrivateKeyFromDirectory() throws Exception
    {
        DIRECTORY.mkdirs();
        FileUtils.writeStringToFile(CERT_FILE, CERTIFICATE_FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(MULTI_STORE_REF, certificate);
        assertThat(keyPair, nullValue());
    }

    @Test
    public void retrieveMissingCertificateFromDirectory() throws Exception
    {
        DIRECTORY.mkdirs();
        FileUtils.writeStringToFile(KEY_FILE, KEY_FILE_CONTENT);

        CertifiedKeyPair keyPair = store.retrieve(MULTI_STORE_REF, certificate);
        assertThat(keyPair, notNullValue());
        assertThat(keyPair.getPrivateKey(), equalTo(privateKey));
        assertThat(keyPair.getCertificate(), equalTo((CertifiedPublicKey) certificate));
    }

    @Test
    public void storePrivateKeyToDirectoryWithoutSubjectID() throws Exception
    {
        when(certificate.getExtensions()).thenReturn(null);
        when(certificate.getSubjectKeyIdentifier()).thenReturn(null);

        store.store(MULTI_STORE_REF, keyPair);

        assertThat(FileUtils.readFileToString(new File(DIRECTORY, SERIAL + ", " + ISSUER + ".key")),
            equalTo(KEY_FILE_CONTENT));
        assertThat(FileUtils.readFileToString(new File(DIRECTORY, SERIAL + ", " + ISSUER + ".cert")),
            equalTo(CERTIFICATE_FILE_CONTENT));
    }
}
