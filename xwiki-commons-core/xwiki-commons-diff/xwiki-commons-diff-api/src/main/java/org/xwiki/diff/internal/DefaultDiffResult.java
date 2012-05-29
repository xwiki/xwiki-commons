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
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.Patch;
import org.xwiki.logging.LogQueue;

/**
 * Default implementation of {@link DiffResult}.
 * 
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class DefaultDiffResult<E> implements DiffResult<E>
{
    /**
     * @see #getPrevious()
     */
    private List<E> previous;

    /**
     * @see #getNext()
     */
    private List<E> next;

    /**
     * @see #getLog()
     */
    private LogQueue log = new LogQueue();

    /**
     * @see #getPatch()
     */
    private Patch<E> patch;

    /**
     * @see #getUnifiedDiff()
     */
    private Patch<E> unifiedPatch;

    /**
     * @param previous the list before the modification
     * @param next the list after the modification
     */
    public DefaultDiffResult(List<E> previous, List<E> next)
    {
        this.previous = previous;
        this.next = next;
    }

    @Override
    public List<E> getNext()
    {
        return this.next;
    }

    @Override
    public List<E> getPrevious()
    {
        return this.previous;
    }

    @Override
    public LogQueue getLog()
    {
        return this.log;
    }

    @Override
    public Patch<E> getPatch()
    {
        return this.patch;
    }

    /**
     * @param patch the patch
     */
    public void setPatch(Patch<E> patch)
    {
        this.patch = patch;
    }

    @Override
    public Patch<E> getUnifiedDiff()
    {
        if (this.unifiedPatch == null) {
            if (this.patch.isEmpty()) {
                if (this.previous != null && !this.previous.isEmpty()) {
                    this.unifiedPatch.add(new SameDelta<E>(new DefaultChunk<E>(0, this.previous)));
                }
            } else {
                int index = 0;

                for (Delta<E> delta : this.patch) {
                    Chunk<E> previousChunk = delta.getPrevious();

                    // Put unmodified elements
                    int i = index;
                    while (index < previousChunk.getIndex()) {
                        ++index;
                    }
                    if (i != index) {
                        this.unifiedPatch
                            .add(new SameDelta<E>(new DefaultChunk<E>(i, this.previous.subList(i, index))));
                    }

                    // Put delta
                    this.unifiedPatch.add(delta);

                    index = previousChunk.getLastIndex() + 1;
                }

                if (index < this.previous.size()) {
                    this.unifiedPatch.add(new SameDelta<E>(new DefaultChunk<E>(index, this.previous.subList(index,
                        this.previous.size()))));
                }
            }
        }

        return this.unifiedPatch;
    }
}
