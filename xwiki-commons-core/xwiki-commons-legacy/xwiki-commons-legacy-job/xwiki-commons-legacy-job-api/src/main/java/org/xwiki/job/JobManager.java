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

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.job.event.status.JobStatus;

/**
 * Proxy used to simplify execution of jobs.
 *
 * @version $Id$
 * @since 4.0M1
 * @deprecated since 6.1M2, use {@link JobExecutor} instead
 */
@Role
@Deprecated
public interface JobManager
{
    /**
     * @return the job currently running or the latest job, null if there is no job
     */
    Job getCurrentJob();

    /**
     * Return job status corresponding to the provided id from the current executed job or stored history.
     *
     * @param id the id of the job
     * @return the job status corresponding to the provided job id, null if none can be found
     */
    JobStatus getJobStatus(String id);

    /**
     * Return job status corresponding to the provided id from the current executed job or stored history.
     *
     * @param id the id of the job
     * @return the job status corresponding to the provided job id, null if none can be found
     * @since 4.1M2
     */
    JobStatus getJobStatus(List<String> id);

    /**
     * Start a new job with the provided identifier and wait until its finished.
     *
     * @param jobType the role hint of the job component
     * @param request the request
     * @return the created job
     * @throws JobException error when creating the job
     */
    Job executeJob(String jobType, Request request) throws JobException;

    /**
     * Add a new job in the queue of jobs to execute.
     *
     * @param jobType the role hint of the job component
     * @param request the request
     * @return the created job
     * @throws JobException error when creating the job
     */
    Job addJob(String jobType, Request request) throws JobException;

    /**
     * Add a new job in the queue of jobs to execute.
     *
     * @param job the job to execute
     * @since 5.1M2
     */
    void addJob(Job job);
}
