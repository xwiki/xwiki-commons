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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.S3BlobStoreProperties;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import software.amazon.awssdk.services.s3.S3Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3BlobStore}.
 *
 * @version $Id$
 */
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:ClassFanOutComplexity" })
@ComponentTest
class S3BlobStoreTest
{
    private static final String STORE_NAME = "test-store";

    private static final String BUCKET_NAME = "test-bucket";

    private static final String KEY_PREFIX = "prefix";

    private static final BlobPath BLOB_PATH = BlobPath.of(List.of("dir", "file.txt"));

    private static final BlobPath SOURCE_PATH = BlobPath.of(List.of("source", "file.txt"));

    private static final BlobPath TARGET_PATH = BlobPath.of(List.of("target", "file.txt"));

    @InjectMockComponents
    private S3BlobStore store;

    @MockComponent
    private S3ClientManager clientManager;

    @MockComponent
    private S3CopyOperations copyOperations;

    @MockComponent
    private S3DeleteOperations deleteOperations;

    private S3Client s3Client;

    @BeforeEach
    void setUp()
    {
        this.s3Client = mock();
        when(this.clientManager.getS3Client()).thenReturn(this.s3Client);
        S3BlobStoreProperties props = getS3BlobStoreProperties(BUCKET_NAME, KEY_PREFIX);
        this.store.initialize(STORE_NAME, props);
    }

    private static S3BlobStoreProperties getS3BlobStoreProperties(String bucket, String keyPrefix)
    {
        S3BlobStoreProperties props = new S3BlobStoreProperties();
        props.setBucket(bucket);
        props.setKeyPrefix(keyPrefix);
        return props;
    }

    @ParameterizedTest
    @CsvSource({
        "my-store, my-bucket, my-prefix/, my-prefix",
        "store, bucket, '', ''"
    })
    void initialize(String storeName, String bucketName, String inputKeyPrefix, String expectedKeyPrefix)
    {
        S3BlobStoreProperties props = getS3BlobStoreProperties(bucketName, inputKeyPrefix);
        this.store.initialize(storeName, props);

        assertEquals(storeName, this.store.getName());
        assertEquals(bucketName, this.store.getBucketName());
        assertEquals(expectedKeyPrefix, this.store.getKeyMapper().getKeyPrefix());
    }

    @Test
    void getBlobCreatesS3BlobWithCorrectArguments()
    {
        BlobPath path = BlobPath.of(List.of("my", "test", "file.dat"));

        try (MockedConstruction<S3Blob> mockedBlob = mockConstruction(S3Blob.class, (mock, context) -> {
            assertEquals(path, context.arguments().get(0));
            assertEquals(BUCKET_NAME, context.arguments().get(1));
            assertEquals(KEY_PREFIX + "/my/test/file.dat", context.arguments().get(2));
            assertEquals(this.store, context.arguments().get(3));
            assertEquals(this.s3Client, context.arguments().get(4));
        })) {

            Blob blob = this.store.getBlob(path);

            assertThat(blob, instanceOf(S3Blob.class));
            assertEquals(1, mockedBlob.constructed().size());
        }
    }

    @Test
    void getBlobWithRootPath()
    {
        BlobPath rootPath = BlobPath.of(List.of("file.txt"));

        try (MockedConstruction<S3Blob> mockedBlob = mockConstruction(S3Blob.class, (mock, context) -> {
            assertEquals(rootPath, context.arguments().get(0));
            assertEquals(BUCKET_NAME, context.arguments().get(1));
            assertEquals(KEY_PREFIX + "/file.txt", context.arguments().get(2));
        })) {

            Blob blob = this.store.getBlob(rootPath);

            assertThat(blob, instanceOf(S3Blob.class));
            assertEquals(1, mockedBlob.constructed().size());
        }
    }

