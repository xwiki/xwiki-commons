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
package org.xwiki.diff.internal;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.Delta;

/**
 * Base class used for various types of {@link Delta}s.
 *
 * @param <E> the type of compared elements
 * @version $Id$
 */
public abstract class AbstractDelta<E> implements Delta<E>
{
    /**
     * @see #getType()
     */
    private Type type;

    /**
     * @see #getPrevious()
     */
    private Chunk<E> previous;

    /**
     * @see #getNext()
     */
    private Chunk<E> next;

    /**
     * @param previous the chunk before the modification
     * @param next the chunk after the modification
     * @param type the type of modification applied to the list
     */
    public AbstractDelta(Chunk<E> previous, Chunk<E> next, Type type)
    {
        this.type = type;
        this.previous = previous;
        this.next = next;
    }

    /**
     * @param original the chunk before the modification
     * @param revised the chunk after the modification
     * @param type the type of modification applied to the list
     */
    public AbstractDelta(com.github.difflib.patch.Chunk<E> original, com.github.difflib.patch.Chunk<E> revised,
        Type type)
    {
        this.type = type;
        this.previous = new DefaultChunk<>(original);
        this.next = new DefaultChunk<>(revised);
    }

    @Override
    public Type getType()
    {
        return this.type;
    }

    @Override
    public Chunk<E> getPrevious()
    {
        return this.previous;
    }

    /**
     * @param previous the chunk before the modification
     */
    public void setPrevious(Chunk<E> previous)
    {
        this.previous = previous;
    }

    @Override
    public Chunk<E> getNext()
    {
        return this.next;
    }

    /**
     * @param next the chunk after the modification
     */
    public void setNext(Chunk<E> next)
    {
        this.next = next;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getType());
        builder.append(getPrevious());
        builder.append(getNext());

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Delta) {
            Delta<?> otherDelta = (Delta<?>) obj;

            return getType() == otherDelta.getType() && Objects.equals(getPrevious(), otherDelta.getPrevious())
                && Objects.equals(getNext(), otherDelta.getNext());
        }

        return false;
    }

    @Override
    public String toString()
    {
        return getType().name() + "[-" + getPrevious() + " +" + getNext() + "]";
    }
}
