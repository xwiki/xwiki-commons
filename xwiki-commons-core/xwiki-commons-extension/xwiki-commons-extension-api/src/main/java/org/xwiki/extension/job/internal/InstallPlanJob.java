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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.job.Request;

/**
 * Create an Extension installation plan.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Named(InstallPlanJob.JOBTYPE)
public class InstallPlanJob extends AbstractInstallPlanJob<InstallRequest>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "installplan";

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

    @Override
    protected void runInternal() throws Exception
    {
        Map<ExtensionId, Collection<String>> extensionsByNamespace =
            new LinkedHashMap<>();

        Collection<ExtensionId> extensions = getRequest().getExtensions();

        for (ExtensionId extensionId : extensions) {
            if (getRequest().hasNamespaces()) {
                Collection<String> namespaces = getRequest().getNamespaces();

                for (String namespace : namespaces) {
                    addExtensionToProcess(extensionsByNamespace, extensionId, namespace);
                }
            } else {
                addExtensionToProcess(extensionsByNamespace, extensionId, null);
            }
        }

        // Start the actual plan creation
        start(extensionsByNamespace);
    }
}