    @Test
    void listBlobsReturnsEmptyStream()
    {
        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> when(mock.hasNext()).thenReturn(false))) {

            Stream<Blob> blobs = this.store.listBlobs(BLOB_PATH);
            List<Blob> blobList = blobs.toList();

            assertEquals(0, blobList.size());
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void listBlobsReturnsMultipleBlobs()
    {
        List<Blob> expectedBlobs = List.of(mock(), mock(), mock());

        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> {
                // Mock forEachRemaining which is called by toList() for efficiency.
                doAnswer(invocation -> {
                    Consumer<Blob> action = invocation.getArgument(0);
                    expectedBlobs.forEach(action);
                    return null;
                }).when(mock).forEachRemaining(any());
            })) {

            Stream<Blob> blobs = this.store.listBlobs(BLOB_PATH);
            List<Blob> blobList = blobs.toList();

            // Verify the iterator was actually used.
            S3BlobIterator iterator = mockedIterator.constructed().get(0);
            verify(iterator).forEachRemaining(any());
            assertEquals(1, mockedIterator.constructed().size());

            assertEquals(expectedBlobs, blobList);
        }
    }

    @Test
    void listBlobsUsesCorrectPrefix()
    {
        BlobPath path = BlobPath.of(List.of("folder", "subfolder"));

        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> {
                when(mock.hasNext()).thenReturn(false);
                // Verify constructor arguments
                assertEquals(KEY_PREFIX + "/folder/subfolder/", context.arguments().get(0));
                assertEquals(BUCKET_NAME, context.arguments().get(1));
                assertEquals(1000, context.arguments().get(2));
                assertEquals(this.s3Client, context.arguments().get(3));
                assertEquals(this.store, context.arguments().get(4));
            })) {

            try (Stream<Blob> blobs = this.store.listBlobs(path)) {
                assertEquals(0, blobs.count());
            }

            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void listBlobsWithRootPath()
    {
        BlobPath rootPath = BlobPath.of(List.of());

        try (MockedConstruction<S3BlobIterator> ignored = mockConstruction(S3BlobIterator.class,
            (mock, context) -> when(mock.hasNext()).thenReturn(false))) {

            try (Stream<Blob> blobs = this.store.listBlobs(rootPath)) {
                assertEquals(0, blobs.count());
            }
        }
    }

    @Test
    void copyBlobInternalDelegates() throws BlobStoreException
    {
        Blob expectedBlob = mock();
        when(this.copyOperations.copyBlob(this.store, SOURCE_PATH, this.store, TARGET_PATH))
            .thenReturn(expectedBlob);

        Blob result = this.store.copyBlob(SOURCE_PATH, TARGET_PATH);

        assertEquals(expectedBlob, result);
        verify(this.copyOperations).copyBlob(this.store, SOURCE_PATH, this.store, TARGET_PATH);
    }

    @Test
    void copyBlobCrossStoreDelegates() throws BlobStoreException
    {
        BlobStore sourceStore = mock();
        Blob expectedBlob = mock();
        when(this.copyOperations.copyBlob(sourceStore, SOURCE_PATH, this.store, TARGET_PATH))
            .thenReturn(expectedBlob);

        Blob result = this.store.copyBlob(sourceStore, SOURCE_PATH, TARGET_PATH);

        assertEquals(expectedBlob, result);
        verify(this.copyOperations).copyBlob(sourceStore, SOURCE_PATH, this.store, TARGET_PATH);
    }

    @Test
    void copyBlobPropagatesException() throws BlobStoreException
    {
        BlobStoreException exception = new BlobStoreException("Copy failed");
        when(this.copyOperations.copyBlob(this.store, SOURCE_PATH, this.store, TARGET_PATH))
            .thenThrow(exception);

        BlobStoreException thrown = assertThrows(BlobStoreException.class,
            () -> this.store.copyBlob(SOURCE_PATH, TARGET_PATH));

        assertEquals(exception, thrown);
        assertEquals("Copy failed", thrown.getMessage());
    }

    @Test
    void moveBlobInternalCopiesAndDeletes() throws BlobStoreException
    {
        Blob copiedBlob = mock();
        when(this.copyOperations.copyBlob(this.store, SOURCE_PATH, this.store, TARGET_PATH))
            .thenReturn(copiedBlob);

        Blob result = this.store.moveBlob(SOURCE_PATH, TARGET_PATH);

        assertEquals(copiedBlob, result);
        verify(this.copyOperations).copyBlob(this.store, SOURCE_PATH, this.store, TARGET_PATH);
        verify(this.deleteOperations).deleteBlob(this.store, SOURCE_PATH);
    }

    @Test
    void moveBlobCrossStoreCopiesAndDeletes() throws BlobStoreException
    {
        BlobStore sourceStore = mock();
        Blob copiedBlob = mock();
        when(this.copyOperations.copyBlob(sourceStore, SOURCE_PATH, this.store, TARGET_PATH))
            .thenReturn(copiedBlob);

        Blob result = this.store.moveBlob(sourceStore, SOURCE_PATH, TARGET_PATH);

        assertEquals(copiedBlob, result);
        verify(this.copyOperations).copyBlob(sourceStore, SOURCE_PATH, this.store, TARGET_PATH);
        verify(sourceStore).deleteBlob(SOURCE_PATH);
    }

    @Test
    void moveBlobDoesNotDeleteOnCopyFailure() throws BlobStoreException
    {
        when(this.copyOperations.copyBlob(this.store, SOURCE_PATH, this.store, TARGET_PATH))
            .thenThrow(new BlobStoreException("Copy failed"));

        assertThrows(BlobStoreException.class, () -> this.store.moveBlob(SOURCE_PATH, TARGET_PATH));

        verify(this.deleteOperations, never()).deleteBlob(any(), any());
    }

    @Test
    void moveBlobCrossStoreDoesNotDeleteOnCopyFailure() throws BlobStoreException
    {
        BlobStore sourceStore = mock();
        when(this.copyOperations.copyBlob(sourceStore, SOURCE_PATH, this.store, TARGET_PATH))
            .thenThrow(new BlobStoreException("Copy failed"));

        assertThrows(BlobStoreException.class, () -> this.store.moveBlob(sourceStore, SOURCE_PATH, TARGET_PATH));

        verify(sourceStore, never()).deleteBlob(any());
    }

    @Test
    void isEmptyDirectoryWhenEmpty() throws BlobStoreException
    {
        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> {
                when(mock.hasNext()).thenReturn(false);
                // Verify page size is 1 for efficiency
                assertEquals(1, context.arguments().get(2));
            })) {

            boolean result = this.store.isEmptyDirectory(BLOB_PATH);

            assertTrue(result);
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void isEmptyDirectoryWhenNotEmpty() throws BlobStoreException
    {
        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> when(mock.hasNext()).thenReturn(true))) {

            boolean result = this.store.isEmptyDirectory(BLOB_PATH);

            assertFalse(result);
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void isEmptyDirectoryWrapsRuntimeException()
    {
        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> when(mock.hasNext()).thenThrow(new RuntimeException("S3 connection failed")))) {

            BlobPath path = BlobPath.of(List.of("error", "path"));

            BlobStoreException thrown = assertThrows(BlobStoreException.class,
                () -> this.store.isEmptyDirectory(path));

            assertThat(thrown.getMessage(), containsString("Failed to check if directory is empty"));
            assertThat(thrown.getMessage(), containsString("error/path"));
            assertEquals(RuntimeException.class, thrown.getCause().getClass());
            assertEquals("S3 connection failed", thrown.getCause().getMessage());
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void isEmptyDirectoryWrapsIllegalStateException()
    {
        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> when(mock.hasNext()).thenThrow(new IllegalStateException("Invalid state")))) {

            BlobStoreException thrown = assertThrows(BlobStoreException.class,
                () -> this.store.isEmptyDirectory(BLOB_PATH));

            assertThat(thrown.getMessage(), containsString("Failed to check if directory is empty"));
            assertEquals(IllegalStateException.class, thrown.getCause().getClass());
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void deleteBlobDelegates() throws BlobStoreException
    {
        this.store.deleteBlob(BLOB_PATH);

        verify(this.deleteOperations).deleteBlob(this.store, BLOB_PATH);
    }

    @Test
    void deleteBlobPropagatesException() throws BlobStoreException
    {
        BlobStoreException exception = new BlobStoreException("Delete failed");
        doThrow(exception).when(this.deleteOperations).deleteBlob(this.store, BLOB_PATH);

        BlobStoreException thrown = assertThrows(BlobStoreException.class,
            () -> this.store.deleteBlob(BLOB_PATH));

        assertEquals(exception, thrown);
        assertEquals("Delete failed", thrown.getMessage());
    }

    @Test
    void deleteBlobsListsAndDeletes() throws BlobStoreException
    {
        Blob blob1 = mock();
        Blob blob2 = mock();

        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> {
                when(mock.hasNext()).thenReturn(true, true, false);
                when(mock.next()).thenReturn(blob1, blob2);
            })) {

            this.store.deleteBlobs(BLOB_PATH);

            verify(this.deleteOperations).deleteBlobs(eq(this.store), any());
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void deleteBlobsClosesStream() throws BlobStoreException
    {
        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> when(mock.hasNext()).thenReturn(false))) {

            this.store.deleteBlobs(BLOB_PATH);

            // Verify that the stream is properly closed by using try-with-resources
            // If the stream wasn't closed, this would potentially leak resources
            verify(this.deleteOperations).deleteBlobs(eq(this.store), any());
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void deleteBlobsWithEmptyDirectory() throws BlobStoreException
    {
        try (MockedConstruction<S3BlobIterator> mockedIterator = mockConstruction(S3BlobIterator.class,
            (mock, context) -> when(mock.hasNext()).thenReturn(false))) {

            this.store.deleteBlobs(BLOB_PATH);

            verify(this.deleteOperations).deleteBlobs(eq(this.store), any());
            assertEquals(1, mockedIterator.constructed().size());
        }
    }

    @Test
    void equalsAndHashCodeWithSameInstance()
    {
        assertEquals(this.store, this.store);
        assertEquals(this.store.hashCode(), this.store.hashCode());
    }

    @ParameterizedTest
    @CsvSource({
        "test-store, test-bucket, prefix, true",
        "test-store, different-bucket, prefix, false",
        "test-store, test-bucket, different-prefix, false",
        // Different name but same other properties should still be equal.
        "different-name, test-bucket, prefix, true"
    })
    void equalsAndHashCode(String otherStoreName, String otherBucketName, String otherKeyPrefix,
        boolean expectedEquality)
    {
        S3BlobStore other = new S3BlobStore();
        other.initialize(otherStoreName, getS3BlobStoreProperties(otherBucketName, otherKeyPrefix));

        if (expectedEquality) {
            assertEquals(this.store, other);
            assertEquals(this.store.hashCode(), other.hashCode());
        } else {
            assertNotEquals(this.store, other);
            assertNotEquals(this.store.hashCode(), other.hashCode());
        }
    }

    @Test
    void equalsWithNull()
    {
        assertNotEquals(null, this.store);
    }

    @Test
    void equalsWithDifferentClass()
    {
        assertNotEquals("not a store", this.store);
    }

    @Test
    void getBucketNameReturnsCorrectValue()
    {
        assertEquals(BUCKET_NAME, this.store.getBucketName());
    }

    @Test
    void getKeyMapperReturnsCorrectValue()
    {
        S3KeyMapper mapper = this.store.getKeyMapper();

        assertEquals(KEY_PREFIX, mapper.getKeyPrefix());
    }

    @Test
    void getNameReturnsCorrectValue()
    {
        assertEquals(STORE_NAME, this.store.getName());
    }
}
