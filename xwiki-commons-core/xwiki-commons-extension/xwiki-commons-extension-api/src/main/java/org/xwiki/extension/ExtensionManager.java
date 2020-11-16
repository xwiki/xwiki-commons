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
package org.xwiki.extension;

import java.util.Collections;

import org.xwiki.component.annotation.Role;
import org.xwiki.component.namespace.Namespace;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.stability.Unstable;

/**
 * Main entry point for some extensions management tasks.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Role
public interface ExtensionManager
{
    /**
     * @param extensionId the extension identifier
     * @return true if the extension exists in the repository
     * @since 12.10RC1
     */
    @Unstable
    default boolean exists(ExtensionId extensionId)
    {
        try {
            resolveExtension(extensionId);

            return true;
        } catch (ResolveException e) {
            // The extension does not exist
        }

        return false;
    }

    /**
     * Search the provided extension among all repositories including core and local repositories.
     * <p>
     * The search is done in the following order:
     * <ul>
     * <li>Is it a core extension ?</li>
     * <li>Is it a local extension ?</li>
     * <li>Is it a remote extension in one of the configured remote repositories ?</li>
     * </ul>
     * The first one found is returned.
     *
     * @param extensionId the extension identifier
     * @return the resolved extension
     * @throws ResolveException error when trying to resolve extension
     */
    Extension resolveExtension(ExtensionId extensionId) throws ResolveException;

    /**
     * Search the provided extension as a dependency of another extension among all repositories including core and
     * local repositories.
     * <p>
     * The search is done in the following order:
     * <ul>
     * <li>Is it a core extension ?</li>
     * <li>Is it a local extension ?</li>
     * <li>Is it a remote extension in one of the configured remote repositories ?</li>
     * </ul>
     * The first one found is returned.
     *
     * @param extensionDependency the extension as dependency
     * @return the resolved extension
     * @throws ResolveException error when trying to resolve extension
     * @deprecated since 5.3M1, use {@link #resolveExtension(ExtensionDependency, String)} instead
     */
    @Deprecated
    Extension resolveExtension(ExtensionDependency extensionDependency) throws ResolveException;

    /**
     * Search the provided extension as a dependency of another extension among all repositories including core and
     * local repositories.
     * <p>
     * The search is done in the following order:
     * <ul>
     * <li>Is it a core extension ?</li>
     * <li>Is it a local extension ?</li>
     * <li>Is this feature installed in current namespace or parent ?</li>
     * <li>Is it a remote extension in one of the configured remote repositories ?</li>
     * </ul>
     * The first one found is returned.
     *
     * @param extensionDependency the extension as dependency
     * @param namespace the namespace where to search for the dependency
     * @return the resolved extension
     * @throws ResolveException error when trying to resolve extension
     * @since 5.3M1
     */
    Extension resolveExtension(ExtensionDependency extensionDependency, String namespace) throws ResolveException;

    /**
     * Return a repository based on its id.
     * <p>
     * This method also return <tt>local</tt>, <tt>installed</tt> and <tt>core</tt> repositories.
     *
     * @param repositoryId the id of the repository
     * @return the repository
     * @since 4.0M2
     */
    ExtensionRepository getRepository(String repositoryId);

    /**
     * Get the accessible (i.e. core or installed) extension instance from the passed namespace and matching the passed
     * feature/id.
     * 
     * @param feature the extension id or provided feature (virtual extension)
     * @param namespace the namespace from where the extension is usable
     * @return the {@link Extension} instance
     * @since 10.0RC1
     */
    default Extension getAccessibleExtension(String feature, Namespace namespace)
    {
        return null;
    }

    /**
     * Search accessible (i.e. core or installed) extensions based of the provided query and only in the passed
     * namespace.
     *
     * @param namespace the namespace where to search
     * @param query the extension query used to filter and order the result
     * @return the found extensions descriptors, empty list if nothing could be found
     * @throws SearchException error when trying to search provided pattern
     * @since 10.0RC1
     */
    default IterableResult<Extension> searchAccessibleExtensions(Namespace namespace, ExtensionQuery query)
        throws SearchException
    {
        return new CollectionIterableResult<>(0, query.getOffset(), Collections.emptyList());
    }
}
