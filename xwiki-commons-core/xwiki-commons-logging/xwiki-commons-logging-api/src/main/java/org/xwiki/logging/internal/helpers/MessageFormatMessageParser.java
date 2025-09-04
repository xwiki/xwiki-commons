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

import java.text.MessageFormat;

/**
 * {@link MessageFormat} based message parser.
 * <p>
 * <ul>
 * <li>The escaping character is '</li>
 * <li>If ' is not used to escape a special syntax, it's removed</li>
 * </ul>
 * </p>
 * 
 * @version $Id$
 * @since 16.10.11
 * @since 17.8.0RC1
 * @since 17.4.5
 */
// TODO: add support for conditional argument syntax
public class MessageFormatMessageParser extends AbstractMessageParser
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
     * Character used to escape syntax in message pattern in case of SLF4J.
     */
    private static final char ESCAPE_CHAR = '\'';

    /**
     * @param buffer the content to parse
     */
    public MessageFormatMessageParser(String buffer)
    {
        this(buffer.toCharArray());
    }

    /**
     * @param buffer the content to parse
     */
    public MessageFormatMessageParser(char[] buffer)
    {
        super(buffer, ESCAPE_CHAR);
    }

    @Override
    public MessageElement next()
    {
        StringBuilder str = new StringBuilder(this.buffer.length - this.bufferIndex);

        int i = this.bufferIndex;
        boolean escaped = false;
        for (; i < this.buffer.length; ++i) {
            char c = this.buffer[i];

            if (!escaped) {
                if (c == ESCAPE_CHAR) {
                    // Mark next character as escaped
                    escaped = true;

                    continue;
                } else if (c == ARGUMENT_START) {
                    // If there is already bufferized plain text, stop parsing
                    if (!str.isEmpty()) {
                        break;
                    }

                    // Extract the number and return it if it's value
                    MessageIndex index = extractIndex(i);
                    if (index != null) {
                        // Move the caret
                        this.bufferIndex = i + index.getString().length();

                        // Set the index as current element
                        this.currentMessageElement = index;

                        return index;
                    }
                }
            }

            // Remember the character as plain text
            str.append(c);
            escaped = false;
        }

        // If there is bufferized plain text, return it
        if (!str.isEmpty()) {
            // Move the caret
            this.bufferIndex = i;

            // Create the plain text message element
            this.currentMessageElement = new MessageString(str.toString());

            // Return the plain text message element
            return this.currentMessageElement;
        }

        // We reached the end (or the string is empty)
        return null;
    }

    private MessageIndex extractIndex(int current)
    {
        int i = current;

        if (this.buffer[current] != ARGUMENT_START) {
            return null;
        }

        ++i;

        if (i == this.buffer.length) {
            return null;
        }

        for (; this.buffer[i] != ARGUMENT_STOP; ++i) {
            if (i == this.buffer.length) {
                // We reached the end of the buffer so it cannot be a message index
                return null;
            }

            if (this.buffer[i] < '0' || this.buffer[i] > '9') {
                // It's not a number, so it cannot be a message index
                return null;
            }
        }

        return new MessageIndex(new String(this.buffer, current, i + 1 - current),
            Integer.parseInt(new String(this.buffer, current + 1, i - current - 1)));
    }
}
