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
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
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
     * Remove extension from local repository.
     * 
     * @param extension the extension to remove
     * @throws ResolveException error when trying to find provided extension
     */
    void removeExtension(LocalExtension extension) throws ResolveException;

    // Installed extensions

    /**
     * Return the installed extension associated to the provided feature for the provided namespace (or root namespace
     * since namespaces inherit from root).
     * 
     * @param feature the extension id or provided feature (virtual extension)
     * @param namespace the namespace where the extension is installed, null mean installed in all namespaces (root
     *            namespace)
     * @return the extension, null if none could be found
     * @deprecated will be removed as soon as it's fully implemented in
     *             {@link org.xwiki.extension.repository.internal.local.DefaultInstalledExtensionRepository#getInstalledExtension(String, String)}
     */
    @Deprecated
    LocalExtension getInstalledExtension(String feature, String namespace);

    /**
     * Indicate that the provided extension is installed in the provided namespace.
     * 
     * @param extension the extension to install
     * @param namespace the namespace in which the extension is installed
     * @param dependency indicate if the extension is stored as a dependency of another one
     * @throws InstallException error when trying to install provided extension
     * @deprecated will be removed as soon as it's fully implemented in
     *             {@link org.xwiki.extension.repository.internal.local.DefaultInstalledExtensionRepository#installExtension(LocalExtension, String, boolean)}
     */
    @Deprecated
    void installExtension(LocalExtension extension, String namespace, boolean dependency) throws InstallException;

    /**
     * Indicate that the provided extension is uninstalled from provided namespace.
     * <p>
     * Extension is never removed form the local repository. It's just namespace related informations.
     * 
     * @param extension the extension to uninstall
     * @param namespace the namespace from which the extension is uninstalled
     * @throws UninstallException error when trying to uninstall provided extension
     * @deprecated will be removed as soon as it's fully implemented in
     *             {@link org.xwiki.extension.repository.internal.local.DefaultInstalledExtensionRepository#installExtension(LocalExtension, String, boolean)}
     */
    @Deprecated
    void uninstallExtension(LocalExtension extension, String namespace) throws UninstallException;

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
     * @deprecated will be removed as soon as it's fully implemented in
     *             {@link org.xwiki.extension.repository.internal.local.DefaultInstalledExtensionRepository#getBackwardDependencies(String, String)}
     */
    @Deprecated
    Collection<LocalExtension> getBackwardDependencies(String feature, String namespace) throws ResolveException;

    /**
     * Get all backward dependencies by namespace for the provided installed extension.
     * 
     * @param extensionId the extension identifier
     * @return the extension backward dependencies in all namespaces
     * @throws ResolveException error when searching for extension backward dependencies
     * @deprecated will be removed as soon as it's fully implemented in
     *             {@link org.xwiki.extension.repository.internal.local.DefaultInstalledExtensionRepository#getBackwardDependencies(ExtensionId)}
     */
    @Deprecated
    Map<String, Collection<LocalExtension>> getBackwardDependencies(ExtensionId extensionId) throws ResolveException;

    // ExtensionRepository

    @Override
    LocalExtension resolve(ExtensionDependency extensionDependency) throws ResolveException;

    @Override
    LocalExtension resolve(ExtensionId extensionId) throws ResolveException;
}
