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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.extension.version.InvalidVersionRangeException;

public class DefaultVersionRangeTest
{
    @Test
    public void testIsCompatible() throws InvalidVersionRangeException
    {
        // Compatible
        Assert.assertTrue(new DefaultVersionRange("[1.0]").isCompatible(new DefaultVersionRange("[1.0]")));
        Assert.assertTrue(new DefaultVersionRange("[1.0,2.0]").isCompatible(new DefaultVersionRange("[1.0]")));
        Assert.assertTrue(new DefaultVersionRange("[1.0,2.0]").isCompatible(new DefaultVersionRange("[1.0,3.0]")));
        Assert.assertTrue(new DefaultVersionRange("[1.0,2.0]").isCompatible(new DefaultVersionRange("[2.0,3.0]")));
        Assert.assertTrue(new DefaultVersionRange("(,2.0]").isCompatible(new DefaultVersionRange("[2.0,)")));
        Assert.assertTrue(new DefaultVersionRange("(,2.0]").isCompatible(new DefaultVersionRange("[1.0,)")));
        Assert.assertTrue(new DefaultVersionRange("(,2.0)").isCompatible(new DefaultVersionRange("(1.0,)")));

        // Not compatible
        Assert.assertFalse(new DefaultVersionRange("[1.0,2.0)").isCompatible(new DefaultVersionRange("(2.0,3.0]")));
        Assert.assertFalse(new DefaultVersionRange("(,2.0)").isCompatible(new DefaultVersionRange("(2.0,)")));
        Assert.assertFalse(new DefaultVersionRange("[1.0]").isCompatible(new DefaultVersionRange("[2.0]")));
    }

    @Test
    public void testGetValue()
    {
        Assert.assertEquals("[1.0,2.0]", new DefaultVersionRange(new DefaultVersion("1.0"), true, new DefaultVersion(
            "2.0"), true).getValue());
        Assert.assertEquals("(1.0,2.0]", new DefaultVersionRange(new DefaultVersion("1.0"), false, new DefaultVersion(
            "2.0"), true).getValue());
        Assert.assertEquals("[1.0,2.0)", new DefaultVersionRange(new DefaultVersion("1.0"), true, new DefaultVersion(
            "2.0"), false).getValue());
        Assert.assertEquals("(1.0,2.0)", new DefaultVersionRange(new DefaultVersion("1.0"), false, new DefaultVersion(
            "2.0"), false).getValue());

        Assert
            .assertEquals("(1.0,)", new DefaultVersionRange(new DefaultVersion("1.0"), false, null, false).getValue());
        Assert.assertEquals("[1.0,)", new DefaultVersionRange(new DefaultVersion("1.0"), true, null, false).getValue());
        Assert.assertEquals("(1.0,]", new DefaultVersionRange(new DefaultVersion("1.0"), false, null, true).getValue());
        Assert.assertEquals("[1.0,]", new DefaultVersionRange(new DefaultVersion("1.0"), true, null, true).getValue());
    }

    @Test
    public void testToString()
    {
        Assert.assertEquals("[1.0,2.0]", new DefaultVersionRange(new DefaultVersion("1.0"), true, new DefaultVersion(
            "2.0"), true).toString());
    }

    @Test
    public void testHashCode() throws InvalidVersionRangeException
    {
        Assert.assertEquals(new DefaultVersionRange("[1.0,2.0]").hashCode(),
            new DefaultVersionRange("[1.0,2.0]").hashCode());
        Assert.assertEquals(new DefaultVersionRange("[1.0]").hashCode(),
            new DefaultVersionRange("[1.0,1.0]").hashCode());

        Assert.assertNotEquals(new DefaultVersionRange("[1.0,2.0)").hashCode(),
            new DefaultVersionRange("[1.0.2.0]").hashCode());
    }

    @Test
    public void testEquals() throws InvalidVersionRangeException
    {
        Assert.assertEquals(new DefaultVersionRange("[1.0,2.0]"), new DefaultVersionRange("[1.0,2.0]"));
        Assert.assertEquals(new DefaultVersionRange("[1.0]"), new DefaultVersionRange("[1.0,1.0]"));

        Assert.assertNotEquals(new DefaultVersionRange("[1.0,2.0)"), new DefaultVersionRange("[1.0.2.0]"));
    }

    @Test
    public void testContainsVersion() throws InvalidVersionRangeException
    {
        Assert.assertTrue(new DefaultVersionRange("[1.0,2.0]").containsVersion(new DefaultVersion("1.0")));
        Assert.assertTrue(new DefaultVersionRange("[1.0,2.0]").containsVersion(new DefaultVersion("2.0")));

        Assert.assertFalse(new DefaultVersionRange("[2.0]").containsVersion(new DefaultVersion("1.0")));
        Assert.assertFalse(new DefaultVersionRange("[2.0]").containsVersion(new DefaultVersion("3.0")));
        Assert.assertFalse(new DefaultVersionRange("(1.0,2.0)").containsVersion(new DefaultVersion("1.0")));
        Assert.assertFalse(new DefaultVersionRange("(1.0,2.0)").containsVersion(new DefaultVersion("2.0")));
    }

    @Test(expected = InvalidVersionRangeException.class)
    public void testInvalidVersionRangeWithSingleVersionWithLowerBoundInclusive() throws InvalidVersionRangeException
    {
        new DefaultVersionRange("]1.0]");
    }

    @Test(expected = InvalidVersionRangeException.class)
    public void testInvalidVersionRangeWithSingleVersionWithUpperBoundInclusive() throws InvalidVersionRangeException
    {
        new DefaultVersionRange("[1.0[");
    }

    @Test(expected = InvalidVersionRangeException.class)
    public void testInvalidVersionRangeWithRangeMoreThanTwoBound() throws InvalidVersionRangeException
    {
        new DefaultVersionRange("[1.0,2.0,3.0]");
    }

    @Test(expected = InvalidVersionRangeException.class)
    public void testInvalidVersionRangeWithLowerBoundGreaterThanUpperBound() throws InvalidVersionRangeException
    {
        new DefaultVersionRange("[2.0,1.0]");
    }
}
