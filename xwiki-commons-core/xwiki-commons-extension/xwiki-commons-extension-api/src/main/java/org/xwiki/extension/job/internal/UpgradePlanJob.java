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
import java.util.List;
import java.util.ListIterator;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.version.Version;
import org.xwiki.job.Request;

/**
 * Create an Extension installation plan.
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

    /**
     * @param extension the extension currently installed
     * @param namespace the namespace where the extension is installed
     */
    protected void upgradeExtension(InstalledExtension extension, String namespace)
    {
        if (!extension.isDependency()) {
            String extensionId = extension.getId().getId();

            IterableResult<Version> versions;
            try {
                versions = this.repositoryManager.resolveVersions(extensionId, 0, -1);

                if (versions.getSize() == 0) {
                    throw new ResolveException("Can't find any remote version for extension ([" + extension + "]");
                }

                List<Version> versionList = new ArrayList<Version>(versions.getSize());
                for (Version version : versions) {
                    versionList.add(version);
                }

                for (ListIterator<Version> it = versionList.listIterator(versionList.size()); it.hasPrevious();) {
                    Version version = it.previous();

                    // Only upgrade if the existing version is greater than the current one
                    if (extension.getId().getVersion().compareTo(version) >= 0) {
                        break;
                    }

                    // Only upgrade beta if the current is beta etc.
                    if (extension.getId().getVersion().getType().ordinal() <= version.getType().ordinal()) {
                        if (tryInstallExtension(new ExtensionId(extensionId, version), namespace)) {
                            break;
                        }
                    }
                }
            } catch (ResolveException e) {
                this.logger.debug("Failed to resolve versions for extension id [{}]", extensionId, e);
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
        ModifableExtensionPlanTree currentTree = this.extensionTree.clone();

        try {
            installExtension(extensionId, namespace, currentTree);

            setExtensionTree(currentTree);

            return true;
        } catch (InstallException e) {
            this.logger.debug("Can't install extension [{}] on namespace [{}].",
                new Object[] {extensionId, namespace, e});
        }

        return false;
    }

    @Override
    protected void start() throws Exception
    {
        Collection<String> namespaces = getRequest().getNamespaces();
        if (namespaces == null) {
            Collection<InstalledExtension> installedExtensions =
                this.installedExtensionRepository.getInstalledExtensions();

            for (InstalledExtension installedExtension : installedExtensions) {
                if (installedExtension.getNamespaces() == null) {
                    upgradeExtension(installedExtension, null);
                } else {
                    for (String namespace : installedExtension.getNamespaces()) {
                        upgradeExtension(installedExtension, namespace);
                    }
                }
            }
        } else {
            for (String namespace : namespaces) {
                Collection<InstalledExtension> installedExtensions =
                    this.installedExtensionRepository.getInstalledExtensions(namespace);

                for (InstalledExtension installedExtension : installedExtensions) {
                    upgradeExtension(installedExtension, namespace);
                }
            }
        }
    }
}
