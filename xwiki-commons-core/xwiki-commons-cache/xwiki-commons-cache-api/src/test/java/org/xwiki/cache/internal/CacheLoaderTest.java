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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.component.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CacheLoader}.
 *
 * @version $Id$
 */
class CacheLoaderTest
{
    protected static final String KEY = "key";

    protected static final String VALUE = "value";

    protected static final String VALUE_2 = "value2";

    protected static final String KEY_2 = "key2";

    @Test
    void basicLoadAndStore() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        FailableFunction<String, String, Exception> loader = mock();
        when(loader.apply(KEY)).thenReturn(VALUE);

        FailableBiConsumer<String, String, Exception> setter = mock();

        // Load a value
        String result = cacheLoader.loadAndStoreInCache(KEY, loader, setter);
        assertEquals(VALUE, result);
        verify(loader).apply(KEY);
        verify(setter).accept(KEY, VALUE);

        // Load the same value again - should call loader again.
        String secondResult = cacheLoader.loadAndStoreInCache(KEY, loader, setter);
        assertEquals(VALUE, secondResult);
        verify(loader, times(2)).apply(KEY);
        verify(setter, times(2)).accept(KEY, VALUE);
    }

    @Test
    void invalidation()
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        Consumer<String> invalidateConsumer = mock();

        // Invalidate the key
        cacheLoader.invalidate(KEY, invalidateConsumer);

        verify(invalidateConsumer).accept(KEY);
    }

    @Test
    void invalidateAll()
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();
        Runnable invalidateRunnable = mock();

        cacheLoader.invalidateAll(invalidateRunnable);
        verify(invalidateRunnable).run();
    }

    @Test
    void parallelLoadsOfSameKey() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedLoadFuture = new CompletableFuture<>();
        CompletableFuture<String> continueLoadFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> loader = mock();
        when(loader.apply(KEY)).thenAnswer(invocation -> {
            arrivedLoadFuture.complete(KEY);
            return continueLoadFuture.get(10, TimeUnit.SECONDS);
        });

        FailableBiConsumer<String, String, Exception> setter = mock();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            // Start two parallel loads
            Future<String> firstLoadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));
            Future<String> secondLoadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));

            // Wait until the first load reaches the loader function
            assertEquals(KEY, arrivedLoadFuture.get(5, TimeUnit.SECONDS));

            // Both futures should be waiting
            assertThrows(TimeoutException.class, () -> firstLoadFuture.get(100, TimeUnit.MILLISECONDS));
            assertThrows(TimeoutException.class, () -> secondLoadFuture.get(100, TimeUnit.MILLISECONDS));

            // Allow the load to complete
            continueLoadFuture.complete(VALUE);

            // Both futures should complete with the same result
            String firstResult = firstLoadFuture.get(5, TimeUnit.SECONDS);
            String secondResult = secondLoadFuture.get(5, TimeUnit.SECONDS);

            assertEquals(VALUE, firstResult);
            assertEquals(VALUE, secondResult);
            verify(loader, times(1)).apply(KEY);
            verify(setter, times(1)).accept(KEY, VALUE);
        } finally {
            executorService.shutdown();
        }
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void parallelLoadOfSecondKeyDoesntBlock() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedLoadFuture = new CompletableFuture<>();
        CompletableFuture<String> continueLoadFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> loader = mock();
        when(loader.apply(KEY)).thenAnswer(invocation -> {
            arrivedLoadFuture.complete(invocation.getArgument(0));
            return continueLoadFuture.get(10, TimeUnit.SECONDS);
        });

        CompletableFuture<String> arrivedInSetFuture = new CompletableFuture<>();
        CompletableFuture<String> continueInSetFuture = new CompletableFuture<>();

        FailableBiConsumer<String, String, Exception> setter = mock();
        doAnswer(invocation -> {
            arrivedInSetFuture.complete(invocation.getArgument(0));
            return continueInSetFuture.get(10, TimeUnit.SECONDS);
        }).when(setter).accept(KEY, VALUE);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            // Start a load
            Future<String> loadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));

            // Wait until the load reaches the loader function
            assertEquals(KEY, arrivedLoadFuture.get(5, TimeUnit.SECONDS));

            // Start a second load which should not block.
            verifyLoad(KEY_2, VALUE_2, cacheLoader);

            // Allow the first load to continue
            continueLoadFuture.complete(VALUE);

            // Wait until the load reaches the setter.
            assertEquals(KEY, arrivedInSetFuture.get(5, TimeUnit.SECONDS));

            // Start a third load which shouldn't block.
            verifyLoad("key3", "value3", cacheLoader);

            // Unblock the setter.
            continueInSetFuture.complete(VALUE);

            String loadResult = loadFuture.get(5, TimeUnit.SECONDS);

            assertEquals(VALUE, loadResult);
            verify(loader).apply(KEY);
            verify(setter).accept(KEY, VALUE);
        } finally {
            executorService.shutdown();
        }
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    private static void verifyLoad(String key, String value, CacheLoader<String, Exception> cacheLoader)
        throws Exception
    {
        FailableFunction<String, String, Exception> loader = mock();
        doReturn(value).when(loader).apply(key);
        FailableBiConsumer<String, String, Exception> setter = mock();
        assertEquals(value, cacheLoader.loadAndStoreInCache(key, loader, setter));
        verify(setter).accept(key, value);
    }

    @Test
    void invalidationDuringLoad() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedLoadFuture = new CompletableFuture<>();
        CompletableFuture<String> continueLoadFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> loader = mock();
        when(loader.apply(KEY)).thenAnswer(invocation -> {
            arrivedLoadFuture.complete(invocation.getArgument(0));
            return continueLoadFuture.get(10, TimeUnit.SECONDS);
        });

        FailableBiConsumer<String, String, Exception> setter = mock();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            // Start a load
            Future<String> loadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));

            // Wait until the load reaches the loader function
            assertEquals(KEY, arrivedLoadFuture.get(5, TimeUnit.SECONDS));

            Consumer<String> invalidateConsumer = mock();

            // Invalidate the key while loading is in progress
            cacheLoader.invalidate(KEY, invalidateConsumer);
            verify(invalidateConsumer).accept(KEY);

            // Start a second load which should not block.
            verifyLoad(KEY, VALUE_2, cacheLoader);

            // Allow the first load to complete
            continueLoadFuture.complete(VALUE);

            // The load should complete but the value should not be stored in the cache
            String loadResult = loadFuture.get(5, TimeUnit.SECONDS);

            assertEquals(VALUE, loadResult);
            verify(loader).apply(KEY);
            verifyNoInteractions(setter);
        } finally {
            executorService.shutdown();
        }
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void invalidationBlocksDuringCacheWrite() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedInSetFuture = new CompletableFuture<>();
        CompletableFuture<Void> continueInSetFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> loader = mock();
        when(loader.apply(KEY)).thenReturn(VALUE);
        FailableBiConsumer<String, String, Exception> setter = mock();
        doAnswer(invocation -> {
            arrivedInSetFuture.complete(invocation.getArgument(0));
            return continueInSetFuture.get(10, TimeUnit.SECONDS);
        }).when(setter).accept(KEY, VALUE);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<String> loadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));

            assertEquals(KEY, arrivedInSetFuture.get(5, TimeUnit.SECONDS));

            // Invalidate the key while the setter is waiting
            Consumer<String> invalidateConsumer = mock();
            CompletableFuture<String> arrivedInInvalidateFuture = new CompletableFuture<>();
            doAnswer(invocation -> {
                arrivedInInvalidateFuture.complete(invocation.getArgument(0));
                return null;
            }).when(invalidateConsumer).accept(KEY);

            Future<?> invalidateFuture =
                executorService.submit(() -> cacheLoader.invalidate(KEY, invalidateConsumer));

            // Assert that invalidate is blocked and waiting.
            assertThrows(TimeoutException.class, () -> arrivedInInvalidateFuture.get(100, TimeUnit.MILLISECONDS));

            continueInSetFuture.complete(null);
            assertEquals(VALUE, loadFuture.get(5, TimeUnit.SECONDS));

            invalidateFuture.get(5, TimeUnit.SECONDS);
            verify(loader).apply(KEY);
            verify(setter).accept(KEY, VALUE);
            verify(invalidateConsumer).accept(KEY);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void loadStartedDuringInvalidateStoresAfterInvalidate() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedInSetFuture = new CompletableFuture<>();
        CompletableFuture<Void> continueInSetFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> loader = mock();
        when(loader.apply(KEY)).thenReturn(VALUE);
        FailableBiConsumer<String, String, Exception> setter = mock();
        doAnswer(invocation -> {
            arrivedInSetFuture.complete(invocation.getArgument(0));
            return continueInSetFuture.get(10, TimeUnit.SECONDS);
        }).when(setter).accept(KEY, VALUE);

        CompletableFuture<String> arrivedInInvalidateFuture = new CompletableFuture<>();
        CompletableFuture<Void> continueInInvalidateFuture = new CompletableFuture<>();
        Consumer<String> invalidateConsumer = mock();
        doAnswer(invocation -> {
            arrivedInInvalidateFuture.complete(invocation.getArgument(0));
            return continueInInvalidateFuture.get(10, TimeUnit.SECONDS);
        }).when(invalidateConsumer).accept(KEY);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<?> invalidateFuture = executorService.submit(() -> cacheLoader.invalidate(KEY, invalidateConsumer));

            // Wait for the thread to arrive in invalidate.
            arrivedInInvalidateFuture.get(5, TimeUnit.SECONDS);

            Future<String> loadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));

            // Ensure that the load is blocked now as the invalidate call is still running.
            assertThrows(TimeoutException.class, () -> arrivedInSetFuture.get(100, TimeUnit.MILLISECONDS));

            continueInInvalidateFuture.complete(null);
            // Invalidate should finish now.
            invalidateFuture.get(5, TimeUnit.SECONDS);

            // We should arrive in the set method now.
            arrivedInSetFuture.get(5, TimeUnit.SECONDS);
            continueInSetFuture.complete(null);

            assertEquals(VALUE, loadFuture.get(5, TimeUnit.SECONDS));

            verify(loader).apply(KEY);
            verify(setter).accept(KEY, VALUE);
            verify(invalidateConsumer).accept(KEY);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void invalidateAllStopsAllRunningRequestsFromStoring(boolean invalidate) throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        Map<String, CompletableFuture<Object>> arrivedInLoadFutures =
            Map.of(KEY, new CompletableFuture<>(), KEY_2, new CompletableFuture<>());

        CompletableFuture<String> continueInLoadFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> loader = mock();

        when(loader.apply(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            arrivedInLoadFutures.get(key).complete(key);
            return continueInLoadFuture.get(10, TimeUnit.SECONDS) + key;
        });

        FailableBiConsumer<String, String, Exception> setter = mock();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            // Start two parallel loads
            Future<String> firstLoadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));
            Future<String> secondLoadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY_2, loader, setter));

            // Wait until the first load reaches the loader function
            assertEquals(KEY, arrivedInLoadFutures.get(KEY).get(5, TimeUnit.SECONDS));

            // Wait until the second load reaches the loader function
            assertEquals(KEY_2, arrivedInLoadFutures.get(KEY_2).get(5, TimeUnit.SECONDS));

            if (invalidate) {
                // Invalidate all keys while loading is in progress
                Runnable invalidateRunnable = mock();
                cacheLoader.invalidateAll(invalidateRunnable);
                verify(invalidateRunnable).run();
            }

            // Allow the loads to complete
            continueInLoadFuture.complete(VALUE);

            String firstResult = firstLoadFuture.get(5, TimeUnit.SECONDS);
            String secondResult = secondLoadFuture.get(5, TimeUnit.SECONDS);

            assertEquals(VALUE + KEY, firstResult);
            assertEquals(VALUE + KEY_2, secondResult);
            verify(loader, times(2)).apply(anyString());
            if (invalidate) {
                verifyNoInteractions(setter);
            } else {
                verify(setter).accept(KEY, VALUE + KEY);
                verify(setter).accept(KEY_2, VALUE + KEY_2);
            }
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void loadStartedAfterInvalidateNotRemovedByFirstLoad() throws Exception
    {
        // Scenario: an initial load starts but is then invalidated. While the initial load still runs, two loads for
        // the same key are started. After the first one started, the initial load completes. Ensure that the second one
        // still shares the data with the first one, i.e., that the initial load doesn't remove the load after
        // completing.

        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedInInitialLoadFuture = new CompletableFuture<>();
        CompletableFuture<String> continueInInitialLoadFuture = new CompletableFuture<>();

        FailableFunction<String, String, Exception> initialLoader = mock();
        when(initialLoader.apply(KEY)).thenAnswer(invocation -> {
            arrivedInInitialLoadFuture.complete(KEY);
            return continueInInitialLoadFuture.get(10, TimeUnit.SECONDS);
        });

        FailableBiConsumer<String, String, Exception> setter = mock();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        try {
            Future<String> initialLoadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, initialLoader, setter));

            arrivedInInitialLoadFuture.get(5, TimeUnit.SECONDS);

            Consumer<String> invalidateConsumer = mock();
            cacheLoader.invalidate(KEY, invalidateConsumer);
            verify(invalidateConsumer).accept(KEY);

            CompletableFuture<String> arrivedSecondLoadFuture = new CompletableFuture<>();
            CompletableFuture<String> continueSecondLoadFuture = new CompletableFuture<>();
            FailableFunction<String, String, Exception> loader2 = mock();
            when(loader2.apply(KEY)).thenAnswer(invocation -> {
                arrivedSecondLoadFuture.complete(KEY);
                return continueSecondLoadFuture.get(10, TimeUnit.SECONDS);
            });
            FailableBiConsumer<String, String, Exception> setter2 = mock();

            Future<String> secondLoadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader2, setter2));

            // Wait for the second load to call the loader.
            arrivedSecondLoadFuture.get(5, TimeUnit.SECONDS);

            // Let the initial load continue.
            continueInInitialLoadFuture.complete(VALUE);

            assertEquals(VALUE, initialLoadFuture.get(5, TimeUnit.SECONDS));

            // Start a third load.
            FailableFunction<String, String, Exception> loader3 = mock();
            FailableBiConsumer<String, String, Exception> setter3 = mock();
            Future<String> thirdLoadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader3, setter3));

            // Wait a bit on the third load to ensure that a) it doesn't complete immediately and b) ensure that it
            // starts waiting for the second load.
            assertThrows(TimeoutException.class, () -> thirdLoadFuture.get(500, TimeUnit.MILLISECONDS));

            continueSecondLoadFuture.complete(VALUE_2);

            assertEquals(VALUE_2, secondLoadFuture.get(5, TimeUnit.SECONDS));
            assertEquals(VALUE_2, thirdLoadFuture.get(5, TimeUnit.SECONDS));

            verifyNoInteractions(loader3, setter3, setter);
            verify(setter2).accept(KEY, VALUE_2);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void exceptionPropagation() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        RuntimeException expectedException = new RuntimeException("Test exception");
        FailableFunction<String, String, Exception> loader = mock();
        when(loader.apply(KEY)).thenThrow(expectedException);

        FailableBiConsumer<String, String, Exception> setter = mock();

        ExecutionException exception = assertThrows(ExecutionException.class,
            () -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));

        assertEquals(expectedException, exception.getCause());
        verifyNoInteractions(setter);

        // Verify that the next load works again normally, even when executed by another thread.
        // There is a special case for the current thread to remove the loader entry when it exists, ensure that it also
        // works outside this special case.
        doReturn(VALUE).when(loader).apply(KEY);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<String> loadFuture =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader, setter));

            String result = loadFuture.get(5, TimeUnit.SECONDS);
            assertEquals(VALUE, result);
            verify(loader, times(2)).apply(KEY);
            verify(setter).accept(KEY, VALUE);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void recursiveLoad() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        FailableBiConsumer<String, String, Exception> setter = mock();
        FailableBiConsumer<String, String, Exception> setter2 = mock();
        FailableFunction<String, String, Exception> loader = key -> {
            assertEquals(VALUE_2, cacheLoader.loadAndStoreInCache(key, k -> VALUE_2, setter2));
            return VALUE;
        };

        assertEquals(VALUE, cacheLoader.loadAndStoreInCache(KEY, loader, setter));
        verify(setter2).accept(KEY, VALUE_2);
        verifyNoInteractions(setter);
    }

    @Test
    void twoRecursiveLoads() throws Exception
    {
        // Create two threads where the first thread starts with KEY, the second starts with KEY_2, and then after
        // both threads arrived in the loader, they load the other key.
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        CompletableFuture<String> arrivedInLoad1 = new CompletableFuture<>();
        CompletableFuture<String> continueLoad1 = new CompletableFuture<>();
        FailableFunction<String, String, Exception> innerLoad1 = mock();
        when(innerLoad1.apply(KEY_2)).thenReturn(VALUE_2);
        FailableBiConsumer<String, String, Exception> setter1 = mock();
        FailableBiConsumer<String, String, Exception> innerSetter1 = mock();

        CompletableFuture<String> arrivedInLoad2 = new CompletableFuture<>();
        CompletableFuture<String> continueLoad2 = new CompletableFuture<>();

        FailableFunction<String, String, Exception> loader1 = key -> {
            arrivedInLoad1.complete(key);
            continueLoad1.get(10, TimeUnit.SECONDS);
            assertEquals(VALUE_2, cacheLoader.loadAndStoreInCache(KEY_2, innerLoad1, innerSetter1));
            return VALUE;
        };

        FailableFunction<String, String, Exception> innerLoad2 = mock();
        when(innerLoad2.apply(KEY)).thenReturn(VALUE);
        FailableBiConsumer<String, String, Exception> setter2 = mock();
        FailableBiConsumer<String, String, Exception> innerSetter2 = mock();

        FailableFunction<String, String, Exception> loader2 = key -> {
            arrivedInLoad2.complete(key);
            continueLoad2.get(10, TimeUnit.SECONDS);
            assertEquals(VALUE, cacheLoader.loadAndStoreInCache(KEY, innerLoad2, innerSetter2));
            return VALUE_2;
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<String> loadFuture1 =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY, loader1, setter1));
            Future<String> loadFuture2 =
                executorService.submit(() -> cacheLoader.loadAndStoreInCache(KEY_2, loader2, setter2));

            // Wait until the first load reaches the loader function
            assertEquals(KEY, arrivedInLoad1.get(5, TimeUnit.SECONDS));

            // Wait until the second load reaches the loader function
            assertEquals(KEY_2, arrivedInLoad2.get(5, TimeUnit.SECONDS));

            // Allow the loads to complete
            continueLoad1.complete(VALUE);
            continueLoad2.complete(VALUE_2);

            String result1 = loadFuture1.get(5, TimeUnit.SECONDS);
            String result2 = loadFuture2.get(5, TimeUnit.SECONDS);

            assertEquals(VALUE, result1);
            assertEquals(VALUE_2, result2);

            verify(innerLoad1).apply(KEY_2);
            verify(innerLoad2).apply(KEY);
            verifyNoInteractions(innerSetter1, innerSetter2);
            verify(setter1).accept(KEY, VALUE);
            verify(setter2).accept(KEY_2, VALUE_2);
        } finally {
            executorService.shutdown();
        }

        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void testRecursiveCallWithException() throws Exception
    {
        CacheLoader<String, Exception> cacheLoader = new CacheLoader<>();

        FailableBiConsumer<String, String, Exception> setter = mock();
        FailableBiConsumer<String, String, Exception> setter2 = mock();
        FailableFunction<String, String, Exception> loader = key -> {
            RuntimeException testException = new RuntimeException("Test exception");
            ExecutionException executionException = assertThrows(ExecutionException.class,
                () -> cacheLoader.loadAndStoreInCache(KEY_2, k -> {
                    throw testException;
                }, setter2));
            assertSame(testException, executionException.getCause());
            return VALUE;
        };

        assertEquals(VALUE, cacheLoader.loadAndStoreInCache(KEY, loader, setter));
        verify(setter).accept(KEY, VALUE);
        verifyNoInteractions(setter2);

        // Get the internal cache loader map and check that it is empty.
        Field currentLoadsField = ReflectionUtils.getField(CacheLoader.class, "currentLoads");
        currentLoadsField.setAccessible(true);
        Map<?, ?> currentLoads = (Map<?, ?>) currentLoadsField.get(cacheLoader);
        assertTrue(currentLoads.isEmpty(), "CacheLoader should be empty after exception: " + currentLoads);

        // Get the internal thread local and check that it has been cleared.
        Field threadLocalField = ReflectionUtils.getField(CacheLoader.class, "currentLoad");
        threadLocalField.setAccessible(true);
        ThreadLocal<?> threadLocal = (ThreadLocal<?>) threadLocalField.get(cacheLoader);
        assertNull(threadLocal.get(), "ThreadLocal should be empty after exception: " + threadLocal.get());
    }
}
