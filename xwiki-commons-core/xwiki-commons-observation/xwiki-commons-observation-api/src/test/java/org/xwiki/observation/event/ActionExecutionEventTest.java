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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ActionExecutionEvent}.
 *
 * @version $Id$
 */
public class ActionExecutionEventTest
{
    @Test
    public void testActionExecutionEventString()
    {
        ActionExecutionEvent event = new ActionExecutionEvent("action");

        assertEquals("action", event.getActionName());

        // matches

        assertTrue(event.matches(event));
        assertTrue(event.matches(new ActionExecutionEvent("action")));

        assertFalse(event.matches(new ActionExecutionEvent("action2")));
        assertFalse(event.matches(AllEvent.ALLEVENT));

        // equals

        assertEquals(event, event);
        assertEquals(event, new ActionExecutionEvent("action"));

        assertFalse(event.equals(new ActionExecutionEvent("action2")));
        assertFalse(event.equals(AllEvent.ALLEVENT));

        // hashcode

        assertEquals(event.hashCode(), new ActionExecutionEvent("action").hashCode());
        assertNotEquals(event.hashCode(), new ActionExecutionEvent("action2").hashCode());
    }
}
