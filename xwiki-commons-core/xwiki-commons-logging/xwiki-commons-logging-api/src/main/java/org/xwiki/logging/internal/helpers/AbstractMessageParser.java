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
package org.xwiki.logging.internal.helpers;

/**
 * Base class to implement a message parser.
 * 
 * @version $Id$
 */
public abstract class AbstractMessageParser
{
    /**
     * Argument start syntax in message pattern.
     */
    static final char ARGUMENT_START = '{';

    /**
     * Argument end syntax in message pattern.
     */
    static final char ARGUMENT_STOP = '}';

    /**
     * A message element.
     * 
     * @version $Id$
     */
    public static class MessageElement
    {
        private String string;

        protected MessageElement(String string)
        {
            this.string = string;
        }

        /**
         * @return the string associated with this element
         */
        public String getString()
        {
            return this.string;
        }

        @Override
        public String toString()
        {
            return getString();
        }
    }

    /**
     * An element describing an indexed place holder.
     * 
     * @version $Id$
     */
    public static class MessageIndex extends MessageElement
    {
        private int index;

        /**
         * @param string the string of the element
         * @param index the index of the place holder
         */
        public MessageIndex(String string, int index)
        {
            super(string);

            this.index = index;
        }

        /**
         * @return the index of the place holder
         */
        public int getIndex()
        {
            return this.index;
        }

        @Override
        public String toString()
        {
            return getIndex() + ':' + getString();
        }
    }

    /**
     * A plain text element.
     * 
     * @version $Id$
     */
    public static class MessageString extends MessageElement
    {
        /**
         * @param string the text of the element
         */
        public MessageString(String string)
        {
            super(string);
        }
    }

    protected final char[] buffer;

    protected int bufferIndex;

    protected final char escapeChar;

    protected MessageElement currentMessageElement;

    protected AbstractMessageParser(char[] buffer, char escapeChar)
    {
        this.buffer = buffer;
        this.escapeChar = escapeChar;
    }

    /**
     * @return the current element
     */
    public MessageElement getCurrentMessageElement()
    {
        return this.currentMessageElement;
    }

    /**
     * Move the current element and return it.
     * 
     * @return the current element
     */
    public MessageElement next()
    {
        StringBuilder str = new StringBuilder(this.buffer.length - this.bufferIndex);

        int i = this.bufferIndex;
        boolean escaped = false;
        for (; i < this.buffer.length; ++i) {
            char c = this.buffer[i];

            if (!escaped) {
                if (c == this.escapeChar) {
                    if (isEscaped(i)) {
                        // Mark next character as escaped
                        escaped = true;

                        continue;
                    }
                } else if (c == ARGUMENT_START) {
                    // If there is already bufferized plain text, stop parsing
                    if (!str.isEmpty()) {
                        break;
                    }

                    // Extract the number and return it if it's valid
                    MessageIndex index = handleMessageIndex(i);
                    if (index != null) {
                        return index;
                    }
                }
            }

            // Remember the character as plain text
            str.append(c);

            // Remove the escaping marker, if needed
            if (shouldStopEscaping(escaped, i)) {
                escaped = false;
            }
        }

        // If there is bufferized plain text, return it
        if (!str.isEmpty()) {
            return handleMessageString(str, i);
        }

        // We reached the end (or the string is empty)
        return null;
    }

    protected MessageIndex handleMessageIndex(int current)
    {
        MessageIndex index = extractIndex(current);
        if (index != null) {
            // Move the caret
            this.bufferIndex = current + index.getString().length();

            // Set the index as current element
            this.currentMessageElement = index;

            return index;
        }

        return null;
    }

    protected abstract MessageIndex extractIndex(int current);

    protected MessageElement handleMessageString(StringBuilder str, int current)
    {
        // Move the caret
        this.bufferIndex = current;

        // Create the plain text message element
        this.currentMessageElement = new MessageString(str.toString());

        // Return the plain text message element
        return this.currentMessageElement;
    }

    protected abstract boolean isEscaped(int current);

    protected abstract boolean shouldStopEscaping(boolean escaped, int current);
}
