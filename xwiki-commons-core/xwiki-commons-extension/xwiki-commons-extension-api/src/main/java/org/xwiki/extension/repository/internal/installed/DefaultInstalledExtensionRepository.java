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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.xwiki.extension.job.internal.ExtensionPlanContext;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.version.VersionConstraint;

/**
 * Default implementation of {@link InstalledExtensionRepository}.
 *
 * @version $Id$
 * @since 4.0M2
 */
@Component
@Singleton
public class DefaultInstalledExtensionRepository extends AbstractInstalledExtensionRepository<DefaultInstalledExtension>
    implements InstalledExtensionRepository, Initializable
{
    private static class InstalledRootFeature
    {
        DefaultInstalledExtension extension;

        final Set<DefaultInstalledExtension> invalidExtensions = new HashSet<>();

        final String namespace;

        final Set<DefaultInstalledExtension> backwardDependencies = new HashSet<>();

        Set<DefaultInstalledExtension> optionalBackwardDependencies = this.backwardDependencies;

        InstalledRootFeature(String namespace)
        {
            this.namespace = namespace;
        }

        Set<DefaultInstalledExtension> getBackwardDependencies(boolean withOptionals)
        {
            return withOptionals ? this.optionalBackwardDependencies : this.backwardDependencies;
        }

        void addBackwardDependency(DefaultInstalledExtension extension, boolean optional)
        {
            if (optional && this.optionalBackwardDependencies == this.backwardDependencies) {
                this.optionalBackwardDependencies = new HashSet<>(this.backwardDependencies);
            }

            this.optionalBackwardDependencies.add(extension);

            if (!optional) {
                this.backwardDependencies.add(extension);
            }
        }

        void removeBackwardDependency(DefaultInstalledExtension extension)
        {
            this.backwardDependencies.remove(extension);
            if (this.optionalBackwardDependencies != this.backwardDependencies) {
                this.optionalBackwardDependencies.remove(extension);
            }
        }
    }

    private static class InstalledFeature
    {
        final InstalledRootFeature root;

        final ExtensionId feature;

        InstalledFeature(InstalledRootFeature root, ExtensionId feature)
        {
            this.root = root;
            this.feature = feature;
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
     * The installed extensions sorted by provided feature and namespace.
     * <p>
     * <feature, <namespace, extension>>
     */
    private Map<String, Map<String, InstalledFeature>> extensionNamespaceByFeature = new ConcurrentHashMap<>();

    /**
     * Temporary map used only during init.
     * <p>
     * <feature, <namespace, extensions>>
     */
    private Map<String, Map<String, Set<LocalExtension>>> localInstalledExtensionsCache;

    private boolean updateBackwardDependencies;

    @Override
    public void initialize() throws InitializationException
    {
        setDescriptor(new DefaultExtensionRepositoryDescriptor("installed", "installed",
            this.localRepository.getDescriptor().getURI()));

        // Wait for all installed extension to be registered
        // before calculating backward dependencies
        this.updateBackwardDependencies = false;

        // Get installed extensions from local repository
        this.localInstalledExtensionsCache = new HashMap<>();
        for (LocalExtension localExtension : this.localRepository.getLocalExtensions()) {
            if (DefaultInstalledExtension.isInstalled(localExtension)) {
                getInstalledLocalExtension(localExtension);
            }
        }

        // Validate installed extensions
        validate();

        // Update backward dependencies
        updateMissingBackwardDependencies();

        // Put back backdependencies update for each extension add
        this.updateBackwardDependencies = true;

        // Reset temporary cache
        this.localInstalledExtensionsCache = null;
    }

    private void validate()
    {
        // Validate top level installed extensions
        for (LocalExtension localExtension : this.localRepository.getLocalExtensions()) {
            if (DefaultInstalledExtension.isInstalled(localExtension)) {
                validateExtension(localExtension, false);
            }
        }

        // Validate dependencies (just in case some dependencies don't have any backward dependencies)
        for (LocalExtension localExtension : this.localRepository.getLocalExtensions()) {
            if (DefaultInstalledExtension.isInstalled(localExtension)) {
                validateExtension(localExtension, true);
            }
        }
    }

    private void getInstalledLocalExtension(LocalExtension localExtension)
    {
        getInstalledLocalExtension(localExtension.getId().getId(), localExtension);

        for (ExtensionId feature : localExtension.getExtensionFeatures()) {
            getInstalledLocalExtension(feature.getId(), localExtension);
        }
    }

    private void getInstalledLocalExtension(String feature, LocalExtension localExtension)
    {
        Collection<String> namespaces = DefaultInstalledExtension.getNamespaces(localExtension);

        if (namespaces == null) {
            getInstalledLocalExtension(feature, null, localExtension);
        } else {
            for (String namespace : namespaces) {
                getInstalledLocalExtension(feature, namespace, localExtension);
            }
        }
    }

    private void getInstalledLocalExtension(String feature, String namespace, LocalExtension localExtension)
    {
        Map<String, Set<LocalExtension>> localInstallExtensionFeature =
            this.localInstalledExtensionsCache.computeIfAbsent(feature, key -> new HashMap<>());

        Set<LocalExtension> localInstallExtensionNamespace =
            localInstallExtensionFeature.computeIfAbsent(namespace, k -> new HashSet<>());

        localInstallExtensionNamespace.add(localExtension);
    }

    // Validation

    /**
     * Check extension validity and set it as not installed if not.
     *
     * @param localExtension the extension to validate
     * @param dependencies true if dependencies should be validated
     * @throws InvalidExtensionException when the passed extension is fond invalid
     */
    private void validateExtension(LocalExtension localExtension, boolean dependencies)
    {
        Collection<String> namespaces = DefaultInstalledExtension.getNamespaces(localExtension);

        if (namespaces == null) {
            if (dependencies || !DefaultInstalledExtension.isDependency(localExtension, null)) {
                try {
                    validateExtension(localExtension, null, new ExtensionPlanContext());
                } catch (InvalidExtensionException | StackOverflowError e) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.warn("Invalid extension [{}]", localExtension.getId(), e);
                    } else {
                        this.logger.warn("Invalid extension [{}] ({})", localExtension.getId(),
                            ExceptionUtils.getRootCauseMessage(e));
                    }

                    addInstalledExtension(localExtension, null, false);
                }
            }
        } else {
            for (String namespace : namespaces) {
                if (dependencies || !DefaultInstalledExtension.isDependency(localExtension, namespace)) {
                    try {
                        validateExtension(localExtension, namespace, new ExtensionPlanContext());
                    } catch (InvalidExtensionException | StackOverflowError e) {
                        if (this.logger.isDebugEnabled()) {
                            this.logger.warn("Invalid extension [{}] on namespace [{}]", localExtension.getId(),
                                namespace, e);
                        } else {
                            this.logger.warn("Invalid extension [{}] on namespace [{}] ({})", localExtension.getId(),
                                namespace, ExceptionUtils.getRootCauseMessage(e));
                        }

                        addInstalledExtension(localExtension, namespace, false);
                    }
                }
            }
        }
    }

    private LocalExtension getInstalledLocalExtension(ExtensionDependency dependency, String namespace)
    {
        Map<String, Set<LocalExtension>> localInstallExtensionFeature =
            this.localInstalledExtensionsCache.get(dependency.getId());

        if (localInstallExtensionFeature != null) {
            Set<LocalExtension> localInstallExtensionNamespace = localInstallExtensionFeature.get(namespace);

            if (localInstallExtensionNamespace != null) {
                for (LocalExtension dependencyVersion : localInstallExtensionNamespace) {
                    if (isCompatible(dependencyVersion, dependency)) {
                        return dependencyVersion;
                    }
                }
            }
        }

        // Try on root namespace
        if (namespace != null) {
            return getInstalledLocalExtension(dependency, null);
        }

        return null;
    }

    private void validateDependency(ExtensionDependency dependency, String namespace,
        ExtensionPlanContext extensionContext) throws InvalidExtensionException
    {
        CoreExtension coreExtension = this.coreExtensionRepository.getCoreExtension(dependency.getId());

        if (coreExtension != null) {
            if (!isCompatible(coreExtension, dependency)) {
                throw new InvalidExtensionException(String
                    .format("Dependency [%s] is incompatible with the core extension [%s]", dependency, coreExtension));
            }
        } else {
            LocalExtension dependencyExtension =
                this.localInstalledExtensionsCache != null ? getInstalledLocalExtension(dependency, namespace)
                    : getInstalledExtension(dependency.getId(), namespace);

            if (dependencyExtension == null) {
                throw new InvalidExtensionException(
                    String.format("No compatible extension is installed for dependency [%s]", dependency));
            } else {
                try {
                    DefaultInstalledExtension installedExtension = validateExtension(dependencyExtension, namespace,
                        new ExtensionPlanContext(extensionContext, dependency));

                    if (!installedExtension.isValid(namespace)) {
                        throw new InvalidExtensionException(
                            String.format("Extension dependency [%s] is invalid", installedExtension.getId()));
                    }
                } catch (InvalidExtensionException | StackOverflowError e) {
                    if (this.localInstalledExtensionsCache != null) {
                        addInstalledExtension(dependencyExtension, namespace, false);
                    }

                    if (e instanceof InvalidExtensionException) {
                        throw e;
                    }

                    throw new InvalidExtensionException(String.format(
                        "Unknown problem when validating installed extension dependency [%s] on namespace [%s]",
                        dependency, namespace), e);
                }
            }
        }
    }

    /**
     * Check extension validity against a specific namespace.
     *
     * @param localExtension the extension to validate
     * @param namespace the namespace
     * @param extensionContext the current extension context
     * @return the corresponding {@link DefaultInstalledExtension}
     * @throws InvalidExtensionException when the passed extension is fond invalid
     */
    private DefaultInstalledExtension validateExtension(LocalExtension localExtension, String namespace,
        ExtensionPlanContext extensionContext) throws InvalidExtensionException
    {
        DefaultInstalledExtension installedExtension = this.extensions.get(localExtension.getId());
        if (installedExtension != null && installedExtension.isValidated(namespace)) {
            // Already validated
            return installedExtension;
        }

        // Actually validate

        if (namespace != null && DefaultInstalledExtension.getNamespaces(localExtension) == null) {
            // This extension is supposed to be installed on root namespace only so redirecting to null namespace
            // initialization
            return validateExtension(localExtension, null, extensionContext);
        }

        if (!DefaultInstalledExtension.isInstalled(localExtension, namespace)) {
            throw new InvalidExtensionException(String.format("Extension [%s] is not installed", localExtension));
        }

        if (this.coreExtensionRepository.exists(localExtension.getId().getId())) {
            throw new InvalidExtensionException(
                String.format("Extension [%s] already exists as a core extension", localExtension));
        }

        // Validate dependencies
        InvalidExtensionException dependencyException = null;
        for (ExtensionDependency dependency : localExtension.getDependencies()) {
            try {
                // Replace with managed dependency if any
                dependency = extensionContext.getDependency(dependency, localExtension);

                // Check for excludes
                if (!extensionContext.isExcluded(dependency)) {
                    validateDependency(dependency, namespace,
                        new ExtensionPlanContext(extensionContext, localExtension));
                }
            } catch (InvalidExtensionException | StackOverflowError e) {
                if (!dependency.isOptional()) {
                    // Continue to make sure all extensions are validated in the right order
                    if (dependencyException == null) {
                        if (e instanceof InvalidExtensionException) {
                            dependencyException = (InvalidExtensionException) e;
                        } else {
                            dependencyException = new InvalidExtensionException(String.format(
                                "Unknown problem when validating installed extension dependency [%s] on namespace [%s]",
                                dependency, namespace), e);
                        }
                    }
                }
            }
        }

        // Throw exception if any issue has been found with dependencies
        if (dependencyException != null) {
            throw dependencyException;
        }

        // Complete local extension installation
        return localExtension instanceof DefaultInstalledExtension ? (DefaultInstalledExtension) localExtension
            : addInstalledExtension(localExtension, namespace, true);
    }

    private boolean isValid(DefaultInstalledExtension installedExtension, String namespace,
        ExtensionPlanContext extensionContext)
    {
        try {
            validateExtension(installedExtension, namespace, extensionContext);

            return true;
        } catch (InvalidExtensionException e) {
            this.logger.debug("Invalid extension [{}] on namespace [{}]", installedExtension.getId(), namespace, e);
        }

        return false;
    }

    private boolean isCompatible(Extension extension, ExtensionDependency dependency)
    {
        return isCompatible(extension, dependency.getId(), dependency.getVersionConstraint());
    }

    private boolean isCompatible(Extension extension, String feature, VersionConstraint versionConstraint)
    {
        ExtensionId featureExtension = extension.getExtensionFeature(feature);

        return versionConstraint.isCompatible(featureExtension.getVersion());
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

        for (ExtensionId feature : installedExtension.getExtensionFeatures()) {
            removeInstalledFeature(feature.getId(), namespace);
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
     * @param installedExtension the extension to install
     * @param namespace the namespace
     * @param dependency indicate if the extension is stored as a dependency of another one
     * @param properties the custom properties to set on the installed extension for the specified namespace
     * @param extensionContext the current extension context
     * @throws InstallException error when trying to uninstall extension
     * @see #installExtension(LocalExtension, String)
     */
    private void applyInstallExtension(DefaultInstalledExtension installedExtension, String namespace,
        boolean dependency, Map<String, Object> properties, ExtensionPlanContext extensionContext)
        throws InstallException
    {
        // INSTALLED
        installedExtension.setInstalled(true, namespace);
        installedExtension.setInstallDate(new Date(), namespace);

        // DEPENDENCY
        installedExtension.setDependency(dependency, namespace);

        // Add custom install properties for the specified namespace. The map holding the namespace properties should
        // not be null because it is initialized by the InstalledExtension#setInstalled(true, namespace) call above.
        installedExtension.getNamespaceProperties(namespace).putAll(properties);

        // Save properties
        try {
            this.localRepository.setProperties(installedExtension.getLocalExtension(),
                installedExtension.getProperties());
        } catch (Exception e) {
            throw new InstallException("Failed to modify extension descriptor", e);
        }

        // VALID
        installedExtension.setValid(namespace, isValid(installedExtension, namespace, extensionContext));

        // Update caches

        addInstalledExtension(installedExtension, namespace);
    }

    private void removeFromBackwardDependencies(DefaultInstalledExtension installedExtension, String namespace)
    {
        // Clean provided extension dependencies backward dependencies
        for (ExtensionDependency dependency : installedExtension.getDependencies()) {
            if (this.coreExtensionRepository.getCoreExtension(dependency.getId()) == null) {
                InstalledFeature installedFeature = getInstalledFeatureFromCache(dependency.getId(), namespace);

                if (installedFeature != null) {
                    installedFeature.root.removeBackwardDependency(installedExtension);
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

        boolean isValid = installedExtension.isValid(namespace);

        // Register the extension in the installed extensions for the provided namespace
        addInstalledFeatureToCache(installedExtension.getId(), namespace, installedExtension, isValid);

        // Add virtual extensions
        for (ExtensionId feature : installedExtension.getExtensionFeatures()) {
            addInstalledFeatureToCache(feature, namespace, installedExtension, isValid);
        }

        if (this.updateBackwardDependencies) {
            // Recalculate backward dependencies index
            updateMissingBackwardDependencies();
        }
    }

    private void updateMissingBackwardDependencies()
    {
        for (DefaultInstalledExtension installedExtension : this.extensions.values()) {
            updateMissingBackwardDependencies(installedExtension);
        }
    }

    private void updateMissingBackwardDependencies(DefaultInstalledExtension installedExtension)
    {
        Collection<String> namespaces = installedExtension.getNamespaces();

        if (namespaces == null) {
            if (installedExtension.isValid(null)) {
                updateMissingBackwardDependencies(installedExtension, null);
            }
        } else {
            for (String namespace : namespaces) {
                if (installedExtension.isValid(namespace)) {
                    updateMissingBackwardDependencies(installedExtension, namespace);
                }
            }
        }
    }

    private void updateMissingBackwardDependencies(DefaultInstalledExtension installedExtension, String namespace)
    {
        // Add the extension as backward dependency
        for (ExtensionDependency dependency : installedExtension.getDependencies()) {
            if (!this.coreExtensionRepository.exists(dependency.getId())) {
                // Get the extension for the dependency feature for the provided namespace
                DefaultInstalledExtension dependencyLocalExtension =
                    (DefaultInstalledExtension) getInstalledExtension(dependency.getId(), namespace);

                if (dependencyLocalExtension != null) {
                    ExtensionId feature = dependencyLocalExtension.getExtensionFeature(dependency.getId());

                    // Make sure to register backward dependency on the right namespace
                    InstalledFeature dependencyInstalledExtension =
                        addInstalledFeatureToCache(feature, namespace, dependencyLocalExtension, false);

                    dependencyInstalledExtension.root.addBackwardDependency(installedExtension,
                        dependency.isOptional());
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
     * @param installedExtension the extension
     * @return the installed extension informations
     */
    private InstalledFeature addInstalledFeatureToCache(ExtensionId feature, String namespace,
        DefaultInstalledExtension installedExtension, boolean forceCreate)
    {
        Map<String, InstalledFeature> installedExtensionsForFeature =
            this.extensionNamespaceByFeature.get(feature.getId());

        if (installedExtensionsForFeature == null) {
            installedExtensionsForFeature = new HashMap<>();
            this.extensionNamespaceByFeature.put(feature.getId(), installedExtensionsForFeature);
        }

        InstalledFeature installedFeature = installedExtensionsForFeature.get(namespace);
        if (forceCreate || installedFeature == null) {
            // Find or create root feature
            InstalledRootFeature rootInstalledFeature;
            if (installedExtension.getId().getId().equals(feature.getId())) {
                rootInstalledFeature = new InstalledRootFeature(namespace);
            } else {
                rootInstalledFeature = getInstalledFeatureFromCache(installedExtension.getId().getId(), namespace).root;
            }

            // Create new feature
            installedFeature = new InstalledFeature(rootInstalledFeature, feature);

            // Add new feature
            installedExtensionsForFeature.put(namespace, installedFeature);
        }

        if (installedExtension.isValid(namespace)) {
            installedFeature.root.extension = installedExtension;
        } else {
            installedFeature.root.invalidExtensions.add(installedExtension);
        }

        return installedFeature;
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
        if (feature == null) {
            return null;
        }

        Map<String, InstalledFeature> installedExtensionsForFeature = this.extensionNamespaceByFeature.get(feature);

        if (installedExtensionsForFeature == null) {
            return null;
        }

        InstalledFeature installedExtension = installedExtensionsForFeature.get(namespace);

        // Fallback on root namespace if the feature could not be found
        if (installedExtension == null && namespace != null) {
            installedExtension = getInstalledFeatureFromCache(feature, null);
        }

        return installedExtension;
    }

    // InstalledExtensionRepository

    @Override
    public InstalledExtension getInstalledExtension(String feature, String namespace)
    {
        InstalledFeature installedFeature = getInstalledFeatureFromCache(feature, namespace);

        if (installedFeature != null) {
            if (installedFeature.root.extension != null) {
                return installedFeature.root.extension;
            }

            return installedFeature.root.invalidExtensions.isEmpty() ? null
                : installedFeature.root.invalidExtensions.iterator().next();
        }

        return null;
    }

    @Override
    public InstalledExtension installExtension(LocalExtension extension, String namespace, boolean dependency,
        Map<String, Object> properties) throws InstallException
    {
        DefaultInstalledExtension installedExtension = this.extensions.get(extension.getId());

        if (installedExtension != null && installedExtension.isInstalled(namespace)
            && installedExtension.isValid(namespace)) {
            if (installedExtension.isDependency(namespace) == dependency) {
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

            applyInstallExtension(installedExtension, namespace, dependency, properties, new ExtensionPlanContext());
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
    public Collection<InstalledExtension> getBackwardDependencies(String feature, String namespace,
        boolean withOptionals) throws ResolveException
    {
        if (getInstalledExtension(feature, namespace) == null) {
            throw new ResolveException(
                String.format("Extension [%s] is not installed on namespace [%s]", feature, namespace));
        }

        return getSafeBackwardDependencies(feature, namespace, withOptionals);
    }

    private Collection<InstalledExtension> getSafeBackwardDependencies(String feature, String namespace,
        boolean withOptionals)
    {
        Map<String, InstalledFeature> installedExtensionsByFeature = this.extensionNamespaceByFeature.get(feature);
        if (installedExtensionsByFeature != null) {
            InstalledFeature installedExtension = installedExtensionsByFeature.get(namespace);

            if (installedExtension != null) {
                Set<DefaultInstalledExtension> backwardDependencies =
                    installedExtension.root.getBackwardDependencies(withOptionals);

                // copy the list to allow use cases like uninstalling all backward dependencies without getting a
                // concurrent issue on the list
                return backwardDependencies.isEmpty() ? Collections.<InstalledExtension>emptyList()
                    : new ArrayList<>(backwardDependencies);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public Map<String, Collection<InstalledExtension>> getBackwardDependencies(ExtensionId extensionId,
        boolean withOptionals) throws ResolveException
    {
        Map<String, Collection<InstalledExtension>> result;

        DefaultInstalledExtension installedExtension = resolve(extensionId);

        Collection<String> namespaces = installedExtension.getNamespaces();

        Map<String, InstalledFeature> featureExtensions =
            this.extensionNamespaceByFeature.get(installedExtension.getId().getId());
        if (featureExtensions != null) {
            result = new HashMap<>();
            for (InstalledFeature festureExtension : featureExtensions.values()) {
                if ((namespaces == null || namespaces.contains(festureExtension.root.namespace))) {
                    Set<DefaultInstalledExtension> backwardDependencies =
                        festureExtension.root.getBackwardDependencies(withOptionals);
                    if (!backwardDependencies.isEmpty()) {
                        // copy the list to allow use cases like uninstalling all backward dependencies without getting
                        // a concurrent issue on the list
                        result.put(festureExtension.root.namespace,
                            new ArrayList<InstalledExtension>(backwardDependencies));
                    }
                }
            }
        } else {
            result = Collections.emptyMap();
        }

        return result;
    }
}
