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

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.PatchException;

/**
 * Default implementation of {@link Chunk}.
 * 
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class DefaultChunk<E> implements Chunk<E>
{
    /**
     * @see #getIndex()
     */
    private final int index;

    /**
     * @see #getElements()
     */
    private List<E> elements;

    /**
     * @param index the index where to find the provided snippet of elements in the source list
     * @param elements the snippet of elements
     */
    public DefaultChunk(int index, List<E> elements)
    {
        this.index = index;
        this.elements = elements;
    }

    /**
     * @param chunk the chunk to convert
     */
    public DefaultChunk(difflib.Chunk chunk)
    {
        this(chunk.getPosition(), (List<E>) chunk.getLines());
    }

    @Override
    public void verify(List<E> target) throws PatchException
    {
        if (getLastIndex() > target.size()) {
            throw new PatchException("Incorrect Chunk: the position of chunk > target size");
        }

        for (int i = 0; i < size(); i++) {
            if (!target.get(index + i).equals(elements.get(i))) {
                throw new PatchException("Incorrect Chunk: the chunk content doesn't match the target");
            }
        }
    }

    @Override
    public int getIndex()
    {
        return this.index;
    }

    @Override
    public void setElements(List<E> lines)
    {
        this.elements = lines;
    }

    @Override
    public List<E> getElements()
    {
        return this.elements;
    }

    @Override
    public int size()
    {
        return this.elements.size();
    }

    @Override
    public int getLastIndex()
    {
        return getIndex() + size() - 1;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getElements());
        builder.append(getIndex());

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Chunk) {
            Chunk<E> otherChunk = (Chunk<E>) obj;
            return getIndex() == otherChunk.getIndex() && ObjectUtils.equals(getElements(), otherChunk.getElements());
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "[position: " + this.index + ", size: " + size() + ", lines: " + this.elements + "]";
    }
}
