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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobWriteMode;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3BlobOutputStream}.
 *
 * @version $Id$
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
@ExtendWith(MockitoExtension.class)
class S3BlobOutputStreamTest
{
    private static final String BUCKET_NAME = "test-bucket";

    private static final String S3_KEY = "test-key";

    private static final String UPLOAD_ID = "test-upload-id";

    private static final String ETAG = "test-etag";

    // 5MB
    private static final int PART_SIZE = 5 * 1024 * 1024;

    @Mock
    private S3Client s3Client;

    @Mock
    private BlobPath blobPath;

    private final List<byte[]> capturedUploadPartData = new ArrayList<>();

    private byte[] capturedPutObjectData;

    @BeforeEach
    void setUp()
    {
        this.capturedUploadPartData.clear();
        this.capturedPutObjectData = null;

        lenient().when(this.s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenAnswer(invocation -> {
                assertNull(this.capturedPutObjectData, "putObject called multiple times");
                RequestBody body = invocation.getArgument(1);
                try (InputStream stream = body.contentStreamProvider().newStream()) {
                    this.capturedPutObjectData = stream.readAllBytes();
                }
                return null;
            });

        lenient().when(this.s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
            .thenAnswer(this::mockedUploadPart);
    }

    private UploadPartResponse mockedUploadPart(InvocationOnMock invocation) throws IOException
    {
        RequestBody body = invocation.getArgument(1);
        try (InputStream stream = body.contentStreamProvider().newStream()) {
            this.capturedUploadPartData.add(stream.readAllBytes());
        }
        return UploadPartResponse.builder().eTag(ETAG).build();
    }

    /**
     * Helper method to fill a byte array with deterministic but non-repeating data.
     * Uses a seeded Random to create a pseudo-random pattern that is reproducible
     * across test runs but doesn't repeat within reasonable test data sizes.
     *
     * @param data the byte array to fill
     */
    private void fillArray(byte[] data)
    {
        Random random = new Random(0x123456789ABCDEFL);
        random.nextBytes(data);
    }

    @Test
    void writeSmallFileWithSimpleUpload() throws IOException
    {
        // Test simple upload for files smaller than the multipart threshold
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = "Hello, World!".getBytes();
        outputStream.write(data);
        outputStream.close();

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        verify(this.s3Client, never()).createMultipartUpload(any(CreateMultipartUploadRequest.class));

        PutObjectRequest request = requestCaptor.getValue();
        assertEquals(BUCKET_NAME, request.bucket());
        assertEquals(S3_KEY, request.key());
        assertNull(request.ifMatch());
        assertNull(request.ifNoneMatch());

        assertArrayEquals(data, this.capturedPutObjectData);
    }

    @Test
    void writeLargeFileWithMultipartUpload() throws IOException
    {
        // Test multipart upload for files larger than the threshold
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        // Write more than one part of data.
        byte[] data = new byte[PART_SIZE + 1000];
        fillArray(data);
        outputStream.write(data);
        outputStream.close();

        verify(this.s3Client).createMultipartUpload(any(CreateMultipartUploadRequest.class));
        verify(this.s3Client, times(2)).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
        ArgumentCaptor<CompleteMultipartUploadRequest> completeCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).completeMultipartUpload(completeCaptor.capture());

        CompleteMultipartUploadRequest completeRequest = completeCaptor.getValue();
        assertEquals(BUCKET_NAME, completeRequest.bucket());
        assertEquals(S3_KEY, completeRequest.key());
        assertEquals(UPLOAD_ID, completeRequest.uploadId());
        assertNull(completeRequest.ifMatch());
        assertNull(completeRequest.ifNoneMatch());

        assertEquals(2, this.capturedUploadPartData.size());
        assertArrayEquals(Arrays.copyOfRange(data, 0, PART_SIZE), this.capturedUploadPartData.get(0));
        assertArrayEquals(Arrays.copyOfRange(data, PART_SIZE, data.length), this.capturedUploadPartData.get(1));
    }

    @Test
    void writeMultiplePartsWithoutRemainder() throws IOException
    {
        // Test multipart upload with 3 full parts
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        // 3 full parts
        byte[] data = new byte[PART_SIZE * 3];
        fillArray(data);
        outputStream.write(data);
        outputStream.close();

        verify(this.s3Client).createMultipartUpload(any(CreateMultipartUploadRequest.class));
        verify(this.s3Client, times(3)).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
        verify(this.s3Client).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));

