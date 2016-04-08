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
package org.xwiki.test;

import ch.qos.logback.classic.Level;

/**
 * Helper class to represent Logging levels to capture.
 *
 * @version $Id$
 * @since 8.1M2
 */
public enum LogLevel
{
    /**
     * Trace level.
     */
    TRACE(Level.TRACE),

    /**
     * Debug level.
     */
    DEBUG(Level.DEBUG),

    /**
     * Info level.
     */
    INFO(Level.INFO),

    /**
     * Warn level.
     */
    WARN(Level.WARN),

    /**
     * Error level.
     */
    ERROR(Level.ERROR);

    /**
     * @see #LogLevel(ch.qos.logback.classic.Level)
     */
    private Level internalLevel;

    /**
     * @param level see {@link #getLevel()}
     */
    LogLevel(Level level)
    {
        this.internalLevel = level;
    }

    /**
     * @return the log level to capture
     */
    public Level getLevel()
    {
        return this.internalLevel;
    }
}
