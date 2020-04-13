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

import java.security.GeneralSecurityException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.crypto.pkix.CertificateChainBuilder;
import org.xwiki.crypto.pkix.CertificateProvider;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.X509CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.x509certificate.extension.KeyUsage;
import org.xwiki.crypto.pkix.params.x509certificate.extension.X509Extensions;

/**
 * X.509 implementation of {@link org.xwiki.crypto.pkix.CertificateChainBuilder} based on Bouncy Castle.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Component
@Singleton
@Named("X509")
public class BcX509CertificateChainBuilder implements CertificateChainBuilder
{
    @Override
    public Collection<CertifiedPublicKey> build(CertifiedPublicKey certificate, CertificateProvider provider)
    {
        if (certificate == null) {
            return null;
        }

        Deque<CertifiedPublicKey> result = new ArrayDeque<>();
        build(result, certificate, provider);
        return result;
    }

    private Collection<CertifiedPublicKey> build(Deque<CertifiedPublicKey> result, CertifiedPublicKey certificate,
        CertificateProvider provider)
    {
        if (result.contains(certificate)) {
            // Avoid circular references
            return result;
        }

        if (!(certificate instanceof X509CertifiedPublicKey)) {
            throw new IllegalArgumentException("Certificate of incompatible type ["
                + certificate.getClass().getName() + "] for subject [" + certificate.getSubject().getName() + "]");
        }

        result.push(certificate);

        CertifiedPublicKey issuer = getIssuer((X509CertifiedPublicKey) certificate, provider);

        return (issuer != null && !issuer.equals(certificate)) ? build(result, issuer, provider) : result;
    }

    private CertifiedPublicKey getIssuer(X509CertifiedPublicKey cert, CertificateProvider provider)
    {
        X509Extensions extensions = cert.getExtensions();

        if (extensions != null) {
            byte[] authKey = extensions.getAuthorityKeyIdentifier();
            if (authKey != null) {
                if (Arrays.equals(extensions.getSubjectKeyIdentifier(), authKey)) {
                    // Self-signed
                    return cert;
                }

                return validatedIssuer(cert, provider.getCertificate(authKey));
            }
        }

        Collection<CertifiedPublicKey> certs = provider.getCertificate(cert.getIssuer());
        if (certs != null) {
            for (CertifiedPublicKey issuerCert : certs) {
                CertifiedPublicKey issuer = validatedIssuer(cert, issuerCert);
                if (issuer != null) {
                    return issuer;
                }
            }
        }

        return null;
    }

    private CertifiedPublicKey validatedIssuer(X509CertifiedPublicKey cert, CertifiedPublicKey issuerCert)
    {
        if (issuerCert == null || !(issuerCert instanceof X509CertifiedPublicKey)) {
            return null;
        }

        X509CertifiedPublicKey issuer = (X509CertifiedPublicKey) issuerCert;

        if (issuer.getVersionNumber() == 3) {
            X509Extensions extensions = issuer.getExtensions();
            if (extensions == null || !extensions.hasCertificateAuthorityBasicConstraints()) {
                return null;
            }

            EnumSet<KeyUsage> usage = extensions.getKeyUsage();
            if (!usage.contains(KeyUsage.keyCertSign)) {
                return null;
            }
        }

        try {
            return cert.isSignedBy(issuer.getPublicKeyParameters()) ? issuer : null;
        } catch (GeneralSecurityException e) {
            return null;
        }
    }
}
