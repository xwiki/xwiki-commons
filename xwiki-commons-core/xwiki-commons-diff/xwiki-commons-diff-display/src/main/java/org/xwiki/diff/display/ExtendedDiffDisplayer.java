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

import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;

/**
 * Extends the {@link UnifiedDiffDisplayer} with the ability to provide changes for sub-elements when an element is
 * modified. This allows us to display changes at two levels of granularity (elements and their sub-elements).
 * 
 * @param <E> the type of elements that are compared to produce the first level of diff
 * @param <F> the type of sub-elements that are compared to produce the second level of diff
 * @version $Id$
 * @since 4.1RC1
 */
public class ExtendedDiffDisplayer<E, F> extends UnifiedDiffDisplayer<E>
{
    /**
     * The component used to determine the second level of changes, inside a modified element.
     */
    private final DiffManager diffManager;

    /**
     * The component used to split elements into sub-elements.
     */
    private final Splitter<E, F> splitter;

    /**
     * Creates a new instance.
     * 
     * @param diffManager the component used to determine the second level of changes, inside a modified element
     * @param splitter the component used to split elements into sub-elements
     */
    public ExtendedDiffDisplayer(DiffManager diffManager, Splitter<E, F> splitter)
    {
        this.diffManager = diffManager;
        this.splitter = splitter;
    }

    @Override
    protected void onChange(Delta<E> delta)
    {
        super.onChange(delta);

        // An element is modified when it is replaced by a single element.
        if (delta.getPrevious().size() == 1 && delta.getNext().size() == 1) {
            UnifiedDiffBlock<E> lastBlock = blocks.peek();
            UnifiedDiffElement<E> previous = lastBlock.get(lastBlock.size() - 2);
            UnifiedDiffElement<E> next = lastBlock.get(lastBlock.size() - 1);
            List<UnifiedDiffElement<E>> extendedElements = displayInlineDiff(previous, next);
            lastBlock.set(lastBlock.size() - 2, extendedElements.get(0));
            lastBlock.set(lastBlock.size() - 1, extendedElements.get(1));
        }
    }

    /**
     * Computes the changes between two versions of an element by splitting the element into sub-elements and displays
     * the result using the in-line format.
     * 
     * @param previous the previous version
     * @param next the next version version
     * @return the passed elements extended with information about sub-element changes
     */
    private List<UnifiedDiffElement<E>> displayInlineDiff(UnifiedDiffElement<E> previous, UnifiedDiffElement<E> next)
    {
        List<UnifiedDiffElement<E>> result = new ArrayList<UnifiedDiffElement<E>>();
        try {
            List<F> previousSubElements = splitter.split(previous.getValue());
            List<F> nextSubElements = splitter.split(next.getValue());
            DiffResult<F> diffResult = diffManager.diff(previousSubElements, nextSubElements, null);

            List<InlineDiffChunk<F>> chunks = new InlineDiffDisplayer().display(diffResult);
            List<InlineDiffChunk<F>> previousChunks = new ArrayList<InlineDiffChunk<F>>();
            List<InlineDiffChunk<F>> nextChunks = new ArrayList<InlineDiffChunk<F>>();
            for (InlineDiffChunk<F> chunk : chunks) {
                if (!chunk.isAdded()) {
                    previousChunks.add(chunk);
                }
                if (!chunk.isDeleted()) {
                    nextChunks.add(chunk);
                }
            }

            result.add(new ExtendedDiffElement<E, F>(previous, previousChunks));
            result.add(new ExtendedDiffElement<E, F>(next, nextChunks));
        } catch (DiffException e) {
            result.add(previous);
            result.add(next);
        }
        return result;
    }
}
