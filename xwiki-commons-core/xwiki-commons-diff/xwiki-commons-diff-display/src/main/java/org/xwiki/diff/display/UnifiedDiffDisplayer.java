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
import org.xwiki.diff.display.UnifiedDiffLine.LineType;

/**
 * Displays a {@link DiffResult} as a <a href="http://en.wikipedia.org/wiki/Diff#Unified_format">unified diff</a>.
 * <p>
 * NOTE: This class was greatly inspired by the <a href="http
 * ://cvsgrab.cvs.sourceforge.net/viewvc/cvsgrab/cvsgrab/src/java/org/apache/commons/jrcs/diff/print/UnifiedPrint
 * .java">{@code UnifiedPrint}</a> class written by <a href="mailto:ludovicc@users.sourceforge.net">Ludovic Claude</a>
 * for the <a href="http://cvsgrab.sourceforge.net/">CVSGrab</a> project under the Apache Software License version 1.1.
 * 
 * @param <E> the type of content that is being compared
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
     * The number of unmodified lines to display before and after a block of modified lines.
     */
    private int contextSize = 3;

    /**
     * The last change processed by this builder.
     */
    private Delta<E> lastDelta;

    /**
     * The original version.
     */
    private List<E> original;

    /**
     * @return the number of unmodified lines to display before and after a block of modified lines
     */
    public int getContextSize()
    {
        return contextSize;
    }

    /**
     * Sets the number of unmodified lines to display before and after a block of modified lines.
     * 
     * @param contextSize the context size
     */
    public void setContextSize(int contextSize)
    {
        this.contextSize = contextSize;
    }

    /**
     * Displays the give diff result as a unified diff.
     * 
     * @param diffResult the diff result
     * @return the unified diff
     */
    public List<UnifiedDiffBlock<E>> display(DiffResult<E> diffResult)
    {
        lastDelta = null;
        original = diffResult.getPrevious();
        blocks = new Stack<UnifiedDiffBlock<E>>();

        for (Delta<E> delta : diffResult.getPatch()) {
            // Add context lines before changed lines.
            maybeStartBlock(delta);

            // Add changed lines.
            switch (delta.getType()) {
                case CHANGE:
                    onChange(delta);
                    break;
                case DELETE:
                    blocks.peek().addAll(getLines(delta.getPrevious(), LineType.DELETED));
                    break;
                case INSERT:
                    blocks.peek().addAll(getLines(delta.getNext(), LineType.ADDED));
                    break;
                default:
                    break;
            }

            lastDelta = delta;
        }

        // Add context lines after the last changed line.
        maybeEndBlock();

        return blocks;
    }

    /**
     * Starts a new {@link UnifiedDiffBlock} if the provided change is in a different context.
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
        int lastChangedLineNumber = lastDelta == null ? -1 : lastDelta.getPrevious().getLastIndex();
        for (int i = border; i > 0; i--) {
            if (anchor - i > lastChangedLineNumber) {
                blocks.peek().add(new UnifiedDiffLine<E>(anchor - i, LineType.CONTEXT, original.get(anchor - i)));
            }
        }

        lastDelta = delta;
    }

    /**
     * Processed a change.
     * 
     * @param delta the change
     */
    protected void onChange(Delta<E> delta)
    {
        blocks.peek().addAll(getLines(delta.getPrevious(), LineType.DELETED));
        blocks.peek().addAll(getLines(delta.getNext(), LineType.ADDED));
    }

    /**
     * @param chunk the modified lines
     * @param lineType the modification type
     * @return the list of corresponding unified diff lines
     */
    private List<UnifiedDiffLine<E>> getLines(Chunk<E> chunk, LineType lineType)
    {
        int lineNumber = chunk.getIndex();
        List<UnifiedDiffLine<E>> lines = new ArrayList<UnifiedDiffLine<E>>();
        for (E lineContent : chunk.getElements()) {
            lines.add(new UnifiedDiffLine<E>(lineNumber++, lineType, lineContent));
        }
        return lines;
    }

    /**
     * Ends the last {@link UnifiedDiffBlock}.
     */
    private void maybeEndBlock()
    {
        if (!blocks.isEmpty()) {
            int lastLineNumber = lastDelta.getPrevious().getLastIndex();
            for (int i = lastLineNumber + 1; i <= lastLineNumber + getContextSize() && i < original.size(); i++) {
                blocks.peek().add(new UnifiedDiffLine<E>(i, LineType.CONTEXT, original.get(i)));
            }
        }
    }
}
