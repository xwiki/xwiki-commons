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
package org.xwiki.logging;

import java.util.Arrays;

import org.slf4j.Marker;
import org.xwiki.logging.event.BeginLogEvent;
import org.xwiki.logging.event.EndLogEvent;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.internal.helpers.MessageParser;
import org.xwiki.logging.internal.helpers.MessageParser.MessageElement;
import org.xwiki.logging.internal.helpers.MessageParser.MessageIndex;
import org.xwiki.stability.Unstable;

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
        return newLogEvent(marker, level, message, argumentArray, throwable, System.currentTimeMillis());
    }

    /**
     * Create and return a new {@link LogEvent} instance based on the passed parameters.
     *
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @param timeStamp the number of milliseconds elapsed from 1/1/1970 until logging event was created.
     * @return the {@link LogEvent}
     * @since 6.4M1
     */
    public static LogEvent newLogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray,
        Throwable throwable, long timeStamp)
    {
        if (marker != null) {
            if (marker.contains(LogEvent.MARKER_BEGIN)) {
                return new BeginLogEvent(marker, level, message, argumentArray, throwable, timeStamp);
            } else if (marker.contains(LogEvent.MARKER_END)) {
                return new EndLogEvent(marker, level, message, argumentArray, throwable, timeStamp);
            }
        }

        return new LogEvent(marker, level, message, argumentArray, throwable, timeStamp);
    }

    /**
     * Create and return a new {@link LogEvent} instance based on the passed parameters.
     *
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param arguments the event arguments to insert in the message and/or the {@link Throwable} associated with the
     *            message
     * @return the {@link LogEvent}
     * @since 14.0RC1
     */
    @Unstable
    public static LogEvent newLogEvent(Marker marker, LogLevel level, String message, Object[] arguments)
    {
        Object[] actualArray;
        Throwable throwable;
        if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            actualArray = Arrays.copyOf(arguments, arguments.length - 1);
            throwable = (Throwable) arguments[arguments.length - 1];
        } else {
            actualArray = arguments;
            throwable = null;
        }

        return newLogEvent(marker, level, message, actualArray, throwable);
    }

    /**
     * Translate the passed {@link LogEvent} based on the passed translation message pattern.
     * <p>
     * The translation message pattern use the same syntax than standard message pattern except that it's optionally
     * possible to provide a custom index as in <code>Some {1} translation {0} message</code> in order to modify the
     * order of the argument which can be required depending on the language.
     *
     * @param logEvent the {@link LogEvent} to translate
     * @param translatedMessage the translated version of the {@link LogEvent} message
     * @return the translated version of the passed {@link LogEvent}
     */
    public static LogEvent translate(LogEvent logEvent, String translatedMessage)
    {
        if (translatedMessage != null) {
            MessageParser parser = new MessageParser(translatedMessage, true);

            Object[] defaultArguments = logEvent.getArgumentArray();
            Object[] arguments = new Object[defaultArguments.length];
            StringBuilder message = new StringBuilder();

            int index = 0;
            for (MessageElement element = parser.next(); element != null; element = parser.next()) {
                if (element instanceof MessageIndex) {
                    message.append(MessageParser.ARGUMENT_STR);
                    arguments[index++] = defaultArguments[((MessageIndex) element).getIndex()];
                } else {
                    message.append(element.getString());
                }
            }

            for (; index < arguments.length; ++index) {
                arguments[index] = defaultArguments[index];
            }

            return new LogEvent(logEvent.getMarker(), logEvent.getLevel(), message.toString(), arguments,
                logEvent.getThrowable(), logEvent.getTimeStamp());
        }

        return logEvent;
    }
}
