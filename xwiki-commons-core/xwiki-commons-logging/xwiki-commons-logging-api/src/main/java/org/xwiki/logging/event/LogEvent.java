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

import javax.inject.Singleton;

import org.slf4j.helpers.MessageFormatter;
import org.xwiki.component.annotation.Component;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.internal.helpers.ExtendedMessageFormatter;
import org.xwiki.observation.event.Event;

/**
 * Sent when logger is called.
 * 
 * @version $Id$
 * @since 3.2M1
 */
@Component
@Singleton
public class LogEvent implements Event
{
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
        this.level = level;
        this.message = message;
        this.argumentArray = argumentArray;
        this.throwable = throwable;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return true;
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
     */
    public List<String> getMessageElements()
    {
        return ExtendedMessageFormatter.parseMessage(getMessage(), getArgumentArray());
    }

    @Override
    public String toString()
    {
        return getLevel().toString() + ':' + getFormattedMessage();
    }
}
