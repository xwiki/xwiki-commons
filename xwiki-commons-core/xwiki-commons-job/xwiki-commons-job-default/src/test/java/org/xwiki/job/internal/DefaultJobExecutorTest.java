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
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.GroupedJobInitializer;
import org.xwiki.job.GroupedJobInitializerManager;
import org.xwiki.job.Job;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.test.TestBasicGroupedJob;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    @MockComponent
    private JobManagerConfiguration jobManagerConfiguration;

    @BeforeEach
    public void setup()
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

    private void waitJobState(State expected, Job job)
    {
        int wait = 0;

        do {
            if (expected == job.getStatus().getState()) {
                return;
            }

            // Wait a bit
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                fail("Job state monitor has been interrupted");
            }

            wait += 1;
        } while (wait < WAIT_VALUE);

        fail(String.format("Job never reached expected state [%s]. Still [%s] after %s milliseconds",
            expected, job.getStatus().getState(), WAIT_VALUE));
    }

    @Test
    void matchingGroupPathAreBlocked()
    {
        GroupedJobInitializer groupedJobInitializer = mock(GroupedJobInitializer.class);
        when(groupedJobInitializer.getPoolSize()).thenReturn(1);
        when(groupedJobInitializer.getDefaultPriority()).thenReturn(Thread.NORM_PRIORITY);
        when(this.groupedJobInitializerManager.getGroupedJobInitializer(any())).thenReturn(groupedJobInitializer);
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

        ////////////////////
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

        ////////////////////
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

        assertSame(State.FINISHED, job1.getStatus().getState());
        assertSame(State.FINISHED, job1.getStatus().getState());
    }

    @Test
    void matchingGroupPathAreBlockedPoolMultiSizeParentFirst()
    {
        // Check the following setup:
        // Pool A of size 1 with 2 jobs (A1, A2)
        // Pool AB of size 2 with 3 jobs (AB1, AB2, AB3)
        // Order of starting jobs:
        //   - A1
        //   - AB1 && AB2
        //   - A2
        //   - AB3

        GroupedJobInitializer groupedJobInitializer = mock(GroupedJobInitializer.class);
        when(groupedJobInitializer.getPoolSize()).thenReturn(1);
        when(groupedJobInitializer.getDefaultPriority()).thenReturn(Thread.NORM_PRIORITY);

        JobGroupPath jobGroupPathA = new JobGroupPath(Collections.singletonList("A"));
        when(this.groupedJobInitializerManager.getGroupedJobInitializer(eq(jobGroupPathA)))
            .thenReturn(groupedJobInitializer);

        groupedJobInitializer = mock(GroupedJobInitializer.class);
        when(groupedJobInitializer.getPoolSize()).thenReturn(2);
        when(groupedJobInitializer.getDefaultPriority()).thenReturn(Thread.NORM_PRIORITY);

        JobGroupPath jobGroupPathAB = new JobGroupPath(Arrays.asList("A", "B"));
        when(this.groupedJobInitializerManager.getGroupedJobInitializer(eq(jobGroupPathAB)))
            .thenReturn(groupedJobInitializer);

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

        // Give enough time for the jobs to be fully taken into ABcount
        waitJobWaiting(jobA1);

        // Give following jobs to JobExecutor (to make sure they are ABtually after since the grouped job executor queue
        // is not "fair")
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

        GroupedJobInitializer groupedJobInitializer = mock(GroupedJobInitializer.class);
        when(groupedJobInitializer.getPoolSize()).thenReturn(2);
        when(groupedJobInitializer.getDefaultPriority()).thenReturn(Thread.NORM_PRIORITY);

        JobGroupPath jobGroupPathA = new JobGroupPath(Collections.singletonList("A"));
        when(this.groupedJobInitializerManager.getGroupedJobInitializer(eq(jobGroupPathA)))
            .thenReturn(groupedJobInitializer);

        groupedJobInitializer = mock(GroupedJobInitializer.class);
        when(groupedJobInitializer.getPoolSize()).thenReturn(2);
        when(groupedJobInitializer.getDefaultPriority()).thenReturn(Thread.NORM_PRIORITY);

        JobGroupPath jobGroupPathAB = new JobGroupPath(Arrays.asList("A", "B"));
        when(this.groupedJobInitializerManager.getGroupedJobInitializer(eq(jobGroupPathAB)))
            .thenReturn(groupedJobInitializer);

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

        ////////////////////
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

    private TestBasicGroupedJob groupedJob(String... path)
    {
        return new TestBasicGroupedJob("type", new JobGroupPath(Arrays.asList(path)), new DefaultRequest());
    }
}
