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

import org.xwiki.job.Request;
import org.xwiki.job.annotation.Serializable;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.observation.ObservationManager;

/**
 * Default implementation of {@link org.xwiki.job.event.status.JobStatus}.
 *
 * @param <R> the request type associated to the job
 * @version $Id$
 * @since 4.0M1
 * @deprecated since 7.4M1, use {@link org.xwiki.job.DefaultJobStatus} instead
 */
@Serializable
@Deprecated
public class DefaultJobStatus<R extends Request> extends AbstractJobStatus<R>
{
    /**
     * @param request the request provided when started the job
     * @param observationManager the observation manager component
     * @param loggerManager the logger manager component
     * @param parentJobStatus the status of the parent job (i.e. the status of the job that started this one); pass
     *            {@code null} if this job hasn't been started by another job (i.e. if this is not a sub-job)
     */
    public DefaultJobStatus(R request, ObservationManager observationManager, LoggerManager loggerManager,
        JobStatus parentJobStatus)
    {
        super(request, observationManager, loggerManager, parentJobStatus);
    }
}
