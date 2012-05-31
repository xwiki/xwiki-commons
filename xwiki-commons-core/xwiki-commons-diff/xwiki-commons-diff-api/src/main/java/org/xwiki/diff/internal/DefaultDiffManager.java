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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.diff.Delta;
import org.xwiki.diff.Delta.Type;
import org.xwiki.diff.DiffConfiguration;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeException;
import org.xwiki.diff.MergeResult;
import org.xwiki.diff.Patch;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;

import difflib.DiffUtils;
import difflib.PatchFailedException;

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
                patch.add(new InsertDelta<E>(new DefaultChunk<E>(0, Collections.<E> emptyList()), new DefaultChunk<E>(
                    0, next)));
            }
        } else if (next == null || next.isEmpty()) {
            patch = new DefaultPatch<E>();
            patch.add(new DeleteDelta<E>(new DefaultChunk<E>(0, previous), new DefaultChunk<E>(0, Collections
                .<E> emptyList())));
        } else {
            patch = new DefaultPatch<E>(DiffUtils.diff(previous, next));
        }

        result.setPatch(patch);

        return result;
    }

    /**
     * @param <E> the type of compared elements
     * @param mergeResult the result of the merge
     * @param message the error message
     * @param throwable the error exception
     * @param arguments the error message arguments
     */
    private <E> void error(MergeResult<E> mergeResult, String message, Throwable throwable, Object... arguments)
    {
        mergeResult.getLog().add(new LogEvent(LogLevel.ERROR, message, arguments, throwable));
    }

    /**
     * @param <E> the type of compared elements
     * @param mergeResult the result of the merge
     * @param message the warning message
     * @param throwable the warning exception
     * @param arguments the warning message arguments
     */
    private <E> void warn(MergeResult<E> mergeResult, String message, Throwable throwable, Object... arguments)
    {
        mergeResult.getLog().add(new LogEvent(LogLevel.WARN, message, arguments, throwable));
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
                warn(mergeResult, "The modification was already applied", null);
            } else {
                // The current version has been replaced by an empty string
                error(mergeResult, "The current value is empty", null);
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
            } else {
                merge(mergeResult, commonAncestor, patchNext, patchCurrent, configuration);
            }
        }

        return mergeResult;
    }

    /**
     * @param <E> the type of compared elements
     * @param mergeResult the result of the merge
     * @param commonAncestor the common ancestor of the two versions of the content to compare
     * @param patchNext the diff between common ancestor and next version
     * @param patchCurrent the diff between common ancestor and current version
     * @param configuration the configuration of the merge behavior
     */
    private <E> void merge(DefaultMergeResult<E> mergeResult, List<E> commonAncestor, Patch<E> patchNext,
        Patch<E> patchCurrent, MergeConfiguration<E> configuration)
    {
        // Merge the two diffs
        List<E> merged = new ArrayList<E>();
        
        mergeResult.setMerged(merged);

        Delta<E> deltaNext = nextElement(patchNext);
        Delta<E> deltaCurrent = nextElement(patchCurrent);

        // Before common ancestor
        if (deltaCurrent.getType() == Type.INSERT && deltaCurrent.getPrevious().getIndex() == 0) {
            merged.addAll(deltaCurrent.getNext().getElements());
            deltaCurrent = nextElement(patchCurrent);
            if (deltaNext.getType() == Type.INSERT && deltaNext.getPrevious().getIndex() == 0) {
                logConflict(mergeResult, deltaCurrent, deltaNext);
                deltaNext = nextElement(patchNext);
            }
        } else if (deltaNext.getType() == Type.INSERT && deltaNext.getPrevious().getIndex() == 0) {
            merged.addAll(deltaNext.getNext().getElements());
            deltaNext = nextElement(patchNext);
        }

        // In common ancestor
        int index = 0;
        for (; index < commonAncestor.size(); ++index) {
            if (isPreviousIndex(deltaCurrent, index)) {
                index = apply(deltaCurrent, merged, index);

                if (isInPreviousDelta(deltaNext, deltaCurrent.getPrevious().getLastIndex())) {
                    logConflict(mergeResult, deltaCurrent, deltaNext);
                    deltaNext = nextElement(patchNext);
                }
            } else if (isPreviousIndex(deltaNext, index)) {
                if (isInPreviousDelta(deltaCurrent, deltaNext.getPrevious().getLastIndex())) {
                    logConflict(mergeResult, deltaCurrent, deltaNext);
                    deltaNext = nextElement(patchNext);
                } else {
                    index = apply(deltaNext, merged, index);
                }
            } else {
                merged.add(commonAncestor.get(index));
            }
        }

        // After common ancestor
        if (deltaCurrent != null) {
            merged.addAll(deltaCurrent.getNext().getElements());
            if (deltaNext != null) {
                logConflict(mergeResult, deltaCurrent, deltaNext);
            }
        } else if (deltaNext != null) {
            merged.addAll(deltaNext.getNext().getElements());
        }
    }
    
    private <E> void logConflict(DefaultMergeResult<E> mergeResult, Delta<E> deltaCurrent, Delta<E> deltaNext)
    {
        error(mergeResult, "Conflict between [{}] and [{}]", null, deltaCurrent, deltaNext);
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

    private <E> boolean isInPreviousDelta(Delta<E> delta, int index)
    {
        return delta != null && delta.getPrevious().getIndex() <= index && delta.getPrevious().getIndex() >= index;
    }

    /**
     * @param <E> the type of compared elements
     * @param commonAncestor the common ancestor of the two versions of the content to compare
     * @param next the next version of the content to compare
     * @param current the current version of the content to compare
     * @param configuration the configuration of the merge behavior
     * @param mergeResult the result of the merge
     */
    // TODO: improve the algo and get rid of String special case, just applying a patch on the current version does not
    // always give good result
    public <E> void merge(List<E> commonAncestor, List<E> next, List<E> current, MergeConfiguration<E> configuration,
        DefaultMergeResult<E> mergeResult)
    {

        difflib.Patch patch = DiffUtils.diff(commonAncestor, next);

        try {
            List<E> result = (List<E>) patch.applyTo(current);

            mergeResult.setMerged(result);
        } catch (PatchFailedException e) {
            error(mergeResult, "Failed to apply differences between [{}] and [{}] on current list [{}]", e,
                commonAncestor, next, current);
        }
    }

    /**
     * @param list the lines
     * @return the multilines text
     */
    private String toString(List<String> list)
    {
        return StringUtils.join(list, '\n');
    }

    /**
     * @param str the multilines text
     * @return the lines
     */
    private List<String> toLines(String str)
    {
        try {
            return IOUtils.readLines(new StringReader(str));
        } catch (IOException e) {
            // Should never happen
            return null;
        }
    }
}
