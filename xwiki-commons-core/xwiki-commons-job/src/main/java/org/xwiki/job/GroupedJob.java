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

/**
 * A Job which is part of a group of jobs. Grouped jobs are executed synchronously usually in the same thread.
 * <p>
 * The {@link GroupedJob} return a group hierarchy interpreted as follow:
 * <ul>
 * <li>a {@link GroupedJob} from group ["group", "subgroup"] won't be executed at the same time of another
 * {@link GroupedJob} from group ["group", "subgroup"]
 * <li>a {@link GroupedJob} from group ["group", "subgroup1"] can be executed at the same time of a {@link GroupedJob}
 * from group ["group", "subgroup2"]
 * <li>a {@link GroupedJob} from group ["group", "subgroup"] won't be executed at the same time of a {@link GroupedJob}
 * from group ["group"]
 * </ul>
 *
 * @version $Id$
 * @since 6.1M2
 */
public interface GroupedJob extends Job
{
    /**
     * @return the group hierarchy of the job. If null the job won't be grouped.
     */
    JobGroupPath getGroupPath();
}
