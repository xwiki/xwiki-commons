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

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.diff.Conflict;
import org.xwiki.diff.DiffResult;

/**
 * Displays a {@link DiffResult} as a <a href="http://en.wikipedia.org/wiki/Diff#Unified_format">unified diff</a>. The
 * unified diff consists in a sequence of blocks, each having elements marked as either added or removed, padded with
 * unmodified elements that put changes in context.
 *
 * @version $Id$
 * @since 4.1RC1
 */
@Role
public interface UnifiedDiffDisplayer
{
    /**
     * @param <E> the type of elements that are compared to produce the diff
     * @param <F> the type of sub-elements that can be compared to produce an in-line diff when an element is modified
     * @return the default configuration for this displayer
     */
    <E, F> UnifiedDiffConfiguration<E, F> getDefaultConfiguration();

    /**
     * Displays the given diff result as an unified diff using the default configuration.
     *
     * @param <E> the type of elements that are compared to produce the diff
     * @param <F> the type of sub-elements that can be compared to produce an in-line diff when an element is modified
     * @param diffResult the diff result
     * @return the list of blocks that form the unified diff
     * @see #display(DiffResult, UnifiedDiffConfiguration)
     */
    <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult);

    /**
     * Displays the given diff result as an unified diff using the default configuration and the list of conflicts.
     *
     * @param <E> the type of elements that are compared to produce the diff
     * @param <F> the type of sub-elements that can be compared to produce an in-line diff when an element is modified
     * @param diffResult the diff result
     * @param conflicts the {@link Conflict} to take into consideration for the display.
     * @return the list of blocks that form the unified diff
     * @see #display(DiffResult, UnifiedDiffConfiguration)
     * @since 11.7RC1
     */
    default <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult, List<Conflict<E>> conflicts)
    {
        return display(diffResult);
    }

    /**
     * Displays the given diff result as an unified diff using the provided configuration. An unified diff consists in a
     * list of blocks, each block grouping changes that are close to each other. The distance between two changes in a
     * block is less than {@code 2 * context size}, where context size represents the number of unmodified elements to
     * include before and after a change in order to place that change in context.
     * <p>
     * If the elements can be split in sub-elements, i.e. if a splitter is provided through the configuration, then the
     * unified diff displays also the changes inside the modified elements.
     * <p>
     * If changes are computed at the line level in a text, i.e. the elements that are compared to produce the diff are
     * lines of text, and a word splitter is provided through configuration then the following is a block from a unified
     * diff:
     *
     * <pre>
     * {@code @@ -81,5 +85,5 @@
     *  first line of context
     *  another unmodified line
     * -this line <del>has been removed</del>
     * +this line <ins>replaced the previous line</ins>
     *  close the block with unmodified lines
     *  last line of context}
     * </pre>
     *
     * @param <E> the type of elements that were compared to produce the diff
     * @param <F> the type of sub-elements that can be compared to produce an in-line diff when an element is modified
     * @param diffResult the diff result
     * @param config the configuration
     * @return the list of blocks that form the unified diff
     */
    <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult, UnifiedDiffConfiguration<E, F> config);

    /**
     * Displays the given diff result as an unified diff using the provided configuration. An unified diff consists in a
     * list of blocks, each block grouping changes that are close to each other. The distance between two changes in a
     * block is less than {@code 2 * context size}, where context size represents the number of unmodified elements to
     * include before and after a change in order to place that change in context.
     * <p>
     * If the elements can be split in sub-elements, i.e. if a splitter is provided through the configuration, then the
     * unified diff displays also the changes inside the modified elements.
     * <p>
     * If changes are computed at the line level in a text, i.e. the elements that are compared to produce the diff are
     * lines of text, and a word splitter is provided through configuration then the following is a block from a unified
     * diff:
     *
     * <pre>
     * {@code @@ -81,5 +85,5 @@
     *  first line of context
     *  another unmodified line
     * -this line <del>has been removed</del>
     * +this line <ins>replaced the previous line</ins>
     *  close the block with unmodified lines
     *  last line of context}
     * </pre>
     * <p>
     * If a change is part of a merge conflict, then the block elements contain the information for solving
     * the conflicts.
     *
     * @param <E> the type of elements that were compared to produce the diff
     * @param <F> the type of sub-elements that can be compared to produce an in-line diff when an element is modified
     * @param diffResult the diff result
     * @param config the configuration
     * @param conflicts the {@link Conflict} to take into consideration for the display.
     * @return the list of blocks that form the unified diff
     * @since 11.7RC1
     */
    default <E, F> List<UnifiedDiffBlock<E, F>> display(DiffResult<E> diffResult, List<Conflict<E>> conflicts,
        UnifiedDiffConfiguration<E, F> config)
    {
        return display(diffResult, config);
    }
}
