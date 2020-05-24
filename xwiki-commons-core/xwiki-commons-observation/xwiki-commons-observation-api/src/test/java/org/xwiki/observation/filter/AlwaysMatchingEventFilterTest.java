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
package org.xwiki.observation.filter;

import org.junit.jupiter.api.Test;
import org.xwiki.observation.event.filter.AlwaysMatchingEventFilter;
import org.xwiki.observation.event.filter.FixedNameEventFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AlwaysMatchingEventFilter}.
 *
 * @version $Id$
 */
public class AlwaysMatchingEventFilterTest
{
    AlwaysMatchingEventFilter filter = AlwaysMatchingEventFilter.INSTANCE;

    @Test
    void testGetFilter()
    {
        assertEquals(".*", this.filter.getFilter());
    }

    @Test
    void testEquals()
    {
        assertFalse(this.filter.equals(null));
        assertFalse(this.filter.equals(new FixedNameEventFilter("filter")));

        assertEquals(this.filter, this.filter);
    }

    @Test
    void testMatches()
    {
        assertTrue(this.filter.matches(null));
        assertTrue(this.filter.matches(new FixedNameEventFilter("filter")));
        assertTrue(this.filter.matches(this.filter));
    }

    @Test
    void testHashcode()
    {
        assertEquals(0, this.filter.hashCode());
    }
}
