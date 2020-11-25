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

/**
 * A delta between two version of a list.
 *
 * @param <E> the type of compared elements
 * @version $Id$
 */
public interface Delta<E>
{
    /**
     * The kind of modification to apply on the list.
     *
     * @version $Id$
     */
    enum Type
    {
        /**
         * Change one or several following elements.
         */
        CHANGE,

        /**
         * Deleted one or several following elements.
         */
        DELETE,

        /**
         * Insert one or several following elements.
         */
        INSERT
    }

    /**
     * Try to apply the delta on the provided list.
     *
     * @param target the list to modify
     * @throws PatchException if the delta cannot be applied
     */
    void verify(List<E> target) throws PatchException;

    /**
     * Apply the delta on the provided list.
     *
     * @param target the list to modify
     * @throws PatchException if the delta cannot be applied
     */
    void apply(List<E> target) throws PatchException;

    /**
     * Apply the an inverted version of the delta on the provided list.
     *
     * @param target the list to modify
     * @throws PatchException if the delta cannot be applied
     */
    void restore(List<E> target) throws PatchException;

    /**
     * @return the type of modification applied to the list
     */
    Type getType();

    /**
     * @return the chunk before the modification
     */
    Chunk<E> getPrevious();

    /**
     * @return the chunk after the modification
     */
    Chunk<E> getNext();

    /**
     * @return the max size between the two chunks of the delta.
     * @since 11.8RC1
     */
    default int getMaxChunkSize()
    {
        return Math.max(getNext().size(), getPrevious().size());
    }
}
