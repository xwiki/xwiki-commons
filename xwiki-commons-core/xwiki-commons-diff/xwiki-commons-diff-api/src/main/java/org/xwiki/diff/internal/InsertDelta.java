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

import org.xwiki.diff.Chunk;
import org.xwiki.diff.PatchException;

/**
 * Implementation of {@link org.xwiki.diff.Delta} representing a chunk which does not exist in previous version.
 *
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class InsertDelta<E> extends AbstractDelta<E>
{
    /**
     * @param previous the chunk before the modification
     * @param next the chunk after the modification
     */
    public InsertDelta(Chunk<E> previous, Chunk<E> next)
    {
        super(previous, next, Type.INSERT);
    }

    /**
     * @param original the chunk before the modification
     * @param revised the chunk after the modification
     */
    public InsertDelta(com.github.difflib.patch.Chunk<E> original, com.github.difflib.patch.Chunk<E> revised)
    {
        super(original, revised, Type.INSERT);
    }

    @Override
    public void apply(List<E> target) throws PatchException
    {
        verify(target);

        int index = getPrevious().getIndex();
        List<E> elements = getNext().getElements();
        for (int i = 0; i < elements.size(); i++) {
            target.add(index + i, elements.get(i));
        }
    }

    @Override
    public void restore(List<E> target)
    {
        int index = getNext().getIndex();
        int size = getNext().size();
        for (int i = 0; i < size; i++) {
            target.remove(index);
        }
    }

    @Override
    public void verify(List<E> target) throws PatchException
    {
        if (getPrevious().getIndex() > target.size()) {
            throw new PatchException("Incorrect patch for delta: delta original position > target size");
        }
    }
}
