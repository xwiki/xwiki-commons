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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.WriteCondition;
import org.xwiki.store.blob.WriteConditionFailedException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

/**
 * An OutputStream implementation that uses streaming multipart uploads for large files
 * and simple uploads for small files to efficiently handle data without loading everything into memory.
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
public class S3BlobOutputStream extends OutputStream
{
    // Use 5MB as the minimum part size for multipart uploads (AWS requirement)
    // There can be at maximum 10k parts, so this allows for files up to 50GB.
    private static final int MULTIPART_THRESHOLD = 5 * 1024 * 1024;

    private static final int PART_SIZE = 5 * 1024 * 1024;

    private static final String WILDCARD = "*";

    private final String bucketName;

    private final String s3Key;

    private final S3Client s3Client;

    private final ByteArrayOutputStream buffer;

    private boolean closed;

    private final List<WriteCondition> writeConditions;

    private final BlobPath blobPath;

    // Multipart upload state
    private String uploadId;

    private final List<CompletedPart> completedParts;

    private int partNumber;

    private long totalBytesWritten;

    /**
     * Constructor with write condition.
     *
     * @param bucketName the S3 bucket name
     * @param s3Key the S3 key
     * @param s3Client the S3 client
     * @param conditions the write conditions to check
     * @param blobPath the blob path for error reporting
     */
    public S3BlobOutputStream(String bucketName, String s3Key, S3Client s3Client, List<WriteCondition> conditions,
        BlobPath blobPath)
    {
        this.bucketName = bucketName;
        this.s3Key = s3Key;
        this.s3Client = s3Client;
        this.writeConditions = conditions;
        this.blobPath = blobPath;
        this.buffer = new ByteArrayOutputStream();
        this.completedParts = new ArrayList<>();
        this.partNumber = 1;
    }

    @Override
    public void write(int b) throws IOException
    {
        checkClosed();
        this.buffer.write(b);
        this.totalBytesWritten++;

        // Check if we need to upload a part
        if (this.buffer.size() >= PART_SIZE) {
            uploadPart();
        }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        checkClosed();

        int remaining = len;
        int offset = off;

        while (remaining > 0) {
            int spaceInBuffer = PART_SIZE - this.buffer.size();
            int toWrite = Math.min(remaining, spaceInBuffer);

            this.buffer.write(b, offset, toWrite);
            this.totalBytesWritten += toWrite;
            offset += toWrite;
            remaining -= toWrite;

            // Check if we need to upload a part
            if (this.buffer.size() >= PART_SIZE) {
                uploadPart();
            }
        }
    }

    @Override
    public void flush() throws IOException
    {
        checkClosed();
        // For S3, we don't need to do anything special on flush
        // Data will be uploaded in parts or on close
    }

    @Override
    public void close() throws IOException
    {
        if (this.closed) {
            return;
        }

        try {
            if (this.uploadId != null) {
                // Complete multipart upload
                if (this.buffer.size() > 0) {
                    // Upload the final part
                    uploadPart();
                }
                completeMultipartUpload();
            } else if (this.buffer.size() > 0) {
                // Small file - use simple upload
                uploadSimple();
            }
        } catch (Exception e) {
            // Clean up multipart upload if it was started
            if (this.uploadId != null) {
                try {
                    abortMultipartUpload();
                } catch (Exception abortException) {
                    // Log but don't mask the original exception
                    e.addSuppressed(abortException);
                }
            }
            throw new IOException("Failed to finish upload to S3", e);
        } finally {
            this.buffer.close();
            this.closed = true;
        }
    }

    private void uploadPart() throws IOException
    {
        if (this.buffer.size() == 0) {
            return;
        }

        try {
            // Initialize multipart upload if not already done
            if (this.uploadId == null && this.totalBytesWritten >= MULTIPART_THRESHOLD) {
                initializeMultipartUpload();
            }

            if (this.uploadId != null) {
                // Upload as part of multipart upload
                byte[] data = this.buffer.toByteArray();

                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(this.bucketName)
                    .key(this.s3Key)
                    .uploadId(this.uploadId)
                    .partNumber(this.partNumber)
                    .build();

                UploadPartResponse response = this.s3Client.uploadPart(uploadPartRequest,
                    RequestBody.fromBytes(data));

                CompletedPart completedPart = CompletedPart.builder()
                    .partNumber(this.partNumber)
                    .eTag(response.eTag())
                    .build();

                this.completedParts.add(completedPart);
                this.partNumber++;

                // Clear the buffer for the next part
                this.buffer.reset();
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload part to S3", e);
        }
    }

    private void initializeMultipartUpload() throws IOException
    {
        try {
            CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key)
                .build();

            CreateMultipartUploadResponse response = this.s3Client.createMultipartUpload(createRequest);
            this.uploadId = response.uploadId();
        } catch (Exception e) {
            throw new IOException("Failed to initialize multipart upload", e);
        }
    }

    private void completeMultipartUpload() throws IOException
    {
        boolean ifNotExists = this.writeConditions.contains(BlobDoesNotExistCondition.INSTANCE);
        try {
            CompleteMultipartUploadRequest.Builder builder = CompleteMultipartUploadRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key)
                .uploadId(this.uploadId)
                .multipartUpload(completedUpload -> completedUpload.parts(this.completedParts));

            if (ifNotExists) {
                builder.ifNoneMatch(WILDCARD);
            }

            CompleteMultipartUploadRequest completeRequest = builder.build();

            this.s3Client.completeMultipartUpload(completeRequest);
        } catch (S3Exception e) {
            handleS3Exception(e, ifNotExists);
        }
    }

    private void handleS3Exception(S3Exception e, boolean ifNotExists) throws IOException
    {
        // Check if this is a precondition failed error (412) for conditional requests
        if (e.statusCode() == 412 && ifNotExists) {
            throw new IOException("Write condition failed - blob already exists",
                new WriteConditionFailedException(this.blobPath, this.writeConditions, e));
        }
        throw new IOException("Failed to upload to S3", e);
    }

    private void abortMultipartUpload()
    {
        if (this.uploadId != null) {
            try {
                AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(this.bucketName)
                    .key(this.s3Key)
                    .uploadId(this.uploadId)
                    .build();

                this.s3Client.abortMultipartUpload(abortRequest);
            } catch (Exception e) {
                // Ignore exceptions during cleanup
            }
        }
    }

    private void uploadSimple() throws IOException
    {
        boolean ifNotExists = this.writeConditions.stream().anyMatch(BlobDoesNotExistCondition.class::isInstance);
        try {
            byte[] data = this.buffer.toByteArray();
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key);

            // Add conditional headers if needed
            if (ifNotExists) {
                requestBuilder.ifNoneMatch(WILDCARD);
            }

            PutObjectRequest putRequest = requestBuilder.build();
            this.s3Client.putObject(putRequest, RequestBody.fromBytes(data));
        } catch (S3Exception e) {
            // Check if this is a precondition failed error (412) for conditional requests
            handleS3Exception(e, ifNotExists);
        }
    }

    private void checkClosed() throws IOException
    {
        if (this.closed) {
            throw new IOException("Stream is closed");
        }
    }
}
