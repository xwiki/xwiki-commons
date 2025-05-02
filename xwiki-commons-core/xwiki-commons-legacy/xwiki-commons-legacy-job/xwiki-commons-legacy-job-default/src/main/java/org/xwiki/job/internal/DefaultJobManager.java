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
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

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
import org.xwiki.job.JobStatusStore;
import org.xwiki.job.Request;
import org.xwiki.job.event.status.JobStatus;

/**
 * Default implementation of {@link JobManager}.
 *
 * @version $Id$
 * @since 4.0M1
 * @deprecated since 6.1M2, use {@link DefaultJobExecutor} instead
 */
@Component
@Singleton
@Deprecated
public class DefaultJobManager implements JobManager, Runnable, Initializable
{
    /**
     * Used to lookup {@link Job} implementations.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManager;

    /**
     * Used to store the results of the jobs execution.
     */
    @Inject
    private JobStatusStore store;

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
    private BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();

    /**
     * The thread on which the job manager is running.
     */
    private Thread thread;

    @Override
    public void initialize() throws InitializationException
    {
        initialize("Job Manager daemon thread");
    }

    protected void initialize(String threadName) throws InitializationException
    {
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
        this.thread.start();
        this.thread.setName(threadName);
    }

    // Runnable

    @Override
    public void run()
    {
        while (!this.thread.isInterrupted()) {
            runJob();
        }
    }

    /**
     * Execute one job.
     */
    public void runJob()
    {
        try {
            this.currentJob = this.jobQueue.take();

            // Create a clean Execution Context
            ExecutionContext context = new ExecutionContext();

            try {
                this.executionContextManager.initialize(context);
            } catch (ExecutionContextException e) {
                throw new RuntimeException("Failed to initialize Job " + this.currentJob + " execution context", e);
            }

            this.currentJob.run();
        } catch (InterruptedException e) {
            // Thread has been stopped
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
            job = this.componentManager.get().getInstance(Job.class, jobType);
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

        job.initialize(request);

        this.jobQueue.offer(job);

        return job;
    }

    @Override
    public void addJob(Job job)
    {
        this.jobQueue.offer(job);
    }

    @Override
    public JobStatus getJobStatus(String id)
    {
        return getJobStatus(Arrays.asList(id));
    }

    @Override
    public JobStatus getJobStatus(List<String> id)
    {
        // Is it the current job
        if (this.currentJob != null && Objects.equals(id, this.currentJob.getRequest().getId())) {
            return this.currentJob.getStatus();
        }

        // Is it in queue
        for (Job job : this.jobQueue) {
            if (Objects.equals(id, job.getRequest().getId())) {
                return job.getStatus();
            }
        }

        // Is it stored
        return this.store.getJobStatus(id);
    }
}
