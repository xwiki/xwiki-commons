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

import java.util.ArrayList;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.DisposePriority;
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
import org.xwiki.job.event.status.CancelableJobStatus;

/**
 * Default implementation of {@link JobExecutor}.
 * <p>
 * The executor is disposed before the other components (see {@link #DISPOSE_PRIORITY}): the jobs it waits for
 * during the shutdown need the stores, the caches, the event listeners and the logging to be still alive, otherwise a
 * job keeps running in a half-disposed system and finishes with silently missing side effects.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Singleton
@DisposePriority(DefaultJobExecutor.DISPOSE_PRIORITY)
public class DefaultJobExecutor implements JobExecutor, Initializable, Disposable
{
    /**
     * The dispose priority of the executor, lower than any other component's so that the running jobs are waited for
     * while the rest of the system is intact.
     *
     * @since 18.10.0RC1
     */
    public static final int DISPOSE_PRIORITY = 100;

    /**
     * Logger dedicated to shutdown information (same logger name as in other components).
     */
    private static final Logger SHUTDOWN_LOGGER = LoggerFactory.getLogger("org.xwiki.shutdown");

    /**
     * The maximum part of the shutdown timeout, in milliseconds, kept to let the jobs stopped at the end of the wait
     * (canceled or interrupted) store their status. It's also limited to half of the shutdown timeout.
     */
    private static final long MAX_STOP_RESERVE = 2000L;

    private static final String DISPOSED_MESSAGE = "The job executor is disposed";

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
            // Track the job before waiting for the group lock (which cannot be interrupted) so that #dispose() takes it
            // into account, but only list it as a current job of the group once it holds the lock
            addTriggeredJob(t, r);

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
                // Use computeIfPresent for synchronization.
                DefaultJobExecutor.this.groupedJobs.computeIfPresent(jobId, (k, v) -> {
                    // Remove the job, but only when it is the first job in the queue.
                    if (v.peek() == job) {
                        v.poll();
                    }

                    // If the queue is empty, remove it from the map.
                    if (v.isEmpty()) {
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
        /**
         * The jobs given a thread by this executor and not finished yet, including those still waiting for their group
         * lock (so not necessarily {@link org.xwiki.job.event.status.JobStatus.State#RUNNING}). Only used by
         * {@link DefaultJobExecutor#dispose()}.
         */
        private final Map<Job, Thread> triggeredJobs = Collections.synchronizedMap(new IdentityHashMap<>());

        JobThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue)
        {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        /**
         * @param t the thread of this executor which executes the job
         * @param r the job given to a thread of this executor
         */
        void addTriggeredJob(Thread t, Runnable r)
        {
            this.triggeredJobs.put((Job) r, t);
        }

        /**
         * Interrupt the thread executing the passed job, if it's still executed by this executor.
         *
         * @param job the job to interrupt
         */
        void interrupt(Job job)
        {
            Thread thread = this.triggeredJobs.get(job);
            if (thread != null) {
                thread.interrupt();
            }
        }

        /**
         * @return a copy of the set of jobs given a thread by this executor and not finished yet
         */
        List<Job> getTriggeredJobs()
        {
            // As this is a synchronized set, we need to explicitly synchronize on it for the iteration.
            synchronized (this.triggeredJobs) {
                return List.copyOf(this.triggeredJobs.keySet());
            }
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
            // Already done by grouped job executors before waiting for the group lock, adding it again is harmless
            addTriggeredJob(t, r);

            // Set a custom thread name corresponding to the job to make debugging easier
            Thread.currentThread().setName(getThreadName(r));

            // Make sure to set a clean classloader
            Thread.currentThread().setContextClassLoader(classloaderManager.getURLClassLoader(null, false));
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            Job job = (Job) r;

            this.triggeredJobs.remove(job);

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
     * Map&lt;groupname, group executor&gt;.
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
        List<JobThreadExecutor> executors;
        // Same monitor as #executeGroupedJob() so that no group executor is created after the snapshot
        synchronized (this.groupExecutors) {
            this.disposed = true;

            executors = new ArrayList<>(this.groupExecutors.size() + 1);
            executors.add(this.jobExecutor);
            executors.addAll(this.groupExecutors.values());
        }

        // Stop accepting new jobs and take the jobs which have not started yet out of the queues: it's too late for
        // them
        List<Runnable> queuedJobs = new ArrayList<>();
        for (JobThreadExecutor executor : executors) {
            executor.shutdown();
            executor.getQueue().drainTo(queuedJobs);
        }
        dropQueuedJobs(queuedJobs);

        List<Job> triggeredJobs = getTriggeredJobs(executors);
        if (!triggeredJobs.isEmpty()) {
            stopJobs(executors, triggeredJobs);
        }
    }

    /**
     * Stop the jobs given a thread by the executors within the shutdown timeout T, which is the whole budget of this
     * step: wait for the jobs to finish until T minus a reserve (min(2s, T/2)), then cancel the cancelable jobs still
     * running and interrupt the other ones, and use the reserve to let them stop and store their status. The jobs
     * still running at T are interrupted without waiting for them. With T = 0, the jobs are interrupted right away.
     *
     * @param executors the executors running the jobs
     * @param triggeredJobs the jobs given a thread by the executors and not finished yet
     */
    private void stopJobs(List<JobThreadExecutor> executors, List<Job> triggeredJobs)
    {
        long timeout = this.jobManagerConfiguration.getShutdownTimeout();

        if (timeout > 0) {
            long start = System.currentTimeMillis();
            long reserve = Math.min(MAX_STOP_RESERVE, timeout / 2);

            // Give the running jobs a chance to finish properly
            SHUTDOWN_LOGGER.info("Waiting up to [{}]ms for the following [{}] job(s) to finish: {}", timeout - reserve,
                triggeredJobs.size(), toJobIds(triggeredJobs));
            awaitTermination(executors, start + timeout - reserve);

            List<Job> remainingJobs = getTriggeredJobs(executors);
            if (!remainingJobs.isEmpty()) {
                SHUTDOWN_LOGGER.warn("The following [{}] job(s) did not finish within [{}]ms and are stopped (canceled"
                    + " when they are cancelable, interrupted otherwise) with [{}]ms left to store their status: {}."
                    + " Increase job.shutdownTimeout (and the time given to XWiki to stop) to let such jobs finish.",
                    remainingJobs.size(), timeout - reserve, reserve, toJobIds(remainingJobs));

                cancelOrInterruptJobs(executors, remainingJobs);
                awaitTermination(executors, start + timeout);

                remainingJobs = getTriggeredJobs(executors);
                if (!remainingJobs.isEmpty()) {
                    SHUTDOWN_LOGGER.warn("Interrupting the following [{}] job(s) still running after [{}]ms, without"
                        + " waiting for them: {}", remainingJobs.size(), timeout, toJobIds(remainingJobs));

                    executors.forEach(JobThreadExecutor::shutdownNow);
                }
            }
        } else {
            SHUTDOWN_LOGGER.warn("Interrupting the following [{}] running job(s) without waiting for them since"
                + " job.shutdownTimeout is 0: {}", triggeredJobs.size(), toJobIds(triggeredJobs));

            executors.forEach(JobThreadExecutor::shutdownNow);
        }
    }

    /**
     * Cancel the cancelable jobs, so that they stop at their next checkpoint, and interrupt the other ones.
     *
     * @param executors the executors running the jobs
     * @param jobs the jobs to stop
     */
    private void cancelOrInterruptJobs(List<JobThreadExecutor> executors, List<Job> jobs)
    {
        for (Job job : jobs) {
            if (job.getStatus() instanceof CancelableJobStatus status && status.isCancelable()) {
                status.cancel();
            } else {
                executors.forEach(executor -> executor.interrupt(job));
            }
        }
    }

    /**
     * Release the callers of jobs which are never going to be executed.
     *
     * @param queuedJobs the jobs which were waiting for a thread
     */
    private void dropQueuedJobs(List<Runnable> queuedJobs)
    {
        if (!queuedJobs.isEmpty()) {
            List<Job> droppedJobs = queuedJobs.stream().map(Job.class::cast).toList();

            SHUTDOWN_LOGGER.warn("The following [{}] job(s) were waiting to be executed and are dropped: {}",
                droppedJobs.size(), toJobIds(droppedJobs));

            for (Job job : droppedJobs) {
                if (job instanceof org.xwiki.job.AbstractJob<?, ?> abstractJob) {
                    // Mark the job finished (with an error) and store its status so that the callers waiting for it
                    // are released and a trace of it is kept. The error is caused by an interruption, like for the
                    // running jobs interrupted by the shutdown, so that both are reported the same way.
                    abstractJob.abort(new JobException("The job has not been executed because XWiki is stopping",
                        new InterruptedException("XWiki is stopping")));
                }
            }
        }
    }

    /**
     * @param executors the executors
     * @return the jobs given a thread by the passed executors and not finished yet (not necessarily
     *         {@link org.xwiki.job.event.status.JobStatus.State#RUNNING}: a grouped job may still be waiting for the
     *         lock of its group)
     */
    private List<Job> getTriggeredJobs(List<JobThreadExecutor> executors)
    {
        List<Job> triggeredJobs = new ArrayList<>();

        for (JobThreadExecutor executor : executors) {
            triggeredJobs.addAll(executor.getTriggeredJobs());
        }

        return triggeredJobs;
    }

    /**
     * @param jobs the jobs
     * @return the identifiers of the passed jobs, or their type for the jobs without identifier
     */
    private List<Object> toJobIds(List<Job> jobs)
    {
        return jobs.stream().map(job -> {
            List<String> id = job.getRequest() != null ? job.getRequest().getId() : null;

            return id != null ? id : "<" + job.getType() + '>';
        }).toList();
    }

    /**
     * Wait for the passed executors to terminate, until the passed deadline. The deadline is shared by all the
     * executors: the executors are waited for one after the other, but the total time spent is bounded by the deadline.
     *
     * @param executors the executors to wait for
     * @param deadline the time in milliseconds (see {@link System#currentTimeMillis()}) at which to stop waiting
     */
    private void awaitTermination(List<JobThreadExecutor> executors, long deadline)
    {
        try {
            for (JobThreadExecutor executor : executors) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0 || !executor.awaitTermination(remaining, TimeUnit.MILLISECONDS)) {
                    // Too late, don't wait for the other executors (the running jobs are reported by the caller)
                    return;
                }
            }
        } catch (InterruptedException e) {
            SHUTDOWN_LOGGER.warn("Interrupted while waiting for the running jobs to finish");

            Thread.currentThread().interrupt();
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
            throw new RejectedExecutionException(DISPOSED_MESSAGE);
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
            // Check again under the monitor so that no group executor is created after #dispose() took its snapshot
            if (this.disposed) {
                throw new RejectedExecutionException(DISPOSED_MESSAGE);
            }

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
