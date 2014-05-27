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

package org.xwiki.crypto.store;

import org.xwiki.component.annotation.Role;
import org.xwiki.crypto.pkix.params.CertifiedKeyPair;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.stability.Unstable;

/**
 * Store and retrieve private key from a key store.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Role
@Unstable
public interface KeyStore
{
    /**
     * Store a private key and its certificate into a given store.
     *
     * NOT VERY SECURE, since the key will be store AS IS without encryption.
     *
     * @param store the store where to save the key and its certificate.
     * @param keyPair the key pair to be stored.
     * @throws KeyStoreException on error.
     */
    void store(StoreReference store, CertifiedKeyPair keyPair) throws KeyStoreException;

    /**
     * Store a private key and its certificate into a given store, encrypting the key with a password.
     *
     * @param store the store where to save the key and its certificate.
     * @param keyPair the key pair to be stored.
     * @param password the password to encrypt the private key.
     * @throws KeyStoreException on error.
     */
    void store(StoreReference store, CertifiedKeyPair keyPair, byte[] password) throws KeyStoreException;

    /**
     * Retrieve a private key from a given store that may contains only a single key.
     *
     * @param store the single-key store where the key is stored with its certificate.
     * @return the certified key pair, or null if none have been found.
     * @throws KeyStoreException on error.
     */
    CertifiedKeyPair retrieve(StoreReference store) throws KeyStoreException;

    /**
     * Retrieve the certified key pair from a given store that may contains only a single key and decrypt it using
     * the given password.
     *
     * @param store the single-key store where the key is stored encrypted with its certificate.
     * @param password the password to decrypt the private key.
     * @return the certified key pair, or null if none have been found.
     * @throws KeyStoreException on error.
     */
    CertifiedKeyPair retrieve(StoreReference store, byte[] password) throws KeyStoreException;


    /**
     * Retrieve the certified key pair from a given store that match the given certificate.
     *
     * @param store the multi-key store where the key has been stored with its certificate.
     * @param publicKey for which the private key is requested.
     * @return the certified key pair corresponding to the given certificate, or null if none have been found.
     * @throws KeyStoreException on error.
     */
    CertifiedKeyPair retrieve(StoreReference store, CertifiedPublicKey publicKey) throws KeyStoreException;

    /**
     * Retrieve the certified key pair from a given store that match the given certificate and decrypt it using
     * the given password.
     *
     * @param store the multi-key store where the key has been stored encrypted with its certificate
     * @param publicKey for which the private key is requested.
     * @param password the password to decrypt the private key.
     * @return the certified key pair corresponding to the given certificate, or null if none have been found.
     * @throws KeyStoreException on error.
     */
    CertifiedKeyPair retrieve(StoreReference store, CertifiedPublicKey publicKey, byte[] password)
        throws KeyStoreException;
}
