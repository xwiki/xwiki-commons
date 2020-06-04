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

import org.xwiki.job.annotation.Serializable;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.observation.ObservationManager;

/**
 * Default implementation of {@link org.xwiki.job.event.status.JobStatus}.
 *
 * @param <R> the request type associated to the job
 * @version $Id$
 * @since 7.4M1
 */
@Serializable
public class DefaultJobStatus<R extends Request> extends AbstractJobStatus<R>
{
    /**
     * @param request the request provided when started the job
     * @param parentJobStatus the status of the parent job (i.e. the status of the job that started this one); pass
     *            {@code null} if this job hasn't been started by another job (i.e. if this is not a sub-job)
     * @param observationManager the observation manager component
     * @param loggerManager the logger manager component
     * @deprecated since 9.2RC1, use
     *             {@link #DefaultJobStatus(String, Request, JobStatus, ObservationManager, LoggerManager)} instead
     */
    @Deprecated
    public DefaultJobStatus(R request, JobStatus parentJobStatus, ObservationManager observationManager,
        LoggerManager loggerManager)
    {
        super(request, parentJobStatus, observationManager, loggerManager);
    }

    /**
     * @param jobType the type of the job
     * @param request the request provided when started the job
     * @param parentJobStatus the status of the parent job (i.e. the status of the job that started this one); pass
     *            {@code null} if this job hasn't been started by another job (i.e. if this is not a sub-job)
     * @param observationManager the observation manager component
     * @param loggerManager the logger manager component
     */
    public DefaultJobStatus(String jobType, R request, JobStatus parentJobStatus, ObservationManager observationManager,
        LoggerManager loggerManager)
    {
        super(jobType, request, parentJobStatus, observationManager, loggerManager);
    }
}
