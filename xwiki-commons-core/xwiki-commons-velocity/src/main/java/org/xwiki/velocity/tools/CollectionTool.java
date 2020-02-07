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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Velocity Tool allowing to create various type of collections.
 *
 * @version $Id$
 * @since 12.1RC1
 */
public class CollectionTool extends org.apache.velocity.tools.generic.CollectionTool
{
    /**
     * Create and return a new {@link ArrayList}, an unbounded list with constant access time and good performance for
     * most additions at the end of the list, but which performs poorly when deleting items, when inserting a new item
     * in the list and when appending a new item requires resizing the allocated space.
     *
     * @param <E> the type of the elements in the list
     * @return a new, empty {@link ArrayList}
     */
    public <E> List<E> getArrayList()
    {
        return new ArrayList<>();
    }

    /**
     * Create and return a new {@link LinkedList}, optimized for insertion and deletion of items, and for sequential
     * iteration over the items, but not for quick access to random positions in the list.
     *
     * @param <E> the type of the elements in the list
     * @return a new, empty {@link LinkedList}
     */
    public <E> List<E> getLinkedList()
    {
        return new LinkedList<>();
    }

    /**
     * Create and return a new {@link Map}, providing good speed for insertion, retrieval and deletion of items, but
     * with no guarantees on the order of the map.
     *
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     * @return a new, empty {@link HashMap}
     */
    public <K, V> Map<K, V> getMap()
    {
        return new HashMap<>();
    }

    /**
     * Create and return a new {@link SortedMap}, which ensures that iterating the map will always return the entries in
     * the natural order of the keys.
     *
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     * @return a new, empty {@link TreeMap}
     */
    public <K extends Comparable<K>, V> SortedMap<K, V> getSortedMap()
    {
        return new TreeMap<>();
    }

    /**
     * Create and return a new {@link Map}, which ensures that iterating the map will always return the entries in the
     * same order as they were added.
     *
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     * @return a new, empty {@link LinkedHashMap}
     */
    public <K, V> Map<K, V> getOrderedMap()
    {
        return new LinkedHashMap<>();
    }

    /**
     * Create and return a new {@link Set}, providing good speed for insertion, retrieval and deletion of items, but
     * with no guarantees on the order of the set.
     *
     * @param <E> the type of the elements in the set
     * @return a new, empty {@link HashSet}
     */
    public <E> Set<E> getSet()
    {
        return new HashSet<>();
    }

    /**
     * Create and return a new {@link SortedSet}, which ensures that iterating the set will always return the entries in
     * the natural order of the items.
     *
     * @param <E> the type of the elements in the set
     * @return a new, empty {@link TreeSet}
     */
    public <E extends Comparable<E>> SortedSet<E> getSortedSet()
    {
        return new TreeSet<>();
    }

    /**
     * Create and return a new {@link Set}, which ensures that iterating the set will always return the entries in the
     * same order as they were added.
     *
     * @param <E> the type of the elements in the set
     * @return a new, empty {@link LinkedHashSet}
     */
    public <E> Set<E> getOrderedSet()
    {
        return new LinkedHashSet<>();
    }

    /**
     * Create and return a new {@link Queue}, an unbounded list where items are ordered in a FIFO (first-in-first-out)
     * manner.
     *
     * @param <E> the type of the elements in the queue
     * @return a new, empty {@link LinkedList}
     */
    public <E> Queue<E> getQueue()
    {
        return new LinkedList<>();
    }

    /**
     * Create and return a new {@link BlockingQueue}, an unbounded queue that additionally supports operations that wait
     * for the queue to become non-empty when retrieving an element.
     *
     * @param <E> the type of the elements in the queue
     * @return a new, empty {@link BlockingQueue}
     */
    public <E> BlockingQueue<E> getBlockingQueue()
    {
        return new LinkedBlockingQueue<>();
    }

