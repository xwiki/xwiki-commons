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

import org.xwiki.diff.Chunk;
import org.xwiki.diff.Conflict;
import org.xwiki.stability.Unstable;

/**
 * This is a {@link Conflict} representation for the unified diff.
 * It represents only a part of a conflict for a {@link UnifiedDiffElement}.
 *
 * @param <E> the type of elements that are compared to produce the first-level diff
 * @version $Id$
 * @since 11.7RC1
 */
@Unstable
public class UnifiedDiffConflictElement<E>
{
    private Conflict<E> conflict;

    private int elementIndex;

    /**
     * Creates a conflict element based on a given conflict and the index of the {@link UnifiedDiffElement}.
     * @param conflict the original conflict.
     * @param elementIndex the index of the element.
     */
    public UnifiedDiffConflictElement(Conflict<E> conflict, int elementIndex)
    {
        this.conflict = conflict;
        this.elementIndex = elementIndex;
    }

    private E getElementFromChunk(Chunk<E> chunk)
    {
        int occurenceIndex = elementIndex - chunk.getIndex();
        if (occurenceIndex < 0 || occurenceIndex > chunk.getLastIndex()) {
            return null;
        } else {
            return chunk.getElements().get(occurenceIndex);
        }
    }

    /**
     * @return the previous version of the element.
     */
    public E getPreviousElement()
    {
        Chunk<E> previousChunk = this.conflict.getDeltaCurrent().getPrevious();
        return getElementFromChunk(previousChunk);
    }

    /**
     * @return the next version of the element.
     */
    public E getNextElement()
    {
        Chunk<E> nextChunk = this.conflict.getDeltaNext().getNext();
        return getElementFromChunk(nextChunk);
    }

    /**
     * @return the current version of the element.
     */
    public E getCurrentElement()
    {
        Chunk<E> currentChunk = this.conflict.getDeltaCurrent().getNext();
        return getElementFromChunk(currentChunk);
    }

    /**
     * @return the conflict reference.
     */
    public String getReference()
    {
        return this.conflict.getReference();
    }
}
