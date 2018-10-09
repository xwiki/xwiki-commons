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

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.test.TestVersion;
import org.xwiki.extension.version.IncompatibleVersionConstraintException;
import org.xwiki.extension.version.InvalidVersionRangeException;
import org.xwiki.extension.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultVersionConstraintTest
{
    @Test
    public void merge() throws IncompatibleVersionConstraintException
    {
        assertEquals("2.0", new DefaultVersionConstraint("1.0").merge(new DefaultVersionConstraint("2.0")).getValue());
        assertEquals("[2.0]",
            new DefaultVersionConstraint("[1.0,2.0]").merge(new DefaultVersionConstraint("[2.0]")).getValue());
        assertEquals("{[1.0,2.0]},{[2.0,]}",
            new DefaultVersionConstraint("[1.0,2.0]").merge(new DefaultVersionConstraint("2.0")).getValue());
        assertEquals("[2.0]",
            new DefaultVersionConstraint("1.0").merge(new DefaultVersionConstraint("[2.0]")).getValue());

        // Invalid

        Throwable exception = assertThrows(IncompatibleVersionConstraintException.class, () -> {
            new DefaultVersionConstraint("[1.0]").merge(new DefaultVersionConstraint("[2.0]")).getValue();
        });
        assertEquals("Ranges [[1.0]] and [[2.0]] are incompatibles", exception.getMessage());

        exception = assertThrows(IncompatibleVersionConstraintException.class, () -> {
            new DefaultVersionConstraint("[1.0]").merge(new DefaultVersionConstraint("2.0")).getValue();
        });
        assertEquals("Ranges [[1.0]] and [[2.0,]] are incompatibles", exception.getMessage());
    }

    @Test
    public void parse() throws InvalidVersionRangeException
    {
        assertEquals("1.0", new DefaultVersionConstraint("1.0").getVersion().getValue());
        assertEquals(Arrays.asList(new DefaultVersionRangeCollection("[1.0]")),
            new DefaultVersionConstraint("[1.0]").getRanges());
        assertEquals(Arrays.asList(new DefaultVersionRangeCollection("[1.0]")),
            new DefaultVersionConstraint("{[1.0]}").getRanges());
        assertEquals(
            Arrays.asList(new DefaultVersionRangeCollection("[1.0,3.0]"),
                new DefaultVersionRangeCollection("[1.0,2.0],[1.0,3.0]")),
            new DefaultVersionConstraint("{[1.0,3.0]},{[1.0,2.0],[1.0,3.0]}").getRanges());

        // Invalid goes to version

        assertEquals("[1.0", new DefaultVersionConstraint("[1.0").getVersion().getValue());
    }

    @Test
    public void containsVersion()
    {
        assertTrue(new DefaultVersionConstraint("1.0").containsVersion((Version) new DefaultVersion("1.0")));
        assertFalse(new DefaultVersionConstraint("1.0").containsVersion((Version) new DefaultVersion("2.0")));

        assertTrue(new DefaultVersionConstraint("1.0").containsVersion(new TestVersion("1.0")));
        assertFalse(new DefaultVersionConstraint("1.0").containsVersion(new TestVersion("2.0")));
    }

    @Test
    public void isCompatible()
    {
        assertTrue(new DefaultVersionConstraint("1.0").isCompatible((Version) new DefaultVersion("1.0")));
        assertTrue(new DefaultVersionConstraint("1.0").isCompatible((Version) new DefaultVersion("2.0")));

        assertTrue(new DefaultVersionConstraint("1.0").isCompatible(new TestVersion("1.0")));
        assertTrue(new DefaultVersionConstraint("1.0").isCompatible(new TestVersion("2.0")));
    }
}
