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

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobWriteMode;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BlobStoreMigrator}.
 *
 * @version $Id$
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@ComponentTest
class BlobStoreMigratorTest
{
    private static final String STORE_NAME = "testStore";

    private static final BlobPath MARKER_PATH = BlobPath.absolute("_migration.txt");

    @InjectMockComponents
    private BlobStoreMigrator migrator;

    @Mock
    private BlobStore targetStore;

    @Mock
    private BlobStore sourceStore;

    @Mock
    private Blob markerBlob;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    @BeforeEach
    void configureStores() throws Exception
    {
        when(this.targetStore.getBlob(MARKER_PATH)).thenReturn(this.markerBlob);

        lenient().when(this.targetStore.getName()).thenReturn(STORE_NAME);
        lenient().when(this.targetStore.getHint()).thenReturn("s3");
        lenient().when(this.sourceStore.getName()).thenReturn(STORE_NAME);
        lenient().when(this.sourceStore.getHint()).thenReturn("filesystem");
    }

    @Test
    void migrateMovesBlobs() throws Exception
    {
        BlobPath blobPath = BlobPath.parse("/documents/file.txt");
        Blob sourceBlob = mockBlob(blobPath);

        when(this.sourceStore.listBlobs(BlobPath.root())).thenReturn(Stream.of(sourceBlob));

        this.migrator.migrate(this.targetStore, this.sourceStore);

        verify(this.markerBlob).writeFromStream(any(InputStream.class), eq(BlobWriteMode.CREATE_NEW));
        verify(this.targetStore).moveBlob(this.sourceStore, blobPath, blobPath, BlobWriteMode.REPLACE_EXISTING);
        verify(this.targetStore).deleteBlob(MARKER_PATH);

        assertEquals(
            "Starting blob store migration for [testStore] (source: [filesystem], target: [s3]).",
            this.logCapture.getMessage(0));
        assertEquals("Completed blob store migration for [testStore].", this.logCapture.getMessage(1));
    }

    @Test
    void migrateWhenMarkerAlreadyExistsResumes() throws Exception
    {
        doThrow(new BlobAlreadyExistsException(MARKER_PATH)).when(this.markerBlob)
            .writeFromStream(any(InputStream.class), eq(BlobWriteMode.CREATE_NEW));
        when(this.sourceStore.listBlobs(BlobPath.root())).thenReturn(Stream.empty());

        assertDoesNotThrow(() -> this.migrator.migrate(this.targetStore, this.sourceStore));

        verify(this.targetStore).deleteBlob(MARKER_PATH);

        assertEquals("Detected existing migration marker [/_migration.txt]; "
                + "resuming migration of blob store [testStore].", this.logCapture.getMessage(0));
        assertEquals("Completed blob store migration for [testStore].", this.logCapture.getMessage(1));
    }

    @Test
    void migrateStopsOnMoveFailure() throws Exception
    {
        BlobPath blobPath = BlobPath.parse("/documents/file.txt");
        Blob sourceBlob = mockBlob(blobPath);
        when(this.sourceStore.listBlobs(BlobPath.root())).thenReturn(Stream.of(sourceBlob));
        doThrow(new BlobStoreException("move failed")).when(this.targetStore)
            .moveBlob(this.sourceStore, blobPath, blobPath, BlobWriteMode.REPLACE_EXISTING);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.migrator.migrate(this.targetStore, this.sourceStore));

        assertThat(exception.getMessage(), containsString("Failed to move blob [/documents/file.txt]"));
        assertThat(exception.getMessage(), containsString("while migrating [testStore]"));
        assertThat(exception.getMessage(), containsString("from [filesystem] to [s3]"));
        verify(this.targetStore, never()).deleteBlob(MARKER_PATH);

        assertEquals(
            "Starting blob store migration for [testStore] (source: [filesystem], target: [s3]).",
            this.logCapture.getMessage(0));
    }

    @Test
    void migrateSkipsBlobThatDisappears() throws Exception
    {
        BlobPath blobPath1 = BlobPath.parse("/documents/file1.txt");
        BlobPath blobPath2 = BlobPath.parse("/documents/file2.txt");
        Blob sourceBlob1 = mockBlob(blobPath1);
        Blob sourceBlob2 = mockBlob(blobPath2);

        when(this.sourceStore.listBlobs(BlobPath.root())).thenReturn(Stream.of(sourceBlob1, sourceBlob2));

        // First blob disappears during migration
        doThrow(new BlobNotFoundException(blobPath1)).when(this.targetStore)
            .moveBlob(this.sourceStore, blobPath1, blobPath1, BlobWriteMode.REPLACE_EXISTING);

        this.migrator.migrate(this.targetStore, this.sourceStore);

        verify(this.targetStore).moveBlob(this.sourceStore, blobPath1, blobPath1, BlobWriteMode.REPLACE_EXISTING);
        verify(this.targetStore).moveBlob(this.sourceStore, blobPath2, blobPath2, BlobWriteMode.REPLACE_EXISTING);
        verify(this.targetStore).deleteBlob(MARKER_PATH);

        assertEquals(
            "Starting blob store migration for [testStore] (source: [filesystem], target: [s3]).",
            this.logCapture.getMessage(0));
        assertEquals("Completed blob store migration for [testStore].", this.logCapture.getMessage(1));
    }

    @Test
    void migrateFailsWhenMarkerCannotBeDeleted() throws Exception
    {
        when(this.sourceStore.listBlobs(BlobPath.root())).thenReturn(Stream.empty());
        doThrow(new BlobStoreException("marker deletion failed")).when(this.targetStore).deleteBlob(MARKER_PATH);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.migrator.migrate(this.targetStore, this.sourceStore));

        assertThat(exception.getMessage(), containsString("Failed to delete migration marker [/_migration.txt]"));
        assertThat(exception.getMessage(), containsString("after migrating store [testStore]"));
        assertThat(exception.getMessage(), containsString("Remove the marker manually"));

        assertEquals(
            "Starting blob store migration for [testStore] (source: [filesystem], target: [s3]).",
            this.logCapture.getMessage(0));
        assertEquals("Completed blob store migration for [testStore].", this.logCapture.getMessage(1));
    }

    @Test
    void migrateFailsWhenMarkerCannotBeCreated() throws Exception
    {
        doThrow(new BlobStoreException("marker creation failed")).when(this.markerBlob)
            .writeFromStream(any(InputStream.class), eq(BlobWriteMode.CREATE_NEW));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.migrator.migrate(this.targetStore, this.sourceStore));

        assertThat(exception.getMessage(), containsString("Failed to create migration marker"));
        assertThat(exception.getMessage(), containsString("for store [testStore]"));
        verify(this.sourceStore, never()).listBlobs(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isMigrationInProgress(boolean markerExists) throws Exception
    {
        when(this.markerBlob.exists()).thenReturn(markerExists);

        boolean result = this.migrator.isMigrationInProgress(this.targetStore);

        assertEquals(markerExists, result);
    }

    private Blob mockBlob(BlobPath path) throws Exception
    {
        Blob blob = mock();
        when(blob.getPath()).thenReturn(path);
        when(this.sourceStore.getBlob(path)).thenReturn(blob);
        return blob;
    }
}
