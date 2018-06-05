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

import org.junit.jupiter.api.Test;
import org.xwiki.observation.event.Event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void matchesAllComponentRoles()
    {
        ComponentDescriptorAddedEvent event = new ComponentDescriptorAddedEvent();
        // Note: We use any class for the test but it's supposed to be a component role class.
        assertTrue(event.matches(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint")));
    }

    @Test
    public void matchesWhenDifferentEvent()
    {
        ComponentDescriptorAddedEvent event = new ComponentDescriptorAddedEvent();
        assertFalse(event.matches((Event) e -> false));
    }

    @Test
    public void matchesWhenSpecificRoleSpecified()
    {
        // Note: We use any class for the test but it's supposed to be a component role class.
        ComponentDescriptorAddedEvent event = new ComponentDescriptorAddedEvent((Type) Dummy.class);
        assertTrue(event.matches(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint")));
        // Use a different class so that it doesn't match
        assertFalse(event.matches(new ComponentDescriptorAddedEvent((Type) ComponentDescriptorAddedEvent.class,
            "rolehint")));
    }

    @Test
    public void testEquals()
    {
        assertEquals(new ComponentDescriptorAddedEvent(), new ComponentDescriptorAddedEvent());
        assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class), new ComponentDescriptorAddedEvent(
            (Type) Dummy.class));
        assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint"),
            new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint"));
    }

    @Test
    public void testHashCode()
    {
        assertEquals(new ComponentDescriptorAddedEvent().hashCode(),
            new ComponentDescriptorAddedEvent().hashCode());
        assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class).hashCode(),
            new ComponentDescriptorAddedEvent((Type) Dummy.class).hashCode());
        assertEquals(new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint").hashCode(),
            new ComponentDescriptorAddedEvent((Type) Dummy.class, "rolehint").hashCode());
    }
}
