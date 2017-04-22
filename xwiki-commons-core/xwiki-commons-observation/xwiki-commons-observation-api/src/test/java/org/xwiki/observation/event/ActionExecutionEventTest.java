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

public class ActionExecutionEventTest
{
    // Tests

    @Test
    public void testActionExecutionEventString()
    {
        ActionExecutionEvent event = new ActionExecutionEvent("action");

        Assert.assertEquals("action", event.getActionName());

        // matches

        Assert.assertTrue(event.matches(event));
        Assert.assertTrue(event.matches(new ActionExecutionEvent("action")));

        Assert.assertFalse(event.matches(new ActionExecutionEvent("action2")));
        Assert.assertFalse(event.matches(AllEvent.ALLEVENT));

        // equals

        Assert.assertEquals(event, event);
        Assert.assertEquals(event, new ActionExecutionEvent("action"));

        Assert.assertFalse(event.equals(new ActionExecutionEvent("action2")));
        Assert.assertFalse(event.equals(AllEvent.ALLEVENT));

        // hashcode

        Assert.assertEquals(event.hashCode(), new ActionExecutionEvent("action").hashCode());

        Assert.assertNotEquals(event.hashCode(), new ActionExecutionEvent("action2").hashCode());
    }
}
