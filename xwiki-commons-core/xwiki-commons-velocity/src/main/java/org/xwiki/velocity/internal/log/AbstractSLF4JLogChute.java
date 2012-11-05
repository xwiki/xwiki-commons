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

import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;

/**
 * Based class for all SLF4J based implementation of {@link LogChute}.
 * 
 * @version $Id$
 * @since 4.3M2
 */
public abstract class AbstractSLF4JLogChute implements LogChute
{
    /**
     * @return the logger
     */
    protected abstract Logger getLogger();

    @Override
    public void log(int level, String message)
    {
        switch (level) {
            case LogChute.WARN_ID:
                getLogger().warn(message);
                break;
            case LogChute.INFO_ID:
                getLogger().info(message);
                break;
            case LogChute.TRACE_ID:
                getLogger().trace(message);
                break;
            case LogChute.ERROR_ID:
                getLogger().error(message);
                break;
            case LogChute.DEBUG_ID:
            default:
                getLogger().debug(message);
                break;
        }
    }

    @Override
    public void log(int level, String message, Throwable t)
    {
        switch (level) {
            case LogChute.WARN_ID:
                getLogger().warn(message, t);
                break;
            case LogChute.INFO_ID:
                getLogger().info(message, t);
                break;
            case LogChute.TRACE_ID:
                getLogger().trace(message, t);
                break;
            case LogChute.ERROR_ID:
                getLogger().error(message, t);
                break;
            case LogChute.DEBUG_ID:
            default:
                getLogger().debug(message, t);
                break;
        }
    }

    @Override
    public boolean isLevelEnabled(int level)
    {
        boolean islevelEnabled;

        switch (level) {
            case LogChute.DEBUG_ID:
                islevelEnabled = getLogger().isDebugEnabled();
                break;
            case LogChute.INFO_ID:
                islevelEnabled = getLogger().isInfoEnabled();
                break;
            case LogChute.TRACE_ID:
                islevelEnabled = getLogger().isTraceEnabled();
                break;
            case LogChute.WARN_ID:
                islevelEnabled = getLogger().isWarnEnabled();
                break;
            case LogChute.ERROR_ID:
                islevelEnabled = getLogger().isErrorEnabled();
                break;
            default:
                islevelEnabled = true;
        }

        return islevelEnabled;
    }
}
