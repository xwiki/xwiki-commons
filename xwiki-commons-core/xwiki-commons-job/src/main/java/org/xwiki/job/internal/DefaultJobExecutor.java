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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.Request;

/**
 * Default implementation of {@link JobExecutor}.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Singleton
public class DefaultJobExecutor implements JobExecutor, Initializable, Disposable
{
    private class JobGroupExecutor extends JobThreadExecutor implements ThreadFactory
    {
        private final ThreadFactory threadFactory = Executors.defaultThreadFactory();

        private final JobGroupPath path;

        private Job currentJob;

        private String groupThreadName;

        JobGroupExecutor(JobGroupPath path)
        {
            super(1, 36000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

            setThreadFactory(this);

            this.path = path;
            this.groupThreadName = this.path + " job group daemon thread";
        }

        @Override
        protected String getThreadName(Runnable r)
        {
            return this.groupThreadName + " - " + this.currentJob;
        }

        @Override
        protected String getExecutorThreadName(Runnable r)
        {
            return this.groupThreadName;
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r)
        {
            DefaultJobExecutor.this.lockTree.lock(this.path);

            this.currentJob = (Job) r;

            super.beforeExecute(t, r);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            DefaultJobExecutor.this.lockTree.unlock(this.path);

            this.currentJob = null;

            super.afterExecute(r, t);

            Job job = (Job) r;

            List<String> jobId = job.getRequest().getId();
            if (jobId != null) {
                synchronized (DefaultJobExecutor.this.groupedJobs) {
                    Queue<Job> jobQueue = DefaultJobExecutor.this.groupedJobs.get(jobId);
                    if (jobQueue != null) {
                        if (jobQueue.peek() == job) {
                            jobQueue.poll();
                        }
                    }
                }
            }
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread thread = this.threadFactory.newThread(r);

            thread.setDaemon(true);
            thread.setName(this.groupThreadName);

            return thread;
        }
    }

    private class JobThreadExecutor extends ThreadPoolExecutor
    {
        JobThreadExecutor(int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
        {
            super(0, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        protected String getThreadName(Runnable r)
        {
            return r.toString();
        }

        protected String getExecutorThreadName(Runnable r)
        {
            return "Unused job pool thread";
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r)
        {
            // Set a custom thread name corresponding to the job to make debugging easier
            Thread.currentThread().setName(getThreadName(r));

            // Make sure to set a clean classloader
            Thread.currentThread().setContextClassLoader(initClassLoader);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            Job job = (Job) r;

            List<String> jobId = job.getRequest().getId();
            if (jobId != null) {
                synchronized (DefaultJobExecutor.this.jobs) {
                    Job storedJob = DefaultJobExecutor.this.jobs.get(jobId);
                    if (storedJob == job) {
                        DefaultJobExecutor.this.jobs.remove(jobId);
                    }
                }
            }

            // Reset thread name since it's not used anymore
            Thread.currentThread().setName(getExecutorThreadName(r));
        }
    }

    /**
     * Used to lookup {@link Job} implementations.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManager;

    private final Map<List<String>, Queue<Job>> groupedJobs = new ConcurrentHashMap<List<String>, Queue<Job>>();

    private final Map<List<String>, Job> jobs = new ConcurrentHashMap<List<String>, Job>();

    /**
     * Handle care of hierarchical locking for grouped jobs.
     */
    private final JobGroupPathLockTree lockTree = new JobGroupPathLockTree();

    private ClassLoader initClassLoader;

    /**
     * Map<groupname, group executor>.
     */
    private final Map<JobGroupPath, JobGroupExecutor> groupExecutors =
        new ConcurrentHashMap<JobGroupPath, JobGroupExecutor>();

    /**
     * Execute non grouped jobs.
     */
    private JobThreadExecutor jobExecutor;

    private volatile boolean disposed;

    @Override
    public void initialize() throws InitializationException
    {
        this.initClassLoader = Thread.currentThread().getContextClassLoader();

        this.jobExecutor =
            new JobThreadExecutor(Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        synchronized (this) {
            this.disposed = true;

            this.jobExecutor.shutdownNow();
            for (JobGroupExecutor executor : this.groupExecutors.values()) {
                executor.shutdownNow();
            }
        }
    }

    // JobManager

    @Override
    public Job getCurrentJob(JobGroupPath path)
    {
        JobGroupExecutor executor = this.groupExecutors.get(path);

        return executor != null ? executor.currentJob : null;
    }

    @Override
    public Job getJob(List<String> id)
    {
        // Is it a standalone job
        Job job = this.jobs.get(id);
        if (job != null) {
            return job;
        }

        // Is it in a group
        Queue<Job> jobQueue = this.groupedJobs.get(id);
        if (jobQueue != null) {
            job = jobQueue.peek();
            if (job != null) {
                return job;
            }
        }

        return null;
    }

    /**
     * @param jobType the job id
     * @param request the request
     * @return a new job
     * @throws JobException failed to create a job for the provided type
     */
    private Job createJob(String jobType, Request request) throws JobException
    {
        Job job;
        try {
            job = this.componentManager.get().getInstance(Job.class, jobType);
        } catch (ComponentLookupException e) {
            throw new JobException("Failed to lookup any Job for role hint [" + jobType + "]", e);
        }

        job.initialize(request);

        return job;
    }

    @Override
    public Job execute(String jobType, Request request) throws JobException
    {
        Job job = createJob(jobType, request);

        execute(job);

        return job;
    }

    @Override
    public void execute(Job job)
    {
        if (!this.disposed) {
            if (job instanceof GroupedJob) {
                executeGroupedJob((GroupedJob) job);
            } else {
                executeSingleJob(job);
            }
        } else {
            throw new RejectedExecutionException("The job executor is disposed");
        }
    }

    private void executeSingleJob(Job job)
    {
        this.jobExecutor.execute(job);

        List<String> jobId = job.getRequest().getId();
        if (jobId != null) {
            synchronized (this.jobs) {
                this.jobs.put(jobId, job);
            }
        }
    }

    private void executeGroupedJob(GroupedJob job)
    {
        synchronized (this.groupExecutors) {
            JobGroupPath path = job.getGroupPath();

            // If path is null execute as non grouped job
            if (path == null) {
                executeSingleJob(job);

                return;
            }

            JobGroupExecutor groupExecutor = this.groupExecutors.get(path);

            if (groupExecutor == null) {
                groupExecutor = new JobGroupExecutor(path);
                this.groupExecutors.put(path, groupExecutor);
            }

            groupExecutor.execute(job);

            List<String> jobId = job.getRequest().getId();
            if (jobId != null) {
                synchronized (this.groupedJobs) {
                    Queue<Job> jobQueue = this.groupedJobs.get(jobId);
                    if (jobQueue == null) {
                        jobQueue = new ConcurrentLinkedQueue<Job>();
                        this.groupedJobs.put(jobId, jobQueue);
                    }
                    jobQueue.offer(job);
                }
            }
        }
    }
}
