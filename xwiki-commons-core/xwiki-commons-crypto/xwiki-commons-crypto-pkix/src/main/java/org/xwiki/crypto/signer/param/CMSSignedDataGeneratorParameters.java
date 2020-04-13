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

package org.xwiki.crypto.signer.param;

import java.util.ArrayList;
import java.util.Collection;

import org.xwiki.crypto.pkix.CertifyingSigner;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;

/**
 * Parameters for the generation of SignedData.
 *
 * @version $Id$
 * @since 6.0M1
 */
public class CMSSignedDataGeneratorParameters
{
    private Collection<CMSSignerInfo> signatures = new ArrayList<>();

    private Collection<CertifyingSigner> signers = new ArrayList<>();

    private Collection<CertifiedPublicKey> certificates = new ArrayList<>();

    /**
     * Add existing signature.
     *
     * @param signer a signer info containing an already calculated signature for the targeted data.
     * @return this object for call chaining.
     */
    public CMSSignedDataGeneratorParameters addSignature(CMSSignerInfo signer)
    {
        this.signatures.add(signer);
        return this;
    }

    /**
     * Add a new signer.
     *
     * @param signer a certifying signer to be used to sign the content data.
     * @return this object for call chaining.
     */
    public CMSSignedDataGeneratorParameters addSigner(CertifyingSigner signer)
    {
        this.signers.add(signer);
        return this;
    }

    /**
     * Add a collection of existing signatures.
     *
     * @param signers a collection of signer info containing an already calculated signature for the targeted data.
     * @return this object for call chaining.
     */
    public CMSSignedDataGeneratorParameters addSignatures(Collection<CMSSignerInfo> signers)
    {
        this.signatures.addAll(signers);
        return this;
    }

    /**
     * Add a collection of new signers.
     *
     * @param signers a collection of certifying signer to be used to sign the content data.
     * @return this object for call chaining.
     */
    public CMSSignedDataGeneratorParameters addSigners(Collection<CertifyingSigner> signers)
    {
        this.signers.addAll(signers);
        return this;
    }

    /**
     * Add a certificate.
     *
     * @param certificate a certificate.
     * @return this object for call chaining.
     */
    public CMSSignedDataGeneratorParameters addCertificate(CertifiedPublicKey certificate)
    {
        this.certificates.add(certificate);
        return this;
    }

    /**
     * Add a collection of certificates.
     *
     * @param certificates a collection of certificates to be joined with the signed data.
     * @return this object for call chaining.
     */
    public CMSSignedDataGeneratorParameters addCertificates(Collection<CertifiedPublicKey> certificates)
    {
        this.certificates.addAll(certificates);
        return this;
    }

    /**
     * @return the aggregated collection of certificates to be joined with the signed data.
     */
    public Collection<CertifiedPublicKey> getCertificates()
    {
        return this.certificates;
    }

    /**
     * @return the aggregated collection of signer info containing an already calculated signature.
     */
    public Collection<CMSSignerInfo> getSignatures()
    {
        return this.signatures;
    }

    /**
     * @return the aggregated collection of certifying signer to be used to sign the content data.
     */
    public Collection<CertifyingSigner> getSigners()
    {
        return this.signers;
    }
}
