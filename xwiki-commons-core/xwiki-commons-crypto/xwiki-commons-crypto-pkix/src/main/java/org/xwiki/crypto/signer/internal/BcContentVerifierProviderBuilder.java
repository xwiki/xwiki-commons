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

package org.xwiki.crypto.signer.internal;

import org.bouncycastle.operator.ContentVerifierProvider;
import org.xwiki.component.annotation.Role;
import org.xwiki.crypto.params.cipher.asymmetric.PublicKeyParameters;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;

/**
 * Bridge {@link ContentVerifierProvider} with XWiki components.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Role
public interface BcContentVerifierProviderBuilder
{
    /**
     * Build a content verifier provider for the given certificate.
     *
     * @param certificate the certificate to be associated with the provider generated content verifier.
     * @return a content verifier provider.
     */
    ContentVerifierProvider build(CertifiedPublicKey certificate);

    /**
     * Build a content verifier provider for the given public key.
     *
     * @param publicKey the public key to be used with the provider generated content verifier.
     * @return a content verifier provider.
     */
    ContentVerifierProvider build(PublicKeyParameters publicKey);
}
