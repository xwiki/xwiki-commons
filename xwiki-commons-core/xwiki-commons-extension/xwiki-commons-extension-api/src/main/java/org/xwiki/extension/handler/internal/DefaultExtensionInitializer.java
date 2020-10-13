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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
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
public class DefaultExtensionInitializer implements ExtensionInitializer, Initializable
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
    public void initialize(String namespaceToInitialize, String type)
    {
        Map<String, Set<InstalledExtension>> initializedExtensions = new HashMap<>();

        // Load extensions from local repository
        Collection<InstalledExtension> installedExtensions;
        if (namespaceToInitialize != null) {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions(namespaceToInitialize);
        } else {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions();
        }
        for (InstalledExtension installedExtension : installedExtensions) {
            if (type == null || type.equals(installedExtension.getType())) {
                try {
                    initializeExtension(installedExtension, namespaceToInitialize, initializedExtensions);
                } catch (Throwable t) {
                    this.logger.error("Failed to initialize local extension [{}]", installedExtension.getId(), t);
                }
            }
        }
    }

    /**
     * Initialize extension.
     *
     * @param installedExtension the extension to initialize
     * @param namespaceToLoad the namespace to be initialized, null for all
     * @param initializedExtensions the currently initialized extensions set
     * @throws ExtensionException when an initialization error occurs
     */
    private void initializeExtension(InstalledExtension installedExtension, String namespaceToLoad,
        Map<String, Set<InstalledExtension>> initializedExtensions) throws ExtensionException
    {
        if (installedExtension.getNamespaces() != null) {
            if (namespaceToLoad == null) {
                for (String namespace : installedExtension.getNamespaces()) {
                    initializeExtensionInNamespace(installedExtension, namespace, initializedExtensions);
                }
            } else if (installedExtension.getNamespaces().contains(namespaceToLoad)) {
                initializeExtensionInNamespace(installedExtension, namespaceToLoad, initializedExtensions);
            }
        } else if (namespaceToLoad == null) {
            initializeExtensionInNamespace(installedExtension, null, initializedExtensions);
        }
    }

    /**
     * Initialize an extension in the given namespace.
     *
     * @param installedExtension the extension to initialize
     * @param namespace the namespace in which the extention is initialized, null for global
     * @param initializedExtensions the currently initialized extensions set (to avoid initializing twice a dependency)
     * @throws ExtensionException when an initialization error occurs
     */
    private void initializeExtensionInNamespace(InstalledExtension installedExtension, String namespace,
        Map<String, Set<InstalledExtension>> initializedExtensions) throws ExtensionException
    {
        // Check if the extension can be available from this namespace
        if (!installedExtension.isValid(namespace)) {
            return;
        }

        Set<InstalledExtension> initializedExtensionsInNamespace = initializedExtensions.get(namespace);

        if (initializedExtensionsInNamespace == null) {
            initializedExtensionsInNamespace = new HashSet<>();
            initializedExtensions.put(namespace, initializedExtensionsInNamespace);
        }

        if (!initializedExtensionsInNamespace.contains(installedExtension)) {
            initializeExtensionInNamespace(installedExtension, namespace, initializedExtensions,
                initializedExtensionsInNamespace);
        }
    }

    private void initializeExtensionInNamespace(InstalledExtension installedExtension, String namespace,
        Map<String, Set<InstalledExtension>> initializedExtensions,
        Set<InstalledExtension> initializedExtensionsInNamespace) throws ExtensionException
    {
        if (namespace != null && installedExtension.getNamespaces() == null) {
            // This extension is supposed to be installed on root namespace only so redirecting to null namespace
            // initialization
            initializeExtensionInNamespace(installedExtension, null, initializedExtensions);
        } else {
            // Initialize dependencies
            for (ExtensionDependency dependency : installedExtension.getDependencies()) {
                initializeExtensionDependencyInNamespace(installedExtension, dependency, namespace,
                    initializedExtensions);
            }

            // Initialize the extension
            this.extensionHandlerManager.initialize(installedExtension, namespace);

            // Cache the extension to not initialize several times
            initializedExtensionsInNamespace.add(installedExtension);
        }
    }

    private void initializeExtensionDependencyInNamespace(InstalledExtension installedExtension,
        ExtensionDependency dependency, String namespace, Map<String, Set<InstalledExtension>> initializedExtensions)
        throws ExtensionException
    {
        if (!this.coreExtensionRepository.exists(dependency.getId())) {
            InstalledExtension dependencyExtension =
                this.installedExtensionRepository.getInstalledExtension(dependency.getId(), namespace);

            if (dependencyExtension != null) {
                if (dependencyExtension == installedExtension) {
                    throw new ExtensionException(String.format(
                        "Extension [%s] has itself as a dependency ([%s]). "
                            + "It usually means an extension is installed along with one of its features.",
                        installedExtension, dependency));
                }

                try {
                    initializeExtensionInNamespace(dependencyExtension, namespace, initializedExtensions);
                } catch (Exception e) {
                    if (dependency.isOptional()) {
                        this.logger.warn("Failed to initialize dependency [{}]: {}", dependency,
                            ExceptionUtils.getRootCauseMessage(e));
                    } else {
                        throw new ExtensionException(String.format(
                            "Failed to initialize dependency [%s]", dependency), e);
                    }
                }
            } else if (!dependency.isOptional()) {
                throw new ExtensionException(String.format("Mandatory dependency [%s] of extension [%s] "
                    + "is not installed", dependency, installedExtension.getId()));
            }
        }
    }
}
