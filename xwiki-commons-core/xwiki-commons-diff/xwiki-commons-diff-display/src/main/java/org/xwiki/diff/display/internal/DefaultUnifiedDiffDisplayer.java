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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.Conflict;
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.InlineDiffChunk;
import org.xwiki.diff.display.InlineDiffDisplayer;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.display.UnifiedDiffConfiguration;
import org.xwiki.diff.display.UnifiedDiffDisplayer;
import org.xwiki.diff.display.UnifiedDiffElement;
import org.xwiki.diff.display.UnifiedDiffElement.Type;
import org.xwiki.diff.internal.DefaultChunk;
import org.xwiki.diff.internal.DeltaFactory;

/**
 * Displays a {@link DiffResult} as a <a href="http://en.wikipedia.org/wiki/Diff#Unified_format">unified diff</a>. The
 * unified diff consists in a sequence of blocks, each having elements marked as either added or removed, padded with
 * unmodified elements that put changes in context.
 * <p>
 * NOTE: This class was greatly inspired by the {@code UnifiedPrint} class written by
 * <a href="mailto:ludovicc@users.sourceforge.net">Ludovic Claude</a> for the
 * <a href="http://cvsgrab.sourceforge.net/">CVSGrab</a> project under the Apache Software License version 1.1.
 *
 * @version $Id$
 * @since 4.1RC1
 */
@Component
@Singleton
public class DefaultUnifiedDiffDisplayer implements UnifiedDiffDisplayer
{
    /**
     * The state of the displayer.
     *
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second-level diff
     */
    private static class State<E, F>
    {
        /**
         * The collection of unified diff blocks build so far.
         */
        private final Stack<UnifiedDiffBlock<E, F>> blocks = new Stack<UnifiedDiffBlock<E, F>>();

        /**
         * The previous version, used to take the unmodified elements from.
         */
        private final List<E> previous;

        /**
         * The last change processed by the displayer.
         */
        private Delta<E> lastDelta;

        private Conflict<E> lastConflict;

        /**
         * Creates a new instance.
         *
         * @param previous the previous version used to take the unmodified elements from
         */
        State(List<E> previous)
        {
            this.previous = previous;
        }

        /**
         * @return the last processed change
         */
        public Delta<E> getLastDelta()
        {
            return this.lastDelta;
        }

        /**
         * Sets the last processed change.
         *
         * @param lastDelta the last processed change
         */
        public void setLastDelta(Delta<E> lastDelta)
        {
            this.lastDelta = lastDelta;
        }

        /**
         * Sets the last detected conflict.
         *
         * @param lastConflict the conflict detected.
         * @since 11.8RC1
         */
        public void setLastConflict(Conflict<E> lastConflict)
        {
            this.lastConflict = lastConflict;
        }

        /**
         * @return the last detected conflict.
         * @since 11.8RC1
         */
        public Conflict<E> getLastConflict()
        {
            return this.lastConflict;
        }

        /**
         * @return the collection of unified diff blocks build so far
         */
        public Stack<UnifiedDiffBlock<E, F>> getBlocks()
        {
            return this.blocks;
        }

        /**
         * @return the previous version, used to take the unmodified elements from
         */
        public List<E> getPrevious()
        {
            return this.previous;
        }
    }

    /**
     * The component used to determine the second level of changes, inside a modified element.
     */
    @Inject
    private DiffManager diffManager;

    /**
     * The component used to display the second level of changes, between modified elements.
     */
    @Inject
    private InlineDiffDisplayer inlineDisplayer;

    @Override
    public <E, F> UnifiedDiffConfiguration<E, F> getDefaultConfiguration()
    {
        return new UnifiedDiffConfiguration<E, F>();
    }

