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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.crypto.DigestFactory;
import org.xwiki.crypto.pkix.CertifyingSigner;
import org.xwiki.crypto.pkix.internal.BcUtils;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.param.CMSSignedDataGeneratorParameters;
import org.xwiki.crypto.signer.param.CMSSignerInfo;

/**
 * Default implementation of {@link CMSSignedDataGenerator} based on Bouncy Castle.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Component
@Singleton
public class DefaultCMSSignedDataGenerator implements org.xwiki.crypto.signer.CMSSignedDataGenerator, Initializable
{
    @Inject
    private DigestFactory digestProvider;

    @Override
    public void initialize() throws InitializationException
    {
        if (!(this.digestProvider instanceof DigestCalculatorProvider)) {
            throw new InitializationException("Incompatible DigestFactory for this signed data generator.");
        }
    }

    @Override
    public byte[] generate(byte[] data, CMSSignedDataGeneratorParameters parameters) throws GeneralSecurityException
    {
        return generate(data, parameters, false);
    }

    @Override
    public byte[] generate(byte[] data, CMSSignedDataGeneratorParameters parameters, boolean embedData)
        throws GeneralSecurityException
    {
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

        Collection<CMSSignerInfo> signersInfo = parameters.getSignatures();
        if (!signersInfo.isEmpty()) {
            // Add existing signatures
            List<SignerInformation> signers = new ArrayList<>(parameters.getSignatures().size());
            for (CMSSignerInfo signerInfo : parameters.getSignatures()) {
                if (!(signerInfo instanceof BcCMSSignerInfo)) {
                    throw new GeneralSecurityException("Incompatible pre-calculated signature for this signed data "
                        + "generator");
                }
                signers.add(((BcCMSSignerInfo) signerInfo).getSignerInfo());
            }
            generator.addSigners(new SignerInformationStore(signers));
        }

        try {
            // Add new signers
            Collection<CertifyingSigner> signers = parameters.getSigners();
            for (CertifyingSigner signer : signers) {
                if (signer.getAlgorithmIdentifier() == null) {
                    throw new GeneralSecurityException("Incompatible signer for this signed data generator for subject "
                        + signer.getCertifier().getSubject().getName());
                }

                generator.addSignerInfoGenerator(
                    new SignerInfoGeneratorBuilder((DigestCalculatorProvider) this.digestProvider)
                        .build(signer, BcUtils.getX509CertificateHolder(signer.getCertifier()))
                );
            }

            // Add certificates
            for (CertifiedPublicKey certifiedPublicKey : parameters.getCertificates()) {
                generator.addCertificate(BcUtils.getX509CertificateHolder(certifiedPublicKey));
            }

            return generator.generate(new CMSProcessableByteArray(data), embedData).getEncoded();
        } catch (CMSException e) {
            throw new GeneralSecurityException("Unable to generate CMS signature", e);
        } catch (OperatorCreationException e) {
            throw new GeneralSecurityException("Unable to prepare signers", e);
        } catch (IOException e) {
            throw new GeneralSecurityException("Unable to encode signed data", e);
        }
    }
}
