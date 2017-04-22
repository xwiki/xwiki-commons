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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.observation.event.filter.EventFilter;
import org.xwiki.observation.event.filter.FixedNameEventFilter;

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

    // Tests

    @Test
    public void testAbstractCancelableEvent()
    {
        TestCancelableEvent event = new TestCancelableEvent();

        // matches

        Assert.assertTrue(event.matches(event));
        Assert.assertTrue(event.matches(new TestCancelableEvent("name")));
        Assert.assertTrue(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name2"))));

        Assert.assertFalse(event.matches(new TestCancelableEvent2()));

        // equals

        Assert.assertEquals(event, event);
        Assert.assertEquals(event, new TestCancelableEvent());

        Assert.assertFalse(event.equals(new TestCancelableEvent("name")));
        Assert.assertFalse(event.equals(new TestCancelableEvent(new FixedNameEventFilter("name"))));
        Assert.assertFalse(event.equals(new TestCancelableEvent2()));

        // hashcode

        Assert.assertEquals(event.hashCode(), new TestCancelableEvent().hashCode());
        Assert.assertFalse(event.equals(new TestCancelableEvent2()));

        Assert.assertNotEquals(event.hashCode(), new TestCancelableEvent("name").hashCode());
        Assert.assertNotEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name")).hashCode());
    }

    @Test
    public void testAbstractCancelableEventEventFilter()
    {
        TestCancelableEvent event = new TestCancelableEvent(new FixedNameEventFilter("name"));

        // matches

        Assert.assertTrue(event.matches(event));
        Assert.assertTrue(event.matches(new TestCancelableEvent("name")));
        Assert.assertTrue(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name"))));

        Assert.assertFalse(event.matches(new TestCancelableEvent("name2")));
        Assert.assertFalse(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        Assert.assertFalse(event.matches(new TestCancelableEvent2()));

        // equals

        Assert.assertEquals(event, event);
        Assert.assertEquals(event, new TestCancelableEvent("name"));
        Assert.assertEquals(event, new TestCancelableEvent(new FixedNameEventFilter("name")));

        Assert.assertFalse(event.equals(null));
        Assert.assertFalse(event.equals(new TestCancelableEvent("name2")));
        Assert.assertFalse(event.equals(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        Assert.assertFalse(event.equals(new TestCancelableEvent2()));

        // hashcode

        Assert.assertEquals(event.hashCode(), new TestCancelableEvent("name").hashCode());
        Assert.assertEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name")).hashCode());

        Assert.assertNotEquals(event.hashCode(), new TestCancelableEvent("name2").hashCode());
        Assert.assertNotEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name2")).hashCode());
    }

    @Test
    public void testAbstractCancelableEventString()
    {
        TestCancelableEvent event = new TestCancelableEvent("name");

        // matches

        Assert.assertTrue(event.matches(event));
        Assert.assertTrue(event.matches(new TestCancelableEvent("name")));
        Assert.assertTrue(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name"))));

        Assert.assertFalse(event.matches(new TestCancelableEvent("name2")));
        Assert.assertFalse(event.matches(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        Assert.assertFalse(event.matches(new TestCancelableEvent2()));

        // equals

        Assert.assertEquals(event, event);
        Assert.assertEquals(event, new TestCancelableEvent("name"));
        Assert.assertEquals(event, new TestCancelableEvent(new FixedNameEventFilter("name")));

        Assert.assertFalse(event.equals(null));
        Assert.assertFalse(event.equals(new TestCancelableEvent("name2")));
        Assert.assertFalse(event.equals(new TestCancelableEvent(new FixedNameEventFilter("name2"))));
        Assert.assertFalse(event.equals(new TestCancelableEvent2()));

        // hashcode

        Assert.assertEquals(event.hashCode(), new TestCancelableEvent("name").hashCode());
        Assert.assertEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name")).hashCode());

        Assert.assertNotEquals(event.hashCode(), new TestCancelableEvent("name2").hashCode());
        Assert.assertNotEquals(event.hashCode(), new TestCancelableEvent(new FixedNameEventFilter("name2")).hashCode());
    }

    @Test
    public void testCancel()
    {
        CancelableEvent event = new TestCancelableEvent();
        Assert.assertFalse(event.isCanceled());
        Assert.assertNull(event.getReason());
        event.cancel();
        Assert.assertTrue(event.isCanceled());
        Assert.assertNull(event.getReason());
    }

    @Test
    public void testCancelWithReason()
    {
        String reason = "Tralala";
        CancelableEvent event = new TestCancelableEvent();
        Assert.assertFalse(event.isCanceled());
        Assert.assertNull(event.getReason());
        event.cancel(reason);
        Assert.assertTrue(event.isCanceled());
        Assert.assertEquals(reason, event.getReason());
    }
}
