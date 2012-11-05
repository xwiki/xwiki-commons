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
package org.xwiki.velocity.internal.log;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redirects Velocity's LogChute messages to slf4j.
 * 
 * @version $Id$
 * @since 4.3M2
 */
public class SLF4JLogChute implements LogChute
{
    /**
     * The name of the property containing custom logger name.
     */
    public static final String RUNTIME_LOG_SLF4J_LOGGER = "runtime.log.logsystem.slf4j.logger";

    /**
     * Default name for the logger instance.
     */
    public static final String DEFAULT_LOG_NAME = "org.apache.velocity";

    /**
     * The SLF4J Logger instance.
     */
    protected Logger logger;

    @Override
    public void init(RuntimeServices rs) throws Exception
    {
        String name = (String) rs.getProperty(RUNTIME_LOG_SLF4J_LOGGER);

        if (name == null) {
            name = DEFAULT_LOG_NAME;
        }

        this.logger = LoggerFactory.getLogger(name);

        log(LogChute.DEBUG_ID, "SLF4JLogChute name is '" + name + "'");
    }

    @Override
    public void log(int level, String message)
    {
        switch (level) {
            case LogChute.WARN_ID:
                this.logger.warn(message);
                break;
            case LogChute.INFO_ID:
                this.logger.info(message);
                break;
            case LogChute.TRACE_ID:
                this.logger.trace(message);
                break;
            case LogChute.ERROR_ID:
                this.logger.error(message);
                break;
            case LogChute.DEBUG_ID:
            default:
                this.logger.debug(message);
                break;
        }
    }

    @Override
    public void log(int level, String message, Throwable t)
    {
        switch (level) {
            case LogChute.WARN_ID:
                this.logger.warn(message, t);
                break;
            case LogChute.INFO_ID:
                this.logger.info(message, t);
                break;
            case LogChute.TRACE_ID:
                this.logger.trace(message, t);
                break;
            case LogChute.ERROR_ID:
                this.logger.error(message, t);
                break;
            case LogChute.DEBUG_ID:
            default:
                this.logger.debug(message, t);
                break;
        }
    }

    @Override
    public boolean isLevelEnabled(int level)
    {
        boolean islevelEnabled;

        switch (level) {
            case LogChute.DEBUG_ID:
                islevelEnabled = this.logger.isDebugEnabled();
                break;
            case LogChute.INFO_ID:
                islevelEnabled = this.logger.isInfoEnabled();
                break;
            case LogChute.TRACE_ID:
                islevelEnabled = this.logger.isTraceEnabled();
                break;
            case LogChute.WARN_ID:
                islevelEnabled = this.logger.isWarnEnabled();
                break;
            case LogChute.ERROR_ID:
                islevelEnabled = this.logger.isErrorEnabled();
                break;
            default:
                islevelEnabled = true;
        }

        return islevelEnabled;
    }
}
