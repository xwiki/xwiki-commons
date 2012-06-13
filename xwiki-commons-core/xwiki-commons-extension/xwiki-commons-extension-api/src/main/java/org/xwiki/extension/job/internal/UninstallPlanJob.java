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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.job.plan.ExtensionPlanTree;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlan;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanAction;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanNode;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanTree;
import org.xwiki.job.Request;

/**
 * Create an Extension uninstallation plan.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Named(UninstallPlanJob.JOBTYPE)
public class UninstallPlanJob extends AbstractExtensionJob<UninstallRequest>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "uninstallplan";

    /**
     * Error message used in exception throw when trying to uninstall an extension which is not installed.
     */
    private static final String EXCEPTION_NOTINSTALLED = "Extension [{0}] is not installed";

    /**
     * Error message used in exception throw when trying to uninstall an extension which is not installed.
     */
    private static final String EXCEPTION_NOTINSTALLEDNAMESPACE = EXCEPTION_NOTINSTALLED + " from namespace [{1}]";

    /**
     * The install plan.
     */
    private ExtensionPlanTree extensionTree = new DefaultExtensionPlanTree();

    @Override
    public String getType()
    {
        return JOBTYPE;
    }

    @Override
    protected DefaultExtensionPlan<UninstallRequest> createNewStatus(UninstallRequest request)
    {
        return new DefaultExtensionPlan<UninstallRequest>(request, this.observationManager, this.loggerManager,
            this.extensionTree);
    }

    @Override
    protected UninstallRequest castRequest(Request request)
    {
        UninstallRequest uninstallRequest;
        if (request instanceof UninstallRequest) {
            uninstallRequest = (UninstallRequest) request;
        } else {
            uninstallRequest = new UninstallRequest(request);
        }

        return uninstallRequest;
    }

    @Override
    protected void start() throws Exception
    {
        Collection<ExtensionId> extensions = getRequest().getExtensions();

        notifyPushLevelProgress(extensions.size());

        try {
            for (ExtensionId extensionId : extensions) {
                if (extensionId.getVersion() != null) {
                    InstalledExtension installedExtension = this.installedExtensionRepository.resolve(extensionId);

                    if (getRequest().hasNamespaces()) {
                        uninstallExtension(installedExtension, getRequest().getNamespaces(), this.extensionTree);
                    } else if (installedExtension.getNamespaces() != null) {
                        // Duplicate the namespace list to avoid ConcurrentModificationException
                        uninstallExtension(installedExtension,
                            new ArrayList<String>(installedExtension.getNamespaces()), this.extensionTree);
                    } else {
                        uninstallExtension(installedExtension, (String) null, this.extensionTree);
                    }
                } else {
                    if (getRequest().hasNamespaces()) {
                        uninstallExtension(extensionId.getId(), getRequest().getNamespaces(), this.extensionTree);
                    } else {
                        uninstallExtension(extensionId.getId(), (String) null, this.extensionTree);
                    }
                }

                notifyStepPropress();
            }
        } finally {
            notifyPopLevelProgress();
        }
    }

    /**
     * @param extensionId the identifier of the extension to uninstall
     * @param namespaces the namespaces from where to uninstall the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws UninstallException error when trying to uninstall provided extensions
     */
    private void uninstallExtension(String extensionId, Collection<String> namespaces,
        Collection<ExtensionPlanNode> parentBranch) throws UninstallException
    {
        notifyPushLevelProgress(namespaces.size());

        try {
            for (String namespace : namespaces) {
                uninstallExtension(extensionId, namespace, parentBranch);

                notifyStepPropress();
            }
        } finally {
            notifyPopLevelProgress();
        }
    }

    /**
     * @param extensionId the identifier of the extension to uninstall
     * @param namespace the namespace from where to uninstall the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws UninstallException error when trying to uninstall provided extension
     */
    private void uninstallExtension(String extensionId, String namespace, Collection<ExtensionPlanNode> parentBranch)
        throws UninstallException
    {
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(extensionId, namespace);

        if (installedExtension == null) {
            throw new UninstallException(MessageFormat.format(EXCEPTION_NOTINSTALLED, extensionId));
        }

        try {
            uninstallExtension(installedExtension, namespace, parentBranch);
        } catch (Exception e) {
            throw new UninstallException("Failed to uninstall extension", e);
        }
    }

    /**
     * @param installedExtension the extension to uninstall
     * @param namespaces the namespaces from where to uninstall the extension
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws UninstallException error when trying to uninstall provided extension
     */
    private void uninstallExtension(InstalledExtension installedExtension, Collection<String> namespaces,
        Collection<ExtensionPlanNode> parentBranch) throws UninstallException
    {
        for (String namespace : namespaces) {
            uninstallExtension(installedExtension, namespace, parentBranch);
        }
    }

    /**
     * @param extensions the installed extensions to uninstall
     * @param namespace the namespaces from where to uninstall the extensions
     * @param parentBranch the children of the parent {@link DefaultExtensionPlanNode}
     * @throws UninstallException error when trying to uninstall provided extensions
     */
    private void uninstallExtensions(Collection<InstalledExtension> extensions, String namespace,
        Collection<ExtensionPlanNode> parentBranch) throws UninstallException
    {
        for (InstalledExtension backardDependency : extensions) {
            uninstallExtension(backardDependency, namespace, parentBranch);
        }
    }

    /**
     * @param installedExtension the extension to uninstall
     * @param namespace the namespace from where to uninstall the extension
     * @param parentBranch the children of the parent {@link ExtensionPlanNode}
     * @throws UninstallException error when trying to uninstall provided extension
     */
    private void uninstallExtension(InstalledExtension installedExtension, String namespace,
        Collection<ExtensionPlanNode> parentBranch) throws UninstallException
    {
        if (namespace != null
            && (installedExtension.getNamespaces() == null
            || !installedExtension.getNamespaces().contains(namespace))) {
            throw new UninstallException(MessageFormat.format(EXCEPTION_NOTINSTALLEDNAMESPACE, installedExtension,
                namespace));
        }

        // Log progression
        if (namespace != null) {
            this.logger.info("Resolving extension [{}] from namespace [{}]", installedExtension.getId(), namespace);
        } else {
            this.logger.info("Resolving extension [{}]", installedExtension.getId());
        }

        notifyPushLevelProgress(2);

        try {
            // Uninstall backward dependencies
            List<ExtensionPlanNode> children = new ArrayList<ExtensionPlanNode>();
            try {
                if (namespace != null) {
                    uninstallExtensions(this.installedExtensionRepository.getBackwardDependencies(installedExtension
                        .getId().getId(), namespace), namespace, children);
                } else {
                    for (Map.Entry<String, Collection<InstalledExtension>> entry : this.installedExtensionRepository
                        .getBackwardDependencies(installedExtension.getId()).entrySet()) {
                        uninstallExtensions(entry.getValue(), entry.getKey(), children);
                    }
                }
            } catch (ResolveException e) {
                throw new UninstallException("Failed to resolve backward dependencies of extension ["
                    + installedExtension + "]", e);
            }

            notifyStepPropress();

            DefaultExtensionPlanAction action =
                new DefaultExtensionPlanAction(installedExtension, null, Action.UNINSTALL, namespace, false);
            parentBranch.add(new DefaultExtensionPlanNode(action, children, null));
        } finally {
            notifyPopLevelProgress();
        }
    }
}
