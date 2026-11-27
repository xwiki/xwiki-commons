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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.xwiki.collection.SoftCache.KeyReference;
import org.xwiki.collection.SoftCache.ValueReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate {@link SoftCache}.
 *
 * @version $Id$
 */
class SoftCacheTest
{
    private static final String KEY = "key";

    private static final String OTHER_KEY = "other";

    private static final String VALUE = "value";

    private final SoftCache<String, String> cache = new SoftCache<>();

    @Test
    void getWithStrongReferences()
    {
        String defaultValue = "default";

        assertNull(this.cache.get(VALUE));
        assertSame(defaultValue, this.cache.get(VALUE, defaultValue));

        this.cache.put(VALUE, VALUE);

        assertSame(VALUE, this.cache.get(VALUE));
        assertSame(VALUE, this.cache.get(VALUE, defaultValue));
    }

    @Test
    void getWithEqualKey()
    {
        this.cache.put(KEY, VALUE);

        assertSame(VALUE, this.cache.get(new String(KEY)));
        assertSame(VALUE, this.cache.get(new String(KEY), "default"));
    }

    @Test
    void nullKeyAndValue()
    {
        assertNull(this.cache.get(null));

        assertThrows(NullPointerException.class, () -> this.cache.get(null, VALUE));
        assertThrows(NullPointerException.class, () -> this.cache.get(KEY, null));
        assertThrows(NullPointerException.class, () -> this.cache.put(null, VALUE));
        assertThrows(NullPointerException.class, () -> this.cache.put(KEY, null));

        assertEquals(0, this.cache.size());
    }

    @Test
    void putReplacesValue()
    {
        String otherValue = "other value";

        this.cache.put(KEY, VALUE);
        this.cache.put(new String(KEY), otherValue);

        assertSame(otherValue, this.cache.get(KEY));
        assertEquals(1, this.cache.size());
    }

    @Test
    void getWithDefaultValueReturnsTheSameInstanceToConcurrentThreads() throws Exception
    {
        int threadCount = 8;
        int keyCount = 1000;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            List<Future<String[]>> futures = new ArrayList<>();
            for (int thread = 0; thread < threadCount; thread++) {
                futures.add(executor.submit(() -> {
                    barrier.await();

                    String[] values = new String[keyCount];
                    for (int i = 0; i < keyCount; i++) {
                        String key = KEY + i;
                        values[i] = this.cache.get(key, new String(key));
                    }

                    return values;
                }));
            }

            String[] expectedValues = futures.get(0).get();
            for (Future<String[]> future : futures) {
                String[] values = future.get();
                for (int i = 0; i < keyCount; i++) {
                    assertSame(expectedValues[i], values[i], "Different values were returned for key " + i);
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void getWithDefaultValueReplacesCollectedValue()
    {
        String defaultValue = "default";

        this.cache.put(KEY, VALUE);
        this.cache.getValueReference(KEY).clear();

        assertNull(this.cache.get(KEY));
        assertSame(defaultValue, this.cache.get(KEY, defaultValue));
        assertSame(defaultValue, this.cache.get(KEY));
        assertEquals(1, this.cache.size());
    }

    @Test
    void purgeEntryWithCollectedValue()
    {
        this.cache.put(KEY, VALUE);
        ValueReference<String, String> valueReference = this.cache.getValueReference(KEY);
        valueReference.clear();
        valueReference.enqueue();

        assertEquals(1, this.cache.size());

        this.cache.put(OTHER_KEY, VALUE);

        assertNull(this.cache.getValueReference(KEY));
        assertEquals(1, this.cache.size());
    }

    @Test
    void purgeEntryWithCollectedKey()
    {
        this.cache.put(KEY, VALUE);
        KeyReference<String> keyReference = this.cache.getValueReference(KEY).getKeyReference();
        keyReference.clear();
        keyReference.enqueue();

        assertNull(this.cache.get(KEY));
        assertEquals(1, this.cache.size());

        this.cache.put(OTHER_KEY, VALUE);

        assertSame(VALUE, this.cache.get(OTHER_KEY));
        assertEquals(1, this.cache.size());
    }

    @Test
    void keepEntryWhileItsStoredKeyIsNotCollected()
    {
        // The map keeps the first key when a value is replaced, so collecting the key passed with the new value
        // doesn't remove the entry: it's removed once the stored key is collected.
        this.cache.put(KEY, VALUE);
        this.cache.put(new String(KEY), VALUE + 2);
        ValueReference<String, String> valueReference = this.cache.getValueReference(KEY);
        valueReference.getKeyReference().clear();
        valueReference.getKeyReference().enqueue();
        valueReference.clear();
        valueReference.enqueue();

        this.cache.put(OTHER_KEY, VALUE);

        assertNull(this.cache.get(KEY));
        assertEquals(2, this.cache.size());
    }
}
