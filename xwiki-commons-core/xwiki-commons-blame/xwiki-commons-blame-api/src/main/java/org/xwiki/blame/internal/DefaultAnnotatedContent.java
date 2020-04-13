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

package org.xwiki.blame.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.xwiki.blame.AnnotatedContent;
import org.xwiki.blame.AnnotatedElement;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;

/**
 * Hold content during blame analysis and provides actual results.
 *
 * @param <R> type of the revision object that old metadata about the revision.
 * @param <E> type of the element to annotate (ie: String holding a line).
 * @version $Id$
 * @since 6.2M2
 */
public class DefaultAnnotatedContent<R, E> implements AnnotatedContent<R, E>
{
    private final List<R> sourceRevisions;
    private final List<E> initialContent;
    private final List<E> currentRevisionContent;
    private R currentRevision;
    private final List<Integer> elementList;
    private final int size;

    DefaultAnnotatedContent(R revision, List<E> initialContent)
    {
        this.size = initialContent.size();
        this.sourceRevisions = new ArrayList<>(this.size);
        this.initialContent = initialContent;
        this.currentRevisionContent = new ArrayList<>(initialContent);
        this.currentRevision = revision;
        this.elementList = new ArrayList<>(this.size);
        for (int i = 0; i < size; i++) {
            this.sourceRevisions.add(null);
            this.elementList.add(i);
        }
    }

    private class AnnotatedContentIterator implements Iterator<AnnotatedElement<R, E>>
    {
        private int index = -1;

        @Override
        public boolean hasNext()
        {
            return index < (size - 1);
        }

        @Override
        public AnnotatedElement<R, E> next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException("No more annotated content");
            }

            index += 1;
            return new DefaultAnnotatedElement<>(sourceRevisions.get(index), initialContent.get(index));
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterator<AnnotatedElement<R, E>> iterator()
    {
        return new AnnotatedContentIterator();
    }

    @Override
    public R getOldestRevision()
    {
        return currentRevision;
    }

    @Override
    public boolean isEntirelyAnnotated()
    {
        for (int i = 0; i < this.size; i++) {
            if (this.sourceRevisions.get(i) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resolve revision of line to current revision based on given previous content, and prepare for next analysis.
     *
     * @param revision the revision of the content provided.
     * @param previous the content in a previous revision.
     */
    void analyseRevision(R revision, List<E> previous)
    {
        if (currentRevision == null) {
            return;
        }

        if (previous == null || previous.isEmpty()) {
            resolveRemainingToCurrent();
        } else {
            resolveToCurrent(DiffUtils.diff(currentRevisionContent, previous).getDeltas());
            assert currentRevisionContent.equals(previous) : "Patch application failed";
        }

        currentRevision = revision;
    }

    /**
     * Resolve revision of line to current revision based on given previous content.
     *
     * Thanks to Michael Schierl <schierlm%40gmx.de> for sharing this code on StackOverflow.
     *
     * @param deltas the delta to apply to current content to move to previous revision.
     */
    private void resolveToCurrent(List<Delta<E>> deltas)
    {
        int lineOffset = 0;

        for (Delta<E> d : deltas) {
            Chunk<E> original = d.getOriginal();
            Chunk<E> revised = d.getRevised();

            int pos = original.getPosition() + lineOffset;
            // delete lines
            for (int i = 0; i < original.size(); i++) {
                int origLine = elementList.remove(pos);
                currentRevisionContent.remove(pos);
                if (origLine != -1) {
                    sourceRevisions.set(origLine, currentRevision);
                }
            }

            for (int i = 0; i < revised.size(); i++) {
                currentRevisionContent.add(pos + i, revised.getLines().get(i));
                elementList.add(pos + i, -1);
            }

            lineOffset += revised.size() - original.size();
        }
    }

    /**
     * Resolve all line remaining without revision to current.
     */
    private void resolveRemainingToCurrent()
    {
        for (int i = 0; i < this.size; i++) {
            if (sourceRevisions.get(i) == null) {
                sourceRevisions.set(i, currentRevision);
            }
        }
    }
}
