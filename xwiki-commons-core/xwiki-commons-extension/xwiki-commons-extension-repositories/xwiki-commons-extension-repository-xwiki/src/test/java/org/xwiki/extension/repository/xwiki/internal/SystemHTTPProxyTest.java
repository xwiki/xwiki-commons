package org.xwiki.extension.repository.xwiki.internal;

import static com.github.tomakehurst.wiremock.client.RequestPatternBuilder.allRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
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

@AllComponents
public class SystemHTTPProxyTest
{
    @Rule
    public WireMockRule proxyWireMockRule = new WireMockRule(8888);

    @Rule
    public MockitoComponentMockingRule<ExtensionRepositoryFactory> repositoryFactory =
        new MockitoComponentMockingRule<ExtensionRepositoryFactory>(XWikiExtensionRepositoryFactory.class);

    @Test
    public void testProxy() throws ClientProtocolException, IOException, ExtensionRepositoryException,
        ComponentLookupException, URISyntaxException
    {
        ExtensionRepository repository =
            this.repositoryFactory.getComponentUnderTest().createRepository(
                new DefaultExtensionRepositoryDescriptor("id", "xwiki", new URI("http://host")));

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
