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
package org.xwiki.extension.job.history.internal;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.extension.job.history.ReplayRequest;
import org.xwiki.extension.job.internal.AbstractExtensionJob;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.AbstractRequest;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.Job;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.Request;

/**
 * A job that can replay records from the extension job history.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@Component
@Named(ReplayJob.JOB_TYPE)
public class ReplayJob extends AbstractJob<ReplayRequest, ReplayJobStatus> implements GroupedJob
{
    /**
     * The id of the job.
     */
    public static final String JOB_TYPE = "replay";

    /**
     * Specifies the group this job is part of. If all the extension history records to be replayed by this job target
     * the same wiki then this job is part of the group of extension jobs that run on that wiki (it will only block the
     * extension jobs that run on that wiki). Otherwise, if there is at least one record that targets multiple wikis or
     * the root namespace then this job is part of the group of extension jobs that run on global (root) namespace.
     */
    private JobGroupPath groupPath;

    /**
     * The default constructor.
     */
    public ReplayJob()
    {
        // We don't need an execution context in this job and more importantly we want to make sure sub jobs use their
        // own context (children job reuse parent job context by default)
        this.initExecutionContext = false;
    }

    @Override
    public String getType()
    {
        return JOB_TYPE;
    }

    @Override
    public JobGroupPath getGroupPath()
    {
        return this.groupPath;
    }

    @Override
    public void initialize(Request request)
    {
        super.initialize(request);

        // Build the job group path.
        String targetNamespace = getTargetNamespace();
        if (targetNamespace != null) {
            this.groupPath = new JobGroupPath(targetNamespace, AbstractExtensionJob.ROOT_GROUP);
        } else {
            this.groupPath = AbstractExtensionJob.ROOT_GROUP;
        }
    }

    @Override
    protected ReplayJobStatus createNewStatus(ReplayRequest request)
    {
        return new ReplayJobStatus(request, this.observationManager, this.loggerManager);
    }

    @Override
    protected void runInternal() throws Exception
    {
        List<ExtensionJobHistoryRecord> records = this.request.getRecords();
        if (records == null) {
            return;
        }

        this.progressManager.pushLevelProgress(records.size(), this);

        try {
            int currentRecordNumber = 0;
            for (ExtensionJobHistoryRecord record : records) {
                this.progressManager.startStep(this);
                this.status.setCurrentRecordNumber(currentRecordNumber++);
                replay(record);
                this.progressManager.endStep(this);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    private void replay(ExtensionJobHistoryRecord record) throws ComponentLookupException
    {
        // Make sure the executed job log end up in the replay job log
        if (record.getRequest() instanceof AbstractRequest) {
            ((AbstractRequest) record.getRequest()).setStatusLogIsolated(false);
        }

        Job job = this.componentManager.getInstance(Job.class, record.getJobType());
        job.initialize(record.getRequest());
        job.run();
    }

    private String getTargetNamespace()
    {
        List<ExtensionJobHistoryRecord> records = this.request.getRecords();
        if (records == null) {
            return null;
        }

        String targetNamespace = null;
        for (ExtensionJobHistoryRecord record : records) {
            Collection<String> namespaces = record.getRequest().getNamespaces();
            if (namespaces != null && namespaces.size() == 1) {
                String namespace = namespaces.iterator().next();
                if (targetNamespace == null) {
                    targetNamespace = namespace;
                } else if (!targetNamespace.equals(namespace)) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return targetNamespace;
    }
}
