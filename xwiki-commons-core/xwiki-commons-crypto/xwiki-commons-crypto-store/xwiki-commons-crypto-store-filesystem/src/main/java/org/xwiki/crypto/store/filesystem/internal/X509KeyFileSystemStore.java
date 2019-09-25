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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.crypto.AsymmetricKeyFactory;
import org.xwiki.crypto.params.cipher.asymmetric.PrivateKeyParameters;
import org.xwiki.crypto.password.PrivateKeyPasswordBasedEncryptor;
import org.xwiki.crypto.pkix.params.CertifiedKeyPair;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.store.KeyStore;
import org.xwiki.crypto.store.KeyStoreException;
import org.xwiki.crypto.store.StoreReference;

/**
 * X509 implementation of {@link org.xwiki.crypto.store.KeyStore} for a wiki store.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Named("X509file")
@Singleton
public class X509KeyFileSystemStore extends AbstractX509FileSystemStore implements KeyStore
{
    private static final String PRIVATE_KEY = "PRIVATE KEY";

    private static final String ENCRYPTED_PRIVATE_KEY = "ENCRYPTED " + PRIVATE_KEY;

    /**
     * Used to encrypt/decrypt private keys.
     */
    @Inject
    private PrivateKeyPasswordBasedEncryptor encryptor;

    /**
     * Used to create private key from encoded bytes.
     */
    @Inject
    private AsymmetricKeyFactory keyFactory;

    @Override
    public void store(StoreReference store, CertifiedKeyPair keyPair) throws KeyStoreException
    {
        storeKeyPair(store, getPublicKey(keyPair.getCertificate()), keyPair.getPrivateKey().getEncoded(), PRIVATE_KEY);
    }

    @Override
    public void store(StoreReference store, CertifiedKeyPair keyPair, byte[] password) throws KeyStoreException
    {
        byte[] key;

        try {
            key = this.encryptor.encrypt(password, keyPair.getPrivateKey());
        } catch (Exception e) {
            throw new KeyStoreException(
                String.format("Error while encrypting private key to store a key pair in [%s]", store), e);
        }

        storeKeyPair(store, getPublicKey(keyPair.getCertificate()), key, ENCRYPTED_PRIVATE_KEY);
    }

    private void storeKeyPair(StoreReference store, X509CertifiedPublicKey certificate, byte[] privateKey, String type)
        throws KeyStoreException
    {
        File file = getStoreFile(store);

        try {
            if (isMulti(store)) {
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        throw new KeyStoreException(String.format("Error while creating path [%s]", file));
                    }
                }

                String filename = getCertIdentifier(certificate);
                File keyfile = new File(file, filename + KEY_FILE_EXTENSION);
                File certfile = new File(file, filename + CERTIFICATE_FILE_EXTENSION);

                store(new BufferedWriter(new FileWriter(keyfile)), type, privateKey);
                store(new BufferedWriter(new FileWriter(certfile)), CERTIFICATE, certificate.getEncoded());
            } else {
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        throw new KeyStoreException(String.format("Error while creating file [%s]", file));
                    }
                }

                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                write(out, type, privateKey);
                store(out, CERTIFICATE, certificate.getEncoded());
            }
        } catch (IOException e) {
            throw new KeyStoreException(String.format("Error while writing private key to file [%s]", file), e);
        }
    }

    @Override
    public CertifiedKeyPair retrieve(StoreReference store) throws KeyStoreException
    {
        return retrieve(store, (byte[]) null);
    }

    @Override
    public CertifiedKeyPair retrieve(StoreReference store, byte[] password) throws KeyStoreException
    {
        File file = getStoreFile(store);

        if (isMulti(store)) {
            throw new KeyStoreException(
                String.format("Unexpected store reference, [%s] should be single key store.", file));
        }

        X509CertifiedPublicKey cert = null;
        PrivateKeyParameters key = null;

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));

            Object obj;
            while ((obj = readObject(in, password)) != null) {
                if (obj instanceof X509CertifiedPublicKey) {
                    cert = (X509CertifiedPublicKey) obj;
                    continue;
                }
                if (obj instanceof PrivateKeyParameters) {
                    key = ((PrivateKeyParameters) obj);
                }
            }
        } catch (IOException e) {
            throw new KeyStoreException(String.format("Error while reading from file [%s]", file), e);
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException(String.format("Error while decrypting private key from file [%s]", file), e);
        }

        if (key != null && cert != null) {
            return new CertifiedKeyPair(key, cert);
        }
        return null;
    }

    @Override
    public CertifiedKeyPair retrieve(StoreReference store, CertifiedPublicKey publicKey) throws KeyStoreException
    {
        return retrieve(store, publicKey, null);
    }

    @Override
    public CertifiedKeyPair retrieve(StoreReference store, CertifiedPublicKey publicKey, byte[] password)
        throws KeyStoreException
    {
        File file = getStoreFile(store);
        X509CertifiedPublicKey certificate = getPublicKey(publicKey);

        if (!isMulti(store)) {
            throw new KeyStoreException(String.format("Unexpected store reference, [%s] should be multi key store.",
                file));
        }

        try {
            File keyfile = new File(file, getCertIdentifier(certificate) + KEY_FILE_EXTENSION);

            if (keyfile.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(keyfile));
                Object obj;
                while ((obj = readObject(in, password)) != null) {
                    if (obj instanceof PrivateKeyParameters) {
                        return new CertifiedKeyPair(((PrivateKeyParameters) obj), certificate);
                    }
                }
            }
        } catch (IOException e) {
            throw new KeyStoreException(String.format("Error while reading private key from store [%s]", file), e);
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException(String.format("Error while decrypting private key from store [%s]", file), e);
        }
        return null;
    }

    @Override
    protected Object processObject(BufferedReader in, String line, byte[] password)
        throws IOException, GeneralSecurityException
    {
        if (line.contains(PEM_BEGIN + PRIVATE_KEY + DASHES)) {
            return this.keyFactory.fromPKCS8(readBytes(in, PEM_END + PRIVATE_KEY + DASHES));
        }
        if (line.contains(PEM_BEGIN + ENCRYPTED_PRIVATE_KEY + DASHES)) {
            return this.encryptor.decrypt(password, readBytes(in, PEM_END + ENCRYPTED_PRIVATE_KEY + DASHES));
        }
        return super.processObject(in, line, password);
    }
}
