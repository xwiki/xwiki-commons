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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    public void getMap()
    {
        Map<String, String> result = this.tool.getMap();
        assertNotNull(result, "Returned null instead of a map");
        assertTrue(result.isEmpty(), "Returned map wasn't initially empty");

        // Check that null values are supported
        assertFalse(result.containsKey("nullvalue"), "null value already present in the empty map");
        assertFalse(result.containsValue(null), "null value already present in the empty map");
        result.put("nullvalue", null);
        assertNull(result.get("nullvalue"));
        assertTrue(result.containsKey("nullvalue"), "Failed to insert null value in the map");
        assertTrue(result.containsValue(null), "Failed to insert null value in the map");

        // Check that null keys are supported
        assertFalse(result.containsKey(null), "null key already present in the empty map");
        assertFalse(result.containsValue("nullkey"), "null key already present in the empty map");
        result.put(null, "nullkey");
        assertEquals("nullkey", result.get(null), "Failed to insert null key in the map");
        assertTrue(result.containsKey(null), "Failed to insert null key in the map");
        assertTrue(result.containsValue("nullkey"), "Failed to insert null key in the map");
    }

    @Test
    public void getSortedMap()
    {
        SortedMap<Double, Integer> result = this.tool.getSortedMap();
        assertNotNull(result, "Returned null instead of a map");
        assertTrue(result.isEmpty(), "Returned map wasn't initially empty");

        // Check that the map is indeed sorted by the key
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.put(r.nextDouble(), i);
        }
        Double d = 0.0;
        for (Map.Entry<Double, Integer> e : result.entrySet()) {
            assertTrue(e.getKey() >= d, "Map doesn't preserve the natural order of the elements");
            d = e.getKey();
        }
    }

    @Test
    public void getOrderedMap()
    {
        Map<Double, Integer> result = this.tool.getOrderedMap();
        assertNotNull(result, "Returned null instead of a map");
        assertTrue(result.isEmpty(), "Returned map wasn't initially empty");

        // Check that the map is indeed ordered
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.put(r.nextDouble(), i);
        }
        int i = 0;
        for (Map.Entry<Double, Integer> e : result.entrySet()) {
            assertEquals(i++, e.getValue().intValue(), "Map doesn't preserve the insertion order");
        }
    }

    @Test
    public void getSet()
    {
        Set<String> result = this.tool.getSet();
        assertNotNull(result, "Returned null instead of a set");
        assertTrue(result.isEmpty(), "Returned set wasn't initially empty");

        // Check that null values are supported
        assertFalse(result.contains(null), "null value already present in the empty set");
        assertTrue(result.add(null), "null value already present in the empty set");
        assertTrue(result.contains(null), "null value wasn't inserted in the set");
        assertFalse(result.add(null), "null value was re-inserted in the set");
        assertTrue(result.remove(null), "failed to remove null value from the set");
        assertFalse(result.contains(null), "null value present in the set after being removed");
    }

    @Test
    public void getSortedSet()
    {
        SortedSet<Double> result = this.tool.getSortedSet();
        assertNotNull(result, "Returned null instead of a set");
        assertTrue(result.isEmpty(), "Returned set wasn't initially empty");

        // Check that the set is indeed sorted
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.add(r.nextDouble());
        }
        Double d = 0.0;
        for (Double e : result) {
            assertTrue(e >= d, "Set doesn't preserve the natural order of the elements");
            d = e;
        }
    }

    @Test
    public void getOrderedSet()
    {
        Set<Double> result = this.tool.getOrderedSet();
        assertNotNull(result, "Returned null instead of a set");
        assertTrue(result.isEmpty(), "Returned set wasn't initially empty");

        // Check that the set is indeed ordered
        Random r = new Random();
        List<Double> values = new ArrayList<Double>(150);
        for (int i = 0; i < 100; ++i) {
            Double d = r.nextDouble();
            result.add(d);
            values.add(d);
        }
        Iterator<Double> valueIterator = values.iterator();
        for (Double e : result) {
            assertEquals(valueIterator.next(), e, "Set doesn't preserve the insertion order");
        }
    }

    @Test
    public void getArrayList()
    {
        List<String> result = this.tool.getArrayList();
        assertNotNull(result, "Returned null instead of a list");
        assertTrue(result.isEmpty(), "Returned list wasn't initially empty");
    }

    @Test
    public void getLinkedList()
    {
        List<String> result = this.tool.getLinkedList();
        assertNotNull(result, "Returned null instead of a list");
        assertTrue(result.isEmpty(), "Returned list wasn't initially empty");
    }

    @Test
    public void getQueue()
    {
        Queue<String> result = this.tool.getQueue();
        assertNotNull(result, "Returned null instead of a queue");
        assertTrue(result.isEmpty(), "Returned queue wasn't initially empty");
        assertNull(result.poll());
    }

    @Test
    public void getBlockingQueue()
    {
        BlockingQueue<String> result = this.tool.getBlockingQueue();
        assertNotNull(result, "Returned null instead of a queue");
        assertTrue(result.isEmpty(), "Returned queue wasn't initially empty");
    }

    @Test
    public void getPriorityQueue()
    {
        Queue<Double> result = this.tool.getPriorityQueue();
        assertNotNull(result, "Returned null instead of a queue");
        assertTrue(result.isEmpty(), "Returned queue wasn't initially empty");

        // Check that the queue is indeed prioritized
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.add(r.nextDouble());
        }
        Double d = 0.0;
        while (!result.isEmpty()) {
            Double e = result.poll();
            assertTrue(e >= d, "Queue doesn't preserve the natural order of the elements");
            d = e;
        }
    }

    @Test
    public void unmodifiableList()
    {
        List<String> original = this.tool.getLinkedList();
        original.add("one");
        original.add("two");
        original.add("three");
        List<String> result = this.tool.unmodifiable(original);
        assertNotNull(result, "Returned null instead of a proper unmodifiable wrapper");
        assertFalse(result.isEmpty(), "Returned wrapper was empty");
        assertEquals(3, result.size(), "Wrong size of the wrapper list");
        boolean failed = false;
        try {
            result.add("four");
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        assertTrue(failed, "Shouldn't be allowed to add new items");
        original.add("four");
        assertEquals(4, result.size(), "Wrapper list doesn't show changes in the original list");
        Iterator<String> it = result.iterator();
        failed = false;
        try {
            it.remove();
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        assertTrue(failed, "Shouldn't be allowed to remove items using the iterator");
    }

    @Test
    public void unmodifiableMap()
    {
        Map<String, Integer> original = this.tool.getMap();
        original.put("one", 1);
        original.put("two", 2);
        original.put("three", 3);
        Map<String, Integer> result = this.tool.unmodifiable(original);
        assertNotNull(result, "Returned null instead of a proper unmodifiable wrapper");
        assertFalse(result.isEmpty(), "Returned wrapper was empty");
        assertEquals(3, result.size(), "Wrong size of the wrapper map");
        boolean failed = false;
        try {
            result.put("four", 4);
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        assertTrue(failed, "Shouldn't be allowed to add new items");
        original.put("four", 4);
        assertEquals(4, result.size(), "Wrapper map doesn't show changes in the original map");
    }

    @Test
    public void unmodifiableSet()
    {
        SortedSet<Integer> original = this.tool.getSortedSet();
        for (int i = 0; i < 100; ++i) {
            original.add(150 - i);
        }
        Set<Integer> result = this.tool.unmodifiable(original);
        assertNotNull(result, "Returned null instead of a proper unmodifiable wrapper");
        assertFalse(result.isEmpty(), "Returned wrapper was empty");
        assertEquals(100, result.size(), "Wrong size of the wrapper set");
        assertEquals(51, result.iterator().next().intValue(),
            "Wrapper set doesn't preserve the order of the original set");
        boolean failed = false;
        try {
            result.add(307);
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        assertTrue(failed, "Shouldn't be allowed to add new items");
        original.add(42);
        assertEquals(101, result.size(), "Wrapper set doesn't show changes in the original set");
        assertEquals(42, result.iterator().next().intValue(),
            "Wrapper set doesn't preserve the order of the original set");
    }

    @Test
    public void unmodifiableCollection()
    {
        Collection<Integer> original = this.tool.getPriorityQueue();
        for (int i = 0; i < 100; ++i) {
            original.add(150 - i);
        }
        Collection<Integer> result = this.tool.unmodifiable(original);
        assertNotNull(result, "Returned null instead of a proper unmodifiable wrapper");
        assertFalse(result.isEmpty(), "Returned wrapper was empty");
        assertEquals(100, result.size(), "Wrong size of the wrapper collection");
        assertEquals(51, result.iterator().next().intValue(),
            "Wrapper collection doesn't preserve the order of the original collection");
        boolean failed = false;
        try {
            result.add(307);
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        assertTrue(failed, "Shouldn't be allowed to add new items");
        original.add(42);
        assertEquals(101, result.size(), "Wrapper set doesn't show changes in the original set");
        assertEquals(42, result.iterator().next().intValue(),
            "Wrapper set doesn't preserve the order of the original set");
    }

    @Test
    public void nullUnmodifiable()
    {
        assertNull(this.tool.unmodifiable((List<Object>) null));
        assertNull(this.tool.unmodifiable((Set<Object>) null));
        assertNull(this.tool.unmodifiable((Map<Object, Object>) null));
        assertNull(this.tool.unmodifiable((Collection<Object>) null));
    }

    @Test
    public void setUnion()
    {
        Set<String> set1 = this.tool.getSet();
        set1.add("one");
        set1.add("two");
        set1.add("three");
        Set<String> set2 = this.tool.getSet();
        set2.add("three");
        set2.add("four");
        set2.add("five");
        Collection<String> result = this.tool.union(set1, set2);
        assertNotNull(result, "Returned null instead of a proper collection");
        assertFalse(result.isEmpty(), "Returned union was empty");
        assertEquals(5, result.size(), "Wrong size of the union collection");
        assertTrue(result.containsAll(set1), "Not all elements from the first set were included in the union");
        assertTrue(result.containsAll(set2), "Not all elements from the second set were included in the union");
    }

    @Test
    public void listUnion()
    {
        List<String> list1 = this.tool.getLinkedList();
        list1.add("one");
        list1.add("two");
        list1.add("three");
        list1.add("three");
        list1.add("three");
        List<String> list2 = this.tool.getArrayList();
        list2.add("three");
        list2.add("four");
        list2.add("four");
        list2.add("five");
        Collection<String> result = this.tool.union(list1, list2);
        assertNotNull(result, "Returned null instead of a proper collection");
        assertFalse(result.isEmpty(), "Returned union was empty");
        assertEquals(8, result.size(), "Wrong size of the union collection");
        assertTrue(result.containsAll(list1), "Not all elements from the first list were included in the union");
        assertTrue(result.containsAll(list2), "Not all elements from the second list were included");
    }

    @Test
    public void nullUnion()
    {
        List<String> list = this.tool.getLinkedList();
        list.add("one");
        list.add("two");
        list.add("three");
        assertEquals(list, this.tool.union(list, null));
        assertEquals(list, this.tool.union(null, list));
        assertNull(this.tool.union(null, null));
    }

    @Test
    public void setIntersection()
    {
        Set<String> set1 = this.tool.getSet();
        set1.add("one");
        set1.add("two");
        set1.add("three");
        Set<String> set2 = this.tool.getSet();
        set2.add("three");
        set2.add("four");
        set2.add("five");
        Collection<String> result = this.tool.intersection(set1, set2);
        assertNotNull(result, "Returned null instead of a proper collection");
        assertFalse(result.isEmpty(), "Returned intersection was empty");
        assertEquals(1, result.size(), "Wrong size of the intersection collection");
        assertEquals("three", result.iterator().next(), "Wrong element included in the intersection");
    }

    @Test
    public void listIntersection()
    {
        List<String> list1 = this.tool.getLinkedList();
        list1.add("one");
        list1.add("two");
        list1.add("three");
        list1.add("three");
        list1.add("three");
        List<String> list2 = this.tool.getArrayList();
        list2.add("three");
        list2.add("three");
        list2.add("four");
        list2.add("five");
        Collection<String> result = this.tool.intersection(list1, list2);
        assertNotNull(result, "Returned null instead of a proper collection");
        assertFalse(result.isEmpty(), "Returned intersection was empty");
        assertEquals(2, result.size(), "Wrong size of the intersection collection");
        Iterator<String> it = result.iterator();
        assertEquals("three", it.next(), "Wrong element included in the intersection");
        assertEquals("three", it.next(), "Wrong element included in the intersection");
        assertFalse(it.hasNext(), "Wrong size of the intersection collection");
    }

    @Test
    public void nullIntersection()
    {
        List<String> list = this.tool.getLinkedList();
        list.add("one");
        list.add("two");
        list.add("three");
        assertEquals(list, this.tool.intersection(list, null));
        assertEquals(list, this.tool.intersection(null, list));
        assertNull(this.tool.intersection(null, null));
    }

    @Test
    public void setDisjunction()
    {
        Set<String> set1 = this.tool.getSet();
        set1.add("one");
        set1.add("two");
        set1.add("three");
        Set<String> set2 = this.tool.getSet();
        set2.add("three");
        set2.add("four");
        set2.add("five");
        Collection<String> result = this.tool.disjunction(set1, set2);
        assertNotNull(result, "Returned null instead of a proper collection");
        assertFalse(result.isEmpty(), "Returned disjunction was empty");
        assertEquals(4, result.size(), "Wrong size of the intersection collection");
        assertTrue(result.contains("one"), "Missing element from the disjunction");
        assertTrue(result.contains("two"), "Missing element from the disjunction");
        assertFalse(result.contains("three"), "Wrong element included in the disjunction");
        assertTrue(result.contains("four"), "Missing element from the disjunction");
        assertTrue(result.contains("five"), "Missing element from the disjunction");
    }

    @Test
    public void listDisjunction()
    {
        List<String> list1 = this.tool.getLinkedList();
        list1.add("one");
        list1.add("two");
        list1.add("three");
        list1.add("three");
        list1.add("three");
        list1.add("four");
        List<String> list2 = this.tool.getArrayList();
        list2.add("three");
        list2.add("three");
        list2.add("four");
        list2.add("five");
        Collection<String> result = this.tool.disjunction(list1, list2);
        assertNotNull(result, "Returned null instead of a proper collection");
        assertFalse(result.isEmpty(), "Returned disjunction was empty");
        assertEquals(4, result.size(), "Wrong size of the disjunction collection");
        assertTrue(result.contains("one"), "Missing element from the disjunction");
        assertTrue(result.contains("two"), "Missing element from the disjunction");
        assertTrue(result.contains("three"), "Missing element from the disjunction");
        assertFalse(result.contains("four"), "Wrong element included in the disjunction");
        assertTrue(result.contains("five"), "Missing element from the disjunction");
    }

    @Test
    public void nullDisjunction()
    {
        List<String> list = this.tool.getLinkedList();
        list.add("one");
        list.add("two");
        list.add("three");
        assertEquals(list, this.tool.disjunction(list, null));
        assertEquals(list, this.tool.disjunction(null, list));
        assertNull(this.tool.disjunction(null, null));
    }

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
