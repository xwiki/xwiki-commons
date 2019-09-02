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

import org.xwiki.stability.Unstable;

/**
 * Represent a conflict that happened during a merge operation.
 * For more information, see {@link DiffManager#merge(List, List, List, MergeConfiguration)}.
 * @param <E> the type of element used in the merge operation.
 * @version $Id$
 * @since 11.7RC1
 */
@Unstable
public interface Conflict<E>
{
    /**
     * @return the index where the conflict happened during the merge.
     */
    int getIndex();

    /**
     * @return the {@link Delta} of the current version of the merge.
     */
    Delta<E> getDeltaCurrent();

    /**
     * @return the {@link Delta} of the next version of the merge.
     */
    Delta<E> getDeltaNext();

    /**
     * @param delta the delta that might be concerned by this conflict.
     * @return {@code true} if this conflict might impact the given delta.
     * @since 11.8RC1
     */
    default boolean concerns(Delta<E> delta)
    {
        return false;
    }

    /**
     * @param chunk the chunk that might be concerned by this conflict.
     * @return {@code true} if this conflict might impact the given chunk.
     * @since 11.8RC1
     */
    default boolean concerns(Chunk<E> chunk)
    {
        return false;
    }

    /**
     * @return a unique reference for a conflict.
     * @since 11.8RC1
     */
    default String getReference()
    {
        return String.valueOf(this.hashCode());
    }

    /**
     * The max size of a conflict is defined by the max size of the current or next chunk size.
     *
     * @return the conflict max size.
     * @since 11.8RC1
     */
    @Unstable
    default int getMaxSize()
    {
        return Math.max(getDeltaCurrent().getNext().size(), getDeltaNext().getNext().size());
    }
}
