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

import java.security.GeneralSecurityException;
import java.util.Collection;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.xwiki.crypto.DigestFactory;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.internal.BcContentVerifierProviderBuilder;

/**
 * Utility class for Bouncy Castle CMS.
 *
 * @version $Id$
 * @since 6.0M1
 */
public final class BcCMSUtils
{
    private BcCMSUtils()
    {
        // Utility class
    }

    /**
     * Verify a CMS signature.
     *
     * @param signer the signer to verify.
     * @param certKey the certified public key of the signer.
     * @param contentVerifierProviderBuilder a builder of content provider.
     * @param digestProvider a digest provider.
     * @return true if the signature is verified and the certificate was valid at the time of signature.
     * @throws CMSException if the verifier is unable to create appropriate ContentVerifiers or DigestCalculators.
     */
    public static boolean verify(SignerInformation signer,
        CertifiedPublicKey certKey, BcContentVerifierProviderBuilder contentVerifierProviderBuilder,
        DigestFactory digestProvider) throws CMSException
    {
        if (certKey == null) {
            throw new CMSException("No certified key for proceeding to signature validation.");
        }

        return signer.verify(
            new SignerInformationVerifier(
                new DefaultCMSSignatureAlgorithmNameGenerator(),
                new DefaultSignatureAlgorithmIdentifierFinder(),
                contentVerifierProviderBuilder.build(certKey),
                (DigestCalculatorProvider) digestProvider));
    }

    /**
     * Build a Bouncy Castle {@link CMSSignedData} from bytes.
     *
     * @param signature the signature.
     * @param data the data signed.
     * @return a CMS signed data.
     * @throws GeneralSecurityException if the signature could not be decoded.
     */
    public static CMSSignedData getSignedData(byte[] signature, byte[] data) throws GeneralSecurityException
    {
        CMSSignedData signedData;
        try {
            if (data != null) {
                signedData = new CMSSignedData(new CMSProcessableByteArray(data), signature);
            } else {
                signedData = new CMSSignedData(signature);
            }
        } catch (CMSException e) {
            throw new GeneralSecurityException("Unable to decode signature", e);
        }
        return signedData;
    }

    /**
     * Create a new {@link org.xwiki.crypto.signer.param.CMSSignedDataVerified} for the given signed data.
     *
     * The verified data is filled with the signed data content, content type, and certificates.
     *
     * @param signedData the signed data about to be verified.
     * @param factory a certificate factory to be used for certificates conversion.
     * @return a new verified signed data to be completed with the signature verifications.
     */
    public static BcCMSSignedDataVerified getCMSSignedDataVerified(CMSSignedData signedData,
        CertificateFactory factory)
    {
        BcCMSSignedDataVerified verifiedData = new BcCMSSignedDataVerified(signedData.getSignedContentTypeOID(),
            (signedData.getSignedContent() != null ? (byte[]) signedData.getSignedContent().getContent() : null));

        BcStoreUtils.addCertificatesToVerifiedData(signedData.getCertificates(), verifiedData, factory);
        return verifiedData;
    }

    static Collection<SignerInformation> getSigners(CMSSignedData signedData)
    {
        return signedData.getSignerInfos().getSigners();
    }
}
