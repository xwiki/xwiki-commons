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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.diff.Conflict;
import org.xwiki.diff.Delta;
import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Default implementation of a {@link Conflict}.
 * @param <E> the type of element used for the merge operation.
 * @version $Id$
 * @since 11.7RC1
 */
@Unstable
public class DefaultConflict<E> implements Conflict<E>
{
    private int index;

    private Delta<E> deltaCurrent;

    private Delta<E> deltaNext;

    /**
     * Default constructor.
     * @param index where the conflict happened during the merge
     * @param current the {@link Delta} current version
     * @param next the {@link Delta} next version
     */
    public DefaultConflict(int index, Delta<E> current, Delta<E> next)
    {
        this.index = index;
        this.deltaCurrent = current;
        this.deltaNext = next;
    }

    @Override
    public int getIndex()
    {
        return index;
    }

    @Override
    public Delta<E> getDeltaCurrent()
    {
        return deltaCurrent;
    }

    @Override
    public Delta<E> getDeltaNext()
    {
        return deltaNext;
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

        DefaultConflict otherConflict = (DefaultConflict) object;
        return new EqualsBuilder()
            .append(index, otherConflict.index)
            .append(deltaCurrent, otherConflict.deltaCurrent)
            .append(deltaNext, otherConflict.deltaNext)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(23, 13)
            .append(index)
            .append(deltaCurrent)
            .append(deltaNext)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);
        builder.append("Index", index);
        builder.append("Delta Current", deltaCurrent);
        builder.append("Delta Next", deltaNext);
        return builder.toString();
    }
}
