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

/**
 * By default Jobs are either executed asynchronously whenever there is a free thread in the pool. Jobs can implement
 * {@link GroupedJob} to make sure they con't be executed at the same time than the jobs from the same groups.
 *
 * @version $Id$
 * @see GroupedJob
 * @see Job
 * @since 6.1M2
 */
@Role
public interface JobExecutor
{
    /**
     * The current job running in the passed jobs group.
     *
     * @param groupPath the group path
     * @return the currently running job in the passed group
     */
    Job getCurrentJob(JobGroupPath groupPath);

    /**
     * Return job corresponding to the provided id from the current executed or waiting jobs.
     *
     * @param jobId the id of the job
     * @return the job status corresponding to the provided job id, null if none can be found
     */
    Job getJob(List<String> jobId);

    /**
     * Create and add a new job in the queue of jobs to execute.
     *
     * @param jobType the role hint of the job component
     * @param request the request
     * @return the created job
     * @throws JobException error when creating the job
     * @throws java.util.concurrent.RejectedExecutionException if this task cannot be accepted for execution (for
     *             example when the {@link JobExecutor} is disposed).
     */
    Job execute(String jobType, Request request) throws JobException;

    /**
     * Add a new job in the queue of jobs to execute.
     *
     * @param job the job to execute
     * @throws java.util.concurrent.RejectedExecutionException if this task cannot be accepted for execution (for
     *             example when the {@link JobExecutor} is disposed).
     */
    void execute(Job job);
}
