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

package org.xwiki.crypto.signer;

import java.security.GeneralSecurityException;
import java.util.Collection;

import org.xwiki.component.annotation.Role;
import org.xwiki.crypto.pkix.CertificateProvider;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.param.CMSSignedDataVerified;

/**
 * Verify SignedData according to RFC 3852.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Role
public interface CMSSignedDataVerifier
{
    /**
     * Verify all signature contained in the signature against the embedded data.
     *
     * @param signature the encoded signature to verify.
     * @return the result of that verification, and information contained in the signed data.
     * @throws java.security.GeneralSecurityException on error.
     */
    CMSSignedDataVerified verify(byte[] signature) throws GeneralSecurityException;

    /**
     * Verify all signature contained in the signature against the embedded data.
     *
     * @param signature the encoded signature to verify.
     * @param certificates additional certificates to proceed to the verification.
     * @return the result of that verification, and information contained in the signed data.
     * @throws java.security.GeneralSecurityException on error.
     */
    CMSSignedDataVerified verify(byte[] signature, Collection<CertifiedPublicKey> certificates)
        throws GeneralSecurityException;

    /**
     * Verify all signature contained in the signature against the embedded data.
     *
     * @param signature the encoded signature to verify.
     * @param certificateProvider provider of additional certificate to proceed to the verification.
     * @return the result of that verification, and information contained in the signed data.
     * @throws java.security.GeneralSecurityException on error.
     */
    CMSSignedDataVerified verify(byte[] signature, CertificateProvider certificateProvider)
        throws GeneralSecurityException;

    /**
     * Verify all signature contained in the signature against the provided data.
     *
     * @param signature the encoded signature to verify.
     * @param data the data to check the signature against.
     * @return the result of that verification, and information contained in the signed data.
     * @throws java.security.GeneralSecurityException on error.
     */
    CMSSignedDataVerified verify(byte[] signature, byte[] data) throws GeneralSecurityException;

    /**
     * Verify all signature contained in the signature against the provided data.
     *
     * @param signature the encoded signature to verify.
     * @param data the data to check the signature against.
     * @param certificates additional certificates to proceed to the verification.
     * @return the result of that verification, and information contained in the signed data.
     * @throws java.security.GeneralSecurityException on error.
     */
    CMSSignedDataVerified verify(byte[] signature, byte[] data, Collection<CertifiedPublicKey> certificates)
        throws GeneralSecurityException;

    /**
     * Verify all signature contained in the signature against the provided data.
     *
     * @param signature the encoded signature to verify.
     * @param data the data to check the signature against.
     * @param certificateProvider provider of additional certificate to proceed to the verification.
     * @return the result of that verification, and information contained in the signed data.
     * @throws java.security.GeneralSecurityException on error.
     */
    CMSSignedDataVerified verify(byte[] signature, byte[] data, CertificateProvider certificateProvider)
        throws GeneralSecurityException;
}
