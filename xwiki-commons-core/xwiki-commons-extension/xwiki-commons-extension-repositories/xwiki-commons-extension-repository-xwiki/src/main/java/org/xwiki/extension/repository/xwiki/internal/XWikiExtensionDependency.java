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

import java.net.URISyntaxException;

import org.xwiki.extension.AbstractExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionDependency;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionRepository;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;

/**
 * XWiki Repository implementation of {@link org.xwiki.extension.ExtensionDependency}.
 * 
 * @version $Id$
 * @since 7.3M1
 */
public class XWikiExtensionDependency extends AbstractExtensionDependency
{
    /**
     * @param restDependency the REST representation of an Extension dependency
     * @param extensionRepository the repository of the parent extension
     * @param factory tool to share instances of various parts of an {@link Extension}
     * @since 8.4
     */
    public XWikiExtensionDependency(ExtensionDependency restDependency,
        ExtensionRepositoryDescriptor extensionRepository, ExtensionFactory factory)
    {
        super(restDependency.getId(), new DefaultVersionConstraint(restDependency.getConstraint()),
            restDependency.isOptional());

        // Make sure the dependency will be resolved in the extension repository first
        if (extensionRepository != null) {
            addRepository(extensionRepository);
        }

        // Repositories
        for (ExtensionRepository restRepository : restDependency.getRepositories()) {
            try {
                addRepository(XWikiExtension.toExtensionRepositoryDescriptor(restRepository, factory));
            } catch (URISyntaxException e) {
                // TODO: Log something ?
            }
        }
    }
}
