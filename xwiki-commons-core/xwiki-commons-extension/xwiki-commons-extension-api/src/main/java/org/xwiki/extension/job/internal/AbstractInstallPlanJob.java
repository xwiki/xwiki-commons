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
package org.xwiki.extension.job.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.namespace.NamespaceNotAllowedException;
import org.xwiki.component.namespace.NamespaceValidator;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanAction;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanNode;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanTree;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.version.IncompatibleVersionConstraintException;
import org.xwiki.extension.version.VersionConstraint;

/**
 * Create an Extension plan.
 *
 * @version $Id$
 * @since 4.1M1
 */
public abstract class AbstractInstallPlanJob<R extends ExtensionRequest> extends AbstractExtensionPlanJob<R>
{
    protected static class ModifableExtensionPlanTree extends DefaultExtensionPlanTree implements Cloneable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public ModifableExtensionPlanTree clone()
        {
            ModifableExtensionPlanTree tree = new ModifableExtensionPlanTree();

            for (ExtensionPlanNode node : this) {
                tree.add(((ModifableExtensionPlanNode) node).clone());
            }

            return tree;
        }
    }

    protected static class ModifableExtensionPlanNode extends DefaultExtensionPlanNode implements Cloneable
    {
        // never change

        private final ExtensionDependency initialDependency;

        // can change

        public VersionConstraint versionConstraint;

        public final List<ModifableExtensionPlanNode> duplicates = new ArrayList<ModifableExtensionPlanNode>();

        // helpers

        public ModifableExtensionPlanNode()
        {
            this.initialDependency = null;
        }

        public ModifableExtensionPlanNode(ExtensionDependency initialDependency, VersionConstraint versionConstraint)
        {
            this.initialDependency = initialDependency;
            this.versionConstraint = versionConstraint;
        }

        public ModifableExtensionPlanNode(ExtensionDependency initialDependency, ModifableExtensionPlanNode node)
        {
            this.initialDependency = initialDependency;

            set(node);
        }

        @Override
        protected ModifableExtensionPlanNode clone()
        {
            try {
                return (ModifableExtensionPlanNode) super.clone();
            } catch (CloneNotSupportedException e) {
                // this shouldn't happen, since we are Cloneable
                throw new InternalError();
            }
        }

        public void set(ModifableExtensionPlanNode node)
        {
            this.action = node.action;
            this.children = node.children;
        }

        @Override
        public VersionConstraint getInitialVersionConstraint()
        {
            return this.initialDependency != null ? this.initialDependency.getVersionConstraint() : null;
        }

        public void setAction(ExtensionPlanAction action)
        {
            this.action = action;
        }

        public void setChildren(Collection<? extends ExtensionPlanNode> children)
        {
            this.children = (Collection) children;
        }
    }

    /**
     * Used to resolve extensions to install.
     */
    @Inject
    protected ExtensionRepositoryManager repositoryManager;

    /**
     * Used to check if extension or its dependencies are already core extensions.
     */
    @Inject
    protected CoreExtensionRepository coreExtensionRepository;

    @Inject
    private NamespaceValidator namespaceResolver;

    /**
     * Used to make sure dependencies are compatible between each other in the whole plan.
     * <p>
     * <id, <namespace, node>>.
     */
    private Map<String, Map<String, ModifableExtensionPlanNode>> extensionsNodeCache =
        new HashMap<String, Map<String, ModifableExtensionPlanNode>>();

    protected void setExtensionTree(ModifableExtensionPlanTree extensionTree)
    {
        this.extensionTree = extensionTree;
        this.status.setTree(this.extensionTree);
    }

    /**
     * @param extensionsByNamespace the map to fill
     * @param extensionId the id of the extension to install/upgrade
     * @param namespace the namespace where to install the extension
     */
    protected void addExtensionToProcess(Map<ExtensionId, Collection<String>> extensionsByNamespace,
        ExtensionId extensionId, String namespace)
    {
        Collection<String> namespaces;

        // Get namespaces
        if (extensionsByNamespace.containsKey(extensionId) && namespace != null) {
            namespaces = extensionsByNamespace.get(extensionId);
        } else {
            if (namespace == null) {
                namespaces = null;
            } else {
                namespaces = new HashSet<String>();
            }

            extensionsByNamespace.put(extensionId, namespaces);
        }

        // Add namespace
        if (namespaces != null) {
            namespaces.add(namespace);
        }
    }

    protected void start(Map<ExtensionId, Collection<String>> extensionsByNamespace) throws Exception
    {
        this.progressManager.pushLevelProgress(extensionsByNamespace.size(), this);

        try {
            for (Map.Entry<ExtensionId, Collection<String>> entry : extensionsByNamespace.entrySet()) {
                this.progressManager.startStep(this);

                ExtensionId extensionId = entry.getKey();
                Collection<String> namespaces = entry.getValue();

                if (namespaces != null) {
                    this.progressManager.pushLevelProgress(namespaces.size(), this);

                    try {
                        for (String namespace : namespaces) {
                            this.progressManager.startStep(this);

                            installExtension(extensionId, namespace, this.extensionTree);
                        }
                    } finally {
                        this.progressManager.popLevelProgress(this);
                    }
                } else {
                    installExtension(extensionId, null, this.extensionTree);
                }
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    private ModifableExtensionPlanNode getExtensionNode(String id, String namespace)
    {
        Map<String, ModifableExtensionPlanNode> extensionsById = this.extensionsNodeCache.get(id);

        ModifableExtensionPlanNode node = null;
        if (extensionsById != null) {
            node = extensionsById.get(namespace);

            if (node == null && namespace != null) {
                node = extensionsById.get(null);
            }
        }

        return node;
    }

    private void addExtensionNode(ModifableExtensionPlanNode node)
    {
        String id = node.getAction().getExtension().getId().getId();

        Map<String, ModifableExtensionPlanNode> extensionsById = this.extensionsNodeCache.get(id);

        if (extensionsById == null) {
            extensionsById = new HashMap<String, ModifableExtensionPlanNode>();
            this.extensionsNodeCache.put(id, extensionsById);
        }

        ModifableExtensionPlanNode existingNode = extensionsById.get(node.getAction().getNamespace());

        if (existingNode != null) {
            existingNode.set(node);
            for (ModifableExtensionPlanNode duplicate : existingNode.duplicates) {
                duplicate.set(node);
            }
            existingNode.duplicates.add(node);
        } else {
            extensionsById.put(node.getAction().getNamespace(), node);
        }
    }

    /**
     * Install provided extension.
     *
     * @param extensionId the identifier of the extension to install
     * @param namespace the namespace where to install the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws InstallException error when trying to install provided extension
     */
    protected void installExtension(ExtensionId extensionId, String namespace, ModifableExtensionPlanTree parentBranch)
        throws InstallException
    {
        try {
            installExtension(extensionId, false, namespace, parentBranch);
        } catch (ResolveException e) {
            throw new InstallException("An unexpected exception has been raised", e);
        }
    }

    /**
     * Install provided extension.
     *
     * @param extensionId the identifier of the extension to install
     * @param dependency indicate if the extension is installed as a dependency
     * @param namespace the namespace where to install the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws InstallException error when trying to install provided extension
     * @throws ResolveException unexpected exception has been raised
     */
    protected void installExtension(ExtensionId extensionId, boolean dependency, String namespace,
        ModifableExtensionPlanTree parentBranch) throws InstallException, ResolveException
    {
        if (getRequest().isVerbose()) {
            if (namespace != null) {
                this.logger.info(LOG_RESOLVE_NAMESPACE, "Resolving extension [{}] on namespace [{}]", extensionId,
                    namespace);
            } else {
                this.logger.info(LOG_RESOLVE, "Resolving extension [{}] on all namespaces", extensionId);
            }
        }

        // Check if the feature is already in the install plan
        if (checkExistingPlanNodeExtension(extensionId, true, namespace)) {
            return;
        }

        // Check if the feature is a core extension
        checkCoreExtension(extensionId.getId());

        // Check if the feature is a root extension
        if (namespace != null) {
            // If a namespace extension already exist on root, fail the install
            checkRootExtension(extensionId.getId());
        }

        ModifableExtensionPlanNode node = installExtension(extensionId, dependency, namespace);

        addExtensionNode(node);
        parentBranch.add(node);
    }

    private boolean checkCoreDependency(ExtensionDependency extensionDependency,
        List<ModifableExtensionPlanNode> parentBranch) throws InstallException
    {
        CoreExtension coreExtension = this.coreExtensionRepository.getCoreExtension(extensionDependency.getId());

        if (coreExtension != null) {
            ExtensionId feature = coreExtension.getExtensionFeature(extensionDependency.getId());
            if (!extensionDependency.getVersionConstraint().isCompatible(feature.getVersion())) {
                throw new InstallException("Dependency [" + extensionDependency
                    + "] is not compatible with core extension feature [" + feature + "] ([" + coreExtension + "])");
            } else {
                if (getRequest().isVerbose()) {
                    this.logger.debug(
                        "There is already a core extension feature [{}] ([{}]) covering extension dependency [{}]",
                        feature, coreExtension, extensionDependency);
                }

                ModifableExtensionPlanNode node =
                    new ModifableExtensionPlanNode(extensionDependency, extensionDependency.getVersionConstraint());
                node.setAction(new DefaultExtensionPlanAction(coreExtension, null, Action.NONE, null, true));

                parentBranch.add(node);

                return true;
            }
        }

        return false;
    }

    private VersionConstraint checkExistingPlanNodeDependency(ExtensionDependency extensionDependency, String namespace,
        List<ModifableExtensionPlanNode> parentBranch, VersionConstraint previousVersionConstraint)
            throws InstallException
    {
        VersionConstraint versionConstraint = previousVersionConstraint;

        ModifableExtensionPlanNode existingNode = getExtensionNode(extensionDependency.getId(), namespace);
        if (existingNode != null) {
            if (versionConstraint.isCompatible(existingNode.getAction().getExtension().getId().getVersion())) {
                ModifableExtensionPlanNode node = new ModifableExtensionPlanNode(extensionDependency, existingNode);
                addExtensionNode(node);
                parentBranch.add(node);

                return null;
            } else {
                if (existingNode.versionConstraint != null) {
                    try {
                        versionConstraint = versionConstraint.merge(existingNode.versionConstraint);
                    } catch (IncompatibleVersionConstraintException e) {
                        throw new InstallException("Dependency [" + extensionDependency
                            + "] is incompatible with current constaint [" + existingNode.versionConstraint + "]", e);
                    }
                } else {
                    throw new InstallException("Dependency [" + extensionDependency + "] incompatible with extension ["
                        + existingNode.getAction().getExtension() + "]");
                }
            }
        }

        return versionConstraint;
    }

    private boolean checkExistingPlanNodeExtension(Extension extension, String namespace) throws InstallException
    {
        if (checkExistingPlanNodeExtension(extension.getId(), true, namespace)) {
            return true;
        }

        for (ExtensionId feature : extension.getExtensionFeatures()) {
            checkExistingPlanNodeExtension(feature, false, namespace);
        }

        return false;
    }

    private boolean checkExistingPlanNodeExtension(ExtensionId extensionId, boolean isId, String namespace)
        throws InstallException
    {
        ModifableExtensionPlanNode existingNode = getExtensionNode(extensionId.getId(), namespace);
        if (existingNode != null) {
            if (isId && existingNode.getAction().getExtension().getId().equals(extensionId)) {
                // Same extension already planned for install
                return true;
            }

            if (existingNode.versionConstraint != null) {
                if (!existingNode.versionConstraint.isCompatible(extensionId.getVersion())) {
                    throw new InstallException(
                        String.format("Extension feature [%s] is incompatible with existing constraint [%s]",
                            extensionId, existingNode.versionConstraint));
                }
            }
        }

        return false;
    }

    private ExtensionDependency checkInstalledDependency(ExtensionDependency extensionDependency,
        VersionConstraint versionConstraint, String namespace, List<ModifableExtensionPlanNode> parentBranch)
            throws InstallException
    {
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(extensionDependency.getId(), namespace);

        ExtensionDependency targetDependency = extensionDependency;

        if (installedExtension != null) {
            // Check if already installed version is compatible
            if (installedExtension.isValid(namespace)) {
                ExtensionId feature = installedExtension.getExtensionFeature(extensionDependency.getId());

                if (versionConstraint.isCompatible(feature.getVersion())) {
                    if (getRequest().isVerbose()) {
                        this.logger.debug(
                            "There is already an installed extension [{}] covering extension dependency [{}]",
                            installedExtension.getId(), extensionDependency);
                    }

                    ModifableExtensionPlanNode node =
                        new ModifableExtensionPlanNode(extensionDependency, versionConstraint);
                    node.setAction(new DefaultExtensionPlanAction(installedExtension, null, Action.NONE, namespace,
                        installedExtension.isDependency(namespace)));

                    addExtensionNode(node);
                    parentBranch.add(node);

                    return null;
                }
            }

            // If incompatible root extension fail
            if (namespace != null && installedExtension.isInstalled(null)) {
                throw new InstallException(
                    String.format("Dependency [%s] is incompatible with installed root extension [%s]",
                        extensionDependency, installedExtension.getId()));
            }

            // If not compatible with it, try to merge dependencies constraint of all backward dependencies to find a
            // new compatible version for this extension
            VersionConstraint mergedVersionContraint;
            try {
                if (installedExtension.isInstalled(null)) {
                    Map<String, Collection<InstalledExtension>> backwardDependencies =
                        this.installedExtensionRepository.getBackwardDependencies(installedExtension.getId());

                    mergedVersionContraint = mergeVersionConstraints(backwardDependencies.get(null),
                        extensionDependency.getId(), versionConstraint);
                    if (namespace != null) {
                        mergedVersionContraint = mergeVersionConstraints(backwardDependencies.get(namespace),
                            extensionDependency.getId(), mergedVersionContraint);
                    }
                } else {
                    Collection<InstalledExtension> backwardDependencies = this.installedExtensionRepository
                        .getBackwardDependencies(installedExtension.getId().getId(), namespace);

                    mergedVersionContraint =
                        mergeVersionConstraints(backwardDependencies, extensionDependency.getId(), versionConstraint);
                }
            } catch (IncompatibleVersionConstraintException e) {
                throw new InstallException("Provided depency is incompatible with already installed extensions", e);
            } catch (ResolveException e) {
                throw new InstallException("Failed to resolve backward dependencies", e);
            }

            if (mergedVersionContraint != versionConstraint) {
                targetDependency = new DefaultExtensionDependency(extensionDependency, mergedVersionContraint);
            }
        }

        return targetDependency;
    }

    /**
     * Install provided extension dependency.
     *
     * @param extensionDependency the extension dependency to install
     * @param namespace the namespace where to install the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws InstallException error when trying to install provided extension
     * @throws ResolveException
     * @throws IncompatibleVersionConstraintException
     */
    private void installExtensionDependency(ExtensionDependency extensionDependency, String namespace,
        List<ModifableExtensionPlanNode> parentBranch)
            throws InstallException, IncompatibleVersionConstraintException, ResolveException
    {
        if (getRequest().isVerbose()) {
            if (namespace != null) {
                this.logger.info(LOG_RESOLVEDEPENDENCY_NAMESPACE,
                    "Resolving extension dependency [{}] on namespace [{}]", extensionDependency, namespace);
            } else {
                this.logger.info(LOG_RESOLVEDEPENDENCY, "Resolving extension dependency [{}] on all namespaces",
                    extensionDependency);
            }
        }

        VersionConstraint versionConstraint = extensionDependency.getVersionConstraint();

        // Make sure the dependency is not already a core extension
        if (checkCoreDependency(extensionDependency, parentBranch)) {
            // Already exists and added to the tree by #checkCoreExtension
            return;
        }

        // Make sure the dependency is not already in the current plan
        versionConstraint =
            checkExistingPlanNodeDependency(extensionDependency, namespace, parentBranch, versionConstraint);
        if (versionConstraint == null) {
            // Already exists and added to the tree by #checkExistingPlan
            return;
        }

        // Check installed extensions
        ExtensionDependency targetDependency =
            checkInstalledDependency(extensionDependency, versionConstraint, namespace, parentBranch);
        if (targetDependency == null) {
            // Already exists and added to the tree by #checkInstalledExtension
            return;
        }

        // For root dependency make sure to generate a version constraint compatible with any already existing
        // namespace extension backward dependency
        if (namespace == null) {
            versionConstraint = mergeBackwardDependenciesVersionConstraints(targetDependency.getId(), namespace,
                targetDependency.getVersionConstraint());
            targetDependency = new DefaultExtensionDependency(targetDependency, versionConstraint);
        }

        // Not found locally, search it remotely
        ModifableExtensionPlanNode node = installExtensionDependency(targetDependency, true, namespace);

        node.versionConstraint = versionConstraint;

        addExtensionNode(node);
        parentBranch.add(node);
    }

    /**
     * Install provided extension.
     *
     * @param targetDependency used to search the extension to install in remote repositories
     * @param dependency indicate if the extension is installed as a dependency
     * @param namespace the namespace where to install the extension
     * @return the install plan node for the provided extension
     * @throws InstallException error when trying to install provided extension
     */
    private ModifableExtensionPlanNode installExtensionDependency(ExtensionDependency targetDependency,
        boolean dependency, String namespace) throws InstallException
    {
        this.progressManager.pushLevelProgress(2, this);

        try {
            this.progressManager.startStep(this);

            // Check if the extension is already in local repository
            Extension extension = resolveExtension(targetDependency);

            this.progressManager.startStep(this);

            try {
                return installExtension(extension, dependency, namespace, targetDependency);
            } catch (Exception e) {
                throw new InstallException(
                    String.format("Failed to create an install plan for extension dependency [%s]", targetDependency),
                    e);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    /**
     * @param extensions the extensions containing the dependencies for which to merge the constraints
     * @param feature the id of the dependency
     * @param previousMergedVersionContraint if not null it's merged with the provided extension dependencies version
     *            constraints
     * @return the merged version constraint
     * @throws IncompatibleVersionConstraintException the provided version constraint is compatible with the provided
     *             version constraint
     */
    private VersionConstraint mergeVersionConstraints(Collection<? extends Extension> extensions, String feature,
        VersionConstraint previousMergedVersionContraint) throws IncompatibleVersionConstraintException
    {
        VersionConstraint mergedVersionContraint = previousMergedVersionContraint;

        if (extensions != null) {
            for (Extension extension : extensions) {
                ExtensionDependency dependency = getDependency(extension, feature);

                if (dependency != null) {
                    if (mergedVersionContraint == null) {
                        mergedVersionContraint = dependency.getVersionConstraint();
                    } else {
                        mergedVersionContraint = mergedVersionContraint.merge(dependency.getVersionConstraint());
                    }
                }
            }
        }

        return mergedVersionContraint;
    }

    /**
     * Extract extension with the provided id from the provided extension.
     *
     * @param extension the extension
     * @param dependencyId the id of the dependency
     * @return the extension dependency or null if none has been found
     */
    private ExtensionDependency getDependency(Extension extension, String dependencyId)
    {
        for (ExtensionDependency dependency : extension.getDependencies()) {
            if (dependency.getId().equals(dependencyId)) {
                return dependency;
            }
        }

        return null;
    }

    /**
     * Install provided extension.
     *
     * @param extensionId the identifier of the extension to install
     * @param dependency indicate if the extension is installed as a dependency
     * @param namespace the namespace where to install the extension
     * @return the install plan node for the provided extension
     * @throws InstallException error when trying to install provided extension
     */
    private ModifableExtensionPlanNode installExtension(ExtensionId extensionId, boolean dependency, String namespace)
        throws InstallException
    {
        this.progressManager.pushLevelProgress(2, this);

        try {
            this.progressManager.startStep(this);

            // Check is the extension is already in local repository
            Extension extension = resolveExtension(extensionId);

            this.progressManager.startStep(this);

            try {
                return installExtension(extension, dependency, namespace, null);
            } catch (Exception e) {
                throw new InstallException("Failed to resolve extension", e);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    /**
     * @param extensionId the identifier of the extension to install
     * @return the extension
     * @throws InstallException error when trying to resolve extension
     */
    private Extension resolveExtension(ExtensionId extensionId) throws InstallException
    {
        // Check is the extension is already in local repository
        Extension extension = this.localExtensionRepository.getLocalExtension(extensionId);

        if (extension == null) {
            this.logger.debug("Can't find extension in local repository, trying to download it.");

            // Resolve extension
            try {
                extension = this.repositoryManager.resolve(extensionId);
            } catch (ResolveException e1) {
                throw new InstallException(String.format("Failed to resolve extension [%s]", extensionId), e1);
            }
        }

        return extension;
    }

    /**
     * @param extensionDependency describe the extension to install
     * @return the extension
     * @throws InstallException error when trying to resolve extension
     */
    private Extension resolveExtension(ExtensionDependency extensionDependency) throws InstallException
    {
        // Check is the extension is already in local repository
        Extension extension;
        try {
            extension = this.localExtensionRepository.resolve(extensionDependency);
        } catch (ResolveException e) {
            this.logger.debug("Can't find extension dependency in local repository, trying to download it.", e);

            // Resolve extension
            try {
                extension = this.repositoryManager.resolve(extensionDependency);
            } catch (ResolveException e1) {
                throw new InstallException(
                    String.format("Failed to resolve extension dependency [%s]", extensionDependency), e1);
            }
        }

        return extension;
    }

    /**
     * @param extension the new extension to install
     * @param dependency indicate if the extension is installed as a dependency
     * @param namespace the namespace where to install the extension
     * @return the install plan node for the provided extension
     * @param initialDependency the initial dependency used to resolve the extension
     * @throws InstallException error when trying to install provided extension
     * @throws IncompatibleVersionConstraintException
     * @throws UninstallException
     * @throws NamespaceNotAllowedException when passed namespace is not compatible with the passed extension
     */
    private ModifableExtensionPlanNode installExtension(Extension extension, boolean dependency, String namespace,
        ExtensionDependency initialDependency) throws InstallException, ResolveException,
            IncompatibleVersionConstraintException, UninstallException, NamespaceNotAllowedException
    {
        // Check the namespace is compatible with the Extension
        this.namespaceResolver.checkAllowed(extension.getAllowedNamespaces(), namespace);

        // Check if the extension is already in the install plan
        checkExistingPlanNodeExtension(extension, namespace);

        // Check if the extension is already installed
        checkInstalledExtension(extension, namespace);

        // Check if the extension is a core extension
        checkCoreExtension(extension);

        // Find out what need to be upgraded and uninstalled

        this.progressManager.pushLevelProgress(6, this);

        try {
            this.progressManager.startStep(this);

            ExtensionHandler extensionHandler;

            // Is type supported ?
            try {
                extensionHandler = this.componentManager.getInstance(ExtensionHandler.class, extension.getType());
            } catch (ComponentLookupException e) {
                throw new InstallException(String.format("Unsupported type [%s]", extension.getType()), e);
            }

            this.progressManager.startStep(this);

            // Is installing the extension allowed ?
            extensionHandler.checkInstall(extension, namespace, getRequest());

            this.progressManager.startStep(this);

            // Find all existing versions of the extension
            Set<InstalledExtension> previousExtensions = getReplacedInstalledExtensions(extension, namespace);

            this.progressManager.startStep(this);

            // Mark replaced extensions as uninstalled
            for (InstalledExtension previousExtension : new ArrayList<>(previousExtensions)) {
                if (!previousExtension.isInstalled(namespace)
                    || !previousExtension.getId().getId().equals(extension.getId().getId())) {
                    if (namespace == null && previousExtension.getNamespaces() != null) {
                        for (String previousNamespace : previousExtension.getNamespaces()) {
                            uninstallExtension(previousExtension, previousNamespace, this.extensionTree, false);
                            previousExtensions.remove(previousExtension);
                        }
                    } else {
                        uninstallExtension(previousExtension, namespace, this.extensionTree, false);
                    }
                }
            }

            this.progressManager.startStep(this);

            // Check dependencies
            Collection<? extends ExtensionDependency> dependencies = extension.getDependencies();

            List<ModifableExtensionPlanNode> children = null;
            if (!dependencies.isEmpty()) {
                this.progressManager.pushLevelProgress(dependencies.size() + 1, this);

                try {
                    children = new ArrayList<ModifableExtensionPlanNode>();
                    for (ExtensionDependency dependencyDependency : extension.getDependencies()) {
                        this.progressManager.startStep(this);

                        installExtensionDependency(dependencyDependency, namespace, children);
                    }
                } finally {
                    this.progressManager.popLevelProgress(this);
                }
            }

            this.progressManager.startStep(this);

            ModifableExtensionPlanNode node = initialDependency != null
                ? new ModifableExtensionPlanNode(initialDependency, initialDependency.getVersionConstraint())
                : new ModifableExtensionPlanNode();

            node.setChildren(children);

            Action action;
            if (!previousExtensions.isEmpty()) {
                InstalledExtension previousExtension = previousExtensions.iterator().next();
                ExtensionId newFeature = extension.getId();
                ExtensionId previousFeature = previousExtension.getExtensionFeature(newFeature.getId());
                if (previousFeature == null) {
                    for (ExtensionId extensionFeature : extension.getExtensionFeatures()) {
                        previousFeature = previousExtension.getExtensionFeature(extensionFeature.getId());
                        if (previousFeature != null) {
                            newFeature = extensionFeature;
                            break;
                        }
                    }
                }

                if (previousFeature != null && previousFeature.getVersion().compareTo(newFeature.getVersion()) > 0) {
                    action = Action.DOWNGRADE;
                } else {
                    action = Action.UPGRADE;
                }
            } else {
                action = Action.INSTALL;
            }

            node.setAction(
                new DefaultExtensionPlanAction(extension, previousExtensions, action, namespace, dependency));

            return node;
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    private void checkInstalledExtension(Extension extension, String namespace) throws InstallException
    {
        // Check if the extension conflict with an extension installed on root namespace
        if (namespace != null) {
            checkRootExtension(extension);
        }

        // Check if the exact same extension is already installed on target namespace
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(extension.getId());
        if (installedExtension != null && installedExtension.isInstalled(namespace)) {
            throw new InstallException(
                String.format("Extension [%s] is already installed on namespace [%s]", extension.getId(), namespace));
        }
    }

    private void checkRootExtension(Extension extension) throws InstallException
    {
        checkRootExtension(extension.getId().getId());

        for (ExtensionId feature : extension.getExtensionFeatures()) {
            checkRootExtension(feature.getId());
        }
    }

    private void checkRootExtension(String feature) throws InstallException
    {
        InstalledExtension rootExtension = this.installedExtensionRepository.getInstalledExtension(feature, null);
        if (rootExtension != null) {
            throw new InstallException(
                String.format("An extension with feature [%s] is already installed on root namespace ([%s])", feature,
                    rootExtension.getId()));
        }
    }

    private void checkCoreExtension(Extension extension) throws InstallException
    {
        for (ExtensionId feature : extension.getExtensionFeatures()) {
            checkCoreExtension(feature.getId());
        }
    }

    private void checkCoreExtension(String feature) throws InstallException
    {
        if (this.coreExtensionRepository.exists(feature)) {
            throw new InstallException(String.format("There is already a core covering feature [%s]", feature));
        }
    }

    /**
     * Search and validate existing extensions that will be replaced by the extension.
     */
    private Set<InstalledExtension> getReplacedInstalledExtensions(Extension extension, String namespace)
        throws IncompatibleVersionConstraintException, ResolveException, InstallException
    {
        // If a namespace extension already exist on root, fail the install
        if (namespace != null) {
            checkRootExtension(extension.getId().getId());
        }

        Set<InstalledExtension> previousExtensions = new LinkedHashSet<>();

        Set<InstalledExtension> installedExtensions = getInstalledExtensions(extension.getId().getId(), namespace);

        VersionConstraint versionConstraint =
            checkReplacedInstalledExtensions(installedExtensions, extension.getId(), namespace, null);
        previousExtensions.addAll(installedExtensions);

        for (ExtensionId feature : extension.getExtensionFeatures()) {
            // If a namespace extension already exist on root, fail the install
            if (namespace != null) {
                checkRootExtension(feature.getId());
            }

            installedExtensions = getInstalledExtensions(feature.getId(), namespace);
            versionConstraint =
                checkReplacedInstalledExtensions(installedExtensions, feature, namespace, versionConstraint);
            previousExtensions.addAll(installedExtensions);
        }

        return previousExtensions;
    }

    private VersionConstraint mergeBackwardDependenciesVersionConstraints(String feature, String namespace,
        VersionConstraint versionConstraint) throws IncompatibleVersionConstraintException, ResolveException
    {
        Set<InstalledExtension> installedExtensions = getInstalledExtensions(feature, namespace);

        return mergeBackwardDependenciesVersionConstraints(installedExtensions, feature, namespace, versionConstraint);
    }

    private VersionConstraint mergeBackwardDependenciesVersionConstraints(
        Collection<InstalledExtension> installedExtensions, String feature, String namespace,
        VersionConstraint versionConstraint) throws IncompatibleVersionConstraintException, ResolveException
    {
        if (installedExtensions.isEmpty()) {
            return versionConstraint;
        }

        // Get all backward dependencies
        Map<String, Set<InstalledExtension>> backwardDependencies =
            getBackwardDependencies(installedExtensions, namespace);

        // Merge all backward dependencies constraints
        for (Map.Entry<String, Set<InstalledExtension>> entry : backwardDependencies.entrySet()) {
            versionConstraint = mergeVersionConstraints(entry.getValue(), feature, versionConstraint);
        }

        return versionConstraint;
    }

    private VersionConstraint checkReplacedInstalledExtensions(Collection<InstalledExtension> installedExtensions,
        ExtensionId feature, String namespace, VersionConstraint versionConstraint)
            throws IncompatibleVersionConstraintException, ResolveException
    {
        if (installedExtensions.isEmpty()) {
            return versionConstraint;
        }

        // Get all backward dependencies
        Map<String, Set<InstalledExtension>> backwardDependencies =
            getBackwardDependencies(installedExtensions, namespace);

        // Merge all backward dependencies constraints
        for (Map.Entry<String, Set<InstalledExtension>> entry : backwardDependencies.entrySet()) {
            versionConstraint = mergeVersionConstraints(entry.getValue(), feature.getId(), versionConstraint);

            // Validate version constraint
            if (versionConstraint != null) {
                if (!versionConstraint.isCompatible(feature.getVersion())) {
                    throw new IncompatibleVersionConstraintException(String.format(
                        "The extension with feature [%s] is not compatible with existing backward dependency constraint [%s]",
                        feature, versionConstraint));
                }
            }
        }

        return versionConstraint;
    }

    private Map<String, Set<InstalledExtension>> getBackwardDependencies(
        Collection<InstalledExtension> installedExtensions, String namespace) throws ResolveException
    {
        Map<String, Set<InstalledExtension>> backwardDependencies = new LinkedHashMap<>();

        for (InstalledExtension installedExtension : installedExtensions) {
            if (namespace == null) {
                for (Map.Entry<String, Collection<InstalledExtension>> entry : this.installedExtensionRepository
                    .getBackwardDependencies(installedExtension.getId()).entrySet()) {
                    Set<InstalledExtension> namespaceBackwardDependencies = backwardDependencies.get(entry.getKey());
                    if (namespaceBackwardDependencies == null) {
                        namespaceBackwardDependencies = new LinkedHashSet<>();
                        backwardDependencies.put(entry.getKey(), namespaceBackwardDependencies);
                    }

                    namespaceBackwardDependencies.addAll(entry.getValue());
                }
            } else {
                for (InstalledExtension backwardDependency : this.installedExtensionRepository
                    .getBackwardDependencies(installedExtension.getId().getId(), namespace)) {
                    Set<InstalledExtension> namespaceBackwardDependencies = backwardDependencies.get(namespace);
                    if (namespaceBackwardDependencies == null) {
                        namespaceBackwardDependencies = new LinkedHashSet<>();
                        backwardDependencies.put(namespace, namespaceBackwardDependencies);
                    }

                    namespaceBackwardDependencies.add(backwardDependency);
                }
            }
        }

        return backwardDependencies;
    }

    private Set<InstalledExtension> getInstalledExtensions(String feature, String namespace)
    {
        Set<InstalledExtension> installedExtensions = new LinkedHashSet<>();

        getInstalledExtensions(feature, namespace, installedExtensions);

        return installedExtensions;
    }

    private void getInstalledExtensions(String feature, String namespace, Set<InstalledExtension> installedExtensions)
    {
        if (namespace == null) {
            getInstalledExtensions(feature, installedExtensions);
        } else {
            InstalledExtension installedExtension =
                this.installedExtensionRepository.getInstalledExtension(feature, namespace);
            if (installedExtension != null) {
                installedExtensions.add(installedExtension);
            }
        }
    }

    private void getInstalledExtensions(String feature, Set<InstalledExtension> installedExtensions)
    {
        for (InstalledExtension installedExtension : this.installedExtensionRepository.getInstalledExtensions()) {
            // Check id
            if (installedExtension.getId().getId().equals(feature)) {
                installedExtensions.add(installedExtension);
                continue;
            }

            // Check features
            for (ExtensionId installedFeature : installedExtension.getExtensionFeatures())
                if (installedFeature.getId().equals(feature)) {
                    installedExtensions.add(installedExtension);
                    break;
                }
        }
    }
}
