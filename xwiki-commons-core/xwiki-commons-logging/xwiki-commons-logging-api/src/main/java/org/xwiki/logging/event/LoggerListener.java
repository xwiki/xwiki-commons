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
package org.xwiki.logging.event;

import org.slf4j.Logger;
import org.xwiki.logging.util.LoggingUtils;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

/**
 * Redirect all received event to the provided {@link Logger}.
 *
 * @version $Id$
 * @since 8.0M2
 */
public class LoggerListener extends AbstractEventListener
{
    /**
     * The logger where to send received events.
     */
    private Logger logger;

    /**
     * @param name the name of the listener
     * @param logger the queue where to store received {@link LogEvent}s
     */
    public LoggerListener(String name, Logger logger)
    {
        super(name, new LoggingEventEvent());

        this.logger = logger;
    }

    /**
     * @return the logger
     */
    public Logger getLogger()
    {
        return this.logger;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        LoggingUtils.log(((LoggingEventEvent) event).getLoggingEvent(), this.logger);
    }
}
