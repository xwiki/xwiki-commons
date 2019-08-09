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
package org.xwiki.diff;

import java.util.List;

import org.xwiki.component.annotation.Role;

/**
 * Various diff/merge tools.
 *
 * @version $Id$
 */
@Role
public interface DiffManager
{
    /**
     * Produce a diff between the two provided versions.
     *
     * @param <E> the type of compared elements
     * @param previous the previous version of the content to compare
     * @param next the next version of the content to compare
     * @param configuration the configuration of the diff behavior
     * @return the result of the diff
     * @throws DiffException error when executing the diff
     */
    <E> DiffResult<E> diff(List<E> previous, List<E> next, DiffConfiguration<E> configuration) throws DiffException;

    /**
     * Execute a 3-way merge on provided versions.
     * If a conflict is detected during the merge, no error is triggered and the returned {@link MergeResult} object
     * always has a result (see {@link MergeResult#getMerged()}): the conflict is automatically fixed with a fallback
     * defined by the {@link MergeConfiguration}.
     *
     * If the {@link MergeConfiguration} instance contains some {@link ConflictDecision}
     * (see {@link MergeConfiguration#setConflictDecisionList(List)}), then those decisions are taken into account
     * to solve the conflict. The decision linked to the conflict is retrieved and applied, unless the decision state
     * is still {@link org.xwiki.diff.ConflictDecision.DecisionType#UNDECIDED}. When a decision is used to solve a
     * conflict, the conflict is not recorded in the {@link MergeResult}.
     *
     * If the decision is {@link org.xwiki.diff.ConflictDecision.DecisionType#UNDECIDED}, or if no decision is available
     * for this conflict, then the fallback version defined in the {@link MergeConfiguration}
     * (see {@link MergeConfiguration#setFallbackOnConflict(MergeConfiguration.Version)}) is used to fix the conflict,
     * but in that case the conflict is recorded in the returned {@link MergeResult}.
     *
     * Finally the configuration parameter accepts a null value: in that case, the fallback version is always the
     * current version.
     *
     * @param <E> the type of compared elements
     * @param commonAncestor the common ancestor of the two versions of the content to compare
     * @param next the next version of the content to compare
     * @param current the current version of the content to compare
     * @param configuration the configuration of the merge behavior
     * @return the result of the merge
     * @throws MergeException error when executing the merge
     */
    <E> MergeResult<E> merge(List<E> commonAncestor, List<E> next, List<E> current, MergeConfiguration<E> configuration)
        throws MergeException;
}
