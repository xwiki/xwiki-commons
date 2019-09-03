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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.ConflictDecision;
import org.xwiki.diff.Delta;
import org.xwiki.diff.Delta.Type;
import org.xwiki.diff.DiffConfiguration;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeConfiguration.Version;
import org.xwiki.diff.MergeException;
import org.xwiki.diff.MergeResult;
import org.xwiki.diff.Patch;

import difflib.DiffUtils;

/**
 * Default implementation of {@link DiffManager}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultDiffManager implements DiffManager
{
    @Override
    public <E> DiffResult<E> diff(List<E> previous, List<E> next, DiffConfiguration<E> diff) throws DiffException
    {
        DefaultDiffResult<E> result = new DefaultDiffResult<E>(previous, next);

        // DiffUtils#diff does not support null
        Patch<E> patch;
        if (previous == null || previous.isEmpty()) {
            patch = new DefaultPatch<E>();
            if (next != null && !next.isEmpty()) {
                patch.add(new InsertDelta<E>(new DefaultChunk<E>(0, Collections.<E>emptyList()),
                    new DefaultChunk<E>(0, next)));
            }
        } else if (next == null || next.isEmpty()) {
            patch = new DefaultPatch<E>();
            patch.add(new DeleteDelta<E>(new DefaultChunk<E>(0, previous),
                new DefaultChunk<E>(0, Collections.<E>emptyList())));
        } else {
            patch = new DefaultPatch<E>(DiffUtils.diff(previous, next));
        }

        result.setPatch(patch);

        return result;
    }

    @Override
    public <E> MergeResult<E> merge(List<E> commonAncestor, List<E> next, List<E> current,
        MergeConfiguration<E> configuration) throws MergeException
    {
        DefaultMergeResult<E> mergeResult = new DefaultMergeResult<E>(commonAncestor, next, current);

        // Get diff between common ancestor and next version

        DiffResult<E> diffNextResult;
        try {
            diffNextResult = diff(commonAncestor, next, null);
        } catch (DiffException e) {
            throw new MergeException("Faile to diff between common ancestor and next version", e);
        }
        mergeResult.getLog().addAll(diffNextResult.getLog());

        Patch<E> patchNext = diffNextResult.getPatch();

        // If there is no modification stop there

        if (patchNext.isEmpty()) {
            // No change so nothing to do
            return mergeResult;
        }

        // Check current version

        if (current.isEmpty() && (commonAncestor.isEmpty() || next.isEmpty())) {
            // Empty current version
            if (commonAncestor.isEmpty()) {
                mergeResult.setMerged(next);
            } else if (next.isEmpty()) {
                // The new modification was already applied
                mergeResult.getLog().warn("The modification was already applied");
            }
        } else {
            // Get diff between common ancestor and current version
            DiffResult<E> diffCurrentResult;
            try {
                diffCurrentResult = diff(commonAncestor, current, null);
            } catch (DiffException e) {
                throw new MergeException("Fail to diff between common ancestor and current version", e);
            }
            mergeResult.getLog().addAll(diffCurrentResult.getLog());

            Patch<E> patchCurrent = diffCurrentResult.getPatch();

            mergeResult.setMerged(new ArrayList<>());
            if (patchCurrent.isEmpty()) {
                mergeResult.setMerged(next);
            } else if (!current.equals(next) && (isFullyModified(commonAncestor, patchCurrent)
                || isFullyModified(commonAncestor, patchNext))) {
                // If current or next  is completely modified compared to the common ancestor we assume
                // any change in next or current is a conflict
                // ... except if the current content is identical to the next one!
                Delta<E> deltaNext = nextElement(patchNext);
                Delta<E> deltaCurrent = nextElement(patchCurrent);
                List<ConflictDecision<E>> conflictDecisionList =
                    (configuration != null) ? configuration.getConflictDecisionList() : Collections.emptyList();
                int newIndex = applyDecision(conflictDecisionList, mergeResult.getMerged(), 0);
                if (newIndex == Integer.MIN_VALUE) {
                    logConflict(mergeResult, deltaCurrent, deltaNext, commonAncestor, next, current, 0);
                    mergeResult.setMerged(fallback(commonAncestor, next, current, configuration));
                }
            } else {
                merge(mergeResult, commonAncestor, next, current, patchNext, patchCurrent, configuration);
            }
        }

        return mergeResult;
    }

    private <E> ConflictDecision<E> findDecision(List<ConflictDecision<E>> decisions, int currentIndex)
    {
        ConflictDecision<E> result = null;
        if (decisions != null && !decisions.isEmpty()) {
            for (ConflictDecision<E> decision : decisions) {
                if (decision.getConflict().getIndex() == currentIndex) {
                    result = decision;
                    break;
                }
            }
        }
        return result;
    }

    private <E> int applyDecision(List<ConflictDecision<E>> decisions, List<E> merged, int currentIndex)
    {
        ConflictDecision<E> conflictDecision = findDecision(decisions, currentIndex);
        if (conflictDecision == null || conflictDecision.getType() == ConflictDecision.DecisionType.UNDECIDED) {
            return Integer.MIN_VALUE;
        } else {
            merged.addAll(conflictDecision.getChunk().getElements());
            return merged.size();
        }
    }

    private <E> int applyDecision(List<ConflictDecision<E>> decisions, List<E> commonAncestor, Delta<E> deltaNext,
        Delta<E> deltaCurrent, List<E> merged, int currentIndex)
    {
        ConflictDecision<E> conflictDecision = findDecision(decisions, currentIndex);
        if (conflictDecision == null || conflictDecision.getType() == ConflictDecision.DecisionType.UNDECIDED) {
            return Integer.MIN_VALUE;
        } else {
            switch (conflictDecision.getType()) {
                case PREVIOUS:
                    return fallback(commonAncestor, deltaNext, deltaCurrent, merged, currentIndex, Version.PREVIOUS);

                case NEXT:
                    return fallback(commonAncestor, deltaNext, deltaCurrent, merged, currentIndex, Version.NEXT);

                case CURRENT:
                    return fallback(commonAncestor, deltaNext, deltaCurrent, merged, currentIndex, Version.CURRENT);

                case CUSTOM:
                    merged.addAll(conflictDecision.getChunk().getElements());
                    return Math.max(deltaCurrent.getPrevious().getLastIndex(), deltaNext.getPrevious().getLastIndex());

                default:
                    // should never occur
                    throw new IllegalArgumentException();
            }
        }
    }

    private <E> List<E> fallback(List<E> commonAncestor, List<E> next, List<E> current,
        MergeConfiguration<E> configuration)
    {
        Version fallbackVersion;
        if (configuration != null) {
            fallbackVersion = configuration.getFallbackOnConflict();
        } else {
            fallbackVersion = Version.CURRENT;
        }

        switch (fallbackVersion) {
            case NEXT:
                return next;
            case PREVIOUS:
                return commonAncestor;
            default:
                return current;
        }
    }

    private <E> int fallback(List<E> commonAncestor, Delta<E> deltaNext, Delta<E> deltaCurrent, List<E> merged,
        int currentIndex, MergeConfiguration<E> configuration)
    {
        Version fallbackVersion;
        if (configuration != null) {
            fallbackVersion = configuration.getFallbackOnConflict();
        } else {
            fallbackVersion = Version.CURRENT;
        }
        return fallback(commonAncestor, deltaNext, deltaCurrent, merged, currentIndex, fallbackVersion);
    }

    private <E> int fallback(List<E> commonAncestor, Delta<E> deltaNext, Delta<E> deltaCurrent, List<E> merged,
        int currentIndex, Version fallbackVersion)
    {
        int newIndex = currentIndex;

        switch (fallbackVersion) {
            case NEXT:
                for (; newIndex < deltaNext.getPrevious().getIndex(); ++newIndex) {
                    merged.add(commonAncestor.get(newIndex));
                }
                newIndex = apply(deltaNext, merged, currentIndex);
                break;
            case PREVIOUS:
                int stopIndex =
                    Math.max(deltaCurrent.getPrevious().getLastIndex(), deltaNext.getPrevious().getLastIndex()) + 1;
                for (; newIndex < stopIndex; ++newIndex) {
                    merged.add(commonAncestor.get(newIndex));
                }

                if (currentIndex != newIndex) {
                    // each time this fallback is called, the loop increment back the index
                    // so we have to decrement it to be sure we are at the right position.
                    newIndex--;
                }
                break;
            default:
                // CURRENT is the default
                for (; newIndex < deltaCurrent.getPrevious().getIndex(); ++newIndex) {
                    merged.add(commonAncestor.get(newIndex));
                }
                newIndex = apply(deltaCurrent, merged, currentIndex);
                break;
        }

        return newIndex;
    }

    /**
     * @param <E> the type of compared elements
     * @param mergeResult the result of the merge
     * @param commonAncestor the common ancestor of the two versions of the content to compare
     * @param patchNext the diff between common ancestor and next version
     * @param patchCurrent the diff between common ancestor and current version
     * @param configuration the configuration of the merge behavior
     * @throws MergeException failed to merge
     */
    private <E> void merge(DefaultMergeResult<E> mergeResult, List<E> commonAncestor, List<E> next, List<E> current,
        Patch<E> patchNext, Patch<E> patchCurrent, MergeConfiguration<E> configuration)
        throws MergeException
    {
        List<ConflictDecision<E>> conflictDecisions =
            (configuration != null) ? configuration.getConflictDecisionList() : Collections.emptyList();
        // Merge the two diffs
        List<E> merged = new ArrayList<E>();

        mergeResult.setMerged(merged);

        Delta<E> deltaNext = nextElement(patchNext);
        Delta<E> deltaCurrent = nextElement(patchCurrent);

        // Before common ancestor
        if (deltaCurrent.getType() == Type.INSERT && deltaCurrent.getPrevious().getIndex() == 0
            && deltaNext.getType() == Type.INSERT && deltaNext.getPrevious().getIndex() == 0) {
            merged.addAll(or(deltaCurrent.getNext().getElements(), deltaNext.getNext().getElements()));
            deltaCurrent = nextElement(patchCurrent);
            deltaNext = nextElement(patchNext);
        } else {
            if (deltaCurrent.getType() == Type.INSERT && deltaCurrent.getPrevious().getIndex() == 0) {
                merged.addAll(deltaCurrent.getNext().getElements());
                deltaCurrent = nextElement(patchCurrent);
            }

            if (deltaNext.getType() == Type.INSERT && deltaNext.getPrevious().getIndex() == 0) {
                merged.addAll(deltaNext.getNext().getElements());
                deltaNext = nextElement(patchNext);
            }
        }

        // In common ancestor
        int index = 0;
        for (; index < commonAncestor.size(); ++index) {
            if (isPreviousIndex(deltaCurrent, index)) {
                // Modification in current
                if (isPreviousIndex(deltaNext, index)) {
                    // Modifications in both current and next at the same index
                    if (deltaNext.equals(deltaCurrent)) {
                        // Choose current
                        index = apply(deltaCurrent, merged, index);
                        if (deltaCurrent.getType() == Type.INSERT) {
                            merged.add(commonAncestor.get(index));
                        }
                    } else if (deltaCurrent.getType() == Type.INSERT) {
                        if (deltaNext.getType() == Type.INSERT) {
                            int newIndex = applyDecision(conflictDecisions, commonAncestor, deltaNext, deltaCurrent,
                                merged, index);
                            if (newIndex == Integer.MIN_VALUE) {
                                // Conflict
                                logConflict(mergeResult, deltaCurrent, deltaNext, commonAncestor, next, current, index);
                                index = fallback(commonAncestor, deltaNext, deltaCurrent, merged, index, configuration);
                            } else {
                                index = newIndex;
                            }
                            merged.add(commonAncestor.get(index));
                        } else {
                            index = apply(deltaCurrent, merged, index);
                            index = apply(deltaNext, merged, index);
                        }
                    } else if (deltaNext.getType() == Type.INSERT) {
                        index = apply(deltaNext, merged, index);
                        index = apply(deltaCurrent, merged, index);
                    } else {
                        int newIndex = applyDecision(conflictDecisions, commonAncestor, deltaNext, deltaCurrent,
                            merged, index);
                        if (newIndex == Integer.MIN_VALUE) {
                            // Conflict
                            logConflict(mergeResult, deltaCurrent, deltaNext, commonAncestor, next, current, index);
                            index = fallback(commonAncestor, deltaNext, deltaCurrent, merged, index, configuration);
                        } else {
                            index = newIndex;
                        }
                    }

                    deltaNext = nextElement(patchNext, index);
                } else {
                    if (deltaNext != null
                        && deltaCurrent.getPrevious().isOverlappingWith(deltaNext.getPrevious())) {
                        int newIndex = applyDecision(conflictDecisions, commonAncestor, deltaNext, deltaCurrent,
                            merged, index);
                        if (newIndex == Integer.MIN_VALUE) {
                            // Conflict
                            logConflict(mergeResult, deltaCurrent, deltaNext, commonAncestor, next, current, index);
                            index = fallback(commonAncestor, deltaNext, deltaCurrent, merged, index, configuration);
                        } else {
                            index = newIndex;
                        }
                        deltaNext = nextElement(patchNext, index);
                    } else {
                        index = apply(deltaCurrent, merged, index);
                        if (deltaCurrent.getType() == Type.INSERT) {
                            merged.add(commonAncestor.get(index));
                        }
                    }
                }

                deltaCurrent = nextElement(patchCurrent, index);
            } else if (isPreviousIndex(deltaNext, index)) {
                // Modification in next
                if (deltaCurrent != null
                    && deltaCurrent.getPrevious().isOverlappingWith(deltaNext.getPrevious())) {
                    int newIndex = applyDecision(conflictDecisions, commonAncestor, deltaNext, deltaCurrent,
                        merged, index);
                    if (newIndex == Integer.MIN_VALUE) {
                        // Conflict
                        logConflict(mergeResult, deltaCurrent, deltaNext, commonAncestor, next, current, index);
                        index = fallback(commonAncestor, deltaNext, deltaCurrent, merged, index, configuration);
                    } else {
                        index = newIndex;
                    }
                    deltaCurrent = nextElement(patchCurrent, index);
                } else {
                    index = apply(deltaNext, merged, index);
                    if (deltaNext.getType() == Type.INSERT) {
                        merged.add(commonAncestor.get(index));
                    }
                }

                deltaNext = nextElement(patchNext, index);
            } else {
                merged.add(commonAncestor.get(index));
            }
        }

        // After common ancestor
        if (deltaCurrent != null) {
            if (deltaCurrent.getType() == Type.INSERT) {
                merged.addAll(deltaCurrent.getNext().getElements());
            }

            if (deltaNext != null && !deltaCurrent.equals(deltaNext)) {
                merged.addAll(deltaNext.getNext().getElements());
            }
        } else if (deltaNext != null) {
            merged.addAll(deltaNext.getNext().getElements());
        }
    }

    private <E> List<E> or(List<E> previous, List<E> next) throws MergeException
    {
        DiffResult<E> diffCurrentResult;
        try {
            diffCurrentResult = diff(previous, next, null);
        } catch (DiffException e) {
            throw new MergeException("Faile to diff between two versions", e);
        }

        List<E> result = new ArrayList<E>(previous.size() + next.size());
        int index = 0;
        for (Delta<E> delta : diffCurrentResult.getPatch()) {
            if (delta.getPrevious().getIndex() > index) {
                result.addAll(previous.subList(index, delta.getPrevious().getIndex()));
            }

            if (delta.getType() != Type.INSERT) {
                result.addAll(delta.getPrevious().getElements());
            }
            if (delta.getType() != Type.DELETE) {
                result.addAll(delta.getNext().getElements());
            }

            index = delta.getPrevious().getLastIndex() + 1;
        }

        if (previous.size() > index) {
            result.addAll(previous.subList(index, previous.size()));
        }

        return result;
    }

    private <E> List<E> extractConflictPart(Delta<E> delta, List<E> previous, List<E> next, int chunkSize, int index)
    {
        int previousChangeSize, remainingChunkSize;

        switch (delta.getType()) {
            case DELETE:
                previousChangeSize = delta.getPrevious().size();
                remainingChunkSize = chunkSize - previousChangeSize;
                return (remainingChunkSize > 0) ?
                    previous.subList(index + previousChangeSize, index + remainingChunkSize) : Collections.emptyList();

            case CHANGE:
                return next.subList(index, index + Math.min(chunkSize, next.size()));

            case INSERT:
                int newIndex = Math.min(delta.getNext().getIndex(), index);
                int listSize = Math.min(chunkSize, delta.getNext().size());
                return next.subList(newIndex, newIndex + Math.min(listSize, next.size()));

            default:
                throw new IllegalArgumentException(
                    String.format("Cannot extract conflict part for unknown type [%s]", delta.getType()));
        }
    }

    private <E> void logConflict(DefaultMergeResult<E> mergeResult, Delta<E> deltaCurrent, Delta<E> deltaNext,
        List<E> previous, List<E> next, List<E> current, int index)
    {
        Delta<E> conflictDeltaCurrent, conflictDeltaNext;
        int chunkSize;
        List<E> subsetPrevious;

        if (deltaCurrent.getType() == Type.INSERT && deltaNext.getType() == Type.INSERT) {
            chunkSize = Math.max(deltaCurrent.getNext().size(), deltaNext.getNext().size());
            subsetPrevious = Collections.emptyList();
        } else {
            chunkSize = Math.max(deltaCurrent.getPrevious().size(), deltaNext.getPrevious().size());
            subsetPrevious = new ArrayList<>(previous.subList(index, index + Math.min(chunkSize, previous.size())));
        }

        List<E> subsetNext = new ArrayList<>(extractConflictPart(deltaNext, previous, next, chunkSize, index));
        List<E> subsetCurrent = new ArrayList<>(extractConflictPart(deltaCurrent, previous, current, chunkSize, index));

        // We might have found a conflict such as [a, b], [b, c], [d, c].
        // In that case we only want to record the conflict between b and d VS a.
        // We don't care about c since it's validated in both current and next versions.
        if (subsetPrevious.size() == subsetNext.size() && subsetPrevious.size() == subsetCurrent.size()) {
            for (int i = subsetNext.size() - 1; i > 0; i--) {
                if (subsetCurrent.get(i).equals(subsetNext.get(i))) {
                    subsetCurrent.remove(i);
                    subsetNext.remove(i);
                    subsetPrevious.remove(i);
                } else {
                    break;
                }
            }
        }

        Chunk<E> previousChunk = new DefaultChunk<>(index, subsetPrevious);
        Chunk<E> nextChunk = new DefaultChunk<>(index, subsetNext);
        Chunk<E> currentChunk = new DefaultChunk<>(index, subsetCurrent);

        conflictDeltaCurrent = DeltaFactory.createDelta(previousChunk, currentChunk, Type.CHANGE);
        conflictDeltaNext = DeltaFactory.createDelta(previousChunk, nextChunk, Type.CHANGE);
        mergeResult.getLog().error("Conflict between [{}] and [{}]", conflictDeltaCurrent, conflictDeltaNext);
        mergeResult.addConflict(new DefaultConflict<E>(index, conflictDeltaCurrent, conflictDeltaNext));
    }

    private <E> int apply(Delta<E> delta, List<E> merged, int currentIndex)
    {
        int index = currentIndex;

        switch (delta.getType()) {
            case DELETE:
                index = delta.getPrevious().getLastIndex();
                break;
            case INSERT:
                merged.addAll(delta.getNext().getElements());
                break;
            case CHANGE:
                merged.addAll(delta.getNext().getElements());
                index = delta.getPrevious().getLastIndex();
                break;
            default:
                break;
        }

        return index;
    }

    private <E> E nextElement(List<E> list)
    {
        return list != null && !list.isEmpty() ? list.remove(0) : null;
    }

    /**
     * Get the next element in the list and removed it from the list.
     * If the element last index is before the current index, then it can be ignored:
     * we already skipped this index, because of a fallback for example.
     * @param list the list of delta elements
     * @param index the current index
     * @param <E> the elements to be merged
     * @return a new delta that has been removed from the list or null if the list is now empty.
     */
    private <E> Delta<E> nextElement(List<Delta<E>> list, int index)
    {
        Delta<E> result = null;
        do {
            result = nextElement(list);
        } while (result != null && result.getPrevious().getLastIndex() <= index);
        return result;
    }

    private <E> boolean isPreviousIndex(Delta<E> delta, int index)
    {
        return delta != null && delta.getPrevious().getIndex() == index;
    }

    /**
     * Check if the content is completely different between the ancestor and the current version
     *
     * @param <E> the type of compared elements
     * @param commonAncestor previous version
     * @param patchCurrent patch to the current version
     * @return either or not the user has changed everything
     */
    private <E> boolean isFullyModified(List commonAncestor, Patch<E> patchCurrent)
    {
        return patchCurrent.size() == 1 && commonAncestor.size() == patchCurrent.get(0).getPrevious().size();
    }
}