        assertEquals(3, this.capturedUploadPartData.size());
        assertArrayEquals(Arrays.copyOfRange(data, 0, PART_SIZE), this.capturedUploadPartData.get(0));
        assertArrayEquals(Arrays.copyOfRange(data, PART_SIZE, PART_SIZE * 2), this.capturedUploadPartData.get(1));
        assertArrayEquals(Arrays.copyOfRange(data, PART_SIZE * 2, PART_SIZE * 3), this.capturedUploadPartData.get(2));
    }

    @Test
    void writeSingleByte() throws IOException
    {
        // Test writing single bytes
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        // 'A'
        outputStream.write(65);
        // 'B'
        outputStream.write(66);
        outputStream.close();

        verify(this.s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertArrayEquals(new byte[]{65, 66}, this.capturedPutObjectData);
    }

    @Test
    void writeWithOffset() throws IOException
    {
        // Test writing with offset and length parameters
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = "Hello, World!".getBytes();
        // Write "World"
        outputStream.write(data, 7, 5);
        outputStream.close();

        verify(this.s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertArrayEquals("World".getBytes(), this.capturedPutObjectData);
    }

    @Test
    void writeEmptyStream() throws IOException
    {
        // Test closing an empty stream
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        outputStream.close();

        verify(this.s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verifyNoMoreInteractions(this.s3Client);

        assertArrayEquals(new byte[0], this.capturedPutObjectData);
    }

    @Test
    void writeWithCreateNewMode() throws IOException
    {
        // Test conditional write with CREATE_NEW mode
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE, BlobWriteMode.CREATE_NEW);

        byte[] data = "Test data".getBytes();
        outputStream.write(data);
        outputStream.close();

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest request = requestCaptor.getValue();
        assertEquals("*", request.ifNoneMatch());

        assertArrayEquals(data, this.capturedPutObjectData);
    }

    @Test
    void writeWithCreateNewModeMultipart() throws IOException
    {
        // Test conditional write with multipart upload
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE, BlobWriteMode.CREATE_NEW);

        byte[] data = new byte[PART_SIZE + 1000];
        fillArray(data);
        outputStream.write(data);
        outputStream.close();

        ArgumentCaptor<CompleteMultipartUploadRequest> requestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).completeMultipartUpload(requestCaptor.capture());

        CompleteMultipartUploadRequest request = requestCaptor.getValue();
        assertEquals("*", request.ifNoneMatch());

        assertEquals(2, this.capturedUploadPartData.size());
        assertArrayEquals(Arrays.copyOfRange(data, 0, PART_SIZE), this.capturedUploadPartData.get(0));
        assertArrayEquals(Arrays.copyOfRange(data, PART_SIZE, data.length), this.capturedUploadPartData.get(1));
    }

    @Test
    void writeBlobAlreadyExistsExceptionSimpleUpload() throws IOException
    {
        // Test write condition failure for simple upload
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .statusCode(412)
            .message("Precondition Failed")
            .build();
        when(this.s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(s3Exception);

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE, BlobWriteMode.CREATE_NEW);

        byte[] data = "Test data".getBytes();
        outputStream.write(data);

        IOException exception = assertThrows(IOException.class, outputStream::close);
        assertThat(exception.getMessage(), containsString("Blob already exists"));
        assertInstanceOf(BlobAlreadyExistsException.class, exception.getCause());
        assertEquals(this.blobPath, ((BlobAlreadyExistsException) exception.getCause()).getBlobPath());

        // Verify that the condition was actually checked.
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertEquals("*", request.ifNoneMatch());
    }

    @Test
    void writeBlobAlreadyExistsExceptionMultipartUpload() throws IOException
    {
        // Test write condition failure for multipart upload
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .statusCode(412)
            .message("Precondition Failed")
            .build();
        when(this.s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
            .thenThrow(s3Exception);

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE, BlobWriteMode.CREATE_NEW);

        byte[] data = new byte[PART_SIZE + 1000];
        outputStream.write(data);

        IOException exception = assertThrows(IOException.class, outputStream::close);
        assertThat(exception.getMessage(), containsString("Blob already exists"));
        assertInstanceOf(BlobAlreadyExistsException.class, exception.getCause());
        assertEquals(this.blobPath, ((BlobAlreadyExistsException) exception.getCause()).getBlobPath());

        // Verify that the condition was actually checked.
        ArgumentCaptor<CompleteMultipartUploadRequest> requestCaptor = ArgumentCaptor.captor();
        verify(this.s3Client).completeMultipartUpload(requestCaptor.capture());
        CompleteMultipartUploadRequest request = requestCaptor.getValue();
        assertEquals("*", request.ifNoneMatch());
    }

    @Test
    void multipartUploadAbortedOnError()
    {
        // Test that multipart upload is aborted when an error occurs
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        when(this.s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("Upload failed"));

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = new byte[PART_SIZE + 1000];
        assertThrows(IOException.class, () -> outputStream.write(data));

        verify(this.s3Client).abortMultipartUpload(any(AbortMultipartUploadRequest.class));

        assertDoesNotThrow(outputStream::close);
    }

    @Test
    void multipartUploadNotAbortedIfCreateFails()
    {
        // Test that multipart upload is not aborted if creation fails
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenThrow(new RuntimeException("Creation failed"));

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = new byte[PART_SIZE + 1000];
        assertThrows(IOException.class, () -> outputStream.write(data));

        verify(this.s3Client, never()).abortMultipartUpload(any(AbortMultipartUploadRequest.class));
    }

    @Test
    void multipartUploadAbortedWhenSecondWriteFails() throws IOException
    {
        // Test that multipart upload is aborted when the second write fails
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        when(this.s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
            .thenAnswer(this::mockedUploadPart)
            .thenThrow(new RuntimeException("Second upload failed"));

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        // First write should succeed.
        byte[] firstPart = new byte[PART_SIZE + 1000];
        outputStream.write(firstPart);

        // Close should fail due to second upload failure.
        assertThrows(IOException.class, outputStream::close);

        verify(this.s3Client).abortMultipartUpload(any(AbortMultipartUploadRequest.class));
    }

    @Test
    void writeAfterCloseThrowsException() throws IOException
    {
        // Test that writing after close throws an exception
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        outputStream.close();

        assertThrows(IOException.class, () -> outputStream.write(65));
        assertThrows(IOException.class, () -> outputStream.write(new byte[10]));
        assertThrows(IOException.class, () -> outputStream.write(new byte[10], 0, 5));
    }

    @Test
    void flushDoesNothing() throws IOException
    {
        // Test that flush doesn't trigger any S3 operations
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = "Test data".getBytes();
        outputStream.write(data);
        outputStream.flush();

        verify(this.s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(this.s3Client, never()).createMultipartUpload(any(CreateMultipartUploadRequest.class));

        outputStream.close();

        assertArrayEquals(data, this.capturedPutObjectData);
    }

    @Test
    void closeMultipleTimesIsSafe() throws IOException
    {
        // Test that closing multiple times doesn't cause issues
        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = "Test data".getBytes();
        outputStream.write(data);
        outputStream.close();
        // Second close should be safe
        outputStream.close();

        verify(this.s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertArrayEquals(data, this.capturedPutObjectData);
    }

    @Test
    void writeExactlyAtPartSizeBoundary() throws IOException
    {
        // Test writing exactly PART_SIZE bytes
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = new byte[PART_SIZE];
        fillArray(data);
        outputStream.write(data);
        outputStream.close();

        verify(this.s3Client).createMultipartUpload(any(CreateMultipartUploadRequest.class));
        verify(this.s3Client, times(1)).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
        verify(this.s3Client).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));

        assertEquals(1, this.capturedUploadPartData.size());
        assertArrayEquals(data, this.capturedUploadPartData.get(0));
    }

    @Test
    void s3ExceptionDuringSimpleUpload() throws IOException
    {
        // Test S3 exception during simple upload (non-412 error)
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .statusCode(500)
            .message("Internal Server Error")
            .build();
        when(this.s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(s3Exception);

        S3BlobOutputStream outputStream = new S3BlobOutputStream(BUCKET_NAME, S3_KEY, this.s3Client,
            this.blobPath, PART_SIZE);

        byte[] data = "Test data".getBytes();
        outputStream.write(data);

        IOException exception = assertThrows(IOException.class, outputStream::close);
        assertTrue(exception.getMessage().contains("Failed to upload to S3"));
        assertEquals(s3Exception, exception.getCause());
    }
}
