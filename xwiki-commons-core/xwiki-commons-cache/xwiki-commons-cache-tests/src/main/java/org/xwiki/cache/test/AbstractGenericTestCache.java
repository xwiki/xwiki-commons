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
package org.xwiki.cache.test;

import org.junit.jupiter.api.Test;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheFactory;
import org.xwiki.cache.config.CacheConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Base class for testing cache component implementation.
 * 
 * @version $Id$
 */
public abstract class AbstractGenericTestCache extends AbstractTestCache
{
    /**
     * @param roleHint the role hint of the cache component implementation to test.
     */
    protected AbstractGenericTestCache(String roleHint)
    {
        super(roleHint);
    }

    // ///////////////////////////////////////////////////////::
    // Tests

    /**
     * Validate factory initialization.
     * 
     * @throws Exception error.
     */
    @Test
    public void getFactory() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        CacheFactory factory2 = getCacheFactory();

        assertSame(factory, factory2);
    }

    /**
     * Validate some basic cache use case without any constraints.
     * 
     * @throws Exception error.
     */
    @Test
    public void createAndDestroyCacheSimple() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        Cache<Object> cache = factory.newCache(new CacheConfiguration());

        assertNotNull(cache);

        cache.set(KEY, VALUE);
        cache.set(KEY2, VALUE2);

        assertEquals(VALUE, cache.get(KEY));
        assertEquals(VALUE2, cache.get(KEY2));

        cache.dispose();
    }

    /**
     * Validate {@link Cache#remove(String)}.
     * 
     * @throws Exception error.
     */
    @Test
    public void remove() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        Cache<Object> cache = factory.newCache(new CacheConfiguration());

        cache.set(KEY, VALUE);
        cache.set(KEY2, VALUE2);

        cache.remove(KEY);

        assertNull(cache.get(KEY));
        assertEquals(VALUE2, cache.get(KEY2));
    }

    /**
     * Validate {@link Cache#removeAll()}.
     * 
     * @throws Exception error.
     */
    @Test
    public void removeAll() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        Cache<Object> cache = factory.newCache(new CacheConfiguration());

        cache.set(KEY, VALUE);
        cache.set(KEY2, VALUE2);

        cache.removeAll();

        assertNull(cache.get(KEY));
        assertNull(cache.get(KEY2));
    }

    /**
     * Validate event management.
     * 
     * @throws Exception error.
     */
    @Test
    public void events() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        Cache<Object> cache = factory.newCache(new CacheConfiguration());

        CacheEntryListenerTest eventListener = new CacheEntryListenerTest();

        cache.addCacheEntryListener(eventListener);

        cache.set(KEY, VALUE);

        assertNotNull(eventListener.getAddedEvent());
        assertSame(cache, eventListener.getAddedEvent().getCache());
        assertEquals(KEY, eventListener.getAddedEvent().getEntry().getKey());
        assertEquals(VALUE, eventListener.getAddedEvent().getEntry().getValue());

        cache.set(KEY, VALUE2);

        assertNotNull(eventListener.getModifiedEvent());
        assertSame(cache, eventListener.getModifiedEvent().getCache());
        assertEquals(KEY, eventListener.getModifiedEvent().getEntry().getKey());
        assertEquals(VALUE2, eventListener.getModifiedEvent().getEntry().getValue());

        cache.remove(KEY);
        cache.get(KEY);

        assertNotNull(eventListener.getRemovedEvent());
        assertSame(cache, eventListener.getRemovedEvent().getCache());
        assertEquals(KEY, eventListener.getRemovedEvent().getEntry().getKey());
        assertEquals(VALUE2, eventListener.getRemovedEvent().getEntry().getValue());

        eventListener.reinitRemovedEvent();
        cache.remove(KEY);

        assertNull(eventListener.getRemovedEvent());
    }

    /**
     * Validate that two different caches are really different.
     * 
     * @throws Exception error.
     */
    @Test
    public void severalCaches() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        Cache<Object> cache = factory.newCache(new CacheConfiguration());
        Cache<Object> cache2 = factory.newCache(new CacheConfiguration());

        cache.set(KEY, VALUE);

        assertNull(cache2.get(KEY));
    }

    /**
     * Validate that when recreating a cache with the same id the second instance is in a proper state.
     * 
     * @throws Exception error
     */
    @Test
    public void recreateCache() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        CacheConfiguration configuration = new CacheConfiguration();
        configuration.setConfigurationId("test");

        Cache<Object> cache = factory.newCache(configuration);

        cache.set(KEY, VALUE);

        assertEquals(VALUE, cache.get(KEY));

        // dispose the first cache

        cache.dispose();

        // recreate it

        cache = factory.newCache(configuration);

        assertNull(cache.get(KEY));

        cache.set(KEY, VALUE);

        assertEquals(VALUE, cache.get(KEY));
    }
}
