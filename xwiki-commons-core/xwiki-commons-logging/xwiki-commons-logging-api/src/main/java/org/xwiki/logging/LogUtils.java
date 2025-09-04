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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Strings;
import org.slf4j.Marker;
import org.xwiki.logging.event.BeginLogEvent;
import org.xwiki.logging.event.EndLogEvent;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.internal.helpers.AbstractMessageParser;
import org.xwiki.logging.internal.helpers.AbstractMessageParser.MessageElement;
import org.xwiki.logging.internal.helpers.AbstractMessageParser.MessageIndex;
import org.xwiki.logging.internal.helpers.MessageFormatMessageParser;
import org.xwiki.logging.internal.helpers.SLF4JMessageParser;
import org.xwiki.stability.Unstable;

/**
 * @version $Id$
 * @since 5.4RC1
 */
public final class LogUtils
{
    /**
     * The identifier of the {@link MessageFormat} based translation syntax.
     */
    public static final String MESSAGE_FORMAT_SYNTAX = "messagetool/1.0";

    private LogUtils()
    {
        // Utility class
    }

    /**
     * Create and return a new {@link LogEvent} instance based on the passed parameters.
     *
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @return the {@link LogEvent}
     */
    public static LogEvent newLogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray,
        Throwable throwable)
    {
        return newLogEvent(marker, level, message, argumentArray, throwable, System.currentTimeMillis());
    }

    /**
     * Create and return a new {@link LogEvent} instance based on the passed parameters.
     *
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @param timeStamp the number of milliseconds elapsed from 1/1/1970 until logging event was created.
     * @return the {@link LogEvent}
     * @since 6.4M1
     */
    public static LogEvent newLogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray,
        Throwable throwable, long timeStamp)
    {
        if (marker != null) {
            if (marker.contains(LogEvent.MARKER_BEGIN)) {
                return new BeginLogEvent(marker, level, message, argumentArray, throwable, timeStamp);
            } else if (marker.contains(LogEvent.MARKER_END)) {
                return new EndLogEvent(marker, level, message, argumentArray, throwable, timeStamp);
            }
        }

        return new LogEvent(marker, level, message, argumentArray, throwable, timeStamp);
    }

    /**
     * Create and return a new {@link LogEvent} instance based on the passed parameters.
     *
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param arguments the event arguments to insert in the message and/or the {@link Throwable} associated with the
     *            message
     * @return the {@link LogEvent}
     * @since 14.0RC1
     */
    public static LogEvent newLogEvent(Marker marker, LogLevel level, String message, Object[] arguments)
    {
        Object[] actualArray;
        Throwable throwable;
        if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            actualArray = Arrays.copyOf(arguments, arguments.length - 1);
            throwable = (Throwable) arguments[arguments.length - 1];
        } else {
            actualArray = arguments;
            throwable = null;
        }

        return newLogEvent(marker, level, message, actualArray, throwable);
    }

    /**
     * Translate the passed {@link LogEvent} based on the passed translation message pattern.
     * <p>
     * The translation message pattern use the same syntax than standard message pattern except that it's optionally
     * possible to provide a custom index as in <code>Some {1} translation {0} message</code> in order to modify the
     * order of the argument which can be required depending on the language.
     *
     * @param logEvent the {@link LogEvent} to translate
     * @param translatedMessage the translated version of the {@link LogEvent} message
     * @return the translated version of the passed {@link LogEvent}
     */
    public static LogEvent translate(LogEvent logEvent, String translatedMessage)
    {
        return (LogEvent) translate((Message) logEvent, translatedMessage);
    }

    /**
     * Translate the passed {@link Message} based on the passed translation message pattern.
     * <p>
     * The translation message pattern use the same syntax than standard message pattern except that it's optionally
     * possible to provide a custom index as in <code>Some {1} translation {0} message</code> in order to modify the
     * order of the argument which can be required depending on the language.
     *
     * @param message the {@link Message} to translate
     * @param translatedPattern the translated version of the {@link Message} message
     * @return the translated version of the passed {@link Message}
     * @since 15.0RC1
     * @since 14.10.1
     */
    @Unstable
    public static Message translate(Message message, String translatedPattern)
    {
        return translate(message, translatedPattern, null);
    }

    /**
     * Translate the passed {@link Message} based on the passed translation message pattern.
     * <p>
     * The translation message pattern use the same syntax than standard message pattern except that it's optionally
     * possible to provide a custom index as in <code>Some {1} translation {0} message</code> in order to modify the
     * order of the argument which can be required depending on the language.
     *
     * @param <M> the type of Message
     * @param message the {@link Message} to translate
     * @param translationPattern the pattern to use to translate the {@link Message}
     * @param translatedPatternSyntax the syntax of the translation pattern
     * @return the translated version of the passed {@link Message}
     * @since 17.8.0RC1
     * @since 17.4.5
     * @since 16.10.11
     */
    public static <M extends Message> M translate(M message, String translationPattern, String translatedPatternSyntax)
    {
        if (translationPattern != null) {
            AbstractMessageParser parser = Strings.CS.equals(translatedPatternSyntax, MESSAGE_FORMAT_SYNTAX)
                ? new MessageFormatMessageParser(translationPattern) : new SLF4JMessageParser(translationPattern);

            Object[] defaultArguments = message.getArgumentArray();
            List<Object> arguments = new ArrayList<>(defaultArguments.length);
            StringBuilder translatedMessage = new StringBuilder();

            for (MessageElement element = parser.next(); element != null; element = parser.next()) {
                if (element instanceof MessageIndex messageIndex) {
                    // Add place holder to the message
                    translatedMessage.append(SLF4JMessageParser.ARGUMENT_STR);

                    // Reorder argument based on the translation index
                    arguments.add(defaultArguments[messageIndex.getIndex()]);
                } else {
                    translatedMessage.append(element.getString());
                }
            }

            // Add remaining arguments
            for (int index = arguments.size(); index < defaultArguments.length; ++index) {
                arguments.add(defaultArguments[index]);
            }

            if (message instanceof LogEvent logEvent) {
                return (M) new LogEvent(logEvent.getMarker(), logEvent.getLevel(), translatedMessage.toString(),
                    arguments.toArray(), logEvent.getThrowable(), logEvent.getTimeStamp());
            } else {
                return (M) new Message(message.getMarker(), translatedMessage.toString(), arguments.toArray(),
                    message.getThrowable());
            }
        }

        return message;
    }

    /**
     * Filter out the log {@link Throwable} from passed arguments.
     * 
     * @param arguments the passed argument from which to filter out the throwable
     * @return the actual argument
     * @since 15.0RC1
     * @since 14.10.1
     */
    @Unstable
    public static Object[] getArgumentArray(Object... arguments)
    {
        if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            return Arrays.copyOf(arguments, arguments.length - 1);
        }

        return arguments;
    }

    /**
     * Extract the log {@link Throwable} from passed arguments.
     * 
     * @param arguments the passed argument from which to extract the throwable
     * @return the throwable
     * @since 15.0RC1
     * @since 14.10.1
     */
    @Unstable
    public static Throwable getThrowable(Object... arguments)
    {
        if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            return (Throwable) arguments[arguments.length - 1];
        }

        return null;
    }

}
