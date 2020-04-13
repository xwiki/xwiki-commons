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
import java.util.Collection;
import java.util.List;

import org.slf4j.Marker;
import org.xwiki.logging.event.LogEvent;

/**
 * Redirect received log to several {@link Logger}s.
 *
 * @version $Id$
 * @since 5.4M1
 */
public class CompositeLogger implements Logger
{
    private List<Logger> loggers;

    /**
     * @param loggers the loggers
     */
    public CompositeLogger(Collection<Logger> loggers)
    {
        this.loggers = new ArrayList<>(loggers);
    }

    /**
     * @param loggers the loggers
     */
    public CompositeLogger(Logger... loggers)
    {
        this.loggers = new ArrayList<>(loggers.length);

        for (Logger logger : loggers) {
            this.loggers.add(logger);
        }
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean isTraceEnabled()
    {
        for (Logger logger : this.loggers) {
            if (logger.isTraceEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void trace(String msg)
    {
        for (Logger logger : this.loggers) {
            logger.trace(msg);
        }
    }

    @Override
    public void trace(String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.trace(format, arg);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.trace(format, arg1, arg2);
        }
    }

    @Override
    public void trace(String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.trace(format, arguments);
        }
    }

    @Override
    public void trace(String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.trace(msg, t);
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker)
    {
        for (Logger logger : this.loggers) {
            if (logger.isTraceEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void trace(Marker marker, String msg)
    {
        for (Logger logger : this.loggers) {
            logger.trace(marker, msg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.trace(marker, format, arg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.trace(marker, format, arg1, arg2);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.trace(marker, format, arguments);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.trace(marker, msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled()
    {
        for (Logger logger : this.loggers) {
            if (logger.isDebugEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void debug(String msg)
    {
        for (Logger logger : this.loggers) {
            logger.debug(msg);
        }
    }

    @Override
    public void debug(String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.debug(format, arg);
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.debug(format, arg1, arg2);
        }
    }

    @Override
    public void debug(String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.debug(format, arguments);
        }
    }

    @Override
    public void debug(String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.debug(msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker)
    {
        for (Logger logger : this.loggers) {
            if (logger.isDebugEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void debug(Marker marker, String msg)
    {
        for (Logger logger : this.loggers) {
            logger.debug(marker, msg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.debug(marker, format, arg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.debug(marker, format, arg1, arg2);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.debug(marker, format, arguments);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.debug(marker, msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled()
    {
        for (Logger logger : this.loggers) {
            if (logger.isInfoEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void info(String msg)
    {
        for (Logger logger : this.loggers) {
            logger.info(msg);
        }
    }

    @Override
    public void info(String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.info(format, arg);
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.info(format, arg1, arg2);
        }
    }

    @Override
    public void info(String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.info(format, arguments);
        }
    }

    @Override
    public void info(String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.info(msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker)
    {
        for (Logger logger : this.loggers) {
            if (logger.isInfoEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void info(Marker marker, String msg)
    {
        for (Logger logger : this.loggers) {
            logger.info(marker, msg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.info(marker, format, arg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.info(marker, format, arg1, arg2);
        }
    }

    @Override
    public void info(Marker marker, String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.info(marker, format, arguments);
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.info(marker, msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled()
    {
        for (Logger logger : this.loggers) {
            if (logger.isWarnEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void warn(String msg)
    {
        for (Logger logger : this.loggers) {
            logger.warn(msg);
        }
    }

    @Override
    public void warn(String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.warn(format, arg);
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.warn(format, arg1, arg2);
        }
    }

    @Override
    public void warn(String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.warn(format, arguments);
        }
    }

    @Override
    public void warn(String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.warn(msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker)
    {
        for (Logger logger : this.loggers) {
            if (logger.isWarnEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void warn(Marker marker, String msg)
    {
        for (Logger logger : this.loggers) {
            logger.warn(marker, msg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.warn(marker, format, arg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.warn(marker, format, arg1, arg2);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.warn(marker, format, arguments);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.warn(marker, msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled()
    {
        for (Logger logger : this.loggers) {
            if (logger.isErrorEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void error(String msg)
    {
        for (Logger logger : this.loggers) {
            logger.error(msg);
        }
    }

    @Override
    public void error(String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.error(format, arg);
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.error(format, arg1, arg2);
        }
    }

    @Override
    public void error(String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.error(format, arguments);
        }
    }

    @Override
    public void error(String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.error(msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker)
    {
        for (Logger logger : this.loggers) {
            if (logger.isErrorEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void error(Marker marker, String msg)
    {
        for (Logger logger : this.loggers) {
            logger.error(marker, msg);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg)
    {
        for (Logger logger : this.loggers) {
            logger.error(marker, format, arg);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2)
    {
        for (Logger logger : this.loggers) {
            logger.error(marker, format, arg1, arg2);
        }
    }

    @Override
    public void error(Marker marker, String format, Object... arguments)
    {
        for (Logger logger : this.loggers) {
            logger.error(marker, format, arguments);
        }
    }

    @Override
    public void error(Marker marker, String msg, Throwable t)
    {
        for (Logger logger : this.loggers) {
            logger.error(marker, msg, t);
        }
    }

    @Override
    public void log(LogEvent logEvent)
    {
        for (Logger logger : this.loggers) {
            logger.log(logEvent);
        }
    }

}
