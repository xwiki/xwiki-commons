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

import java.io.IOException;
import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.tail.LoggerTail;

/**
 * Persistent storage for job statuses. Internal role that is used by {@link DefaultPersistentJobStatusStore}.
 *
 * @version $Id$
 * @since 18.1.0RC1
 */
@Role
public interface PersistentJobStatusStore
{
    /**
     * Save the job status to the persistent storage.
     * <p>This method must be called with the write lock acquired.</p>
     * @param status the job status to save
     * @throws IOException when failing to save the job status
     */
    void saveJobStatusWithLock(JobStatus status) throws IOException;

    /**
     * Load the job status from the persistent storage.
     * <p>This method must be called with the read lock acquired.</p>
     * @param id the id of the job
     * @return the job status
     * @throws IOException when failing to load the job status
     */
    JobStatus loadJobStatusWithLock(List<String> id) throws IOException;

    /**
     * Remove the job status from the persistent storage.
     * <p>This method must be called with the write lock acquired.</p>
     * @param id the id of the job
     * @throws IOException when failing to remove the job status
     */
    void removeJobStatusWithLock(List<String> id) throws IOException;

    /**
     * @param jobId the identifier of the job
     * @param readonly true of the log is readonly
     * @return the {@link LoggerTail} instance
     */
    LoggerTail createLoggerTail(List<String> jobId, boolean readonly);
}
