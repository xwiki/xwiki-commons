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

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;

/**
 * Cache loading helper. Handles parallel loads and invalidations during loads to ensure that no invalidated data is
 * inserted into caches. The class is intentionally not tied to a single cache to support more complex use cases with
 * complex cache designs.
 * <p>
 * When several caches use the same keys but store values of different types, like a cache of documents and a cache
 * that only stores if a document exists, they need a separate cache loader each as loads can only be shared between
 * loads of the same type. Pass the same {@link CacheLoaderGroup} to all of them in that case so that invalidating a
 * key also stops the running loads of the other cache loaders of the group from storing a stale value.
 *
 * @param <V> the value of the cache
 * @param <E> the exception type thrown by the methods for getting, loading and storing values
 *
 * @version $Id$
 * @since 17.3.0RC1
 */
public class CacheLoader<V, E extends Exception>
{
    private final CacheLoaderGroup group;

    /**
     * Create a cache loader that is invalidated independently of any other cache loader.
     */
    public CacheLoader()
    {
        this(new CacheLoaderGroup());
    }

    /**
     * Create a cache loader that is invalidated together with all other cache loaders of the given group.
     *
     * @param group the group of cache loaders that use the same keys and are thus invalidated together
     * @since 18.7.0RC1
     */
    public CacheLoader(CacheLoaderGroup group)
    {
        this.group = group;
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
        return this.group.loadAndStoreInCache(this, key, loader, setter);
    }

    /**
     * Invalidate the given key and remove it from the cache by calling the passed function. This method ensures that
     * after the passed function is called, no value whose loading started before this method was called will be
     * written to the cache. The passed function is also executed under a lock that ensures that no writes of that cache
     * entry happen while it is removed.
     * <p>
     * When this cache loader is part of a group, this also applies to all other cache loaders of that group.
     *
     * @param key the key to invalidate
     * @param invalidate the function that removes the entry from the cache
     */
    public void invalidate(String key, Consumer<String> invalidate)
    {
        this.group.invalidate(key, invalidate);
    }

    /**
     * Invalidate all cache entries. This ensures that after the passed function is called, no value whose loading
     * started before this method was called is written to the cache. The passed method is called under a lock that
     * ensures that no writes of the cache happen in parallel.
     * <p>
     * When this cache loader is part of a group, this also applies to all other cache loaders of that group.
     *
     * @param invalidateAll the function to clear the cache
     */
    public void invalidateAll(Runnable invalidateAll)
    {
        this.group.invalidateAll(invalidateAll);
    }
}
