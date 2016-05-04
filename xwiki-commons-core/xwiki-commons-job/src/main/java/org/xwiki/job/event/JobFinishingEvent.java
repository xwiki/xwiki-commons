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

package org.xwiki.job.event;

import java.util.Arrays;
import java.util.List;

import org.xwiki.job.Request;

/**
 * Job finishing event triggered just after a job is done and we start to save status etc. Give a chance to listeners to
 * do action as part of the job execution before it's "closed". Additional data may contains an exception if the job has
 * not been finished with success.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the related {@link org.xwiki.job.Job} instance</li>
 * <li>data: an {@link Throwable} if the job execution failed or null otherwise</li>
 * </ul>
 *
 * @version $Id$
 * @since 8.1RC1
 */
public class JobFinishingEvent extends AbstractJobEvent
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public JobFinishingEvent()
    {

    }

    /**
     * @param jobId the event related job id
     */
    public JobFinishingEvent(String jobId)
    {
        super(jobId);
    }

    /**
     * @param jobId the event related job id
     * @param jobType the event related job type
     * @param request the event related job request
     */
    public JobFinishingEvent(String jobId, String jobType, Request request)
    {
        this(Arrays.asList(jobId), jobType, request);
    }

    /**
     * @param jobId the event related job id
     * @param jobType the event related job type
     * @param request the event related job request
     * @since 4.1M2
     */
    public JobFinishingEvent(List<String> jobId, String jobType, Request request)
    {
        super(jobId, jobType, request);
    }

}
