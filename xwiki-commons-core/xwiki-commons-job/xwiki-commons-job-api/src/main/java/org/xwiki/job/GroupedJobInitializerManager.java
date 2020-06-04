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

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * A component dedicated to find the appropriate {@link GroupedJobInitializer} based on a {@link JobGroupPath}.
 *
 * @since 12.5RC1
 * @version $Id$
 */
@Unstable
@Role
public interface GroupedJobInitializerManager
{
    /**
     * Retrieve a matching {@link GroupedJobInitializer} for the given {@link JobGroupPath}.
     * If no exact match can be find, this methods will check the parent paths to find an appropriate initializer.
     *
     * @param jobGroupPath the patch for which to find a matching initializer.
     * @return a matching initializer or a default one if none can be find.
     */
    GroupedJobInitializer getGroupedJobInitializer(JobGroupPath jobGroupPath);

    /**
     * Invalidate the cache of {@link GroupedJobInitializer}.
     */
    void invalidateCache();
}
