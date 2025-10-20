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
import java.util.function.Consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.WriteCondition;
import org.xwiki.store.blob.WriteConditionFailedException;

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
 * @since 17.7.0RC1
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

    private static final String WILDCARD = "*";

    private final String bucketName;

    private final String s3Key;

    private final S3Client s3Client;

    private final BlobPath blobPath;

    private final List<WriteCondition> writeConditions;

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
     * @param writeConditions optional write conditions to enforce
     * @throws IOException if initialization fails
     */
    public S3MultipartUploadHelper(String bucketName, String s3Key, S3Client s3Client, BlobPath blobPath,
        List<WriteCondition> writeConditions) throws IOException
    {
        this.bucketName = bucketName;
        this.s3Key = s3Key;
        this.s3Client = s3Client;
        this.blobPath = blobPath;
        this.writeConditions = writeConditions;
        this.completedParts = new ArrayList<>();
        this.nextPartNumber = 1;
        this.completed = false;
        this.aborted = false;

        // Initialize the multipart upload immediately
        try {
            CreateMultipartUploadRequest.Builder requestBuilder = CreateMultipartUploadRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key);

            CreateMultipartUploadRequest createRequest = requestBuilder.build();
            CreateMultipartUploadResponse response = this.s3Client.createMultipartUpload(createRequest);
            this.uploadId = response.uploadId();

            LOGGER.debug("Initialized multipart upload for key {} with upload ID: {}", this.s3Key, this.uploadId);
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
                    + "Maximum supported size is %d GB",
                MAX_PARTS, MAX_PARTS * MAX_PART_SIZE / (1024 * 1024 * 1024)));
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

        LOGGER.debug("Added completed part {} for upload ID: {}", this.nextPartNumber, this.uploadId);

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

        try {
            CompleteMultipartUploadRequest.Builder builder = CompleteMultipartUploadRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key)
                .uploadId(this.uploadId)
                .multipartUpload(b -> b.parts(this.completedParts));

            // Add conditional headers if needed
            if (hasIfNotExistsCondition()) {
                builder.ifNoneMatch(WILDCARD);
            }

            // Allow the caller to customize the request.
            if (requestCustomizer != null) {
                requestCustomizer.accept(builder);
            }

            CompleteMultipartUploadRequest completeRequest = builder.build();
            this.s3Client.completeMultipartUpload(completeRequest);

            this.completed = true;

            LOGGER.debug("Completed multipart upload for key {} with upload ID: {}", this.s3Key, this.uploadId);
        } catch (S3Exception e) {
            handleS3Exception(e);
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

            LOGGER.debug("Aborted multipart upload for key {} with upload ID: {}", this.s3Key, this.uploadId);
        } catch (Exception e) {
            // Log but don't throw - abort is best-effort cleanup
            LOGGER.warn("Failed to abort multipart upload for blob at path {} with upload ID {}, root cause: {}",
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

    private void handleS3Exception(S3Exception e) throws IOException
    {
        // Check if this is a precondition failed error (412) for conditional requests
        if (e.statusCode() == 412 && hasIfNotExistsCondition()) {
            throw new IOException("Write condition failed - blob already exists",
                new WriteConditionFailedException(this.blobPath, this.writeConditions, e));
        }
        throw new IOException("S3 operation failed for blob at path " + this.blobPath, e);
    }

    private boolean hasIfNotExistsCondition()
    {
        return this.writeConditions != null && this.writeConditions.contains(BlobDoesNotExistCondition.INSTANCE);
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
