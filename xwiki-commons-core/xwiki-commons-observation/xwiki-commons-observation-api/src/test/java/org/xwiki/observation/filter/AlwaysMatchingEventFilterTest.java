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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.observation.event.filter.AlwaysMatchingEventFilter;
import org.xwiki.observation.event.filter.FixedNameEventFilter;

public class AlwaysMatchingEventFilterTest
{
    AlwaysMatchingEventFilter filter = AlwaysMatchingEventFilter.INSTANCE;

    @Test
    public void testGetFilter()
    {
        Assert.assertEquals(".*", this.filter.getFilter());
    }

    @Test
    public void testEquals()
    {
        Assert.assertFalse(this.filter.equals(null));
        Assert.assertFalse(this.filter.equals(new FixedNameEventFilter("filter")));

        Assert.assertEquals(this.filter, this.filter);
    }

    @Test
    public void testMatches()
    {
        Assert.assertTrue(this.filter.matches(null));
        Assert.assertTrue(this.filter.matches(new FixedNameEventFilter("filter")));
        Assert.assertTrue(this.filter.matches(this.filter));
    }

    @Test
    public void testHashcode()
    {
        Assert.assertEquals(0, this.filter.hashCode());
    }
}
