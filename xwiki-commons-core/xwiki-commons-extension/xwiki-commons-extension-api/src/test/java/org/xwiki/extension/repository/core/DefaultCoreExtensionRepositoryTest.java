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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.environment.Environment;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.test.ConfigurableDefaultCoreExtensionRepository;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;

@AllComponents
public class DefaultCoreExtensionRepositoryTest
{
    @Rule
    public MockitoComponentMockingRule<CoreExtensionRepository> mocker =
        new MockitoComponentMockingRule<CoreExtensionRepository>(ConfigurableDefaultCoreExtensionRepository.class);

    private ConfigurableDefaultCoreExtensionRepository coreExtensionRepository;

    @Before
    public void before() throws Exception
    {
        this.mocker.registerMockComponent(Environment.class);

        this.coreExtensionRepository =
            (ConfigurableDefaultCoreExtensionRepository) this.mocker.getInstance(CoreExtensionRepository.class);
    }

    /**
     * Validate core extension loading and others initializations.
     */
    @Test
    public void testInit()
    {
        Assert.assertTrue(this.coreExtensionRepository.countExtensions() > 0);
    }

    /**
     * Validate {@link CoreExtensionRepository#getCoreExtension(String)}
     */
    @Test
    public void testGetCoreExtension()
    {
        Assert.assertNull(this.coreExtensionRepository.getCoreExtension("unexistingextension"));

        this.coreExtensionRepository.addExtensions("existingextension", new DefaultVersion("version"));

        Extension extension = this.coreExtensionRepository.getCoreExtension("existingextension");

        Assert.assertNotNull(extension);
        Assert.assertEquals("existingextension", extension.getId().getId());
        Assert.assertEquals("version", extension.getId().getVersion().getValue());
    }

    /**
     * Validate {@link CoreExtensionRepository#resolve(ExtensionId)}
     */
    @Test
    public void testResolve() throws ResolveException
    {
        try {
            this.coreExtensionRepository.resolve(new ExtensionId("unexistingextension", "version"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        this.coreExtensionRepository.addExtensions("existingextension", new DefaultVersion("version"));

        try {
            this.coreExtensionRepository.resolve(new ExtensionId("existingextension", "wrongversion"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension = this.coreExtensionRepository.resolve(new ExtensionId("existingextension", "version"));

        Assert.assertNotNull(extension);
        Assert.assertEquals("existingextension", extension.getId().getId());
        Assert.assertEquals("version", extension.getId().getVersion().getValue());
    }

    /**
     * Make sure only one result is returned for an extension having several features.
     * 
     * @throws SearchException
     */
    @Test
    public void testSearchWithSeveralFeatures() throws SearchException
    {
        this.coreExtensionRepository.addExtensions("extension", new DefaultVersion("version"),
            new ExtensionId("testfeature1"), new ExtensionId("testfeature2"));

        IterableResult<Extension> result = this.coreExtensionRepository.search("testfeature", 0, -1);

        assertEquals(1, result.getTotalHits());
        assertEquals(1, result.getSize());
    }
}
