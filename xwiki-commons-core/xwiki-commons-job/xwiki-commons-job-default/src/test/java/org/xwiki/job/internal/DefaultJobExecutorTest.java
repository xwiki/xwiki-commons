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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.annotation.DisposePriority;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.GroupedJobInitializer;
import org.xwiki.job.GroupedJobInitializerManager;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.JobStatusStore;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.test.TestBasicGroupedJob;
import org.xwiki.job.test.TestBlockingJob;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultJobExecutor};
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList({ContextComponentManagerProvider.class, JobGroupPathLockTree.class})
class DefaultJobExecutorTest
{
    /**
     * Wait value used during the test: we use 100 milliseconds for not slowing down the test and it should be plenty
     * enough. Change the value for debugging purpose only.
     */
    private static final int WAIT_VALUE = 100;

    @InjectMockComponents
    private DefaultJobExecutor executor;

    @InjectMockComponents
    private TestBlockingJob blockingJob;

    @MockComponent
    private JobManagerConfiguration jobManagerConfiguration;

    @MockComponent
    private JobStatusStore jobStatusStore;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @BeforeEach
    void setup()
    {
        // We use a very short keep alive time.
        when(this.jobManagerConfiguration.getGroupedJobThreadKeepAliveTime()).thenReturn(1L);
        when(this.jobManagerConfiguration.getSingleJobThreadKeepAliveTime()).thenReturn(1L);
    }

    @MockComponent
    private GroupedJobInitializerManager groupedJobInitializerManager;

    private void waitJobWaiting(Job job)
    {
        waitJobState(State.WAITING, job);
    }

    private void waitJobFinished(Job job)
    {
        waitJobState(State.FINISHED, job);
    }

    /**
     * Waits until the passed job reaches the passed state, failing the test when it does not within
     * {@link #WAIT_VALUE} milliseconds.
     * <p>
     * The timeout is raised by throwing rather than through {@code fail()} on purpose: this method is a barrier that
     * drives the choreography to its next step, while what the test expects of each job is stated explicitly by the
     * assertion that follows the wait. Using {@code fail()} here makes every call count as an assertion of the
     * calling test, counting each checkpoint twice and making a test that only waits look like it verifies something.
     *
     * @param expected the state the job is expected to reach
     * @param job the job to watch
     */
    private void waitJobState(State expected, Job job)
    {
        waitCondition(() -> expected == job.getStatus().getState(),
            () -> String.format("Job never reached expected state [%s]. Still [%s] after %s milliseconds", expected,
                job.getStatus().getState(), WAIT_VALUE));
    }

    /**
     * Waits until the passed condition is true, failing when it's not within {@link #WAIT_VALUE} milliseconds.
     *
     * @param condition the condition to wait for
     * @param message the failure message
     */
    private void waitCondition(BooleanSupplier condition, Supplier<String> message)
    {
        waitCondition(condition, message, WAIT_VALUE);
    }

    /**
     * Waits until the passed condition is true, failing when it's not within the passed number of milliseconds.
     *
     * @param condition the condition to wait for
     * @param message the failure message
     * @param maxWait the maximum number of milliseconds to wait
     */
    private void waitCondition(BooleanSupplier condition, Supplier<String> message, int maxWait)
    {
        int wait = 0;

        do {
            if (condition.getAsBoolean()) {
                return;
            }

            // Wait a bit
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                throw new AssertionError("Condition monitor has been interrupted", e);
            }

            wait += 1;
        } while (wait < maxWait);

