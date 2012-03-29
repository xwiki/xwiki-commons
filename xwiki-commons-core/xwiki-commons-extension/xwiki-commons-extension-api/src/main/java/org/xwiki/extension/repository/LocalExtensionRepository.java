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
package org.xwiki.extension.repository;

import java.util.Collection;
import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.search.Searchable;

/**
 * A repository containing local extension.
 * <p>
 * That's were remote extension are stored after being downloaded and from where extension are actually installed by
 * their respective handlers.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Role
public interface LocalExtensionRepository extends ExtensionRepository, Searchable
{
    /**
     * @return the number of local extensions
     */
    int countExtensions();

    /**
     * @return all the local extensions, an empty collection if none could be found
     */
    Collection<LocalExtension> getLocalExtensions();

    /**
     * Store provided extension (generally a remote extension) in the local repository.
     * 
     * @param extension the extension to store
     * @return the new local extension
     * @throws LocalExtensionRepositoryException error when trying store provided extension in the local repository
     */
    LocalExtension storeExtension(Extension extension) throws LocalExtensionRepositoryException;

    /**
     * @param localExtension the local extension to modify
     * @param properties the properties to set
     * @throws LocalExtensionRepositoryException error when trying to save the extension change
     */
    void setProperties(LocalExtension localExtension, Map<String, Object> properties)
        throws LocalExtensionRepositoryException;

    /**
     * Remove extension from local repository.
     * 
     * @param extension the extension to remove
     * @throws ResolveException error when trying to find provided extension
     */
    void removeExtension(LocalExtension extension) throws ResolveException;

    // ExtensionRepository

    @Override
    LocalExtension resolve(ExtensionDependency extensionDependency) throws ResolveException;

    @Override
    LocalExtension resolve(ExtensionId extensionId) throws ResolveException;
}
