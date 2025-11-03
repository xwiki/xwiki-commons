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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobDoesNotExistOption;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyPartResult;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.UploadPartCopyRequest;
import software.amazon.awssdk.services.s3.model.UploadPartCopyResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3CopyOperations}.
 *
 * @version $Id$
 */
@ComponentTest
class S3CopyOperationsTest
{
    @InjectMockComponents
    private S3CopyOperations copyOperations;

    @MockComponent
    private S3ClientManager clientManager;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.DEBUG);

    @Mock
    private S3BlobStore sourceStore;

    @Mock
    private S3BlobStore targetStore;

    @Mock
    private BlobPath sourcePath;

    @Mock
    private BlobPath targetPath;

    @Mock
    private S3KeyMapper keyMapper;

    @Mock
    private S3Client s3Client;

    @Mock
    private Blob sourceBlob;

    @Mock
    private Blob targetBlob;

    @BeforeEach
    void setUp()
    {
        when(this.sourceStore.getKeyMapper()).thenReturn(this.keyMapper);
        when(this.targetStore.getKeyMapper()).thenReturn(this.keyMapper);
        when(this.keyMapper.buildS3Key(this.sourcePath)).thenReturn("source-key");
        when(this.keyMapper.buildS3Key(this.targetPath)).thenReturn("target-key");
        when(this.sourceStore.getBucketName()).thenReturn("source-bucket");
        when(this.targetStore.getBucketName()).thenReturn("target-bucket");
        when(this.sourceStore.getBlob(this.sourcePath)).thenReturn(this.sourceBlob);
        when(this.targetStore.getBlob(this.targetPath)).thenReturn(this.targetBlob);
        when(this.clientManager.getS3Client()).thenReturn(this.s3Client);

        // Default copy size: 512MB (512 * 1024 * 1024 bytes)
        when(this.targetStore.getMultipartPartCopySizeBytes()).thenReturn(512 * 1024L * 1024L);
    }

    @Test
    void copyBlobThrowsExceptionWhenSourceAndTargetAreSame()
    {
        BlobStore store = mock();
        BlobPath path = mock();

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.copyOperations.copyBlob(store, path, store, path));

        assertEquals("Source and target blob are the same: " + path, exception.getMessage());
    }

    @Test
    void copyBlobWithStreamWhenStoresAreNotS3() throws Exception
    {
        BlobStore nonS3SourceStore = mock();
        BlobStore nonS3TargetStore = mock();
        BlobPath nonS3SourcePath = mock();
        BlobPath nonS3TargetPath = mock();

        Blob nonS3SourceBlob = mock();
        Blob nonS3TargetBlob = mock();
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        when(nonS3SourceStore.getBlob(nonS3SourcePath)).thenReturn(nonS3SourceBlob);
        when(nonS3SourceBlob.getStream()).thenReturn(inputStream);
        when(nonS3TargetStore.getBlob(nonS3TargetPath)).thenReturn(nonS3TargetBlob);
        when(nonS3TargetBlob.exists()).thenReturn(false);

        Blob result = this.copyOperations.copyBlob(nonS3SourceStore, nonS3SourcePath, nonS3TargetStore,
            nonS3TargetPath);

        assertNotNull(result);
        assertEquals(nonS3TargetBlob, result);
        verify(nonS3TargetBlob).writeFromStream(any(InputStream.class), any(BlobDoesNotExistOption.class));
    }

    @Test
    void copyBlobWithStreamThrowsExceptionWhenTargetExists() throws Exception
    {
        BlobStore nonS3SourceStore = mock();
        BlobStore nonS3TargetStore = mock();
        BlobPath nonS3SourcePath = mock();
        BlobPath nonS3TargetPath = mock();

        Blob nonS3SourceBlob = mock();
        Blob nonS3TargetBlob = mock();
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        when(nonS3SourceStore.getBlob(nonS3SourcePath)).thenReturn(nonS3SourceBlob);
        when(nonS3SourceBlob.getStream()).thenReturn(inputStream);
        when(nonS3TargetStore.getBlob(nonS3TargetPath)).thenReturn(nonS3TargetBlob);
        when(nonS3TargetBlob.exists()).thenReturn(true);

        assertThrows(BlobAlreadyExistsException.class,
            () -> this.copyOperations.copyBlob(nonS3SourceStore, nonS3SourcePath, nonS3TargetStore,
                nonS3TargetPath));

        verify(nonS3TargetBlob, never()).writeFromStream(any(), any());
    }

    @Test
    void copyBlobWithStreamWrapsNonBlobStoreExceptions() throws Exception
    {
        BlobStore nonS3SourceStore = mock();
        BlobStore nonS3TargetStore = mock();
        BlobPath nonS3SourcePath = mock();
        BlobPath nonS3TargetPath = mock();

        Blob nonS3SourceBlob = mock();

        when(nonS3SourceStore.getBlob(nonS3SourcePath)).thenReturn(nonS3SourceBlob);
        when(nonS3SourceBlob.getStream()).thenThrow(new RuntimeException("IO error"));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.copyOperations.copyBlob(nonS3SourceStore, nonS3SourcePath, nonS3TargetStore,
                nonS3TargetPath));

        assertEquals("Failed to copy blob from external store", exception.getMessage());
    }

    @Test
    void copyBlobS3StoreSimpleCopyForSmallObject() throws Exception
    {
        when(this.targetBlob.exists()).thenReturn(false);
        when(this.sourceBlob.getSize()).thenReturn(1024L);
        when(this.s3Client.copyObject(any(CopyObjectRequest.class))).thenReturn(mock());

        Blob result = this.copyOperations.copyBlob(this.sourceStore, this.sourcePath, this.targetStore,
            this.targetPath);

        assertNotNull(result);
        ArgumentCaptor<CopyObjectRequest> requestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).copyObject(requestCaptor.capture());

        CopyObjectRequest request = requestCaptor.getValue();
        assertEquals("source-bucket", request.sourceBucket());
        assertEquals("source-key", request.sourceKey());
        assertEquals("target-bucket", request.destinationBucket());
        assertEquals("target-key", request.destinationKey());
        assertEquals("COPY", request.metadataDirective().toString());
    }

    @Test
    void copyBlobS3StoreThrowsExceptionWhenTargetExists() throws Exception
    {
        when(this.targetBlob.exists()).thenReturn(true);

        assertThrows(BlobAlreadyExistsException.class,
            () -> this.copyOperations.copyBlob(this.sourceStore, this.sourcePath, this.targetStore, this.targetPath));
    }

    @Test
    void copyBlobS3StoreThrowsExceptionWhenSourceNotFound() throws Exception
    {
        when(this.targetBlob.exists()).thenReturn(false);
        when(this.sourceBlob.getSize()).thenReturn(-1L);

        assertThrows(BlobNotFoundException.class,
            () -> this.copyOperations.copyBlob(this.sourceStore, this.sourcePath, this.targetStore, this.targetPath));
    }

    @Test
    void copyBlobS3StoreMultipartCopyForLargeObject() throws Exception
    {
        // Use 600 MiB so that with a 512 MiB copy part size we get 2 parts
        long largeObjectSize = 600L * 1024 * 1024;

        when(this.targetBlob.exists()).thenReturn(false);
        when(this.sourceBlob.getSize()).thenReturn(largeObjectSize);

        // Mock HeadObjectResponse with metadata
        HeadObjectResponse headResponse = mock();
        Map<String, String> metadata = Map.of("content-type", "application/octet-stream", "custom-key", "custom-value");
        when(headResponse.metadata()).thenReturn(metadata);
        when(this.s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headResponse);

        CreateMultipartUploadResponse createResponse = mock();
        when(createResponse.uploadId()).thenReturn("test-upload-id");
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        UploadPartCopyResponse part1Response = mock();
        CopyPartResult part1Result = mock();
        when(part1Result.eTag()).thenReturn("etag1");
        when(part1Response.copyPartResult()).thenReturn(part1Result);

        UploadPartCopyResponse part2Response = mock();
        CopyPartResult part2Result = mock();
        when(part2Result.eTag()).thenReturn("etag2");
        when(part2Response.copyPartResult()).thenReturn(part2Result);

        when(this.s3Client.uploadPartCopy(any(UploadPartCopyRequest.class)))
            .thenReturn(part1Response, part2Response);

        when(this.s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
            .thenReturn(mock());

        Blob result = this.copyOperations.copyBlob(this.sourceStore, this.sourcePath, this.targetStore,
            this.targetPath);

        assertNotNull(result);

        // Verify HeadObject was called to retrieve metadata
        ArgumentCaptor<HeadObjectRequest> headRequestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).headObject(headRequestCaptor.capture());
        HeadObjectRequest headRequest = headRequestCaptor.getValue();
        assertEquals("source-bucket", headRequest.bucket());
        assertEquals("source-key", headRequest.key());

        // Verify CreateMultipartUpload was called with metadata
        ArgumentCaptor<CreateMultipartUploadRequest> createRequestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).createMultipartUpload(createRequestCaptor.capture());
        CreateMultipartUploadRequest createRequest = createRequestCaptor.getValue();
        assertEquals(metadata, createRequest.metadata());

        verify(this.s3Client, times(2)).uploadPartCopy(any(UploadPartCopyRequest.class));
        verify(this.s3Client).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));

        // Log messages from S3MultipartUploadHelper constructor
        assertEquals("Initialized multipart upload for key target-key with upload ID: test-upload-id",
            this.logCapture.getMessage(0));

        // Log message from S3CopyOperations
        assertEquals("Initiated multipart copy with upload ID: test-upload-id", this.logCapture.getMessage(1));

        // Log messages from S3MultipartUploadHelper.addCompletedPart
        assertEquals("Added completed part 1 for upload ID: test-upload-id", this.logCapture.getMessage(2));

        // Log message from S3CopyOperations
        assertEquals("Copied part 1 (bytes 0-536870911)", this.logCapture.getMessage(3));

        // Log messages from S3MultipartUploadHelper.addCompletedPart
        assertEquals("Added completed part 2 for upload ID: test-upload-id", this.logCapture.getMessage(4));

        // Log message from S3CopyOperations
        assertEquals("Copied part 2 (bytes 536870912-629145599)", this.logCapture.getMessage(5));

        // Log messages from S3MultipartUploadHelper.complete
        assertEquals("Completed multipart upload for key target-key with upload ID: test-upload-id",
            this.logCapture.getMessage(6));

        // Log message from S3CopyOperations
        assertEquals("Completed multipart copy for key: target-key", this.logCapture.getMessage(7));
    }

    @Test
    void copyBlobS3StoreMultipartCopyAbortsOnFailure() throws Exception
    {
        // Use 600 MiB so that with a 512 MiB copy part size we get 2 parts
        long largeObjectSize = 600L * 1024 * 1024;

        when(this.targetBlob.exists()).thenReturn(false);
        when(this.sourceBlob.getSize()).thenReturn(largeObjectSize);

        // Mock HeadObjectResponse with metadata
        HeadObjectResponse headResponse = mock();
        when(headResponse.metadata()).thenReturn(Map.of());
        when(this.s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headResponse);

        CreateMultipartUploadResponse createResponse = mock();
        when(createResponse.uploadId()).thenReturn("test-upload-id");
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        when(this.s3Client.uploadPartCopy(any(UploadPartCopyRequest.class)))
            .thenThrow(new RuntimeException("Upload failed"));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.copyOperations.copyBlob(this.sourceStore, this.sourcePath, this.targetStore, this.targetPath));

        assertEquals("Failed to perform multipart copy", exception.getMessage());

        // Log messages from S3MultipartUploadHelper constructor
        assertEquals("Initialized multipart upload for key target-key with upload ID: test-upload-id",
            this.logCapture.getMessage(0));

        // Log message from S3CopyOperations
        assertEquals("Initiated multipart copy with upload ID: test-upload-id", this.logCapture.getMessage(1));

        // Log message from S3MultipartUploadHelper.abort
        assertEquals("Aborted multipart upload for key target-key with upload ID: test-upload-id",
            this.logCapture.getMessage(2));
    }

    @Test
    void copyBlobS3StoreSimpleCopyThrowsExceptionOnFailure() throws Exception
    {
        when(this.targetBlob.exists()).thenReturn(false);
        when(this.sourceBlob.getSize()).thenReturn(1024L);
        when(this.s3Client.copyObject(any(CopyObjectRequest.class)))
            .thenThrow(new RuntimeException("Copy failed"));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.copyOperations.copyBlob(this.sourceStore, this.sourcePath, this.targetStore, this.targetPath));

        assertEquals("Failed to perform simple copy", exception.getMessage());
    }

    @Test
    void copyBlobS3StoreAtExactThreshold() throws Exception
    {
        long exactThreshold = 512L * 1024 * 1024;

        when(this.targetBlob.exists()).thenReturn(false);
        when(this.sourceBlob.getSize()).thenReturn(exactThreshold);
        when(this.s3Client.copyObject(any(CopyObjectRequest.class))).thenReturn(mock());

        this.copyOperations.copyBlob(this.sourceStore, this.sourcePath, this.targetStore, this.targetPath);

        // Should use simple copy at threshold.
        verify(this.s3Client).copyObject(any(CopyObjectRequest.class));
        verify(this.s3Client, never()).uploadPartCopy(any(UploadPartCopyRequest.class));
    }
}
