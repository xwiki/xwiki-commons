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

import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.xwiki.job.Request;
import org.xwiki.job.event.status.JobProgress;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.event.LogQueueListener;
import org.xwiki.observation.ObservationManager;

/**
 * Base implementation of {@link JobStatus}.
 * 
 * @param <R>
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractJobStatus<R extends Request> implements JobStatus
{
    /**
     * Used register itself to receive logging and progress related events.
     */
    private transient ObservationManager observationManager;

    /**
     * Used to isolate job related log.
     */
    private transient LoggerManager loggerManager;

    /**
     * General state of the job.
     */
    private State state = State.NONE;

    /**
     * Request provided when starting the job.
     */
    private R request;

    /**
     * Log sent during job execution.
     */
    private LogQueue logs = new LogQueue();

    /**
     * @see #getStartDate()
     */
    private Date startDate;

    /**
     * @see #getEndDate()
     */
    private Date endDate;

    /**
     * Used to lock #ask().
     */
    private final transient ReentrantLock askLock = new ReentrantLock();

    /**
     * Condition for waiting answer.
     */
    private final transient Condition answered = this.askLock.newCondition();

    /**
     * The question.
     */
    private transient volatile Object question;

    /**
     * Take care of progress related events to produce a progression information usually used in a progress bar.
     */
    private transient DefaultJobProgress progress;

    /**
     * @param request the request provided when started the job
     * @param observationManager the observation manager component
     * @param loggerManager the logger manager component
     */
    public AbstractJobStatus(R request, ObservationManager observationManager, LoggerManager loggerManager)
    {
        this.request = request;
        this.observationManager = observationManager;
        this.loggerManager = loggerManager;
    }

    /**
     * Start listening to events.
     */
    public void startListening()
    {
        // Register progress listener
        this.progress = new DefaultJobProgress(Thread.currentThread());
        this.observationManager.addListener(this.progress);

        // Isolate log for the job status
        this.loggerManager.pushLogListener(new LogQueueListener(LogQueueListener.class.getName() + '_' + hashCode(),
            this.logs));
    }

    /**
     * Stop listening to events.
     */
    public void stopListening()
    {
        this.loggerManager.popLogListener();
        this.observationManager.removeListener(this.progress.getName());
    }

    // JobStatus

    @Override
    public State getState()
    {
        return this.state;
    }

    /**
     * @param state the general state of the job
     */
    public void setState(State state)
    {
        this.state = state;
    }

    @Override
    public R getRequest()
    {
        return this.request;
    }

    @Override
    public LogQueue getLog()
    {
        return this.logs;
    }

    @Override
    public JobProgress getProgress()
    {
        return this.progress;
    }

    @Override
    public void ask(Object question) throws InterruptedException
    {
        this.question = question;

        this.askLock.lockInterruptibly();

        try {
            // Wait for the answer
            this.state = State.WAITING;
            this.answered.await();
            this.state = State.RUNNING;
        } finally {
            this.askLock.unlock();
        }
    }

    @Override
    public Object getQuestion()
    {
        return this.question;
    }

    @Override
    public void answered()
    {
        this.askLock.lock();

        this.question = null;

        try {
            this.answered.signal();
        } finally {
            this.askLock.unlock();
        }
    }

    @Override
    public Date getStartDate()
    {
        return this.startDate;
    }

    /**
     * @param startDate the date and time when the job has been started
     */
    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate()
    {
        return this.endDate;
    }

    /**
     * @param endDate the date and time when the job finished
     */
    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    // Deprecated

    @Override
    @Deprecated
    public List<LogEvent> getLog(LogLevel level)
    {
        return this.logs.getLogs(level);
    }
}
