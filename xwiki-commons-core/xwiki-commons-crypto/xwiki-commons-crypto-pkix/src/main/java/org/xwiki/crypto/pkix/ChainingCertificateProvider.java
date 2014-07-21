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

import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.PrincipalIndentifier;

/**
 * Chain two or more certificate provider, asking each of them until found for the requested certificate.
 *
 * @version $Id$
 * @since 6.0RC1
 */
public class ChainingCertificateProvider implements CertificateProvider
{
    private final CertificateProvider[] providers;

    /**
     * Create a new chaining certificate provider from the given providers.
     *
     * @param providers providers to be chained in order.
     */
    public ChainingCertificateProvider(CertificateProvider... providers)
    {
        this.providers = providers;
    }

    @Override
    public CertifiedPublicKey getCertificate(byte[] keyIdentifier)
    {
        CertifiedPublicKey result = null;
        for (CertificateProvider provider : this.providers) {
            result = provider.getCertificate(keyIdentifier);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public CertifiedPublicKey getCertificate(PrincipalIndentifier issuer, BigInteger serial)
    {
        CertifiedPublicKey result = null;
        for (CertificateProvider provider : this.providers) {
            result = provider.getCertificate(issuer, serial);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public CertifiedPublicKey getCertificate(PrincipalIndentifier issuer, BigInteger serial, byte[] keyIdentifier)
    {
        CertifiedPublicKey result = null;
        for (CertificateProvider provider : this.providers) {
            result = provider.getCertificate(issuer, serial, keyIdentifier);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public Collection<CertifiedPublicKey> getCertificate(PrincipalIndentifier subject)
    {
        Collection<CertifiedPublicKey> result = null;
        for (CertificateProvider provider : this.providers) {
            result = provider.getCertificate(subject);
            if (result != null) {
                break;
            }
        }
        return result;
    }
}
