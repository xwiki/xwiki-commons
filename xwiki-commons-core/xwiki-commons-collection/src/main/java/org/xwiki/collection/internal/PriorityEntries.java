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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A concurrent storage for a Map where values are sorted both by comparison and time of insertion.
 * 
 * @param <E> the type of the values stored in the map
 * @version $Id$
 * @since 15.4RC1
 */
public class PriorityEntries<E extends Comparable<E>>
{
    protected final Map<String, E> map;

    protected List<E> sorted;

    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Create a new empty {@link PriorityEntries}.
     */
    public PriorityEntries()
    {
        this.map = new LinkedHashMap<>();
    }

    /**
     * Create a new empty {@link PriorityEntries}.
     * 
     * @param initialCapacity the initial capacity
     */
    public PriorityEntries(int initialCapacity)
    {
        this.map = new LinkedHashMap<>(initialCapacity);
    }

    /**
     * @return the lock
     */
    public ReentrantReadWriteLock getLock()
    {
        return this.lock;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     * key.
     * 
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this there is no mapping for the key
     */
    public E get(String key)
    {
        this.lock.readLock().lock();

        try {
            return this.map.get(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings
     */
    public boolean isEmpty()
    {
        this.lock.readLock().lock();

        try {
            return this.map.isEmpty();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains more than {@code Integer.MAX_VALUE}
     * elements, returns {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this map
     */
    public int size()
    {
        this.lock.readLock().lock();

        try {
            return this.map.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * @return the values sorted by natural order
     */
    public List<E> getSorted()
    {
        if (this.sorted == null) {
            this.lock.readLock().lock();

            try {
                this.sorted = this.map.values().stream().sorted().collect(Collectors.toList());
            } finally {
                this.lock.readLock().unlock();
            }
        }

        return this.sorted;
    }

    /**
     * Performs the given action for each value in this map until all entries have been processed or the action throws
     * an exception.
     * <p>
     * In this process, no specific order is guaranteed. If you need a fully sorted version, you should use
     * {@link #getSorted()}.
     * 
     * @param action the action to be performed for each value
     */
    public void forEachEntry(Consumer<E> action)
    {
        this.lock.readLock().lock();

        try {
            this.map.values().forEach(action);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * @param key the key to insert
     * @param value the value to insert
     * @exception NullPointerException if the key or value is {@code null}
     */
    public void put(String key, E value)
    {
        // Make sure the value is not null
        if (value == null) {
            throw new NullPointerException();
        }

        this.lock.writeLock().lock();

        try {
            this.map.put(key, value);
            this.sorted = null;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}.
     */
    public E remove(String key)
    {
        this.lock.writeLock().lock();

        try {
            E value = this.map.remove(key);
            if (value != null) {
                this.sorted = null;
            }

            return value;
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
