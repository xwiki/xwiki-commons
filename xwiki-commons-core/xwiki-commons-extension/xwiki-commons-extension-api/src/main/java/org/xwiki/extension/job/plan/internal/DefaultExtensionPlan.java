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
package org.xwiki.extension.job.plan.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.job.plan.ExtensionPlanTree;
import org.xwiki.job.AbstractJobStatus;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.observation.ObservationManager;

/**
 * A plan of extension related actions to perform.
 *
 * @param <R>
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultExtensionPlan<R extends ExtensionRequest> extends AbstractJobStatus<R> implements ExtensionPlan
{
    /**
     * @see #getTree()
     */
    // TODO: find a way to serialize before making DefaultExtensionPlan Serializable (the main issue is the Extension
    // objects in the nodes)
    protected transient ExtensionPlanTree tree;

    /**
     * @param jobType the type of the job
     * @param request the request provided when started the job
     * @param observationManager the observation manager component
     * @param loggerManager the logger manager component
     * @param tree the tree representation of the plan, it's not copied but taken as it it to allow filling it from
     *            outside
     * @param parentJobStatus the status of the parent job (i.e. the status of the job that started this one); pass
     *            {@code null} if this job hasn't been started by another job (i.e. if this is not a sub-job)
     */
    public DefaultExtensionPlan(String jobType, R request, ObservationManager observationManager,
        LoggerManager loggerManager, ExtensionPlanTree tree, JobStatus parentJobStatus)
    {
        super(jobType, request, parentJobStatus, observationManager, loggerManager);

        this.tree = tree;
    }

    /**
     * @param extensions the list of fill with actions
     * @param nodes of branch of the tree representation of the plan
     */
    private void fillExtensionActions(Set<ExtensionPlanAction> extensions, Collection<ExtensionPlanNode> nodes)
    {
        for (ExtensionPlanNode node : nodes) {
            fillExtensionActions(extensions, node.getChildren());

            extensions.add(node.getAction());
        }
    }

    @Override
    public ExtensionPlanTree getTree()
    {
        return this.tree;
    }

    /**
     * @param tree the tree
     */
    public void setTree(ExtensionPlanTree tree)
    {
        this.tree = tree;
    }

    @Override
    public Collection<ExtensionPlanAction> getActions()
    {
        if (getState() != State.FINISHED) {
            Set<ExtensionPlanAction> extensions = new LinkedHashSet<ExtensionPlanAction>();
            fillExtensionActions(extensions, this.tree);

            return extensions;
        } else {
            Set<ExtensionPlanAction> actionsCache = new LinkedHashSet<ExtensionPlanAction>();
            fillExtensionActions(actionsCache, this.tree);

            return actionsCache;
        }
    }

    @Override
    public String toString()
    {
        return this.tree.toString();
    }
}
