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
package org.xwiki.extension.repository.internal.installed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.InvalidExtensionException;
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
import org.xwiki.extension.version.VersionConstraint;

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
    private transient LocalExtensionRepository localRepository;

    /**
     * Used to check for existing core extensions.
     */
    @Inject
    private transient CoreExtensionRepository coreExtensionRepository;

    /**
     * The logger to log.
     */
    @Inject
    private transient Logger logger;

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
    private Map<String, Map<String, InstalledFeature>> extensionNamespaceByFeature =
        new ConcurrentHashMap<String, Map<String, InstalledFeature>>();

    /**
     * The installed extensions grouped by ids and ordered by version DESC.
     * <p>
     * <extension id, extensions>
     */
    private Map<String, List<DefaultInstalledExtension>> extensionsVersionsById =
        new ConcurrentHashMap<String, List<DefaultInstalledExtension>>();

    @Override
    public void initialize() throws InitializationException
    {
        setId(new ExtensionRepositoryId("installed", "installed", this.localRepository.getId().getURI()));

        // Validate local extension

        for (LocalExtension localExtension : this.localRepository.getLocalExtensions()) {
            if (DefaultInstalledExtension.isInstalled(localExtension)) {
                validateExtension(localExtension);
            }
        }
    }

    // Validation

    /**
     * Check extension validity and set it as not installed if not.
     * 
     * @param localExtension the extension to validate
     * @throws InvalidExtensionException when the passed extension is fond invalid
     */
    private void validateExtension(LocalExtension localExtension)
    {
        Collection<String> namespaces = DefaultInstalledExtension.getNamespaces(localExtension);

        if (namespaces == null) {
            try {
                validateExtension(localExtension, null);
            } catch (InvalidExtensionException e) {
                this.logger.error("Invalid extension [{}] it will not be loaded", localExtension.getId(), e);

                addInstalledExtension(localExtension, null, false);
            }
        } else {
            for (String namespace : namespaces) {
                try {
                    validateExtension(localExtension, namespace);
                } catch (InvalidExtensionException e) {
                    this.logger.error("Invalid extension [{}] on namespace [], it will not be loaded", new Object[] {
                    localExtension.getId(), namespace, e});
                
                    addInstalledExtension(localExtension, namespace, false);
                }
            }
        }
    }

    /**
     * Check extension validity against a specific namespace and set it as not installed if not.
     * 
     * @param localExtension the extension to validate
     * @param namespace the namespace
     * @throws InvalidExtensionException when the passed extension is fond invalid
     */
    private void validateExtension(LocalExtension localExtension, String namespace) throws InvalidExtensionException
    {
        InstalledFeature feature = getInstalledFeatureFromCache(localExtension.getId().getId(), namespace);
        if (feature != null && StringUtils.equals(feature.namespace, "namespace")) {
            // Already validated
            return;
        }

        // Actually validate

        if (!DefaultInstalledExtension.isInstalled(localExtension, namespace)) {
            throw new InvalidExtensionException("Extension [" + localExtension + "] is not installed");
        }

        if (this.coreExtensionRepository.exists(localExtension.getId().getId())) {
            throw new InvalidExtensionException("Extension [" + localExtension + "] already exists as a core extension");
        }

        // Validate dependencies
        for (ExtensionDependency dependency : localExtension.getDependencies()) {
            CoreExtension coreExtension = this.coreExtensionRepository.getCoreExtension(dependency.getId());

            if (coreExtension != null) {
                if (!isCompatible(coreExtension.getId().getVersion(), dependency.getVersionConstraint())) {
                    throw new InvalidExtensionException("Extension [" + localExtension
                        + "] is incompatible with the core extension [" + coreExtension + "]");
                }
            } else {
                boolean valid = false;

                List<LocalExtension> dependencyVersions =
                    new ArrayList<LocalExtension>(this.localRepository.getLocalExtensionVersions(dependency.getId()));
                Collections.reverse(dependencyVersions);
                for (LocalExtension dependencyVersion : dependencyVersions) {
                    if (isCompatible(dependencyVersion.getId().getVersion(), dependency.getVersionConstraint())) {
                        try {
                            validateExtension(dependencyVersion, namespace);
                            valid = true;
                            break;
                        } catch (InvalidExtensionException e) {
                            // Lets try next one
                        }
                    }
                }

                if (!valid) {
                    throw new InvalidExtensionException("The dependency [" + dependency + "] of extension ["
                        + localExtension + "] is invalid");
                }
            }
        }

        // Complete local extension installation
        addInstalledExtension(localExtension, namespace, true);
    }

    private boolean isCompatible(Version existingVersion, VersionConstraint versionConstraint)
    {
        boolean compatible = true;

        if (versionConstraint.getVersion() == null) {
            compatible = versionConstraint.containsVersion(existingVersion);
        } else {
            compatible = existingVersion.compareTo(versionConstraint.getVersion()) >= 0;
        }

        return compatible;
    }

    // Install/Uninstall

    /**
     * Uninstall provided extension.
     * 
     * @param installedExtension the extension to uninstall
     * @param namespace the namespace
     * @throws UninstallException error when trying to uninstall extension
     * @see #uninstallExtension(LocalExtension, String)
     */
    private void removeInstalledExtension(DefaultInstalledExtension installedExtension, String namespace)
        throws UninstallException
    {
        if (namespace == null) {
            this.extensionNamespaceByFeature.remove(installedExtension.getId().getId());
        } else {
            Map<String, InstalledFeature> namespaceInstalledExtension =
                this.extensionNamespaceByFeature.get(installedExtension.getId().getId());

            namespaceInstalledExtension.remove(namespace);
        }

        removeFromBackwardDependencies(installedExtension, namespace);

        if (!installedExtension.isInstalled()) {
            this.extensions.remove(installedExtension.getId());
        }
    }

    /**
     * Install provided extension.
     * 
     * @param localExtension the extension to install
     * @param namespace the namespace
     * @throws InstallException error when trying to uninstall extension
     * @see #installExtension(LocalExtension, String)
     */
    private void applyInstallExtension(DefaultInstalledExtension installedExtension, String namespace)
        throws InstallException
    {
        installedExtension.setInstalled(true, namespace);

        try {
            this.localRepository.setProperties(installedExtension.getLocalExtension(),
                installedExtension.getProperties());
        } catch (Exception e) {
            throw new InstallException("Failed to modify extension descriptor", e);
        }

        // Update caches

        addInstalledExtension(installedExtension, namespace, true);
    }

    private void removeFromBackwardDependencies(DefaultInstalledExtension installedExtension, String namespace)
    {
        // Clean provided extension dependencies backward dependencies
        for (ExtensionDependency dependency : installedExtension.getDependencies()) {
            if (this.coreExtensionRepository.getCoreExtension(dependency.getId()) == null) {
                InstalledFeature installedFeature = getInstalledFeatureFromCache(dependency.getId(), namespace);

                if (installedFeature == null) {
                    // That should never happen so lets log it
                    this.logger.warn("Extension [{}] is not installed", installedExtension.getId());
                } else if (!installedFeature.backwardDependencies.remove(installedExtension)) {
                    // That should never happen so lets log it
                    this.logger.warn("Extension [{}] was not regisistered as backward dependency of [{}]",
                        installedExtension.getId(), installedFeature.extension.getId());
                }
            }
        }
    }

    /**
     * Register a newly installed extension in backward dependencies map.
     * 
     * @param localExtension the local extension to register
     * @param namespace the namespace
     * @param valid is the extension valid
     */
    private void addInstalledExtension(LocalExtension localExtension, String namespace, boolean valid)
    {
        DefaultInstalledExtension installedExtension = this.extensions.get(localExtension.getId());
        if (installedExtension == null) {
            installedExtension = new DefaultInstalledExtension(localExtension, this);
        }

        installedExtension.setInstalled(true, namespace);
        installedExtension.setValid(namespace, valid);

        addInstalledExtension(installedExtension, namespace);
    }

    /**
     * Register a newly installed extension in backward dependencies map.
     * 
     * @param installedExtension the installed extension to register
     * @param namespace the namespace
     */
    private void addInstalledExtension(DefaultInstalledExtension installedExtension, String namespace)
    {
        this.extensions.put(installedExtension.getId(), installedExtension);

        // Register the extension in the installed extensions for the provided namespace
        addInstalledFeatureToCache(installedExtension.getId().getId(), namespace, installedExtension);

        // Add virtual extensions
        for (String feature : installedExtension.getFeatures()) {
            addInstalledFeatureToCache(feature, namespace, installedExtension);
        }

        // Add backward dependencies
        for (ExtensionDependency dependency : installedExtension.getDependencies()) {
            if (!this.coreExtensionRepository.exists(dependency.getId())) {
                // Get the extension for the dependency feature for the provided namespace
                DefaultInstalledExtension dependencyLocalExtension =
                    (DefaultInstalledExtension) getInstalledExtension(dependency.getId(), namespace);

                // Make sure to register backward dependency on the right namespace
                InstalledFeature dependencyInstalledExtension =
                    addInstalledFeatureToCache(dependency.getId(), namespace, dependencyLocalExtension);

                dependencyInstalledExtension.backwardDependencies.add(installedExtension);
            }
        }
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
    private InstalledFeature addInstalledFeatureToCache(String feature, String namespace,
        DefaultInstalledExtension localExtension)
    {
        Map<String, InstalledFeature> installedExtensionsForFeature = this.extensionNamespaceByFeature.get(feature);

        if (installedExtensionsForFeature == null) {
            installedExtensionsForFeature = new HashMap<String, InstalledFeature>();
            this.extensionNamespaceByFeature.put(feature, installedExtensionsForFeature);
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
    private InstalledFeature getInstalledFeatureFromCache(String feature, String namespace)
    {
        Map<String, InstalledFeature> installedExtensionsForFeature = this.extensionNamespaceByFeature.get(feature);

        if (installedExtensionsForFeature == null) {
            return null;
        }

        InstalledFeature installedExtension = installedExtensionsForFeature.get(namespace);

        if (installedExtension == null && namespace != null) {
            installedExtension = getInstalledFeatureFromCache(feature, null);
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
        List<DefaultInstalledExtension> versions = this.extensionsVersionsById.get(extensionDependency.getId());

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
        List<DefaultInstalledExtension> versions = this.extensionsVersionsById.get(id);

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
        List<InstalledExtension> result = new ArrayList<InstalledExtension>(this.extensions.size());
        for (InstalledExtension installedExtension : this.extensions.values()) {
            if (installedExtension.isInstalled(namespace)) {
                result.add(installedExtension);
            }
        }

        return result;
    }

    @Override
    public Collection<InstalledExtension> getInstalledExtensions()
    {
        return Collections.<InstalledExtension> unmodifiableCollection(this.extensions.values());
    }

    @Override
    public InstalledExtension getInstalledExtension(String feature, String namespace)
    {
        InstalledFeature installedFeature = getInstalledFeatureFromCache(feature, namespace);

        if (installedFeature != null) {
            return installedFeature.extension;
        }

        return null;
    }

    @Override
    public InstalledExtension installExtension(LocalExtension extension, String namespace, boolean dependency)
        throws InstallException
    {
        DefaultInstalledExtension installedExtension = this.extensions.get(extension.getId());

        if (installedExtension != null && installedExtension.isInstalled(namespace)) {
            if (installedExtension.isDependency() == dependency) {
                throw new InstallException("The extension [" + installedExtension
                    + "] is already installed on namespace [" + namespace + "]");
            }

            installedExtension.setDependency(dependency);

            try {
                this.localRepository.setProperties(installedExtension.getLocalExtension(),
                    installedExtension.getProperties());
            } catch (Exception e) {
                throw new InstallException("Failed to modify extension descriptor", e);
            }
        } else {
            LocalExtension localExtension;
            try {
                localExtension = this.localRepository.resolve(extension.getId());
            } catch (ResolveException e) {
                // Should be a very rare use case since we explicitly ask for a LocalExtension
                throw new InstallException("The extension [" + extension + "] need to be stored first");
            }

            if (installedExtension == null) {
                installedExtension = new DefaultInstalledExtension(localExtension, this);
            }

            if (dependency || installedExtension.getProperty(InstalledExtension.PKEY_DEPENDENCY) == null) {
                installedExtension.setDependency(dependency);
            }

            applyInstallExtension(installedExtension, namespace);
        }

        return installedExtension;
    }

    @Override
    public void uninstallExtension(InstalledExtension extension, String namespace) throws UninstallException
    {
        DefaultInstalledExtension installedExtension =
            (DefaultInstalledExtension) getInstalledExtension(extension.getId().getId(), namespace);

        if (installedExtension != null) {
            applyUninstallExtension(installedExtension, namespace);
        }
    }

    private void applyUninstallExtension(DefaultInstalledExtension installedExtension, String namespace)
        throws UninstallException
    {
        installedExtension.setInstalled(false, namespace);

        try {
            this.localRepository.setProperties(installedExtension.getLocalExtension(),
                installedExtension.getProperties());
        } catch (Exception e) {
            throw new UninstallException("Failed to modify extension descriptor", e);
        }

        // Clean caches

        removeInstalledExtension(installedExtension, namespace);
    }

    @Override
    public Collection<InstalledExtension> getBackwardDependencies(String feature, String namespace)
        throws ResolveException
    {
        if (getInstalledExtension(feature, namespace) == null) {
            throw new ResolveException("Extension [" + feature + "] is not installed on namespace [" + namespace + "]");
        }

        Map<String, InstalledFeature> installedExtensionsByFeature = this.extensionNamespaceByFeature.get(feature);
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
            this.extensionNamespaceByFeature.get(installedExtension.getId().getId());

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
