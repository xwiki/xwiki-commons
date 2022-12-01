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

/**
 * An exception which support the same parameter system than {@link Message}.
 * 
 * @version $Id$
 * @since 15.0RC1
 * @since 14.10.1
 */
public abstract class AbstractMessageException extends Exception
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    private final Message message;

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param translationKey the key used to find the translation
     * @param message the log message
     * @param arguments the arguments to insert in the message. If the last argument is a {@link Throwable}, it's
     *            extracted as the cause.
     * @see Message#Message
     */
    protected AbstractMessageException(String translationKey, String message, Object... arguments)
    {
        super(LogUtils.getThrowable(arguments));

        this.message = new Message(translationKey, message, arguments);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param marker the log marker
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param cause the throwable associated to the event
     * @see Message#Message(Marker, String, Object[], Throwable)
     */
    protected AbstractMessageException(Marker marker, String message, Object[] argumentArray, Throwable cause)
    {
        super(cause);

        this.message = new Message(marker, message, argumentArray, cause);
    }

    @Override
    public String getMessage()
    {
        return this.message.getFormattedMessage();
    }

    /**
     * @return the actual {@link Message}
     */
    public Message getParameterizedMessage()
    {
        return this.message;
    }
}
