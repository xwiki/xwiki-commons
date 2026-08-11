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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CacheLoaderGroup}.
 *
 * @version $Id$
 */
class CacheLoaderGroupTest
{
    private static final String KEY = "key";

    private static final String KEY_2 = "key2";

    private static final String VALUE = "value";

    private final CacheLoaderGroup group = new CacheLoaderGroup();

    /**
     * A cache loader storing full values, similar to a document cache.
     */
    private final CacheLoader<String, Exception> valueCacheLoader = new CacheLoader<>(this.group);

    /**
     * A cache loader storing if a value exists, similar to a page exist cache.
     */
    private final CacheLoader<Boolean, Exception> existsCacheLoader = new CacheLoader<>(this.group);

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void invalidationDuringLoadOfOtherCacheLoaderOfTheGroup(boolean invalidateAll) throws Exception
    {
        CompletableFuture<String> arrivedInLoadFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> continueLoadFuture = new CompletableFuture<>();

        FailableFunction<String, Boolean, Exception> loader = mock();
        when(loader.apply(KEY)).thenAnswer(invocation -> {
            arrivedInLoadFuture.complete(invocation.getArgument(0));
            return continueLoadFuture.get(10, TimeUnit.SECONDS);
        });

        FailableBiConsumer<String, Boolean, Exception> setter = mock();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            // Start a load on the exists cache loader.
            Future<Boolean> loadFuture =
                executorService.submit(() -> this.existsCacheLoader.loadAndStoreInCache(KEY, loader, setter));

            assertEquals(KEY, arrivedInLoadFuture.get(5, TimeUnit.SECONDS));

            // Invalidate through the other cache loader of the group, this is what happens when a document is saved.
            if (invalidateAll) {
                Runnable invalidateAllRunnable = mock();
                this.valueCacheLoader.invalidateAll(invalidateAllRunnable);
                verify(invalidateAllRunnable).run();
            } else {
                Consumer<String> invalidateConsumer = mock();
                this.valueCacheLoader.invalidate(KEY, invalidateConsumer);
                verify(invalidateConsumer).accept(KEY);
            }

            // Let the load complete with the now stale information that the value doesn't exist.
            continueLoadFuture.complete(false);

            assertFalse(loadFuture.get(5, TimeUnit.SECONDS));
            verify(loader).apply(KEY);
            // The stale value must not be stored in the cache.
            verifyNoInteractions(setter);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void invalidationOfOtherKeyDoesntStopLoad() throws Exception
    {
        CompletableFuture<String> arrivedInLoadFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> continueLoadFuture = new CompletableFuture<>();

        FailableFunction<String, Boolean, Exception> loader = mock();
        when(loader.apply(KEY)).thenAnswer(invocation -> {
            arrivedInLoadFuture.complete(invocation.getArgument(0));
            return continueLoadFuture.get(10, TimeUnit.SECONDS);
        });

        FailableBiConsumer<String, Boolean, Exception> setter = mock();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            Future<Boolean> loadFuture =
                executorService.submit(() -> this.existsCacheLoader.loadAndStoreInCache(KEY, loader, setter));

            assertEquals(KEY, arrivedInLoadFuture.get(5, TimeUnit.SECONDS));

            Consumer<String> invalidateConsumer = mock();
            this.group.invalidate(KEY_2, invalidateConsumer);
            verify(invalidateConsumer).accept(KEY_2);

            continueLoadFuture.complete(true);

            assertTrue(loadFuture.get(5, TimeUnit.SECONDS));
            verify(setter).accept(KEY, true);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void parallelLoadsOfTheSameKeyByDifferentCacheLoaders() throws Exception
    {
        CompletableFuture<String> arrivedInValueLoadFuture = new CompletableFuture<>();
        CompletableFuture<String> continueValueLoadFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> valueLoader = mock();
        when(valueLoader.apply(KEY)).thenAnswer(invocation -> {
            arrivedInValueLoadFuture.complete(invocation.getArgument(0));
            return continueValueLoadFuture.get(10, TimeUnit.SECONDS);
        });

        FailableBiConsumer<String, String, Exception> valueSetter = mock();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            Future<String> valueLoadFuture =
                executorService.submit(() -> this.valueCacheLoader.loadAndStoreInCache(KEY, valueLoader, valueSetter));

            assertEquals(KEY, arrivedInValueLoadFuture.get(5, TimeUnit.SECONDS));

            // A load of the same key by another cache loader of the group must neither block nor return the value of
            // the running load as it has a different type.
            FailableFunction<String, Boolean, Exception> existsLoader = mock();
            when(existsLoader.apply(KEY)).thenReturn(true);
            FailableBiConsumer<String, Boolean, Exception> existsSetter = mock();

            assertTrue(this.existsCacheLoader.loadAndStoreInCache(KEY, existsLoader, existsSetter));
            verify(existsLoader).apply(KEY);
            verify(existsSetter).accept(KEY, true);

            continueValueLoadFuture.complete(VALUE);

            assertEquals(VALUE, valueLoadFuture.get(5, TimeUnit.SECONDS));
            verify(valueSetter).accept(KEY, VALUE);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void invalidationDoesntAffectCacheLoaderOfOtherGroup() throws Exception
    {
        CacheLoader<Boolean, Exception> otherGroupCacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedInLoadFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> continueLoadFuture = new CompletableFuture<>();

        FailableFunction<String, Boolean, Exception> loader = mock();
        when(loader.apply(KEY)).thenAnswer(invocation -> {
            arrivedInLoadFuture.complete(invocation.getArgument(0));
            return continueLoadFuture.get(10, TimeUnit.SECONDS);
        });

        FailableBiConsumer<String, Boolean, Exception> setter = mock();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            Future<Boolean> loadFuture =
                executorService.submit(() -> otherGroupCacheLoader.loadAndStoreInCache(KEY, loader, setter));

            assertEquals(KEY, arrivedInLoadFuture.get(5, TimeUnit.SECONDS));

            Consumer<String> invalidateConsumer = mock();
            this.group.invalidate(KEY, invalidateConsumer);
            verify(invalidateConsumer).accept(KEY);

            continueLoadFuture.complete(true);

            assertTrue(loadFuture.get(5, TimeUnit.SECONDS));
            verify(setter).accept(KEY, true);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }
}
