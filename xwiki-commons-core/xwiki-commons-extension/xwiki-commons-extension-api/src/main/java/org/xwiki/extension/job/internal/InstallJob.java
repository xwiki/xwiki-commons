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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.extension.Extension;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.repository.LocalExtensionRepositoryException;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.Job;
import org.xwiki.job.Request;
import org.xwiki.logging.marker.TranslationMarker;

/**
 * Extension installation related task.
 * <p>
 * This task generates related events.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Named(InstallJob.JOBTYPE)
public class InstallJob extends AbstractExtensionJob<InstallRequest, DefaultJobStatus<InstallRequest>>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "install";

    private static final TranslationMarker LOG_DOWNLOADING = new TranslationMarker("extension.log.job.downloading");

    /**
     * Used to generate the install plan.
     */
    @Inject
    @Named(InstallPlanJob.JOBTYPE)
    private Job installPlanJob;

    /**
     * Used to access the execution context.
     */
    @Inject
    private Execution execution;

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
        this.progressManager.pushLevelProgress(3, this);

        ExecutionContext context = this.execution.getContext();

        try {
            this.progressManager.startStep(this);

            // Create the plan

            InstallRequest planRequest = new InstallRequest(getRequest());
            planRequest.setId((List<String>) null);

            planRequest.setVerbose(getRequest().isVerbose());

            this.installPlanJob.initialize(planRequest);
            this.installPlanJob.run();

            ExtensionPlan plan = (ExtensionPlan) this.installPlanJob.getStatus();

            if (plan.getError() != null) {
                throw new InstallException("Failed to create install plan", plan.getError());
            }

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            // Put the plan in context
            // TODO: use a stack ?
            context.setProperty(CONTEXTKEY_PLAN, plan);

            // Apply the plan

            Collection<ExtensionPlanAction> actions = plan.getActions();

            // Download all extensions

            this.progressManager.pushLevelProgress(actions.size(), actions);

            try {
                for (ExtensionPlanAction action : actions) {
                    this.progressManager.startStep(actions);

                    store(action);

                    this.progressManager.endStep(actions);
                }
            } finally {
                this.progressManager.popLevelProgress(actions);
            }

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            // Install all extensions

            applyActions(actions);
        } finally {
            this.progressManager.popLevelProgress(this);

            // Clean context
            context.removeProperty(CONTEXTKEY_PLAN);
        }
    }

    /**
     * @param action the action containing the extension to download
     * @throws LocalExtensionRepositoryException failed to store extension
     */
    private void store(ExtensionPlanAction action) throws LocalExtensionRepositoryException
    {
        if (action.getAction() == Action.INSTALL || action.getAction() == Action.UPGRADE
            || action.getAction() == Action.DOWNGRADE) {
            storeExtension(action.getExtension());
        }
    }

    /**
     * @param extension the extension to store
     * @throws LocalExtensionRepositoryException failed to store extension
     */
    private void storeExtension(Extension extension) throws LocalExtensionRepositoryException
    {
        if (!this.localExtensionRepository.exists(extension.getId())) {
            if (getRequest().isVerbose()) {
                this.logger.info(LOG_DOWNLOADING, "Downloading extension [{}]", extension.getId());
            }

            this.localExtensionRepository.storeExtension(extension);
        }
    }
}
