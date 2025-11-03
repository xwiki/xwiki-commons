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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobDoesNotExistOption;
import org.xwiki.store.blob.BlobOption;
import org.xwiki.store.blob.BlobPath;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

/**
 * An OutputStream implementation that uses streaming multipart uploads for large files
 * and simple uploads for small files to efficiently handle data without loading everything into memory.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class S3BlobOutputStream extends OutputStream
{
    private static final String WILDCARD = "*";

    private static final String GENERIC_FAILED_UPLOAD_MESSAGE = "Failed to upload to S3";

    /**
     * Extended ByteArrayOutputStream to expose an InputStream view of the current buffer.
     * This avoids unnecessary copying of the internal buffer when uploading to S3.
     */
    private static class ByteArrayOutputStreamWithInputStream extends ByteArrayOutputStream
    {
        ByteArrayOutputStreamWithInputStream()
        {
            super();
        }

        public InputStream toInputStream()
        {
            return new ByteArrayInputStream(this.buf, 0, this.count);
        }
    }

    private final String bucketName;

    private final String s3Key;

    private final S3Client s3Client;

    private final ByteArrayOutputStreamWithInputStream buffer;

    private final int partSize;

    private boolean closed;

    private boolean failed;

    private final List<BlobOption> options;

    private final BlobPath blobPath;

    // Multipart upload helper
    private S3MultipartUploadHelper uploadHelper;

    /**
     * Constructor with options.
     *
     * @param bucketName the S3 bucket name
     * @param s3Key the S3 key
     * @param s3Client the S3 client
     * @param options the options for writing to the stream
     * @param blobPath the blob path for error reporting
     * @param partSizeBytes the configured multipart upload part size in bytes
     */
    public S3BlobOutputStream(String bucketName, String s3Key, S3Client s3Client, List<BlobOption> options,
        BlobPath blobPath, long partSizeBytes)
    {
        this.bucketName = bucketName;
        this.s3Key = s3Key;
        this.s3Client = s3Client;
        this.options = options;
        this.blobPath = blobPath;
        this.buffer = new ByteArrayOutputStreamWithInputStream();
        this.failed = false;
        // Cap part size to Integer.MAX_VALUE since ByteArrayOutputStream uses int for size (and about 2GB is in
        // fact already too much as upload buffer).
        this.partSize = (int) Math.min(partSizeBytes, Integer.MAX_VALUE);
    }

    @Override
    public void write(int b) throws IOException
    {
        checkStreamState();

        // Success flag to track if write completed without exceptions.
        boolean success = false;
        try {
            this.buffer.write(b);

            // Check if we need to upload a part.
            if (this.buffer.size() >= this.partSize) {
                uploadPartFromBuffer();
            }

            success = true;
        } finally {
            if (!success) {
                // An error occurred during write, mark stream as failed.
                markAsFailedAndCleanup();
            }
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
        checkStreamState();

        // Success flag to track if write completed without exceptions.
        boolean success = false;
        try {
            int remaining = len;
            int offset = off;

            while (remaining > 0) {
                int spaceInBuffer = this.partSize - this.buffer.size();
                int toWrite = Math.min(remaining, spaceInBuffer);

                this.buffer.write(b, offset, toWrite);
                offset += toWrite;
                remaining -= toWrite;

                // Check if we need to upload a part
                if (this.buffer.size() >= this.partSize) {
                    uploadPartFromBuffer();
                }
            }

            success = true;
        } finally {
            if (!success) {
                // An error occurred during write, mark stream as failed.
                markAsFailedAndCleanup();
            }
        }
    }

    @Override
    public void flush() throws IOException
    {
        checkStreamState();
        // For S3, we do not need to do anything special on flush
        // Data will be uploaded in parts or on close
    }

    @Override
    public void close() throws IOException
    {
        if (this.closed) {
            return;
        }

        this.closed = true;

        if (this.failed) {
            // If already in failed state, just return.
            return;
        }

        // Success flag to track if close completed without exceptions.
        boolean success = false;
        try {
            if (this.uploadHelper != null) {
                // Complete multipart upload.
                // Upload the final part (if any data is left in the buffer).
                uploadPartFromBuffer();
                this.uploadHelper.complete();
            } else {
                // Small file - use simple upload
                uploadSimple();
            }
            success = true;
        } finally {
            if (!success) {
                // An error occurred during close, mark stream as failed.
                markAsFailedAndCleanup();
            }
        }
    }

    private void uploadPartFromBuffer() throws IOException
    {
        if (this.buffer.size() == 0) {
            return;
        }

        // Initialize multipart upload if not already done.
        ensureMultipartUploadInitialized();

        // Get the next part number and ensure we have not exceeded limits.
        int partNumber = this.uploadHelper.getNextPartNumber();

        try (InputStream inputStream = this.buffer.toInputStream()) {
            // Upload as part of multipart upload.
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key)
                .uploadId(this.uploadHelper.getUploadId())
                .partNumber(partNumber)
                .build();

            UploadPartResponse response = this.s3Client.uploadPart(uploadPartRequest,
                RequestBody.fromInputStream(inputStream, this.buffer.size()));

            this.uploadHelper.addCompletedPart(response.eTag());

            // Clear the buffer for the next part
            this.buffer.reset();
        } catch (Exception e) {
            throw new IOException("Failed to upload part to S3", e);
        }
    }

    private void ensureMultipartUploadInitialized() throws IOException
    {
        if (this.uploadHelper != null) {
            return;
        }

        this.uploadHelper = new S3MultipartUploadHelper(
            this.bucketName,
            this.s3Key,
            this.s3Client,
            this.blobPath,
            this.options
        );
    }

    private void uploadSimple() throws IOException
    {
        try (InputStream inputStream = this.buffer.toInputStream()) {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key);

            // Add conditional headers if needed.
            if (hasIfNotExistsOption()) {
                requestBuilder.ifNoneMatch(WILDCARD);
            }

            this.s3Client.putObject(requestBuilder.build(),
                RequestBody.fromInputStream(inputStream, this.buffer.size()));
        } catch (S3Exception e) {
            handleS3Exception(e);
        } catch (Exception e) {
            throw new IOException(GENERIC_FAILED_UPLOAD_MESSAGE, e);
        }
    }

    private void handleS3Exception(S3Exception e) throws IOException
    {
        // Check if this is a precondition failed error (412) for conditional requests.
        if (e.statusCode() == 412 && hasIfNotExistsOption()) {
            throw new IOException("Blob already exists",
                new BlobAlreadyExistsException(this.blobPath, e));
        }
        throw new IOException(GENERIC_FAILED_UPLOAD_MESSAGE, e);
    }

    private boolean hasIfNotExistsOption()
    {
        return this.options != null && this.options.contains(BlobDoesNotExistOption.INSTANCE);
    }

    private void checkStreamState() throws IOException
    {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
        if (this.failed) {
            throw new IOException("Stream is in failed state due to previous error");
        }
    }

    private void markAsFailedAndCleanup()
    {
        this.failed = true;
        if (this.uploadHelper != null) {
            this.uploadHelper.abort();
        }
    }
}
