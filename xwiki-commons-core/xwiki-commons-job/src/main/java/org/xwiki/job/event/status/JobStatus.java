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
package org.xwiki.job.event.status;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.xwiki.job.Request;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.event.LogEvent;

/**
 * Describe the current status of a job.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface JobStatus
{
    /**
     * Job status.
     *
     * @version $Id$
     */
    enum State
    {
        /**
         * Default status, generally mean that the task has not been started yet.
         */
        NONE,

        /**
         * The job is running.
         */
        RUNNING,

        /**
         * The job is waiting.
         */
        WAITING,

        /**
         * The job is done.
         */
        FINISHED
    }

    /**
     * @return the general state of the job
     */
    State getState();

    /**
     * @return the {@link Throwable} on which the job stopped
     * @since 8.1RC1
     */
    default Throwable getError()
    {
        return null;
    }

    /**
     * @return the job request provided when starting it
     */
    Request getRequest();

    /**
     * @return the log sent during job execution
     */
    LogQueue getLog();

    /**
     * @return progress information about the job (percent, etc.)
     */
    JobProgress getProgress();

    /**
     * @param question the question to ask as a Java bean
     * @throws InterruptedException if the current thread is interrupted (and interruption of thread suspension is
     *             supported)
     * @since 4.0M2
     */
    void ask(Object question) throws InterruptedException;

    /**
     * @param question the question to ask as a Java bean
     * @param time the maximum time to wait
     * @param unit the time unit of the {@code time} argument
     * @return {@code false} if the waiting time detectably elapsed before return from the method, else {@code true}
     * @throws InterruptedException if the current thread is interrupted (and interruption of thread suspension is
     *             supported)
     * @since 9.1RC1
     */
    default boolean ask(Object question, long time, TimeUnit unit) throws InterruptedException
    {
        ask(question);

        return true;
    }

    /**
     * @return the question
     * @since 4.0M2
     */
    Object getQuestion();

    /**
     * Indicate that the question has been answered.
     *
     * @since 4.0M2
     */
    void answered();

    /**
     * @return the date and time when the job has been started
     */
    Date getStartDate();

    /**
     * @return the date and time when the job finished
     */
    Date getEndDate();

    // Deprecated

    /**
     * @param level the level of the log
     * @return the log sent with the provided level
     * @deprecated since 4.1RC1 use {@link LogQueue#getLogs(LogLevel)} instead
     */
    @Deprecated
    List<LogEvent> getLog(LogLevel level);
}
