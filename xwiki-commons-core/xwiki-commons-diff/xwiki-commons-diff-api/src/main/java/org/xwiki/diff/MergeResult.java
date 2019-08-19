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
package org.xwiki.diff;

import java.util.Collections;
import java.util.List;

import org.xwiki.logging.LogQueue;
import org.xwiki.stability.Unstable;

/**
 * @param <E> the type of compared elements
 * @version $Id$
 */
public interface MergeResult<E>
{
    /**
     * @return the common ancestor
     */
    List<E> getCommonAncestor();

    /**
     * @return the new version
     */
    List<E> getNext();

    /**
     * @return the current version
     */
    List<E> getCurrent();

    /**
     * @return the log of what append during the merge execution
     */
    LogQueue getLog();

    /**
     * @return the result of the 3 ways merge
     */
    List<E> getMerged();

    /**
     * @return the list of conflicts obtained during the merge
     * @since 11.7RC1
     */
    @Unstable
    default List<Conflict<E>> getConflicts()
    {
        return Collections.emptyList();
    }
}
