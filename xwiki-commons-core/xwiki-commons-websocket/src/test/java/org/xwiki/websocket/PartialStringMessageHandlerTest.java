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
import static org.junit.jupiter.api.Assertions.fail;

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

        public CollectingPartialStringMessageHandler()
        {
            super();
        }

        public CollectingPartialStringMessageHandler(int maxMessageSize)
        {
            super(maxMessageSize);
        }

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

    @Test
    void onMessageExceedingMaxSize()
    {
        CollectingPartialStringMessageHandler handler = new CollectingPartialStringMessageHandler(10);
        assertEquals(0, handler.getMessages().size());

        try {
            // Exceed the maximum message size on the first message part.
            handler.onMessage("alice,carol,", false);
            fail(
                "Expected an IllegalStateException to be thrown since the message size exceeds the configured limit of [10].");
        } catch (IllegalStateException e) {
            assertEquals("Message size exceeds the configured limit of [10].", e.getMessage());
        }

        handler = new CollectingPartialStringMessageHandler(10);
        assertEquals(0, handler.getMessages().size());

        handler.onMessage("alice,", false);
        try {
            // Exceed the maximum message size on an intermediate message part.
            handler.onMessage("carol,", false);
            fail(
                "Expected an IllegalStateException to be thrown since the message size exceeds the configured limit of [10].");
        } catch (IllegalStateException e) {
            assertEquals("Message size exceeds the configured limit of [10].", e.getMessage());
        }

        handler = new CollectingPartialStringMessageHandler(10);
        assertEquals(0, handler.getMessages().size());

        handler.onMessage("alice,", false);
        try {
            // Exceed the maximum message size on the final message part.
            handler.onMessage("carol,", true);
            fail(
                "Expected an IllegalStateException to be thrown since the message size exceeds the configured limit of [10].");
        } catch (IllegalStateException e) {
            assertEquals("Message size exceeds the configured limit of [10].", e.getMessage());
        }
    }
}
