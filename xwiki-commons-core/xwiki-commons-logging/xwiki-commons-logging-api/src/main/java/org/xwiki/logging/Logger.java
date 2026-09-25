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

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.xwiki.logging.event.LogEvent;

/**
 * A {@link org.slf4j.Logger} with support for {@link LogEvent}.
 *
 * @version $Id$
 * @since 5.4M1
 */
public interface Logger extends org.slf4j.Logger
{
    /**
     * Marker used to indicate the log should not be filtered and always be allowed to end up in the main log.
     * 
     * @since 11.9RC1
     */
    Marker ROOT_MARKER = MarkerFactory.getMarker("root");

    /**
     * Marker used to indicate that the stack trace of the exception passed to the log is the point of the log and must
     * be printed in full, whatever the level of the log and whatever the console is configured to shorten. Use it for
     * the rare warnings whose exception carries no information of its own and only exists to record the call site.
     *
     * @since 18.7.0RC1
     */
    Marker STACKTRACE_MARKER = MarkerFactory.getMarker("xwiki.stacktrace");

    /**
     * @param logEvent the log event
     */
    void log(LogEvent logEvent);
}
