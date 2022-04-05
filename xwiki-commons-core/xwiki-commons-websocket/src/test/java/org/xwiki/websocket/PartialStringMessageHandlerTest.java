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
package org.xwiki.websocket;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link AbstractPartialStringMessageHandler}.
 * 
 * @version $Id$
 */
class PartialStringMessageHandlerTest
{
    static class CollectingPartialStringMessageHandler extends AbstractPartialStringMessageHandler
    {
        private final List<String> messages = new LinkedList<>();

        @Override
        public void onMessage(String message)
        {
            this.messages.add(message);
        }

        public List<String> getMessages()
        {
            return this.messages;
        }
    }

    @Test
    void onMessage()
    {
        CollectingPartialStringMessageHandler handler = new CollectingPartialStringMessageHandler();
        assertEquals(0, handler.getMessages().size());

        handler.onMessage("one,", false);
        handler.onMessage("two,", false);
        handler.onMessage("three", true);
        handler.onMessage("four,", false);
        handler.onMessage("five", true);
        handler.onMessage("six,", false);

        assertEquals(Arrays.asList("one,two,three", "four,five"), handler.getMessages());
    }
}
