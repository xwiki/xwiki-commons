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
package org.xwiki.job.internal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.ObjectUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobManager;
import org.xwiki.job.Request;
import org.xwiki.job.event.status.JobStatus;

/**
 * Default implementation of {@link JobManager}.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultJobManager implements JobManager, Runnable, Initializable
{
    /**
     * A job to execute.
     * 
     * @version $Id$
     */
    private static class JobElement
    {
        /**
         * The job to execute.
         */
        public Job job;

        /**
         * The request to use to control the job.
         */
        public Request request;

        /**
         * @param job the job to execute
         * @param request the request to use to control the job
         */
        public JobElement(Job job, Request request)
        {
            this.job = job;
            this.request = request;
        }
    }

    /**
     * Used to lookup {@link Job} implementations.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Used to store the results of the jobs execution.
     */
    @Inject
    private JobStatusStorage storage;

    /**
     * Used to get the Execution Context.
     */
    @Inject
    private Execution execution;

    /**
     * Used to create a new Execution Context from scratch.
     */
    @Inject
    private ExecutionContextManager executionContextManager;

    /**
     * @see #getCurrentJob()
     */
    private volatile Job currentJob;

    /**
     * The queue of jobs to execute.
     */
    private BlockingQueue<JobElement> jobQueue = new LinkedBlockingQueue<JobElement>();

    /**
     * The thread on which the job manager is running.
     */
    private Thread thread;

    @Override
    public void initialize() throws InitializationException
    {
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
        this.thread.start();
    }

    // Runnable

    @Override
    public void run()
    {
        // Create a clean Execution Context
        ExecutionContext context = new ExecutionContext();

        try {
            this.executionContextManager.initialize(context);
        } catch (ExecutionContextException e) {
            throw new RuntimeException("Failed to initialize IRC Bot's execution context", e);
        }

        this.execution.pushContext(context);

        try {
            while (!this.thread.isInterrupted()) {
                try {
                    JobElement element = this.jobQueue.take();

                    this.currentJob = element.job;

                    // Wait in case synchronous job is running
                    synchronized (this) {
                        this.currentJob.start(element.request);
                    }
                } catch (InterruptedException e) {
                    // Thread has been stopped
                }
            }
        } finally {
            this.execution.removeContext();
        }
    }

    // JobManager

    @Override
    public Job getCurrentJob()
    {
        return this.currentJob;
    }

    /**
     * @param jobType the job id
     * @return a new job
     * @throws JobException failed to create a job for the provided type
     */
    private Job createJob(String jobType) throws JobException
    {
        Job job;
        try {
            job = this.componentManager.getInstance(Job.class, jobType);
        } catch (ComponentLookupException e) {
            throw new JobException("Failed to lookup any Job for role hint [" + jobType + "]", e);
        }

        return job;
    }

    @Override
    public Job executeJob(String jobType, Request request) throws JobException
    {
        Job job = addJob(jobType, request);

        try {
            job.join();
        } catch (InterruptedException e) {
            // Ignore
        }

        return job;
    }

    @Override
    public Job addJob(String jobType, Request request) throws JobException
    {
        Job job = createJob(jobType);

        this.jobQueue.add(new JobElement(job, request));

        return job;
    }

    @Override
    public JobStatus getJobStatus(String id)
    {
        return getJobStatus(Arrays.asList(id));
    }

    @Override
    public JobStatus getJobStatus(List<String> id)
    {
        if (this.currentJob != null && ObjectUtils.equals(id, this.currentJob.getRequest().getId())) {
            return this.currentJob.getStatus();
        }

        return this.storage.getJobStatus(id);
    }
}
