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

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.job.Request;

/**
 * Create an Extension uninstallation plan.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Named(UninstallPlanJob.JOBTYPE)
public class UninstallPlanJob extends AbstractExtensionPlanJob<UninstallRequest>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "uninstallplan";

    @Override
    public String getType()
    {
        return JOBTYPE;
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
    protected void runInternal() throws Exception
    {
        Collection<ExtensionId> extensions = getRequest().getExtensions();

        this.progressManager.pushLevelProgress(extensions.size(), this);

        try {
            for (ExtensionId extensionId : extensions) {
                this.progressManager.startStep(this);

                if (extensionId.getVersion() != null) {
                    InstalledExtension installedExtension = this.installedExtensionRepository.resolve(extensionId);

                    if (getRequest().hasNamespaces()) {
                        uninstallExtension(installedExtension, getRequest().getNamespaces(), this.extensionTree, true);
                    } else if (installedExtension.getNamespaces() != null) {
                        // Duplicate the namespace list to avoid ConcurrentModificationException
                        uninstallExtension(installedExtension,
                            new ArrayList<>(installedExtension.getNamespaces()), this.extensionTree, true);
                    } else {
                        uninstallExtension(installedExtension, (String) null, this.extensionTree, true);
                    }
                } else {
                    if (getRequest().hasNamespaces()) {
                        uninstallExtension(extensionId.getId(), getRequest().getNamespaces(), this.extensionTree, true);
                    } else {
                        uninstallExtension(extensionId.getId(), (String) null, this.extensionTree, true);
                    }
                }

                this.progressManager.endStep(this);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }
}
