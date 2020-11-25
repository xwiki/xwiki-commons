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
 * A snippet of the source list.
 *
 * @param <E> the type of compared elements
 * @version $Id$
 */
public interface Chunk<E>
{
    /**
     * Verify that the chunk can be found in the provided list.
     *
     * @param target the list where to match the chunk
     * @throws PatchException fail to match the chunk
     */
    void verify(List<E> target) throws PatchException;

    /**
     * @return the elements in the snippet
     */
    List<E> getElements();

    /**
     * @param elements the elements in the snippet
     */
    void setElements(List<E> elements);

    /**
     * @return the size of the snippet
     */
    int size();

    /**
     * @return the start index of the chunk in the list
     */
    int getIndex();

    /**
     * @return the index of the last element of the chunk in the list
     */
    int getLastIndex();

    /**
     * Check if the current chunk is overlapping with another chunk based on the indexes.
     * @param otherChunk the other chunk to test.
     * @return true if both chunks shares some indexes.
     * @since 11.7RC1
     * @since 10.11.10
     * @since 11.3.3
     */
    default boolean isOverlappingWith(Chunk<E> otherChunk)
    {
        return false;
    }
}
