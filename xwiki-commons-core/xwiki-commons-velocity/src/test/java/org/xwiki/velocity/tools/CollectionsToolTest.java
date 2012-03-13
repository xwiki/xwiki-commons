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

import org.junit.Assert;
import org.junit.Test;

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
    public void testGetMap()
    {
        Map<String, String> result = this.tool.getMap();
        Assert.assertNotNull("Returned null instead of a map", result);
        Assert.assertTrue("Returned map wasn't initially empty", result.isEmpty());

        // Check that null values are supported
        Assert.assertFalse("null value already present in the empty map", result.containsKey("nullvalue"));
        Assert.assertFalse("null value already present in the empty map", result.containsValue(null));
        result.put("nullvalue", null);
        Assert.assertNull(result.get("nullvalue"));
        Assert.assertTrue("Failed to insert null value in the map", result.containsKey("nullvalue"));
        Assert.assertTrue("Failed to insert null value in the map", result.containsValue(null));

        // Check that null keys are supported
        Assert.assertFalse("null key already present in the empty map", result.containsKey(null));
        Assert.assertFalse("null key already present in the empty map", result.containsValue("nullkey"));
        result.put(null, "nullkey");
        Assert.assertEquals("Failed to insert null key in the map", "nullkey", result.get(null));
        Assert.assertTrue("Failed to insert null key in the map", result.containsKey(null));
        Assert.assertTrue("Failed to insert null key in the map", result.containsValue("nullkey"));
    }

    @Test
    public void testGetSortedMap()
    {
        SortedMap<Double, Integer> result = this.tool.getSortedMap();
        Assert.assertNotNull("Returned null instead of a map", result);
        Assert.assertTrue("Returned map wasn't initially empty", result.isEmpty());

        // Check that the map is indeed sorted by the key
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.put(r.nextDouble(), i);
        }
        Double d = 0.0;
        for (Map.Entry<Double, Integer> e : result.entrySet()) {
            Assert.assertTrue("Map doesn't preserve the natural order of the elements", e.getKey() >= d);
            d = e.getKey();
        }
    }

    @Test
    public void testGetOrderedMap()
    {
        Map<Double, Integer> result = this.tool.getOrderedMap();
        Assert.assertNotNull("Returned null instead of a map", result);
        Assert.assertTrue("Returned map wasn't initially empty", result.isEmpty());

        // Check that the map is indeed ordered
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.put(r.nextDouble(), i);
        }
        int i = 0;
        for (Map.Entry<Double, Integer> e : result.entrySet()) {
            Assert.assertEquals("Map doesn't preserve the insertion order", i++, e.getValue().intValue());
        }
    }

    @Test
    public void testGetSet()
    {
        Set<String> result = this.tool.getSet();
        Assert.assertNotNull("Returned null instead of a set", result);
        Assert.assertTrue("Returned set wasn't initially empty", result.isEmpty());

        // Check that null values are supported
        Assert.assertFalse("null value already present in the empty set", result.contains(null));
        Assert.assertTrue("null value already present in the empty set", result.add(null));
        Assert.assertTrue("null value wasn't inserted in the set", result.contains(null));
        Assert.assertFalse("null value was re-inserted in the set", result.add(null));
        Assert.assertTrue("failed to remove null value from the set", result.remove(null));
        Assert.assertFalse("null value present in the set after being removed", result.contains(null));
    }

    @Test
    public void testGetSortedSet()
    {
        SortedSet<Double> result = this.tool.getSortedSet();
        Assert.assertNotNull("Returned null instead of a set", result);
        Assert.assertTrue("Returned set wasn't initially empty", result.isEmpty());

        // Check that the set is indeed sorted
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.add(r.nextDouble());
        }
        Double d = 0.0;
        for (Double e : result) {
            Assert.assertTrue("Set doesn't preserve the natural order of the elements", e >= d);
            d = e;
        }
    }

    @Test
    public void testGetOrderedSet()
    {
        Set<Double> result = this.tool.getOrderedSet();
        Assert.assertNotNull("Returned null instead of a set", result);
        Assert.assertTrue("Returned set wasn't initially empty", result.isEmpty());

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
            Assert.assertEquals("Set doesn't preserve the insertion order", valueIterator.next(), e);
        }
    }

    @Test
    public void testGetArrayList()
    {
        List<String> result = this.tool.getArrayList();
        Assert.assertNotNull("Returned null instead of a list", result);
        Assert.assertTrue("Returned list wasn't initially empty", result.isEmpty());
    }

    @Test
    public void testGetLinkedList()
    {
        List<String> result = this.tool.getLinkedList();
        Assert.assertNotNull("Returned null instead of a list", result);
        Assert.assertTrue("Returned list wasn't initially empty", result.isEmpty());
    }

    @Test
    public void testGetQueue()
    {
        Queue<String> result = this.tool.getQueue();
        Assert.assertNotNull("Returned null instead of a queue", result);
        Assert.assertTrue("Returned queue wasn't initially empty", result.isEmpty());
        Assert.assertNull(result.poll());
    }

    @Test
    public void testGetBlockingQueue()
    {
        BlockingQueue<String> result = this.tool.getBlockingQueue();
        Assert.assertNotNull("Returned null instead of a queue", result);
        Assert.assertTrue("Returned queue wasn't initially empty", result.isEmpty());
    }

    @Test
    public void testGetPriorityQueue()
    {
        Queue<Double> result = this.tool.getPriorityQueue();
        Assert.assertNotNull("Returned null instead of a queue", result);
        Assert.assertTrue("Returned queue wasn't initially empty", result.isEmpty());

        // Check that the queue is indeed prioritized
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            result.add(r.nextDouble());
        }
        Double d = 0.0;
        while (!result.isEmpty()) {
            Double e = result.poll();
            Assert.assertTrue("Queue doesn't preserve the natural order of the elements", e >= d);
            d = e;
        }
    }

    @Test
    public void testUnmodifiableList()
    {
        List<String> original = this.tool.getLinkedList();
        original.add("one");
        original.add("two");
        original.add("three");
        List<String> result = this.tool.unmodifiable(original);
        Assert.assertNotNull("Returned null instead of a proper unmodifiable wrapper", result);
        Assert.assertFalse("Returned wrapper was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the wrapper list", 3, result.size());
        boolean failed = false;
        try {
            result.add("four");
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        Assert.assertTrue("Shouldn't be allowed to add new items", failed);
        original.add("four");
        Assert.assertEquals("Wrapper list doesn't show changes in the original list", 4, result.size());
        Iterator<String> it = result.iterator();
        failed = false;
        try {
            it.remove();
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        Assert.assertTrue("Shouldn't be allowed to remove items using the iterator", failed);
    }

    @Test
    public void testUnmodifiableMap()
    {
        Map<String, Integer> original = this.tool.getMap();
        original.put("one", 1);
        original.put("two", 2);
        original.put("three", 3);
        Map<String, Integer> result = this.tool.unmodifiable(original);
        Assert.assertNotNull("Returned null instead of a proper unmodifiable wrapper", result);
        Assert.assertFalse("Returned wrapper was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the wrapper map", 3, result.size());
        boolean failed = false;
        try {
            result.put("four", 4);
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        Assert.assertTrue("Shouldn't be allowed to add new items", failed);
        original.put("four", 4);
        Assert.assertEquals("Wrapper map doesn't show changes in the original map", 4, result.size());
    }

    @Test
    public void testUnmodifiableSet()
    {
        SortedSet<Integer> original = this.tool.getSortedSet();
        for (int i = 0; i < 100; ++i) {
            original.add(150 - i);
        }
        Set<Integer> result = this.tool.unmodifiable(original);
        Assert.assertNotNull("Returned null instead of a proper unmodifiable wrapper", result);
        Assert.assertFalse("Returned wrapper was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the wrapper set", 100, result.size());
        Assert.assertEquals("Wrapper set doesn't preserve the order of the original set", 51,
            result.iterator().next().intValue());
        boolean failed = false;
        try {
            result.add(307);
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        Assert.assertTrue("Shouldn't be allowed to add new items", failed);
        original.add(42);
        Assert.assertEquals("Wrapper set doesn't show changes in the original set", 101, result.size());
        Assert.assertEquals("Wrapper set doesn't preserve the order of the original set", 42,
            result.iterator().next().intValue());
    }

    @Test
    public void testUnmodifiableCollection()
    {
        Collection<Integer> original = this.tool.getPriorityQueue();
        for (int i = 0; i < 100; ++i) {
            original.add(150 - i);
        }
        Collection<Integer> result = this.tool.unmodifiable(original);
        Assert.assertNotNull("Returned null instead of a proper unmodifiable wrapper", result);
        Assert.assertFalse("Returned wrapper was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the wrapper collection", 100, result.size());
        Assert.assertEquals("Wrapper collection doesn't preserve the order of the original collection", 51,
            result.iterator().next().intValue());
        boolean failed = false;
        try {
            result.add(307);
        } catch (UnsupportedOperationException ex) {
            failed = true;
        }
        Assert.assertTrue("Shouldn't be allowed to add new items", failed);
        original.add(42);
        Assert.assertEquals("Wrapper set doesn't show changes in the original set", 101, result.size());
        Assert.assertEquals("Wrapper set doesn't preserve the order of the original set", 42,
            result.iterator().next().intValue());
    }

    @Test
    public void testSetUnion()
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
        Assert.assertNotNull("Returned null instead of a proper collection", result);
        Assert.assertFalse("Returned union was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the union collection", 5, result.size());
        Assert.assertTrue("Not all elements from the first set were included in the union", result.containsAll(set1));
        Assert.assertTrue("Not all elements from the second set were included in the union", result.containsAll(set2));
    }

    @Test
    public void testListUnion()
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
        Assert.assertNotNull("Returned null instead of a proper collection", result);
        Assert.assertFalse("Returned union was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the union collection", 8, result.size());
        Assert.assertTrue("Not all elements from the first list were included in the union", result.containsAll(list1));
        Assert.assertTrue("Not all elements from the second list were included", result.containsAll(list2));
    }

    @Test
    public void testSetIntersection()
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
        Assert.assertNotNull("Returned null instead of a proper collection", result);
        Assert.assertFalse("Returned intersection was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the intersection collection", 1, result.size());
        Assert.assertEquals("Wrong element included in the intersection", "three", result.iterator().next());
    }

    @Test
    public void testListIntersection()
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
        Assert.assertNotNull("Returned null instead of a proper collection", result);
        Assert.assertFalse("Returned intersection was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the intersection collection", 2, result.size());
        Iterator<String> it = result.iterator();
        Assert.assertEquals("Wrong element included in the intersection", "three", it.next());
        Assert.assertEquals("Wrong element included in the intersection", "three", it.next());
        Assert.assertFalse("Wrong size of the intersection collection", it.hasNext());
    }

    @Test
    public void testSetDisjunction()
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
        Assert.assertNotNull("Returned null instead of a proper collection", result);
        Assert.assertFalse("Returned disjunction was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the intersection collection", 4, result.size());
        Assert.assertTrue("Missing element from the disjunction", result.contains("one"));
        Assert.assertTrue("Missing element from the disjunction", result.contains("two"));
        Assert.assertFalse("Wrong element included in the disjunction", result.contains("three"));
        Assert.assertTrue("Missing element from the disjunction", result.contains("four"));
        Assert.assertTrue("Missing element from the disjunction", result.contains("five"));
    }

    @Test
    public void testListDisjunction()
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
        Assert.assertNotNull("Returned null instead of a proper collection", result);
        Assert.assertFalse("Returned disjunction was empty", result.isEmpty());
        Assert.assertEquals("Wrong size of the disjunction collection", 4, result.size());
        Assert.assertTrue("Missing element from the disjunction", result.contains("one"));
        Assert.assertTrue("Missing element from the disjunction", result.contains("two"));
        Assert.assertTrue("Missing element from the disjunction", result.contains("three"));
        Assert.assertFalse("Wrong element included in the disjunction", result.contains("four"));
        Assert.assertTrue("Missing element from the disjunction", result.contains("five"));
    }

    @Test
    public void testReverse()
    {
        List<String> list = this.tool.getLinkedList();
        list.add("one");
        list.add("two");
        list.add("three");
        Assert.assertTrue("Failed to reverse list", this.tool.reverse(list));
        Assert.assertEquals("List wasn't properly reversed", "three", list.get(0));
        Assert.assertEquals("List wasn't properly reversed", "two", list.get(1));
        Assert.assertEquals("List wasn't properly reversed", "one", list.get(2));
        List<String> readonly = this.tool.unmodifiable(list);
        Assert.assertFalse("Unmodifiable list wrongly reversed", this.tool.reverse(readonly));
        Assert.assertEquals("Unmodifiable list was changed", "three", list.get(0));
        Assert.assertFalse("Reversed a null list?", this.tool.reverse(null));
    }
}
