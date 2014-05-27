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
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.store.FileStoreReference;
import org.xwiki.crypto.store.KeyStore;
import org.xwiki.crypto.store.StoreReference;

/**
 * Abstract base class for X.509 file system based store.
 *
 * @version $Id$
 * @since 6.1M2
 */
public abstract class AbstractX509FileSystemStore implements KeyStore
{
    protected static final String CERTIFICATE = "CERTIFICATE";
    protected static final String DASHES = "-----";
    protected static final String PEM_BEGIN = DASHES + "BEGIN ";
    protected static final String PEM_END = DASHES + "END ";
    protected static final String KEY_FILE_EXTENSION = ".key";
    protected static final String CERTIFICATE_FILE_EXTENSION = ".cert";

    /**
     * Used to encode/decode certificates, private keys and subject keys.
     */
    @Inject
    @Named("Base64")
    private BinaryStringEncoder base64;

    /**
     * Used to create certificate from encoded bytes.
     */
    @Inject
    @Named("X509")
    private CertificateFactory certificateFactory;

    /**
     * Write data encoded based64 between PEM line headers and close the writer.
     *
     * @param out the output buffered writer to write to.
     * @param type the type to be put in the header.
     * @param data the bytes data to store encoded.
     * @throws IOException on error.
     */
    protected void store(BufferedWriter out, String type, byte[] data) throws IOException
    {
        write(out, type, data);
        out.close();
    }

    /**
     * Write data encoded based64 between PEM line headers.
     *
     * @param out the output buffered writer to write to.
     * @param type the type to be written in the header.
     * @param data the bytes data to store encoded.
     * @throws IOException on error.
     */
    protected void write(BufferedWriter out, String type, byte[] data) throws IOException
    {
        writeHeader(out, type);
        out.write(base64.encode(data, 64));
        out.newLine();
        writeFooter(out, type);
    }

    /**
     * Write a PEM like header.
     *
     * @param out the output buffered writer to write to.
     * @param type the type to be written in the header.
     * @throws IOException on error.
     */
    private static void writeHeader(BufferedWriter out, String type) throws IOException
    {
        out.write(PEM_BEGIN + type + DASHES);
        out.newLine();
    }

    /**
     * Write a PEM like footer.
     *
     * @param out the output buffered writer to write to.
     * @param type the type to be written in the footer.
     * @throws IOException on error.
     */
    private static void writeFooter(BufferedWriter out, String type) throws IOException
    {
        out.write(PEM_END + type + DASHES);
        out.newLine();
    }

    /**
     * Return the file corresponding to the given store reference.
     *
     * @param store the store reference.
     * @return a file or folder for storage.
     * @throws java.lang.IllegalArgumentException if the reference is not appropriate for a filesystem storage.
     */
    protected File getStoreFile(StoreReference store)
    {
        if (store instanceof FileStoreReference) {
            return ((FileStoreReference) store).getFile();
        }
        throw new IllegalArgumentException(String.format("Unsupported store reference [%s] for this implementation.",
            store.getClass().getName()));
    }

    /**
     * Return true if the store is a multi key store.
     *
     * @param store the store reference.
     * @return true if the store is a multi key store.
     */
    protected boolean isMulti(StoreReference store)
    {
        return !(store instanceof FileStoreReference) || ((FileStoreReference) store).isMulti();
    }

    /**
     * Return the X.509 certificate.
     *
     * @param publicKey the certified public key.
     * @return the cast to an X.509 certificate.
     * @throws java.lang.IllegalArgumentException if the certified key is not an X.509 one.
     */
    protected X509CertifiedPublicKey getPublicKey(CertifiedPublicKey publicKey)
    {
        if (publicKey instanceof X509CertifiedPublicKey) {
            return (X509CertifiedPublicKey) publicKey;
        }

        throw new IllegalArgumentException(String.format("Unsupported certificate [%s], expecting X509 certificates.",
            publicKey.getClass().getName()));
    }

    /**
     * Return a unique identifier appropriate for a file name. If the certificate as a subject key identifier, the
     * result is this encoded identifier. Else, use the concatenation of the certificate serial number and the issuer
     * name.
     *
     * @param publicKey the certificate.
     * @return a unique identifier.
     * @throws java.io.IOException on error.
     */
    protected String getCertIdentifier(X509CertifiedPublicKey publicKey) throws IOException
    {
        byte[] keyId = publicKey.getSubjectKeyIdentifier();
        if (keyId != null) {
            return base64.encode(keyId);
        }
        return publicKey.getSerialNumber().toString() + ", " + publicKey.getIssuer().getName();
    }

    /**
     * Read an object from a PEM like file.
     *
     * @param in the input reader to read from.
     * @param password a password to decrypt encrypted objects. May be null if the object is not encrypted.
     * @return the object read.
     * @throws IOException on I/O error.
     * @throws GeneralSecurityException on decryption error.
     */
    protected Object readObject(BufferedReader in, byte[] password) throws IOException, GeneralSecurityException
    {
        String line;
        Object obj = null;

        while ((line = in.readLine()) != null) {
            obj = processObject(in, line, password);
            if (obj != null) {
                break;
            }
        }

        return obj;
    }

    /**
     * Process an object from a PEM like file.
     *
     * @param in the input reader to read from.
     * @param line the last read line.
     * @param password a password to decrypt encrypted objects. May be null if the object is not encrypted.
     * @return the object read, or null if the line was not a recognized PEM header.
     * @throws IOException on I/O error.
     * @throws GeneralSecurityException on decryption error.
     */
    protected Object processObject(BufferedReader in, String line, byte[] password)
        throws IOException, GeneralSecurityException
    {
        if (line.contains(PEM_BEGIN + CERTIFICATE + DASHES)) {
            return certificateFactory.decode(readBytes(in, PEM_END + CERTIFICATE + DASHES));
        }
        return null;
    }

    /**
     * Read base64 data up to an end marker and decode them.
     *
     * @param in the input reader to read from.
     * @param endMarker the end marker.
     * @return the data read.
     * @throws IOException on error.
     */
    protected byte[] readBytes(BufferedReader in, String endMarker) throws IOException
    {
        String line;
        StringBuilder buf = new StringBuilder();

        while ((line = in.readLine()) != null) {
            if (line.contains(endMarker)) {
                break;
            }
            buf.append(line.trim());
        }

        return base64.decode(buf.toString());
    }
}
