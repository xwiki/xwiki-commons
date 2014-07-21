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
package org.xwiki.crypto.pkix.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.xwiki.crypto.internal.asymmetric.BcPublicKeyParameters;
import org.xwiki.crypto.params.cipher.asymmetric.PublicKeyParameters;
import org.xwiki.crypto.pkix.internal.extension.BcX509Extensions;
import org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509Extensions;
import org.xwiki.crypto.signer.Signer;
import org.xwiki.crypto.signer.SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSignerFactory;

/**
 * Generic implementation of X509CertifiedPublicKey wrapping a Bouncy Castle holder.
 *
 * @version $Id$
 * @since 5.4
 */
public class BcX509CertifiedPublicKey implements X509CertifiedPublicKey
{
    private final X509CertificateHolder holder;

    private final SignerFactory signerFactory;

    BcX509CertifiedPublicKey(X509CertificateHolder holder, SignerFactory signerFactory)
    {
        this.holder = holder;
        this.signerFactory = signerFactory;
    }

    /**
     * @return the native bouncy castle wrapped holder.
     */
    public X509CertificateHolder getX509CertificateHolder()
    {
        return this.holder;
    }

    @Override
    public DistinguishedName getIssuer()
    {
        return new DistinguishedName(this.holder.getIssuer());
    }

    @Override
    public DistinguishedName getSubject()
    {
        return new DistinguishedName(this.holder.getSubject());
    }

    @Override
    public Date getNotAfter()
    {
        return this.holder.getNotAfter();
    }

    @Override
    public Date getNotBefore()
    {
        return this.holder.getNotBefore();
    }

    @Override
    public int getVersionNumber()
    {
        return this.holder.getVersionNumber();
    }

    @Override
    public BigInteger getSerialNumber()
    {
        return this.holder.getSerialNumber();
    }

    @Override
    public boolean isValidOn(Date date)
    {
        return this.holder.isValidOn(date);
    }

    @Override
    public boolean isRootCA()
    {
        X509Extensions exts = this.getExtensions();
        if (exts != null) {
            return exts.hasCertificateAuthorityBasicConstraints() && isSelfSigned();
        }
        return isSelfSigned();
    }

    @Override
    public X509Extensions getExtensions()
    {
        Extensions extensions = this.holder.getExtensions();
        return (extensions != null) ? new BcX509Extensions(extensions) : null;
    }

    @Override
    public byte[] getAuthorityKeyIdentifier()
    {
        X509Extensions exts = this.getExtensions();
        if (exts == null) {
            return null;
        }
        return exts.getAuthorityKeyIdentifier();
    }

    @Override
    public byte[] getSubjectKeyIdentifier()
    {
        X509Extensions exts = this.getExtensions();
        if (exts == null) {
            return null;
        }
        return exts.getSubjectKeyIdentifier();
    }

    @Override
    public PublicKeyParameters getPublicKeyParameters()
    {
        try {
            return new BcPublicKeyParameters(PublicKeyFactory.createKey(this.holder.getSubjectPublicKeyInfo()));
        } catch (IOException e) {
            // Very unlikely
            throw new UnsupportedOperationException("Unsupported public key encoding.", e);
        }
    }

    @Override
    public boolean isSignedBy(PublicKeyParameters publicKey) throws GeneralSecurityException
    {
        TBSCertificate tbsCert = this.holder.toASN1Structure().getTBSCertificate();

        if (!BcUtils.isAlgorithlIdentifierEqual(tbsCert.getSignature(), this.holder.getSignatureAlgorithm())) {
            return false;
        }

        Signer signer = null;

        // Optimisation
        if (this.signerFactory instanceof BcSignerFactory) {
            signer = ((BcSignerFactory) this.signerFactory).getInstance(false, publicKey, tbsCert.getSignature());
        } else {
            try {
                signer =
                    this.signerFactory.getInstance(false, publicKey, this.holder.getSignatureAlgorithm().getEncoded());
            } catch (IOException e) {
                return false;
            }
        }

        try {
            return BcUtils.updateDEREncodedObject(signer, tbsCert).verify(this.holder.getSignature());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isSelfSigned()
    {
        X509Extensions exts = this.getExtensions();
        if (exts != null) {
            byte[] issuerId = exts.getAuthorityKeyIdentifier();
            byte[] subjectId = exts.getSubjectKeyIdentifier();
            if (issuerId != null) {
                return Arrays.equals(issuerId, subjectId);
            }
        }
        return getIssuer().equals(getSubject());
    }

    @Override
    public byte[] getEncoded() throws IOException
    {
        return this.holder.getEncoded();
    }

    /**
     * {@inheritDoc}
     *
     * @since 6.0M1
     */
    @Override
    public boolean equals(Object cert)
    {
        if (this == cert) {
            return true;
        }
        if (cert == null || !(cert instanceof X509CertifiedPublicKey)) {
            return false;
        }

        X509CertifiedPublicKey that = (X509CertifiedPublicKey) cert;

        X509Extensions thisExts = this.getExtensions();
        X509Extensions thatExts = that.getExtensions();

        byte[] thisId = (thisExts != null) ? thisExts.getSubjectKeyIdentifier() : null;
        byte[] thatId = (thatExts != null) ? thatExts.getSubjectKeyIdentifier() : null;

        if (thisId != null) {
            return Arrays.equals(thisId, thatId);
        } else if (thatExts != null) {
            return false;
        }

        return this.getIssuer().equals(that.getIssuer()) && this.getSerialNumber().equals(that.getSerialNumber());
    }

    /**
     * {@inheritDoc}
     *
     * @since 6.0M1
     */
    @Override
    public int hashCode()
    {
        X509Extensions exts = this.getExtensions();
        if (exts != null) {
            byte[] id = exts.getSubjectKeyIdentifier();
            if (id != null) {
                return Arrays.hashCode(id);
            }
        }
        return new HashCodeBuilder(3, 17).append(getIssuer()).append(getSerialNumber()).toHashCode();
    }
}
