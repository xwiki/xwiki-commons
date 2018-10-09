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
package org.xwiki.extension.version.internal;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.test.TestVersion;
import org.xwiki.extension.version.InvalidVersionRangeException;
import org.xwiki.extension.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultVersionRangeTest
{
    @Test
    public void isCompatible() throws InvalidVersionRangeException
    {
        // Compatible
        assertTrue(new DefaultVersionRange("[1.0]").isCompatible(new DefaultVersionRange("[1.0]")));
        assertTrue(new DefaultVersionRange("[1.0,2.0]").isCompatible(new DefaultVersionRange("[1.0]")));
        assertTrue(new DefaultVersionRange("[1.0,2.0]").isCompatible(new DefaultVersionRange("[1.0,3.0]")));
        assertTrue(new DefaultVersionRange("[1.0,2.0]").isCompatible(new DefaultVersionRange("[2.0,3.0]")));
        assertTrue(new DefaultVersionRange("(,2.0]").isCompatible(new DefaultVersionRange("[2.0,)")));
        assertTrue(new DefaultVersionRange("(,2.0]").isCompatible(new DefaultVersionRange("[1.0,)")));
        assertTrue(new DefaultVersionRange("(,2.0)").isCompatible(new DefaultVersionRange("(1.0,)")));

        // Not compatible
        assertFalse(new DefaultVersionRange("[1.0,2.0)").isCompatible(new DefaultVersionRange("(2.0,3.0]")));
        assertFalse(new DefaultVersionRange("(,2.0)").isCompatible(new DefaultVersionRange("(2.0,)")));
        assertFalse(new DefaultVersionRange("[1.0]").isCompatible(new DefaultVersionRange("[2.0]")));
    }

    @Test
    public void getValue()
    {
        assertEquals("[1.0,2.0]", new DefaultVersionRange(new DefaultVersion("1.0"), true, new DefaultVersion(
            "2.0"), true).getValue());
        assertEquals("(1.0,2.0]", new DefaultVersionRange(new DefaultVersion("1.0"), false, new DefaultVersion(
            "2.0"), true).getValue());
        assertEquals("[1.0,2.0)", new DefaultVersionRange(new DefaultVersion("1.0"), true, new DefaultVersion(
            "2.0"), false).getValue());
        assertEquals("(1.0,2.0)", new DefaultVersionRange(new DefaultVersion("1.0"), false, new DefaultVersion(
            "2.0"), false).getValue());

        assertEquals("(1.0,)", new DefaultVersionRange(new DefaultVersion("1.0"), false, null, false).getValue());
        assertEquals("[1.0,)", new DefaultVersionRange(new DefaultVersion("1.0"), true, null, false).getValue());
        assertEquals("(1.0,]", new DefaultVersionRange(new DefaultVersion("1.0"), false, null, true).getValue());
        assertEquals("[1.0,]", new DefaultVersionRange(new DefaultVersion("1.0"), true, null, true).getValue());
    }

    @Test
    public void testToString()
    {
        assertEquals("[1.0,2.0]", new DefaultVersionRange(new DefaultVersion("1.0"), true, new DefaultVersion(
            "2.0"), true).toString());
    }

    @Test
    public void testHashCode() throws InvalidVersionRangeException
    {
        assertEquals(new DefaultVersionRange("[1.0,2.0]").hashCode(),
            new DefaultVersionRange("[1.0,2.0]").hashCode());
        assertEquals(new DefaultVersionRange("[1.0]").hashCode(),
            new DefaultVersionRange("[1.0,1.0]").hashCode());

        assertNotEquals(new DefaultVersionRange("[1.0,2.0)").hashCode(),
            new DefaultVersionRange("[1.0.2.0]").hashCode());
    }

    @Test
    public void testEquals() throws InvalidVersionRangeException
    {
        assertEquals(new DefaultVersionRange("[1.0,2.0]"), new DefaultVersionRange("[1.0,2.0]"));
        assertEquals(new DefaultVersionRange("[1.0]"), new DefaultVersionRange("[1.0,1.0]"));

        assertNotEquals(new DefaultVersionRange("[1.0,2.0)"), new DefaultVersionRange("[1.0.2.0]"));
    }

    @Test
    public void containsVersion() throws InvalidVersionRangeException
    {
        assertTrue(new DefaultVersionRange("[1.0,2.0]").containsVersion((Version) new DefaultVersion("1.0")));
        assertTrue(new DefaultVersionRange("[1.0,2.0]").containsVersion((Version) new DefaultVersion("2.0")));

        assertFalse(new DefaultVersionRange("[2.0]").containsVersion((Version) new DefaultVersion("1.0")));
        assertFalse(new DefaultVersionRange("[2.0]").containsVersion((Version) new DefaultVersion("3.0")));
        assertFalse(new DefaultVersionRange("(1.0,2.0)").containsVersion((Version) new DefaultVersion("1.0")));
        assertFalse(new DefaultVersionRange("(1.0,2.0)").containsVersion((Version) new DefaultVersion("2.0")));

        assertTrue(new DefaultVersionRange("[1.0,2.0]").containsVersion(new TestVersion("1.0")));
        assertTrue(new DefaultVersionRange("[1.0,2.0]").containsVersion(new TestVersion("2.0")));

        assertFalse(new DefaultVersionRange("[2.0]").containsVersion(new TestVersion("1.0")));
        assertFalse(new DefaultVersionRange("[2.0]").containsVersion(new TestVersion("3.0")));
        assertFalse(new DefaultVersionRange("(1.0,2.0)").containsVersion(new TestVersion("1.0")));
        assertFalse(new DefaultVersionRange("(1.0,2.0)").containsVersion(new TestVersion("2.0")));
    }

    @Test
    public void invalidVersionRangeWithSingleVersionWithLowerBoundInclusive()
    {
        assertThrows(InvalidVersionRangeException.class, () -> {
            new DefaultVersionRange("]1.0]");
        });
    }

    @Test
    public void invalidVersionRangeWithSingleVersionWithUpperBoundInclusive()
    {
        assertThrows(InvalidVersionRangeException.class, () -> {
            new DefaultVersionRange("[1.0[");
        });
    }

    @Test
    public void invalidVersionRangeWithRangeMoreThanTwoBound()
    {
        assertThrows(InvalidVersionRangeException.class, () -> {
            new DefaultVersionRange("[1.0,2.0,3.0]");
        });
    }

    @Test
    public void invalidVersionRangeWithLowerBoundGreaterThanUpperBound()
    {
        assertThrows(InvalidVersionRangeException.class, () -> {
            new DefaultVersionRange("[2.0,1.0]");
        });
    }
}
