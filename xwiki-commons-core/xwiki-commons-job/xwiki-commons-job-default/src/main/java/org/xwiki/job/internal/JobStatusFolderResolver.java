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

import java.io.File;
import java.util.List;

import org.xwiki.component.annotation.Role;

/**
 * Get the job folder for the job status and logs. The implementation with the highest priority, i.e., lowest
 * {@link javax.annotation.Priority} value, is used to actually store a job status. If a job status isn't found at
 * this location, all other implementations are tried in priority order, the first found status is moved to the
 * location indicated by the first implementation.
 *
 * @version $Id$
 * @since 17.2.0RC1
 * @since 16.10.6
 */
@Role
public interface JobStatusFolderResolver
{
    /**
     * @param jobID the ID of the job for which the folder shall be retrieved
     * @return the folder where the job status and log should be stored according to the resolver
     */
    File getFolder(List<String> jobID);

    /**
     * @param jobID the ID of the job for which the segments of the folder path shall be retrieved
     * @return the segments of the folder path for the job status and log according to the resolver, starting from
     * the configured storage directory
     * @since 18.1.0RC1
     */
    List<String> getFolderSegments(List<String> jobID);
}
