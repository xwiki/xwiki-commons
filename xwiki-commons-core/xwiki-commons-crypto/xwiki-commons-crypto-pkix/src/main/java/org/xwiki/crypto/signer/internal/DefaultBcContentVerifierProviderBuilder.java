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

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.crypto.params.cipher.CipherParameters;
import org.xwiki.crypto.params.cipher.asymmetric.PublicKeyParameters;
import org.xwiki.crypto.pkix.internal.BcUtils;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSignerFactory;

/**
 * Bridge {@link ContentVerifierProvider} with XWiki components.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Component
@Singleton
public class DefaultBcContentVerifierProviderBuilder implements BcContentVerifierProviderBuilder
{
    @Inject
    private ComponentManager manager;

    @Override
    public ContentVerifierProvider build(final CertifiedPublicKey certificate)
    {
        return new ContentVerifierProvider()
        {
            @Override
            public boolean hasAssociatedCertificate()
            {
                return true;
            }

            @Override
            public X509CertificateHolder getAssociatedCertificate()
            {
                return BcUtils.getX509CertificateHolder(certificate);
            }

            @Override
            public ContentVerifier get(AlgorithmIdentifier algorithm)
            {
                return getInstance(certificate.getPublicKeyParameters(), algorithm);
            }
        };
    }

    @Override
    public ContentVerifierProvider build(final PublicKeyParameters publicKey)
    {
        return new ContentVerifierProvider()
        {
            @Override
            public boolean hasAssociatedCertificate()
            {
                return false;
            }

            @Override
            public X509CertificateHolder getAssociatedCertificate()
            {
                return null;
            }

            @Override
            public ContentVerifier get(AlgorithmIdentifier algorithm)
            {
                return getInstance(publicKey, algorithm);
            }
        };
    }

    private ContentVerifier getInstance(CipherParameters parameters, final AlgorithmIdentifier algId)
    {
        SignerFactory factory = getFactory(algId.getAlgorithm().getId());

        if (factory instanceof BcSignerFactory) {
            return (ContentVerifier) ((BcSignerFactory) factory).getInstance(false, parameters, algId);
        }

        final org.xwiki.crypto.signer.Signer signer;
        try {
            signer = factory.getInstance(false, parameters, algId.getEncoded());
        } catch (IOException e) {
            // Unlikely
            throw new IllegalArgumentException("Unable to encode algorithm identifier.");
        }

        return new ContentVerifier()
        {
            @Override
            public AlgorithmIdentifier getAlgorithmIdentifier()
            {
                return algId;
            }

            @Override
            public OutputStream getOutputStream()
            {
                return signer.getOutputStream();
            }

            @Override
            public boolean verify(byte[] bytes)
            {
                return DefaultBcContentVerifierProviderBuilder.verify(signer, bytes);
            }
        };
    }

    private static boolean verify(org.xwiki.crypto.signer.Signer signer, byte[] bytes)
    {
        try {
            return signer.verify(bytes);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    protected SignerFactory getFactory(String hint)
    {
        try {
            return this.manager.getInstance(SignerFactory.class, hint);
        } catch (ComponentLookupException e) {
            throw new UnsupportedOperationException("Signing algorithm not found.", e);
        }
    }
}
