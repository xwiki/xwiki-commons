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

import org.junit.jupiter.api.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.Job;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.test.TestBasicGroupedJob;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validate {@link DefaultJobExecutor};
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList(ContextComponentManagerProvider.class)
public class DefaultJobExecutorTest
{
    @InjectMockComponents
    private DefaultJobExecutor executor;

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
        } while (wait < 100);

        fail("Job never reached expected state [" + expected + "]. Still [" + job.getStatus().getState()
            + "] after 100 milliseconds");
    }

    @Test
    void matchingGroupPathAreBlocked()
    {
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

    private TestBasicGroupedJob groupedJob(String... path)
    {
        return new TestBasicGroupedJob("type", new JobGroupPath(Arrays.asList(path)), new DefaultRequest());
    }
}
