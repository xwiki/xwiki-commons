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
package org.xwiki.logging.event;

import org.slf4j.event.LoggingEvent;
import org.xwiki.observation.event.Event;

/**
 * Sent when logger is called. Immutable implementation of {@link LoggingEvent}.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the name of the associated logger</li>
 * <li>data: null</li>
 * </ul>
 *
 * @version $Id$
 * @since 8.0M2
 */
public class LoggingEventEvent implements Event
{
    private LoggingEvent loggingEvent;

    /**
     * Matches any {@link LoggingEventEvent}.
     */
    public LoggingEventEvent()
    {

    }

    /**
     * @param loggingEvent the log event to send
     */
    public LoggingEventEvent(LoggingEvent loggingEvent)
    {
        this.loggingEvent = loggingEvent;
    }

    /**
     * @return the {@link LoggingEvent}
     */
    public LoggingEvent getLoggingEvent()
    {
        return this.loggingEvent;
    }

    // Event

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof LoggingEventEvent;
    }

    // Object

    @Override
    public String toString()
    {
        return this.loggingEvent.toString();
    }
}
