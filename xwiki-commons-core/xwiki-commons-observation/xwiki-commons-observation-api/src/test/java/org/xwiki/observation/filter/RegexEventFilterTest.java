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
import org.xwiki.observation.event.filter.FixedNameEventFilter;
import org.xwiki.observation.event.filter.RegexEventFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RegexEventFilter}.
 *
 * @version $Id$
 */
class RegexEventFilterTest
{
    @Test
    void testRegexEventFilter()
    {
        RegexEventFilter regex = new RegexEventFilter("a.*");

        assertEquals("a.*", regex.getFilter());

        assertTrue(regex.matches(new FixedNameEventFilter("aa")));
        assertFalse(regex.matches(new FixedNameEventFilter("bb")));
    }

    @Test
    void equals()
    {
        RegexEventFilter regexA1 = new RegexEventFilter("a.*");
        RegexEventFilter regexA2 = new RegexEventFilter("a.*");
        RegexEventFilter regexB1 = new RegexEventFilter("b.*");
        RegexEventFilter regexB2 = new RegexEventFilter("b.*");

        assertEquals(regexA1, regexA2);
        assertEquals(regexA1, regexA1);
        assertNotEquals(regexA1, regexB1);
        assertNotEquals(null, regexA1);
        assertNotEquals(regexA1, new Object());
        assertEquals(regexB1, regexB2);
    }

    @Test
    void testHashCode()
    {
        int regexA1 = new RegexEventFilter("a.*").hashCode();
        int regexA2 = new RegexEventFilter("a.*").hashCode();
        int regexB1 = new RegexEventFilter("b.*").hashCode();
        int regexB2 = new RegexEventFilter("b.*").hashCode();

        assertEquals(regexA1, regexA2);
        assertNotEquals(regexA1, regexB1);
        assertEquals(regexB1, regexB2);
    }
}
