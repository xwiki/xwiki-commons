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
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.InlineDiffWord.WordType;

/**
 * Displays a {@link DiffResult} as an in-line diff.
 * 
 * @version $Id$
 * @since 4.1RC1
 */
public class InlineDiffDisplayer
{
    /**
     * Displays the given diff result as an in-line diff.
     * 
     * @param <E> the character type
     * @param diffResult the diff result to be displayed
     * @return the in-line diff
     */
    public <E> List<InlineDiffWord<E>> display(DiffResult<E> diffResult)
    {
        List<E> original = diffResult.getPrevious();
        List<InlineDiffWord<E>> words = new ArrayList<InlineDiffWord<E>>();

        Delta<E> previousDelta = null;
        for (Delta<E> delta : diffResult.getPatch()) {
            // Add context word between deltas.
            int contextStart = previousDelta == null ? 0 : previousDelta.getPrevious().getLastIndex() + 1;
            int contextEnd = delta.getPrevious().getIndex();
            if (contextStart < contextEnd) {
                words.add(new InlineDiffWord<E>(WordType.CONTEXT, original.subList(contextStart, contextEnd)));
            }

            // Add changed words.
            switch (delta.getType()) {
                case CHANGE:
                    words.add(new InlineDiffWord<E>(WordType.DELETED, delta.getPrevious().getElements()));
                    words.add(new InlineDiffWord<E>(WordType.ADDED, delta.getNext().getElements()));
                    break;
                case DELETE:
                    words.add(new InlineDiffWord<E>(WordType.DELETED, delta.getPrevious().getElements()));
                    break;
                case INSERT:
                    words.add(new InlineDiffWord<E>(WordType.ADDED, delta.getNext().getElements()));
                    break;
                default:
                    break;
            }

            previousDelta = delta;
        }

        // Add the final context word.
        int contextStart = previousDelta == null ? 0 : previousDelta.getPrevious().getLastIndex() + 1;
        if (contextStart < original.size()) {
            words.add(new InlineDiffWord<E>(WordType.CONTEXT, original.subList(contextStart, original.size())));
        }

        return words;
    }
}
