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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.Conflict;
import org.xwiki.diff.ConflictDecision;
import org.xwiki.stability.Unstable;

/**
 * Default implementation of a {@link ConflictDecision}.
 * @param <E> the type of element used in the merge.
 * @version $Id$
 * @since 11.7RC1
 */
@Unstable
public class DefaultConflictDecision<E> implements ConflictDecision<E>
{
    private DecisionType type;

    private Chunk<E> chunk;

    private Conflict<E> conflict;

    /**
     * Default constructor, set the type to {@link DecisionType#UNDECIDED}.
     * @param conflict the {@link Conflict} for which a decision has to be taken.
     */
    public DefaultConflictDecision(Conflict conflict)
    {
        this.conflict = conflict;
        setType(DecisionType.UNDECIDED);
    }

    @Override
    public void setType(DecisionType type)
    {
        this.type = type;
        switch (type) {
            case NEXT:
                this.chunk = conflict.getDeltaNext().getNext();
                break;

            case CURRENT:
                this.chunk = conflict.getDeltaCurrent().getNext();
                break;

            case PREVIOUS:
                this.chunk = conflict.getDeltaCurrent().getPrevious();
                break;

            case CUSTOM:
                this.chunk = new DefaultChunk<E>(conflict.getIndex(), Collections.emptyList());
                break;

            case UNDECIDED:
            default:
                this.chunk = null;
                break;
        }
    }

    @Override
    public void setCustom(List<E> chunkElement)
    {
        this.type = DecisionType.CUSTOM;
        this.chunk = new DefaultChunk<E>(conflict.getIndex(), chunkElement);
    }

    @Override
    public DecisionType getType()
    {
        return type;
    }

    @Override
    public Chunk<E> getChunk()
    {
        return chunk;
    }

    @Override
    public Conflict<E> getConflict()
    {
        return conflict;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != getClass()) {
            return false;
        }

        DefaultConflictDecision otherDecision = (DefaultConflictDecision) object;
        return new EqualsBuilder()
            .append(type, otherDecision.type)
            .append(chunk, otherDecision.chunk)
            .append(conflict, otherDecision.conflict)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(7, 34)
            .append(type)
            .append(chunk)
            .append(conflict)
            .toHashCode();
    }
}
