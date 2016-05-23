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

public class MessageParser
{
    /**
     * Argument syntax in message pattern.
     */
    public static final String ARGUMENT_STR = "{}";

    /**
     * Argument start syntax in message pattern.
     */
    static final char ARGUMENT_START = '{';

    /**
     * Argument end syntax in message pattern.
     */
    static final char ARGUMENT_STOP = '}';

    /**
     * Character used to escape syntax in message pattern.
     */
    private static final char ESCAPE_CHAR = '\\';

    public static class MessageElement
    {
        private String string;

        protected MessageElement(String string)
        {
            this.string = string;
        }

        public String getString()
        {
            return this.string;
        }
    }

    public static class MessageIndex extends MessageElement
    {
        private int index;

        public MessageIndex(String string, int index)
        {
            super(string);

            this.index = index;
        }

        public int getIndex()
        {
            return this.index;
        }
    }

    public static class MessageString extends MessageElement
    {
        public MessageString(String string)
        {
            super(string);
        }
    }

    private char[] buffer;

    private boolean translations;

    private int bufferIndex;

    private int currentMessageIndex;

    private boolean previousWasDoubleEscaped;

    private MessageElement currentMessageElement;

    public MessageParser(String buffer, boolean translations)
    {
        this(buffer.toCharArray(), translations);
    }

    public MessageParser(char[] buffer, boolean translations)
    {
        this.buffer = buffer;
        this.translations = translations;
    }

    public MessageElement getCurrentMessageElement()
    {
        return this.currentMessageElement;
    }

    public MessageElement next()
    {
        StringBuilder str = new StringBuilder();

        int i = this.bufferIndex;
        for (; i < this.buffer.length; ++i) {
            if (this.buffer[i] == ESCAPE_CHAR) {
                int nb = countEscaping(i);

                int iNext = i + nb;

                if (iNext == this.buffer.length) {
                    i = iNext;
                    break;
                } else {
                    if (isMessageIndex(iNext) > iNext) {
                        str.append(this.buffer, this.bufferIndex, iNext - 1 - this.bufferIndex);
                        i = iNext;
                        this.bufferIndex = i;

                        // Only the last two escaping characters are taken into account, all the previous are plain text
                        if (nb > 1) {
                            this.previousWasDoubleEscaped = true;
                            this.currentMessageElement = new MessageString(str.toString());

                            return this.currentMessageElement;
                        }
                    } else {
                        i = iNext;
                    }
                }
            } else if (this.buffer[i] == ARGUMENT_START) {
                int iNext = isMessageIndex(i);
                if (iNext != i) {
                    if (this.bufferIndex == i) {
                        // Create and return new MessageIndex
                        String messageIndexString = String.valueOf(this.buffer, i, iNext - i);

                        int messageIndex;
                        if (this.translations && messageIndexString.length() > 2) {
                            messageIndex =
                                Integer.parseInt(messageIndexString.substring(1, messageIndexString.length() - 1));
                        } else {
                            messageIndex = this.currentMessageIndex;
                        }
                        ++this.currentMessageIndex;

                        if (this.previousWasDoubleEscaped) {
                            this.currentMessageElement = new MessageIndex("\\" + messageIndexString, messageIndex);
                        } else {
                            this.currentMessageElement = new MessageIndex(messageIndexString, messageIndex);
                        }
                        this.bufferIndex = iNext;

                        return this.currentMessageElement;
                    } else {
                        // Return previous plain text, the actual MessageIndex will be the next element
                        this.currentMessageElement =
                            new MessageString(String.valueOf(this.buffer, this.bufferIndex, i - this.bufferIndex));
                        this.bufferIndex = i;

                        return this.currentMessageElement;
                    }
                }
            }
        }

        if (i == this.bufferIndex) {
            return null;
        }

        this.currentMessageElement = new MessageString(String.valueOf(this.buffer, this.bufferIndex, i - this.bufferIndex));

        this.bufferIndex = i;

        return this.currentMessageElement;
    }

    private int countEscaping(int current)
    {
        int nb = 1;

        for (int i = current + 1; i < this.buffer.length && this.buffer[i] == ESCAPE_CHAR; ++i) {
            ++nb;
        }

        return nb;
    }

    private int isMessageIndex(int current)
    {
        int i = current;

        if (this.buffer[current] != ARGUMENT_START) {
            return current;
        }

        ++i;

        if (i == this.buffer.length) {
            return current;
        }

        if (this.translations) {
            for (; this.buffer[i] != ARGUMENT_STOP; ++i) {
                if (i == this.buffer.length) {
                    return current;
                } else if (this.buffer[i] < '0' || this.buffer[i] > '9') {
                    return current;
                }
            }
        }

        return this.buffer[i] == ARGUMENT_STOP ? i + 1 : current;
    }
}
