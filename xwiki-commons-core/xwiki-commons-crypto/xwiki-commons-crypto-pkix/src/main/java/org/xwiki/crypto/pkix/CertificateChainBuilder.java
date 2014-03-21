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

import java.util.Collection;

import org.xwiki.component.annotation.Role;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.stability.Unstable;

/**
 * Build a certificate chain for a given certificate and a certificate provider.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Role
@Unstable
public interface CertificateChainBuilder
{
    /**
     * Build a certification chain for a given certificate.
     *
     * @param certificate the certificate to build the chain for.
     * @param provider a certificate provider to retrieve required certificates.
     * @return a collection of certificates ordered from the root to the end entity.
     */
    Collection<CertifiedPublicKey> build(CertifiedPublicKey certificate, CertificateProvider provider);
}
