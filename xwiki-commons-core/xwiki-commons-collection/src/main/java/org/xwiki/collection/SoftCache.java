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
package org.xwiki.collection;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;

/**
 * A concurrent version of {@link ReferenceMap} with soft reference for both keys and values.
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @version $Id$
 * @since 10.8RC1
 * @since 9.11.8
 */
public class SoftCache<K, V>
{
    private Map<K, V> map = new ReferenceMap<>(ReferenceStrength.SOFT, ReferenceStrength.SOFT);

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * @param key the entry key
     * @return the value associated to the passed key
     */
    public V get(K key)
    {
        this.lock.readLock().lock();

        try {
            return this.map.get(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Get the value associated to the passed key. If no value can be found store and return the passed default value.
     * 
     * @param key the entry key
     * @param defaultValue the default value
     * @return the value associated to the passed key
     */
    public V get(K key, V defaultValue)
    {
        // Check if we only know an equal entry
        V sharedValue = get(key);

        if (sharedValue == null) {
            // If no entry can be found, store and return the passed one
            sharedValue = defaultValue;

            // Make sure to remember the entry
            put(key, defaultValue);
        }

        // Return the shared entry
        return sharedValue;
    }

    /**
     * Associate passed key to passed value.
     * 
     * @param key the entry key
     * @param value the entry value
     */
    public void put(K key, V value)
    {
        this.lock.writeLock().lock();

        try {
            this.map.put(key, value);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
