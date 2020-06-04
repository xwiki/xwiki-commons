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
package org.xwiki.job;

import java.util.concurrent.TimeUnit;

import org.xwiki.component.annotation.Role;
import org.xwiki.job.event.status.JobStatus;

/**
 * A Job produced from a {@link Request} and exposing a {@link JobStatus}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Role
public interface Job extends Runnable
{
    /**
     * @return the type of the job
     */
    String getType();

    /**
     * @return the status of the job
     */
    JobStatus getStatus();

    /**
     * @return the job request
     */
    Request getRequest();

    /**
     * @param request start the job with provided request
     * @deprecated since 5.1M2 use {@link #initialize(Request)} then {@link #run()} instead
     */
    @Deprecated
    void start(Request request);

    /**
     * @param request configure the job
     * @since 5.1M1
     */
    void initialize(Request request);

    /**
     * Causes the current thread to wait until this job has FINSHED state.
     *
     * @throws InterruptedException if any thread has interrupted the current thread. The <i>interrupted status</i> of
     *             the current thread is cleared when this exception is thrown.
     */
    void join() throws InterruptedException;

    /**
     * Causes the current thread to wait until this job has FINSHED state.
     *
     * @param time the maximum time to wait
     * @param unit the time unit of the {@code time} argument
     * @return {@code false} if the waiting time detectably elapsed before return from the method, else {@code true}
     * @throws InterruptedException if the current thread is interrupted (and interruption of thread suspension is
     *             supported)
     */
    boolean join(long time, TimeUnit unit) throws InterruptedException;
}
