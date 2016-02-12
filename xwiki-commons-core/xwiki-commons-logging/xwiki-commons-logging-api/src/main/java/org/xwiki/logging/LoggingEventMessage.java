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

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * Translatable {@link LoggingEvent}.
 *
 * @version $Id$
 * @since 8.0M2
 */
public class LoggingEventMessage extends Message implements LoggingEvent, Serializable
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
     * @see #getThreadName()
     */
    private String threadName;

    /**
     * @see #getLoggerName()
     */
    private String loggerName;

    /**
     * @see #getLevel()
     */
    private Level level;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was created.
     */
    private long timeStamp;

    /**
     * Matches any {@link LoggingEventMessage}.
     */
    public LoggingEventMessage()
    {

    }

    /**
     * @param logEvent the log event to copy
     */
    public LoggingEventMessage(LoggingEvent logEvent)
    {
        this(logEvent.getMarker(), logEvent.getLevel(), logEvent.getMessage(), logEvent.getArgumentArray(),
            logEvent.getThrowable(), logEvent.getTimeStamp(), logEvent.getLoggerName(), logEvent.getThreadName());
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @param loggerName the name of the logger where this log was sent
     */
    public LoggingEventMessage(Marker marker, Level level, String message, Object[] argumentArray, Throwable throwable,
        String loggerName)
    {
        this(marker, level, message, argumentArray, throwable, System.currentTimeMillis(), loggerName,
            Thread.currentThread().getName());
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param loggerName the name of the logger where this log was sent
     */
    public LoggingEventMessage(Marker marker, Level level, String message, Object[] argumentArray, String loggerName)
    {
        super(marker, message, argumentArray);

        this.loggerName = loggerName;
        this.threadName = Thread.currentThread().getName();
        this.level = level;
        this.timeStamp = System.currentTimeMillis();
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @param timeStamp the number of milliseconds elapsed from 1/1/1970 until logging event was created.
     * @param loggerName the name of the logger where this log was sent
     * @param threadName the name of the thread from where this log was sent
     */
    public LoggingEventMessage(Marker marker, Level level, String message, Object[] argumentArray, Throwable throwable,
        long timeStamp, String loggerName, String threadName)
    {
        super(marker, message, argumentArray, throwable);

        this.loggerName = loggerName;
        this.threadName = threadName;
        this.level = level;
        this.timeStamp = timeStamp;
    }

    /**
     * @param event the LoggingEvent containing the translation key
     * @return the translation key associated to the log
     * @since 8.0M2
     */
    public static String getTranslationKey(LoggingEvent event)
    {
        return getTranslationKey(event.getMarker());
    }

    // LoggingEvent

    @Override
    public String getThreadName()
    {
        return this.threadName;
    }

    @Override
    public String getLoggerName()
    {
        return this.loggerName;
    }

    @Override
    public Level getLevel()
    {
        return this.level;
    }

    @Override
    public long getTimeStamp()
    {
        return this.timeStamp;
    }

    // Object

    @Override
    public String toString()
    {
        return getLevel().toString() + ':' + super.toString();
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

        LoggingEventMessage rhs = (LoggingEventMessage) object;
        return getLevel() == rhs.getLevel();
    }
}
