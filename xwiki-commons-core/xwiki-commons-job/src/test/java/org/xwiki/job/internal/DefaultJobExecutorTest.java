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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.event.status.JobStatus.State;
import org.xwiki.job.test.TestBasicGroupedJob;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Validate {@link DefaultJobExecutor};
 * 
 * @version $Id$
 */
@ComponentList(ContextComponentManagerProvider.class)
public class DefaultJobExecutorTest
{
    @Rule
    public MockitoComponentMockingRule<JobExecutor> mocker =
        new MockitoComponentMockingRule<JobExecutor>(DefaultJobExecutor.class);

    @Before
    public void before() throws Exception
    {
    }

    private TestBasicGroupedJob groupedJob(String... path)
    {
        return new TestBasicGroupedJob("type", new JobGroupPath(Arrays.asList(path)), new DefaultRequest());
    }

    // Tests

    @Test
    public void testMatchingGroupPathAreBlocked() throws ComponentLookupException, InterruptedException
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

        // Give all jobs to JobExecutor
        this.mocker.getComponentUnderTest().execute(jobA);
        // Give enough time for the job to be fully taken into account
        Thread.sleep(10);

        this.mocker.getComponentUnderTest().execute(jobAB);
        // Give enough time for the job to be fully taken into account
        Thread.sleep(10);

        this.mocker.getComponentUnderTest().execute(job12);
        // Give enough time for the job to be fully taken into account
        Thread.sleep(10);

        this.mocker.getComponentUnderTest().execute(job1);
        // Give enough time for the job to be fully taken into account
        Thread.sleep(10);

        ////////////////////
        // A and A/B

        assertSame(State.WAITING, jobA.getStatus().getState());
        assertNull(jobAB.getStatus().getState());

        // Next job
        jobA.unlock();
        Thread.sleep(10);

        assertSame(State.FINISHED, jobA.getStatus().getState());
        assertSame(State.WAITING, jobAB.getStatus().getState());

        // Next job
        jobAB.unlock();
        Thread.sleep(10);

        assertSame(State.FINISHED, jobA.getStatus().getState());
        assertSame(State.FINISHED, jobAB.getStatus().getState());

        ////////////////////
        // 1/2 and 1

        assertSame(State.WAITING, job12.getStatus().getState());
        assertNull(job1.getStatus().getState());

        // Next job
        job12.unlock();
        Thread.sleep(10);

        assertSame(State.FINISHED, job12.getStatus().getState());
        assertSame(State.WAITING, job1.getStatus().getState());

        // Next job
        job1.unlock();
        Thread.sleep(10);

        assertSame(State.FINISHED, job1.getStatus().getState());
        assertSame(State.FINISHED, job1.getStatus().getState());
    }
}
