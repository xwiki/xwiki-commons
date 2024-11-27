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
package org.xwiki.extension.repository.aether.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AetherExtensionRepositoryFactory}.
 *
 * @version $Id$
 */
@AllComponents
@ComponentTest
class HTTPTest
{
    @InjectMockComponents
    private AetherExtensionRepositoryFactory repositoryFactory;

    private WireMockServer wireMockServer;

    private String proxyHost;

    private String proxyPort;

    @BeforeEach
    void beforeEach()
    {
        this.wireMockServer = new WireMockServer(options().dynamicPort());
        this.wireMockServer.start();

        // Remember the standard system properties
        this.proxyHost = System.getProperty("http.proxyHost");
        this.proxyPort = System.getProperty("http.proxyPort");
    }

    @AfterEach
    void afterEach()
    {
        this.wireMockServer.stop();
        this.wireMockServer = null;

        // Cleanup any leftover in System properties
        if (this.proxyHost != null) {
            System.setProperty("http.proxyHost", this.proxyHost);
        } else {
            System.clearProperty("http.proxyHost");
        }
        if (this.proxyPort != null) {
            System.setProperty("http.proxyPort", this.proxyPort);
        } else {
            System.clearProperty("http.proxyPort");
        }
    }

    @Test
    void proxy() throws ExtensionRepositoryException, URISyntaxException
    {
        ExtensionRepository repository = this.repositoryFactory.createRepository(
            new DefaultExtensionRepositoryDescriptor("id", "maven", new URI("http://unknownhostforxwikitest")));

        // Simulate a remote server not responding.
        try {
            repository.resolveVersions("groupid:artifactid", 0, -1);
        } catch (ResolveException e) {
            // We don't really care if the target artifact exist
        }

        assertTrue(this.wireMockServer.findAll(allRequests()).isEmpty(),
            "The repository did not request the " + "proxy server");

        // Set the proxy to be wiremock so that it answers.
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", String.valueOf(this.wireMockServer.port()));

        try {
            repository.resolveVersions("groupid:artifactid", 0, -1);
        } catch (ResolveException e) {
            // We don't really care if the target artifact exist
        }

        assertFalse(this.wireMockServer.findAll(allRequests()).isEmpty(),
            "The repository did not request the proxy server");
    }

    @Test
    void credentials() throws ExtensionRepositoryException, URISyntaxException
    {
        ExtensionRepository repository =
            this.repositoryFactory.createRepository(new DefaultExtensionRepositoryDescriptor("id", "maven",
                new URI("http://localhost:" + this.wireMockServer.port()),
                Map.of("auth.user", "user", "auth.password", "password")));

        this.wireMockServer.stubFor(get(urlEqualTo("/groupid/artifactid/maven-metadata.xml"))
            .willReturn(aResponse().withStatus(401).withHeader("WWW-Authenticate", "Basic")));

        try {
            repository.resolveVersions("groupid:artifactid", 0, -1);
        } catch (ResolveException e) {
            // We don't really care if the target artifact exist
        }

        assertFalse(this.wireMockServer.findAll(allRequests()).isEmpty(),
            "The repository did not request the proxy server");

        this.wireMockServer.verify(getRequestedFor(urlEqualTo("/groupid/artifactid/maven-metadata.xml"))
            .withBasicAuth(new BasicCredentials("user", "password")));
    }
}
