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
import org.xwiki.diff.display.InlineDiffWord.WordType;

/**
 * Extends the {@link UnifiedDiffDisplayer} with the ability to provide character-level changes when a line is modified.
 * 
 * @param <E> the type of line content
 * @param <F> the type of characters within a line
 * @version $Id$
 * @since 4.1RC1
 */
public class ExtendedDiffDisplayer<E, F> extends UnifiedDiffDisplayer<E>
{
    /**
     * The component used to determine character-level changes inside a modified line.
     */
    private final DiffManager diffManager;

    /**
     * The component used to split lines into characters.
     */
    private final Splitter<E, F> splitter;

    /**
     * Creates a new instance.
     * 
     * @param diffManager the component used to determine character-level changes inside a modified line
     * @param splitter the component used to split lines into characters
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

        // A line is modified when it is replaced by a single line.
        if (delta.getPrevious().size() == 1 && delta.getNext().size() == 1) {
            UnifiedDiffBlock<E> lastBlock = blocks.peek();
            UnifiedDiffLine<E> original = lastBlock.get(lastBlock.size() - 2);
            UnifiedDiffLine<E> revised = lastBlock.get(lastBlock.size() - 1);
            List<UnifiedDiffLine<E>> extendedLines = displayInlineDiff(original, revised);
            lastBlock.set(lastBlock.size() - 2, extendedLines.get(0));
            lastBlock.set(lastBlock.size() - 1, extendedLines.get(1));
        }
    }

    /**
     * Displays the in-line diff between two versions of a line.
     * 
     * @param original the original version
     * @param revised the revised version
     * @return the given lines extended with information about character-level changes
     */
    private List<UnifiedDiffLine<E>> displayInlineDiff(UnifiedDiffLine<E> original, UnifiedDiffLine<E> revised)
    {
        List<UnifiedDiffLine<E>> result = new ArrayList<UnifiedDiffLine<E>>();
        try {
            List<F> originalChars = splitter.split(original.getContent());
            List<F> revisedChars = splitter.split(revised.getContent());
            DiffResult<F> diffResult = diffManager.diff(originalChars, revisedChars, null);

            List<InlineDiffWord<F>> words = new InlineDiffDisplayer().display(diffResult);
            List<InlineDiffWord<F>> originalWords = new ArrayList<InlineDiffWord<F>>();
            List<InlineDiffWord<F>> revisedWords = new ArrayList<InlineDiffWord<F>>();
            for (InlineDiffWord<F> word : words) {
                if (word.getType() != WordType.ADDED) {
                    originalWords.add(word);
                }
                if (word.getType() != WordType.DELETED) {
                    revisedWords.add(word);
                }
            }

            result.add(new ExtendedDiffLine<E, F>(original, originalWords));
            result.add(new ExtendedDiffLine<E, F>(revised, revisedWords));
        } catch (DiffException e) {
            result.add(original);
            result.add(revised);
        }
        return result;
    }
}
