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
import org.xwiki.component.namespace.Namespace;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.internal.tree.DefaultExtensionNode;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.tree.ExtensionNode;

/**
 * A repository containing installed extension.
 * <p>
 * It's generally a virtual repository since the actual extension are generally stored in the local repository.
 *
 * @version $Id$
 * @since 4.0M2
 */
@Role
public interface InstalledExtensionRepository extends ExtensionRepository, AdvancedSearchable
{
    /**
     * @return the number of local extensions
     */
    int countExtensions();

    /**
     * Return the installed extension associated to the provided feature for the provided namespace (or root namespace
     * since namespaces inherit from root).
     *
     * @param feature the extension id or provided feature (virtual extension)
     * @param namespace the namespace where the extension is installed, null mean installed in all namespaces (root
     *            namespace)
     * @return the extension, null if none could be found
     */
    InstalledExtension getInstalledExtension(String feature, String namespace);

    /**
     * @return all installed local extensions, an empty collection if none could be found
     */
    Collection<InstalledExtension> getInstalledExtensions();

    /**
     * Return all the extensions available for the provided namespace. This also include root extension since namespaces
     * inherit from root.
     * <p>
     * Note that {@link #getInstalledExtensions()} return all the extensions installed in all namespaces while
     * {@link #getInstalledExtensions(String)} with <code>null</code> return only those that are globally available.
     *
     * @param namespace the namespace where to search for installed extensions, null mean installed in all namespaces
     *            (root namespace)
     * @return all the local extensions installed in the provided namespace, an empty collection if none could be found
     */
    Collection<InstalledExtension> getInstalledExtensions(String namespace);

    /**
     * Indicate that the provided extension is installed in the provided namespace.
     *
     * @param extension the extension to install
     * @param namespace the namespace in which the extension is installed
     * @param dependency indicate if the extension is stored as a dependency of another one
     * @return the new {@link InstalledExtension}
     * @throws InstallException error when trying to install provided extension
     */
    InstalledExtension installExtension(LocalExtension extension, String namespace, boolean dependency)
        throws InstallException;

    /**
     * Indicate that the provided extension is installed in the specified namespace with the given properties.
     * 
     * @param extension the extension to install
     * @param namespace the namespace in which the extension is installed
     * @param dependency indicate if the installed extension is stored as a dependency of another extension
     * @param properties the custom properties to set on the installed extension for the specified namespace
     * @return the new {@link InstalledExtension}
     * @throws InstallException error when trying to install provided extension
     * @since 7.0M2
     */
    InstalledExtension installExtension(LocalExtension extension, String namespace, boolean dependency,
        Map<String, Object> properties) throws InstallException;

    /**
     * Return extension descriptor from the repository. If the extension can't be found <code>null</code> is returned.
     *
     * @param extensionId the extension identifier
     * @return the found extension descriptor or null if none could be found
     * @since 4.2RC1
     */
    InstalledExtension getInstalledExtension(ExtensionId extensionId);

    /**
     * Indicate that the provided extension is uninstalled from provided namespace.
     * <p>
     * Extension is never removed form the local repository. It's just namespace related informations.
     *
     * @param extension the extension to uninstall
     * @param namespace the namespace from which the extension is uninstalled
     * @throws UninstallException error when trying to uninstall provided extension
     */
    void uninstallExtension(InstalledExtension extension, String namespace) throws UninstallException;

    /**
     * Get provided installed extension backward dependencies in the provided namespace.
     * <p>
     * Only look at the backward dependencies in the provided namespace. To get all the dependencies of a root extension
     * (namespace=null) across namespaces use {@link #getBackwardDependencies(ExtensionId)} instead.
     *
     * @param feature the extension unique identifier
     * @param namespace the namespace where to search for backward dependencies
     * @return the backward dependencies, an empty collection of none could be found
     * @throws ResolveException error when searching for backward dependencies
     */
    Collection<InstalledExtension> getBackwardDependencies(String feature, String namespace) throws ResolveException;

