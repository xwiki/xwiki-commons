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
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.Job;
import org.xwiki.job.Request;

/**
 * Extension uninstallation related task.
 * <p>
 * This task generates related events.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Named(UninstallJob.JOBTYPE)
public class UninstallJob extends AbstractExtensionJob<UninstallRequest, DefaultJobStatus<UninstallRequest>>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "uninstall";

    /**
     * Used to generate the install plan.
     */
    @Inject
    @Named(UninstallPlanJob.JOBTYPE)
    private Job uninstallPlanJob;

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
        this.progressManager.pushLevelProgress(2, this);

        ExecutionContext context = this.execution.getContext();

        try {
            this.progressManager.startStep(this);

            // Create the plan

            UninstallRequest planRequest = new UninstallRequest(getRequest());
            planRequest.setId((List<String>) null);

            this.uninstallPlanJob.initialize(planRequest);
            this.uninstallPlanJob.run();

            ExtensionPlan plan = (ExtensionPlan) this.uninstallPlanJob.getStatus();

            if (plan.getError() != null) {
                throw new UninstallException("Failed to create install plan", plan.getError());
            }

            this.progressManager.endStep(this);

            this.progressManager.startStep(this);

            // Put the plan in context
            // TODO: use a stack ?
            context.setProperty(CONTEXTKEY_PLAN, plan);

            // Apply the plan

            Collection<ExtensionPlanAction> actions = plan.getActions();

            applyActions(actions);
        } finally {
            this.progressManager.popLevelProgress(this);

            // Clean context
            context.removeProperty(CONTEXTKEY_PLAN);
        }
    }
}
