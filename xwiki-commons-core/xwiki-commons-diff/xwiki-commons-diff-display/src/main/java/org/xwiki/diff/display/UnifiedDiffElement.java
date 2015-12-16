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
 * Wraps the elements that are compared to produce a diff, holding information like their index and type of change
 * (added, removed, unmodified) to simplify the process of displaying them in an unified diff. If the wrapped element is
 * a composite element (can be split in sub-elements) and was modified (replaced by another element) then this class can
 * also store information about changes at the level of sub-elements.
 *
 * @param <E> the type of elements that are compared to produce the first-level diff
 * @param <F> the type of sub-elements that are compared to produce the second-level diff
 * @version $Id$
 * @since 4.1RC1
 */
public class UnifiedDiffElement<E, F>
{
    /**
     * The possible types of elements you can find within a unified diff.
     */
    public enum Type
    {
        /** An element that was added. */
        ADDED('+'),

        /** An element that was removed. */
        DELETED('-'),

        /** An element that was left unmodified. This type of elements show the context where a change was made. */
        CONTEXT(' ');

        /**
         * The symbol associated with this element type. This is need only for the default string serialization.
         *
         * @see UnifiedDiffElement#toString()
         */
        private final char symbol;

        /**
         * Creates a new element type that has the given symbol associated.
         *
         * @param symbol the symbol associated with this element type
         */
        Type(char symbol)
        {
            this.symbol = symbol;
        }

        /**
         * @return the symbol associated with this element type
         */
        public char getSymbol()
        {
            return this.symbol;
        }
    }

    /**
     * The element index.
     */
    private final int index;

    /**
     * The element type.
     */
    private final Type type;

    /**
     * The wrapped element.
     */
    private final E value;

    /**
     * The list of chunks of sub-elements that form this element.
     */
    private List<InlineDiffChunk<F>> chunks;

    /**
     * Creates a new element in a unified diff.
     *
     * @param index the element index
     * @param type the element type
     * @param value the wrapped element
     */
    public UnifiedDiffElement(int index, Type type, E value)
    {
        this.index = index;
        this.type = type;
        this.value = value;
    }

    /**
     * @return the element index
     */
    public int getIndex()
    {
        return this.index;
    }

    /**
     * @return the element type
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * @return the wrapped element
     */
    public E getValue()
    {
        return this.value;
    }

    /**
     * @return the list of chunks of sub-elements that form this element
     */
    public List<InlineDiffChunk<F>> getChunks()
    {
        return this.chunks;
    }

    /**
     * Sets the list of chunks of sub-elements that form this element.
     *
     * @param chunks the list of chunks
     */
    public void setChunks(List<InlineDiffChunk<F>> chunks)
    {
        this.chunks = chunks;
    }

    /**
     * @return {@code true} if this element was added, {@code false} otherwise
     */
    public boolean isAdded()
    {
        return this.type == Type.ADDED;
    }

    /**
     * @return {@code true} if this element was deleted, {@code false} otherwise
     */
    public boolean isDeleted()
    {
        return this.type == Type.DELETED;
    }

    @Override
    public String toString()
    {
        return this.type.getSymbol() + String.valueOf(this.value) + '\n';
    }
}
