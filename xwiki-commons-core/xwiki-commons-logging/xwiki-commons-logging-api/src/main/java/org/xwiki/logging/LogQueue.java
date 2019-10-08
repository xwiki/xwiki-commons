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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Marker;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.internal.ListLogTailResult;
import org.xwiki.logging.tail.LogTailResult;
import org.xwiki.logging.tail.LoggerTail;

/**
 * A queue of {@link LogEvent}s.
 *
 * @version $Id$
 * @since 3.2M3
 */
public class LogQueue extends ConcurrentLinkedQueue<LogEvent> implements LoggerTail
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Copy the stored log into a passed {@link org.slf4j.Logger}.
     *
     * @param targetLogger the logger where to copy the stored log
     * @since 5.3M1
     */
    @Override
    public void log(org.slf4j.Logger targetLogger)
    {
        for (LogEvent logEvent : this) {
            logEvent.log(targetLogger);
        }
    }

    /**
     * @param level the log level
     * @param format the log message
     * @param arguments the event arguments to insert in the message
     * @return the created {@link LogEvent} instance
     * @since 4.1RC1
     */
    public LogEvent addLogEvent(LogLevel level, String format, Object[] arguments)
    {
        return addLogEvent(null, level, format, arguments);
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param format the log message
     * @param arguments the event arguments to insert in the message
     * @return the created {@link LogEvent} instance
     * @since 4.3M1
     */
    public LogEvent addLogEvent(Marker marker, LogLevel level, String format, Object[] arguments)
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

        return addLogEvent(marker, level, format, actualArray, throwable);
    }

    /**
     * @param level the log level
     * @param format the log message
     * @param arguments the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @return the created {@link LogEvent} instance
     * @since 4.1RC1
     */
    public LogEvent addLogEvent(LogLevel level, String format, Object[] arguments, Throwable throwable)
    {
        return addLogEvent(null, level, format, arguments, throwable);
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param format the log message
     * @param arguments the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @return the created {@link LogEvent} instance
     * @since 4.3M1
     */
    public LogEvent addLogEvent(Marker marker, LogLevel level, String format, Object[] arguments, Throwable throwable)
    {
        LogEvent logEvent = LogUtils.newLogEvent(marker, level, format, arguments, throwable);
        log(logEvent);

        return logEvent;
    }

    @Override
    public void log(LogEvent logEvent)
    {
        add(logEvent);
    }

    @Override
    public LogEvent getLogEvent(int index)
    {
        int size = size();
        if (index < 0 || index >= size) {
            return null;
        }

        int i = 0;
        for (LogEvent log : this) {
            if (i == index) {
                return log;
            }

            ++i;
        }

        return null;
    }

    private List<LogEvent> getLogEvents(LogLevel level, int offset, int limit, boolean exact)
    {
        List<LogEvent> levelLogs = limit > 0 ? new ArrayList<>(limit) : new ArrayList<>();

        int actualIndex = offset >= 0 ? offset : 0;

        int i = 0;
        for (LogEvent log : this) {
            if (i >= actualIndex && (exact ? log.getLevel() == level : log.getLevel().compareTo(level) <= 0)) {
                levelLogs.add(log);

                if (limit > 0 && levelLogs.size() == limit) {
                    break;
                }
            }

            ++i;
        }

        return levelLogs;
    }

    @Override
    public LogTailResult getLogEvents(LogLevel from, int offset, int limit)
    {
        return new ListLogTailResult(getLogEvents(from, offset, limit, false));
    }

    @Override
    public boolean hasLevel(LogLevel from)
    {
        for (LogEvent log : this) {
            if (log.getLevel().compareTo(from) <= 0) {
                return true;
            }
        }

        return false;
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
        return getLogEvents(level, 0, -1, true);
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
        return getLogEvents(level, 0, -1, false);
    }

    /**
     * Indicate if the list contains logs of a specific level.
     *
     * @param level the level of the logs to return
     * @return true if log of provided level or less exist
     * @since 6.0M1
     */
    public boolean containLogsFrom(LogLevel level)
    {
        for (LogEvent log : this) {
            if (log.getLevel().compareTo(level) <= 0) {
                return true;
            }
        }

        return false;
    }

    // Logger

    @Override
    public String getName()
    {
        return null;
    }

    // TRACE

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
        trace((Marker) null, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2)
    {
        trace((Marker) null, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments)
    {
        trace((Marker) null, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t)
    {
        trace((Marker) null, msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void trace(Marker marker, String msg)
    {
        trace(marker, msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void trace(Marker marker, String format, Object arg)
    {
        trace(marker, format, new Object[] { arg });
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2)
    {
        trace(marker, format, new Object[] { arg1, arg2 });
    }

    @Override
    public void trace(Marker marker, String format, Object... arguments)
    {
        addLogEvent(marker, LogLevel.TRACE, format, arguments);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t)
    {
        addLogEvent(marker, LogLevel.TRACE, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    // DEBUG

    @Override
    public boolean isDebugEnabled()
    {
        return true;
    }

    @Override
    public void debug(String msg)
    {
        debug((Marker) null, msg);
    }

    @Override
    public void debug(String format, Object arg)
    {
        debug((Marker) null, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2)
    {
        debug((Marker) null, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments)
    {
        debug((Marker) null, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t)
    {
        debug((Marker) null, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void debug(Marker marker, String msg)
    {
        debug(marker, msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void debug(Marker marker, String format, Object arg)
    {
        debug(marker, format, new Object[] { arg });
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2)
    {
        debug(marker, format, new Object[] { arg1, arg2 });
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments)
    {
        addLogEvent(marker, LogLevel.DEBUG, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t)
    {
        addLogEvent(marker, LogLevel.DEBUG, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    // INFO

    @Override
    public boolean isInfoEnabled()
    {
        return true;
    }

    @Override
    public void info(String msg)
    {
        info((Marker) null, msg);
    }

    @Override
    public void info(String format, Object arg)
    {
        info((Marker) null, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2)
    {
        info((Marker) null, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments)
    {
        info((Marker) null, format, arguments);
    }

    @Override
    public void info(String msg, Throwable t)
    {
        info((Marker) null, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void info(Marker marker, String msg)
    {
        info(marker, msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void info(Marker marker, String format, Object arg)
    {
        info(marker, format, new Object[] { arg });
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2)
    {
        info(marker, format, new Object[] { arg1, arg2 });
    }

    @Override
    public void info(Marker marker, String format, Object... arguments)
    {
        addLogEvent(marker, LogLevel.INFO, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t)
    {
        addLogEvent(marker, LogLevel.INFO, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    // WARN

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public void warn(String msg)
    {
        warn((Marker) null, msg);
    }

    @Override
    public void warn(String format, Object arg)
    {
        warn((Marker) null, format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2)
    {
        warn((Marker) null, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments)
    {
        warn((Marker) null, format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t)
    {
        warn((Marker) null, msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void warn(Marker marker, String msg)
    {
        warn(marker, msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void warn(Marker marker, String format, Object arg)
    {
        warn(marker, format, new Object[] { arg });
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2)
    {
        warn(marker, format, new Object[] { arg1, arg2 });
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments)
    {
        addLogEvent(marker, LogLevel.WARN, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t)
    {
        addLogEvent(marker, LogLevel.WARN, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    // ERROR

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public void error(String msg)
    {
        error((Marker) null, msg);
    }

    @Override
    public void error(String format, Object arg)
    {
        error((Marker) null, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2)
    {
        error((Marker) null, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments)
    {
        error((Marker) null, format, arguments);
    }

    @Override
    public void error(String msg, Throwable t)
    {
        error((Marker) null, msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void error(Marker marker, String msg)
    {
        error(marker, msg, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public void error(Marker marker, String format, Object arg)
    {
        error(marker, format, new Object[] { arg });
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2)
    {
        error(marker, format, new Object[] { arg1, arg2 });
    }

    @Override
    public void error(Marker marker, String format, Object... arguments)
    {
        addLogEvent(marker, LogLevel.ERROR, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t)
    {
        addLogEvent(marker, LogLevel.ERROR, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }

    @Override
    public void flush()
    {
        // Nothing to do
    }

    @Override
    public void close() throws Exception
    {
        // Nothing to do
    }
}
