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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.Message;
import org.xwiki.observation.event.Event;

/**
 * Sent when logger is called.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the name of the associated logger</li>
 * <li>data: null</li>
 * </ul>
 *
 * @version $Id$
 * @since 3.2M1
 */
public class LogEvent extends Message implements Event
{
    /**
     * The marker to use to indicate that we start a group of logs.
     */
    public static final Marker MARKER_BEGIN = MarkerFactory.getMarker("xwiki.begin");

    /**
     * The marker to use to indicate that we stop a group of logs.
     */
    public static final Marker MARKER_END = MarkerFactory.getMarker("xwiki.end");

    /**
     * @see #getLevel()
     */
    private LogLevel level;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was created.
     */
    private long timeStamp;

    /**
     * Matches any {@link LogEvent}.
     */
    public LogEvent()
    {

    }

    /**
     * @param logEvent the log event to copy
     * @since 5.4M1
     */
    public LogEvent(LogEvent logEvent)
    {
        this(logEvent.getMarker(), logEvent.getLevel(), logEvent.getMessage(), logEvent.getArgumentArray(),
            logEvent.getThrowable(), logEvent.getTimeStamp());
    }

    /**
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     */
    public LogEvent(LogLevel level, String message, Object[] argumentArray, Throwable throwable)
    {
        this(null, level, message, argumentArray, throwable);
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @since 4.3M
     */
    public LogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray, Throwable throwable)
    {
        this(marker, level, message, argumentArray, throwable, System.currentTimeMillis());
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @param timeStamp the number of milliseconds elapsed from 1/1/1970 until logging event was created.
     * @since 6.4M1
     */
    public LogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray, Throwable throwable,
        long timeStamp)
    {
        super(marker, message, argumentArray, throwable);

        this.level = level;
        this.timeStamp = timeStamp;
    }

    /**
     * Copy the stored log into a passed {@link Logger}.
     *
     * @param targetLogger the logger where to copy the stored log
     * @since 5.3M1
     */
    public void log(Logger targetLogger)
    {
        Object[] argumentArray = getArgumentArray();
        if (getArgumentArray() == null) {
            log(targetLogger, getLevel(), getMarker(), getMessage(), getThrowable());
        } else {
            if (getThrowable() != null) {
                argumentArray = ArrayUtils.add(getArgumentArray(), getThrowable());
            }

            log(targetLogger, getLevel(), getMarker(), getMessage(), argumentArray);
        }
    }

    private static void log(Logger targetLogger, LogLevel level, Marker marker, String message, Object[] argumentArray)
    {
        switch (level) {
            case TRACE:
                targetLogger.trace(marker, message, argumentArray);
                break;
            case DEBUG:
                targetLogger.debug(marker, message, argumentArray);
                break;
            case INFO:
                targetLogger.info(marker, message, argumentArray);
                break;
            case WARN:
                targetLogger.warn(marker, message, argumentArray);
                break;
            case ERROR:
                targetLogger.error(marker, message, argumentArray);
                break;
            default:
                break;
        }
    }

    private static void log(Logger targetLogger, LogLevel level, Marker marker, String message, Throwable throwable)
    {
        switch (level) {
            case TRACE:
                targetLogger.trace(marker, message, throwable);
                break;
            case DEBUG:
                targetLogger.debug(marker, message, throwable);
                break;
            case INFO:
                targetLogger.info(marker, message, throwable);
                break;
            case WARN:
                targetLogger.warn(marker, message, throwable);
                break;
            case ERROR:
                targetLogger.error(marker, message, throwable);
                break;
            default:
                break;
        }
    }

    /**
     * @return the log level
     */
    public LogLevel getLevel()
    {
        return this.level;
    }

    /**
     * @return the number of milliseconds elapsed from 1/1/1970 until logging event was created
     */
    public long getTimeStamp()
    {
        return this.timeStamp;
    }

    // Event

    @Override
    public boolean matches(Object otherEvent)
    {
        return true;
    }

    // Object

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(getLevel());
        builder.append(':');
        builder.append(super.toString());

        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(7, 11).append(getLevel()).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if (!super.equals(object)) {
            return false;
        }

        LogEvent rhs = (LogEvent) object;
        return getLevel() == rhs.getLevel();
    }
}
