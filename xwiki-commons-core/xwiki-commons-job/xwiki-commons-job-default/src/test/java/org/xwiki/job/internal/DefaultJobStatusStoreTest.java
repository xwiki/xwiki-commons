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
package org.xwiki.job.internal;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.tail.LoggerTail;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultJobStatusStore}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultJobStatusStoreTest
{
    private static final List<String> ID = List.of("id1", "id2");

    private static final String ID_STRING = "id1/id2";

    private static final String ERROR = "error";

    private static final String TYPE = "type";

    @RegisterExtension
    private final LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private DefaultJobStatusStore store;

    @MockComponent
    private PersistentJobStatusStore persistentJobStatusStore;

    @MockComponent
    private CacheManager cacheManager;

    @MockComponent
    private JobManagerConfiguration configuration;

    private Cache<JobStatus> cache;

    @BeforeComponent
    void before() throws Exception
    {
        when(this.configuration.getJobStatusCacheSize()).thenReturn(100);
        this.cache = mock();
        doReturn(this.cache).when(this.cacheManager).createNewCache(any());
    }

    @Test
    void initializeWhenCacheCreationFails() throws CacheException
    {
        when(this.configuration.getJobStatusCacheSize()).thenReturn(100);
        when(this.cacheManager.createNewCache(any())).thenThrow(new CacheException(ERROR));

        assertThrows(Exception.class, () -> this.store.initialize());
    }

    @Test
    void getJobStatusWhenCacheHit()
    {
        JobStatus status = mock();

        when(this.cache.get(ID_STRING)).thenReturn(status);

        assertThat(this.store.getJobStatus(ID), sameInstance(status));

        verify(this.cache).get(ID_STRING);
        verifyNoInteractions(this.persistentJobStatusStore);
    }

    @Test
    void getJobStatusWhenCacheMissThenLoadedAndCached() throws Exception
    {
        JobStatus status = mock();

        when(this.cache.get(ID_STRING)).thenReturn(null);
        when(this.persistentJobStatusStore.loadJobStatusWithLock(ID)).thenReturn(status);

        assertThat(this.store.getJobStatus(ID), sameInstance(status));

        verify(this.persistentJobStatusStore).loadJobStatusWithLock(ID);
        verify(this.cache).set(ID_STRING, status);
    }

    @Test
    void getJobStatusWhenCacheMissThenSecondCheckAvoidsRead()
    {
        JobStatus status = mock();

        when(this.cache.get(ID_STRING)).thenReturn(null, status);

        assertThat(this.store.getJobStatus(ID), sameInstance(status));

        verify(this.cache, times(2)).get(ID_STRING);
        verifyNoInteractions(this.persistentJobStatusStore);
    }

    @Test
    void getJobStatusWhenLoadFailsRemovesCacheAndLogs() throws Exception
    {
        when(this.cache.get(ID_STRING)).thenReturn(null);
        when(this.persistentJobStatusStore.loadJobStatusWithLock(ID)).thenThrow(new IOException(ERROR));

        assertNull(this.store.getJobStatus(ID));

        verify(this.cache).remove(ID_STRING);
        assertEquals("Failed to load job status for id [id1, id2]", logCapture.getMessage(0));
    }

    @Test
    void removeDelegatesAndEvictsCache() throws Exception
    {
        this.store.remove(ID);

        verify(this.persistentJobStatusStore).removeJobStatusWithLock(ID);
        verify(this.cache).remove(ID_STRING);
    }

    @Test
    void removeWhenPersistenceFailsKeepsCacheAndLogs() throws Exception
    {
        doThrow(new IOException(ERROR)).when(this.persistentJobStatusStore).removeJobStatusWithLock(ID);

        this.store.remove(ID);

        verify(this.cache, never()).remove(ID_STRING);
        assertEquals("Failed to remove job status for id [id1, id2]", logCapture.getMessage(0));
    }

    @Test
    void storeWithNullStatusDoesNothing()
    {
        this.store.store(null);

        verifyNoInteractions(this.cache);
        verifyNoInteractions(this.persistentJobStatusStore);
    }

    @Test
    void storeWithNullRequestDoesNothing()
    {
        JobStatus status = new DefaultJobStatus<>(TYPE, null, null, null, null);

        this.store.store(status);

        verifyNoInteractions(this.cache);
        verifyNoInteractions(this.persistentJobStatusStore);
    }

    @Test
    void storeWithNullIdDoesNothing()
    {
        JobStatus status = new DefaultJobStatus<>(TYPE, new DefaultRequest(), null, null, null);

        this.store.store(status);

        verifyNoInteractions(this.cache);
        verifyNoInteractions(this.persistentJobStatusStore);
    }

    @Test
    void storeSerializableSavesSynchronously() throws Exception
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(ID);
        request.setStatusSerialized(true);

        JobStatus status = new DefaultJobStatus<>(TYPE, request, null, null, null);

        this.store.store(status);

        verify(this.cache).set(ID_STRING, status);
        verify(this.persistentJobStatusStore).saveJobStatusWithLock(status);
    }

    @Test
    void storeAsyncSerializableSchedulesSave() throws Exception
    {
        ExecutorService executor = mock();
        ReflectionUtils.setFieldValue(this.store, "executorService", executor);

        DefaultRequest request = new DefaultRequest();
        request.setId(ID);
        request.setStatusSerialized(true);

        JobStatus status = new DefaultJobStatus<>(TYPE, request, null, null, null);

        this.store.storeAsync(status);

        verify(this.cache).set(ID_STRING, status);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.captor();
        verify(executor).execute(runnableCaptor.capture());

        runnableCaptor.getValue().run();

        verify(this.persistentJobStatusStore).saveJobStatusWithLock(status);
    }

    @Test
    void storeSerializableWhenSaveFailsLogs() throws Exception
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(ID);
        request.setStatusSerialized(true);

        JobStatus status = new DefaultJobStatus<>(TYPE, request, null, null, null);

        doThrow(new IOException(ERROR)).when(this.persistentJobStatusStore).saveJobStatusWithLock(status);

        this.store.store(status);

        assertEquals("Failed to save job status for id [id1, id2]", logCapture.getMessage(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void createLoggerTailDelegates(boolean readonly) throws Exception
    {
        LoggerTail loggerTail = mock();
        when(this.persistentJobStatusStore.createLoggerTail(ID, readonly)).thenReturn(loggerTail);

        try (LoggerTail actual = this.store.createLoggerTail(ID, readonly)) {
            assertThat(actual, sameInstance(loggerTail));
        }

        verify(this.persistentJobStatusStore).createLoggerTail(ID, readonly);
        verifyNoMoreInteractions(this.persistentJobStatusStore);
    }
}
