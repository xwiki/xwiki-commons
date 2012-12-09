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
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.logback.internal.DefaultLoggerManager;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

@ComponentList({
    DefaultExtensionManagerConfiguration.class,
    DefaultLoggerManager.class,
    DefaultObservationManager.class
})
public class DefaultExtensionManagerConfigurationTest
{
    @Rule
    public final MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private ExtensionManagerConfiguration configuration;

    private MemoryConfigurationSource source;

    @BeforeComponent
    public void registerComponents() throws Exception
    {
        // Register a Mocked Environment since we need to provide one.
        this.componentManager.registerMockComponent(Environment.class);

        // Register some in-memory Configuration Source for the test
        this.source = this.componentManager.registerMemoryConfigurationSource();
    }

    @Before
    public void setUp() throws Exception
    {
        this.configuration = this.componentManager.getInstance(ExtensionManagerConfiguration.class);
    }

    @Test
    public void testGetRepositoriesWithInvalid() throws ComponentLookupException, Exception
    {
        // Expect warnings to be logged
        LoggerManager logManager = this.componentManager.getInstance(LoggerManager.class);
        logManager.pushLogListener(null);

        this.source.setProperty("extension.repositories", Arrays.asList("id:type:http://url", "invalid"));

        Assert.assertEquals(
            Arrays.asList(new DefaultExtensionRepositoryDescriptor("id", "type", new URI("http://url"))),
            new ArrayList<ExtensionRepositoryDescriptor>(this.configuration.getExtensionRepositoryDescriptors()));
    }

    @Test
    public void testGetExtensionRepositoryDescriptorsEmpty()
    {
        Assert.assertEquals(null, this.configuration.getExtensionRepositoryDescriptors());
    }
}
