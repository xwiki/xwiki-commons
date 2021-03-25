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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.namespace.NamespaceNotAllowedException;
import org.xwiki.component.namespace.NamespaceValidator;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.internal.ExtensionUtils;
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
    protected static class ModifableExtensionPlanNode extends DefaultExtensionPlanNode
    {
        // never change

        private final ExtensionDependency initialDependency;

        // can change

        public VersionConstraint versionConstraint;

        public final List<ModifableExtensionPlanNode> duplicates = new ArrayList<>();

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
    protected NamespaceValidator namespaceResolver;

    @Inject
    protected ExtensionManagerConfiguration configuration;

    @Inject
    protected ExtensionFactory factory;

    /**
     * Used to make sure dependencies are compatible between each other in the whole plan.
     * <p>
     * <id, <namespace, node>>.
     */
    protected Map<String, Map<String, ModifableExtensionPlanNode>> extensionsNodeCache = new HashMap<>();

    protected void setExtensionTree(DefaultExtensionPlanTree extensionTree)
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
                namespaces = new HashSet<>();
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

                            this.progressManager.endStep(this);
                        }
                    } finally {
                        this.progressManager.popLevelProgress(this);
                    }
                } else {
                    installExtension(extensionId, null, this.extensionTree);
                }

                this.progressManager.endStep(this);
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

        addExtensionNode(id, node);

        node.getAction().getExtension().getExtensionFeatures()
            .forEach(feature -> addExtensionNode(feature.getId(), node));
    }

    private void addExtensionNode(String id, ModifableExtensionPlanNode node)
    {
        Map<String, ModifableExtensionPlanNode> extensionsById = this.extensionsNodeCache.get(id);

        if (extensionsById == null) {
            extensionsById = new HashMap<>();
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
    protected void installExtension(ExtensionId extensionId, String namespace, DefaultExtensionPlanTree parentBranch)
        throws InstallException
    {
        installExtension(extensionId, false, namespace, parentBranch);
    }

    /**
     * Install provided extension.
     *
     * @param extensionId the identifier of the extension to install
     * @param dependency indicate if the extension is installed as a dependency
     * @param namespace the namespace where to install the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws InstallException error when trying to install provided extension
     */
    protected void installExtension(ExtensionId extensionId, boolean dependency, String namespace,
        DefaultExtensionPlanTree parentBranch) throws InstallException
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
                    this.logger.info(
                        "There is already a core extension feature [{}] ([{}]) covering extension dependency [{}]",
                        feature, coreExtension.getId(), extensionDependency.toString());
                }

                ModifableExtensionPlanNode node =
                    new ModifableExtensionPlanNode(extensionDependency, extensionDependency.getVersionConstraint());
                node.setAction(
                    new DefaultExtensionPlanAction(coreExtension, coreExtension, null, Action.NONE, null, true));

                parentBranch.add(node);

                return true;
            }
        }

        return false;
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

    private Object checkPlannedDependency(ExtensionDependency extensionDependency,
        VersionConstraint previousVersionConstraint, String namespace) throws InstallException
    {
        VersionConstraint mergedVersionContraint = previousVersionConstraint;

        Map<String, ModifableExtensionPlanNode> idNodes = this.extensionsNodeCache.get(extensionDependency.getId());

        if (idNodes != null) {
            ModifableExtensionPlanNode existingNode = idNodes.get(namespace);

            if (existingNode != null) {
                // Already planned on the same namespace
                ExtensionId feature =
                    existingNode.getAction().getExtension().getExtensionFeature(extensionDependency.getId());

                if (mergedVersionContraint.isCompatible(feature.getVersion())) {
                    // Continue with the existing extension if it's compatible
                    return existingNode;
                } else {
                    // Merge constraints
                    mergedVersionContraint =
                        checkPlannedDependency(existingNode, extensionDependency, mergedVersionContraint);
                }
            } else if (namespace != null) {
                existingNode = idNodes.get(null);

                if (existingNode != null) {
                    // Already planned but on root namespace
                    // Switch current install on root
                    return null;
                }
            } else {
                // Already planned but on sub-namespaces
                // Switch all existing nodes to root namespace
                // Store nodes to remove in a temporary list to avoid concurrent modification issues
                List<ModifableExtensionPlanNode> nodesToRemove = new ArrayList<>();
                for (ModifableExtensionPlanNode node : idNodes.values()) {
                    nodesToRemove.add(node);

                    // Version the version constraints to make sure they are compatibles
                    mergedVersionContraint = checkPlannedDependency(node, extensionDependency, mergedVersionContraint);
                }
                // Switch existing nodes to root namespace and make sure they all are compatibles
                for (ModifableExtensionPlanNode node : nodesToRemove) {
                    setRootNamespace(node, mergedVersionContraint);
                }
            }
        }

        return mergedVersionContraint;
    }

    private void setRootNamespace(ModifableExtensionPlanNode node, VersionConstraint mergedVersionContraint)
    {
        // Remove the node from the index
        removeNodeFromCache(node);

        // Modify the namespace of the node
        ExtensionPlanAction action = node.getAction();
        node.versionConstraint = mergedVersionContraint;
        node.setAction(new DefaultExtensionPlanAction(action.getExtension(), action.getRewrittenExtension(),
            action.getPreviousExtensions(), action.getAction(), null, action.isDependency()));

        // Add back the node in the index
        addExtensionNode(node);
    }

    private void removeNodeFromCache(ModifableExtensionPlanNode node)
    {
        ExtensionPlanAction action = node.getAction();
        Extension extension = action.getExtension();
        String namespace = action.getNamespace();

        removeNodeFromCache(extension.getId().getId(), namespace);

        extension.getExtensionFeatures().forEach(feature -> removeNodeFromCache(feature.getId(), namespace));
    }

    private void removeNodeFromCache(String feature, String namespace)
    {
        this.extensionsNodeCache.get(feature).remove(namespace);
    }

    private VersionConstraint checkPlannedDependency(ModifableExtensionPlanNode existingNode,
        ExtensionDependency extensionDependency, VersionConstraint previousVersionConstraint) throws InstallException
    {
        if (existingNode.versionConstraint != null) {
            try {
                return previousVersionConstraint.merge(existingNode.versionConstraint);
            } catch (IncompatibleVersionConstraintException e) {
                throw new InstallException("Dependency [" + extensionDependency
                    + "] is incompatible with current constaint [" + existingNode.versionConstraint + "]", e);
            }
        } else {
            throw new InstallException("Dependency [" + extensionDependency + "] incompatible with extension ["
                + existingNode.getAction().getExtension() + "]");
        }
    }

    private ExtensionDependency checkInstalledDependency(InstalledExtension installedExtension,
        ExtensionDependency extensionDependency, VersionConstraint versionConstraint, String namespace)
        throws InstallException
    {
        ExtensionDependency targetDependency = extensionDependency;

        if (installedExtension != null) {
            // Check if already installed version is compatible
            if (installedExtension.isValid(namespace)) {
                ExtensionId feature = installedExtension.getExtensionFeature(extensionDependency.getId());

                if (versionConstraint.isCompatible(feature.getVersion())) {
                    if (getRequest().isVerbose()) {
                        this.logger.info(
                            "There is already an installed extension [{}] covering extension dependency [{}]",
                            installedExtension.getId(), extensionDependency.toString());
                    }

                    return null;
                }
            }

            // If incompatible root extension fail
            if (namespace != null && installedExtension.isInstalled(null)) {
                if (!getRequest().isRootModificationsAllowed()) {
                    throw new InstallException(
                        String.format("Dependency [%s] is incompatible with installed root extension [%s]",
                            extensionDependency, installedExtension.getId()));
                }
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
     * @param extensionContext the current extension context
     * @param parents the parents extensions (which triggered this extension install)
     * @throws InstallException error when trying to install provided extension
     * @throws ResolveException
     * @throws IncompatibleVersionConstraintException
     */
    private void installExtensionDependency(ExtensionDependency extensionDependency, String namespace,
        List<ModifableExtensionPlanNode> parentBranch, ExtensionPlanContext extensionContext, Set<String> parents)
        throws InstallException, IncompatibleVersionConstraintException, ResolveException
    {
        if (extensionDependency.isOptional()) {
            installOptionalExtensionDependency(extensionDependency, namespace, parentBranch, extensionContext, parents);
        } else {
            installMandatoryExtensionDependency(extensionDependency, namespace, parentBranch, extensionContext,
                parents);
        }
    }

    /**
     * Install provided extension dependency.
     *
     * @param extensionDependency the extension dependency to install
     * @param namespace the namespace where to install the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @param extensionContext the current extension context
     * @param parents the parents extensions (which triggered this extension install)
     */
    private boolean installOptionalExtensionDependency(ExtensionDependency extensionDependency, String namespace,
        List<ModifableExtensionPlanNode> parentBranch, ExtensionPlanContext extensionContext, Set<String> parents)
    {
        // Save current plan
        List<ModifableExtensionPlanNode> dependencyBranch = new ArrayList<>();

        try {
            installMandatoryExtensionDependency(extensionDependency, namespace, dependencyBranch, extensionContext,
                parents);

            parentBranch.addAll(dependencyBranch);

            return true;
        } catch (Throwable e) {
            if (getRequest().isVerbose()) {
                this.logger.warn("Failed to install optional dependency [{}] with error: {}",
                    extensionDependency.toString(), ExceptionUtils.getRootCauseMessage(e));
            }
        }

        return false;
    }

    /**
     * Install provided extension dependency.
     *
     * @param extensionDependency the extension dependency to install
     * @param namespace the namespace where to install the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @param extensionContext the current extension context
     * @param parents the parents extensions (which triggered this extension install)
     * @throws InstallException error when trying to install provided extension
     * @throws ResolveException
     * @throws IncompatibleVersionConstraintException
     */
    protected void installMandatoryExtensionDependency(ExtensionDependency extensionDependency, String namespace,
        List<ModifableExtensionPlanNode> parentBranch, ExtensionPlanContext extensionContext, Set<String> parents)
        throws InstallException, IncompatibleVersionConstraintException, ResolveException
    {
        // Make sure the dependency have a version constraint
        if (extensionDependency.getVersionConstraint() == null) {
            // TODO: install the last version instead of failing ?
            throw new InstallException("Dependency [" + extensionDependency + "] does not have any version constraint");
        }

        if (getRequest().isVerbose()) {
            if (namespace != null) {
                this.logger.info(LOG_RESOLVEDEPENDENCY_NAMESPACE,
                    "Resolving extension dependency [{}] on namespace [{}]", extensionDependency.toString(), namespace);
            } else {
                this.logger.info(LOG_RESOLVEDEPENDENCY, "Resolving extension dependency [{}] on all namespaces",
                    extensionDependency.toString());
            }
        }

        VersionConstraint versionConstraint = extensionDependency.getVersionConstraint();

        // Make sure the dependency is not already a core extension
        if (checkCoreDependency(extensionDependency, parentBranch)) {
            // Already exists and added to the tree by #checkCoreExtension
            return;
        }

        // If the dependency matches an incompatible root extension switch target namespace to root (to try to
        // upgrade/downgrade/replace it)
        if (namespace != null && getRequest().isRootModificationsAllowed()
            && hasIncompatileRootDependency(extensionDependency)) {
            installMandatoryExtensionDependency(extensionDependency, null, parentBranch, extensionContext, parents);

            return;
        }

        // Check already planned install
        Object plannedResult = checkPlannedDependency(extensionDependency, versionConstraint, namespace);
        if (plannedResult == null) {
            // Switch to root
            installMandatoryExtensionDependency(extensionDependency, null, parentBranch, extensionContext, parents);

            return;
        } else if (plannedResult instanceof ModifableExtensionPlanNode) {
            ModifableExtensionPlanNode existingNode = (ModifableExtensionPlanNode) plannedResult;

            // Already exists in the plan but we don't trust it (might have been previously been installed with totally
            // different managed dependencies and a ton of exclusions) so we check the dependencies anyway
            List<ModifableExtensionPlanNode> children =
                installExtensionDependencies(existingNode.getAction().getExtension(), namespace,
                    new ExtensionPlanContext(extensionContext, extensionDependency), parents);

            ModifableExtensionPlanNode node = new ModifableExtensionPlanNode(extensionDependency, existingNode);
            node.setChildren(children);

            addExtensionNode(node);
            parentBranch.add(node);

            return;
        } else {
            versionConstraint = (VersionConstraint) plannedResult;
        }

        // Check installed extensions
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(extensionDependency.getId(), namespace);
        ExtensionDependency targetDependency =
            checkInstalledDependency(installedExtension, extensionDependency, versionConstraint, namespace);
        if (targetDependency == null) {
            // Already installed but we don't trust it (might have been previously been installed with totally different
            // managed dependencies and a ton of exclusions) so we check the dependencies anyway
            List<ModifableExtensionPlanNode> children = installExtensionDependencies(installedExtension, namespace,
                new ExtensionPlanContext(extensionContext, extensionDependency), parents);

            ModifableExtensionPlanNode node = new ModifableExtensionPlanNode(extensionDependency, versionConstraint);
            node.setAction(new DefaultExtensionPlanAction(installedExtension, installedExtension, null, Action.NONE,
                namespace, installedExtension.isDependency(namespace)));
            node.setChildren(children);

            addExtensionNode(node);
            parentBranch.add(node);

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
        ModifableExtensionPlanNode node =
            installExtensionDependency(targetDependency, true, namespace, extensionContext, parents);

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
     * @param extensionContext the current extension context
     * @param parents the parents extensions (which triggered this extension install)
     * @return the install plan node for the provided extension
     * @throws InstallException error when trying to install provided extension
     */
    private ModifableExtensionPlanNode installExtensionDependency(ExtensionDependency targetDependency,
        boolean dependency, String namespace, ExtensionPlanContext extensionContext, Set<String> parents)
        throws InstallException
    {
        this.progressManager.pushLevelProgress(2, this);

        try {
            this.progressManager.startStep(this);

            // Search the extension
            Extension extension = resolveExtension(targetDependency);

            // Rewrite the extension
            Extension rewrittenExtension;
            if (getRequest().getRewriter() != null) {
                rewrittenExtension = getRequest().getRewriter().rewrite(extension);
            } else {
                rewrittenExtension = extension;
            }

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            try {
                return installExtension(extension, rewrittenExtension, dependency, namespace, targetDependency,
                    new ExtensionPlanContext(extensionContext, targetDependency), parents);
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
        // Check if the feature is a root extension
        if (namespace != null) {
            // Check if the extension already exist on root, throw exception if not allowed
            if (checkRootExtension(extensionId.getId())) {
                // Restart install on root
                return installExtension(extensionId, dependency, null);
            }
        }

        this.progressManager.pushLevelProgress(2, this);

        try {
            this.progressManager.startStep(this);

            // Find the extension in a repository (starting with local one)
            Extension extension = resolveExtension(extensionId);

            // Rewrite the extension
            Extension rewrittenExtension;
            if (getRequest().getRewriter() != null) {
                rewrittenExtension = getRequest().getRewriter().rewrite(extension);
            } else {
                rewrittenExtension = extension;
            }

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            try {
                return installExtension(extension, rewrittenExtension, dependency, namespace, null,
                    new ExtensionPlanContext(), null);
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
        // Check if the extension is already in local repository
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

    protected boolean isNamespaceAllowed(Extension extension, String namespace)
    {
        return this.namespaceResolver.isAllowed(extension.getAllowedNamespaces(), namespace);
    }

    protected void checkTypeInstall(Extension extension, String namespace) throws InstallException
    {
        ExtensionHandler extensionHandler;

        // Is type supported ?
        try {
            extensionHandler = this.componentManager.getInstance(ExtensionHandler.class, extension.getType());
        } catch (ComponentLookupException e) {
            throw new InstallException(String.format("Unsupported type [%s]", extension.getType()), e);
        }

        // Is installing the extension allowed ?
        extensionHandler.checkInstall(extension, namespace, getRequest());
    }

    /**
     * @param sourceExtension the new extension to install
     * @param rewrittenExtension the rewritten version of the passed extension
     * @param dependency indicate if the extension is installed as a dependency
     * @param namespace the namespace where to install the extension
     * @param initialDependency the initial dependency used to resolve the extension
     * @param extensionContext the current extension context
     * @param parents the parents extensions (which triggered this extension install)
     * @return the install plan node for the provided extension
     * @throws InstallException error when trying to install provided extension
     * @throws IncompatibleVersionConstraintException
     * @throws UninstallException
     * @throws NamespaceNotAllowedException when passed namespace is not compatible with the passed extension
     */
    private ModifableExtensionPlanNode installExtension(Extension sourceExtension, Extension rewrittenExtension,
        boolean dependency, String namespace, ExtensionDependency initialDependency,
        ExtensionPlanContext extensionContext, Set<String> parents) throws InstallException, ResolveException,
        IncompatibleVersionConstraintException, UninstallException, NamespaceNotAllowedException
    {
        boolean allowed = isNamespaceAllowed(rewrittenExtension, namespace);

        // Check if the namespace is compatible with the Extension
        if (!allowed) {
            if (namespace != null) {
                if (getRequest().isRootModificationsAllowed()) {
                    // Try to install it on root namespace
                    return installExtension(sourceExtension, rewrittenExtension, dependency, null, initialDependency,
                        extensionContext, parents);
                }
            }

            // Extension is not allowed on target namespace
            throw new NamespaceNotAllowedException(
                String.format("Extension [%s] is not allowed on namespace [%s]. Allowed namespaces are: %s",
                    rewrittenExtension.getId(), namespace, rewrittenExtension.getAllowedNamespaces()));
        }

        // Check if the extension is already in the install plan
        checkExistingPlanNodeExtension(rewrittenExtension, namespace);

        // Check if the extension matches a root extension
        if (namespace != null) {
            // Check if the extension already exist on root, throw exception if not allowed
            if (checkRootExtension(rewrittenExtension)) {
                // Restart install on root
                return installExtension(sourceExtension, rewrittenExtension, dependency, null, initialDependency,
                    extensionContext, parents);
            }
        }

        // Check if the extension is already installed
        Extension installedExtension = checkInstalledExtension(rewrittenExtension, namespace);
        if (installedExtension != rewrittenExtension) {
            sourceExtension = installedExtension;

            // Rewrite the extension
            if (getRequest().getRewriter() != null) {
                rewrittenExtension = getRequest().getRewriter().rewrite(installedExtension);
            } else {
                rewrittenExtension = installedExtension;
            }
        }

        // Check if the extension is a core extension
        checkCoreExtension(rewrittenExtension);

        // Find out what need to be upgraded and uninstalled

        this.progressManager.pushLevelProgress(5, this);

        try {
            this.progressManager.startStep(this);

            // Is installing the extension supported/allowed ?
            checkTypeInstall(rewrittenExtension, namespace);

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            // Find all existing versions of the extension
            Set<InstalledExtension> previousExtensions = getReplacedInstalledExtensions(rewrittenExtension, namespace);

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            // Mark replaced extensions as uninstalled
            for (InstalledExtension previousExtension : new ArrayList<>(previousExtensions)) {
                if (!previousExtension.isInstalled(namespace)
                    || !previousExtension.getId().getId().equals(rewrittenExtension.getId().getId())) {
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

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            // Check dependencies
            List<ModifableExtensionPlanNode> children =
                installExtensionDependencies(rewrittenExtension, namespace, extensionContext, parents);

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            ModifableExtensionPlanNode node = initialDependency != null
                ? new ModifableExtensionPlanNode(initialDependency, initialDependency.getVersionConstraint())
                : new ModifableExtensionPlanNode();

            node.setChildren(children);

            Action action;
            if (!previousExtensions.isEmpty()) {
                if (rewrittenExtension.compareTo(previousExtensions.iterator().next()) < 0) {
                    action = Action.DOWNGRADE;
                } else {
                    action = Action.UPGRADE;
                }
            } else if (rewrittenExtension instanceof InstalledExtension) {
                action = Action.REPAIR;
            } else {
                action = Action.INSTALL;
            }

            node.setAction(new DefaultExtensionPlanAction(sourceExtension, rewrittenExtension, previousExtensions,
                action, namespace, dependency));

            return node;
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    private List<ModifableExtensionPlanNode> installExtensionDependencies(Extension extension, String namespace,
        ExtensionPlanContext extensionContext, Set<String> parents)
        throws InstallException, IncompatibleVersionConstraintException, ResolveException
    {
        Collection<? extends ExtensionDependency> dependencies = extension.getDependencies();

        List<ModifableExtensionPlanNode> children = null;
        if (!dependencies.isEmpty()) {
            parents = ExtensionUtils.append(parents, extension.getId().getId());

            this.progressManager.pushLevelProgress(dependencies.size() + 1, this);

            try {
                children = new ArrayList<>();
                for (ExtensionDependency extensionDependency : dependencies) {
                    this.progressManager.startStep(this);

                    if (parents.contains(extensionDependency.getId())) {
                        // In case of cross dependency just ignore it
                        continue;
                    }

                    // Is ignored
                    if (this.configuration.isIgnoredDependency(extensionDependency)) {
                        continue;
                    }

                    // Replace with managed dependency if any
                    extensionDependency = extensionContext.getDependency(extensionDependency, extension);

                    // Is excluded
                    if (extensionContext.isExcluded(extensionDependency)) {
                        continue;
                    }

                    // Try installing recommended version (if any)
                    boolean valid = false;
                    ExtensionDependency recommendedDependency =
                        ExtensionUtils.getRecommendedDependency(extensionDependency, this.configuration, this.factory);
                    if (recommendedDependency != null) {
                        valid = installOptionalExtensionDependency(recommendedDependency, namespace, children,
                            new ExtensionPlanContext(extensionContext, extension), parents);
                    }

                    // If recommended version is invalid, try the one provided by the extension
                    if (!valid) {
                        installExtensionDependency(extensionDependency, namespace, children,
                            new ExtensionPlanContext(extensionContext, extension), parents);
                    }

                    this.progressManager.endStep(this);
                }
            } finally {
                this.progressManager.popLevelProgress(this);
            }
        }

        return children;
    }

    private Extension checkInstalledExtension(Extension extension, String namespace) throws InstallException
    {
        // Check if the extension conflict with an extension installed on root namespace
        if (namespace != null) {
            checkRootExtension(extension);
        }

        // Check if the exact same valid extension is already installed on target namespace
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(extension.getId());
        if (installedExtension != null && installedExtension.isInstalled(namespace)) {
            if (installedExtension.isValid(namespace)) {
                throw new InstallException(String.format("Extension [%s] is already installed on namespace [%s]",
                    extension.getId(), namespace));
            }

            // In case the extension is already installed on the namespace but is invalid continue with it to make clear
            // to following code we are actually repairing it
            return installedExtension;
        }

        return extension;
    }

    private boolean checkRootExtension(Extension extension) throws InstallException
    {
        boolean isRootExtension = checkRootExtension(extension.getId().getId());

        for (ExtensionId feature : extension.getExtensionFeatures()) {
            isRootExtension |= checkRootExtension(feature.getId());
        }

        return isRootExtension;
    }

    private boolean checkRootExtension(String feature) throws InstallException
    {
        InstalledExtension rootExtension = this.installedExtensionRepository.getInstalledExtension(feature, null);
        if (rootExtension != null) {
            if (!getRequest().isRootModificationsAllowed()) {
                throw new InstallException(
                    String.format("An extension with feature [%s] is already installed on root namespace ([%s])",
                        feature, rootExtension.getId()));
            }

            return true;
        }

        return false;
    }

    private boolean hasIncompatileRootDependency(ExtensionDependency extensionDependency)
    {
        InstalledExtension rootExtension =
            this.installedExtensionRepository.getInstalledExtension(extensionDependency.getId(), null);
        return rootExtension != null && !extensionDependency.isCompatible(rootExtension);
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
            throw new InstallException(
                String.format("There is already a core extension covering feature [%s]", feature));
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

        // If the extensions is already installed (usually mean we are trying to repair an invalid extension) it's not
        // going to replace itself
        if (extension instanceof InstalledExtension) {
            previousExtensions.remove(extension);
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
