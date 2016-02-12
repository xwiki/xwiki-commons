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
package org.xwiki.logging.internal.event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Marker;
import org.slf4j.event.LoggingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.logging.LoggingEventMessage;
import org.xwiki.logging.event.BeginLogEvent;
import org.xwiki.logging.event.EndLogEvent;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.event.LoggingEventEvent;
import org.xwiki.logging.legacy.LegacyLoggingUtils;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

/**
 * Convert old log related event to new ones and the opposite.
 * 
 * @version $Id$
 * @since 8.0M2
 */
@Component
@Named(LegacyBridgeListener.NAME)
@Singleton
public class LegacyBridgeListener extends AbstractEventListener
{
    /**
     * The name of the listener.
     */
    public static final String NAME = "legacy.LegacyBridgeListener";

    @Inject
    private ObservationManager observation;

    /**
     * Default constructor.
     */
    public LegacyBridgeListener()
    {
        super(NAME, new LogEvent(), new BeginLogEvent(),
            new EndLogEvent(), new LoggingEventEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof LogEvent) {
            LogEvent logEvent = (LogEvent) event;

            this.observation.notify(new LoggingEventEvent(new LoggingEventMessage(legacyMarker(logEvent.getMarker()),
                LegacyLoggingUtils.toLevel(logEvent.getLevel()), logEvent.getMessage(), logEvent.getArgumentArray(),
                logEvent.getThrowable(), logEvent.getTimeStamp(), (String) source, null)), source, null);
        } else if (event instanceof LoggingEventEvent) {
            LoggingEvent logEvent = ((LoggingEventEvent) event).getLoggingEvent();

            this.observation.notify(new LogEvent(legacyMarker(logEvent.getMarker()),
                LegacyLoggingUtils.toLogLevel(logEvent.getLevel()), logEvent.getMessage(), logEvent.getArgumentArray(),
                logEvent.getThrowable(), logEvent.getTimeStamp()), logEvent.getLoggerName(), data);
        }
    }

    private Marker legacyMarker(Marker marker)
    {
        if (marker == null) {
            return LogEvent.MARKER_LEGACY;
        }

        marker.add(LogEvent.MARKER_LEGACY);

        return marker;
    }
}
