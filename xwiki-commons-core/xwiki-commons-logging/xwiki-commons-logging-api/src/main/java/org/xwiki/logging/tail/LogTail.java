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
package org.xwiki.logging.tail;

import org.xwiki.logging.LogLevel;
import org.xwiki.logging.Logger;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.stability.Unstable;

/**
 * Extends {@link Logger} and provide tail-like APIs to navigate in it.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
@Unstable
public interface LogTail extends Iterable<LogEvent>
{
    /**
     * @param logger the logger to send logs to
     */
    void log(org.slf4j.Logger logger);

    /**
     * @param index the index of the log event
     * @return the log event associated with the passed index or null if it does not exist
     */
    LogEvent getLogEvent(int index);

    /**
     * @param offset the offset where to start searching for the log events
     * @param limit the maximum number of results to return
     * @return the log events with the passed level or more
     */
    default LogTailResult getLogEvents(int offset, int limit)
    {
        return getLogEvents(null, offset, limit);
    }

    /**
     * @return the last log event
     */
    default LogEvent getFirstLogEvent()
    {
        return size() > 0 ? getLogEvent(0) : null;
    }

    /**
     * @return the last log event
     */
    default LogEvent getLastLogEvent()
    {
        int size = size();

        return size > 0 ? getLogEvent(size() - 1) : null;
    }

    /**
     * @param from the log level from which to select log events
     * @return the first log event with passed level or more
     */
    LogEvent getFirstLogEvent(LogLevel from);

    /**
     * @param from the log level from which to select log events
     * @return the last log event with passed level or more
     */
    LogEvent getLastLogEvent(LogLevel from);

    /**
     * @param from the log level from which to select log events
     * @return the log events with the passed level or more
     */
    default LogTailResult getLogEvents(LogLevel from)
    {
        return getLogEvents(from, 0, -1);
    }

    /**
     * @param from the log level from which to select log events
     * @param offset the offset where to start searching for the log events
     * @param limit the maximum number of results to return
     * @return the log events with the passed level or more
     */
    LogTailResult getLogEvents(LogLevel from, int offset, int limit);

    /**
     * @param from the log level from which to select log events
     * @return true if a log event with the passed log level or more was found
     */
    boolean hasLogLevel(LogLevel from);

    /**
     * @return the number of log event in that tail
     */
    int size();
}
