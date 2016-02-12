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

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.event.LoggingEvent;
import org.xwiki.logging.LoggingEventMessage;
import org.xwiki.logging.internal.helpers.MessageParser;
import org.xwiki.logging.internal.helpers.MessageParser.MessageElement;
import org.xwiki.logging.internal.helpers.MessageParser.MessageIndex;

/**
 * @version $Id$
 * @since 8.0M1
 */
public final class LoggingUtils
{
    private LoggingUtils()
    {
        // Utility class
    }

    /**
     * Translate the passed {@link LoggingEvent} based on the passed translation message pattern.
     * <p>
     * The translation message pattern use the same syntax than standard message pattern except that it's optionally
     * possible to provide a custom index as in <code>Some {1} translation {0} message</code> in order to modify the
     * order of the argument which can be required depending on the language.
     *
     * @param logEvent the {@link LoggingEvent} to translate
     * @param translatedMessage the translated version of the {@link LoggingEvent} message
     * @return the translated version of the passed {@link LoggingEvent}
     */
    public static LoggingEvent translate(LoggingEvent logEvent, String translatedMessage)
    {
        if (translatedMessage != null) {
            MessageParser parser = new MessageParser(translatedMessage, true);

            Object[] defaultArguments = logEvent.getArgumentArray();
            Object[] arguments = new Object[defaultArguments.length];
            StringBuilder message = new StringBuilder();

            int index = 0;
            for (MessageElement element = parser.next(); element != null; element = parser.next()) {
                if (element instanceof MessageIndex) {
                    message.append(MessageParser.ARGUMENT_STR);
                    arguments[index++] = defaultArguments[((MessageIndex) element).getIndex()];
                } else {
                    message.append(element.getString());
                }
            }

            for (; index < arguments.length; ++index) {
                arguments[index] = defaultArguments[index];
            }

            return new LoggingEventMessage(logEvent.getMarker(), logEvent.getLevel(), message.toString(), arguments,
                logEvent.getThrowable(), logEvent.getTimeStamp(), logEvent.getLoggerName(), logEvent.getThreadName());
        }

        return logEvent;
    }

    /**
     * Pass the passed log to the passed {@link Logger}.
     *
     * @param event the event to send
     * @param logger the logger where to send the stored log
     */
    public static void log(LoggingEvent event, Logger logger)
    {
        switch (event.getLevel()) {
            case TRACE:
                logger.trace(event.getMarker(), event.getMessage(),
                    ArrayUtils.add(event.getArgumentArray(), event.getThrowable()));
                break;
            case DEBUG:
                logger.debug(event.getMarker(), event.getMessage(),
                    ArrayUtils.add(event.getArgumentArray(), event.getThrowable()));
                break;
            case INFO:
                logger.info(event.getMarker(), event.getMessage(),
                    ArrayUtils.add(event.getArgumentArray(), event.getThrowable()));
                break;
            case WARN:
                logger.warn(event.getMarker(), event.getMessage(),
                    ArrayUtils.add(event.getArgumentArray(), event.getThrowable()));
                break;
            case ERROR:
                logger.error(event.getMarker(), event.getMessage(),
                    ArrayUtils.add(event.getArgumentArray(), event.getThrowable()));
                break;
            default:
                break;
        }
    }

    /**
     * Pass the passed log to the passed {@link Logger}.
     *
     * @param events the events to send
     * @param logger the logger where to send the stored log
     */
    public static void log(Iterable<? extends LoggingEvent> events, Logger logger)
    {
        for (LoggingEvent logEvent : events) {
            log(logEvent, logger);
        }
    }
}
