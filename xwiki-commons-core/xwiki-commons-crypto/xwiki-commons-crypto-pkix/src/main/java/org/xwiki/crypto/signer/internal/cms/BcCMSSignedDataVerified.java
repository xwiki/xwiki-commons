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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.param.CMSSignedDataVerified;
import org.xwiki.crypto.signer.param.CMSSignerVerifiedInformation;

/**
 * Internal implementation of a {@link CMSSignedDataVerified}.
 *
 * @version $Id$
 * @since 6.0M1
 */
public class BcCMSSignedDataVerified implements CMSSignedDataVerified
{
    private final Collection<CMSSignerVerifiedInformation> signatures = new ArrayList<>();

    private final Collection<CertifiedPublicKey> certificates = new ArrayList<>();

    private final String contentType;

    private final byte[] content;

    BcCMSSignedDataVerified(String contentType, byte[] content)
    {
        this.contentType = contentType;
        this.content = content;
    }

    void addCertificate(CertifiedPublicKey certificate)
    {
        this.certificates.add(certificate);
    }

    void addSignature(CMSSignerVerifiedInformation signature)
    {
        this.signatures.add(signature);
    }

    @Override
    public Collection<CMSSignerVerifiedInformation> getSignatures()
    {
        return Collections.unmodifiableCollection(this.signatures);
    }

    @Override
    public Collection<CertifiedPublicKey> getCertificates()
    {
        return Collections.unmodifiableCollection(this.certificates);
    }

    @Override
    public String getContentType()
    {
        return this.contentType;
    }

    @Override
    public byte[] getContent()
    {
        return this.content;
    }

    @Override
    public boolean isVerified()
    {
        boolean result = !this.signatures.isEmpty();
        for (CMSSignerVerifiedInformation signature : this.signatures) {
            result &= signature.isVerified();
            if (!result) {
                break;
            }
        }
        return result;
    }
}
