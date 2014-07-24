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
package org.xwiki.component.event;

import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.observation.event.Event;

/**
 * Unit tests for {@link ComponentDescriptorAddedEvent}.
 *
 * @version $Id$
 */
public class ComponentDescriptorAddedEventTest
{
    /**
     * Used in tests below.
     */
    private static class Dummy
    {
    }

    @Test
    public void testMatchesAllComponentRoles()
    {
        ComponentDescriptorAddedEvent event = new ComponentDescriptorAddedEvent();
        // Note: We use any class for the test but it's supposed to be a component role class.
        Assert.assertTrue(event.matches(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint")));
    }

    @Test
    public void testMatchesWhenDifferentEvent()
    {
        ComponentDescriptorAddedEvent event = new ComponentDescriptorAddedEvent();
        Assert.assertFalse(event.matches(new Event()
        {
            @Override
            public boolean matches(Object otherEvent)
            {
                return false;
            }
        }));
    }

    @Test
    public void testMatchesWhenSpecificRoleSpecified()
    {
        // Note: We use any class for the test but it's supposed to be a component role class.
        ComponentDescriptorAddedEvent event = new ComponentDescriptorAddedEvent((Type) Dummy.class);
        Assert.assertTrue(event.matches(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint")));
        // Use a different class so that it doesn't match
        Assert.assertFalse(event.matches(new ComponentDescriptorAddedEvent((Type) ComponentDescriptorAddedEvent.class,
            "rolehint")));
    }

    @Test
    public void testEquals()
    {
        Assert.assertEquals(new ComponentDescriptorAddedEvent(), new ComponentDescriptorAddedEvent());
        Assert.assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class), new ComponentDescriptorAddedEvent(
            (Type) Dummy.class));
        Assert.assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint"),
            new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint"));
    }

    @Test
    public void testHashCode()
    {
        Assert.assertEquals(new ComponentDescriptorAddedEvent().hashCode(),
            new ComponentDescriptorAddedEvent().hashCode());
        Assert.assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class).hashCode(),
            new ComponentDescriptorAddedEvent((Type) Dummy.class).hashCode());
        Assert.assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint").hashCode(),
            new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint").hashCode());
    }
}
