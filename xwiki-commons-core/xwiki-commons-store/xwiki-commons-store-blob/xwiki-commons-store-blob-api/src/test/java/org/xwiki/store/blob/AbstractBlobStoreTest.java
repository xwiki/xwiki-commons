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
package org.xwiki.store.blob;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AbstractBlobStore}.
 *
 * @version $Id$
 */
@ExtendWith(MockitoExtension.class)
class AbstractBlobStoreTest
{
    private static final BlobPath SOURCE_PATH = BlobPath.from("source/dir");

    private static final BlobPath TARGET_PATH = BlobPath.from("target/dir");

    @Mock
    private BlobStore otherStore;

    private AbstractBlobStore<BlobStoreProperties> blobStore;

    @BeforeEach
    void setUp()
    {
        this.blobStore = mock(Mockito.CALLS_REAL_METHODS);
        this.blobStore.initialize("test-store", "test-hint", mock(BlobStoreProperties.class));
    }

    @Test
    void moveDirectoryWithinSameStore() throws Exception
    {
        Blob blob1 = mock();
        BlobPath blob1Path = BlobPath.from("source/dir/file1.txt");
        when(blob1.getPath()).thenReturn(blob1Path);

        Blob blob2 = mock();
        BlobPath blob2Path = BlobPath.from("source/dir/subdir/file2.txt");
        when(blob2.getPath()).thenReturn(blob2Path);

        when(this.blobStore.listBlobs(SOURCE_PATH)).thenReturn(Stream.of(blob1, blob2));
        
        this.blobStore.moveDirectory(SOURCE_PATH, TARGET_PATH);
        
        verify(this.blobStore).moveBlob(blob1Path, BlobPath.from("target/dir/file1.txt"));
        verify(this.blobStore).moveBlob(blob2Path, BlobPath.from("target/dir/subdir/file2.txt"));
    }

    @ParameterizedTest
    @CsvSource({
        "source/dir, source/dir/subdir, Cannot move a directory to inside itself",
        "source/dir/subdir, source/dir, Cannot move a directory to one of its ancestors",
        "source/dir, source/dir, Cannot move a directory to inside itself",
        "'', backup, Cannot move a directory to inside itself"
    })
    void moveDirectoryFailureScenarios(String source, String target, String expectedMessage) throws Exception
    {
        BlobPath sourcePath = source.isEmpty() ? BlobPath.ROOT : BlobPath.from(source);
        BlobPath targetPath = BlobPath.from(target);
        
        BlobStoreException exception = assertThrows(BlobStoreException.class, () -> {
            this.blobStore.moveDirectory(sourcePath, targetPath);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
        verify(this.blobStore, never()).listBlobs(any());
    }

    @Test
    void moveDirectoryFromOtherStore() throws Exception
    {
        BlobPath blob1Path = BlobPath.from("source/dir/file1.txt");
        Blob blob1 = mock();
        when(blob1.getPath()).thenReturn(blob1Path);

        Blob blob2 = mock();
        BlobPath blob2Path = BlobPath.from("source/dir/nested/file2.txt");
        when(blob2.getPath()).thenReturn(blob2Path);

        when(this.otherStore.listBlobs(SOURCE_PATH)).thenReturn(Stream.of(blob1, blob2));

        this.blobStore.moveDirectory(this.otherStore, SOURCE_PATH, TARGET_PATH);
        
        verify(this.blobStore).moveBlob(this.otherStore, blob1Path, BlobPath.from("target/dir/file1.txt"));
        verify(this.blobStore).moveBlob(this.otherStore, blob2Path, BlobPath.from("target/dir/nested/file2.txt"));
    }

    @Test
    void moveDirectoryWithEmptyDirectory() throws Exception
    {
        when(this.blobStore.listBlobs(SOURCE_PATH)).thenReturn(Stream.empty());
        
        this.blobStore.moveDirectory(SOURCE_PATH, TARGET_PATH);
        
        verify(this.blobStore, never()).moveBlob(any(BlobPath.class), any(BlobPath.class));
    }

    @Test
    void moveDirectoryClosesStreamOnSuccess() throws Exception
    {
        Stream<Blob> successStream = spy(Stream.of(getSourceFileBlob()));
        when(this.blobStore.listBlobs(SOURCE_PATH)).thenReturn(successStream);

        this.blobStore.moveDirectory(SOURCE_PATH, TARGET_PATH);

        verify(successStream).close();
    }

    @Test
    void moveDirectoryClosesStreamOnExceptionInSameStore() throws Exception
    {
        Stream<Blob> failStream = spy(Stream.of(getSourceFileBlob()));
        when(this.blobStore.listBlobs(SOURCE_PATH)).thenReturn(failStream);
        doThrow(new BlobStoreException("Move failed")).when(this.blobStore)
            .moveBlob(any(BlobPath.class), any(BlobPath.class));
        
        assertThrows(BlobStoreException.class, () -> this.blobStore.moveDirectory(SOURCE_PATH, TARGET_PATH));

        verify(failStream).close();
    }

    @Test
    void moveDirectoryClosesStreamOnExceptionInDifferentStore() throws Exception
    {
        Blob blob = getSourceFileBlob();

        Stream<Blob> crossStoreFailStream = spy(Stream.of(blob));
        when(this.otherStore.listBlobs(SOURCE_PATH)).thenReturn(crossStoreFailStream);
        doThrow(new BlobStoreException("Cross-store move failed")).when(this.blobStore)
            .moveBlob(any(BlobStore.class), any(BlobPath.class), any(BlobPath.class));
        
        assertThrows(BlobStoreException.class,
            () -> this.blobStore.moveDirectory(this.otherStore, SOURCE_PATH, TARGET_PATH));

        verify(crossStoreFailStream).close();
    }

    private static Blob getSourceFileBlob()
    {
        Blob blob = mock();
        BlobPath blobPath = BlobPath.from("source/dir/file.txt");
        when(blob.getPath()).thenReturn(blobPath);
        return blob;
    }
}
