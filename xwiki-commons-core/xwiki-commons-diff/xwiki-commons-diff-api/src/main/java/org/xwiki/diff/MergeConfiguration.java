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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Setup merge behavior.
 *
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class MergeConfiguration<E> extends HashMap<String, Object>
{
    /**
     * The name of the key used to setup the default fallback to use when finding a conflict.
     */
    public static final String KEY_FALLBACKONCONFLICT = "fallbackonconflict";

    private static final long serialVersionUID = 1L;

    private List<ConflictDecision<E>> conflictDecisionList;

    /**
     * Create a merge configuration with the given list of decisions and a fallback to current version by default.
     * @param conflictDecisions the decisions to be taken in case of conflict.
     * @since 11.7RC1
     */
    public MergeConfiguration(List<ConflictDecision<E>> conflictDecisions)
    {
        this(Version.CURRENT, conflictDecisions);
    }

    /**
     * Crate a merge configuration with the given fallback version and the given conflict decisions to be taken.
     * @param version the fallback version.
     * @param conflictDecisions the decisions to be taken in case of conflict.
     * @since 11.7RC1
     */
    public MergeConfiguration(Version version, List<ConflictDecision<E>> conflictDecisions)
    {
        setFallbackOnConflict(version);
        setConflictDecisionList(conflictDecisions);
    }

    /**
     * Create a default merge configuration without any decisions and with a fallback on current version by default.
     */
    public MergeConfiguration()
    {
        this(new ArrayList<>());
    }

    /**
     * One of the merged versions.
     *
     * @version $Id$
     */
    public enum Version
    {
        /**
         * The common ancestor.
         */
        PREVIOUS,

        /**
         * The new version.
         */
        NEXT,

        /**
         * The current version.
         */
        CURRENT
    }

    /**
     * @param version the version to fallback on when finding a conflict. Default is {@link Version#CURRENT}.
     */
    public void setFallbackOnConflict(Version version)
    {
        put(KEY_FALLBACKONCONFLICT, version);
    }

    /**
     * @return the version to fallback on when finding a conflict. Default is {@link Version#CURRENT}.
     */
    public Version getFallbackOnConflict()
    {
        // Default is Version.NEXT
        return containsKey(KEY_FALLBACKONCONFLICT) ? (Version) get(KEY_FALLBACKONCONFLICT) : Version.CURRENT;
    }

    /**
     * @return the list of decisions to be taken in case of conflicts.
     * @since 11.7RC1
     */
    public List<ConflictDecision<E>> getConflictDecisionList()
    {
        return conflictDecisionList;
    }

    /**
     * Set the list of conflict decision to be taken.
     * This list of decision is consumed in {@link DiffManager#merge(List, List, List, MergeConfiguration)}.
     * @param conflictDecisionList the new conflict decision list.
     * @since 11.7RC1
     */
    public void setConflictDecisionList(List<ConflictDecision<E>> conflictDecisionList)
    {
        this.conflictDecisionList = new ArrayList<>(conflictDecisionList);
    }
}
