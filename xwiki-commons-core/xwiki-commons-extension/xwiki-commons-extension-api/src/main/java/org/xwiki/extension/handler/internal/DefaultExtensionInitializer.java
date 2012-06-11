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

package org.xwiki.extension.handler.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.handler.ExtensionHandlerManager;
import org.xwiki.extension.handler.ExtensionInitializer;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.InstalledExtensionRepository;

/**
 * Default implementation of {@link org.xwiki.extension.handler.ExtensionInitializer}.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultExtensionInitializer implements ExtensionInitializer
{
    /**
     * The local extension repository from which extension are initialized.
     */
    @Inject
    private InstalledExtensionRepository installedExtensionRepository;

    /**
     * The extension manager to launch extension initialization.
     */
    @Inject
    private ExtensionHandlerManager extensionHandlerManager;

    /**
     * The core extension repository to check extension dependency availability.
     */
    @Inject
    private CoreExtensionRepository coreExtensionRepository;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Override
    public void initialize()
    {
        initialize(null, null);
    }

    @Override
    public void initialize(String namespaceToLoad)
    {
        initialize(namespaceToLoad, null);
    }

    @Override
    public void initialize(String namespaceToLoad, String type)
    {
        Map<String, Set<InstalledExtension>> loadedExtensions = new HashMap<String, Set<InstalledExtension>>();

        // Load extensions from local repository
        Collection<InstalledExtension> installedExtensions;
        if (namespaceToLoad != null) {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions(namespaceToLoad);
        } else {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions();
        }
        for (InstalledExtension installedExtension : installedExtensions) {
            if (type == null || type.equals(installedExtension.getType())) {
                try {
                    loadExtension(installedExtension, namespaceToLoad, loadedExtensions);
                } catch (Exception e) {
                    this.logger.error("Failed to initialize local extension [" + installedExtension + "]", e);
                }
            }
        }
    }

    /**
     * Initialize extension.
     * 
     * @param installedExtension the extension to initialize
     * @param namespaceToLoad the namespace to be initialized, null for all
     * @param loadedExtensions the currently initialized extensions set
     * @throws ExtensionException when an initialization error occurs
     */
    private void loadExtension(InstalledExtension installedExtension, String namespaceToLoad,
        Map<String, Set<InstalledExtension>> loadedExtensions) throws ExtensionException
    {
        if (installedExtension.getNamespaces() != null) {
            if (namespaceToLoad == null) {
                for (String namespace : installedExtension.getNamespaces()) {
                    loadExtensionInNamespace(installedExtension, namespace, loadedExtensions);
                }
            } else if (installedExtension.getNamespaces().contains(namespaceToLoad)) {
                loadExtensionInNamespace(installedExtension, namespaceToLoad, loadedExtensions);
            }
        } else if (namespaceToLoad == null) {
            loadExtensionInNamespace(installedExtension, null, loadedExtensions);
        }
    }

    /**
     * Initialize an extension in the given namespace.
     * 
     * @param installedExtension the extension to initialize
     * @param namespace the namespace in which the extention is initialized, null for global
     * @param loadedExtensions the currently initialized extensions set (to avoid initializing twice a dependency)
     * @throws ExtensionException when an initialization error occurs
     */
    private void loadExtensionInNamespace(InstalledExtension installedExtension, String namespace,
        Map<String, Set<InstalledExtension>> loadedExtensions) throws ExtensionException
    {
        if (!installedExtension.isValid(namespace)) {
            return;
        }

        Set<InstalledExtension> loadedExtensionsInNamespace = loadedExtensions.get(namespace);

        if (loadedExtensionsInNamespace == null) {
            loadedExtensionsInNamespace = new HashSet<InstalledExtension>();
            loadedExtensions.put(namespace, loadedExtensionsInNamespace);
        }

        if (!loadedExtensionsInNamespace.contains(installedExtension)) {
            for (ExtensionDependency dependency : installedExtension.getDependencies()) {
                if (!this.coreExtensionRepository.exists(dependency.getId())) {
                    InstalledExtension dependencyExtension =
                        this.installedExtensionRepository.getInstalledExtension(dependency.getId(), namespace);
                    loadExtensionInNamespace(dependencyExtension, namespace, loadedExtensions);
                }
            }

            this.extensionHandlerManager.initialize(installedExtension, namespace);

            loadedExtensionsInNamespace.add(installedExtension);
        }
    }
}
