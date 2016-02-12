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
package org.xwiki.logging.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.xwiki.logging.LoggingEventMessage;

/**
 * A queue of {@link LoggingEvent}s.
 *
 * @version $Id$
 * @since 8.0M2
 */
public class LoggingEventMessageQueue extends ConcurrentLinkedQueue<LoggingEventMessage> implements Logger
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Filter logs of a specific level.
     *
     * @param level the level of the logs to return
     * @return the filtered logs
     */
    public List<LoggingEventMessage> getLogs(Level level)
    {
        List<LoggingEventMessage> levelLogs = new LinkedList<LoggingEventMessage>();

        for (LoggingEventMessage log : this) {
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
     */
    public List<LoggingEventMessage> getLogsFrom(Level level)
    {
        List<LoggingEventMessage> levelLogs = new LinkedList<LoggingEventMessage>();

        for (LoggingEventMessage log : this) {
            if (log.getLevel().compareTo(level) <= 0) {
                levelLogs.add(log);
            }
        }

        return levelLogs;
    }

    /**
     * Indicate if the list contains logs of a specific level.
     *
     * @param level the level of the logs to return
     * @return true if log of provided level or less exist
     */
    public boolean containLogsFrom(Level level)
    {
        for (LoggingEventMessage log : this) {
            if (log.getLevel().compareTo(level) <= 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param format the log message
     * @param arguments the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @return the created {@link LogEvent} instance
     */
    protected LoggingEventMessage add(Marker marker, Level level, String format, Object[] arguments,
        Throwable throwable)
    {
        LoggingEventMessage logEvent = new LoggingEventMessage(marker, level, format, arguments, throwable, getName());

        add(logEvent);

        return logEvent;
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param format the log message
     * @param arguments the event arguments to insert in the message
     * @return the created {@link LogEvent} instance
     */
    protected LoggingEventMessage add(Marker marker, Level level, String format, Object[] arguments)
    {
        LoggingEventMessage logEvent =
            (LoggingEventMessage) new LoggingEventMessage(marker, level, format, arguments, getName());

        add(logEvent);

        return logEvent;
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
        add(marker, Level.TRACE, format, arguments);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t)
    {
        add(marker, Level.TRACE, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
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
        add(marker, Level.DEBUG, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t)
    {
        add(marker, Level.DEBUG, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
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
        add(marker, Level.INFO, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t)
    {
        add(marker, Level.INFO, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
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
        add(marker, Level.WARN, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t)
    {
        add(marker, Level.WARN, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
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
        add(marker, Level.ERROR, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t)
    {
        add(marker, Level.ERROR, msg, ArrayUtils.EMPTY_OBJECT_ARRAY, t);
    }
}
