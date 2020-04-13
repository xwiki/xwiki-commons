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
package org.xwiki.diff.display.internal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.InlineDiffChunk;
import org.xwiki.diff.display.InlineDiffChunk.Type;
import org.xwiki.diff.display.InlineDiffDisplayer;

/**
 * Default {@link InlineDiffDisplayer} implementation.
 *
 * @version $Id$
 * @since 4.1RC1
 */
@Component
@Singleton
public class DefaultInlineDiffDisplayer implements InlineDiffDisplayer
{
    @Override
    public <E> List<InlineDiffChunk<E>> display(DiffResult<E> diffResult)
    {
        List<E> previous = diffResult.getPrevious();
        List<InlineDiffChunk<E>> chunks = new ArrayList<>();

        Delta<E> lastDelta = null;
        for (Delta<E> delta : diffResult.getPatch()) {
            // Add a chunk with the unmodified elements between the last delta and the current one.
            int contextStart = lastDelta == null ? 0 : lastDelta.getPrevious().getLastIndex() + 1;
            int contextEnd = delta.getPrevious().getIndex();
            if (contextStart < contextEnd) {
                chunks.add(new InlineDiffChunk<>(Type.UNMODIFIED, previous.subList(contextStart, contextEnd)));
            }

            // Add changed chunks.
            switch (delta.getType()) {
                case CHANGE:
                    chunks.add(new InlineDiffChunk<>(Type.DELETED, delta.getPrevious().getElements()));
                    chunks.add(new InlineDiffChunk<>(Type.ADDED, delta.getNext().getElements()));
                    break;
                case DELETE:
                    chunks.add(new InlineDiffChunk<>(Type.DELETED, delta.getPrevious().getElements()));
                    break;
                case INSERT:
                    chunks.add(new InlineDiffChunk<>(Type.ADDED, delta.getNext().getElements()));
                    break;
                default:
                    break;
            }

            lastDelta = delta;
        }

        // Add the final chunk with the unmodified elements after the last delta.
        int contextStart = lastDelta == null ? 0 : lastDelta.getPrevious().getLastIndex() + 1;
        if (contextStart < previous.size()) {
            chunks.add(new InlineDiffChunk<>(Type.UNMODIFIED, previous.subList(contextStart, previous.size())));
        }

        return chunks;
    }
}
