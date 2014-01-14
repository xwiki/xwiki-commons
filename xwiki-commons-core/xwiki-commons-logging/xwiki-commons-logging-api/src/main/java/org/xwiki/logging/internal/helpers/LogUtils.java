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
package org.xwiki.logging.internal.helpers;

import org.slf4j.Marker;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.BeginLogEvent;
import org.xwiki.logging.event.EndLogEvent;
import org.xwiki.logging.event.LogEvent;

/**
 * @version $Id$
 * @since 5.4RC1
 */
public final class LogUtils
{
    private LogUtils()
    {
        // Utility class
    }

    /**
     * Create and return a new {@link LogEvent} instance based on the passed parameters.
     * 
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @return the {@link LogEvent}
     */
    public static LogEvent newLogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray,
        Throwable throwable)
    {
        if (marker != null) {
            if (marker.contains(LogEvent.MARKER_BEGIN)) {
                return new BeginLogEvent(marker, level, message, argumentArray, throwable);
            } else if (marker.contains(LogEvent.MARKER_END)) {
                return new EndLogEvent(marker, level, message, argumentArray, throwable);
            }
        }

        return new LogEvent(marker, level, message, argumentArray, throwable);
    }
}
