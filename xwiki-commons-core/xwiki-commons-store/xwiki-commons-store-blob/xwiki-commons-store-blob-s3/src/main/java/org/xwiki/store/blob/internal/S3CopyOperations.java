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
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.UploadPartCopyRequest;
import software.amazon.awssdk.services.s3.model.UploadPartCopyResponse;

/**
 * Strategy for copying blobs in S3, supporting both simple and multipart copies.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component(roles = S3CopyOperations.class)
@Singleton
public class S3CopyOperations
{
    @Inject
    private S3ClientManager clientManager;

    @Inject
    private Logger logger;

    /**
     * Copy a blob from any blob store to another blob store. If both stores are S3 blob stores, use S3's server-side
     * copy capabilities.
     *
     * @param sourceStore the source blob store
     * @param sourcePath the source blob path
     * @param targetStore the target blob store
     * @param targetPath the target blob path
     * @return the copied blob
     * @throws BlobStoreException if the copy operation fails
     */
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobStore targetStore, BlobPath targetPath)
        throws BlobStoreException
    {
        if (sourceStore.equals(targetStore) && sourcePath.equals(targetPath)) {
            throw new BlobStoreException("Source and target blob are the same: " + sourcePath);
        }

        if (sourceStore instanceof S3BlobStore sourceS3Store && targetStore instanceof S3BlobStore targetS3Store) {
            return copyBlobS3Store(sourceS3Store, sourcePath, targetS3Store, targetPath);
        }

        return copyBlobWithStream(sourceStore, sourcePath, targetStore, targetPath);
    }

    /**
     * Copy a blob from any blob store to another blob store using streams.
     *
     * @param sourceStore the source blob store
     * @param sourcePath the source blob path
     * @param targetStore the target blob store
     * @param targetPath the target blob path
     * @return the copied blob
     * @throws BlobStoreException if the copy operation fails
     */
    private Blob copyBlobWithStream(BlobStore sourceStore, BlobPath sourcePath, BlobStore targetStore,
        BlobPath targetPath)
        throws BlobStoreException
    {
        try (var inputStream = sourceStore.getBlob(sourcePath).getStream()) {
            Blob targetBlob = targetStore.getBlob(targetPath);

            // Check if target already exists before writing
            if (targetBlob.exists()) {
                throw new BlobAlreadyExistsException(targetPath);
            }

            targetBlob.writeFromStream(inputStream, BlobDoesNotExistCondition.INSTANCE);
            return targetBlob;
        } catch (BlobStoreException e) {
            throw e;
        } catch (Exception e) {
            throw new BlobStoreException("Failed to copy blob from external store", e);
        }
    }

    /**
     * Copy a blob within S3 using S3's server-side copy capabilities.
     *
     * @param sourceStore the source S3 blob store
     * @param sourcePath the source blob path
     * @param targetStore the target S3 blob store
     * @param targetPath the target blob path
     * @return the copied blob
     * @throws BlobStoreException if the copy operation fails
     */
    private Blob copyBlobS3Store(S3BlobStore sourceStore, BlobPath sourcePath, S3BlobStore targetStore,
        BlobPath targetPath)
        throws BlobStoreException
    {
        String sourceKey = sourceStore.getKeyMapper().buildS3Key(sourcePath);
        String targetKey = targetStore.getKeyMapper().buildS3Key(targetPath);

        // Check if target already exists
        Blob targetBlob = targetStore.getBlob(targetPath);
        if (targetBlob.exists()) {
            throw new BlobAlreadyExistsException(targetPath);
        }

        // Get source object size
        Blob sourceBlob = sourceStore.getBlob(sourcePath);
        long objectSize = sourceBlob.getSize();

        if (objectSize < 0) {
            throw new BlobNotFoundException(sourcePath);
        }

        // Choose copy strategy based on object size
        if (objectSize <= targetStore.getMultipartPartCopySizeBytes()) {
            performSimpleCopy(sourceStore.getBucketName(), sourceKey, targetStore.getBucketName(), targetKey);
        } else {
            performMultipartCopy(sourceStore, sourceKey, targetStore, targetKey, targetPath, objectSize);
        }

        return targetStore.getBlob(targetPath);
    }

    /**
     * Performs a simple single-operation copy for objects smaller than 5GB.
     *
     * @param sourceBucket the source S3 bucket
     * @param sourceKey the source S3 key
     * @param targetBucket the target S3 bucket
     * @param targetKey the target S3 key
     * @throws BlobStoreException if the copy operation fails
     */
    private void performSimpleCopy(String sourceBucket, String sourceKey, String targetBucket, String targetKey)
        throws BlobStoreException
    {
        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(targetBucket)
                .destinationKey(targetKey)
                .metadataDirective(MetadataDirective.COPY)
                .build();

            this.clientManager.getS3Client().copyObject(copyRequest);
        } catch (Exception e) {
            throw new BlobStoreException("Failed to perform simple copy", e);
        }
    }

    /**
     * Performs a multipart copy for objects larger than 5GB.
     *
     * @param sourceStore the source S3 blob store
     * @param sourceKey the source S3 key
     * @param targetStore the target S3 blob store
     * @param targetKey the target S3 key
     * @param targetPath the target blob path (for error reporting)
     * @param objectSize the size of the object in bytes
     * @throws BlobStoreException if the multipart copy operation fails
     */
    private void performMultipartCopy(S3BlobStore sourceStore, String sourceKey, S3BlobStore targetStore,
        String targetKey, BlobPath targetPath, long objectSize) throws BlobStoreException
    {
        S3MultipartUploadHelper uploadHelper = null;
        boolean success = false;
        try {
            S3Client s3Client = this.clientManager.getS3Client();

            // Retrieve source object metadata
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(sourceStore.getBucketName())
                .key(sourceKey)
                .build();
            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            Map<String, String> metadata = headResponse.metadata();

            // Step 1: Initialize multipart upload with metadata
            uploadHelper = new S3MultipartUploadHelper(
                targetStore.getBucketName(),
                targetKey,
                s3Client,
                targetPath,
                List.of(BlobDoesNotExistCondition.INSTANCE),
                metadata
            );

            this.logger.debug("Initiated multipart copy with upload ID: {}", uploadHelper.getUploadId());

            // Step 2: Copy parts using configured part size
            // Use the configured multipart copy size for the size of copy parts.
            long partSizeBytes = targetStore.getMultipartPartCopySizeBytes();
            long bytePosition = 0;

            while (bytePosition < objectSize) {
                long lastByte = Math.min(bytePosition + partSizeBytes - 1, objectSize - 1);
                String copySourceRange = "bytes=%d-%d".formatted(bytePosition, lastByte);

                int partNumber = uploadHelper.getNextPartNumber();

                UploadPartCopyRequest uploadPartCopyRequest = UploadPartCopyRequest.builder()
                    .sourceBucket(sourceStore.getBucketName())
                    .sourceKey(sourceKey)
                    .destinationBucket(targetStore.getBucketName())
                    .destinationKey(targetKey)
                    .uploadId(uploadHelper.getUploadId())
                    .partNumber(partNumber)
                    .copySourceRange(copySourceRange)
                    .build();

                UploadPartCopyResponse uploadPartCopyResponse = s3Client.uploadPartCopy(uploadPartCopyRequest);

                uploadHelper.addCompletedPart(uploadPartCopyResponse.copyPartResult().eTag());

                this.logger.debug("Copied part {} (bytes {}-{})", partNumber, bytePosition, lastByte);

                bytePosition += partSizeBytes;
            }

            // Step 3: Complete multipart upload
            uploadHelper.complete();

            this.logger.debug("Completed multipart copy for key: {}", targetKey);

            success = true;
        } catch (Exception e) {
            throw new BlobStoreException("Failed to perform multipart copy", e);
        } finally {
            // Abort the multipart upload on any kind of failure
            if (!success && uploadHelper != null) {
                uploadHelper.abort();
            }
        }
    }
}
