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
import org.xwiki.observation.event.EndEvent;

/**
 * Job finished event triggered when a job is finished. Additional data may contains an exception if the job has not
 * been finished with success.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the related {@link org.xwiki.job.Job} instance</li>
 * <li>data: an {@link Throwable} if the job execution failed or null otherwise</li>
 * </ul>
 *
 * @version $Id$
 * @since 4.0M1
 */
public class JobFinishedEvent extends AbstractJobEvent implements EndEvent
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public JobFinishedEvent()
    {

    }

    /**
     * @param jobId the event related job id
     */
    public JobFinishedEvent(String jobId)
    {
        super(jobId);
    }

    /**
     * @param jobId the event related job id
     * @param jobType the event related job type
     * @param request the event related job request
     */
    public JobFinishedEvent(String jobId, String jobType, Request request)
    {
        this(Arrays.asList(jobId), jobType, request);
    }

    /**
     * @param jobId the event related job id
     * @param jobType the event related job type
     * @param request the event related job request
     * @since 4.1M2
     */
    public JobFinishedEvent(List<String> jobId, String jobType, Request request)
    {
        super(jobId, jobType, request);
    }

}
