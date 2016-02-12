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
package org.xwiki.logging.legacy;

import org.slf4j.event.Level;
import org.xwiki.logging.LogLevel;

/**
 * @version $Id$
 * @since 8.0M2
 */
public final class LegacyLoggingUtils
{
    private LegacyLoggingUtils()
    {
        // Utility class
    }

    /**
     * Convert from {@link LogLevel} to {@link Level}.
     * 
     * @param logLevel the {@link LogLevel} to convert
     * @return the resulting {@link Level}
     */
    public static Level toLevel(LogLevel logLevel)
    {
        Level level;

        switch (logLevel) {
            case TRACE:
                level = Level.TRACE;
                break;
            case DEBUG:
                level = Level.DEBUG;
                break;
            case INFO:
                level = Level.INFO;
                break;
            case WARN:
                level = Level.WARN;
                break;
            case ERROR:
                level = Level.ERROR;
                break;
            default:
                level = null;
                break;
        }

        return level;
    }

    /**
     * Convert from {@link Level} to {@link LogLevel}.
     * 
     * @param logLevel the {@link Level} to convert
     * @return the resulting {@link LogLevel}
     */
    public static LogLevel toLogLevel(Level logLevel)
    {
        LogLevel level;

        switch (logLevel) {
            case TRACE:
                level = LogLevel.TRACE;
                break;
            case DEBUG:
                level = LogLevel.DEBUG;
                break;
            case INFO:
                level = LogLevel.INFO;
                break;
            case WARN:
                level = LogLevel.WARN;
                break;
            case ERROR:
                level = LogLevel.ERROR;
                break;
            default:
                level = null;
                break;
        }

        return level;
    }
}
