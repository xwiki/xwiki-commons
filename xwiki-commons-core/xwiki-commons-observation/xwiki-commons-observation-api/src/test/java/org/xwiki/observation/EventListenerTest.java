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
package org.xwiki.observation;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.observation.event.ActionExecutionEvent;
import org.xwiki.observation.event.Event;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link AbstractEventListener}.
 *
 * @version $Id$
 */
public class EventListenerTest
{
    static class TestEventListener extends AbstractEventListener
    {
        public TestEventListener(String name, List<? extends Event> events)
        {
            super(name, events);
        }

        public TestEventListener(String name, Event... events)
        {
            super(name, events);
        }

        @Override
        public void onEvent(Event event, Object source, Object data)
        {
        }
    }

    @Test
    public void constructorWithTwoEvents()
    {
        TestEventListener listener =
            new TestEventListener("name", new ActionExecutionEvent("action1"), new ActionExecutionEvent("action2"));

        assertEquals("name", listener.getName());
        assertEquals(Arrays.asList(new ActionExecutionEvent("action1"), new ActionExecutionEvent("action2")),
            listener.getEvents());
    }

    @Test
    public void constructorWithList()
    {
        TestEventListener listener =
            new TestEventListener("name", Arrays.asList(new ActionExecutionEvent("action1"), new ActionExecutionEvent(
                "action2")));

        assertEquals("name", listener.getName());
        assertEquals(Arrays.asList(new ActionExecutionEvent("action1"), new ActionExecutionEvent("action2")),
            listener.getEvents());
    }
}
