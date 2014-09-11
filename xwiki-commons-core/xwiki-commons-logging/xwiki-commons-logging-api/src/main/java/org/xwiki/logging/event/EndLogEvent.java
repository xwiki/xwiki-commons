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

import org.slf4j.Marker;
import org.xwiki.logging.LogLevel;
import org.xwiki.observation.event.EndEvent;

/**
 * The beginning of a group of logs.
 *
 * @version $Id$
 * @since 5.4RC1
 */
public class EndLogEvent extends LogEvent implements EndEvent
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Matches any {@link LogEvent}.
     */
    protected EndLogEvent()
    {

    }

    /**
     * @param logEvent the log event to copy
     */
    public EndLogEvent(LogEvent logEvent)
    {
        super(logEvent);
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     */
    public EndLogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray, Throwable throwable)
    {
        super(marker, level, message, argumentArray, throwable);
    }
}
