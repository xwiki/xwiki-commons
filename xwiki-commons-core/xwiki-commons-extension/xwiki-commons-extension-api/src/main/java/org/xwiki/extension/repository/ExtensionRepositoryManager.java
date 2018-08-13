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

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.version.Version;

/**
 * Proxy repository in to make easier to search in all remote extension repositories.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Role
public interface ExtensionRepositoryManager extends ExtensionRepository, AdvancedSearchable
{
    /**
     * The default priority for extension repository ordering.
     * 
     * @since 8.3M1
     */
    int DEFAULT_PRIORITY = 500;

    /**
     * Create and add a new repository.
     *
     * @param repositoryId the repository identifier
     * @return the newly created repository
     * @throws ExtensionRepositoryException failed to create {@link ExtensionRepository} for provided identifier
     * @deprecated since 4.3M1 use {@link #addRepository(ExtensionRepositoryDescriptor)} instead
     */
    @Deprecated
    ExtensionRepository addRepository(ExtensionRepositoryId repositoryId) throws ExtensionRepositoryException;

    /**
     * Create and add a new repository.
     *
     * @param repositoryDescriptor the repository descriptor
     * @return the newly created repository
     * @throws ExtensionRepositoryException failed to create {@link ExtensionRepository} for provided identifier
     * @since 4.3M1
     */
    ExtensionRepository addRepository(ExtensionRepositoryDescriptor repositoryDescriptor)
        throws ExtensionRepositoryException;

    /**
     * Create and add a new repository.
     *
     * @param priority the priority in the list of repositories
     * @param repositoryDescriptor the repository descriptor
     * @return the newly created repository
     * @throws ExtensionRepositoryException failed to create {@link ExtensionRepository} for provided identifier
     * @since 8.3M1
     */
    default ExtensionRepository addRepository(ExtensionRepositoryDescriptor repositoryDescriptor, int priority)
        throws ExtensionRepositoryException
    {
        return addRepository(repositoryDescriptor);
    }

    /**
     * @param repository add an existing repository
     */
    void addRepository(ExtensionRepository repository);

    /**
     * @param priority the priority in the list of repositories
     * @param repository add an existing repository
     * @since 8.3M1
     */
    default void addRepository(ExtensionRepository repository, int priority)
    {
        addRepository(repository);
    }

    /**
     * Remove a repository form the list.
     *
     * @param repositoryId the repository unique identifier
     * @see ExtensionRepository#getId()
     */
    void removeRepository(String repositoryId);

    /**
     * @param repositoryId the repository unique identifier
     * @return the repository, null if none could be found
     * @see ExtensionRepository#getId()
     */
    ExtensionRepository getRepository(String repositoryId);

    /**
     * @return the repositories
     */
    Collection<ExtensionRepository> getRepositories();

    // ExtensionRepository

    /**
     * Get extension descriptor found in one of the repositories.
     * <p>
     * The proxy search in all repositories and return the first extension it could find.
     *
     * @param extensionId the extension identifier
     * @return the found extension descriptor
     * @throws ExtensionNotFoundException when the extension does not exist in any of the repositories
     * @throws ResolveException failed to find extension in the repository
     */
    @Override
    Extension resolve(ExtensionId extensionId) throws ResolveException;

    /**
     * Get extension descriptor found in one of the repositories.
     * <p>
     * The proxy search in all repositories and return the first extension it could find.
     * <p>
     * This method takes {@link ExtensionDependency} instead of {@link ExtensionId} to allow any implementation of
     * {@link ExtensionRepository} to extension dependencies with filter not supported yet by Extension Manage. As an
     * example Aether implementation add support from classifiers, excludes and version ranges.
     *
     * @param extensionDependency the extension dependency
     * @return the found extension descriptor
     * @throws ExtensionNotFoundException when the dependency does not match any extension in any of the repositories
     * @throws ResolveException failed to find extension in the repository
     */
    @Override
    Extension resolve(ExtensionDependency extensionDependency) throws ResolveException;

    /**
     * Return ordered (ascendent) versions for the provided extension id.
     *
     * @param id the id of the extensions for which to return versions
     * @param offset the offset from where to start returning versions
     * @param nb the maximum number of versions to return
     * @return the versions of the provided extension id
     * @throws ExtensionNotFoundException when the extension does not exist in any of the repositories
     * @throws ResolveException fail to find extension for provided id
     */
    @Override
    IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException;

    // AdvancedSearchable

    /**
     * Search among all repositories implementing {@link org.xwiki.extension.repository.search.Searchable} interface.
     *
     * @param pattern the pattern to search
     * @param offset the offset from where to start returning search results, 0-based
     * @param nb the maximum number of search results to return. -1 indicate no limit. 0 indicate that no result will be
     *            returned but it can be used to get the total hits.
     * @return the found extensions descriptors, empty list if nothing could be found
     * @see org.xwiki.extension.repository.search.Searchable
     */
    @Override
    IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException;

    /**
     * Search among all repositories implementing {@link org.xwiki.extension.repository.search.AdvancedSearchable}
     * interface.
     * 
     * @param query the query
     * @return the found extensions descriptors, empty list if nothing could be found
     * @see org.xwiki.extension.repository.search.AdvancedSearchable
     * @since 7.1M1
     */
    @Override
    IterableResult<Extension> search(ExtensionQuery query) throws SearchException;
}
