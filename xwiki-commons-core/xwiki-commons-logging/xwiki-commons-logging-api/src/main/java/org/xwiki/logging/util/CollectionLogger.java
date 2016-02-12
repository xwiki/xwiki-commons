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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.xwiki.logging.LoggingEventMessage;

/**
 * Store all received event in the passed Collection.
 * 
 * @version $Id$
 * @since 8.0M1
 */
public class CollectionLogger implements Logger
{
    private String name;

    private Collection<LoggingEvent> eventList;

    /**
     * @param name the name of the logger
     * @param events the Collection where to store events
     */
    public CollectionLogger(String name, Collection<LoggingEvent> events)
    {
        this.name = name;
        this.eventList = events;
    }

    @Override
    public String getName()
    {
        return name;
    }

    private void recordEvent(Level level, String msg, Object[] args, Throwable throwable)
    {
        recordEvent(level, null, msg, args, throwable);
    }

    private void recordEvent(Level level, Marker marker, String message, Object[] argumentArray, Throwable throwable)
    {
        LoggingEventMessage loggingEvent =
            new LoggingEventMessage(marker, level, message, argumentArray, throwable, this.name);

        this.eventList.add(loggingEvent);
    }

    @Override
    public boolean isTraceEnabled()
    {
        return true;
    }

    @Override
    public void trace(String msg)
    {
        recordEvent(Level.TRACE, msg, null, null);
    }

    @Override
    public void trace(String format, Object arg)
    {
        recordEvent(Level.TRACE, format, new Object[] { arg }, null);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2)
    {
        recordEvent(Level.TRACE, format, new Object[] { arg1, arg2 }, null);
    }

    @Override
    public void trace(String format, Object... arguments)
    {
        recordEvent(Level.TRACE, format, arguments, null);
    }

    @Override
    public void trace(String msg, Throwable t)
    {
        recordEvent(Level.TRACE, msg, null, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void trace(Marker marker, String msg)
    {
        recordEvent(Level.TRACE, marker, msg, null, null);

    }

    @Override
    public void trace(Marker marker, String format, Object arg)
    {
        recordEvent(Level.TRACE, marker, format, new Object[] { arg }, null);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2)
    {
        recordEvent(Level.TRACE, marker, format, new Object[] { arg1, arg2 }, null);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray)
    {
        recordEvent(Level.TRACE, marker, format, argArray, null);

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t)
    {
        recordEvent(Level.TRACE, marker, msg, null, t);
    }

    @Override
    public boolean isDebugEnabled()
    {
        return true;
    }

    @Override
    public void debug(String msg)
    {
        recordEvent(Level.TRACE, msg, null, null);
    }

    @Override
    public void debug(String format, Object arg)
    {
        recordEvent(Level.DEBUG, format, new Object[] { arg }, null);

    }

    @Override
    public void debug(String format, Object arg1, Object arg2)
    {
        recordEvent(Level.DEBUG, format, new Object[] { arg1, arg2 }, null);

    }

    @Override
    public void debug(String format, Object... arguments)
    {
        recordEvent(Level.DEBUG, format, arguments, null);
    }

    @Override
    public void debug(String msg, Throwable t)
    {
        recordEvent(Level.DEBUG, msg, null, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void debug(Marker marker, String msg)
    {
        recordEvent(Level.DEBUG, marker, msg, null, null);
    }

    @Override
    public void debug(Marker marker, String format, Object arg)
    {
        recordEvent(Level.DEBUG, marker, format, new Object[] { arg }, null);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2)
    {
        recordEvent(Level.DEBUG, marker, format, new Object[] { arg1, arg2 }, null);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments)
    {
        recordEvent(Level.DEBUG, marker, format, arguments, null);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t)
    {
        recordEvent(Level.DEBUG, marker, msg, null, t);
    }

    @Override
    public boolean isInfoEnabled()
    {
        return true;
    }

    @Override
    public void info(String msg)
    {
        recordEvent(Level.INFO, msg, null, null);
    }

    @Override
    public void info(String format, Object arg)
    {
        recordEvent(Level.INFO, format, new Object[] { arg }, null);
    }

    @Override
    public void info(String format, Object arg1, Object arg2)
    {
        recordEvent(Level.INFO, format, new Object[] { arg1, arg2 }, null);
    }

    @Override
    public void info(String format, Object... arguments)
    {
        recordEvent(Level.INFO, format, arguments, null);
    }

    @Override
    public void info(String msg, Throwable t)
    {
        recordEvent(Level.INFO, msg, null, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void info(Marker marker, String msg)
    {
        recordEvent(Level.INFO, marker, msg, null, null);
    }

    @Override
    public void info(Marker marker, String format, Object arg)
    {
        recordEvent(Level.INFO, marker, format, new Object[] { arg }, null);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2)
    {
        recordEvent(Level.INFO, marker, format, new Object[] { arg1, arg2 }, null);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments)
    {
        recordEvent(Level.INFO, marker, format, arguments, null);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t)
    {
        recordEvent(Level.INFO, marker, msg, null, t);

    }

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public void warn(String msg)
    {
        recordEvent(Level.WARN, msg, null, null);
    }

    @Override
    public void warn(String format, Object arg)
    {
        recordEvent(Level.WARN, format, new Object[] { arg }, null);

    }

    @Override
    public void warn(String format, Object arg1, Object arg2)
    {
        recordEvent(Level.WARN, format, new Object[] { arg1, arg2 }, null);
    }

    @Override
    public void warn(String format, Object... arguments)
    {
        recordEvent(Level.WARN, format, arguments, null);
    }

    @Override
    public void warn(String msg, Throwable t)
    {
        recordEvent(Level.WARN, msg, null, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void warn(Marker marker, String msg)
    {
        recordEvent(Level.WARN, msg, null, null);
    }

    @Override
    public void warn(Marker marker, String format, Object arg)
    {
        recordEvent(Level.WARN, format, new Object[] { arg }, null);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2)
    {
        recordEvent(Level.WARN, marker, format, new Object[] { arg1, arg2 }, null);

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments)
    {
        recordEvent(Level.WARN, marker, format, arguments, null);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t)
    {
        recordEvent(Level.WARN, marker, msg, null, t);
    }

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public void error(String msg)
    {
        recordEvent(Level.ERROR, msg, null, null);
    }

    @Override
    public void error(String format, Object arg)
    {
        recordEvent(Level.ERROR, format, new Object[] { arg }, null);

    }

    @Override
    public void error(String format, Object arg1, Object arg2)
    {
        recordEvent(Level.ERROR, format, new Object[] { arg1, arg2 }, null);

    }

    @Override
    public void error(String format, Object... arguments)
    {
        recordEvent(Level.ERROR, format, arguments, null);

    }

    @Override
    public void error(String msg, Throwable t)
    {
        recordEvent(Level.ERROR, msg, null, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public void error(Marker marker, String msg)
    {
        recordEvent(Level.ERROR, marker, msg, null, null);

    }

    @Override
    public void error(Marker marker, String format, Object arg)
    {
        recordEvent(Level.ERROR, marker, format, new Object[] { arg }, null);

    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2)
    {
        recordEvent(Level.ERROR, marker, format, new Object[] { arg1, arg2 }, null);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments)
    {
        recordEvent(Level.ERROR, marker, format, arguments, null);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t)
    {
        recordEvent(Level.ERROR, marker, msg, null, t);
    }
}
