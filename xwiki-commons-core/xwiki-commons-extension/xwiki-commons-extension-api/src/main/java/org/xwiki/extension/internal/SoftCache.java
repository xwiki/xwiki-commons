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
package org.xwiki.extension.internal;

import java.lang.ref.SoftReference;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A concurrent version of {@link WeakHashMap} in which the values are soft references.
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @version $Id$
 * @since 9.1RC1
 * @since 8.4.3
 */
public class SoftCache<K, V>
{
    private WeakHashMap<K, SoftReference<V>> map = new WeakHashMap<>();

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * @param key the entry key
     * @return the value associated to the passed key
     */
    public V get(K key)
    {
        this.lock.readLock().lock();

        try {
            SoftReference<V> reference = this.map.get(key);

            return reference != null ? reference.get() : null;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Get the value associated to the passed key. If no value can be found stored and return the passed default value.
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
            this.map.put(key, new SoftReference<>(value));
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
