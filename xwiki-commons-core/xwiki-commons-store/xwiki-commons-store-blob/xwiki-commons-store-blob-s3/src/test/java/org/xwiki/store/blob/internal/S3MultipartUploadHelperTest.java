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
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobWriteMode;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3MultipartUploadHelper}.
 *
 * @version $Id$
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@ExtendWith(MockitoExtension.class)
class S3MultipartUploadHelperTest
{
    private static final String BUCKET_NAME = "test-bucket";

    private static final String S3_KEY = "test/key";

    private static final String UPLOAD_ID = "test-upload-id";

    @Mock
    private S3Client s3Client;

    @Mock
    private BlobPath blobPath;

    // Capture debug logs only from our own package to avoid capturing debug logs of background threads that might
    // still be running from previously executed tests and that would interfere with assertions on the logs in this
    // test class.
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.DEBUG, "org.xwiki.store.blob");

    @BeforeEach
    void setUp()
    {
        CreateMultipartUploadResponse createResponse = CreateMultipartUploadResponse.builder()
            .uploadId(UPLOAD_ID)
            .build();

        lenient().when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenReturn(createResponse);
    }

    @Test
    void constructorInitializesMultipartUpload() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        assertNotNull(helper.getUploadId());
        assertEquals(UPLOAD_ID, helper.getUploadId());
        verify(this.s3Client).createMultipartUpload(any(CreateMultipartUploadRequest.class));

        assertInitializationLog(0);
    }

    @Test
    void constructorThrowsIOExceptionWhenInitializationFails()
    {
        when(this.s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
            .thenThrow(S3Exception.builder().message("S3 error").build());

        IOException exception = assertThrows(IOException.class, () -> {
            new S3MultipartUploadHelper(BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);
        });

        assertTrue(exception.getMessage().contains("Failed to initialize multipart upload"));
    }

    @Test
    void getNextPartNumberReturnsSequentialNumbers() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        assertEquals(1, helper.getNextPartNumber());
        helper.addCompletedPart("etag1");
        assertEquals(2, helper.getNextPartNumber());
        helper.addCompletedPart("etag2");
        assertEquals(3, helper.getNextPartNumber());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletedPartLog(2, 2);
    }

    @Test
    void getNextPartNumberThrowsWhenExceedingMaxParts() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        // Add MAX_PARTS parts
        for (int i = 0; i < S3MultipartUploadHelper.MAX_PARTS; i++) {
            helper.getNextPartNumber();
            helper.addCompletedPart("etag" + i);
        }

        // Try to get part 10001
        IOException exception = assertThrows(IOException.class, helper::getNextPartNumber);
        assertEquals(
            "Exceeded maximum number of parts (10000) for multipart upload. "
                + "Consider increasing the part size to reduce the number of parts.",
            exception.getMessage());

        assertInitializationLog(0);
        for (int i = 1; i <= S3MultipartUploadHelper.MAX_PARTS; i++) {
            assertCompletedPartLog(i, i);
        }
    }

    @Test
    void addCompletedPartAddsPartInOrder() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");
        helper.getNextPartNumber();
        helper.addCompletedPart("etag2");

        // Verify by completing the upload and checking the request
        helper.complete();

        ArgumentCaptor<CompleteMultipartUploadRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).completeMultipartUpload(captor.capture());

        CompleteMultipartUploadRequest request = captor.getValue();
        assertEquals(2, request.multipartUpload().parts().size());
        assertEquals("etag1", request.multipartUpload().parts().get(0).eTag());
        assertEquals("etag2", request.multipartUpload().parts().get(1).eTag());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletedPartLog(2, 2);
        assertCompletionLog(3);
    }

    @Test
    void completeSuccessfullyCompletesUpload() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");
        helper.complete();

        ArgumentCaptor<CompleteMultipartUploadRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).completeMultipartUpload(captor.capture());

        CompleteMultipartUploadRequest request = captor.getValue();
        assertEquals(BUCKET_NAME, request.bucket());
        assertEquals(S3_KEY, request.key());
        assertEquals(UPLOAD_ID, request.uploadId());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletionLog(2);
    }

    @Test
    void completeWithCustomizerAppliesCustomization() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");

        Consumer<CompleteMultipartUploadRequest.Builder> customizer = builder -> 
            builder.requestPayer("requester");

        helper.complete(customizer);

        ArgumentCaptor<CompleteMultipartUploadRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).completeMultipartUpload(captor.capture());

        CompleteMultipartUploadRequest request = captor.getValue();
        assertEquals("requester", request.requestPayerAsString());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletionLog(2);
    }

    @Test
    void completeWithCreateNewModeAddsIfNoneMatch() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath, BlobWriteMode.CREATE_NEW);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");
        helper.complete();

        ArgumentCaptor<CompleteMultipartUploadRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).completeMultipartUpload(captor.capture());

        CompleteMultipartUploadRequest request = captor.getValue();
        assertEquals("*", request.ifNoneMatch());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletionLog(2);
    }

    @Test
    void completeThrowsIOExceptionOn412WithCreateNew() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath, BlobWriteMode.CREATE_NEW);

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .message("Precondition failed")
            .statusCode(412)
            .build();

        when(this.s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
            .thenThrow(s3Exception);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");

        IOException exception = assertThrows(IOException.class, helper::complete);
        assertThat(exception.getMessage(), containsString("Blob already exists"));
        assertInstanceOf(BlobAlreadyExistsException.class, exception.getCause());
        assertEquals(this.blobPath, ((BlobAlreadyExistsException) exception.getCause()).getBlobPath());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
    }

    @Test
    void completeThrowsIOExceptionOnS3Error() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        when(this.s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
            .thenThrow(S3Exception.builder().message("S3 error").statusCode(500).build());

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");

        IOException exception = assertThrows(IOException.class, helper::complete);
        assertTrue(exception.getMessage().contains("S3 operation failed"));

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
    }

    @Test
    void completeThrowsWhenAlreadyCompleted() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");
        helper.complete();

        IOException exception = assertThrows(IOException.class, helper::complete);
        assertEquals("Multipart upload already completed", exception.getMessage());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletionLog(2);
    }

    @Test
    void abortAbortsMultipartUpload() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.abort();

        ArgumentCaptor<AbortMultipartUploadRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).abortMultipartUpload(captor.capture());

        AbortMultipartUploadRequest request = captor.getValue();
        assertEquals(BUCKET_NAME, request.bucket());
        assertEquals(S3_KEY, request.key());
        assertEquals(UPLOAD_ID, request.uploadId());

        assertInitializationLog(0);
        assertAbortLog(1);
    }

    @Test
    void abortIsIdempotent() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.abort();
        helper.abort();
        helper.abort();

        verify(this.s3Client, times(1)).abortMultipartUpload(any(AbortMultipartUploadRequest.class));

        assertInitializationLog(0);
        assertAbortLog(1);
    }

    @Test
    void abortLogsWarningOnFailure() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        when(this.s3Client.abortMultipartUpload(any(AbortMultipartUploadRequest.class)))
            .thenThrow(new RuntimeException("Abort failed"));

        helper.abort();

        // Verify no exception is thrown and warning is logged
        verify(this.s3Client).abortMultipartUpload(any(AbortMultipartUploadRequest.class));

        assertInitializationLog(0);
        assertFailedAbortLog(1);
    }

    @Test
    void getNextPartNumberThrowsWhenAborted() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.abort();

        IOException exception = assertThrows(IOException.class, helper::getNextPartNumber);
        assertEquals("Multipart upload has been aborted", exception.getMessage());

        assertInitializationLog(0);
        assertAbortLog(1);
    }

    @Test
    void addCompletedPartThrowsWhenAborted() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.abort();

        IOException exception = assertThrows(IOException.class, () -> helper.addCompletedPart("etag"));
        assertEquals("Multipart upload has been aborted", exception.getMessage());

        assertInitializationLog(0);
        assertAbortLog(1);
    }

    @Test
    void completeThrowsWhenAborted() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.abort();

        IOException exception = assertThrows(IOException.class, helper::complete);
        assertEquals("Multipart upload has been aborted", exception.getMessage());

        assertInitializationLog(0);
        assertAbortLog(1);
    }

    @Test
    void addCompletedPartThrowsWhenCompleted() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");
        helper.complete();

        IOException exception = assertThrows(IOException.class, () -> helper.addCompletedPart("etag2"));
        assertEquals("Multipart upload already completed", exception.getMessage());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletionLog(2);
    }

    @Test
    void getNextPartNumberThrowsWhenCompleted() throws IOException
    {
        S3MultipartUploadHelper helper = new S3MultipartUploadHelper(
            BUCKET_NAME, S3_KEY, this.s3Client, this.blobPath);

        helper.getNextPartNumber();
        helper.addCompletedPart("etag1");
        helper.complete();

        IOException exception = assertThrows(IOException.class, helper::getNextPartNumber);
        assertEquals("Multipart upload already completed", exception.getMessage());

        assertInitializationLog(0);
        assertCompletedPartLog(1, 1);
        assertCompletionLog(2);
    }

    // Helper methods for log assertions

    /**
     * Asserts the initialization log message at the specified index.
     *
     * @param index the log message index
     */
    private void assertInitializationLog(int index)
    {
        assertEquals("Initialized multipart upload for key test/key with upload ID: test-upload-id",
            this.logCapture.getMessage(index));
    }

    /**
     * Asserts the completed part log message at the specified index.
     *
     * @param index the log message index
     * @param partNumber the part number that was completed
     */
    private void assertCompletedPartLog(int index, int partNumber)
    {
        assertEquals(String.format("Added completed part %d for upload ID: test-upload-id", partNumber),
            this.logCapture.getMessage(index));
    }

    /**
     * Asserts the completion log message at the specified index.
     *
     * @param index the log message index
     */
    private void assertCompletionLog(int index)
    {
        assertEquals("Completed multipart upload for key test/key with upload ID: test-upload-id",
            this.logCapture.getMessage(index));
    }

    /**
     * Asserts the abort log message at the specified index.
     *
     * @param index the log message index
     */
    private void assertAbortLog(int index)
    {
        assertEquals("Aborted multipart upload for key test/key with upload ID: test-upload-id",
            this.logCapture.getMessage(index));
    }

    /**
     * Asserts the failed abort warning log message at the specified index.
     *
     * @param index the log message index
     */
    private void assertFailedAbortLog(int index)
    {
        String message = this.logCapture.getMessage(index);
        assertTrue(message.startsWith("Failed to abort multipart upload for blob at path"));
        assertTrue(message.contains("test-upload-id"));
    }
}
