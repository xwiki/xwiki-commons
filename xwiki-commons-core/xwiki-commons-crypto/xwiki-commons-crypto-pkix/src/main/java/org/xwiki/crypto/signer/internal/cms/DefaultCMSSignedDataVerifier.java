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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.crypto.DigestFactory;
import org.xwiki.crypto.pkix.CertificateChainBuilder;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.CertificateProvider;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.CMSSignedDataVerifier;
import org.xwiki.crypto.signer.internal.BcContentVerifierProviderBuilder;
import org.xwiki.crypto.signer.param.CMSSignedDataVerified;

/**
 * Default implementation of {@link CMSSignedDataVerifier} based on Bouncy Castle.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Component
@Singleton
public class DefaultCMSSignedDataVerifier implements CMSSignedDataVerifier, Initializable
{
    @Inject
    private DigestFactory digestProvider;

    @Inject
    private BcContentVerifierProviderBuilder contentVerifierProviderBuilder;

    @Inject
    @Named("X509")
    private CertificateFactory certFactory;

    @Inject
    @Named("X509")
    private CertificateChainBuilder chainBuilder;

    @Inject
    private ComponentManager manager;

    @Override
    public void initialize() throws InitializationException
    {
        if (!(digestProvider instanceof DigestCalculatorProvider)) {
            throw new InitializationException("Incompatible DigestFactory for this signed data verifier.");
        }
    }

    @Override
    public CMSSignedDataVerified verify(byte[] signature) throws GeneralSecurityException
    {
        return verify(signature, null, (CertificateProvider) null);
    }

    @Override
    public CMSSignedDataVerified verify(byte[] signature, Collection<CertifiedPublicKey> certificates)
        throws GeneralSecurityException
    {
        return verify(signature, null, certificates);
    }

    @Override
    public CMSSignedDataVerified verify(byte[] signature, CertificateProvider certificateProvider)
        throws GeneralSecurityException
    {
        return verify(signature, null, certificateProvider);
    }

    @Override
    public CMSSignedDataVerified verify(byte[] signature, byte[] data) throws GeneralSecurityException
    {
        return verify(signature, data, (CertificateProvider) null);
    }

    @Override
    public CMSSignedDataVerified verify(byte[] signature, byte[] data,
        Collection<CertifiedPublicKey> certificates) throws GeneralSecurityException
    {
        return verify(signature, data, BcStoreUtils.getCertificateProvider(manager, certificates));
    }

    @Override
    public CMSSignedDataVerified verify(byte[] signature, byte[] data, CertificateProvider certificateProvider)
        throws GeneralSecurityException
    {
        CMSSignedData signedData = BcCMSUtils.getSignedData(signature, data);

        CertificateProvider provider = BcStoreUtils.getCertificateProvider(manager, signedData.getCertificates(),
            certificateProvider);

        return verify(signedData, provider);
    }

    private CMSSignedDataVerified verify(CMSSignedData signedData, CertificateProvider provider)
    {
        BcCMSSignedDataVerified verifiedData = BcCMSUtils.getCMSSignedDataVerified(signedData, certFactory);

        for (SignerInformation signer : BcCMSUtils.getSigners(signedData)) {
            CertifiedPublicKey certKey = BcStoreUtils.getCertificate(provider, signer, certFactory);

            try {
                verifiedData.addSignature(
                    new BcCMSSignerVerifiedInformation(signer,
                        BcCMSUtils.verify(signer, certKey, contentVerifierProviderBuilder, digestProvider),
                        chainBuilder.build(certKey, provider)
                    ));
            } catch (CMSException e) {
                verifiedData.addSignature(
                    new BcCMSSignerVerifiedInformation(signer,
                        false,
                        chainBuilder.build(certKey, provider)));
            }
        }

        return verifiedData;
    }
}
