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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3DeleteOperations}.
 *
 * @version $Id$
 */
@ComponentTest
class S3DeleteOperationsTest
{
    private static final String TEST_BUCKET = "test-bucket";

    private static final String TEST_KEY = "test-key";

    private static final BlobPath TEST_BLOB_PATH = BlobPath.absolute("test-path.txt");

    @InjectMockComponents
    private S3DeleteOperations deleteOperations;

    @Mock
    private S3Client s3Client;

    @MockComponent
    private S3ClientManager s3ClientManager;

    @Mock
    private S3BlobStore blobStore;

    @Mock
    private S3KeyMapper keyMapper;

    @BeforeEach
    void setup()
    {
        when(this.s3ClientManager.getS3Client()).thenReturn(this.s3Client);
        when(this.blobStore.getBucketName()).thenReturn(TEST_BUCKET);
        when(this.blobStore.getKeyMapper()).thenReturn(this.keyMapper);
        when(this.keyMapper.buildS3Key(TEST_BLOB_PATH)).thenReturn(TEST_KEY);
    }

    @Test
    void deleteBlobSuccessfully() throws BlobStoreException
    {
        DeleteObjectResponse response = mock();
        when(this.s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(response);

        this.deleteOperations.deleteBlob(this.blobStore, TEST_BLOB_PATH);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(this.s3Client).deleteObject(captor.capture());
        DeleteObjectRequest request = captor.getValue();
        assertEquals(TEST_BUCKET, request.bucket());
        assertEquals(TEST_KEY, request.key());
    }

    @Test
    void deleteBlobWithS3Exception()
    {
        AwsServiceException exception = S3Exception.builder()
            .message("Access Denied")
            .statusCode(403)
            .build();
        when(this.s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(exception);

        BlobStoreException thrown = assertThrows(BlobStoreException.class,
            () -> this.deleteOperations.deleteBlob(this.blobStore, TEST_BLOB_PATH));

        assertTrue(thrown.getMessage().contains("Failed to delete blob"));
        assertTrue(thrown.getMessage().contains(TEST_BLOB_PATH.toString()));
        assertEquals(exception, thrown.getCause());
    }

    @Test
    void deleteBlobsWithEmptyStream() throws BlobStoreException
    {
        Stream<Blob> emptyStream = Stream.empty();

        this.deleteOperations.deleteBlobs(this.blobStore, emptyStream);

        // Verify no S3 calls were made
        verify(this.s3Client, times(0)).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void deleteBlobsWithSingleBlob() throws BlobStoreException
    {
        Blob blob = mock();
        BlobPath path = BlobPath.absolute("single.txt");
        when(blob.getPath()).thenReturn(path);
        when(this.keyMapper.buildS3Key(path)).thenReturn("single-key");

        DeleteObjectsResponse response = mock();
        when(response.errors()).thenReturn(Collections.emptyList());
        when(this.s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(response);

        this.deleteOperations.deleteBlobs(this.blobStore, Stream.of(blob));

        ArgumentCaptor<DeleteObjectsRequest> captor = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(this.s3Client).deleteObjects(captor.capture());

        DeleteObjectsRequest request = captor.getValue();
        assertEquals(TEST_BUCKET, request.bucket());
        assertEquals(1, request.delete().objects().size());
        assertEquals("single-key", request.delete().objects().get(0).key());
    }

    @Test
    void deleteBlobsWithMultipleBlobs() throws BlobStoreException
    {
        List<Blob> blobs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Blob blob = mock();
            BlobPath path = BlobPath.absolute("file" + i + ".txt");
            when(blob.getPath()).thenReturn(path);
            when(this.keyMapper.buildS3Key(path)).thenReturn("key" + i);
            blobs.add(blob);
        }

        DeleteObjectsResponse response = mock();
        when(response.errors()).thenReturn(Collections.emptyList());
        when(this.s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(response);

        this.deleteOperations.deleteBlobs(this.blobStore, blobs.stream());

        ArgumentCaptor<DeleteObjectsRequest> captor = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(this.s3Client).deleteObjects(captor.capture());

        DeleteObjectsRequest request = captor.getValue();
        assertEquals(TEST_BUCKET, request.bucket());
        assertEquals(5, request.delete().objects().size());
    }

    @Test
    void deleteBlobsWithExactly1000Blobs() throws BlobStoreException
    {
        // Setup - exactly at batch limit
        List<Blob> blobs = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Blob blob = mock();
            BlobPath path = BlobPath.absolute("file" + i + ".txt");
            when(blob.getPath()).thenReturn(path);
            when(this.keyMapper.buildS3Key(path)).thenReturn("key" + i);
            blobs.add(blob);
        }

        DeleteObjectsResponse response = mock();
        when(response.errors()).thenReturn(Collections.emptyList());
        when(this.s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(response);

        this.deleteOperations.deleteBlobs(this.blobStore, blobs.stream());

        verify(this.s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void deleteBlobsWithMoreThan1000Blobs() throws BlobStoreException
    {
        // Setup - more than batch limit, should trigger multiple batches
        List<Blob> blobs = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            Blob blob = mock();
            BlobPath path = BlobPath.absolute("file" + i + ".txt");
            when(blob.getPath()).thenReturn(path);
            when(this.keyMapper.buildS3Key(path)).thenReturn("key" + i);
            blobs.add(blob);
        }

        DeleteObjectsResponse response = mock();
        when(response.errors()).thenReturn(Collections.emptyList());
        when(this.s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(response);

        this.deleteOperations.deleteBlobs(this.blobStore, blobs.stream());

        // Verify - should be 3 batches (1000 + 1000 + 500)
        verify(this.s3Client, times(3)).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void deleteBlobsWithMoreThan1000BlobsAndSomeErrorsInFirstPart()
    {
        // Setup - more than batch limit, should trigger multiple batches
        List<Blob> blobs = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            Blob blob = mock();
            BlobPath path = BlobPath.absolute("file" + i + ".txt");
            when(blob.getPath()).thenReturn(path);
            when(this.keyMapper.buildS3Key(path)).thenReturn("key" + i);
            blobs.add(blob);
        }

        S3Error error = S3Error.builder()
            .key("key500")
            .code("AccessDenied")
            .message("Access Denied")
            .build();

        DeleteObjectsResponse firstResponse = DeleteObjectsResponse.builder()
            .errors(Collections.singletonList(error))
            .build();
        DeleteObjectsResponse secondResponse = DeleteObjectsResponse.builder()
            .errors(Collections.emptyList())
            .build();
        when(this.s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
            .thenReturn(firstResponse)
            .thenReturn(secondResponse);

        // Execute & Verify
        BlobStoreException thrown = assertThrows(BlobStoreException.class,
            () -> this.deleteOperations.deleteBlobs(this.blobStore, blobs.stream()));

        assertThat(thrown.getMessage(), containsString("Failed to delete some blobs"));

        // Verify - should be 2 batches (1000 + 500)
        verify(this.s3Client, times(2)).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void deleteBlobsWithPartialErrors()
    {
        // Setup
        Blob blob1 = mock();
        Blob blob2 = mock();
        BlobPath path1 = BlobPath.absolute("file1.txt");
        BlobPath path2 = BlobPath.absolute("file2.txt");
        when(blob1.getPath()).thenReturn(path1);
        when(blob2.getPath()).thenReturn(path2);
        when(this.keyMapper.buildS3Key(path1)).thenReturn("key1");
        when(this.keyMapper.buildS3Key(path2)).thenReturn("key2");

        S3Error error = S3Error.builder()
            .key("key2")
            .code("AccessDenied")
            .message("Access Denied")
            .build();

        DeleteObjectsResponse response = DeleteObjectsResponse.builder()
            .errors(Collections.singletonList(error))
            .build();
        when(this.s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(response);

        // Execute & Verify
        BlobStoreException thrown = assertThrows(BlobStoreException.class,
            () -> this.deleteOperations.deleteBlobs(this.blobStore, Stream.of(blob1, blob2)));

        assertThat(thrown.getMessage(), containsString("Failed to delete some blobs"));
    }

    @Test
    void deleteBlobsWithS3Exception()
    {
        // Setup
        Blob blob = mock();
        BlobPath path = BlobPath.absolute("file.txt");
        when(blob.getPath()).thenReturn(path);
        when(this.keyMapper.buildS3Key(path)).thenReturn("key");

        AwsServiceException exception = S3Exception.builder()
            .message("Service Unavailable")
            .statusCode(503)
            .build();
        when(this.s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenThrow(exception);

        // Execute & Verify
        BlobStoreException thrown = assertThrows(BlobStoreException.class,
            () -> this.deleteOperations.deleteBlobs(this.blobStore, Stream.of(blob)));

        assertThat(thrown.getMessage(), containsString("Failed to batch delete blobs"));
        assertEquals(exception, thrown.getCause());
    }
}
