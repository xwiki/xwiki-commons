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
import org.xwiki.extension.repository.ExtensionRepositoryManager;

/**
 * Extensions repositories identifier stored in the configuration.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
@Named("default.xwiki")
public class ExtensionXWikiOrgExtensionRepositorySource extends AbstractExtensionRepositorySource
{
    /**
     * The priority of extensions.xwiki.org repository.
     */
    // Make extensions.xwiki.org repository checked after Nexus (Nexus contains much more extensions including most of
    // those located on extensions.xwiki.org)
    public static final int PRIORITY = ExtensionRepositoryManager.DEFAULT_PRIORITY + 200;

    /**
     * Used to get configuration properties containing repositories.
     */
    @Inject
    private ExtensionManagerConfiguration configuration;

    @Override
    public int getPriority()
    {
        return PRIORITY;
    }

    @Override
    public Collection<ExtensionRepositoryDescriptor> getExtensionRepositoryDescriptors()
    {
        Collection<ExtensionRepositoryDescriptor> configuredRepositories =
            this.configuration.getExtensionRepositoryDescriptors();

        Collection<ExtensionRepositoryDescriptor> repositories = new ArrayList<ExtensionRepositoryDescriptor>();

        if (configuredRepositories == null) {
            try {
                repositories.add(new DefaultExtensionRepositoryDescriptor("extensions.xwiki.org", "xwiki",
                    new URI("https://extensions.xwiki.org/xwiki/rest/")));
            } catch (URISyntaxException e) {
                // Should never happen
            }
        }

        return repositories;
    }
}