        throw new AssertionError(message.get());
    }

    @Test
    void matchingGroupPathAreBlocked()
    {
        // Every job group of this test uses a pool of size 1.
        mockAllPools(1);

        TestBasicGroupedJob jobA = groupedJob("A");
        TestBasicGroupedJob jobAB = groupedJob("A", "B");

        TestBasicGroupedJob job12 = groupedJob("1", "2");
        TestBasicGroupedJob job1 = groupedJob("1");

        // Pre-lock all jobs
        jobA.lock();
        jobAB.lock();
        job12.lock();
        job1.lock();

        // Give first jobs to JobExecutor
        this.executor.execute(jobA);
        this.executor.execute(job12);

        // Give enough time for the jobs to be fully taken into account
        waitJobWaiting(jobA);
        waitJobWaiting(job12);

        // Give following jobs to JobExecutor (to make sure they are actually after since the grouped job executor queue
        // is not "fair")
        this.executor.execute(jobAB);
        this.executor.execute(job1);

        // A and A/B

        assertSame(State.WAITING, jobA.getStatus().getState());
        assertNull(jobAB.getStatus().getState());

        // Next job
        jobA.unlock();
        waitJobWaiting(jobAB);

        assertSame(State.FINISHED, jobA.getStatus().getState());
        assertSame(State.WAITING, jobAB.getStatus().getState());

        // Next job
        jobAB.unlock();
        waitJobFinished(jobAB);

        assertSame(State.FINISHED, jobA.getStatus().getState());
        assertSame(State.FINISHED, jobAB.getStatus().getState());

        // 1/2 and 1

        assertSame(State.WAITING, job12.getStatus().getState());
        assertNull(job1.getStatus().getState());

        // Next job
        job12.unlock();
        waitJobWaiting(job1);

        assertSame(State.FINISHED, job12.getStatus().getState());
        assertSame(State.WAITING, job1.getStatus().getState());

        // Next job
        job1.unlock();
        waitJobFinished(job1);

        assertSame(State.FINISHED, job12.getStatus().getState());
        assertSame(State.FINISHED, job1.getStatus().getState());
    }

    // There is no observable state change to poll for instead: see the comment at the Thread.sleep() call below.
    @SuppressWarnings("java:S2925")
    @Test
    void matchingGroupPathAreBlockedPoolMultiSizeParentFirst() throws InterruptedException
    {
        // Check the following setup:
        // Pool A of size 1 with 2 jobs (A1, A2)
        // Pool AB of size 2 with 3 jobs (AB1, AB2, AB3)
        // Order of starting jobs:
        //   - A1
        //   - AB1 && AB2
        //   - A2
        //   - AB3

        mockPool(1, "A");
        mockPool(2, "A", "B");

        TestBasicGroupedJob jobA1 = groupedJob("A");
        TestBasicGroupedJob jobA2 = groupedJob("A");

        TestBasicGroupedJob jobAB1 = groupedJob("A", "B");
        TestBasicGroupedJob jobAB2 = groupedJob("A", "B");
        TestBasicGroupedJob jobAB3 = groupedJob("A", "B");

        // Pre-lock all jobs
        jobA1.lock();
        jobA2.lock();
        jobAB1.lock();
        jobAB2.lock();
        jobAB3.lock();

        // Give first jobs to JobExecutor
        this.executor.execute(jobA1);

        // Give enough time for the jobs to be fully taken into account
        waitJobWaiting(jobA1);

        // Give following jobs to JobExecutor (to make sure they are actually after since the grouped job executor
        // queue is not "fair")
        this.executor.execute(jobAB1);
        this.executor.execute(jobAB2);

        assertSame(State.WAITING, jobA1.getStatus().getState());
        assertNull(jobA2.getStatus().getState());
        assertNull(jobAB1.getStatus().getState());
        assertNull(jobAB2.getStatus().getState());

        // Next job
        jobA1.unlock();
        waitJobFinished(jobA1);

        // AB1 and AB2 can now start since they were already waiting on a lock
        waitJobWaiting(jobAB1);
        waitJobWaiting(jobAB2);

        // We don't execute A2 at the same time as A1 because in that case A2 is put in the queue of the pool
        // and is not directly waiting on the lock. So in that case it's not deterministic to know which one between
        // AB1 or A2 will get the lock first. To avoid such situation we just wait after AB1 started to start A2.
        this.executor.execute(jobA2);

        // AB1 and AB2 were waiting on the lock: they can start both since the pool is of size 2
        // A2 is now waiting on a lock
        assertSame(State.FINISHED, jobA1.getStatus().getState());
        assertSame(State.WAITING, jobAB1.getStatus().getState());
        assertSame(State.WAITING, jobAB2.getStatus().getState());
        assertNull(jobA2.getStatus().getState());

        // Next job
        jobAB1.unlock();

        waitJobFinished(jobAB1);

        // A2 was given to the executor above but, unlike a lock acquisition, there is no observable state change we
        // can wait on to know its worker thread has actually reached (and registered itself on) the parent group's
        // lock yet: it may still only be queued. Wait a bit to make it overwhelmingly likely that has happened
        // before starting AB3, otherwise AB3's own registration could occasionally race ahead of A2's and this
        // assertion would flicker.
        Thread.sleep(WAIT_VALUE);

        // Start AB3 only now to be sure it does not take the lock before A2.
        this.executor.execute(jobAB3);

        // AB3 cannot start yet even if the pool is of 2 because A2 requested for the lock.
        assertSame(State.FINISHED, jobAB1.getStatus().getState());
        assertSame(State.WAITING, jobAB2.getStatus().getState());
        assertNull(jobAB3.getStatus().getState());
        assertNull(jobA2.getStatus().getState());

        // Next job
        jobAB2.unlock();

        // Once AB2 is finished the lock for A2 is released, it's starting.
        // AB3 is still waiting A2 to release the lock.
        waitJobFinished(jobAB2);
        waitJobWaiting(jobA2);

        assertSame(State.FINISHED, jobAB2.getStatus().getState());
        assertSame(State.WAITING, jobA2.getStatus().getState());
        assertNull(jobAB3.getStatus().getState());

        // Next job
        jobA2.unlock();

        // A2 is finished so AB3 can now take its turn.
        waitJobFinished(jobA2);
        waitJobWaiting(jobAB3);

        assertSame(State.FINISHED, jobA2.getStatus().getState());
        assertSame(State.WAITING, jobAB3.getStatus().getState());

        jobAB3.unlock();
        waitJobFinished(jobAB3);
        assertSame(State.FINISHED, jobAB3.getStatus().getState());
    }

    @Test
    void matchingGroupPathAreBlockedPoolMultiSizeChildrenFirst() throws InterruptedException
    {
        // Check the following setup:
        // Pool A of size 2 with 2 jobs (A1, A2)
        // Pool AB of size 2 with 3 jobs (AB1, AB2, AB3)
        // Order of starting jobs:
        //   - AB1 && AB2
        //   - A1 && A2
        //   - AB3

        mockPool(2, "A");
        mockPool(2, "A", "B");

        TestBasicGroupedJob jobA1 = groupedJob("A");
        TestBasicGroupedJob jobA2 = groupedJob("A");

        TestBasicGroupedJob jobAB1 = groupedJob("A", "B");
        TestBasicGroupedJob jobAB2 = groupedJob("A", "B");
        TestBasicGroupedJob jobAB3 = groupedJob("A", "B");

        // Pre-lock all jobs
        jobA1.lock();
        jobA2.lock();
        jobAB1.lock();
        jobAB2.lock();
        jobAB3.lock();

        // A/B and A

        this.executor.execute(jobAB1);
        this.executor.execute(jobAB2);

        waitJobWaiting(jobAB1);
        waitJobWaiting(jobAB2);

        this.executor.execute(jobA1);
        // We wait a bit to ensure A1 take the lock on the group before A2.
        // FIXME: We cannot use waitJobWaiting since the job itself is not started yet (blocked by previous jobs)
        Thread.sleep(WAIT_VALUE);
        this.executor.execute(jobA2);

        assertSame(State.WAITING, jobAB1.getStatus().getState());
        assertSame(State.WAITING, jobAB2.getStatus().getState());
        // AB1 and AB2 are taking all groups locks so A1 and A2 are not started yet
        assertNull(jobA1.getStatus().getState());
        assertNull(jobA2.getStatus().getState());

        // Unlock AB1 and finish it
        jobAB1.unlock();
        waitJobFinished(jobAB1);

        // We wait a bit to ensure A2 take the lock on the group before AB3.
        // FIXME: We cannot use waitJobWaiting since the job itself is not started yet (blocked by previous jobs)
        Thread.sleep(WAIT_VALUE);
        this.executor.execute(jobAB3);

        // AB1 released a seat so A1 can take it and start
        waitJobWaiting(jobA1);

        assertSame(State.FINISHED, jobAB1.getStatus().getState());
        assertSame(State.WAITING, jobAB2.getStatus().getState());
        assertSame(State.WAITING, jobA1.getStatus().getState());
        assertNull(jobA2.getStatus().getState());
        assertNull(jobAB3.getStatus().getState());

        // Unlock AB2 and finish it
        jobAB2.unlock();
        waitJobFinished(jobAB2);

        // AB2 released a seat so A2 can take it and start
        waitJobWaiting(jobA2);

        assertSame(State.FINISHED, jobAB2.getStatus().getState());
        assertSame(State.WAITING, jobA1.getStatus().getState());
        assertSame(State.WAITING, jobA2.getStatus().getState());
        assertNull(jobAB3.getStatus().getState());

        // Unlock A1 and A2 and finish them
        jobA1.unlock();
        jobA2.unlock();
        waitJobFinished(jobA1);
        waitJobFinished(jobA2);

        // There is now enough free seat for AB3 to start
        waitJobWaiting(jobAB3);

        assertSame(State.FINISHED, jobA1.getStatus().getState());
        assertSame(State.FINISHED, jobA2.getStatus().getState());
        assertSame(State.WAITING, jobAB3.getStatus().getState());

        jobAB3.unlock();
        waitJobFinished(jobAB3);
        assertSame(State.FINISHED, jobAB3.getStatus().getState());
    }

    /**
     * Give the blocking job to the executor.
     *
     * @param path the group of the job, null for a single job
     * @return the status of the job
     */
    private DefaultJobStatus<DefaultRequest> executeBlockingJob(JobGroupPath path)
    {
        DefaultRequest request = new DefaultRequest();
        request.setId("blocking");
        this.blockingJob.initialize(request);
        this.blockingJob.setGroupPath(path);

        this.executor.execute(this.blockingJob);

        return this.blockingJob.getStatus();
    }

    /**
     * Start the blocking job (as a single job) and wait for it to be running.
     *
     * @return the status of the started job
     */
    private DefaultJobStatus<DefaultRequest> startBlockingJob()
    {
        DefaultJobStatus<DefaultRequest> status = executeBlockingJob(null);

        waitJobState(State.RUNNING, this.blockingJob);

        return status;
    }

    /**
     * Dispose the executor in another thread (for the tests which need to act on the jobs while dispose() waits).
     *
     * @return the started thread
     */
    private Thread disposeLater()
    {
        Thread disposer = new Thread(() -> {
            try {
                this.executor.dispose();
            } catch (ComponentLifecycleException e) {
                throw new AssertionError(e);
            }
        });
        disposer.start();

        return disposer;
    }

    /**
     * Execute the passed action in a new thread after {@link #WAIT_VALUE} milliseconds: there is no observable state
     * change to wait for instead, the point is to release a running job while dispose() is waiting for it.
     *
     * @param action the action to execute
     * @return the started thread
     */
    @SuppressWarnings("java:S2925")
    private Thread executeLater(Runnable action)
    {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(WAIT_VALUE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            action.run();
        });
        thread.start();

        return thread;
    }

    @Test
    void disposedBeforeTheOtherComponents()
    {
        // The wait for the running jobs is only useful while the components they rely on are still alive
        DisposePriority priority = DefaultJobExecutor.class.getAnnotation(DisposePriority.class);

        assertNotNull(priority);
        assertTrue(priority.value() < DisposePriority.DEFAULT_PRIORITY);
        assertTrue(priority.value() < DefaultJobStatusStore.class.getAnnotation(DisposePriority.class).value());
    }

    @Test
    void disposeWaitsForRunningJob() throws Exception
    {
        when(this.jobManagerConfiguration.getShutdownTimeout()).thenReturn(10000L);

        DefaultJobStatus<DefaultRequest> status = startBlockingJob();

        // Release the job a bit after dispose() started waiting for it
        Thread unblocker = executeLater(this.blockingJob::unblock);

        this.executor.dispose();

        unblocker.join();

        // dispose() waited for the job to finish normally
        assertSame(State.FINISHED, status.getState());
        assertNull(status.getError());
        assertFalse(status.isCanceled());
        verify(this.jobStatusStore).storeAsync(status);

        // No new job is accepted
        assertThrows(RejectedExecutionException.class, () -> this.executor.execute(this.blockingJob));
    }

    /**
     * @param timeout the shutdown timeout
     * @param jobs the jobs listed in the message
     * @return the warning logged when the jobs don't finish within the wait part of the shutdown timeout
     */
    private String notFinishedMessage(long timeout, String jobs)
    {
        long reserve = Math.min(2000L, timeout / 2);

        return "The following [1] job(s) did not finish within [" + (timeout - reserve) + "]ms and are stopped"
            + " (canceled when they are cancelable, interrupted otherwise) with [" + reserve + "]ms left to store"
            + " their status: " + jobs + ". Increase job.shutdownTimeout (and the time given to XWiki to stop) to let"
            + " such jobs finish.";
    }

    @Test
    void disposeInterruptsAfterTimeout() throws Exception
    {
        when(this.jobManagerConfiguration.getShutdownTimeout()).thenReturn(1000L);

        DefaultJobStatus<DefaultRequest> status = startBlockingJob();

        long start = System.currentTimeMillis();
        this.executor.dispose();
        long duration = System.currentTimeMillis() - start;

        // The job is not cancelable: it was interrupted after the wait (timeout minus the 500ms reserve) and finished
        // right away, before the end of the reserve
        assertTrue(duration >= 500L && duration < 1000L, "dispose() took " + duration + "ms");
        assertSame(State.FINISHED, status.getState());
        assertInstanceOf(InterruptedException.class, status.getError());
        // The job is not cancelable so the error is the only marker
        assertFalse(status.isCanceled());
        verify(this.jobStatusStore).storeAsync(status);

        assertEquals(notFinishedMessage(1000L, "[[blocking]]"), this.logCapture.getMessage(0));
        assertEquals("Exception thrown during job execution", this.logCapture.getMessage(1));
        assertEquals(2, this.logCapture.size());
    }

    @Test
    void disposeCancelsAfterTimeout() throws Exception
    {
        when(this.jobManagerConfiguration.getShutdownTimeout()).thenReturn(1000L);

        this.blockingJob.setStopOnCancel(true);
        DefaultJobStatus<DefaultRequest> status = startBlockingJob();
        status.setCancelable(true);

        long start = System.currentTimeMillis();
        this.executor.dispose();
        long duration = System.currentTimeMillis() - start;

        // The cancelable job was canceled after the wait and stopped by itself, without being interrupted, before the
        // end of the reserve
        assertTrue(duration >= 500L && duration < 1000L, "dispose() took " + duration + "ms");
        assertSame(State.FINISHED, status.getState());
        assertNull(status.getError());
        assertTrue(status.isCanceled());
        verify(this.jobStatusStore).storeAsync(status);

        assertEquals(notFinishedMessage(1000L, "[[blocking]]"), this.logCapture.getMessage(0));
        assertEquals(1, this.logCapture.size());
    }

    @Test
    void disposeInterruptsCanceledJobWhichDoesNotStop() throws Exception
    {
        when(this.jobManagerConfiguration.getShutdownTimeout()).thenReturn(1000L);

        DefaultJobStatus<DefaultRequest> status = startBlockingJob();
        status.setCancelable(true);

        long start = System.currentTimeMillis();
        this.executor.dispose();
        long duration = System.currentTimeMillis() - start;

        // The job ignored the cancel: it was interrupted at the end of the timeout, without waiting for it
        assertTrue(duration >= 1000L && duration < 2000L, "dispose() took " + duration + "ms");
        waitCondition(() -> status.getState() == State.FINISHED, () -> "The interrupted job did not finish", 5000);
        assertInstanceOf(InterruptedException.class, status.getError());
        assertTrue(status.isCanceled());
        verify(this.jobStatusStore).storeAsync(status);

        assertEquals(notFinishedMessage(1000L, "[[blocking]]"), this.logCapture.getMessage(0));
        assertEquals("Interrupting the following [1] job(s) still running after [1000]ms, without waiting for them:"
            + " [[blocking]]", this.logCapture.getMessage(1));
        assertEquals("Exception thrown during job execution", this.logCapture.getMessage(2));
        assertEquals(3, this.logCapture.size());
    }

    @Test
    void disposeWithoutTimeoutInterruptsImmediately() throws Exception
    {
        when(this.jobManagerConfiguration.getShutdownTimeout()).thenReturn(0L);

        DefaultJobStatus<DefaultRequest> status = startBlockingJob();

        long start = System.currentTimeMillis();
        this.executor.dispose();
        long duration = System.currentTimeMillis() - start;

        // dispose() did not wait for the job, neither before nor after interrupting it (which would take 5s)
        assertTrue(duration < 1000L, "dispose() took " + duration + "ms");

        waitJobFinished(this.blockingJob);

        assertInstanceOf(InterruptedException.class, status.getError());
        // The job is not cancelable so the error is the only marker
        assertFalse(status.isCanceled());
        verify(this.jobStatusStore).storeAsync(status);

        assertEquals("Interrupting the following [1] running job(s) without waiting for them since"
            + " job.shutdownTimeout is 0: [[blocking]]", this.logCapture.getMessage(0));
        assertEquals("Exception thrown during job execution", this.logCapture.getMessage(1));
    }

    @Test
    void disposeDropsQueuedJobs() throws Exception
    {
        when(this.jobManagerConfiguration.getShutdownTimeout()).thenReturn(10000L);
        mockAllPools(1);

        TestBasicGroupedJob jobA1 = groupedJob("A");
        jobA1.lock();

        this.executor.execute(jobA1);
        waitJobWaiting(jobA1);

        // The blocking job is queued behind the first job of the group
        JobGroupPath path = new JobGroupPath(List.of("A"));
        DefaultJobStatus<DefaultRequest> status = executeBlockingJob(path);
        assertEquals(List.of(jobA1), this.executor.getCurrentJobs(path));

        // Dispose in another thread since the lock of the first job can only be released by the thread which locked it
        Thread disposer = disposeLater();

        // Wait for dispose() to have dropped the queued job before letting the running job finish
        waitCondition(() -> this.logCapture.size() > 0, () -> "The queued job has not been dropped");
        assertEquals("The following [1] job(s) were waiting to be executed and are dropped: [[blocking]]",
            this.logCapture.getMessage(0));
        jobA1.unlock();

        disposer.join();

        assertSame(State.FINISHED, jobA1.getStatus().getState());
        assertEquals(List.of(), this.executor.getCurrentJobs(path));

        // The queued job has never been started but its callers are released and it has a stored status
        assertSame(State.FINISHED, status.getState());
        assertInstanceOf(JobException.class, status.getError());
        assertEquals("The job has not been executed because XWiki is stopping", status.getError().getMessage());
        // Caused by an interruption, like the running jobs interrupted by the shutdown
        assertInstanceOf(InterruptedException.class, status.getError().getCause());
        assertNotNull(status.getStartDate());
        assertNotNull(status.getEndDate());
        assertTrue(this.blockingJob.join(1, TimeUnit.SECONDS));
        verify(this.jobStatusStore).store(status);
        assertEquals(1, this.logCapture.size());
    }

    @Test
    void disposeCancelsJobWaitingForGroupLock() throws Exception
    {
        // 2s to wait, then a 2s reserve during which the test releases the lock
        when(this.jobManagerConfiguration.getShutdownTimeout()).thenReturn(4000L);
        mockAllPools(1);

        TestBasicGroupedJob jobA = groupedJob("A");
        jobA.lock();

        this.executor.execute(jobA);
        waitJobWaiting(jobA);

        // The blocking job has its own group, so its own thread, but it has to wait for the lock held by the parent
        // group
        JobGroupPath path = new JobGroupPath(List.of("A", "B"));
        this.blockingJob.setStopOnCancel(true);
        DefaultJobStatus<DefaultRequest> status = executeBlockingJob(path);
        status.setCancelable(true);

        // Dispose in another thread since the lock of the first job can only be released by the thread which locked it
        Thread disposer = disposeLater();

        // dispose() takes into account the job waiting for its group lock: it's canceled after the wait
        waitCondition(status::isCanceled, () -> "The job waiting for its group lock has not been canceled", 5000);
        assertSame(State.NONE, status.getState());
        // It's not listed as a current job of its group since it does not hold the group lock
        assertEquals(List.of(), this.executor.getCurrentJobs(path));
        assertTrue(this.logCapture.getMessage(0).startsWith("The following [2] job(s) did not finish within [2000]ms"
            + " and are stopped"), this.logCapture.getMessage(0));
        assertTrue(this.logCapture.getMessage(0).contains("[blocking]"), this.logCapture.getMessage(0));

        // Let the first job finish: the canceled job gets the lock, starts and stops at its first checkpoint
        jobA.unlock();

        disposer.join();

        assertSame(State.FINISHED, jobA.getStatus().getState());
        assertSame(State.FINISHED, status.getState());
        assertNull(status.getError());
        assertTrue(status.isCanceled());
        assertEquals(1, this.logCapture.size());
    }

    private void mockAllPools(int poolSize)
    {
        // Note: the initializer must be built before the when() call since Mockito does not allow a stubbing to be
        // started while another one is in progress.
        GroupedJobInitializer initializer = groupedJobInitializer(poolSize);

        when(this.groupedJobInitializerManager.getGroupedJobInitializer(any())).thenReturn(initializer);
    }

    private void mockPool(int poolSize, String... path)
    {
        GroupedJobInitializer initializer = groupedJobInitializer(poolSize);

        when(this.groupedJobInitializerManager.getGroupedJobInitializer(new JobGroupPath(Arrays.asList(path))))
            .thenReturn(initializer);
    }

    private GroupedJobInitializer groupedJobInitializer(int poolSize)
    {
        GroupedJobInitializer initializer = mock();
        when(initializer.getPoolSize()).thenReturn(poolSize);
        when(initializer.getDefaultPriority()).thenReturn(Thread.NORM_PRIORITY);

        return initializer;
    }

    private TestBasicGroupedJob groupedJob(String... path)
    {
        return new TestBasicGroupedJob("type", new JobGroupPath(Arrays.asList(path)), new DefaultRequest());
    }
}
