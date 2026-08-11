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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * A group of {@link CacheLoader}s that all use the same keys but load values of different types, like a cache of
 * documents and a cache that only stores if a document exists.
 * <p>
 * The group is the unit of invalidation while each {@link CacheLoader} of the group is the unit of load sharing: a
 * single invalidation stops the running loads of all cache loaders of the group from storing their values, but a load
 * is only ever shared between calls that use both the same cache loader and the same key as loads of different cache
 * loaders produce values of different types.
 * <p>
 * Cache loaders that are used independently of each other shall not be part of the same group as this would lead to
 * needless invalidations.
 *
 * @version $Id$
 * @since 18.7.0RC1
 */
public class CacheLoaderGroup
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheLoaderGroup.class);

    private final ConcurrentHashMap<LoadKey, LoaderEntry<?>> currentLoads = new ConcurrentHashMap<>();

    private final ReadWriteLock invalidationLock = new ReentrantReadWriteLock();

    // Stores the current load of a thread to enable the detection of recursive calls to loadAndStoreInCache() with
    // different keys. This is stored per group and not per cache loader as a recursive call could also use a different
    // cache loader of the same group.
    // We store the full loader entry to make debugging easier.
    private final ThreadLocal<LoaderEntry<?>> currentLoad = new ThreadLocal<>();

    /**
     * The identifier of a load. Loads are only shared within a single cache loader as different cache loaders of the
     * same group load values of different types. This also means that all entries stored for a given load key are
     * {@code LoaderEntry} instances of the value type of that cache loader.
     *
     * @param cacheLoader the cache loader that started the load
     * @param key the key that is being loaded
     */
    private record LoadKey(CacheLoader<?, ?> cacheLoader, String key)
    {
    }

    private class LoaderEntry<V>
    {
        private final Thread creatingThread = Thread.currentThread();

        private final FutureTask<V> future;

        private volatile boolean invalidated;

        <E extends Exception> LoaderEntry(String key, FailableFunction<String, V, E> loader)
        {
            this.future = new FutureTask<>(() -> loader.apply(key));
        }

        private <E extends Exception> void maybeRunSetter(FailableBiConsumer<String, V, E> setter, String key, V value)
        {
            // First, check outside the lock if the entry is invalidated.
            // This avoids needless locking when the entry has already been invalidated.
            if (!LoaderEntry.this.invalidated) {
                // Use the read lock to avoid that invalidated is set to true while this code runs.
                Lock lock = CacheLoaderGroup.this.invalidationLock.readLock();
                lock.lock();
                try {
                    if (!LoaderEntry.this.invalidated) {
                        setter.accept(key, value);
                    }
                } catch (Exception e) {
                    // Don't propagate exceptions from the setter as a failed cache store shouldn't block using the
                    // read value.
                    LOGGER.error("Error setting value for key [{}] on cache.", key, e);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * Load a value from a secondary store and then insert it into the cache.
     *
     * @param cacheLoader the cache loader on which the load has been requested
     * @param key the key of the value to fetch
     * @param loader the load function that fetches the data from a store
     * @param setter the method for storing the retrieved value in the cache
     * @param <V> the type of the value to load
     * @param <E> the exception type thrown by the passed functions
     * @return the value loaded by either this call or another call that was already running
     * @throws ExecutionException when executing one of the passed functions fails
     */
    <V, E extends Exception> V loadAndStoreInCache(CacheLoader<V, E> cacheLoader, String key,
        FailableFunction<String, V, E> loader, FailableBiConsumer<String, V, E> setter) throws ExecutionException
    {
        LoadKey loadKey = new LoadKey(cacheLoader, key);
        LoaderEntry<V> newEntry = new LoaderEntry<>(key, loader);
        LoaderEntry<V> loaderEntry = getOrInsertLoaderEntry(loadKey, newEntry);

        if (loaderEntry == newEntry) {
            try {
                // We've just inserted that entry, or we are in a recursive call.
                // In both cases, we need to run the loader.
                runLoader(loaderEntry);

                V value = Uninterruptibles.getUninterruptibly(loaderEntry.future);

                loaderEntry.maybeRunSetter(setter, key, value);

                return value;
            } finally {
                // Remove this loader entry from the current loads, but only if it hasn't been superseded by a
                // new entry yet.
                this.currentLoads.computeIfPresent(loadKey, (k, v) -> v == loaderEntry ? null : v);
            }
        }

        return Uninterruptibles.getUninterruptibly(loaderEntry.future);
    }

    @SuppressWarnings("unchecked")
    private <V> LoaderEntry<V> getOrInsertLoaderEntry(LoadKey loadKey, LoaderEntry<V> newEntry)
    {
        // The cast is safe as all entries stored for a load key have been created by the cache loader of that load
        // key and thus have its value type.
        LoaderEntry<V> loaderEntry = (LoaderEntry<V>) this.currentLoads.compute(loadKey, (k, value) -> {
            if (value != null) {
                if (value.creatingThread != Thread.currentThread()) {
                    // If the entry is already being loaded by another thread, return it, everything is fine.
                    return value;
                } else {
                    // If the entry is being loaded by this thread, we're in a recursive call from the loader.
                    // This happens in Hibernate migrations.
                    // Best is to just ignore the original call and call the loader again.
                    // Make sure the original loader entry is invalidated so it doesn't store a value in the cache.
                    // As it is the same thread, there is no need for a lock here.
                    value.invalidated = true;
                }
            }
            return newEntry;
        });

        if (loaderEntry != newEntry && this.currentLoad.get() != null) {
            // We are inside a recursive call, but another thread is already loading the value.
            // In theory, we should be able to wait for that other thread, but it would be possible that this other
            // thread itself does a recursive call and waits for us.
            // Imagine thread A loads X and that loads Y and thread B loads Y and that loads X.
            // This would lead to a deadlock.
            // Therefore, we don't wait for the other thread and just execute the loader again.
            // We set the entry to "invalidated" to disable storing the value in the cache - it will already be stored
            // by the other thread which is hopefully faster than this thread as it inserted the loader entry before
            // this thread.
            // Further, it is important that all entries that haven't been invalidated are in the map to allow the cache
            // invalidation to work correctly.
            loaderEntry = newEntry;
            loaderEntry.invalidated = true;
        }

        return loaderEntry;
    }

    private void runLoader(LoaderEntry<?> loaderEntry)
    {
        LoaderEntry<?> activeLoaderEntry = this.currentLoad.get();
        try {
            this.currentLoad.set(loaderEntry);

            loaderEntry.future.run();
        } finally {
            if (activeLoaderEntry == null) {
                this.currentLoad.remove();
            } else {
                // Restore the old entry.
                this.currentLoad.set(activeLoaderEntry);
            }
        }
    }

    /**
     * Invalidate the given key in all cache loaders of this group and remove it from the cache(s) by calling the passed
     * function. This method ensures that after the passed function is called, no value whose loading started before
     * this method was called will be written to any cache of this group. The passed function is also executed under a
     * lock that ensures that no writes of that cache entry happen while it is removed.
     *
     * @param key the key to invalidate
     * @param invalidate the function that removes the entry from the cache(s)
     */
    public void invalidate(String key, Consumer<String> invalidate)
    {
        // Lock the write lock to ensure that when this method returns, the corresponding loaders won't store any value
        // anymore (which they could otherwise by reading the flag before the method is executed and storing the value
        // afterward).
        Lock lock = this.invalidationLock.writeLock();
        lock.lock();
        try {
            // Invalidate the running loads of that key in all cache loaders of this group as all their values are
            // affected by this invalidation. There is only a handful of running loads at any time, so iterating over
            // all of them is cheaper than maintaining an index by key.
            this.currentLoads.entrySet().removeIf(entry -> {
                if (entry.getKey().key().equals(key)) {
                    entry.getValue().invalidated = true;

                    return true;
                }

                return false;
            });
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
     * Invalidate all cache entries of all cache loaders of this group. This ensures that after the passed function is
     * called, no value whose loading started before this method was called is written to any cache of this group. The
     * passed method is called under a lock that ensures that no writes of the cache(s) happen in parallel.
     *
     * @param invalidateAll the function to clear the cache(s)
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
