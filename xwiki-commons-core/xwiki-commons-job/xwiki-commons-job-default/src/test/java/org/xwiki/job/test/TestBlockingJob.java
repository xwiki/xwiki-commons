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
package org.xwiki.job.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;

/**
 * A job which blocks until {@link #unblock()} is called or its thread is interrupted, or until it's canceled when
 * {@link #setStopOnCancel(boolean)} is enabled. It's a single job unless a group path is set.
 *
 * @version $Id$
 */
@Component
@Named(TestBlockingJob.TYPE)
public class TestBlockingJob extends AbstractJob<DefaultRequest, DefaultJobStatus<DefaultRequest>> implements GroupedJob
{
    public static final String TYPE = "blocking";

    private final CountDownLatch latch = new CountDownLatch(1);

    private JobGroupPath groupPath;

    private volatile boolean stopOnCancel;

    @Override
    public String getType()
    {
        return TYPE;
    }

    @Override
    public JobGroupPath getGroupPath()
    {
        return this.groupPath;
    }

    /**
     * @param groupPath the group of the job, null for a single job
     */
    public void setGroupPath(JobGroupPath groupPath)
    {
        this.groupPath = groupPath;
    }

    /**
     * @param stopOnCancel true if the job should stop (without error) when its status is canceled
     */
    public void setStopOnCancel(boolean stopOnCancel)
    {
        this.stopOnCancel = stopOnCancel;
    }

    /**
     * Let the job finish.
     */
    public void unblock()
    {
        this.latch.countDown();
    }

    @Override
    protected void runInternal() throws Exception
    {
        // Check the cancel regularly, like the jobs which stop at their next checkpoint when they are canceled
        while (!this.latch.await(10, TimeUnit.MILLISECONDS)) {
            if (this.stopOnCancel && getStatus().isCanceled()) {
                return;
            }
        }
    }
}