    @Override
    public <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult)
    {
        return display(diffResult, this.<E, F>getDefaultConfiguration());
    }

    @Override
    public <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult, List<Conflict<E>> conflicts)
    {
        return display(diffResult, conflicts, this.getDefaultConfiguration());
    }

    @Override
    public <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult, UnifiedDiffConfiguration<E, F> config)
    {
        return display(diffResult, null, config);
    }

    /**
     * Find the first conflict matching the delta, or null if there is no matching conflict.
     *
     * @param delta the delta for which we want to find a matching conflict.
     * @param conflicts the list of possible conflicts.
     * @param <E> the type of elements we manipulate.
     * @return an instance of conflict from the given list, or null if no conflict has been found.
     */
    private <E> Conflict<E> findConflict(Delta<E> delta, List<Conflict<E>> conflicts)
    {
        for (Conflict<E> conflict : conflicts) {
            if (conflict.concerns(delta)) {
                return conflict;
            }
        }
        return null;
    }

    /**
     * Isolate part of a chunk concerns by a conflict, and create the right pre and post chunks.
     *
     * @param originalChunk the chunk to split.
     * @param conflictIndex index where a conflict occurred, this is the started index where the chunk needs to be
     *                  isolated.
     * @param conflictSize the size of a conflict, this is the size of the part of the chunk which needs to be isolated.
     * @param <E> the type of elements we manipulate.
     * @return a list of chunks, which can contain from 1 (if the whole original chunk is concerned by the conflict) to
     *          3 chunks maximal (if there is a prefix chunk and a postfix chunk).
     */
    private <E> List<Chunk<E>> splitChunk(Chunk<E> originalChunk, int conflictIndex, int conflictSize)
    {
        List<Chunk<E>> result = new ArrayList<>();
        List<E> elements = originalChunk.getElements();
        int index = originalChunk.getIndex();
        int listIndex = 0;

        // prefix chunk
        if (index < conflictIndex) {
            result.add(new DefaultChunk<>(index, elements.subList(0, conflictIndex - index)));
            index = conflictIndex;
            listIndex = conflictIndex;
        }

        // chunk concerned by the conflict
        int listLastIndex = Math.min(elements.size(), listIndex + conflictSize);
        result.add(new DefaultChunk<>(index, elements.subList(listIndex, listLastIndex)));
        index += conflictSize;
        listIndex = listLastIndex;

        // postfix chunk
        if (listIndex < elements.size()) {
            result.add(new DefaultChunk<>(index, elements.subList(listIndex, elements.size())));
        }

        return result;
    }

    /**
     * Split a delta element to isolate the delta that is related to a conflict.
     *
     * @param delta the delta to split.
     * @param conflict the conflict which is related to this delta.
     * @param <E> the type of element we manipulate.
     * @return a list of 2 or 3 deltas, depending if the conflict is on the middle of the delta, or on top/bottom side.
     */
    private <E> List<Delta<E>> splitDelta(Delta<E> delta, Conflict<E> conflict)
    {
        List<Delta<E>> result = new ArrayList<>();
        List<Chunk<E>> previousChunks = splitChunk(delta.getPrevious(), conflict.getIndex(), conflict.getMaxSize());
        List<Chunk<E>> nextChunks = splitChunk(delta.getNext(), conflict.getIndex(), conflict.getMaxSize());

        for (int i = 0; i < Math.max(previousChunks.size(), nextChunks.size()); i++) {
            Chunk<E> previousChunk;
            Chunk<E> nextChunk;
            Delta.Type deltaType;

            if (i < previousChunks.size()) {
                previousChunk = previousChunks.get(i);
            } else {
                previousChunk = null;
            }

            if (i < nextChunks.size()) {
                nextChunk = nextChunks.get(i);
            } else {
                nextChunk = new DefaultChunk<>(previousChunk.getIndex(), Collections.emptyList());
            }

            if (previousChunk == null) {
                previousChunk = new DefaultChunk<>(nextChunk.getIndex(), Collections.emptyList());
            }

            if (previousChunk.getElements().isEmpty()) {
                deltaType = Delta.Type.INSERT;
            } else if (nextChunk.getElements().isEmpty()) {
                deltaType = Delta.Type.DELETE;
            } else {
                deltaType = Delta.Type.CHANGE;
            }
            result.add(DeltaFactory.createDelta(previousChunk, nextChunk, deltaType));
        }
        return result;
    }

    /**
     * Build a map of delta and their associated conflicts.
     * If only a subset of a delta is concerned by a conflict, then the delta is splitted to allow fixing the
     * conflict only, we use a recursive algorithm to do so.
     * If the conflicts list is null or empty, then return the original list of delta from the patch
     * with a null value associated.
     *
     * @param deltaList the list of delta to check
     * @param conflicts the list of conflicts to resolve, this list is consumed during the built of the map.
     * @param <E> the type of elements that are compared.
     * @return a map containing the delta to display and their associated conflicts. The map is ordered by delta
     *          insertion, which is related to their index.
     */
    private <E> Map<Delta<E>, Conflict<E>> buildDeltaConflictMap(List<Delta<E>> deltaList, List<Conflict<E>> conflicts)
    {
        // We need to use a LinkedHashMap here since we want to keep track of the order of insertion of the elements.
        Map<Delta<E>, Conflict<E>> result = new LinkedHashMap<>();
        for (Delta<E> delta : deltaList) {
            if (conflicts == null || conflicts.isEmpty()) {
                result.put(delta, null);
            } else {
                Conflict<E> conflict = findConflict(delta, conflicts);

                // a delta can be splitted only if one of its chunk is > 1
                boolean deltaCanBeSplitted = (delta.getPrevious().size() > 1 || delta.getNext().size() > 1);

                boolean conflictIsSubpartOfDelta = conflict != null
                    && (conflict.getMaxSize() != delta.getNext().size()
                    || conflict.getMaxSize() != delta.getPrevious().size());

                // If we found a conflict, but it only concerns a subpart of the delta, then we need to split this
                // delta, so we can associate the conflict with only the part of the delta concerned by the conflict.
                if (conflictIsSubpartOfDelta && deltaCanBeSplitted) {
                    List<Delta<E>> splittedDeltas = splitDelta(delta, conflict);
                    result.putAll(buildDeltaConflictMap(splittedDeltas, conflicts));

                // Else the conflict concerns the whole delta, so we can add it to the map and associate it with the
                // delta. We remove the conflict from our list, since it's already associated with a delta.
                } else {
                    result.put(delta, conflict);
                    conflicts.remove(conflict);
                }
            }
        }
        return result;
    }

    @Override
    public <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult, List<Conflict<E>> conflicts,
        UnifiedDiffConfiguration<E, F> config)
    {
        State<E, F> state = new State<E, F>(diffResult.getPrevious());

        // We use a new array list of conflicts here, since the conflicts will be consumed during the creation of the
        // map later.
        List<Conflict<E>> conflictList = null;
        if (conflicts != null) {
            conflictList = new ArrayList<>(conflicts);
        }

        // The current diffResult contains a list of delta, but we want two things:
        // 1. To associate each delta with a conflict if there is any
        // 2. To split up the delta, to only get the area concerned by the conflicts.
        Map<Delta<E>, Conflict<E>> deltaAndConflicts = buildDeltaConflictMap(diffResult.getPatch(), conflictList);

        for (Delta<E> delta : deltaAndConflicts.keySet()) {
            Conflict<E> conflict = deltaAndConflicts.get((delta));

            // Add unmodified elements before the current delta. Start a new block if the distance between the current
            // delta and the last one is greater than or equal to 2 * context size.
            // In case of conflict, we actually create a dedicated block for the context, before the conflict area.
            maybeStartBlock(delta, state, config.getContextSize(), conflict);

            // Add changed elements.
            switch (delta.getType()) {
                case CHANGE:
                    state.getBlocks().peek().addAll(this.<E, F>getModifiedElements(delta, config));
                    break;
                case DELETE:
                    state.getBlocks().peek().addAll(this.<E, F>getElements(delta.getPrevious(), Type.DELETED));
                    break;
                case INSERT:
                    state.getBlocks().peek().addAll(this.<E, F>getElements(delta.getNext(), Type.ADDED));
                    break;
                default:
                    break;
            }

            state.setLastDelta(delta);
        }

        // Add unmodified elements after the last delta.
        maybeEndBlock(state, config.getContextSize(), true);

        return state.getBlocks();
    }

    /**
     * Starts a new {@link UnifiedDiffBlock} if the provided change is in a different context, or if it belongs to a
     * conflict. The distance between two changes inside the same block is less than 2 * context size.
     *
     * @param delta the change
     * @param state the state of the displayer
     * @param contextSize the number of unmodified elements to display before and after each change
     * @param conflict the conflict the change is related to
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second-level diff when a composite element
     *            is modified
     */
    private <E, F> void maybeStartBlock(Delta<E> delta, State<E, F> state, int contextSize, Conflict<E> conflict)
    {
        if (state.getLastConflict() != conflict
            || state.getLastDelta() == null
            || state.getLastDelta().getPrevious().getLastIndex() < delta.getPrevious().getIndex() - contextSize * 2) {
            maybeEndBlock(state, contextSize, false);
            state.getBlocks().push(new UnifiedDiffBlock<E, F>());
        }

        // Add the unmodified elements before the given delta.
        int count = state.getBlocks().peek().isEmpty() ? contextSize : contextSize * 2;
        int lastChangeIndex = state.getLastDelta() == null ? -1 : state.getLastDelta().getPrevious().getLastIndex();
        int end = delta.getPrevious().getIndex();
        int start = Math.max(end - count, lastChangeIndex + 1);
        state.getBlocks().peek().addAll(this.<E, F>getUnmodifiedElements(state.getPrevious(), start, end));

        if (conflict != null && !state.getBlocks().peek().isEmpty()) {
            state.getBlocks().push(new UnifiedDiffBlock<>());
        }

        state.getBlocks().peek().setConflict(conflict);
        state.setLastConflict(conflict);
    }

    /**
     * Processes a change. In a unified diff the modified elements are either added or removed so we model a change by
     * listing the removed elements (from the previous version) followed by the added elements (from the next version).
     * If a splitter is provided through the given configuration object then we use it to split the changed elements (if
     * the number of removed elements equals the number of added elements) in sub-elements and produce an in-line diff
     * for the changes inside the modified elements.
     *
     * @param delta the change
     * @param config the configuration used to access the splitter
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second-level diff when a composite element
     *            is modified
     * @return the list of unified diff elements corresponding to the elements modified in the given delta
     */
    private <E, F> List<UnifiedDiffElement<E, F>> getModifiedElements(Delta<E> delta,
        UnifiedDiffConfiguration<E, F> config)
    {
        List<UnifiedDiffElement<E, F>> elements = new ArrayList<UnifiedDiffElement<E, F>>();
        elements.addAll(this.<E, F>getElements(delta.getPrevious(), Type.DELETED));
        elements.addAll(this.<E, F>getElements(delta.getNext(), Type.ADDED));

        // Compute the in-line diff if the number of removed elements equals the number of added elements.
        if (config.getSplitter() != null && delta.getPrevious().size() == delta.getNext().size()) {
            int changeSize = delta.getPrevious().size();
            for (int i = 0; i < changeSize; i++) {
                displayInlineDiff(elements.get(i), elements.get(changeSize + i), config);
            }
        }

        return elements;
    }

    /**
     * @param chunk the modified elements (both added and deleted)
     * @param changeType the change type
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second-level diff when a composite element
     *            is modified
     * @return the list of corresponding unified diff elements, matching the change type
     */
    private <E, F> List<UnifiedDiffElement<E, F>> getElements(Chunk<E> chunk, Type changeType)
    {
        int index = chunk.getIndex();
        List<UnifiedDiffElement<E, F>> elements = new ArrayList<UnifiedDiffElement<E, F>>();
        for (E element : chunk.getElements()) {
            elements.add(new UnifiedDiffElement<E, F>(index++, changeType, element));
        }
        return elements;
    }

    /**
     * @param previous the previous version
     * @param start the index of the first unmodified element to return
     * @param end the index to stop at
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second-level diff when a composite element
     *            is modified
     * @return the list of unmodified elements between the given start and end index
     */
    private <E, F> List<UnifiedDiffElement<E, F>> getUnmodifiedElements(List<E> previous, int start, int end)
    {
        List<UnifiedDiffElement<E, F>> unmodifiedElements = new ArrayList<UnifiedDiffElement<E, F>>();
        for (int i = start; i < end; i++) {
            unmodifiedElements.add(new UnifiedDiffElement<E, F>(i, Type.CONTEXT, previous.get(i)));
        }
        return unmodifiedElements;
    }

    /**
     * Ends the last {@link UnifiedDiffBlock} by adding a number of unmodified elements.
     *
     * @param state the state of the displayer
     * @param contextSize the number of unmodified elements to display at the end of a block
     * @param lastBlock if true, it's actually the last call after all delta, so we might want to display unmodified
     *         elements in a new block if previous one was a conflict block.
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second-level diff when a composite element
     *            is modified
     */
    private <E, F> void maybeEndBlock(State<E, F> state, int contextSize, boolean lastBlock)
    {
        if (!state.getBlocks().isEmpty() && (state.getLastConflict() == null || lastBlock)) {
            int start = state.getLastDelta().getPrevious().getLastIndex() + 1;
            int end = Math.min(start + contextSize, state.getPrevious().size());

            List<UnifiedDiffElement<E, F>> unmodifiedElements =
                this.<E, F>getUnmodifiedElements(state.getPrevious(), start, end);
            if (!unmodifiedElements.isEmpty() && state.getLastConflict() != null) {
                state.getBlocks().push(new UnifiedDiffBlock<>());
            }
            state.getBlocks().peek().addAll(unmodifiedElements);
        }
    }

    /**
     * Computes the changes between two versions of an element by splitting the element into sub-elements and displays
     * the result using the in-line format.
     *
     * @param previous the previous version
     * @param next the next version version
     * @param config the configuration for the in-line diff
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second-level diff when a composite element
     *            is modified
     */
    private <E, F> void displayInlineDiff(UnifiedDiffElement<E, F> previous, UnifiedDiffElement<E, F> next,
        UnifiedDiffConfiguration<E, F> config)
    {
        try {
            List<F> previousSubElements = config.getSplitter().split(previous.getValue());
            List<F> nextSubElements = config.getSplitter().split(next.getValue());
            DiffResult<F> diffResult = this.diffManager.diff(previousSubElements, nextSubElements, config);

            List<InlineDiffChunk<F>> chunks = this.inlineDisplayer.display(diffResult);
            previous.setChunks(new ArrayList<InlineDiffChunk<F>>());
            next.setChunks(new ArrayList<InlineDiffChunk<F>>());
            for (InlineDiffChunk<F> chunk : chunks) {
                if (!chunk.isAdded()) {
                    previous.getChunks().add(chunk);
                }
                if (!chunk.isDeleted()) {
                    next.getChunks().add(chunk);
                }
            }
        } catch (DiffException e) {
            // Do nothing.
        }
    }
}
