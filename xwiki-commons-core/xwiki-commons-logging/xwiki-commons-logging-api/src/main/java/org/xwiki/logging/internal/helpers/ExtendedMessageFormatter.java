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

import java.util.ArrayList;
import java.util.List;

/**
 * Provide what is missing in {@link org.slf4j.helpers.MessageFormatter}.
 * 
 * @version $Id$
 */
// TODO: remove as soon as all that is provided by org.slf4j.helpers.MessageFormatter
public final class ExtendedMessageFormatter
{
    /**
     * Argument start syntax in message pattern.
     */
    static final char DELIM_START = '{';

    /**
     * Argument end syntax in message pattern.
     */
    static final char DELIM_STOP = '}';

    /**
     * Argument syntax in message pattern.
     */
    static final String DELIM_STR = "{}";

    /**
     * Character used to escape syntax in message pattern.
     */
    private static final char ESCAPE_CHAR = '\\';

    /**
     * Default constructor.
     */
    private ExtendedMessageFormatter()
    {

    }

    /**
     * @param messagePattern the message pattern to parse
     * @param arguments the arguments
     * @return the list version of the message pattern
     */
    public static List<String> parseMessage(final String messagePattern, Object[] arguments)
    {
        if (messagePattern == null) {
            return null;
        }

        if (arguments == null) {
            return null;
        }

        List<String> messageList = new ArrayList<String>(arguments.length + 1);

        int i = 0;
        int j;

        int argumentsIndex;
        for (argumentsIndex = 0; argumentsIndex < arguments.length; ++argumentsIndex) {

            j = messagePattern.indexOf(DELIM_STR, i);

            if (j == -1) {
                // no more variables
                if (i == 0) {
                    // this is a simple string
                    messageList.add(messagePattern);

                    return messageList;
                } else {
                    // add the tail string which contains no variables and return the result.
                    messageList.add(messagePattern.substring(i, messagePattern.length()));

                    return messageList;
                }
            } else {
                if (isEscapedDelimeter(messagePattern, j)) {
                    if (!isDoubleEscaped(messagePattern, j)) {
                        // DELIM_START was escaped, thus should not be incremented
                        --argumentsIndex;
                        String str = messagePattern.substring(i, j - 1) + DELIM_START;
                        if (messageList.isEmpty()) {
                            messageList.add(str);
                        } else {
                            messageList.set(messageList.size() - 1, messageList.get(messageList.size() - 1) + str);
                        }
                        i = j + 1;
                    } else {
                        // The escape character preceding the delimiter start is
                        // itself escaped: "abc x:\\{}" we have to consume one backward slash
                        messageList.add(messagePattern.substring(i, j - 1));
                        i = j + 2;
                    }
                } else {
                    // normal case
                    messageList.add(messagePattern.substring(i, j));
                    i = j + 2;
                }
            }
        }

        // append the characters following the last {} pair.
        messageList.add(messagePattern.substring(i, messagePattern.length()));

        return messageList;
    }

    /**
     * @param messagePattern the message pattern
     * @param delimeterStartIndex the index of the start argument
     * @return true if the syntax is escaped twice
     */
    static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex)
    {
        return delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR;
    }

    /**
     * @param messagePattern the message pattern
     * @param delimeterStartIndex the index of the start argument
     * @return true if the syntax is escaped
     */
    static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex)
    {
        if (delimeterStartIndex == 0) {
            return false;
        }

        return messagePattern.charAt(delimeterStartIndex - 1) == ESCAPE_CHAR;
    }
}
