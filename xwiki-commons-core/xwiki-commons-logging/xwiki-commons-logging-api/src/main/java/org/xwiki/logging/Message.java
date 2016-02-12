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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;
import org.xwiki.logging.internal.helpers.ExtendedMessageFormatter;
import org.xwiki.logging.marker.ContainerMarker;
import org.xwiki.logging.marker.TranslationMarker;

/**
 * A translatable message.
 *
 * @version $Id$
 * @since 7.1M2
 */
public class Message implements Serializable, CharSequence
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see #getMarker()
     */
    private Marker marker;

    /**
     * @see Message#getMessage()
     */
    private String message;

    /**
     * @see #getArgumentArray()
     */
    private Object[] argumentArray;

    /**
     * @see Message#getThrowable()
     */
    private Throwable throwable;

    /**
     * Formatted version of the message.
     */
    private transient String formattedMessage;

    /**
     * Matches any {@link Message}.
     */
    protected Message()
    {

    }

    /**
     * @param message the message
     */
    public Message(String message)
    {
        this(new TranslationMarker(message), message, null, null);
    }

    /**
     * @param message the message to copy
     */
    public Message(Message message)
    {
        this(message.getMarker(), message.getMessage(), message.getArgumentArray(), message.getThrowable());
    }

    /**
     * @param message the message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     */
    public Message(String message, Object[] argumentArray, Throwable throwable)
    {
        this(null, message, argumentArray, throwable);
    }

    /**
     * @param translationKey the key used to find the translation
     * @param message the log message
     * @param arguments the arguments to insert in the message
     */
    public Message(String translationKey, String message, Object... arguments)
    {
        this(new TranslationMarker(translationKey), message, arguments);
    }

    /**
     * @param marker the log marker
     * @param message the log message
     * @param arguments the arguments to insert in the message
     */
    public Message(Marker marker, String message, Object... arguments)
    {
        Object[] actualArray;
        Throwable actualThrowable;
        if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            actualArray = Arrays.copyOf(arguments, arguments.length - 1);
            actualThrowable = (Throwable) arguments[arguments.length - 1];
        } else {
            actualArray = arguments;
            actualThrowable = null;
        }

        this.marker = marker;
        this.message = message;
        this.argumentArray = actualArray;
        this.throwable = actualThrowable;
    }

    /**
     * @param marker the log marker
     * @param message the log message
     * @param argumentArray the event arguments to insert in the message
     * @param throwable the throwable associated to the event
     */
    public Message(Marker marker, String message, Object[] argumentArray, Throwable throwable)
    {
        this.marker = marker;
        this.message = message;
        this.argumentArray = argumentArray;
        this.throwable = throwable;
    }

    /**
     * @param marker the marker containing the translation key
     * @return the translation key associated to the Marker
     * @since 8.0M2
     */
    public static String getTranslationKey(Marker marker)
    {
        if (marker instanceof ContainerMarker) {
            ContainerMarker containerMarker = (ContainerMarker) marker;

            TranslationMarker translationMarker = containerMarker.get(TranslationMarker.NAME);

            if (translationMarker != null) {
                return translationMarker.getTranslationKey();
            }
        }

        return null;
    }

    /**
     * @return the log marker
     */
    public Marker getMarker()
    {
        return this.marker;
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
        return getTranslationKey(getMarker());
    }

    // Object

    @Override
    public String toString()
    {
        return getFormattedMessage();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(7, 11).append(getMarker()).append(getMessage()).append(getArgumentArray())
            .append(getThrowable()).toHashCode();
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
            if (object instanceof String) {
                return equals((String) object);
            } else {
                return false;
            }
        }

        Message rhs = (Message) object;
        return new EqualsBuilder().append(getMarker(), rhs.getMarker()).append(getMessage(), rhs.getMessage())
            .append(getArgumentArray(), rhs.getArgumentArray()).append(getThrowable(), rhs.getThrowable()).isEquals();
    }

    /**
     * Helper to compare the message to the default way of displaying this {@link Message}.
     * 
     * @param formatedMessage the formatted message
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    public boolean equals(String formatedMessage)
    {
        return getFormattedMessage().equals(formatedMessage);
    }

    // CharSequence

    @Override
    public int length()
    {
        return getFormattedMessage().length();
    }

    @Override
    public char charAt(int index)
    {
        return getFormattedMessage().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end)
    {
        return getFormattedMessage().subSequence(start, end);
    }
}
