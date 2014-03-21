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

import java.util.ArrayList;
import java.util.Collection;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.CollectionStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.crypto.AbstractPKIXTest;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcDSAKeyFactory;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA1DigestFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.pkix.CertificateChainBuilder;
import org.xwiki.crypto.pkix.CertificateFactory;
import org.xwiki.crypto.pkix.CertificateProvider;
import org.xwiki.crypto.pkix.params.CertifiedPublicKey;
import org.xwiki.crypto.signer.internal.factory.BcDSAwithSHA1SignerFactory;
import org.xwiki.crypto.signer.internal.factory.BcSHA1withRsaSignerFactory;
import org.xwiki.crypto.signer.internal.factory.DefaultSignerFactory;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@ComponentList({Base64BinaryStringEncoder.class, BcRSAKeyFactory.class, BcDSAKeyFactory.class,
    BcSHA1DigestFactory.class, BcSHA1withRsaSignerFactory.class, BcDSAwithSHA1SignerFactory.class,
    DefaultSignerFactory.class, BcStoreX509CertificateProvider.class, BcX509CertificateFactory.class})
public class BcX509CertificateChainBuilderTest extends AbstractPKIXTest
{
    @Rule
    public final MockitoComponentMockingRule<CertificateChainBuilder> mocker =
        new MockitoComponentMockingRule<CertificateChainBuilder>(BcX509CertificateChainBuilder.class);

    private CertificateChainBuilder builder;
    private CertifiedPublicKey v1CaCert;
    private CertifiedPublicKey v1Cert;
    private CertifiedPublicKey v3CaCert;
    private CertifiedPublicKey v3InterCaCert;
    private CertifiedPublicKey v3Cert;

    public void setupTest(MockitoComponentMockingRule<CertificateChainBuilder> mocker) throws Exception
    {
        BinaryStringEncoder base64encoder = mocker.getInstance(BinaryStringEncoder.class, "Base64");
        CertificateFactory certFactory = mocker.getInstance(CertificateFactory.class, "X509");
        v1CaCert = certFactory.decode(base64encoder.decode(V1_CA_CERT));
        v1Cert = certFactory.decode(base64encoder.decode(V1_CERT));
        v3CaCert = certFactory.decode(base64encoder.decode(V3_CA_CERT));
        v3InterCaCert = certFactory.decode(base64encoder.decode(V3_ITERCA_CERT));
        v3Cert = certFactory.decode(base64encoder.decode(V3_CERT));
    }

    @Before
    public void configure() throws Exception
    {
        builder = mocker.getComponentUnderTest();
        setupTest(mocker);
    }

    @Test
    public void testValidV3CertificatePath() throws Exception
    {
        Collection<X509CertificateHolder> certs = new ArrayList<X509CertificateHolder>();
        certs.add(BcUtils.getX509CertificateHolder(v3CaCert));
        certs.add(BcUtils.getX509CertificateHolder(v3InterCaCert));

        CollectionStore store = new CollectionStore(certs);
        CertificateProvider provider = mocker.getInstance(CertificateProvider.class, "BCStoreX509");
        ((BcStoreX509CertificateProvider) provider).setStore(store);

        Collection<CertifiedPublicKey> chain = builder.build(v3Cert, provider);

        assertThat(chain, contains(v3CaCert, v3InterCaCert, v3Cert));
    }

    @Test
    public void testIncompleteV3CertificatePath() throws Exception
    {
        Collection<X509CertificateHolder> certs = new ArrayList<X509CertificateHolder>();
        certs.add(BcUtils.getX509CertificateHolder(v3InterCaCert));

        CollectionStore store = new CollectionStore(certs);
        CertificateProvider provider = mocker.getInstance(CertificateProvider.class, "BCStoreX509");
        ((BcStoreX509CertificateProvider) provider).setStore(store);

        Collection<CertifiedPublicKey> chain = builder.build(v3Cert, provider);

        assertThat(chain, contains(v3InterCaCert, v3Cert));
    }

    @Test
    public void testBrokenV3CertificatePath() throws Exception
    {
        Collection<X509CertificateHolder> certs = new ArrayList<X509CertificateHolder>();
        certs.add(BcUtils.getX509CertificateHolder(v3CaCert));

        CollectionStore store = new CollectionStore(certs);
        CertificateProvider provider = mocker.getInstance(CertificateProvider.class, "BCStoreX509");
        ((BcStoreX509CertificateProvider) provider).setStore(store);

        Collection<CertifiedPublicKey> chain = builder.build(v3Cert, provider);

        assertThat(chain, contains(v3Cert));
    }

    @Test
    public void testValidV1CertificatePath() throws Exception
    {
        Collection<X509CertificateHolder> certs = new ArrayList<X509CertificateHolder>();
        certs.add(BcUtils.getX509CertificateHolder(v1CaCert));

        CollectionStore store = new CollectionStore(certs);
        CertificateProvider provider = mocker.getInstance(CertificateProvider.class, "BCStoreX509");
        ((BcStoreX509CertificateProvider) provider).setStore(store);

        Collection<CertifiedPublicKey> chain = builder.build(v1Cert, provider);

        assertThat(chain, contains(v1CaCert, v1Cert));
    }

    @Test
    public void testIncompleteV1CertificatePath() throws Exception
    {
        CertificateProvider provider = mocker.getInstance(CertificateProvider.class, "BCStoreX509");

        Collection<CertifiedPublicKey> chain = builder.build(v1Cert, provider);

        assertThat(chain, contains(v1Cert));
    }

}
