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

package org.xwiki.crypto.pkix;

import java.math.BigInteger;
import java.util.Collection;

import org.xwiki.component.annotation.Role;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.PrincipalIndentifier;
import org.xwiki.stability.Unstable;

/**
 * Provides certificates.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Role
@Unstable
public interface CertificateProvider
{
    /**
     * Return if available a certificate matching the given keyIdentifier.
     *
     * @param keyIdentifier the subject key identifier of the certificate.
     * @return a matching certificate or null if none were found.
     */
    CertifiedPublicKey getCertificate(byte[] keyIdentifier);

    /**
     * Return if available a certificate matching the given keyIdentifier.
     *
     * @param issuer the subject of the issuer of the certificate.
     * @param serial the serial number attributed by the issuer to the certificate.
     * @return a matching certificate or null if none were found.
     */
    CertifiedPublicKey getCertificate(PrincipalIndentifier issuer, BigInteger serial);

    /**
     * Return if available a certificate matching the given keyIdentifier.
     *
     * @param issuer the subject of the issuer of the certificate.
     * @param serial the serial number attributed by the issuer to the certificate.
     * @param keyIdentifier the subject key identifier of the certificate.
     * @return a matching certificate or null if none were found.
     */
    CertifiedPublicKey getCertificate(PrincipalIndentifier issuer, BigInteger serial, byte[] keyIdentifier);

    /**
     * Return if available a collection of certificate matching the given subject.
     *
     * @param subject the subject of the certificate.
     * @return a collection of matching certificate or null if none were found.
     */
    Collection<CertifiedPublicKey> getCertificate(PrincipalIndentifier subject);
}
