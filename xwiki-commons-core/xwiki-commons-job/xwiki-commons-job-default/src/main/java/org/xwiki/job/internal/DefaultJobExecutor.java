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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
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

import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.GroupedJobInitializer;
import org.xwiki.job.GroupedJobInitializerManager;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.JobManagerConfiguration;
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

        private final Set<Job> currentJobs =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

        private final String groupThreadName;

        private final GroupedJobInitializer initializer;

        JobGroupExecutor(JobGroupPath path, GroupedJobInitializer initializer)
        {
            super(initializer.getPoolSize(), initializer.getPoolSize(),
                DefaultJobExecutor.this.jobManagerConfiguration.getGroupedJobThreadKeepAliveTime(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

            // We want to be sure to not keep idle threads.
            this.allowCoreThreadTimeOut(true);
            this.initializer = initializer;
            setThreadFactory(this);

            this.path = path;
            this.groupThreadName = this.path + " job group daemon thread";
        }

        @Override
        protected String getThreadName(Runnable r)
        {
            return this.groupThreadName + " - " + r;
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

            this.currentJobs.add((Job) r);

            super.beforeExecute(t, r);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            DefaultJobExecutor.this.lockTree.unlock(this.path);

            Job job = (Job) r;

            this.currentJobs.remove(job);

            super.afterExecute(r, t);

            List<String> jobId = job.getRequest().getId();
            if (jobId != null) {
                // Delete the job from the job group's queue. Remove the queue when it is empty.
                // Use compute for synchronization.
                DefaultJobExecutor.this.groupedJobs.compute(jobId, (k, v) -> {
                    // Remove the job, but only when it is the first job in the queue.
                    if (v != null && v.peek() == job) {
                        v.poll();
                    }

                    // If the queue is empty, remove it from the map.
                    if (v == null || v.isEmpty()) {
                        return null;
                    }

                    // Otherwise, keep the (non-empty) queue.
                    return v;
                });
            }
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread thread = this.threadFactory.newThread(r);

            thread.setDaemon(true);
            thread.setName(this.groupThreadName);
            thread.setPriority(this.initializer.getDefaultPriority());

            return thread;
        }
    }

    private class JobThreadExecutor extends ThreadPoolExecutor
    {
        JobThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue)
        {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
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
            Thread.currentThread().setContextClassLoader(classloaderManager.getURLClassLoader(null, false));
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            Job job = (Job) r;

            List<String> jobId = job.getRequest().getId();
            if (jobId != null) {
                // Remove the job from the jobs map if its job ID actually maps to the job instance.
                // Use computeIfPresent for synchronization.
                DefaultJobExecutor.this.jobs.computeIfPresent(jobId, (k, v) -> {
                    if (v == job) {
                        // If the job ID maps to the job instance, remove it from the map.
                        return null;
                    }

                    // If the job ID does not map to the job instance, keep it in the map.
                    return v;
                });
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

    @Inject
    private JobManagerConfiguration jobManagerConfiguration;

    @Inject
    private GroupedJobInitializerManager groupedJobInitializerManager;

    @Inject
    private ClassLoaderManager classloaderManager;

    private final Map<List<String>, Queue<Job>> groupedJobs = new ConcurrentHashMap<>();

    private final Map<List<String>, Job> jobs = new ConcurrentHashMap<>();

    /**
     * Handle care of hierarchical locking for grouped jobs.
     */
    @Inject
    private JobGroupPathLockTree lockTree;

    /**
     * Map<groupname, group executor>.
     */
    private final Map<JobGroupPath, JobGroupExecutor> groupExecutors = new ConcurrentHashMap<>();

    /**
     * Execute non grouped jobs.
     */
    private JobThreadExecutor jobExecutor;

    private volatile boolean disposed;

    @Override
    public void initialize() throws InitializationException
    {
        this.jobExecutor =
            new JobThreadExecutor(0, Integer.MAX_VALUE, this.jobManagerConfiguration.getSingleJobThreadKeepAliveTime(),
                TimeUnit.MILLISECONDS, new SynchronousQueue<>());
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
    public Job getCurrentJob(JobGroupPath groupPath)
    {
        return getCurrentJobs(groupPath).stream().findFirst().orElse(null);
    }

    @Override
    public List<Job> getCurrentJobs(JobGroupPath path)
    {
        JobGroupExecutor executor = this.groupExecutors.get(path);

        if (executor != null) {
            // Return an unmodifiable copy of the set of currently running jobs.
            // As this is a synchronized set, we need to explicitly synchronize on it for the iteration.
            synchronized (executor.currentJobs) {
                return List.copyOf(executor.currentJobs);
            }
        }

        return Collections.emptyList();
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
            return jobQueue.peek();
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
            if (job instanceof GroupedJob groupedJob) {
                executeGroupedJob(groupedJob);
            } else {
                executeSingleJob(job);
            }
        } else {
            throw new RejectedExecutionException("The job executor is disposed");
        }
    }

    private void executeSingleJob(Job job)
    {
        List<String> jobId = job.getRequest().getId();
        if (jobId != null) {
            this.jobs.put(jobId, job);
        }

        try {
            this.jobExecutor.execute(job);
        } catch (Exception e) {
            // In case the job is rejected, remove the entry in the current jobs again.
            if (jobId != null) {
                this.jobs.computeIfPresent(jobId, (k, v) -> v == job ? null : v);
            }

            // Let the caller handle the exception.
            throw e;
        }
    }

    private void executeGroupedJob(GroupedJob job)
    {
        // While synchronization isn't necessary for the insertion in the group executors, this ensures that jobs in
        // the "groupedJobs" queues have the same order as in the executor's queue.
        synchronized (this.groupExecutors) {
            JobGroupPath path = job.getGroupPath();

            // If path is null execute as non grouped job
            if (path == null) {
                executeSingleJob(job);

                return;
            }

            JobGroupExecutor groupExecutor = this.groupExecutors.computeIfAbsent(path,
                p -> new JobGroupExecutor(p, this.groupedJobInitializerManager.getGroupedJobInitializer(p)));

            List<String> jobId = job.getRequest().getId();
            if (jobId != null) {
                // Insert a new queue into the map and add the job to it.
                // Use compute() to synchronize on the job ID to prevent that another thread would remove the empty
                // queue again.
                this.groupedJobs.compute(jobId, (k, v) -> {
                    // If there is no queue for that ID, create a new one.
                    Queue<Job> queue = Objects.requireNonNullElseGet(v, ConcurrentLinkedQueue::new);
                    // Directly add the job to the queue before releasing the lock to ensure that all queues in the
                    // map are non-empty.
                    queue.offer(job);
                    return queue;
                });
            }

            // Execute the job only once it has been inserted in the groupedJobs to ensure that there is no race
            // condition when the job completes before it has been inserted into groupedJobs.
            try {
                groupExecutor.execute(job);
            } catch (Exception e) {
                // Remove the queued job again.
                if (jobId != null) {
                    this.groupedJobs.computeIfPresent(jobId, (k, v) -> {
                        // Remove the job object from the queue to ensure that queue and executor queue are in sync.
                        if (v.removeIf(j -> j == job) && v.isEmpty()) {
                            return null;
                        }

                        return v;
                    });
                }

                // Let the caller handle the exception.
                throw e;
            }
        }
    }
}
