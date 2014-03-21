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

package org.xwiki.crypto.signer.internal.cms;

import java.util.Collection;
import java.util.Collections;

import org.bouncycastle.cms.SignerInformation;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.param.CMSSignerVerifiedInformation;

/**
 * Bouncy Castle based implementation of {@link CMSSignerVerifiedInformation}.
 *
 * @version $Id$
 * @since 6.0M1
 */
public class BcCMSSignerVerifiedInformation extends BcCMSSignerInfo implements CMSSignerVerifiedInformation
{
    private final boolean verified;
    private final Collection<CertifiedPublicKey> chain;

    BcCMSSignerVerifiedInformation(SignerInformation signer, boolean verified, Collection<CertifiedPublicKey> chain)
    {
        super(signer);
        this.verified = verified;
        this.chain = Collections.unmodifiableCollection(chain);
    }

    @Override
    public boolean isVerified()
    {
        return verified;
    }

    @Override
    public Collection<CertifiedPublicKey> getCertificateChain()
    {
        return chain;
    }
}
