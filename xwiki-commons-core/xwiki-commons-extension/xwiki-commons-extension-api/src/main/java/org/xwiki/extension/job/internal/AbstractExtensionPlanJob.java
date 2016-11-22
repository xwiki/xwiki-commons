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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlan;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanAction;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanNode;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanTree;
import org.xwiki.job.Job;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.marker.TranslationMarker;

/**
 * Base class for plan calculation jobs.
 *
 * @param <R> the type of the request
 * @version $Id$
 * @since 5.4RC1
 */
public abstract class AbstractExtensionPlanJob<R extends ExtensionRequest>
    extends AbstractExtensionJob<R, DefaultExtensionPlan<R>>
{
    protected static final TranslationMarker LOG_RESOLVE = new TranslationMarker("extension.log.job.plan.resolve");

    protected static final TranslationMarker LOG_RESOLVE_NAMESPACE =
        new TranslationMarker("extension.log.job.plan.resolve.namespace");

    protected static final TranslationMarker LOG_RESOLVEDEPENDENCY =
        new TranslationMarker("extension.log.job.plan.resolvedependency");

    protected static final TranslationMarker LOG_RESOLVEDEPENDENCY_NAMESPACE =
        new TranslationMarker("extension.log.job.plan.resolvedependency.namespace");

    /**
     * Error message used in exception throw when trying to uninstall an extension which is not installed.
     */
    private static final String EXCEPTION_NOTINSTALLED = "Extension [%s] is not installed";

    /**
     * Error message used in exception throw when trying to uninstall an extension which is not installed.
     */
    private static final String EXCEPTION_NOTINSTALLEDNAMESPACE = EXCEPTION_NOTINSTALLED + " on namespace [%s]";

    /**
     * The install plan.
     */
    protected DefaultExtensionPlanTree extensionTree = new DefaultExtensionPlanTree();

    @Override
    protected DefaultExtensionPlan<R> createNewStatus(R request)
    {
        Job currentJob = this.jobContext.getCurrentJob();
        JobStatus currentJobStatus = currentJob != null ? currentJob.getStatus() : null;
        return new DefaultExtensionPlan<R>(request, this.observationManager, this.loggerManager, this.extensionTree,
            currentJobStatus);
    }

    /**
     * @param extensionId the identifier of the extension to uninstall
     * @param namespaces the namespaces from where to uninstall the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @param withBackWard uninstall also the backward dependencies
     * @throws UninstallException error when trying to uninstall provided extensions
     */
    protected void uninstallExtension(String extensionId, Collection<String> namespaces,
        Collection<ExtensionPlanNode> parentBranch, boolean withBackWard) throws UninstallException
    {
        this.progressManager.pushLevelProgress(namespaces.size(), this);

        try {
            for (String namespace : namespaces) {
                this.progressManager.startStep(this);

                uninstallExtension(extensionId, namespace, parentBranch, withBackWard);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    /**
     * @param extensionId the identifier of the extension to uninstall
     * @param namespace the namespace from where to uninstall the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @param withBackWard uninstall also the backward dependencies
     * @throws UninstallException error when trying to uninstall provided extension
     */
    protected void uninstallExtension(String extensionId, String namespace, Collection<ExtensionPlanNode> parentBranch,
        boolean withBackWard) throws UninstallException
    {
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(extensionId, namespace);

        if (installedExtension == null) {
            throw new UninstallException(String.format(EXCEPTION_NOTINSTALLED, extensionId));
        }

        try {
            uninstallExtension(installedExtension, namespace, parentBranch, withBackWard);
        } catch (Exception e) {
            throw new UninstallException("Failed to uninstall extension", e);
        }
    }

    /**
     * @param installedExtension the extension to uninstall
     * @param namespaces the namespaces from where to uninstall the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @param withBackWard uninstall also the backward dependencies
     * @throws UninstallException error when trying to uninstall provided extension
     */
    protected void uninstallExtension(InstalledExtension installedExtension, Collection<String> namespaces,
        Collection<ExtensionPlanNode> parentBranch, boolean withBackWard) throws UninstallException
    {
        this.progressManager.pushLevelProgress(namespaces.size(), this);

        try {
            for (String namespace : namespaces) {
                this.progressManager.startStep(this);

                uninstallExtension(installedExtension, namespace, parentBranch, withBackWard);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    /**
     * @param extensions the installed extensions to uninstall
     * @param namespace the namespaces from where to uninstall the extensions
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @param withBackWard uninstall also the backward dependencies
     * @throws UninstallException error when trying to uninstall provided extensions
     */
    protected void uninstallExtensions(Collection<InstalledExtension> extensions, String namespace,
        Collection<ExtensionPlanNode> parentBranch, boolean withBackWard) throws UninstallException
    {
        this.progressManager.pushLevelProgress(extensions.size(), this);

        try {
            for (InstalledExtension backardDependency : extensions) {
                this.progressManager.startStep(this);

                uninstallExtension(backardDependency, namespace, parentBranch, withBackWard);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    /**
     * @param installedExtension the extension to uninstall
     * @param namespace the namespace from where to uninstall the extension
     * @param parentBranch the children of the parent {@link ExtensionPlanNode}
     * @param withBackWard uninstall also the backward dependencies
     * @throws UninstallException error when trying to uninstall provided extension
     */
    protected void uninstallExtension(InstalledExtension installedExtension, String namespace,
        Collection<ExtensionPlanNode> parentBranch, boolean withBackWard) throws UninstallException
    {
        if (namespace != null) {
            if (installedExtension.getNamespaces() == null || !installedExtension.getNamespaces().contains(namespace)) {
                throw new UninstallException(
                    String.format(EXCEPTION_NOTINSTALLEDNAMESPACE, installedExtension, namespace));
            }
        }

        ExtensionHandler extensionHandler;

        // Is type supported ?
        try {
            extensionHandler = this.componentManager.getInstance(ExtensionHandler.class, installedExtension.getType());
        } catch (ComponentLookupException e) {
            throw new UninstallException(String.format("Unsupported type [%s]", installedExtension.getType()), e);
        }

        // Is uninstalling the extension allowed ?
        extensionHandler.checkUninstall(installedExtension, namespace, getRequest());

        // Log progression
        if (getRequest().isVerbose()) {
            if (namespace != null) {
                this.logger.info(LOG_RESOLVE_NAMESPACE, "Resolving extension [{}] from namespace [{}]",
                    installedExtension.getId(), namespace);
            } else {
                this.logger.info(LOG_RESOLVE, "Resolving extension [{}]", installedExtension.getId());
            }
        }

        List<ExtensionPlanNode> children = new ArrayList<ExtensionPlanNode>();

        // Uninstall backward dependencies
        if (withBackWard) {
            uninstallBackwardDependencies(installedExtension, namespace, children, withBackWard);
        }

        // Uninstall the extension
        DefaultExtensionPlanAction action = new DefaultExtensionPlanAction(installedExtension, installedExtension,
            Collections.singleton(installedExtension), Action.UNINSTALL, namespace, false);
        parentBranch.add(new DefaultExtensionPlanNode(action, children, null));
    }

    /**
     * @param installedExtension the extension to uninstall
     * @param namespace the namespace from where to uninstall the extension
     * @param parentBranch the children of the parent {@link ExtensionPlanNode}
     * @param withBackWard uninstall also the backward dependencies
     * @throws UninstallException error when trying to uninstall backward dependencies
     * @throws ResolveException error when trying to resolve backward dependencies
     */
    protected void uninstallBackwardDependencies(InstalledExtension installedExtension, String namespace,
        List<ExtensionPlanNode> parentBranch, boolean withBackWard) throws UninstallException
    {
        try {
            if (namespace != null) {
                Collection<InstalledExtension> installedExtensions = this.installedExtensionRepository
                    .getBackwardDependencies(installedExtension.getId().getId(), namespace);
                if (!installedExtensions.isEmpty()) {
                    uninstallExtensions(installedExtensions, namespace, parentBranch, withBackWard);
                }
            } else {
                uninstallBackwardDependencies(installedExtension, parentBranch, withBackWard);
            }
        } catch (ResolveException e) {
            throw new UninstallException(
                "Failed to resolve backward dependencies of extension [" + installedExtension + "]", e);
        }
    }

    /**
     * @param installedExtension the extension to uninstall
     * @param parentBranch the children of the parent {@link ExtensionPlanNode}
     * @param withBackWard uninstall also the backward dependencies
     * @throws UninstallException error when trying to uninstall backward dependencies
     * @throws ResolveException error when trying to resolve backward dependencies
     */
    protected void uninstallBackwardDependencies(InstalledExtension installedExtension,
        List<ExtensionPlanNode> parentBranch, boolean withBackWard) throws UninstallException, ResolveException
    {
        Map<String, Collection<InstalledExtension>> backwardDependencies =
            this.installedExtensionRepository.getBackwardDependencies(installedExtension.getId());

        this.progressManager.pushLevelProgress(backwardDependencies.size(), this);

        try {
            for (Map.Entry<String, Collection<InstalledExtension>> entry : backwardDependencies.entrySet()) {
                this.progressManager.startStep(this);

                uninstallExtensions(entry.getValue(), entry.getKey(), parentBranch, withBackWard);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }
}
