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
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.cache.eviction.EntryEvictionConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.cache.test.CacheEntryListenerTest.EventType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for testing cache component implementation. Also test eviction.
 * <p>
 * Implementations with asynch eviction system should write there own tests.
 *
 * @version $Id$
 */
public abstract class AbstractEvictionGenericTestCache extends AbstractGenericTestCache
{
    /**
     * Indicate if the cache implementation send event for evicted entries.
     */
    private boolean supportEvictionEvent;

    /**
     * @param roleHint             the role hint of the cache component implementation to test.
     * @param supportEvictionEvent indicate of the cache implementation send eviction related events
     */
    protected AbstractEvictionGenericTestCache(String roleHint, boolean supportEvictionEvent)
    {
        super(roleHint);

        this.supportEvictionEvent = supportEvictionEvent;
    }

    protected void customizeEviction(EntryEvictionConfiguration eviction)
    {

    }

    /**
     * Validate the maximum cache entries constraint.
     *
     * @throws Exception error
     */
    @Test
    void createAndDestroyCacheLRUMaxEntries() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        CacheConfiguration conf = new CacheConfiguration();
        LRUEvictionConfiguration lec = new LRUEvictionConfiguration();
        lec.setMaxEntries(1);
        customizeEviction(lec);
        conf.put(LRUEvictionConfiguration.CONFIGURATIONID, lec);

        Cache<Object> cache = factory.newCache(conf);

        assertNotNull(cache);

        CacheEntryListenerTest eventListener;
        if (this.supportEvictionEvent) {
            eventListener = new CacheEntryListenerTest();
            cache.addCacheEntryListener(eventListener);
        } else {
            eventListener = null;
        }

        cache.set(KEY, VALUE);

        assertEquals(VALUE, cache.get(KEY));

        cache.set(KEY2, VALUE2);

        if (eventListener != null) {
            assertTrue(eventListener.waitForEntryEvent(EventType.REMOVE), "No value has been evicted from the cache");
            assertSame(VALUE, eventListener.getRemovedEvent().getEntry().getValue());
        }

        assertNull(cache.get(KEY));
        assertEquals(VALUE2, cache.get(KEY2));

        cache.dispose();
    }

    /**
     * Validate the maximum time to live constraint.
     *
     * @throws Exception error
     */
    @Test
    void createAndDestroyCacheLRUMAxIdle() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        CacheConfiguration conf = new CacheConfiguration();
        LRUEvictionConfiguration lec = new LRUEvictionConfiguration();
        lec.setMaxIdle(1);
        customizeEviction(lec);
        conf.put(LRUEvictionConfiguration.CONFIGURATIONID, lec);

        Cache<Object> cache = factory.newCache(conf);

        assertNotNull(cache);

        CacheEntryListenerTest eventListener;
        if (this.supportEvictionEvent) {
            eventListener = new CacheEntryListenerTest();
            cache.addCacheEntryListener(eventListener);
        } else {
            eventListener = null;
        }

        cache.set(KEY, VALUE);

        assertEquals(VALUE, cache.get(KEY));

        if (eventListener != null) {
            assertTrue(eventListener.waitForEntryEvent(EventType.REMOVE),
                "No value has expired from the cache after provided max idle time");
            assertSame(VALUE, eventListener.getRemovedEvent().getEntry().getValue());
        }

        assertNull(cache.get(KEY));

        cache.dispose();
    }

    /**
     * Validate the maximum time to live constraint.
     *
     * @throws Exception error
     */
    @Test
    void createAndDestroyCacheLRULifespan() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        CacheConfiguration conf = new CacheConfiguration();
        LRUEvictionConfiguration lec = new LRUEvictionConfiguration();
        lec.setLifespan(1);
        customizeEviction(lec);
        conf.put(LRUEvictionConfiguration.CONFIGURATIONID, lec);

        Cache<Object> cache = factory.newCache(conf);

        assertNotNull(cache);

        CacheEntryListenerTest eventListener;
        if (this.supportEvictionEvent) {
            eventListener = new CacheEntryListenerTest();
            cache.addCacheEntryListener(eventListener);
        } else {
            eventListener = null;
        }

        cache.set(KEY, VALUE);

        assertEquals(VALUE, cache.get(KEY));

        if (eventListener != null) {
            assertTrue(eventListener.waitForEntryEvent(EventType.REMOVE),
                "No value has expired from the cache after provide lifespan");
            assertSame(VALUE, eventListener.getRemovedEvent().getEntry().getValue());
        }

        assertNull(cache.get(KEY));

        cache.dispose();
    }

    /**
     * Validate the combination of maximum time to live and maximum cache entries constraints.
     *
     * @throws Exception error
     */
    @Test
    void createAndDestroyCacheLRUAll() throws Exception
    {
        CacheFactory factory = getCacheFactory();

        LRUCacheConfiguration conf = new LRUCacheConfiguration();
        LRUEvictionConfiguration lec = conf.getLRUEvictionConfiguration();
        lec.setMaxEntries(1);
        lec.setMaxIdle(1);
        lec.setLifespan(1);
        customizeEviction(lec);

        Cache<Object> cache = factory.newCache(conf);

        assertNotNull(cache);

        CacheEntryListenerTest eventListener;
        if (this.supportEvictionEvent) {
            eventListener = new CacheEntryListenerTest();
            cache.addCacheEntryListener(eventListener);
        }

        cache.set(KEY, VALUE);

        assertEquals(VALUE, cache.get(KEY));

        cache.set(KEY2, VALUE2);

        assertNull(cache.get(KEY));
        assertEquals(VALUE2, cache.get(KEY2));

        Thread.sleep(1100);

        assertNull(cache.get(KEY));
        assertNull(cache.get(KEY2));

        cache.dispose();
    }
}
