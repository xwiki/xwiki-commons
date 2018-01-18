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
                patch.add(new InsertDelta<E>(new DefaultChunk<E>(0, Collections.<E>emptyList()), new DefaultChunk<E>(
                    0, next)));
            }
        } else if (next == null || next.isEmpty()) {
            patch = new DefaultPatch<E>();
            patch.add(new DeleteDelta<E>(new DefaultChunk<E>(0, previous), new DefaultChunk<E>(0, Collections
                .<E>emptyList())));
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

        if (current.isEmpty()) {
            // Empty current version
            if (commonAncestor.isEmpty()) {
                mergeResult.setMerged(next);
            } else if (next.isEmpty()) {
                // The new modification was already applied
                mergeResult.getLog().warn("The modification was already applied");
            } else {
                // The current version has been replaced by an empty string
                mergeResult.getLog().error("The current value is empty");
            }
        } else {
            // Get diff between common ancestor and current version
            DiffResult<E> diffCurrentResult;
            try {
                diffCurrentResult = diff(commonAncestor, current, null);
            } catch (DiffException e) {
                throw new MergeException("Faile to diff between common ancestor and current version", e);
            }
            mergeResult.getLog().addAll(diffCurrentResult.getLog());

            Patch<E> patchCurrent = diffCurrentResult.getPatch();

            if (patchCurrent.isEmpty()) {
                mergeResult.setMerged(next);
            } else if (isFullyModified(commonAncestor, patchCurrent)) {
                // If current is completely modified compared to the common ancestor we assume any change in next is
                // a conflict
                // ... except if the current content is similar to the next one!
                if (!current.equals(next)) {
                    Delta<E> deltaNext = nextElement(patchNext);
                    Delta<E> deltaCurrent = nextElement(patchCurrent);
                    logConflict(mergeResult, deltaCurrent, deltaNext);
                }
                mergeResult.setMerged(fallback(commonAncestor, next, current, configuration));
            } else {
                merge(mergeResult, commonAncestor, patchNext, patchCurrent, configuration);
            }
        }

        return mergeResult;
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
        int newIndex = currentIndex;

        Version fallbackVersion;
        if (configuration != null) {
            fallbackVersion = configuration.getFallbackOnConflict();
        } else {
            fallbackVersion = Version.CURRENT;
        }

        switch (fallbackVersion) {
            case NEXT:
                newIndex = apply(deltaNext, merged, currentIndex);
                break;
            case PREVIOUS:
                for (; newIndex < deltaNext.getPrevious().getIndex(); ++newIndex) {
                    merged.add(commonAncestor.get(newIndex));
                }
                for (; newIndex < deltaCurrent.getPrevious().getIndex(); ++newIndex) {
                    merged.add(commonAncestor.get(newIndex));
                }
                break;
            default:
                // CURRENT is the default
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
    private <E> void merge(DefaultMergeResult<E> mergeResult, List<E> commonAncestor, Patch<E> patchNext,
        Patch<E> patchCurrent, MergeConfiguration<E> configuration) throws MergeException
    {
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
                            // Conflict
                            logConflict(mergeResult, deltaCurrent, deltaNext);

                            merged.addAll(or(deltaNext.getNext().getElements(), deltaCurrent.getNext().getElements()));
                            merged.add(commonAncestor.get(index));
                        } else {
                            index = apply(deltaCurrent, merged, index);
                            index = apply(deltaNext, merged, index);
                        }
                    } else if (deltaNext.getType() == Type.INSERT) {
                        index = apply(deltaNext, merged, index);
                        index = apply(deltaCurrent, merged, index);
                    } else {
                        // Conflict
                        logConflict(mergeResult, deltaCurrent, deltaNext);

                        index = fallback(commonAncestor, deltaNext, deltaCurrent, merged, index, configuration);
                    }

                    deltaNext = nextElement(patchNext);
                } else {
                    index = apply(deltaCurrent, merged, index);
                    if (deltaCurrent.getType() == Type.INSERT) {
                        merged.add(commonAncestor.get(index));
                    }

                    if (deltaNext != null
                        && deltaNext.getPrevious().getIndex() <= deltaCurrent.getPrevious().getLastIndex()) {
                        // Conflict
                        logConflict(mergeResult, deltaCurrent, deltaNext);
                        deltaNext = nextElement(patchNext);
                    }
                }

                deltaCurrent = nextElement(patchCurrent);
            } else if (isPreviousIndex(deltaNext, index)) {
                // Modification in next
                index = apply(deltaNext, merged, index);
                if (deltaNext.getType() == Type.INSERT) {
                    merged.add(commonAncestor.get(index));
                }

                if (deltaCurrent != null
                    && deltaCurrent.getPrevious().getIndex() <= deltaNext.getPrevious().getLastIndex()) {
                    // Conflict
                    logConflict(mergeResult, deltaCurrent, deltaNext);
                    deltaCurrent = nextElement(patchCurrent);
                }

                deltaNext = nextElement(patchNext);
            } else {
                merged.add(commonAncestor.get(index));
            }
        }

        // After common ancestor
        if (deltaCurrent != null) {
            merged.addAll(deltaCurrent.getNext().getElements());

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

    private <E> void logConflict(DefaultMergeResult<E> mergeResult, Delta<E> deltaCurrent, Delta<E> deltaNext)
    {
        mergeResult.getLog().error("Conflict between [{}] and [{}]", deltaCurrent, deltaNext);
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
    private <E> boolean isFullyModified(List commonAncestor, Patch<E> patchCurrent) {
        return patchCurrent.size() == 1 && commonAncestor.size() == patchCurrent.get(0).getPrevious().size();
    }
}
