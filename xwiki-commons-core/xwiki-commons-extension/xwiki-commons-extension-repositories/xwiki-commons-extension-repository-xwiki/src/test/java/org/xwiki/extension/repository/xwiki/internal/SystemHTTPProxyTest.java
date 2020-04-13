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
package org.xwiki.extension.repository.xwiki.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.extension.repository.ExtensionRepositoryFactory;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.RequestPatternBuilder.allRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@AllComponents
public class SystemHTTPProxyTest
{
    @Rule
    public WireMockRule proxyWireMockRule = new WireMockRule(8888);

    @Rule
    public MockitoComponentMockingRule<ExtensionRepositoryFactory> repositoryFactory =
        new MockitoComponentMockingRule<>(XWikiExtensionRepositoryFactory.class);

    @Test
    public void testProxy() throws ExtensionRepositoryException, ComponentLookupException, URISyntaxException
    {
        ExtensionRepository repository = this.repositoryFactory.getComponentUnderTest()
            .createRepository(new DefaultExtensionRepositoryDescriptor("id", "xwiki", new URI("http://host")));

        try {
            repository.resolveVersions("id", 0, -1);
        } catch (ResolveException e) {
            // We don't really care if the target artifact exist
        }

        assertTrue("The repository did not requested the proxy server", findAll(allRequests()).isEmpty());

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8888");

        try {
            repository.resolveVersions("id", 0, -1);
        } catch (ResolveException e) {
            // We don't really care if the target artifact exist
        }

        assertFalse("The repository did not requested the proxy server", findAll(allRequests()).isEmpty());
    }
}
