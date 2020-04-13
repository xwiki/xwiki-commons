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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.CertificateProvider;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.pkix.params.PrincipalIndentifier;

/**
 * Adapter of a Bouncy Castle {@link Store} to a {@link CertificateProvider}.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Component
@Named("BCStoreX509")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class BcStoreX509CertificateProvider implements CertificateProvider
{
    @Inject
    @Named("X509")
    private CertificateFactory factory;

    private Store store;

    /**
     * Set the store this adapter will delegate to. If no store is set, the adapter does not return any certificates.
     *
     * @param store the store to wrap.
     */
    public void setStore(Store store)
    {
        this.store = store;
    }

    /**
     * Get the first certificate matching the provided selector.
     *
     * @param selector the selector.
     * @return a certificate holder.
     */
    public X509CertificateHolder getCertificate(Selector selector)
    {
        try {
            return (X509CertificateHolder) this.store.getMatches(selector).iterator().next();
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public CertifiedPublicKey getCertificate(byte[] keyIdentifier)
    {
        return BcUtils.convertCertificate(this.factory, getCertificate(new SignerId(keyIdentifier)));
    }

    @Override
    public CertifiedPublicKey getCertificate(PrincipalIndentifier issuer, BigInteger serial)
    {
        return BcUtils.convertCertificate(this.factory,
            getCertificate(new SignerId(BcUtils.getX500Name(issuer), serial)));
    }

    @Override
    public CertifiedPublicKey getCertificate(PrincipalIndentifier issuer, BigInteger serial, byte[] keyIdentifier)
    {
        return BcUtils.convertCertificate(this.factory,
            getCertificate(new SignerId(BcUtils.getX500Name(issuer), serial, keyIdentifier)));
    }

    @Override
    public Collection<CertifiedPublicKey> getCertificate(PrincipalIndentifier subject)
    {
        AttributeCertificateHolder selector = new AttributeCertificateHolder(BcUtils.getX500Name(subject));

        try {
            Collection<?> matches = this.store.getMatches(selector);
            Collection<CertifiedPublicKey> result = new ArrayList<>(matches.size());
            for (Object holder : matches) {
                if (holder instanceof X509CertificateHolder) {
                    result.add(BcUtils.convertCertificate(this.factory, (X509CertificateHolder) holder));
                }
            }
            return (!result.isEmpty()) ? result : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
