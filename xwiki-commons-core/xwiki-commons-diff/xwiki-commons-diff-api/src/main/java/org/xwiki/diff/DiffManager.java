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

import java.util.List;

import org.xwiki.component.annotation.Role;

/**
 * Various diff/merge tools.
 * 
 * @version $Id$
 */
@Role
public interface DiffManager
{
    /**
     * Produce a diff between the two provided versions.
     * 
     * @param <E> the type of compared elements
     * @param previous the previous version of the content to compare
     * @param next the next version of the content to compare
     * @param configuration the configuration of the diff behavior
     * @return the result of the diff
     * @throws DiffException error when executing the diff
     */
    <E> DiffResult<E> diff(List<E> previous, List<E> next, DiffConfiguration<E> configuration) throws DiffException;

    /**
     * Execute a 3-way merge on provided versions.
     * 
     * @param <E> the type of compared elements
     * @param commonAncestor the common ancestor of the two versions of the content to compare
     * @param next the next version of the content to compare
     * @param current the current version of the content to compare
     * @param configuration the configuration of the merge behavior
     * @return the result of the merge
     * @throws MergeException error when executing the merge
     */
    <E> MergeResult<E> merge(List<E> commonAncestor, List<E> next, List<E> current, MergeConfiguration<E> configuration)
        throws MergeException;
}