    /**
     * Get provided installed extension backward dependencies in the provided namespace.
     * <p>
     * Only look at the backward dependencies in the provided namespace. To get all the dependencies of a root extension
     * (namespace=null) across namespaces use {@link #getBackwardDependencies(ExtensionId)} instead.
     *
     * @param feature the extension unique identifier
     * @param withOptionals include optional dependencies in the search
     * @param namespace the namespace where to search for backward dependencies
     * @return the backward dependencies, an empty collection of none could be found
     * @throws ResolveException error when searching for backward dependencies
     * @since 11.10
     */
    default Collection<InstalledExtension> getBackwardDependencies(String feature, String namespace,
        boolean withOptionals) throws ResolveException
    {
        return getBackwardDependencies(feature, namespace);
    }

    /**
     * Get all backward dependencies by namespace for the provided installed extension.
     *
     * @param extensionId the extension identifier
     * @return the extension backward dependencies in all namespaces
     * @throws ResolveException error when searching for extension backward dependencies
     */
    Map<String, Collection<InstalledExtension>> getBackwardDependencies(ExtensionId extensionId)
        throws ResolveException;

    /**
     * Get all backward dependencies by namespace for the provided installed extension.
     *
     * @param extensionId the extension identifier
     * @param withOptionals include optional dependencies in the search
     * @return the extension backward dependencies in all namespaces
     * @throws ResolveException error when searching for extension backward dependencies
     * @since 11.10
     */
    default Map<String, Collection<InstalledExtension>> getBackwardDependencies(ExtensionId extensionId,
        boolean withOptionals) throws ResolveException
    {
        return getBackwardDependencies(extensionId);
    }

    /**
     * Create a dependency tree containing the dependencies which are not shared with other extensions.
     * 
     * @param extension the extension for which to resolve the exclusive dependencies
     * @param namespace the namespace where to search for orphan extension dependencies
     * @return the extensions installed as dependency in the passed namespace and which no longer have backward
     *         dependencies
     * @since 11.10
     */
    default ExtensionNode<InstalledExtension> getOrphanedDependencies(InstalledExtension extension, Namespace namespace)
    {
        return new DefaultExtensionNode<>(namespace, extension);
    }

    // ExtensionRepository

    @Override
    InstalledExtension resolve(ExtensionDependency extensionDependency) throws ResolveException;

    @Override
    InstalledExtension resolve(ExtensionId extensionId) throws ResolveException;

    // Search

    /**
     * Search installed extensions based of the provided pattern and only in the passed namespace.
     * <p>
     * The pattern is a simple character chain.
     *
     * @param pattern the pattern to search
     * @param namespace the namespace where to search
     * @param offset the offset from where to start returning search results
     * @param nb the maximum number of search results to return
     * @return the found extensions descriptors, empty list if nothing could be found
     * @throws SearchException error when trying to search provided pattern
     * @since 5.3M1
     */
    IterableResult<InstalledExtension> searchInstalledExtensions(String pattern, String namespace, int offset, int nb)
        throws SearchException;

    /**
     * Search installed extensions based of the provided query and only in the passed namespace.
     *
     * @param namespace the namespace where to search
     * @param query the extension query used to filter and order the result
     * @return the found extensions descriptors, empty list if nothing could be found
     * @throws SearchException error when trying to search provided pattern
     * @since 7.0M2
     */
    IterableResult<InstalledExtension> searchInstalledExtensions(String namespace, ExtensionQuery query)
        throws SearchException;

    /**
     * Search installed extensions based of the provided query.
     *
     * @param query the extension query used to filter and order the result
     * @return the found extensions descriptors, empty list if nothing could be found
     * @throws SearchException error when trying to search provided pattern
     * @since 8.1RC1
     */
    default IterableResult<InstalledExtension> searchInstalledExtensions(ExtensionQuery query) throws SearchException
    {
        return searchInstalledExtensions((String) null, query);
    }

    /**
     * Search installed extensions based of the provided query and only in the passed namespaces.
     *
     * @param namespaces the namespaces where to search
     * @param query the extension query used to filter and order the result
     * @return the found extensions descriptors, empty list if nothing could be found
     * @throws SearchException error when trying to search provided pattern
     * @since 8.1RC1
     */
    default IterableResult<InstalledExtension> searchInstalledExtensions(Collection<String> namespaces,
        ExtensionQuery query) throws SearchException
    {
        return searchInstalledExtensions(
            namespaces != null && !namespaces.isEmpty() ? namespaces.iterator().next() : null, query);
    }
}
