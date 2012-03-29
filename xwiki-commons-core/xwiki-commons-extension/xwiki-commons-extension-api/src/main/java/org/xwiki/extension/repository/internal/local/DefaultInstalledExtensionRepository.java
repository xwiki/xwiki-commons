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
package org.xwiki.extension.repository.internal.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.search.Searchable;
import org.xwiki.extension.version.Version;

/**
 * Default implementation of {@link InstalledExtensionRepository}.
 * 
 * @version $Id$
 * @since 4.0M2
 */
@Component
@Singleton
// TODO: move all installation related code from local repository to here
public class DefaultInstalledExtensionRepository extends AbstractExtensionRepository implements
    InstalledExtensionRepository, Initializable, Searchable
{
    private static class InstalledFeature
    {
        public DefaultInstalledExtension extension;

        public String feature;

        public String namespace;

        public Set<DefaultInstalledExtension> backwardDependencies = new HashSet<DefaultInstalledExtension>();

        /**
         * @param extension the extension
         * @param feature the feature
         * @param namespace the namespace
         */
        public InstalledFeature(DefaultInstalledExtension extension, String feature, String namespace)
        {
            this.extension = extension;
            this.feature = feature;
            this.namespace = namespace;
        }
    }

    @Inject
    private LocalExtensionRepository localRepository;

    /**
     * Used to check for existing core extensions.
     */
    @Inject
    private CoreExtensionRepository coreExtensionRepository;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * The installed extensions.
     */
    private Map<ExtensionId, DefaultInstalledExtension> extensions =
        new ConcurrentHashMap<ExtensionId, DefaultInstalledExtension>();

    /**
     * The installed extensions sorted by provided feature and namespace.
     * <p>
     * <feature, <namespace, extension>>
     */
    private Map<String, Map<String, InstalledFeature>> installedExtensions =
        new ConcurrentHashMap<String, Map<String, InstalledFeature>>();

    /**
     * The installed extensions grouped by ids and ordered by version DESC.
     * <p>
     * <extension id, extensions>
     */
    private Map<String, List<DefaultInstalledExtension>> extensionsById2 =
        new ConcurrentHashMap<String, List<DefaultInstalledExtension>>();

    @Override
    public void initialize() throws InitializationException
    {
        setId(new ExtensionRepositoryId("installed", "installed", this.localRepository.getId().getURI()));

        // Validate local extension

        Map<String, Set<String>> validatedExtension = new HashMap<String, Set<String>>();
        for (LocalExtension localExtension : this.localRepository.getLocalExtensions()) {
            if (DefaultInstalledExtension.isInstalled(localExtension)) {
                validateExtension(new DefaultInstalledExtension(localExtension, this), validatedExtension);
            }
        }
    }

    /**
     * Check extension validity and set it as not installed if not.
     * 
     * @param localExtension the extension to validate
     * @param validatedExtensions the already validated extensions
     */
    private void validateExtension(DefaultInstalledExtension localExtension,
        Map<String, Set<String>> validatedExtensions)
    {
        if (localExtension.getNamespaces() == null) {
            validateExtension(localExtension, validatedExtensions, null);
        } else {
            for (String namespace : localExtension.getNamespaces()) {
                validateExtension(localExtension, validatedExtensions, namespace);
            }
        }
    }

    /**
     * Check extension validity against a specific namespace and set it as not installed if not.
     * 
     * @param localExtension the extension to validate
     * @param validatedExtensions the already validated extensions
     * @param namespace the namespace
     */
    private void validateExtension(DefaultInstalledExtension localExtension,
        Map<String, Set<String>> validatedExtensions, String namespace)
    {
        Set<String> validatedExtensionsNamespace = validatedExtensions.get(namespace);
        if (validatedExtensionsNamespace == null) {
            validatedExtensionsNamespace = new HashSet<String>();
            validatedExtensions.put(namespace, validatedExtensionsNamespace);
        }

        if (!validatedExtensionsNamespace.contains(localExtension.getId().getId())) {
            validateExtension(localExtension, validatedExtensionsNamespace, namespace);
        }
    }

    /**
     * Check extension validity against a specific namespace and set it as not installed if not.
     * 
     * @param localExtension the extension to validate
     * @param validatedExtensions already validated extensions
     * @param namespace the namespace
     */
    private void validateExtension(DefaultInstalledExtension localExtension, Set<String> validatedExtensions,
        String namespace)
    {
        try {
            if (!localExtension.isInstalled(namespace)) {
                return;
            }

            if (this.coreExtensionRepository.exists(localExtension.getId().getId())) {
                // Impossible to overwrite core extensions
                localExtension.setInstalled(false, namespace);

                this.logger.error("Found local extension [" + localExtension
                    + "] is invalid. Impossible to overwrite core extensions.");

                return;
            }

            // Validate dependencies
            for (ExtensionDependency dependency : localExtension.getDependencies()) {
                boolean enabled = false;

                List<LocalExtension> dependencyVersions = this.extensionsById.get(dependency.getId());
                if (dependencyVersions != null) {
                    for (ListIterator<DefaultLocalExtension> it =
                        dependencyVersions.listIterator(dependencyVersions.size()); it.hasPrevious();) {
                        DefaultLocalExtension dependencyExtension = it.previous();

                        if (!validatedExtensions.contains(dependency.getId())) {
                            validateExtension(dependencyExtension, validatedExtensions, namespace);
                        }

                        if (dependencyExtension.isInstalled(namespace)) {
                            enabled = true;
                            break;
                        }
                    }
                } else {
                    enabled = this.coreExtensionRepository.exists(dependency.getId());
                }

                if (!enabled) {
                    localExtension.setInstalled(false, namespace);

                    this.logger.error("Found local extension [" + localExtension
                        + "] is invalid. One of it's dependency ([" + dependency
                        + "]) is not valid and is not a core extension.");

                    return;
                }
            }

            // Complete local extension installation
            addInstalledExtension(localExtension, namespace);
        } finally {
            validatedExtensions.add(localExtension.getId().getId());
        }
    }

    /**
     * Uninstall provided extension.
     * 
     * @param localExtension the extension to uninstall
     * @param namespace the namespace
     * @throws UninstallException error when trying to uninstall extension
     * @see #uninstallExtension(LocalExtension, String)
     */
    private void uninstallLocalExtension(DefaultInstalledExtension localExtension, String namespace)
        throws UninstallException
    {
        localExtension.setInstalled(false, namespace);

        try {
            this.storage.saveDescriptor(localExtension);
        } catch (Exception e) {
            throw new UninstallException("Failed to modify extension descriptor", e);
        }

        // Clean caches

        if (namespace == null) {
            this.installedExtensions.remove(localExtension.getId().getId());
        } else {
            Map<String, InstalledFeature> namespaceInstalledExtension =
                this.installedExtensions.get(localExtension.getId().getId());

            namespaceInstalledExtension.remove(namespace);
        }

        removeFromBackwardDependencies(localExtension, namespace);
    }

    /**
     * Install provided extension.
     * 
     * @param localExtension the extension to install
     * @param namespace the namespace
     * @throws InstallException error when trying to uninstall extension
     * @see #installExtension(LocalExtension, String)
     */
    private void installLocalExtension(DefaultInstalledExtension localExtension, String namespace)
        throws InstallException
    {
        localExtension.setInstalled(true, namespace);

        try {
            this.storage.saveDescriptor(localExtension);
        } catch (Exception e) {
            throw new InstallException("Failed to modify extension descriptor", e);
        }

        // Update caches

        addInstalledExtension(localExtension, namespace);
    }

    private void removeFromBackwardDependencies(DefaultLocalExtension localExtension, String namespace)
    {
        // Clean provided extension dependencies backward dependencies
        for (ExtensionDependency dependency : localExtension.getDependencies()) {
            if (this.coreExtensionRepository.getCoreExtension(dependency.getId()) == null) {
                InstalledFeature installedExtension = getInstalledExtensionFromCache(dependency.getId(), namespace);

                if (installedExtension == null) {
                    // That should never happen so lets log it
                    this.logger.warn("Extension [" + localExtension + "] is not installed");
                } else if (installedExtension.backwardDependencies.remove(localExtension)) {
                    // That should never happen so lets log it
                    this.logger.warn("Extension [" + localExtension
                        + "] was not regisistered as backward dependency of [" + installedExtension.extension + "]");
                }
            }
        }
    }

    /**
     * Register a newly installed extension in backward dependencies map.
     * 
     * @param localExtension the local extension to register
     * @param namespace the namespace
     * @return the installed extension informations
     */
    private InstalledFeature addInstalledExtension(DefaultInstalledExtension localExtension, String namespace)
    {
        // Register the extension in the installed extensions for the provided namespace
        InstalledFeature installedExtension =
            getInstalledExtensionFromCache(localExtension.getId().getId(), namespace, localExtension);

        // Add virtual extensions
        for (String feature : localExtension.getFeatures()) {
            getInstalledExtensionFromCache(feature, namespace, localExtension);
        }

        // Add backward dependencies
        for (ExtensionDependency dependency : localExtension.getDependencies()) {
            if (!this.coreExtensionRepository.exists(dependency.getId())) {
                // Get the extension for the dependency feature for the provided namespace
                DefaultLocalExtension dependencyLocalExtension =
                    (DefaultLocalExtension) getInstalledExtension(dependency.getId(), namespace);

                // Make sure to register backward dependency on the right namespace
                InstalledFeature dependencyInstalledExtension =
                    getInstalledExtensionFromCache(dependency.getId(), namespace, dependencyLocalExtension);

                dependencyInstalledExtension.backwardDependencies.add(localExtension);
            }
        }

        return installedExtension;
    }

    /**
     * Get extension registered as installed for the provided feature and namespace or can register it if provided.
     * <p>
     * Only look at provide namespace and does take into account inheritance.
     * 
     * @param feature the feature provided by the extension
     * @param namespace the namespace where the extension is installed
     * @param localExtension the extension
     * @return the installed extension informations
     */
    private InstalledFeature getInstalledExtensionFromCache(String feature, String namespace,
        DefaultInstalledExtension localExtension)
    {
        Map<String, InstalledFeature> installedExtensionsForFeature = this.installedExtensions.get(feature);

        if (installedExtensionsForFeature == null) {
            installedExtensionsForFeature = new HashMap<String, InstalledFeature>();
            this.installedExtensions.put(feature, installedExtensionsForFeature);
        }

        InstalledFeature installedExtension = installedExtensionsForFeature.get(namespace);
        if (installedExtension == null) {
            installedExtension = new InstalledFeature(localExtension, feature, namespace);
            installedExtensionsForFeature.put(namespace, installedExtension);
        }

        return installedExtension;
    }

    /**
     * Get extension registered as installed for the provided feature and namespace (including on root namespace).
     * 
     * @param feature the feature provided by the extension
     * @param namespace the namespace where the extension is installed
     * @return the installed extension informations
     */
    private InstalledFeature getInstalledExtensionFromCache(String feature, String namespace)
    {
        Map<String, InstalledFeature> installedExtensionsForFeature = this.installedExtensions.get(feature);

        if (installedExtensionsForFeature == null) {
            return null;
        }

        InstalledFeature installedExtension = installedExtensionsForFeature.get(namespace);

        if (installedExtension == null && namespace != null) {
            installedExtension = getInstalledExtensionFromCache(feature, null);
        }

        return installedExtension;
    }

    // ExtensionRepository

    @Override
    public InstalledExtension resolve(ExtensionId extensionId) throws ResolveException
    {
        InstalledExtension extension = this.extensions.get(extensionId);

        if (extension == null) {
            throw new ResolveException("Extension [" + extensionId + "] is not installed");
        }

        return extension;
    }

    @Override
    public InstalledExtension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        List<DefaultInstalledExtension> versions = this.extensionsById2.get(extensionDependency.getId());

        if (versions != null) {
            for (DefaultInstalledExtension extension : versions) {
                if (extensionDependency.getVersionConstraint().containsVersion(extension.getId().getVersion())) {
                    // Return the higher version which satisfy the version constraint
                    return extension;
                }
            }
        }

        throw new ResolveException("Can't find extension dependency [" + extensionDependency + "]");
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        return this.extensions.containsKey(extensionId);
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        List<DefaultInstalledExtension> versions = this.extensionsById2.get(id);

        if (versions == null) {
            throw new ResolveException("Can't find extension with id [" + id + "]");
        }

        if (nb == 0 || offset >= versions.size()) {
            return new CollectionIterableResult<Version>(versions.size(), offset, Collections.<Version> emptyList());
        }

        int fromId = offset < 0 ? 0 : offset;
        int toId = offset + nb > versions.size() || nb < 0 ? versions.size() - 1 : offset + nb;

        List<Version> result = new ArrayList<Version>(toId - fromId);

        // Invert to sort in ascendent order
        for (int i = toId - 1; i >= fromId; --i) {
            result.add(versions.get(i).getId().getVersion());
        }

        return new CollectionIterableResult<Version>(versions.size(), offset, result);
    }

    // InstalledExtensionRepository

    @Override
    public int countExtensions()
    {
        return this.extensions.size();
    }

    @Override
    public Collection<InstalledExtension> getInstalledExtensions(String namespace)
    {

    }

    @Override
    public Collection<InstalledExtension> getInstalledExtensions()
    {
        return Collections.<InstalledExtension> unmodifiableCollection(this.extensions.values());
    }

    @Override
    public InstalledExtension getInstalledExtension(String id, String namespace)
    {

    }

    @Override
    public InstalledExtension installExtension(LocalExtension extension, String namespace, boolean dependency)
        throws InstallException
    {
        DefaultInstalledExtension installedExtension = this.extensions.get(extension.getId());

        if (installedExtension.isInstalled(namespace)) {
            if (installedExtension.isDependency() == dependency) {
                throw new InstallException("The extension [" + installedExtension
                    + "] is already installed on namespace [" + namespace + "]");
            }

            installedExtension.setDependency(dependency);

            try {
                this.storage.saveDescriptor(installedExtension.getLocalExtension());
            } catch (Exception e) {
                throw new InstallException("Failed to modify extension descriptor", e);
            }
        } else {
            LocalExtension localExtension = this.localRepository.resolve(extension.getId());

            installedExtension = new DefaultInstalledExtension(localExtension, this);

            if (dependency || installedExtension.getProperty(InstalledExtension.PKEY_DEPENDENCY) == null) {
                installedExtension.setDependency(dependency);
            }

            installLocalExtension(installedExtension, namespace);
        }
    }

    @Override
    public void uninstallExtension(InstalledExtension extension, String namespace) throws UninstallException
    {
        DefaultInstalledExtension existingExtension =
            (DefaultInstalledExtension) getInstalledExtension(extension.getId().getId(), namespace);

        if (existingExtension != null) {
            uninstallLocalExtension(existingExtension, namespace);
        }
    }

    @Override
    public Collection<InstalledExtension> getBackwardDependencies(String feature, String namespace)
        throws ResolveException
    {
        if (getInstalledExtension(feature, namespace) == null) {
            throw new ResolveException("Extension [" + feature + "] is not installed on namespace [" + namespace + "]");
        }

        Map<String, InstalledFeature> installedExtensionsByFeature = this.installedExtensions.get(feature);
        if (installedExtensionsByFeature != null) {
            InstalledFeature installedExtension = installedExtensionsByFeature.get(namespace);

            if (installedExtension != null) {
                Set<DefaultInstalledExtension> backwardDependencies = installedExtension.backwardDependencies;

                // copy the list to allow use cases like uninstalling all backward dependencies without getting a
                // concurrent issue on the list
                return backwardDependencies != null ? new ArrayList<InstalledExtension>(backwardDependencies)
                    : Collections.<InstalledExtension> emptyList();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public Map<String, Collection<InstalledExtension>> getBackwardDependencies(ExtensionId extensionId)
        throws ResolveException
    {
        Map<String, Collection<InstalledExtension>> result;

        DefaultInstalledExtension installedExtension = (DefaultInstalledExtension) resolve(extensionId);

        Collection<String> namespaces = installedExtension.getNamespaces();

        Map<String, InstalledFeature> featureExtensions =
            this.installedExtensions.get(installedExtension.getId().getId());

        if (featureExtensions != null) {
            result = new HashMap<String, Collection<InstalledExtension>>();
            for (InstalledFeature festureExtension : featureExtensions.values()) {
                if ((namespaces == null || namespaces.contains(festureExtension.namespace))
                    && !festureExtension.backwardDependencies.isEmpty()) {
                    // copy the list to allow use cases like uninstalling all backward dependencies without getting a
                    // concurrent issue on the list
                    result.put(festureExtension.namespace, new ArrayList<InstalledExtension>(
                        festureExtension.backwardDependencies));
                }
            }
        } else {
            result = Collections.emptyMap();
        }

        return result;
    }

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
    {
        return RepositoryUtils.searchInCollection(pattern, offset, nb, this.extensions.values());
    }
}
