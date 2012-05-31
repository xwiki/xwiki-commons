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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xwiki.diff.Chunk;
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.UnifiedDiffElement.Type;

/**
 * Displays a {@link DiffResult} as a <a href="http://en.wikipedia.org/wiki/Diff#Unified_format">unified diff</a>. The
 * unified diff consists in a sequence of blocks, each having elements marked as either added or removed, padded with
 * unmodified elements that put changes in context.
 * <p>
 * NOTE: This class was greatly inspired by the <a href="http
 * ://cvsgrab.cvs.sourceforge.net/viewvc/cvsgrab/cvsgrab/src/java/org/apache/commons/jrcs/diff/print/UnifiedPrint
 * .java">{@code UnifiedPrint}</a> class written by <a href="mailto:ludovicc@users.sourceforge.net">Ludovic Claude</a>
 * for the <a href="http://cvsgrab.sourceforge.net/">CVSGrab</a> project under the Apache Software License version 1.1.
 * 
 * @param <E> the type of elements that are compared to produce the diff
 * @version $Id$
 * @since 4.1RC1
 */
public class UnifiedDiffDisplayer<E>
{
    /**
     * The collection of unified diff blocks.
     */
    protected Stack<UnifiedDiffBlock<E>> blocks;

    /**
     * The number of unmodified elements to display before and after a chunk of modified elements. This number
     * determines how blocks are created. If the distance between two changes is less than the double of this value then
     * those changes are put in the same block (they share the same context).
     */
    private int contextSize = 3;

    /**
     * The last change processed by this displayer.
     */
    private Delta<E> lastDelta;

    /**
     * The previous version.
     */
    private List<E> previous;

    /**
     * @return the number of unmodified elements to display before and after a chunk of modified elements
     */
    public int getContextSize()
    {
        return contextSize;
    }

    /**
     * Sets the number of unmodified elements to display before and after a chunk of modified elements.
     * 
     * @param contextSize the context size
     */
    public void setContextSize(int contextSize)
    {
        this.contextSize = contextSize;
    }

    /**
     * Displays the given diff result as an unified diff.
     * 
     * @param diffResult the diff result
     * @return the list of blocks that form the unified diff
     */
    public List<UnifiedDiffBlock<E>> display(DiffResult<E> diffResult)
    {
        lastDelta = null;
        previous = diffResult.getPrevious();
        blocks = new Stack<UnifiedDiffBlock<E>>();

        for (Delta<E> delta : diffResult.getPatch()) {
            // Add unmodified elements before the current delta. Start a new block if the distance between the current
            // delta and the last one is greater than or equal to 2 * context size.
            maybeStartBlock(delta);

            // Add changed elements.
            switch (delta.getType()) {
                case CHANGE:
                    onChange(delta);
                    break;
                case DELETE:
                    blocks.peek().addAll(getElements(delta.getPrevious(), Type.DELETED));
                    break;
                case INSERT:
                    blocks.peek().addAll(getElements(delta.getNext(), Type.ADDED));
                    break;
                default:
                    break;
            }

            lastDelta = delta;
        }

        // Add unmodified elements after the last delta.
        maybeEndBlock();

        return blocks;
    }

    /**
     * Starts a new {@link UnifiedDiffBlock} if the provided change is in a different context. The distance between two
     * changes inside the same block is less than 2 * context size.
     * 
     * @param delta the change
     */
    private void maybeStartBlock(Delta<E> delta)
    {
        if (lastDelta == null
            || lastDelta.getPrevious().getLastIndex() < delta.getPrevious().getIndex() - getContextSize() * 2) {
            maybeEndBlock();
            blocks.push(new UnifiedDiffBlock<E>());
        }

        int anchor = delta.getPrevious().getIndex();
        int border = blocks.peek().isEmpty() ? getContextSize() : getContextSize() * 2;
        int lastChangeIndex = lastDelta == null ? -1 : lastDelta.getPrevious().getLastIndex();
        for (int i = border; i > 0; i--) {
            if (anchor - i > lastChangeIndex) {
                blocks.peek().add(new UnifiedDiffElement<E>(anchor - i, Type.CONTEXT, previous.get(anchor - i)));
            }
        }

        lastDelta = delta;
    }

    /**
     * Processes a change. In a unified diff the modified elements are either added or removed so we model a modified
     * element with two elements: one removed (the previous version) and one added (the next version).
     * 
     * @param delta the change
     */
    protected void onChange(Delta<E> delta)
    {
        blocks.peek().addAll(getElements(delta.getPrevious(), Type.DELETED));
        blocks.peek().addAll(getElements(delta.getNext(), Type.ADDED));
    }

    /**
     * @param chunk the modified elements (both added and deleted)
     * @param changeType the change type
     * @return the list of corresponding unified diff elements, matching the change type
     */
    private List<UnifiedDiffElement<E>> getElements(Chunk<E> chunk, Type changeType)
    {
        int index = chunk.getIndex();
        List<UnifiedDiffElement<E>> elements = new ArrayList<UnifiedDiffElement<E>>();
        for (E element : chunk.getElements()) {
            elements.add(new UnifiedDiffElement<E>(index++, changeType, element));
        }
        return elements;
    }

    /**
     * Ends the last {@link UnifiedDiffBlock}.
     */
    private void maybeEndBlock()
    {
        if (!blocks.isEmpty()) {
            int lastIndex = lastDelta.getPrevious().getLastIndex();
            for (int i = lastIndex + 1; i <= lastIndex + getContextSize() && i < previous.size(); i++) {
                blocks.peek().add(new UnifiedDiffElement<E>(i, Type.CONTEXT, previous.get(i)));
            }
        }
    }
}
