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
package org.xwiki.observation.event;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.observation.event.filter.AlwaysMatchingEventFilter;
import org.xwiki.observation.event.filter.EventFilter;
import org.xwiki.observation.event.filter.FixedNameEventFilter;

/**
 * An helper to extend for all Events associated to a single {@link String} value.
 * <p>
 * If the event value is not a {@link String} extending this class should be avoided.
 *
 * @version $Id$
 * @since 2.4M2
 */
public abstract class AbstractFilterableEvent implements FilterableEvent, Serializable
{
    /**
     * The version identifier for this Serializable class. Increment only if the <i>serialized</i> form of the class
     * changes.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A filter for comparing document names, used in {@link #matches(Object)}.
     */
    private EventFilter eventFilter;

    /**
     * Constructor initializing the event filter with an
     * {@link org.xwiki.observation.event.filter.AlwaysMatchingEventFilter}, meaning that this event will match any
     * other event of the same type.
     */
    public AbstractFilterableEvent()
    {
        this.eventFilter = AlwaysMatchingEventFilter.INSTANCE;
    }

    /**
     * Constructor initializing the event filter with a {@link org.xwiki.observation.event.filter.FixedNameEventFilter},
     * meaning that this event will match only events of the same type affecting the same passed name.
     *
     * @param name a generic name that uniquely identifies an event type
     */
    public AbstractFilterableEvent(String name)
    {
        // Assume null means everything
        this.eventFilter = name != null ? new FixedNameEventFilter(name) : AlwaysMatchingEventFilter.INSTANCE;
    }

    /**
     * Constructor using a custom {@link EventFilter}.
     *
     * @param eventFilter the filter to use for matching events
     */
    public AbstractFilterableEvent(EventFilter eventFilter)
    {
        this.eventFilter = eventFilter;
    }

    @Override
    public EventFilter getEventFilter()
    {
        return this.eventFilter;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This type of events match only events of the same type, and only if the internal {@link #eventFilter}s also
     * {@link EventFilter#matches(EventFilter)} match.
     * </p>
     *
     * @see Event#matches(Object)
     * @see EventFilter#matches(EventFilter)
     */
    @Override
    public boolean matches(Object otherEvent)
    {
        if (otherEvent == this) {
            return true;
        }

        boolean isMatching = false;

        if (this.getClass().isAssignableFrom(otherEvent.getClass())) {
            isMatching = getEventFilter().matches(((AbstractFilterableEvent) otherEvent).getEventFilter());
        }

        return isMatching;
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

        FilterableEvent rhs = (FilterableEvent) object;
        return Objects.equals(getEventFilter(), rhs.getEventFilter());
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(3, 125).append(getEventFilter()).toHashCode();
    }
}
