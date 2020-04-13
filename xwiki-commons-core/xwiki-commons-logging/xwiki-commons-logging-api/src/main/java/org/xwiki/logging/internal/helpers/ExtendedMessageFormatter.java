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
import java.util.Arrays;
import java.util.List;

import org.xwiki.logging.internal.helpers.MessageParser.MessageElement;
import org.xwiki.logging.internal.helpers.MessageParser.MessageIndex;
import org.xwiki.logging.internal.helpers.MessageParser.MessageString;

/**
 * Provide what is missing in {@link org.slf4j.helpers.MessageFormatter}.
 *
 * @version $Id$
 * @since 4.2M1
 */
// TODO: remove as soon as all that is provided by org.slf4j.helpers.MessageFormatter
public final class ExtendedMessageFormatter
{
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

        if (arguments == null || arguments.length == 0) {
            return Arrays.asList(messagePattern);
        }

        List<String> messageList = new ArrayList<>(arguments.length + 1);

        MessageParser parser = new MessageParser(messagePattern, false);

        StringBuilder lastElement = new StringBuilder();
        for (MessageElement element = parser.next(), previous = null; element != null; previous = element, element =
                parser.next()) {
            if (arguments.length < messageList.size()) {
                lastElement.append(element.getString());
            } else {
                if (element instanceof MessageString) {
                    messageList.add(element.getString());
                } else if (!(previous instanceof MessageString)) {
                    messageList.add("");
                }
            }
        }

        if (lastElement.length() > 0 || parser.getCurrentMessageElement() instanceof MessageIndex) {
            messageList.add(lastElement.toString());
        }

        return messageList;
    }
}
