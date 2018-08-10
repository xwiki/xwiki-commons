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
package org.xwiki.filter.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link FilterStreamType}.
 *
 * @version $Id$
 */
public class FilterStreamTypeTest
{
    // Tests

    @Test
    public void testSerializeWithDataAndVersion()
    {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");

        assertEquals("type+data/version", type.serialize());
    }

    @Test
    public void testUnserializeWithDataAndVersion()
    {
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");

        assertEquals("type", type.getType().getId());
        assertEquals("data", type.getDataFormat());
        assertEquals("version", type.getVersion());
    }

    @Test
    public void testUnserializeWithData()
    {
        FilterStreamType type = FilterStreamType.unserialize("type+data");

        assertEquals("type", type.getType().getId());
        assertEquals("data", type.getDataFormat());
        assertNull(type.getVersion());
    }

    @Test
    public void testUnserializeWithEmptyData()
    {
        FilterStreamType type = FilterStreamType.unserialize("type+");

        assertEquals("type", type.getType().getId());
        assertEquals("", type.getDataFormat());
        assertNull(type.getVersion());
    }

    @Test
    public void testUnserializeWithVersion()
    {
        FilterStreamType type = FilterStreamType.unserialize("type/version");

        assertEquals("type", type.getType().getId());
        assertNull(type.getDataFormat());
        assertEquals("version", type.getVersion());
    }

    @Test
    public void testUnserializeWithEmptyVersion()
    {
        FilterStreamType type = FilterStreamType.unserialize("type/");

        assertEquals("type", type.getType().getId());
        assertNull(type.getDataFormat());
        assertEquals("", type.getVersion());
    }

    @Test
    public void equals()
    {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");

        assertEquals(type, new FilterStreamType(new SystemType("type"), "data", "version"));

        assertNotEquals(type, new FilterStreamType(new SystemType("type2"), "data", "version"));
        assertNotEquals(type, new FilterStreamType(new SystemType("type"), "data2", "version"));
        assertNotEquals(type, new FilterStreamType(new SystemType("type"), "data", "version2"));
    }

    @Test
    public void compareTo()
    {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");

        assertEquals(0, type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0")));
        assertTrue(type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0")) > 0);
        assertTrue(type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0")) < 0);
    }
}
