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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.xwiki.logging.event.LogEvent;

/**
 * A queue of {@link LogEvent}s.
 * 
 * @version $Id$
 * @since 3.2M3
 */
public class LogQueue extends ConcurrentLinkedQueue<LogEvent> implements Logger
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @return the created {@link LogEvent} instance
     * @since 4.1RC1
     */
    public LogEvent addLogEvent(LogLevel level, String message, Object[] argumentArray)
    {
        Object[] actualArray;
        Throwable throwable;
        if (argumentArray.length > 0 && argumentArray[argumentArray.length - 1] instanceof Throwable) {
            actualArray = Arrays.copyOf(argumentArray, argumentArray.length - 1);
            throwable = (Throwable) argumentArray[argumentArray.length - 1];
        } else {
            actualArray = argumentArray;
            throwable = null;
        }

        return addLogEvent(level, message, actualArray, throwable);
    }

    /**
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @return the created {@link LogEvent} instance
     * @since 4.1RC1
     */
    public LogEvent addLogEvent(LogLevel level, String message, Object[] argumentArray, Throwable throwable)
    {
        LogEvent logEvent = new LogEvent(level, message, argumentArray, throwable);
        add(logEvent);

        return logEvent;
    }

    /**
     * Filter logs of a specific level.
     * 
     * @param level the level of the logs to return
     * @return the filtered logs
     * @since 4.1RC1
     */
    public List<LogEvent> getLogs(LogLevel level)
    {
        List<LogEvent> levelLogs = new LinkedList<LogEvent>();

        for (LogEvent log : this) {
            if (log.getLevel() == level) {
                levelLogs.add(log);
            }
        }

        return levelLogs;
    }

    /**
     * Filter logs of a specific level.
     * 
     * @param level the level of the logs to return
     * @return the filtered logs
     * @since 4.2M1
     */
    public List<LogEvent> getLogsFrom(LogLevel level)
    {
        List<LogEvent> levelLogs = new LinkedList<LogEvent>();

        for (LogEvent log : this) {
            if (log.getLevel().compareTo(level) <= 0) {
                levelLogs.add(log);
            }
        }

        return levelLogs;
    }

    // Logger

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean isTraceEnabled()
    {
        return true;
    }

    @Override
    public void trace(String msg)
    {
        trace(msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void trace(String format, Object arg)
    {
        trace(format, new Object[] {arg});
    }

    @Override
    public void trace(String format, Object arg1, Object arg2)
    {
        trace(format, new Object[] {arg1, arg2});
    }

    @Override
    public void trace(String format, Object[] argArray)
    {
        addLogEvent(LogLevel.TRACE, format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t)
    {
        addLogEvent(LogLevel.TRACE, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void trace(Marker marker, String msg)
    {
        trace(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg)
    {
        trace(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2)
    {
        trace(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object[] argArray)
    {
        trace(format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t)
    {
        trace(msg, t);
    }

    @Override
    public boolean isDebugEnabled()
    {
        return true;
    }

    @Override
    public void debug(String msg)
    {
        debug(msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void debug(String format, Object arg)
    {
        debug(format, new Object[] {arg});
    }

    @Override
    public void debug(String format, Object arg1, Object arg2)
    {
        debug(format, new Object[] {arg1, arg2});
    }

    @Override
    public void debug(String format, Object[] argArray)
    {
        addLogEvent(LogLevel.DEBUG, format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t)
    {
        addLogEvent(LogLevel.DEBUG, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void debug(Marker marker, String msg)
    {
        debug(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg)
    {
        debug(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2)
    {
        debug(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object[] argArray)
    {
        debug(format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t)
    {
        debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled()
    {
        return true;
    }

    @Override
    public void info(String msg)
    {
        info(msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void info(String format, Object arg)
    {
        info(format, new Object[] {arg});
    }

    @Override
    public void info(String format, Object arg1, Object arg2)
    {
        info(format, new Object[] {arg1, arg2});
    }

    @Override
    public void info(String format, Object[] argArray)
    {
        addLogEvent(LogLevel.INFO, format, argArray);
    }

    @Override
    public void info(String msg, Throwable t)
    {
        addLogEvent(LogLevel.INFO, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void info(Marker marker, String msg)
    {
        info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg)
    {
        info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2)
    {
        info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object[] argArray)
    {
        info(format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t)
    {
        info(msg, t);
    }

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public void warn(String msg)
    {
        warn(msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void warn(String format, Object arg)
    {
        warn(format, new Object[] {arg});
    }

    @Override
    public void warn(String format, Object arg1, Object arg2)
    {
        warn(format, new Object[] {arg1, arg2});
    }

    @Override
    public void warn(String format, Object[] argArray)
    {
        addLogEvent(LogLevel.WARN, format, argArray);
    }

    @Override
    public void warn(String msg, Throwable t)
    {
        addLogEvent(LogLevel.WARN, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void warn(Marker marker, String msg)
    {
        warn(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg)
    {
        warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2)
    {
        warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object[] argArray)
    {
        warn(format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t)
    {
        warn(msg, t);
    }

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public void error(String msg)
    {
        error(msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void error(String format, Object arg)
    {
        error(format, new Object[] {arg});
    }

    @Override
    public void error(String format, Object arg1, Object arg2)
    {
        error(format, new Object[] {arg1, arg2});
    }

    @Override
    public void error(String format, Object[] argArray)
    {
        addLogEvent(LogLevel.ERROR, format, argArray);
    }

    @Override
    public void error(String msg, Throwable t)
    {
        addLogEvent(LogLevel.ERROR, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void error(Marker marker, String msg)
    {
        error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg)
    {
        error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2)
    {
        error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object[] argArray)
    {
        error(format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t)
    {
        error(msg, t);
    }
}
