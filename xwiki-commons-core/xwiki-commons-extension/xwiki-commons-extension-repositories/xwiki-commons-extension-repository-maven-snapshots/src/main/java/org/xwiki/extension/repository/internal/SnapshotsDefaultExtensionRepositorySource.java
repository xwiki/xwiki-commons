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
package org.xwiki.extension.repository.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.repository.AbstractExtensionRepositorySource;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.aether.internal.NexusXWikiOrgExtensionRepositorySource;

/**
 * Provides the XWiki snapshots repository as default maven repository.
 *
 * @version $Id$
 * @since 6.2M1
 */
@Component
@Singleton
@Named("default.snapshots")
public class SnapshotsDefaultExtensionRepositorySource extends AbstractExtensionRepositorySource
{
    /**
     * Used to get configuration properties containing repositories.
     */
    @Inject
    private ExtensionManagerConfiguration configuration;

    @Override
    public int getPriority()
    {
        // Test snapshots repository just before release one to reduce loading on nexus.xwiki.org
        return NexusXWikiOrgExtensionRepositorySource.PRIORITY - 1;
    }

    @Override
    public Collection<ExtensionRepositoryDescriptor> getExtensionRepositoryDescriptors()
    {
        Collection<ExtensionRepositoryDescriptor> configuredRepositories =
            this.configuration.getExtensionRepositoryDescriptors();

        Collection<ExtensionRepositoryDescriptor> newRepositories = new ArrayList<>();

        if (configuredRepositories == null && !"true".equals(System.getProperty("skipSnapshotModules"))) {
            newRepositories.add(new DefaultExtensionRepositoryDescriptor("maven-xwiki-snapshot", "maven",
                URI.create("https://nexus.xwiki.org/nexus/content/groups/public-snapshots")));
        }

        return newRepositories;
    }
}
