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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Cache loading helper. Handles parallel loads and invalidations during loads to ensure that no invalidated data is
 * inserted into caches. The class is intentionally not tied to a single cache to support more complex use cases with
 * complex cache designs.
 *
 * @param <V> the value of the cache
 * @param <E> the exception type thrown by the methods for getting, loading and storing values
 *
 * @version $Id$
 * @since 17.3.0RC1
 */
public class CacheLoader<V, E extends Exception>
{
    private final ConcurrentHashMap<String, LoaderEntry> currentLoads = new ConcurrentHashMap<>();

    private final ReadWriteLock invalidationLock = new ReentrantReadWriteLock();

    private class LoaderEntry
    {
        private final FutureTask<V> future;

        private volatile boolean invalidated;

        LoaderEntry(String key, FailableFunction<String, V, E> loader, FailableBiConsumer<String, V, E> setter)
        {
            this.future = new FutureTask<>(() -> {
                V value = loader.apply(key);

                // Use the read lock to avoid that invalidated is set to true while this code runs.
                Lock lock = CacheLoader.this.invalidationLock.readLock();
                lock.lock();
                try {
                    if (!LoaderEntry.this.invalidated) {
                        setter.accept(key, value);
                    }
                } finally {
                    lock.unlock();
                    // Remove this loader entry from the current loads, but only if it hasn't been superseded by a
                    // new entry yet.
                    CacheLoader.this.currentLoads.computeIfPresent(key, (k, v) -> v == LoaderEntry.this ? null : v);
                }

                return value;
            });
        }
    }

    /**
     * Load a value from a secondary store and then inserting it into the cache.
     *
     * @param key the key of the value to fetch
     * @param loader the load function that fetches the data from a store. This method is assumed to be slow and
     * will only be executed once, even when several requests run in parallel.
     * @param setter the method for storing the retrieved value in the cache. This method is only called when the
     * value hasn't been invalidated in the meantime.
     * @return the value loaded by either this call or another call that was already running
     * @throws ExecutionException when executing one of the passed functions fails
     */
    public V loadAndStoreInCache(String key, FailableFunction<String, V, E> loader,
        FailableBiConsumer<String, V, E> setter) throws ExecutionException
    {
        V result;

        LoaderEntry loaderEntry = new LoaderEntry(key, loader, setter);
        LoaderEntry existingEntry = this.currentLoads.putIfAbsent(key, loaderEntry);
        if (existingEntry == null) {
            loaderEntry.future.run();
            result = Uninterruptibles.getUninterruptibly(loaderEntry.future);
        } else {
            result = Uninterruptibles.getUninterruptibly(existingEntry.future);
        }

        return result;
    }

    /**
     * Invalidate the given key and remove it from the cache by calling the passed function. This method ensures that
     * after the passed function is called, no value whose loading started before this method was called will be
     * written to the cache. The passed function is also executed under a lock that ensures that no writes of that cache
     * entry happen while it is removed.
     *
     * @param key the key to invalidate
     * @param invalidate the function that removes the entry from the cache
     */
    public void invalidate(String key, Consumer<String> invalidate)
    {
        // Lock the write lock to ensure that when this method returns, the corresponding loader won't store any value
        // anymore (which it could otherwise by reading the flag before the method is executed and storing the value
        // afterward).
        Lock lock = this.invalidationLock.writeLock();
        lock.lock();
        try {
            LoaderEntry entry = this.currentLoads.remove(key);
            if (entry != null) {
                entry.invalidated = true;
            }
            // Execute the actual removal from the cache under the lock to avoid that any new load that is started
            // after the remove call above stores a value in the cache before this removal. Such a value would be
            // erroneously removed again if this removal was after the lock, leading to a lost load operation which
            // could be costly.
            invalidate.accept(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Invalidate all cache entries. This ensures that after the passed function is called, no value whose loading
     * started before this method was called is written to the cache. The passed method is called under a lock that
     * ensures that no writes of the cache happen in parallel.
     *
     * @param invalidateAll the function to clear the cache
     */
    public void invalidateAll(Runnable invalidateAll)
    {
        Lock lock = this.invalidationLock.writeLock();
        lock.lock();
        try {
            this.currentLoads.forEach((k, v) -> {
                this.currentLoads.remove(k);
                v.invalidated = true;
            });
            invalidateAll.run();
        } finally {
            lock.unlock();
        }
    }
}
