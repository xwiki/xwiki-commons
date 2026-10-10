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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.store.blob.BlobOption;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobWriteMode;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Helper class for managing S3 multipart uploads with proper error handling and cleanup.
 * This class handles the lifecycle of a multipart upload including initialization, part tracking,
 * completion, and cleanup on failure.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class S3MultipartUploadHelper
{
    /**
     * AWS S3 maximum number of parts in a multipart upload.
     */
    public static final int MAX_PARTS = 10000;

    /**
     * Minimum part size for multipart uploads (5MB as per AWS requirement).
     */
    public static final int MIN_PART_SIZE = 5 * 1024 * 1024;

    /**
     * Maximum part size for multipart uploads (5GB as per AWS requirement).
     */
    public static final long MAX_PART_SIZE = 5L * 1024 * 1024 * 1024;

    private static final Logger LOGGER = LoggerFactory.getLogger(S3MultipartUploadHelper.class);

    private final String bucketName;

    private final String s3Key;

    private final S3Client s3Client;

    private final BlobPath blobPath;

    private final BlobWriteMode writeMode;

    private final boolean conditionalWrites;

    private final String uploadId;

    private final List<CompletedPart> completedParts;

    private int nextPartNumber;

    private boolean completed;

    private boolean aborted;

    /**
     * Constructor. Initializes the multipart upload immediately.
     *
     * @param bucketName the S3 bucket name
     * @param s3Key the S3 key for the object
     * @param s3Client the S3 client
     * @param blobPath the blob path (for error reporting)
     * @param options optional options to use for the upload
     * @throws IOException if initialization fails
     */
    public S3MultipartUploadHelper(String bucketName, String s3Key, S3Client s3Client, BlobPath blobPath,
        BlobOption... options) throws IOException
    {
        this(bucketName, s3Key, s3Client, blobPath, null, options);
    }

    /**
     * Constructor. Initializes the multipart upload immediately with metadata.
     *
     * @param bucketName the S3 bucket name
     * @param s3Key the S3 key for the object
     * @param s3Client the S3 client
     * @param blobPath the blob path (for error reporting)
     * @param metadata optional metadata to apply to the object
     * @param options optional options to use for the upload
     * @throws IOException if initialization fails
     */
    public S3MultipartUploadHelper(String bucketName, String s3Key, S3Client s3Client, BlobPath blobPath,
        Map<String, String> metadata, BlobOption... options) throws IOException
    {
        this(bucketName, s3Key, s3Client, blobPath, metadata, true, options);
    }

    /**
     * Constructor. Initializes the multipart upload immediately with metadata and control over conditional writes.
     *
     * @param bucketName the S3 bucket name
     * @param s3Key the S3 key for the object
     * @param s3Client the S3 client
     * @param blobPath the blob path (for error reporting)
     * @param metadata optional metadata to apply to the object
     * @param conditionalWrites {@code true} to implement {@link BlobWriteMode#CREATE_NEW} with a conditional write
     *     ({@code If-None-Match: *}) when completing the upload, {@code false} to check the existence of the object
     *     before completing it instead (for S3 services that do not support conditional writes)
     * @param options optional options to use for the upload
     * @throws IOException if initialization fails
     * @since 18.9.0RC1
     */
    public S3MultipartUploadHelper(String bucketName, String s3Key, S3Client s3Client, BlobPath blobPath,
        Map<String, String> metadata, boolean conditionalWrites, BlobOption... options) throws IOException
    {
        this.bucketName = bucketName;
        this.s3Key = s3Key;
        this.s3Client = s3Client;
        this.blobPath = blobPath;
        BlobOptionSupport.validateSupportedOptions(Set.of(BlobWriteMode.class), options);
        this.writeMode = BlobWriteMode.resolve(BlobWriteMode.REPLACE_EXISTING, options);
        this.conditionalWrites = conditionalWrites;
        this.completedParts = new ArrayList<>();
        this.nextPartNumber = 1;
        this.completed = false;
        this.aborted = false;

        // Initialize the multipart upload immediately.
        try {
            CreateMultipartUploadRequest.Builder requestBuilder = CreateMultipartUploadRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key);

            // Add metadata if provided.
            if (metadata != null && !metadata.isEmpty()) {
                requestBuilder.metadata(metadata);
            }

            CreateMultipartUploadRequest createRequest = requestBuilder.build();
            CreateMultipartUploadResponse response = this.s3Client.createMultipartUpload(createRequest);
            this.uploadId = response.uploadId();

            LOGGER.debug("Initialized multipart upload for key [{}] with upload ID: [{}]", this.s3Key, this.uploadId);
        } catch (Exception e) {
            throw new IOException("Failed to initialize multipart upload for blob at path " + this.blobPath, e);
        }
    }

    /**
     * Get the next part number and ensure we haven't exceeded the maximum number of parts.
     * This method should be called before uploading each part.
     *
     * @return the next part number to use (1-based)
     * @throws IOException if the maximum number of parts has been exceeded
     */
    public int getNextPartNumber() throws IOException
    {
        ensureNotCompleted();
        ensureNotAborted();

        if (this.nextPartNumber > MAX_PARTS) {
            throw new IOException(String.format(
                "Exceeded maximum number of parts (%d) for multipart upload. "
                    + "Consider increasing the part size to reduce the number of parts.", MAX_PARTS));
        }

        return this.nextPartNumber;
    }

    /**
     * Add a completed part to the upload. The part number is tracked internally.
     *
     * @param eTag the ETag returned from the upload
     * @throws IOException if the upload is in an invalid state
     */
    public void addCompletedPart(String eTag) throws IOException
    {
        ensureNotCompleted();
        ensureNotAborted();

        CompletedPart completedPart = CompletedPart.builder()
            .partNumber(this.nextPartNumber)
            .eTag(eTag)
            .build();

        this.completedParts.add(completedPart);

        LOGGER.debug("Added completed part [{}] for upload ID: [{}]", this.nextPartNumber, this.uploadId);

        this.nextPartNumber++;
    }

    /**
     * Complete the multipart upload.
     *
     * @throws IOException if completion fails
     */
    public void complete() throws IOException
    {
        complete(null);
    }

    /**
     * Complete the multipart upload with a custom configuration.
     * This allows callers to add additional settings to the complete request.
     *
     * @param requestCustomizer a consumer to customize the complete request builder
     * @throws IOException if completion fails
     */
    public void complete(Consumer<CompleteMultipartUploadRequest.Builder> requestCustomizer) throws IOException
    {
        ensureNotCompleted();
        ensureNotAborted();

        if (this.writeMode == BlobWriteMode.CREATE_NEW && !this.conditionalWrites) {
            // Non-atomic fallback for services without conditional writes.
            S3ConditionalWriteSupport.assertAbsent(this.s3Client, this.bucketName, this.s3Key, this.blobPath);
        }

        try {
            CompleteMultipartUploadRequest.Builder builder = CompleteMultipartUploadRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key)
                .uploadId(this.uploadId)
                .multipartUpload(b -> b.parts(this.completedParts));

            if (this.writeMode == BlobWriteMode.CREATE_NEW && this.conditionalWrites) {
                builder.ifNoneMatch(S3ConditionalWriteSupport.WILDCARD);
            }

            // Allow the caller to customize the request.
            if (requestCustomizer != null) {
                requestCustomizer.accept(builder);
            }

            CompleteMultipartUploadRequest completeRequest = builder.build();
            this.s3Client.completeMultipartUpload(completeRequest);

            this.completed = true;

            LOGGER.debug("Completed multipart upload for key [{}] with upload ID: [{}]", this.s3Key, this.uploadId);
        } catch (S3Exception e) {
            throw handleS3Exception(e);
        } catch (Exception e) {
            throw new IOException("Failed to complete multipart upload for blob at path " + this.blobPath, e);
        }
    }

    /**
     * Abort the multipart upload and clean up any uploaded parts.
     * This method is idempotent and safe to call multiple times.
     */
    public void abort()
    {
        if (this.aborted) {
            return;
        }

        try {
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key)
                .uploadId(this.uploadId)
                .build();

            this.s3Client.abortMultipartUpload(abortRequest);
            this.aborted = true;

            LOGGER.debug("Aborted multipart upload for key [{}] with upload ID: [{}]", this.s3Key, this.uploadId);
        } catch (Exception e) {
            // Log but don't throw - abort is best-effort cleanup
            LOGGER.warn("Failed to abort multipart upload for blob at path [{}] with upload ID [{}], root cause: [{}]",
                this.blobPath, this.uploadId, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * Get the upload ID.
     *
     * @return the upload ID
     */
    public String getUploadId()
    {
        return this.uploadId;
    }

    private IOException handleS3Exception(S3Exception e)
    {
        String genericMessage = "S3 operation failed for blob at path " + this.blobPath;
        if (this.writeMode == BlobWriteMode.CREATE_NEW && this.conditionalWrites) {
            return S3ConditionalWriteSupport.translateConditionalWriteFailure(e, this.blobPath, genericMessage);
        }
        return new IOException(genericMessage, e);
    }

    private void ensureNotCompleted() throws IOException
    {
        if (this.completed) {
            throw new IOException("Multipart upload already completed");
        }
    }

    private void ensureNotAborted() throws IOException
    {
        if (this.aborted) {
            throw new IOException("Multipart upload has been aborted");
        }
    }
}
