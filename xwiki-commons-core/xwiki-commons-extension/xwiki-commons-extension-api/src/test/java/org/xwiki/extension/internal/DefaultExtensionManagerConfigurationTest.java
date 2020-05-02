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
package org.xwiki.extension.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit test for {@link DefaultExtensionManagerConfiguration}.
 *
 * @version $Id$
 */
@ComponentTest
// @formatter:off
@ComponentList({
    ExtensionFactory.class 
})
// @formatter:on
public class DefaultExtensionManagerConfigurationTest
{
    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private DefaultExtensionManagerConfiguration configuration;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private MemoryConfigurationSource source;

    @BeforeComponent
    public void registerComponents() throws Exception
    {
        // Register some in-memory Configuration Source for the test
        this.source = this.componentManager.registerMemoryConfigurationSource();
    }

    @Test
    void getRepositoriesWithInvalid() throws Exception
    {
        // We define 2 repositories: a valid one and an invalid one.
        // The goal is to verify that the invalid one is ignored but a warning is reported in the logs.
        this.source.setProperty("extension.repositories", Arrays.asList("id:type:http://url", "invalid"));

        assertEquals(
            Arrays.asList(new DefaultExtensionRepositoryDescriptor("id", "type", new URI("http://url"))),
            new ArrayList<>(this.configuration.getExtensionRepositoryDescriptors()));
        assertEquals(1, this.logCapture.size());
        assertEquals("Ignoring invalid repository configuration [invalid]. Root cause "
            + "[ExtensionManagerConfigurationException: Invalid repository configuration format for [invalid]. Should "
            + "have been matching [([^:]+):([^:]+):(.+)].]", this.logCapture.getMessage(0));
    }

    @Test
    void getExtensionRepositoryDescriptorsEmpty()
    {
        assertNull(this.configuration.getExtensionRepositoryDescriptors());
        assertEquals(0, this.logCapture.size());
    }

    @Test
    void getExtensionRepositoryDescriptorsWithProperties() throws URISyntaxException
    {
        this.source.setProperty("extension.repositories", Arrays.asList("id:type:http://url"));
        this.source.setProperty("extension.repositories.id.property", "value");
        this.source.setProperty("extension.repositories.id.property.with.dots", "other value");

        Collection<ExtensionRepositoryDescriptor> descriptors = this.configuration.getExtensionRepositoryDescriptors();

        assertFalse(descriptors.isEmpty());

        ExtensionRepositoryDescriptor descriptor = descriptors.iterator().next();

        assertEquals("id", descriptor.getId());
        assertEquals("type", descriptor.getType());
        assertEquals(new URI("http://url"), descriptor.getURI());
        assertEquals("value", descriptor.getProperty("property"));
        assertEquals("other value", descriptor.getProperty("property.with.dots"));
        assertEquals(0, this.logCapture.size());
    }
}
