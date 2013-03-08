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

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.TranslationMarker;
import org.xwiki.logging.internal.helpers.ExtendedMessageFormatter;
import org.xwiki.observation.event.Event;

/**
 * Sent when logger is called.
 * 
 * @version $Id$
 * @since 3.2M1
 */
public class LogEvent implements Event
{
    /**
     * @see #getMarker()
     */
    private Marker marker;

    /**
     * @see #getLevel()
     */
    private LogLevel level;

    /**
     * @see LogEvent#getMessage()
     */
    private String message;

    /**
     * @see #getArgumentArray()
     */
    private Object[] argumentArray;

    /**
     * @see LogEvent#getThrowable()
     */
    private Throwable throwable;

    /**
     * Formatted version of the message.
     */
    private transient String formattedMessage;

    /**
     * Matches any {@link LogEvent}.
     */
    public LogEvent()
    {

    }

    /**
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     */
    public LogEvent(LogLevel level, String message, Object[] argumentArray, Throwable throwable)
    {
        this(null, level, message, argumentArray, throwable);
    }

    /**
     * @param marker the log marker
     * @param level the log level
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     * @since 4.3M
     */
    public LogEvent(Marker marker, LogLevel level, String message, Object[] argumentArray, Throwable throwable)
    {
        this.marker = marker;
        this.level = level;
        this.message = message;
        this.argumentArray = argumentArray;
        this.throwable = throwable;
    }

    /**
     * @return the log marker
     * @since 4.3M1
     */
    public Marker getMarker()
    {
        return this.marker;
    }

    /**
     * @return the log level
     */
    public LogLevel getLevel()
    {
        return this.level;
    }

    /**
     * @return the log message
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * @return the event arguments to insert in the message
     */
    public Object[] getArgumentArray()
    {
        return this.argumentArray;
    }

    /**
     * @return the throwable associated to the event
     */
    public Throwable getThrowable()
    {
        return this.throwable;
    }

    /**
     * @return the formated version of the message
     */
    public String getFormattedMessage()
    {
        if (this.formattedMessage != null) {
            return this.formattedMessage;
        }
        if (this.argumentArray != null) {
            this.formattedMessage = MessageFormatter.arrayFormat(this.message, this.argumentArray).getMessage();
        } else {
            this.formattedMessage = this.message;
        }

        return this.formattedMessage;
    }

    /**
     * @return the log message cut in peaces
     * @since 4.2M1
     */
    public List<String> getMessageElements()
    {
        return ExtendedMessageFormatter.parseMessage(getMessage(), getArgumentArray());
    }

    /**
     * @return the translation key associated to the log
     * @since 5.0M2
     */
    public String getTranslationKey()
    {
        if (getMarker() instanceof TranslationMarker) {
            return ((TranslationMarker) getMarker()).getTranslationKey();
        }

        return null;
    }

    // Event

    @Override
    public boolean matches(Object otherEvent)
    {
        return true;
    }

    // Object

    @Override
    public String toString()
    {
        return getLevel().toString() + ':' + getFormattedMessage();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(7, 11).append(getMarker()).append(getLevel()).append(getMessage())
            .append(getArgumentArray()).append(getThrowable()).toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != getClass()) {
            return false;
        }
        LogEvent rhs = (LogEvent) object;
        return new EqualsBuilder().append(getMarker(), rhs.getMarker()).append(getLevel(), rhs.getLevel())
            .append(getMessage(), rhs.getMessage()).append(getArgumentArray(), rhs.getArgumentArray())
            .append(getThrowable(), rhs.getThrowable()).isEquals();
    }
}
