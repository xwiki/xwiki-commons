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

import org.xwiki.stability.Unstable;

/**
 * Represent a decision taken to solve a given {@link Conflict}.
 * @param <E> the type of element used in the merge operation.
 * @version $Id$
 * @since 11.7RC1
 */
@Unstable
public interface ConflictDecision<E>
{
    /**
     * @param type the decision made for this conflict.
     */
    void setType(DecisionType type);

    /**
     * Allow to specify a custom chunk. This method should automatically set the type to {@link DecisionType#CUSTOM}.
     * @param chunkElement the custom chunk to set for this decision.
     */
    void setCustom(List<E> chunkElement);

    /**
     * @return the decision made for this conflict.
     */
    DecisionType getType();

    /**
     * @return the appropriate chunk given the decision type. See {@link DecisionType} for more information.
     */
    Chunk<E> getChunk();

    /**
     * @return the conflict for which the decision is taken.
     */
    Conflict<E> getConflict();

    /**
     * The decision made for this conflict.
     */
    enum DecisionType
    {
        /**
         * The decision has not been made yet, the returned chunk might be null.
         */
        UNDECIDED,

        /**
         * Keep the previous version, the returned chunk should be the previous version.
         */
        PREVIOUS,

        /**
         * Keep the current version, the returned chunk should be the current version.
         */
        CURRENT,

        /**
         * Keep the next version, the returned chunk should be the next version.
         */
        NEXT,

        /**
         * Specify a custom version, the returned chunk is the one set by the user. If none is provided, can be null
         * or empty chunk.
         */
        CUSTOM
    }
}
