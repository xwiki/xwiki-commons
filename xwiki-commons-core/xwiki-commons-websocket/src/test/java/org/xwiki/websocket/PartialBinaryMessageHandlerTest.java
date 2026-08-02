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

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AbstractPartialStringMessageHandler}.
 * 
 * @version $Id$
 */
class PartialBinaryMessageHandlerTest
{
    static class CollectingPartialBinaryMessageHandler extends AbstractPartialBinaryMessageHandler
    {
        private final List<byte[]> messages = new LinkedList<>();

        public CollectingPartialBinaryMessageHandler(int maxMessageSize)
        {
            super(maxMessageSize);
        }

        @Override
        public void onMessage(byte[] message)
        {
            this.messages.add(message);
        }

        public List<byte[]> getMessages()
        {
            return this.messages;
        }
    }

    @Test
    void onMessage()
    {
        CollectingPartialBinaryMessageHandler handler = new CollectingPartialBinaryMessageHandler(-1);
        assertEquals(0, handler.getMessages().size());

        handler.onMessage(new byte[] {1, 2, 3}, false);
        handler.onMessage(new byte[] {4, 5}, false);
        handler.onMessage(new byte[] {6}, true);
        handler.onMessage(new byte[] {7, 8}, false);
        handler.onMessage(new byte[] {9, 10, 11}, true);
        handler.onMessage(new byte[] {12}, false);

        assertEquals(2, handler.getMessages().size());
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6}, handler.getMessages().get(0));
        assertArrayEquals(new byte[] {7, 8, 9, 10, 11}, handler.getMessages().get(1));
    }

    @Test
    void onMessageExceedingMaxSize()
    {
        CollectingPartialBinaryMessageHandler firstPartHandler = new CollectingPartialBinaryMessageHandler(5);
        assertEquals(0, firstPartHandler.getMessages().size());

        // Exceed the maximum message size on the first message part.
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> firstPartHandler.onMessage(new byte[] {1, 2, 3, 4, 5, 6}, false));
        assertEquals("Message size exceeds the configured limit of [5].", exception.getMessage());

        CollectingPartialBinaryMessageHandler intermediatePartHandler = new CollectingPartialBinaryMessageHandler(5);
        assertEquals(0, intermediatePartHandler.getMessages().size());

        intermediatePartHandler.onMessage(new byte[] {1, 2, 3}, false);
        // Exceed the maximum message size on an intermediate message part.
        exception = assertThrows(IllegalStateException.class,
            () -> intermediatePartHandler.onMessage(new byte[] {4, 5, 6}, false));
        assertEquals("Message size exceeds the configured limit of [5].", exception.getMessage());

        CollectingPartialBinaryMessageHandler finalPartHandler = new CollectingPartialBinaryMessageHandler(5);
        assertEquals(0, finalPartHandler.getMessages().size());

        finalPartHandler.onMessage(new byte[] {1, 2, 3}, false);
        // Exceed the maximum message size on the final message part.
        exception = assertThrows(IllegalStateException.class,
            () -> finalPartHandler.onMessage(new byte[] {4, 5, 6}, true));
        assertEquals("Message size exceeds the configured limit of [5].", exception.getMessage());
    }
}
