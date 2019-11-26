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
package org.xwiki.observation.event;

import org.junit.jupiter.api.Test;
import org.xwiki.observation.event.filter.EventFilter;
import org.xwiki.observation.event.filter.FixedNameEventFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AbstractCancelableEvent}.
 *
 * @version $Id$
 */
public class CancelableEventTest
{
    static class TestCancelableEvent extends AbstractCancelableEvent
    {
        public TestCancelableEvent()
        {
        }

        public TestCancelableEvent(String name)
        {
            super(name);
        }

        public TestCancelableEvent(EventFilter eventFilter)
        {
            super(eventFilter);
        }
    }

    static class TestCancelableEvent2 extends AbstractCancelableEvent
    {
    }

    @Test
    public void matchesEqualsHashcodeWhenDefaultConstructor()
    {
        TestCancelableEvent event = new TestCancelableEvent();

        // matches

        assertTrue(event.matches(event));
        assertTrue(event.matches(new TestCancelableEvent("name")));
        assertTrue(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name2"))));

        assertFalse(event.matches(new TestCancelableEvent2()));

        // equals

        assertEquals(event, event);
        assertEquals(event, new TestCancelableEvent());

        assertFalse(event.equals(new TestCancelableEvent("name")));
        assertFalse(event.equals(new TestCancelableEvent(new FixedNameEventFilter("name"))));
        assertFalse(event.equals(new TestCancelableEvent2()));

        // hashcode

        assertEquals(event.hashCode(), new TestCancelableEvent().hashCode());
        assertFalse(event.equals(new TestCancelableEvent2()));

        assertNotEquals(event.hashCode(), new TestCancelableEvent("name").hashCode());
        assertNotEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name")).hashCode());
    }

    @Test
    public void matchesEqualsHashcodeWhenNull()
    {
        TestCancelableEvent event = new TestCancelableEvent((String) null);

        // matches

        assertTrue(event.matches(event));
        assertTrue(event.matches(new TestCancelableEvent("name")));
        assertTrue(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name2"))));

        assertFalse(event.matches(new TestCancelableEvent2()));

        // equals

        assertEquals(event, event);
        assertEquals(event, new TestCancelableEvent());

        assertFalse(event.equals(new TestCancelableEvent("name")));
        assertFalse(event.equals(new TestCancelableEvent(new FixedNameEventFilter("name"))));
        assertFalse(event.equals(new TestCancelableEvent2()));

        // hashcode

        assertEquals(event.hashCode(), new TestCancelableEvent().hashCode());
        assertFalse(event.equals(new TestCancelableEvent2()));

        assertNotEquals(event.hashCode(), new TestCancelableEvent("name").hashCode());
        assertNotEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name")).hashCode());
    }

    @Test
    public void matchesEqualsHashcodeWhenFixedNameEvenFilterConstructor()
    {
        TestCancelableEvent event = new TestCancelableEvent(new FixedNameEventFilter("name"));

        // matches

        assertTrue(event.matches(event));
        assertTrue(event.matches(new TestCancelableEvent("name")));
        assertTrue(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name"))));

        assertFalse(event.matches(new TestCancelableEvent("name2")));
        assertFalse(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        assertFalse(event.matches(new TestCancelableEvent2()));

        // equals

        assertEquals(event, event);
        assertEquals(event, new TestCancelableEvent("name"));
        assertEquals(event, new TestCancelableEvent(new FixedNameEventFilter("name")));

        assertFalse(event.equals(null));
        assertFalse(event.equals(new TestCancelableEvent("name2")));
        assertFalse(event.equals(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        assertFalse(event.equals(new TestCancelableEvent2()));

        // hashcode

        assertEquals(event.hashCode(), new TestCancelableEvent("name").hashCode());
        assertEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name")).hashCode());

        assertNotEquals(event.hashCode(), new TestCancelableEvent("name2").hashCode());
        assertNotEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name2")).hashCode());
    }

    @Test
    public void matchesEqualsHashcodeWhenStringConstructor()
    {
        TestCancelableEvent event = new TestCancelableEvent("name");

        // matches

        assertTrue(event.matches(event));
        assertTrue(event.matches(new TestCancelableEvent("name")));
        assertTrue(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name"))));

        assertFalse(event.matches(new TestCancelableEvent("name2")));
        assertFalse(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        assertFalse(event.matches(new TestCancelableEvent2()));

        // equals

        assertEquals(event, event);
        assertEquals(event, new TestCancelableEvent("name"));
        assertEquals(event, new TestCancelableEvent(new FixedNameEventFilter("name")));

        assertFalse(event.equals(null));
        assertFalse(event.equals(new TestCancelableEvent("name2")));
        assertFalse(event.equals(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        assertFalse(event.equals(new TestCancelableEvent2()));

        // hashcode

        assertEquals(event.hashCode(), new TestCancelableEvent("name").hashCode());
        assertEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name")).hashCode());

        assertNotEquals(event.hashCode(), new TestCancelableEvent("name2").hashCode());
        assertNotEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name2")).hashCode());
    }

    @Test
    public void cancel()
    {
        CancelableEvent event = new TestCancelableEvent();
        assertFalse(event.isCanceled());
        assertNull(event.getReason());
        event.cancel();
        assertTrue(event.isCanceled());
        assertNull(event.getReason());
    }

    @Test
    public void cancelWithReason()
    {
        String reason = "Tralala";
        CancelableEvent event = new TestCancelableEvent();
        assertFalse(event.isCanceled());
        assertNull(event.getReason());
        event.cancel(reason);
        assertTrue(event.isCanceled());
        assertEquals(reason, event.getReason());
    }
}
