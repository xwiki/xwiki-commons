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

import java.io.File;

import org.xwiki.component.annotation.Role;

/**
 * Some job manager related configuration.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Role
public interface JobManagerConfiguration
{
    /**
     * @return the folder containing job executing status
     */
    File getStorage();

    /**
     * @return the maximum number of entries to put in the job status cache
     * @since 7.2M2
     */
    int getJobStatusCacheSize();

    /**
     * @return the number of {@link GroupedJobInitializer} to keep in cache.
     * @since 12.5RC1
     */
    default int getGroupedJobInitializerCacheSize()
    {
        return 100;
    }

    /**
     * @return the duration in milliseconds for the thread keep alive in our single job thread executor.
     * @see java.util.concurrent.ThreadPoolExecutor#setKeepAliveTime(long, java.util.concurrent.TimeUnit)
     * @since 12.5RC1
     */
    default long getSingleJobThreadKeepAliveTime()
    {
        return 60000L;
    }

    /**
     * @return the duration in milliseconds for the thread keep alive in our grouped job thread executor.
     * @see java.util.concurrent.ThreadPoolExecutor#setKeepAliveTime(long, java.util.concurrent.TimeUnit)
     * @since 12.5RC1
     */
    default long getGroupedJobThreadKeepAliveTime()
    {
        return 60000L;
    }
}
