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
package org.xwiki.diff.display;

import java.util.List;

/**
 * A group of consecutive elements that are targeted by the same operation (add, remove, keep) in an in-line diff.
 * 
 * @param <E> the type of elements that form a chunk
 * @version $Id$
 * @since 4.1RC1
 */
public class InlineDiffChunk<E>
{
    /**
     * The possible types of chunks you can find within an in-line diff.
     */
    public static enum Type
    {
        /** A chunk that has been added. All the elements that form this chunk have been added. */
        ADDED,

        /** A chunk that has been removed. All the elements that form this chunk have been added. */
        DELETED,

        /**
         * A chunk that stays unmodified. The elements of this chunk are neither added nor removed. They indicate the
         * context where a change has been made.
         */
        UNMODIFIED;
    }

    /**
     * The chunk type. This specified the operation (add, remove, keep) that targets all the chunk elements.
     */
    private final Type type;

    /**
     * The list of elements that form this chunk.
     */
    private final List<E> elements;

    /**
     * Creates a new chunk with the specified type and elements.
     * 
     * @param type the chunk type
     * @param elements the list of elements that form the chunk
     */
    public InlineDiffChunk(Type type, List<E> elements)
    {
        this.type = type;
        this.elements = elements;
    }

    /**
     * @return the chunk type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * @return the list of element that form this chunk
     */
    public List<E> getElements()
    {
        return elements;
    }

    /**
     * @return {@code true} if this chunk was added, {@code false} otherwise
     */
    public boolean isAdded()
    {
        return type == Type.ADDED;
    }

    /**
     * @return {@code true} if this chunk was deleted, {@code false} otherwise
     */
    public boolean isDeleted()
    {
        return type == Type.DELETED;
    }

    /**
     * @return {@code true} if this chunk was left unmodified, {@code false} otherwise
     */
    public boolean isUnmodified()
    {
        return type == Type.UNMODIFIED;
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder(elements.size());
        for (E element : elements) {
            stringBuilder.append(element);
        }
        return stringBuilder.toString();
    }
}
