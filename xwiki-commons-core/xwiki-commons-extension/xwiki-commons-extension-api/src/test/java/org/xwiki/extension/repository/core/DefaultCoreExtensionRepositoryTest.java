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
package org.xwiki.extension.repository.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xwiki.environment.Environment;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.extension.repository.internal.core.ExtensionScanner;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.test.ConfigurableDefaultCoreExtensionRepository;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.test.annotation.AfterComponent;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Validate {@link DefaultCoreExtensionRepository}.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class DefaultCoreExtensionRepositoryTest
{
    @MockComponent
    private Environment environment;

    @MockComponent
    @Named("test")
    private ExtensionScanner scanner;

    @InjectMockComponents
    private ConfigurableDefaultCoreExtensionRepository coreExtensionRepository;

    @AfterComponent
    void afterComponent()
    {
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                Map<String, DefaultCoreExtension> extensions = invocation.getArgument(0);

                extensions.put("id1", new DefaultCoreExtension(coreExtensionRepository, new URL("http://url1"),
                    new ExtensionId("id1", "version1"), "type1"));
                extensions.put("id2", new DefaultCoreExtension(coreExtensionRepository, new URL("http://url2"),
                    new ExtensionId("id2", "version2"), "type2"));

                return null;
            }
        }).when(this.scanner).scanJARs(any(Map.class), any(Collection.class),
            any(DefaultCoreExtensionRepository.class));

        doAnswer(new Answer<DefaultCoreExtension>()
        {
            @Override
            public DefaultCoreExtension answer(InvocationOnMock invocation) throws Throwable
            {
                return new DefaultCoreExtension(coreExtensionRepository, new URL("http://urlE"),
                    new ExtensionId("idE", "versionE"), "typeE");
            }
        }).when(this.scanner).scanEnvironment(any(DefaultCoreExtensionRepository.class));
    }

    /**
     * Validate core extension loading and others initializations.
     */
    @Test
    void init() throws MalformedURLException
    {
        assertEquals(3, this.coreExtensionRepository.countExtensions());

        assertEquals("id1", this.coreExtensionRepository.getCoreExtension("id1").getId().getId());
        assertEquals("version1", this.coreExtensionRepository.getCoreExtension("id1").getId().getVersion().getValue());
        assertEquals(new URL("http://url1"), this.coreExtensionRepository.getCoreExtension("id1").getURL());
        assertEquals("type1", this.coreExtensionRepository.getCoreExtension("id1").getType());

        assertEquals("id2", this.coreExtensionRepository.getCoreExtension("id2").getId().getId());
        assertEquals("version2", this.coreExtensionRepository.getCoreExtension("id2").getId().getVersion().getValue());
        assertEquals(new URL("http://url2"), this.coreExtensionRepository.getCoreExtension("id2").getURL());
        assertEquals("type2", this.coreExtensionRepository.getCoreExtension("id2").getType());

        CoreExtension environmentExtension = this.coreExtensionRepository.getEnvironmentExtension();

        assertEquals("idE", environmentExtension.getId().getId());
        assertEquals("versionE", environmentExtension.getId().getVersion().getValue());
        assertEquals(new URL("http://urlE"), environmentExtension.getURL());
        assertEquals("typeE", environmentExtension.getType());
    }

    /**
     * Validate {@link CoreExtensionRepository#getCoreExtension(String)}
     */
    @Test
    void getCoreExtension()
    {
        assertNull(this.coreExtensionRepository.getCoreExtension("unexistingextension"));

        this.coreExtensionRepository.addExtensions("existingextension", new DefaultVersion("version"));

        Extension extension = this.coreExtensionRepository.getCoreExtension("existingextension");

        assertNotNull(extension);
        assertEquals("existingextension", extension.getId().getId());
        assertEquals("version", extension.getId().getVersion().getValue());
    }

    /**
     * Validate {@link CoreExtensionRepository#resolve(ExtensionId)}
     */
    @Test
    void resolve() throws ResolveException
    {
        try {
            this.coreExtensionRepository.resolve(new ExtensionId("unexistingextension", "version"));

            fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        this.coreExtensionRepository.addExtensions("existingextension", new DefaultVersion("version"));

        try {
            this.coreExtensionRepository.resolve(new ExtensionId("existingextension", "wrongversion"));

            fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension = this.coreExtensionRepository.resolve(new ExtensionId("existingextension", "version"));

        assertNotNull(extension);
        assertEquals("existingextension", extension.getId().getId());
        assertEquals("version", extension.getId().getVersion().getValue());
    }

    /**
     * Make sure only one result is returned for an extension having several features.
     *
     * @throws SearchException
     */
    @Test
    void searchWithSeveralFeatures() throws SearchException
    {
        this.coreExtensionRepository.addExtensions("extension", new DefaultVersion("version"),
            new ExtensionId("testfeature1"), new ExtensionId("testfeature2"));

        IterableResult<Extension> result = this.coreExtensionRepository.search("testfeature", 0, -1);

        assertEquals(1, result.getTotalHits());
        assertEquals(1, result.getSize());
    }
}
