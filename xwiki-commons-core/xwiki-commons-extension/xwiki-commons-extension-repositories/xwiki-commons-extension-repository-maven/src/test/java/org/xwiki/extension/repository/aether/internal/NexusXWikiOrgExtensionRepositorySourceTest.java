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
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.maven.internal.MavenUtils;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NexusXWikiOrgExtensionRepositorySource}.
 *
 * @version $Id$
 */
@ComponentTest
class NexusXWikiOrgExtensionRepositorySourceTest
{
    @InjectMockComponents
    private NexusXWikiOrgExtensionRepositorySource source;

    @MockComponent
    private ExtensionManagerConfiguration configuration;

    @Test
    void getExtensionRepositoryDescriptorsWhenNoneIsConfigured() throws Exception
    {
        when(this.configuration.getExtensionRepositoryDescriptors()).thenReturn(null);

        Collection<ExtensionRepositoryDescriptor> repositories = this.source.getExtensionRepositoryDescriptors();

        assertEquals(1, repositories.size());
        ExtensionRepositoryDescriptor repository = repositories.iterator().next();
        assertEquals("maven-xwiki", repository.getId());
        assertEquals("maven", repository.getType());
        assertEquals(new URI("https://nexus.xwiki.org/nexus/content/groups/public"), repository.getURI());
        // The public group must not be used to resolve snapshots.
        assertEquals("false", repository.getProperty(MavenUtils.REPOSITORY_PROPERTY_SNAPSHOT));
    }

    @Test
    void getExtensionRepositoryDescriptorsWhenSomeAreConfigured() throws Exception
    {
        // The configured repositories are provided by another source, this one only provides the default.
        when(this.configuration.getExtensionRepositoryDescriptors()).thenReturn(
            List.of(new DefaultExtensionRepositoryDescriptor("other", "maven", new URI("https://example.org/maven"))));

        assertTrue(this.source.getExtensionRepositoryDescriptors().isEmpty());
    }

    @Test
    void getPriority()
    {
        assertEquals(NexusXWikiOrgExtensionRepositorySource.PRIORITY, this.source.getPriority());
    }
}
