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
package org.xwiki.store.blob.internal;

import jakarta.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultBlobStoreManager}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultBlobStoreManagerTest
{
    @InjectMockComponents
    private DefaultBlobStoreManager blobStoreManager;

    @MockComponent
    private BlobStoreConfiguration configuration;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @Mock
    private BlobStore testStore;

    @Mock
    private BlobStore specificStore;

    @Mock
    private BlobStore migrationStore;

    @MockComponent
    @Named("file")
    private BlobStoreManager fileBlobStoreManager;

    @MockComponent
    @Named("file/specificStore")
    private BlobStoreManager specificBlobStoreManagerMock;

    @BeforeEach
    void setup() throws BlobStoreException
    {
        when(this.configuration.getStoreHint()).thenReturn("file");
        when(this.fileBlobStoreManager.getBlobStore("testStore")).thenReturn(this.testStore);
        when(this.specificBlobStoreManagerMock.getBlobStore("specificStore")).thenReturn(this.specificStore);
    }

    @Test
    void getBlobStoreWhenStoreExistsInCache() throws Exception
    {
        // Setup: Pre-populate the cache by calling getBlobStore once
        // First call to populate cache
        BlobStore firstResult = this.blobStoreManager.getBlobStore("testStore");

        // Second call should use cache
        BlobStore secondResult = this.blobStoreManager.getBlobStore("testStore");

        assertSame(firstResult, secondResult);
        assertSame(this.testStore, secondResult);
        // The component lookup should only happen once due to caching
        verify(this.fileBlobStoreManager, times(1)).getBlobStore("testStore");
    }

    @Test
    void getBlobStoreWithSpecificHint() throws Exception
    {
        BlobStore result = this.blobStoreManager.getBlobStore("specificStore");

        assertSame(this.specificStore, result);
        verify(this.specificBlobStoreManagerMock).getBlobStore("specificStore");
    }

    @Test
    void getBlobStoreWithMigrationWhenCurrentStoreIsEmpty() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn("s3");

        BlobStoreManager s3BlobStoreManager =
            this.componentManager.registerMockComponent(BlobStoreManager.class, "s3");

        when(s3BlobStoreManager.getBlobStore("testStore")).thenReturn(this.migrationStore);
        when(this.testStore.isEmptyDirectory(BlobPath.ROOT)).thenReturn(true);

        BlobStore result = this.blobStoreManager.getBlobStore("testStore");

        assertSame(this.testStore, result);
        verify(this.testStore).isEmptyDirectory(BlobPath.ROOT);
        verify(this.testStore).moveDirectory(this.migrationStore, BlobPath.ROOT, BlobPath.ROOT);
        verify(this.configuration).getMigrationStoreHint();
    }

    @Test
    void getBlobStoreWithMigrationWhenCurrentStoreIsNotEmpty() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn("s3");

        BlobStoreManager s3BlobStoreManager =
            this.componentManager.registerMockComponent(BlobStoreManager.class, "s3");

        when(s3BlobStoreManager.getBlobStore("testStore")).thenReturn(this.migrationStore);
        when(this.testStore.isEmptyDirectory(BlobPath.ROOT)).thenReturn(false);

        BlobStore result = this.blobStoreManager.getBlobStore("testStore");

        assertSame(this.testStore, result);
        verify(this.testStore).isEmptyDirectory(BlobPath.ROOT);
        verify(this.testStore, never()).moveDirectory(any(), any(), any());
    }

    @Test
    void getBlobStoreWithSameStoreAndMigrationHint() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn("file");

        BlobStore result = this.blobStoreManager.getBlobStore("testStore");

        assertSame(this.testStore, result);
        verify(this.testStore, never()).isEmptyDirectory(any());
        verify(this.testStore, never()).moveDirectory(any(), any(), any());
    }

    @Test
    void getBlobStoreWhenNoMigrationHint() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn(null);

        BlobStore result = this.blobStoreManager.getBlobStore("testStore");

        assertSame(this.testStore, result);
        verify(this.configuration).getMigrationStoreHint();
        verify(this.testStore, never()).isEmptyDirectory(any());
        verify(this.testStore, never()).moveDirectory(any(), any(), any());
    }

    @Test
    void getBlobStoreThrowsComponentLookupException()
    {
        when(this.configuration.getStoreHint()).thenReturn("foo");
        // Don't register any component manager for "foo" hint, causing a lookup exception

        BlobStoreException exception = assertThrows(BlobStoreException.class, () -> {
            this.blobStoreManager.getBlobStore("testStore");
        });

        assertEquals("Failed to get or create blob store with name [testStore]", exception.getMessage());
        assertInstanceOf(ComponentLookupException.class, exception.getCause());
    }

    @Test
    void getBlobStoreThrowsBlobStoreException() throws Exception
    {
        when(this.fileBlobStoreManager.getBlobStore("testStore"))
            .thenThrow(new BlobStoreException("Store error"));

        BlobStoreException exception = assertThrows(BlobStoreException.class, () -> {
            this.blobStoreManager.getBlobStore("testStore");
        });

        assertEquals("Store error", exception.getMessage());
    }

    @Test
    void disposeWithDisposableBlobStores() throws Exception
    {
        // Create a mock that implements both BlobStore and Disposable
        DisposableBlobStore disposableBlobStore = mock();

        when(this.fileBlobStoreManager.getBlobStore("disposableStore")).thenReturn(disposableBlobStore);
        when(this.fileBlobStoreManager.getBlobStore("regularStore")).thenReturn(this.testStore);

        // Populate the cache with both types of stores
        this.blobStoreManager.getBlobStore("disposableStore");
        this.blobStoreManager.getBlobStore("regularStore");

        this.blobStoreManager.dispose();

        verify(disposableBlobStore).dispose();
    }

    // Helper interface to create a mock that implements both BlobStore and Disposable
    private interface DisposableBlobStore extends BlobStore, Disposable
    {
    }
}

