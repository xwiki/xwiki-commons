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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.Request;
import org.xwiki.job.event.status.JobProgress;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.tail.LogTail;

public class TestBasicGroupedJob implements GroupedJob, JobStatus
{
    private final transient ReentrantLock lock = new ReentrantLock();

    private String jobType;

    private Request request;

    private JobGroupPath jobGroupPath;

    private State state;

    private Throwable error;

    private LogQueue logQueue = new LogQueue();

    private Thread thread;

    public TestBasicGroupedJob(String jobType, JobGroupPath jobGroupPath, Request request)
    {
        this.jobType = jobType;
        this.jobGroupPath = jobGroupPath;
        this.request = request;
    }

    @Override
    public String getJobType()
    {
        return this.jobType;
    }

    @Override
    public String getType()
    {
        return "test";
    }

    @Override
    public JobStatus getStatus()
    {
        return this;
    }

    @Override
    public Request getRequest()
    {
        return this.request;
    }

    @Override
    @Deprecated
    public void start(Request request)
    {
        initialize(request);
        run();
    }

    @Override
    public void initialize(Request request)
    {
        this.request = request;
    }

    @Override
    public void join() throws InterruptedException
    {
        // Not implemented
    }

    @Override
    public boolean join(long time, TimeUnit unit) throws InterruptedException
    {
        // Not implemented

        return false;
    }

    @Override
    public void run()
    {
        this.state = State.RUNNING;

        this.thread = Thread.currentThread();

        this.state = State.WAITING;
        lock.lock();
        this.state = State.RUNNING;

        this.state = State.FINISHED;
    }

    @Override
    public JobGroupPath getGroupPath()
    {
        return this.jobGroupPath;
    }

    @Override
    public State getState()
    {
        return this.state;
    }

    @Override
    public Throwable getError()
    {
        return this.error;
    }

    @Override
    public LogQueue getLog()
    {
        return this.logQueue;
    }

    @Override
    public LogTail getLogTail()
    {
        return this.logQueue;
    }

    @Override
    public JobProgress getProgress()
    {
        // Not implemented
        return null;
    }

    @Override
    public void ask(Object question) throws InterruptedException
    {
        // Not implemented
    }

    @Override
    public Object getQuestion()
    {
        // Not implemented
        return null;
    }

    @Override
    public void answered()
    {
        // Not implemented
    }

    @Override
    public Date getStartDate()
    {
        // Not implemented
        return null;
    }

    @Override
    public Date getEndDate()
    {
        // Not implemented
        return null;
    }

    @Override
    public boolean isIsolated()
    {
        // Not implemented
        return true;
    }

    @Override
    public boolean isSerialized()
    {
        // Not implemented
        return true;
    }

    @Override
    @Deprecated
    public List<LogEvent> getLog(LogLevel level)
    {
        return this.logQueue.getLogs(level);
    }

    public Thread getThread()
    {
        return this.thread;
    }

    public void lock()
    {
        this.lock.lock();
    }

    public void unlock()
    {
        this.lock.unlock();
    }
}
