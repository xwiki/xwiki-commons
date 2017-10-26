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
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.plan.internal.DefaultExtensionPlanTree;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.version.Version;
import org.xwiki.job.Request;

/**
 * Create an Extension upgrade plan.
 *
 * @version $Id$
 * @since 4.1M1
 */
@Component
@Named(UpgradePlanJob.JOBTYPE)
public class UpgradePlanJob extends AbstractInstallPlanJob<InstallRequest>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "upgradeplan";

    private static final String FAILED_INSTALL_MESSAGE = "Can't install extension [{}] on namespace [{}].";

    @Override
    public String getType()
    {
        return JOBTYPE;
    }

    @Override
    protected InstallRequest castRequest(Request request)
    {
        InstallRequest installRequest;
        if (request instanceof InstallRequest) {
            installRequest = (InstallRequest) request;
        } else {
            installRequest = new InstallRequest(request);
        }

        return installRequest;
    }

    private boolean isSkipped(InstalledExtension extension, String namespace)
    {
        // Explicitly skipped extensions

        if (getRequest().getExcludedExtensions().contains(extension.getId())) {
            return true;
        }

        // Extensions with no backward dependencies

        Collection<ExtensionId> requestExtensions = getRequest().getExtensions();
        boolean filterDependencies = requestExtensions == null || requestExtensions.isEmpty();

        if (filterDependencies) {
            // Don't skip if the extension has not been installed as dependency
            if (!extension.isDependency(namespace)) {
                return false;
            }

            // Don't skip if the extension "lost" its backward dependencies
            try {
                boolean hasBackwardDependencies;
                if (extension.getNamespaces() == null) {
                    hasBackwardDependencies =
                        !this.installedExtensionRepository.getBackwardDependencies(extension.getId()).isEmpty();
                } else {
                    hasBackwardDependencies = !extension.isDependency(namespace) && !this.installedExtensionRepository
                        .getBackwardDependencies(extension.getId().getId(), namespace).isEmpty();
                }

                return hasBackwardDependencies;
            } catch (ResolveException e) {
                // Should never happen
                this.logger.error("Failed to gather backward dependencies for extension [{}]", extension.getId(), e);
            }
        }

        return false;
    }

    /**
     * @param extension the extension currently installed
     * @param namespace the namespace where the extension is installed
     */
    protected void upgradeExtension(InstalledExtension extension, String namespace)
    {
        if (!isSkipped(extension, namespace)) {
            NavigableSet<Version> versions = getVersions(extension, namespace);

            // Useless to continue if the extension does not have any available version
            if (!versions.isEmpty()) {
                upgradeExtension(extension, namespace, versions.descendingSet());
            }
        }
    }

    private NavigableSet<Version> getVersions(InstalledExtension extension, String namespace)
    {
        NavigableSet<Version> versionList = new TreeSet<>();

        // Search local versions
        try {
            IterableResult<Version> versions =
                this.localExtensionRepository.resolveVersions(extension.getId().getId(), 0, -1);
            for (Version version : versions) {
                versionList.add(version);
            }
        } catch (ResolveException e) {
            this.logger.debug("Failed to resolve local versions for extension id [{}]", extension.getId().getId(), e);
        }

        // Search remote versions
        try {
            IterableResult<Version> versions = this.repositoryManager.resolveVersions(extension.getId().getId(), 0, -1);

            for (Version version : versions) {
                versionList.add(version);
            }
        } catch (ResolveException e) {
            this.logger.debug("Failed to resolve remote versions for extension id [{}]", extension.getId().getId(), e);
        }

        // Make sure the current version is included if the extension is invalid (it's possible this version does
        // not exist on any repository)
        if (!extension.isValid(namespace)) {
            versionList.add(extension.getId().getVersion());
        }

        return versionList;
    }

    protected void upgradeExtension(InstalledExtension extension, String namespace, Collection<Version> versionList)
    {
        for (Version version : versionList) {
            // Don't try to upgrade for lower versions but try to repair same version of the extension is invalid
            int compare = extension.getId().getVersion().compareTo(version);
            if (compare > 0 || (compare == 0 && extension.isValid(namespace))) {
                break;
            }

            // Only upgrade beta if the current is beta etc.
            if (extension.getId().getVersion().getType().ordinal() <= version.getType().ordinal()) {
                if (tryInstallExtension(new ExtensionId(extension.getId().getId(), version), namespace)) {
                    break;
                }
            }
        }
    }

    /**
     * Try to install the provided extension and update the plan if it's working.
     *
     * @param extensionId the extension version to install
     * @param namespace the namespace where to install the extension
     * @return true if the installation would succeed, false otherwise
     */
    protected boolean tryInstallExtension(ExtensionId extensionId, String namespace)
    {
        DefaultExtensionPlanTree currentTree = this.extensionTree.clone();

        try {
            installExtension(extensionId, namespace, currentTree);

            setExtensionTree(currentTree);

            return true;
        } catch (InstallException e) {
            if (getRequest().isVerbose()) {
                this.logger.info(FAILED_INSTALL_MESSAGE, extensionId, namespace, e);
            } else {
                this.logger.debug(FAILED_INSTALL_MESSAGE, extensionId, namespace, e);
            }
        }

        return false;
    }

    protected void upgrade(String namespace, Collection<InstalledExtension> installedExtensions)
    {
        this.progressManager.pushLevelProgress(installedExtensions.size(), this);

        try {
            for (InstalledExtension installedExtension : installedExtensions) {
                this.progressManager.startStep(this);

                if (namespace == null || !installedExtension.isInstalled(null)) {
                    upgradeExtension(installedExtension, namespace);
                }

                this.progressManager.endStep(this);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    protected void upgrade(Collection<InstalledExtension> installedExtensions)
    {
        this.progressManager.pushLevelProgress(installedExtensions.size(), this);

        try {
            for (InstalledExtension installedExtension : installedExtensions) {
                this.progressManager.startStep(this);

                if (installedExtension.getNamespaces() == null) {
                    upgradeExtension(installedExtension, null);
                } else {
                    this.progressManager.pushLevelProgress(installedExtension.getNamespaces().size(), this);

                    try {
                        for (String namespace : installedExtension.getNamespaces()) {
                            this.progressManager.startStep(this);

                            upgradeExtension(installedExtension, namespace);

                            this.progressManager.endStep(this);
                        }
                    } finally {
                        this.progressManager.popLevelProgress(this);
                    }
                }

                this.progressManager.endStep(this);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    protected Collection<InstalledExtension> getInstalledExtensions(String namespace)
    {
        Collection<ExtensionId> requestExtensions = getRequest().getExtensions();

        Collection<InstalledExtension> installedExtensions;

        if (requestExtensions != null && !requestExtensions.isEmpty()) {
            installedExtensions = new ArrayList<>(requestExtensions.size());

            for (ExtensionId requestExtension : requestExtensions) {
                InstalledExtension installedExtension =
                    this.installedExtensionRepository.getInstalledExtension(requestExtension);
                if (installedExtension.isInstalled(namespace)) {
                    installedExtensions.add(installedExtension);
                }
            }
        } else {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions(namespace);
        }

        return installedExtensions;
    }

    protected Collection<InstalledExtension> getInstalledExtensions()
    {
        Collection<ExtensionId> requestExtensions = getRequest().getExtensions();

        Collection<InstalledExtension> installedExtensions;

        if (requestExtensions != null && !requestExtensions.isEmpty()) {
            installedExtensions = new ArrayList<>(requestExtensions.size());

            for (ExtensionId requestExtension : requestExtensions) {
                InstalledExtension installedExtension =
                    this.installedExtensionRepository.getInstalledExtension(requestExtension);
                installedExtensions.add(installedExtension);
            }
        } else {
            installedExtensions = this.installedExtensionRepository.getInstalledExtensions();
        }

        return installedExtensions;
    }

    @Override
    protected void runInternal() throws Exception
    {
        Collection<String> namespaces = getRequest().getNamespaces();

        if (namespaces == null) {
            Collection<InstalledExtension> installedExtensions = getInstalledExtensions();

            upgrade(installedExtensions);
        } else {
            this.progressManager.pushLevelProgress(namespaces.size(), this);

            try {
                for (String namespace : namespaces) {
                    this.progressManager.startStep(this);

                    upgrade(namespace, getInstalledExtensions(namespace));

                    this.progressManager.endStep(this);
                }
            } finally {
                this.progressManager.popLevelProgress(this);
            }
        }
    }
}
