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
 * SLF4J based message parser.
 * <p>
 * <ul>
 * <li>The escaping character is \</li>
 * <li>If \ is not used to escape a special syntax, it's a plain character</li>
 * </ul>
 * </p>
 * 
 * @version $Id$
 * @since 16.10.11
 * @since 17.8.0RC1
 * @since 17.4.5
 */
public class SLF4JMessageParser extends AbstractMessageParser
{
    /**
     * Argument syntax in message pattern.
     */
    public static final String ARGUMENT_STR = "{}";

    /**
     * Character used to escape syntax in message pattern.
     */
    private static final char ESCAPE_CHAR = '\\';

    private int currentMessageIndex;

    /**
     * @param buffer the content to parser
     */
    public SLF4JMessageParser(String buffer)
    {
        this(buffer.toCharArray());
    }

    /**
     * @param buffer the content to parser
     */
    public SLF4JMessageParser(char[] buffer)
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

            // In SLF4J syntax an escaping character escape all following escaping characters
            if (shouldStopEscaping(escaped, i)) {
                escaped = false;
            }
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

    private boolean shouldStopEscaping(boolean escaped, int current)
    {
        return escaped && this.buffer[current + 1] != ESCAPE_CHAR;
    }

    private boolean isEscaped(int current)
    {
        if (current < this.buffer.length) {
            char nextChar = this.buffer[current + 1];

            if (nextChar == ESCAPE_CHAR || nextChar == ARGUMENT_START) {
                return true;
            }
        }

        return false;
    }

    private MessageIndex extractIndex(int current)
    {
        int i = current;

        if (this.buffer[current] != ARGUMENT_START) {
            return null;
        }

        ++i;

        if (i == this.buffer.length || this.buffer[i] != ARGUMENT_STOP) {
            return null;
        }

        return new MessageIndex(ARGUMENT_STR, this.currentMessageIndex++);
    }

    protected int countEscaping(int current)
    {
        int nb = 1;

        for (int i = current + 1; i < this.buffer.length && this.buffer[i] == this.escapeChar; ++i) {
            ++nb;
        }

        return nb;
    }

    protected int isMessageIndex(int current)
    {
        int i = current;

        if (this.buffer[current] != ARGUMENT_START) {
            return current;
        }

        ++i;

        if (i == this.buffer.length) {
            return current;
        }

        return this.buffer[i] == ARGUMENT_STOP ? i + 1 : current;
    }
}