    /**
     * Create and return a new {@link Queue}, which instead of the FIFO ordering uses the natural order of the items
     * added to the queue, so that the retrieved item is always the lowest one. All the items added to this queue must
     * be non-null and be comparable with the other items in the queue.
     *
     * @param <E> the type of the elements in the queue
     * @return a new, empty {@link PriorityQueue}
     */
    public <E extends Comparable<E>> Queue<E> getPriorityQueue()
    {
        return new PriorityQueue<>();
    }

    /**
     * Returns an unmodifiable view of the specified list.
     *
     * @param <E> the type of the elements in the list
     * @param input the list to wrap in an unmodifiable bridge
     * @return an unmodifiable view of the list
     */
    public <E> List<E> unmodifiable(List<E> input)
    {
        if (input == null) {
            return null;
        }
        return Collections.unmodifiableList(input);
    }

    /**
     * Returns an unmodifiable view of the specified map.
     *
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     * @param input the map to wrap in an unmodifiable bridge
     * @return an unmodifiable view of the map
     */
    public <K, V> Map<K, V> unmodifiable(Map<K, V> input)
    {
        if (input == null) {
            return null;
        }
        return Collections.unmodifiableMap(input);
    }

    /**
     * Returns an unmodifiable view of the specified set.
     *
     * @param <E> the type of the elements in the set
     * @param input the set to wrap in an unmodifiable bridge
     * @return an unmodifiable view of the set
     */
    public <E> Set<E> unmodifiable(Set<E> input)
    {
        if (input == null) {
            return null;
        }
        return Collections.unmodifiableSet(input);
    }

    /**
     * Returns an unmodifiable view of the specified collection.
     *
     * @param <E> the type of the elements in the collection
     * @param input the collection to wrap in an unmodifiable bridge
     * @return an unmodifiable view of the collection
     */
    public <E> Collection<E> unmodifiable(Collection<E> input)
    {
        if (input == null) {
            return null;
        }
        return Collections.unmodifiableCollection(input);
    }

    /**
     * Returns a {@link Collection} containing the union of the given {@link Collection}s.
     *
     * @param <E> the type of the elements in the collection
     * @param a the first collection, must be non-null
     * @param b the second collection, must be non-null
     * @return the union of the two collections
     */
    public <E> Collection<E> union(Collection<E> a, Collection<E> b)
    {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return CollectionUtils.union(a, b);
    }

    /**
     * Returns a {@link Collection} containing the intersection of the given {@link Collection}s.
     *
     * @param <E> the type of the elements in the collection
     * @param a the first collection, must be non-null
     * @param b the second collection, must be non-null
     * @return the intersection of the two collections
     */
    public <E> Collection<E> intersection(Collection<E> a, Collection<E> b)
    {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return CollectionUtils.intersection(a, b);
    }

    /**
     * Returns a {@link Collection} containing the exclusive disjunction (symmetric difference) of the given
     * {@link Collection}s.
     *
     * @param <E> the type of the elements in the collection
     * @param a the first collection, must be non-null
     * @param b the second collection, must be non-null
     * @return the symmetric difference of the two collections
     */
    public <E> Collection<E> disjunction(Collection<E> a, Collection<E> b)
    {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return CollectionUtils.disjunction(a, b);
    }

    /**
     * Reverse the order of the elements within a list, so that the last element is moved to the beginning of the list,
     * the next-to-last element to the second position, and so on. The input list is modified in place, so this
     * operation will succeed only if the list is modifiable.
     *
     * @param <E> the type of the elements in the list
     * @param input the list to reverse
     * @return {@code true} if the list was successfully reversed, {@code false} otherwise
     */
    public <E> boolean reverseModifiable(List<E> input)
    {
        if (input == null) {
            return false;
        }
        try {
            Collections.reverse(input);
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        }
    }

    /**
     * Sort the elements within a list according to their natural order. The input list is modified in place, so this
     * operation will succeed only if the list is modifiable.
     *
     * @param <E> the type of the elements in the list
     * @param input the list to sort
     * @return {@code true} if the list was successfully sorted, {@code false} otherwise
     */
    public <E extends Comparable<E>> boolean sortModifiable(List<E> input)
    {
        if (input == null) {
            return false;
        }
        try {
            Collections.sort(input);
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        }
    }
}
