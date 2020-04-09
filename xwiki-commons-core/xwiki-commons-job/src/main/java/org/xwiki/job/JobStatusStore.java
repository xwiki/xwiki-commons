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
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.tail.LoggerTail;

/**
 * Store and retrieve {@link JobStatus} instances.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Role
public interface JobStatusStore
{
    /**
     * @param id the id of the job
     * @return the job status or null if there's no matching job id
     */
    JobStatus getJobStatus(List<String> id);

    /**
     * @param status the job status
     */
    void store(JobStatus status);

    /**
     * @param status the job status
     */
    void storeAsync(JobStatus status);

    /**
     * @param id the id of the job
     */
    void remove(List<String> id);

    /**
     * @param jobId the identifier of the job
     * @param readonly true of the log is readonly
     * @return the {@link LoggerTail} instance
     * @since 11.9RC1
     */
    default LoggerTail createLoggerTail(List<String> jobId, boolean readonly)
    {
        return new LogQueue();
    }
}
