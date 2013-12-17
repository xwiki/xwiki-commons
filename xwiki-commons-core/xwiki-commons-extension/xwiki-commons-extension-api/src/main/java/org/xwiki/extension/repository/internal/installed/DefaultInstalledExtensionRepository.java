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
import java.util.regex.Pattern;

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
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.internal.local.AbstractCachedExtensionRepository;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
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
public class DefaultInstalledExtensionRepository extends AbstractCachedExtensionRepository<DefaultInstalledExtension>
    implements InstalledExtensionRepository, Initializable
{
    private static class InstalledFeature
    {
        public DefaultInstalledExtension extension;

        public Set<DefaultInstalledExtension> invalidExtensions = new HashSet<DefaultInstalledExtension>();

        public String feature;

        public String namespace;

        public Set<DefaultInstalledExtension> backwardDependencies = new HashSet<DefaultInstalledExtension>();

        /**
         * @param feature the feature
         * @param namespace the namespace
         */
        public InstalledFeature(String feature, String namespace)
        {
            this.feature = feature;
            this.namespace = namespace;
        }
    }

    /**
     * Used to access all local extensions.
     */
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
     * The installed extensions sorted by provided feature and namespace.
     * <p>
     * <feature, <namespace, extension>>
     */
    private Map<String, Map<String, InstalledFeature>> extensionNamespaceByFeature =
        new ConcurrentHashMap<String, Map<String, InstalledFeature>>();

    @Override
    public void initialize() throws InitializationException
    {
        setDescriptor(new DefaultExtensionRepositoryDescriptor("installed", "installed", this.localRepository
            .getDescriptor().getURI()));

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
                validateExtension(localExtension, null, true);
            } catch (InvalidExtensionException e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.warn("Invalid extension [{}]", localExtension.getId(), e);
                } else {
                    this.logger.warn("Invalid extension [{}]", localExtension.getId());
                }

                addInstalledExtension(localExtension, null, false);
            }
        } else {
            for (String namespace : namespaces) {
                try {
                    validateExtension(localExtension, namespace, true);
                } catch (InvalidExtensionException e) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.warn("Invalid extension [{}] on namespace [{}]", localExtension.getId(), namespace,
                            e);
                    } else {
                        this.logger.warn("Invalid extension [{}] on namespace [{}]", localExtension.getId(), namespace);
                    }

                    addInstalledExtension(localExtension, namespace, false);
                }
            }
        }
    }

    private LocalExtension getInstalledLocalExtension(ExtensionDependency dependency, String namespace)
    {
        Collection<LocalExtension> dependencyVersionsCollection =
            this.localRepository.getLocalExtensionVersions(dependency.getId());
        if (!dependencyVersionsCollection.isEmpty()) {
            List<LocalExtension> dependencyVersions = new ArrayList<LocalExtension>(dependencyVersionsCollection);
            Collections.reverse(dependencyVersions);
            for (LocalExtension dependencyVersion : dependencyVersions) {
                if (DefaultInstalledExtension.isInstalled(dependencyVersion, namespace)
                    && isCompatible(dependencyVersion.getId().getVersion(), dependency.getVersionConstraint())) {
                    return dependencyVersion;
                }
            }
        }

        return null;
    }

    private void validateDependency(ExtensionDependency dependency, String namespace, boolean register)
        throws InvalidExtensionException
    {
        CoreExtension coreExtension = this.coreExtensionRepository.getCoreExtension(dependency.getId());

        if (coreExtension != null) {
            if (!isCompatible(coreExtension.getId().getVersion(), dependency.getVersionConstraint())) {
                throw new InvalidExtensionException(String.format(
                    "Dependency [%s] is incompatible with the core extension [%s]", dependency, coreExtension));
            }
        } else {
            LocalExtension dependencyExtension =
                register ? getInstalledLocalExtension(dependency, namespace) : getInstalledExtension(
                    dependency.getId(), namespace);

            if (dependencyExtension == null) {
                throw new InvalidExtensionException(String.format(
                    "No compatible extension is installed for dependency [%s]", dependency));
            } else {
                try {
                    DefaultInstalledExtension installedExtension =
                        validateExtension(dependencyExtension, namespace, register);

                    if (!installedExtension.isValid(namespace)) {
                        throw new InvalidExtensionException(String.format("Extension dependency [%s] is invalid",
                            installedExtension.getId()));
                    }
                } catch (InvalidExtensionException e) {
                    if (register) {
                        addInstalledExtension(dependencyExtension, namespace, false);
                    }
                    throw e;
                }
            }
        }
    }

    /**
     * Check extension validity against a specific namespace and set it as not installed if not.
     * 
     * @param localExtension the extension to validate
     * @param namespace the namespace
     * @return the corresponding {@link DefaultInstalledExtension}
     * @throws InvalidExtensionException when the passed extension is fond invalid
     */
    private DefaultInstalledExtension validateExtension(LocalExtension localExtension, String namespace,
        boolean register) throws InvalidExtensionException
    {
        InstalledFeature feature = getInstalledFeatureFromCache(localExtension.getId().getId(), namespace);
        if (feature != null && feature.extension != null) {
            // Already validated
            return feature.extension;
        }

        // Actually validate

        if (namespace != null && DefaultInstalledExtension.getNamespaces(localExtension) == null) {
            // This extension is supposed to be installed on root namespace only so redirecting to null namespace
            // initialization
            return validateExtension(localExtension, null, register);
        }

        if (!DefaultInstalledExtension.isInstalled(localExtension, namespace)) {
            throw new InvalidExtensionException(String.format("Extension [%s] is not installed", localExtension));
        }

        if (this.coreExtensionRepository.exists(localExtension.getId().getId())) {
            throw new InvalidExtensionException(String.format("Extension [%s] already exists as a core extension",
                localExtension));
        }

        // Validate dependencies
        InvalidExtensionException dependencyException = null;
        for (ExtensionDependency dependency : localExtension.getDependencies()) {
            try {
                validateDependency(dependency, namespace, register);
            } catch (InvalidExtensionException e) {
                // Continue to make sure all extension are validated in the right order
                if (dependencyException == null) {
                    dependencyException = e;
                }
            }
        }

        // Throw exception if any issue has been found with dependencies
        if (dependencyException != null) {
            throw dependencyException;
        }

        // Complete local extension installation
        return register ? addInstalledExtension(localExtension, namespace, true)
            : (DefaultInstalledExtension) localExtension;
    }

    private boolean isValid(DefaultInstalledExtension installedExtension, String namespace)
    {
        try {
            validateExtension(installedExtension, namespace, false);

            return true;
        } catch (InvalidExtensionException e) {
            this.logger.debug("Invalid extension [{}] on namespace [{}]", installedExtension.getId(), namespace, e);
        }

        return false;
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
     * @see #uninstallExtension(LocalExtension, String)
     */
    private void removeInstalledExtension(DefaultInstalledExtension installedExtension, String namespace)
    {
        removeInstalledFeature(installedExtension.getId().getId(), namespace);

        for (String feature : installedExtension.getFeatures()) {
            removeInstalledFeature(feature, namespace);
        }

        removeFromBackwardDependencies(installedExtension, namespace);

        if (!installedExtension.isInstalled()) {
            removeCachedExtension(installedExtension);
        }
    }

    /**
     * Uninstall provided extension.
     * 
     * @param feature the feature to uninstall
     * @param namespace the namespace
     * @see #uninstallExtension(LocalExtension, String)
     */
    private void removeInstalledFeature(String feature, String namespace)
    {
        // Extensions namespaces by feature

        if (namespace == null) {
            this.extensionNamespaceByFeature.remove(feature);
        } else {
            Map<String, InstalledFeature> namespaceInstalledExtension = this.extensionNamespaceByFeature.get(feature);

            namespaceInstalledExtension.remove(namespace);
        }
    }

    /**
     * Install provided extension.
     * 
     * @param localExtension the extension to install
     * @param namespace the namespace
     * @param dependency indicate if the extension is stored as a dependency of another one
     * @throws InstallException error when trying to uninstall extension
     * @see #installExtension(LocalExtension, String)
     */
    private void applyInstallExtension(DefaultInstalledExtension installedExtension, String namespace,
        boolean dependency) throws InstallException
    {
        // INSTALLED
        installedExtension.setInstalled(true, namespace);

        // DEPENDENCY
        installedExtension.setDependency(dependency, namespace);

        // Save properties
        try {
            this.localRepository.setProperties(installedExtension.getLocalExtension(),
                installedExtension.getProperties());
        } catch (Exception e) {
            throw new InstallException("Failed to modify extension descriptor", e);
        }

        // VALID
        installedExtension.setValid(namespace, isValid(installedExtension, namespace));

        // Update caches

        addInstalledExtension(installedExtension, namespace, true);
    }

    private void removeFromBackwardDependencies(DefaultInstalledExtension installedExtension, String namespace)
    {
        // Clean provided extension dependencies backward dependencies
        for (ExtensionDependency dependency : installedExtension.getDependencies()) {
            if (this.coreExtensionRepository.getCoreExtension(dependency.getId()) == null) {
                InstalledFeature installedFeature = getInstalledFeatureFromCache(dependency.getId(), namespace);

                if (installedFeature != null) {
                    installedFeature.backwardDependencies.remove(installedExtension);
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
     * @return the new {@link DefaultInstalledExtension}
     */
    private DefaultInstalledExtension addInstalledExtension(LocalExtension localExtension, String namespace,
        boolean valid)
    {
        DefaultInstalledExtension installedExtension = this.extensions.get(localExtension.getId());
        if (installedExtension == null) {
            installedExtension = new DefaultInstalledExtension(localExtension, this);
        }

        installedExtension.setInstalled(true, namespace);
        installedExtension.setValid(namespace, valid);

        addInstalledExtension(installedExtension, namespace);

        return installedExtension;
    }

    /**
     * Register a newly installed extension in backward dependencies map.
     * 
     * @param installedExtension the installed extension to register
     * @param namespace the namespace
     */
    private void addInstalledExtension(DefaultInstalledExtension installedExtension, String namespace)
    {
        addCachedExtension(installedExtension);

        // Register the extension in the installed extensions for the provided namespace
        addInstalledFeatureToCache(installedExtension.getId().getId(), namespace, installedExtension);

        // Add virtual extensions
        for (String feature : installedExtension.getFeatures()) {
            addInstalledFeatureToCache(feature, namespace, installedExtension);
        }

        if (installedExtension.isValid(namespace)) {
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
            installedExtension = new InstalledFeature(feature, namespace);
            installedExtensionsForFeature.put(namespace, installedExtension);
        }

        if (localExtension.isValid(namespace)) {
            installedExtension.extension = localExtension;
        } else {
            installedExtension.invalidExtensions.add(localExtension);
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
    public InstalledExtension getInstalledExtension(ExtensionId extensionId)
    {
        return this.extensions.get(extensionId);
    }

    @Override
    public InstalledExtension getInstalledExtension(String feature, String namespace)
    {
        InstalledFeature installedFeature = getInstalledFeatureFromCache(feature, namespace);

        if (installedFeature != null) {
            if (installedFeature.extension != null) {
                return installedFeature.extension;
            }

            return installedFeature.invalidExtensions.isEmpty() ? null : installedFeature.invalidExtensions.iterator()
                .next();
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
                throw new InstallException(String.format("The extension [%s] is already installed on namespace [%s]",
                    installedExtension, namespace));
            }

            installedExtension.setDependency(dependency, namespace);

            try {
                this.localRepository.setProperties(installedExtension.getLocalExtension(),
                    installedExtension.getProperties());
            } catch (Exception e) {
                throw new InstallException("Failed to modify extension descriptor", e);
            }
        } else {
            LocalExtension localExtension = this.localRepository.getLocalExtension(extension.getId());

            if (localExtension == null) {
                // Should be a very rare use case since we explicitly ask for a LocalExtension
                throw new InstallException(String.format("The extension [%s] need to be stored first", extension));
            }

            if (installedExtension == null) {
                installedExtension = new DefaultInstalledExtension(localExtension, this);
            }

            applyInstallExtension(installedExtension, namespace, dependency);
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
            throw new ResolveException(String.format("Extension [%s] is not installed on namespace [%s]", feature,
                namespace));
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

        DefaultInstalledExtension installedExtension = resolve(extensionId);

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

    // Search

    @Override
    public IterableResult<Extension> searchInstalledExtensions(String pattern, String namespace, int offset, int nb)
        throws SearchException
    {
        Pattern patternMatcher =
            StringUtils.isEmpty(pattern) ? null : Pattern.compile(RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX
                + Pattern.quote(pattern.toLowerCase()) + RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX);

        Set<Extension> set = new HashSet<Extension>();
        List<Extension> result = new ArrayList<Extension>(this.extensionsVersions.size());

        for (InstalledExtension installedExtension : this.extensions.values()) {
            if (installedExtension.isInstalled(namespace)) {
                if ((patternMatcher == null || RepositoryUtils.matches(patternMatcher, installedExtension))
                    && !set.contains(installedExtension)) {
                    result.add(installedExtension);
                    set.add(installedExtension);
                }
            }
        }

        return RepositoryUtils.getIterableResult(offset, nb, result);
    }
}
