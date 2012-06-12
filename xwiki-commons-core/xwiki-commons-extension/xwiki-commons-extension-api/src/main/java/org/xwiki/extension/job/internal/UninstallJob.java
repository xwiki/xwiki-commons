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
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.job.Job;
import org.xwiki.job.Request;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;

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
public class UninstallJob extends AbstractExtensionJob<UninstallRequest>
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
    protected void start() throws Exception
    {
        notifyPushLevelProgress(2);

        try {
            // Create the plan

            UninstallRequest planRequest = new UninstallRequest(getRequest());
            planRequest.setId((List<String>) null);

            this.uninstallPlanJob.start(planRequest);

            ExtensionPlan plan = (ExtensionPlan) this.uninstallPlanJob.getStatus();

            List<LogEvent> log = plan.getLog().getLogs(LogLevel.ERROR);
            if (!log.isEmpty()) {
                throw new UninstallException("Failed to create install plan: " + log.get(0).getFormattedMessage(), log
                    .get(0).getThrowable());
            }

            notifyStepPropress();

            // Apply the plan

            Collection<ExtensionPlanAction> actions = plan.getActions();

            notifyPushLevelProgress(actions.size());

            try {
                for (ExtensionPlanAction action : actions) {
                    if (action.getAction() != Action.NONE) {
                        applyAction(action);
                    }

                    notifyStepPropress();
                }
            } finally {
                notifyPopLevelProgress();
            }
        } finally {
            notifyPopLevelProgress();
        }
    }
}
