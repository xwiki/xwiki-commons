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

package org.xwiki.velocity.tools;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CollectionsTool}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class CollectionsToolTest
{
    private CollectionsTool tool = new CollectionsTool();

    @Test
    public void reverse()
    {
        List<String> list = this.tool.getLinkedList();
        list.add("one");
        list.add("two");
        list.add("three");
        assertTrue(this.tool.reverse(list), "Failed to reverse list");
        assertEquals("three", list.get(0), "List wasn't properly reversed");
        assertEquals("two", list.get(1), "List wasn't properly reversed");
        assertEquals("one", list.get(2), "List wasn't properly reversed");
        List<String> readonly = this.tool.unmodifiable(list);
        assertFalse(this.tool.reverse(readonly), "Unmodifiable list wrongly reversed");
        assertEquals("three", list.get(0), "Unmodifiable list was changed");
        assertFalse(this.tool.reverse(null), "Reversed a null list?");
    }

    @Test
    public void testSort()
    {
        List<String> list = this.tool.getLinkedList();
        list.add("one");
        list.add("two");
        list.add("three");
        assertTrue(this.tool.sort(list), "Failed to sort the list");
        assertEquals("one", list.get(0), "List wasn't properly sorted");
        assertEquals("three", list.get(1), "List wasn't properly sorted");
        assertEquals("two", list.get(2), "List wasn't properly sorted");
        this.tool.reverse(list);
        List<String> readonly = this.tool.unmodifiable(list);
        assertFalse(this.tool.sort(readonly), "Unmodifiable list wrongly sorted");
        assertEquals("two", list.get(0), "Unmodifiable list was changed");
        list = null;
        assertFalse(this.tool.sort(list), "Sorted a null list?");
    }
}
