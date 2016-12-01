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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.JobStatusStore;
import org.xwiki.job.event.status.JobStatus;

/**
 * Default implementation of {@link JobStatusStorage}.
 *
 * @version $Id$
 * @since 4.0M1
 * @deprecated since 6.1M2, use {@link org.xwiki.job.internal.DefaultJobStatusStore} instead
 */
@Component
@Singleton
@Deprecated
public class DefaultJobStatusStorage implements JobStatusStorage
{
    @Inject
    private JobStatusStore store;

    @Override
    public JobStatus getJobStatus(String id)
    {
        return getJobStatus(id != null ? Arrays.asList(id) : (List<String>) null);
    }

    @Override
    public JobStatus getJobStatus(List<String> id)
    {
        return this.store.getJobStatus(id);
    }

    @Override
    public void store(JobStatus status)
    {
        this.store.store(status);
    }

    @Override
    public void storeAsync(JobStatus status)
    {
        this.store.storeAsync(status);
    }

    @Override
    public JobStatus remove(String id)
    {
        return remove(Arrays.asList(id));
    }

    @Override
    public JobStatus remove(List<String> id)
    {
        JobStatus status = this.store.getJobStatus(id);

        if (status != null) {
            this.store.remove(id);
        }

        return status;
    }
}
