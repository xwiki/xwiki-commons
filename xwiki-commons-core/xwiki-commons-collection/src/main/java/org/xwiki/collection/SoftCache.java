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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent map with soft references for both keys and values, mostly used to share equal instances (the key being
 * the value) without preventing them from being garbage collected.
 * <p>
 * Keys are compared with {@link Object#equals(Object)}. Reads don't take any lock, so that many threads can look up
 * entries at the same time. Entries whose key or value has been garbage collected are removed on the next write.
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @version $Id$
 * @since 10.8RC1
 * @since 9.11.8
 */
public class SoftCache<K, V>
{
    private final ConcurrentHashMap<Object, ValueReference<V>> map = new ConcurrentHashMap<>();

    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    /**
     * Soft reference to a key, equal to any other key reference whose key is equal.
     * 
     * @param <K> the type of the key
     */
    static final class KeyReference<K> extends SoftReference<K>
    {
        private final int hash;

        KeyReference(K key, ReferenceQueue<Object> queue)
        {
            super(key, queue);

            this.hash = key.hashCode();
        }

        @Override
        public int hashCode()
        {
            return this.hash;
        }

        @Override
        public boolean equals(Object object)
        {
            // Once cleared, a key reference isn't equal to anything, not even to itself: ConcurrentHashMap checks the
            // identity of keys before calling equals, so the purge can still remove it.
            Object key = get();

            return key != null && object instanceof KeyReference<?> reference && key.equals(reference.get());
        }
    }

    /**
     * Strong reference to a key used to look up an entry without allocating a soft reference.
     */
    private static final class LookupKey
    {
        private final Object key;

        private final int hash;

        LookupKey(Object key)
        {
            this.key = key;
            this.hash = key.hashCode();
        }

        @Override
        public int hashCode()
        {
            return this.hash;
        }

        @Override
        public boolean equals(Object object)
        {
            return object instanceof KeyReference<?> reference && this.key.equals(reference.get());
        }
    }

    /**
     * Soft reference to a value, which remembers the reference to its key so that the entry can be removed when the
     * value is garbage collected.
     * 
     * @param <V> the type of the value
     */
    static final class ValueReference<V> extends SoftReference<V>
    {
        private final KeyReference<?> keyReference;

        ValueReference(V value, KeyReference<?> keyReference, ReferenceQueue<Object> queue)
        {
            super(value, queue);

            this.keyReference = keyReference;
        }

        KeyReference<?> getKeyReference()
        {
            return this.keyReference;
        }
    }

    /**
     * @param key the entry key
     * @return the value associated to the passed key
     */
    public V get(K key)
    {
        if (key == null) {
            return null;
        }

        ValueReference<V> valueReference = this.map.get(new LookupKey(key));

        return valueReference != null ? valueReference.get() : null;
    }

    /**
     * Get the value associated to the passed key. If no value can be found store and return the passed default value.
     * When several threads call this method at the same time with equal keys, they all get the same value.
     * 
     * @param key the entry key
     * @param defaultValue the default value
     * @return the value associated to the passed key
     */
    public V get(K key, V defaultValue)
    {
        V value = get(key);

        if (value != null) {
            return value;
        }

        ValueReference<V> newValueReference = newValueReference(key, defaultValue);

        purge();

        // Keep a strong reference to the returned value so that it cannot be garbage collected before it's returned.
        AtomicReference<V> sharedValue = new AtomicReference<>();
        this.map.compute(newValueReference.getKeyReference(), (keyReference, existingValueReference) -> {
            V existingValue = existingValueReference != null ? existingValueReference.get() : null;

            if (existingValue != null) {
                sharedValue.set(existingValue);

                return existingValueReference;
            }

            sharedValue.set(defaultValue);

            return newValueReference;
        });

        return sharedValue.get();
    }

    /**
     * Associate passed key to passed value.
     * 
     * @param key the entry key
     * @param value the entry value
     */
    public void put(K key, V value)
    {
        ValueReference<V> valueReference = newValueReference(key, value);

        purge();

        this.map.put(valueReference.getKeyReference(), valueReference);
    }

    private ValueReference<V> newValueReference(K key, V value)
    {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        return new ValueReference<>(value, new KeyReference<>(key, this.queue), this.queue);
    }

    private void purge()
    {
        for (Reference<?> reference = this.queue.poll(); reference != null; reference = this.queue.poll()) {
            if (reference instanceof ValueReference<?> valueReference) {
                // Only remove the entry if it's still associated to the collected value.
                this.map.remove(valueReference.getKeyReference(), valueReference);
            } else {
                this.map.remove(reference);
            }
        }
    }

    /**
     * @param key the entry key
     * @return the reference to the value associated to the passed key, {@code null} if there isn't any
     */
    ValueReference<V> getValueReference(K key)
    {
        return this.map.get(new LookupKey(key));
    }

    /**
     * @return the number of entries, including the ones whose key or value was garbage collected but which weren't
     *         removed yet
     */
    int size()
    {
        return this.map.size();
    }
}
