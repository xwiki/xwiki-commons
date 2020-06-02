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
 * An initializer that provides the information to create the appropriate executor for a given {@link GroupedJob}.
 * Note that the initializer is retrieved by the executor manager by looking on the {@link #getId()}: the hint of the
 * component is never used to retrieve it. Moreover initializers are retrieved by looking on the hierarchy of the
 * {@link JobGroupPath}, so if no initializer is found with the exact path, the closest one matching the path hierarchy
 * will be used.
 *
 * @since 12.5RC1
 * @version $Id$
 */
@Unstable
@Role
public interface GroupedJobInitializer
{
    /**
     * @return the actual {@link JobGroupPath} that this initializer should be used for.
     */
    JobGroupPath getId();

    /**
     * @return the size of the pool of threads to be used to execute the {@link GroupedJob}.
     */
    int getPoolSize();

    /**
     * @return the default priority the threads should use.
     */
    int getDefaultPriority();
}
