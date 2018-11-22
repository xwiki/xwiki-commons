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
package org.xwiki.cache.internal;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheEntry;
import org.xwiki.cache.event.CacheEntryEvent;
import org.xwiki.cache.util.AbstractCache;

/**
 * Implementation of {@link Cache} based on a simple Map.
 * <p>
 * Does not support eviction.
 * 
 * @param <T> the class of the data stored in the cache.
 * @version $Id$
 */
public class MapCache<T> extends AbstractCache<T>
{
    private Map<String, T> map = new HashMap<>();

    class MapCacheEntry implements CacheEntry<T>
    {
        private final String key;

        private final T value;

        MapCacheEntry(String key, T value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public Cache<T> getCache()
        {
            return MapCache.this;
        }

        @Override
        public String getKey()
        {
            return this.key;
        }

        @Override
        public T getValue()
        {
            return this.value;
        }
    }

    /**
     * Default constructor.
     */
    public MapCache()
    {
        super(null);
    }

    private CacheEntryEvent<T> toEvent(final String key, final T value)
    {
        return new CacheEntryEvent<T>()
        {
            @Override
            public CacheEntry<T> getEntry()
            {
                return new MapCacheEntry(key, value);
            }

            @Override
            public Cache<T> getCache()
            {
                return MapCache.this;
            }
        };
    }

    @Override
    public void set(String key, T value)
    {
        this.map.put(key, value);

        sendEntryAddedEvent(toEvent(key, value));
    }

    @Override
    public T get(String key)
    {
        return this.map.get(key);
    }

    @Override
    public void remove(String key)
    {
        if (this.map.containsKey(key)) {
            T value = this.map.remove(key);

            sendEntryRemovedEvent(toEvent(key, value));
        } else {
            this.map.remove(key);
        }
    }

    @Override
    public void removeAll()
    {
        this.map.clear();

        Map<String, T> eventMap = new HashMap<>(this.map);

        for (Map.Entry<String, T> entry : eventMap.entrySet()) {
            sendEntryRemovedEvent(toEvent(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public String toString()
    {
        return this.map.toString();
    }
}
