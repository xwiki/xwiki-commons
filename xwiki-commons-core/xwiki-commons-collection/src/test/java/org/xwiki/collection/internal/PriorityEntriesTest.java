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
package org.xwiki.collection.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link PriorityEntries}.
 * 
 * @version $Id$
 */
class PriorityEntriesTest
{
    @Test
    void misc()
    {
        PriorityEntries entries = new PriorityEntries<>();

        assertTrue(entries.isEmpty());
        assertNull(entries.get("name"));
        assertEquals(List.of(), entries.getSorted());
        assertEquals(0, entries.size());
        assertNotNull(entries.getLock());

        entries.put("name1", "value1");

        assertFalse(entries.isEmpty());
        assertEquals("value1", entries.get("name1"));
        assertEquals(List.of("value1"), entries.getSorted());
        assertEquals(1, entries.size());

        entries.put("name3", "value3");
        entries.put("name2", "value2");

        assertEquals(List.of("value1", "value2", "value3"), entries.getSorted());
        assertEquals(3, entries.size());

        List<Object> list = new ArrayList<>();
        entries.forEachEntry(list::add);

        assertEquals(List.of("value1", "value2", "value3"), entries.getSorted());

        entries.remove("name2");

        assertEquals(List.of("value1", "value3"), entries.getSorted());
        assertEquals(2, entries.size());

        entries = new PriorityEntries<>(42);

        assertTrue(entries.isEmpty());
        assertNull(entries.get("name"));
        assertEquals(List.of(), entries.getSorted());
        assertEquals(0, entries.size());
    }
}
