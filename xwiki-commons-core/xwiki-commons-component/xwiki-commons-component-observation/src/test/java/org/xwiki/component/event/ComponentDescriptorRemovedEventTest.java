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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ComponentDescriptorRemovedEvent}.
 *
 * @version $Id$
 */
public class ComponentDescriptorRemovedEventTest
{
    /**
     * Used in tests below.
     */
    private static class Dummy
    {
    }

    @Test
    void matchesAllComponentRoles()
    {
        ComponentDescriptorRemovedEvent event = new ComponentDescriptorRemovedEvent();
        // Note: We use any class for the test but it's supposed to be a component role class.
        assertTrue(event.matches(new ComponentDescriptorRemovedEvent((Type) Dummy.class, "rolehint")));
    }

    @Test
    void matchesWhenDifferentEvent()
    {
        ComponentDescriptorRemovedEvent event = new ComponentDescriptorRemovedEvent();
        assertFalse(event.matches((Event) e -> false));
    }

    @Test
    void matchesWhenSpecificRoleSpecified()
    {
        // Note: We use any class for the test but it's supposed to be a component role class.
        ComponentDescriptorRemovedEvent event = new ComponentDescriptorRemovedEvent((Type) Dummy.class);
        assertTrue(event.matches(new ComponentDescriptorRemovedEvent((Type) Dummy.class, "rolehint")));
        // Use a different class so that it doesn't match
        assertFalse(event.matches(new ComponentDescriptorRemovedEvent((Type) ComponentDescriptorRemovedEvent.class,
            "rolehint")));
    }
}
