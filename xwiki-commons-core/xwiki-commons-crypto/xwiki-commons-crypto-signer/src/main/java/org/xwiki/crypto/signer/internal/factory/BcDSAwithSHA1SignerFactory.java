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
package org.xwiki.crypto.signer.internal.factory;

import javax.inject.Singleton;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.DSASigner;
import org.xwiki.component.annotation.Component;
import org.xwiki.crypto.params.cipher.asymmetric.AsymmetricCipherParameters;

/**
 * Factory for Digital Signature Algorithm with SHA-1 Digest.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Component(hints = { "DSAwithSHA1", "1.2.840.10040.4.3" })
@Singleton
public class BcDSAwithSHA1SignerFactory extends AbstractBcSignerFactory
{
    private static final AlgorithmIdentifier ALG_ID =
        new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa_with_sha1, DERNull.INSTANCE);

    @Override
    protected org.bouncycastle.crypto.Signer getSignerInstance(AsymmetricCipherParameters parameters)
    {
        return new DSADigestSigner(new DSASigner(), new SHA1Digest());
    }

    @Override
    protected AlgorithmIdentifier getSignerAlgorithmIdentifier(AsymmetricCipherParameters parameters)
    {
        return ALG_ID;
    }
}
