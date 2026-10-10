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

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobOption;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobWriteMode;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
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
     * @param options the copy options
     * @return the copied blob
     * @throws BlobStoreException if the copy operation fails
     */
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobStore targetStore, BlobPath targetPath,
        BlobOption... options) throws BlobStoreException
    {
        if (sourceStore.equals(targetStore) && sourcePath.equals(targetPath)) {
            throw new BlobStoreException("Source and target blob are the same: " + sourcePath);
        }

        BlobOptionSupport.validateSupportedOptions(Set.of(BlobWriteMode.class), options);
        BlobWriteMode writeMode = BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW, options);

        if (sourceStore instanceof S3BlobStore sourceS3Store && targetStore instanceof S3BlobStore targetS3Store) {
            return copyBlobS3Store(sourceS3Store, sourcePath, targetS3Store, targetPath, writeMode);
        }

        return copyBlobWithStream(sourceStore, sourcePath, targetStore, targetPath, writeMode);
    }

    /**
     * Copy a blob from any blob store to another blob store using streams.
     *
     * @param sourceStore the source blob store
     * @param sourcePath the source blob path
     * @param targetStore the target blob store
     * @param targetPath the target blob path
     * @param writeMode the write mode to use for the target
     * @return the copied blob
     * @throws BlobStoreException if the copy operation fails
     */
    private Blob copyBlobWithStream(BlobStore sourceStore, BlobPath sourcePath, BlobStore targetStore,
        BlobPath targetPath, BlobWriteMode writeMode)
        throws BlobStoreException
    {
        try (var inputStream = sourceStore.getBlob(sourcePath).getStream()) {
            Blob targetBlob = targetStore.getBlob(targetPath);

            // Check if target already exists before writing
            if (writeMode == BlobWriteMode.CREATE_NEW && targetBlob.exists()) {
                throw new BlobAlreadyExistsException(targetPath);
            }

            targetBlob.writeFromStream(inputStream, writeMode);
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
     * @param writeMode the write mode to use for the target
     * @return the copied blob
     * @throws BlobStoreException if the copy operation fails
     */
    private Blob copyBlobS3Store(S3BlobStore sourceStore, BlobPath sourcePath, S3BlobStore targetStore,
        BlobPath targetPath, BlobWriteMode writeMode)
        throws BlobStoreException
    {
        // Check if target already exists
        Blob targetBlob = targetStore.getBlob(targetPath);
        if (writeMode == BlobWriteMode.CREATE_NEW && targetBlob.exists()) {
            throw new BlobAlreadyExistsException(targetPath);
        }

        // Get source object metadata including ETag
        String sourceKey = sourceStore.getKeyMapper().buildS3Key(sourcePath);
        S3Client s3Client = this.clientManager.getS3Client();
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
            .bucket(sourceStore.getBucketName())
            .key(sourceKey)
            .build();

        HeadObjectResponse headResponse;
        try {
            headResponse = s3Client.headObject(headRequest);
        } catch (NoSuchKeyException e) {
            throw new BlobNotFoundException(sourcePath, e);
        } catch (Exception e) {
            throw new BlobStoreException("Failed to retrieve source object metadata", e);
        }

        long objectSize = headResponse.contentLength();

        if (objectSize < 0) {
            throw new BlobNotFoundException(sourcePath);
        }

        // Choose copy strategy based on object size.
        if (objectSize <= targetStore.getMultipartPartCopySizeBytes()) {
            performSimpleCopy(sourceStore, sourcePath, targetStore, targetPath, headResponse.eTag(), writeMode);
        } else {
            performMultipartCopy(sourceStore, sourcePath, targetStore, targetPath, headResponse, writeMode);
        }

        return targetStore.getBlob(targetPath);
    }

    /**
     * Performs a simple single-operation copy for objects smaller than 5GB.
     *
     * @param sourceStore the source S3 blob store
     * @param sourcePath the source blob path
     * @param targetStore the target S3 blob store
     * @param targetPath the target blob path
     * @param sourceETag the ETag of the source object to ensure it hasn't changed
     * @param writeMode the write mode to use for the target
     * @throws BlobStoreException if the copy operation fails
     */
    private void performSimpleCopy(S3BlobStore sourceStore, BlobPath sourcePath, S3BlobStore targetStore,
        BlobPath targetPath, String sourceETag, BlobWriteMode writeMode)
        throws BlobStoreException
    {
        String sourceKey = sourceStore.getKeyMapper().buildS3Key(sourcePath);
        String targetKey = targetStore.getKeyMapper().buildS3Key(targetPath);

        try {
            CopyObjectRequest.Builder builder = CopyObjectRequest.builder()
                .sourceBucket(sourceStore.getBucketName())
                .sourceKey(sourceKey)
                .destinationBucket(targetStore.getBucketName())
                .destinationKey(targetKey)
                .metadataDirective(MetadataDirective.COPY);

            // Services that don't support conditional requests reject the source ETag precondition too (for example
            // OVHcloud Object Storage answers 501 to x-amz-copy-source-if-match), so it is sent only when conditional
            // requests are enabled. The existence of the target has already been checked; the If-None-Match condition
            // makes that check atomic on services that support it.
            if (targetStore.isConditionalWrites()) {
                builder.copySourceIfMatch(sourceETag);
                if (writeMode == BlobWriteMode.CREATE_NEW) {
                    builder.ifNoneMatch(S3ConditionalWriteSupport.WILDCARD);
                }
            }

            CopyObjectRequest copyRequest = builder.build();

            this.clientManager.getS3Client().copyObject(copyRequest);
        } catch (Exception e) {
            throw translateCopyFailure(e, targetPath, "Failed to perform simple copy");
        }
    }

    private BlobStoreException translateCopyFailure(Exception e, BlobPath targetPath, String genericMessage)
    {
        if (S3ConditionalWriteSupport.isNotImplemented(e)) {
            return new BlobStoreException(S3ConditionalWriteSupport.getNotImplementedMessage(targetPath), e);
        }
        return new BlobStoreException(genericMessage, e);
    }

    /**
     * Performs a multipart copy for objects larger than 5GB.
     *
     * @param sourceStore the source S3 blob store
     * @param sourcePath the source blob path
     * @param targetStore the target S3 blob store
     * @param targetPath the target blob path
     * @param headResponse the head object response containing metadata, size, and ETag
     * @param writeMode the write mode to use for the target
     * @throws BlobStoreException if the multipart copy operation fails
     */
    private void performMultipartCopy(S3BlobStore sourceStore, BlobPath sourcePath, S3BlobStore targetStore,
        BlobPath targetPath, HeadObjectResponse headResponse, BlobWriteMode writeMode)
        throws BlobStoreException
    {
        String sourceKey = sourceStore.getKeyMapper().buildS3Key(sourcePath);
        String targetKey = targetStore.getKeyMapper().buildS3Key(targetPath);

        S3MultipartUploadHelper uploadHelper = null;
        boolean success = false;
        try {
            S3Client s3Client = this.clientManager.getS3Client();

            // Step 1: Initialize multipart upload with metadata from head response
            uploadHelper = new S3MultipartUploadHelper(
                targetStore.getBucketName(),
                targetKey,
                s3Client,
                targetPath,
                headResponse.metadata(),
                targetStore.isConditionalWrites(),
                writeMode
            );

            this.logger.debug("Initiated multipart copy with upload ID: [{}]", uploadHelper.getUploadId());

            // Step 2: Copy parts using configured part size
            long objectSize = headResponse.contentLength();
            String sourceETag = headResponse.eTag();
            boolean conditionalWrites = targetStore.isConditionalWrites();
            long partSizeBytes = targetStore.getMultipartPartCopySizeBytes();
            long bytePosition = 0;

            while (bytePosition < objectSize) {
                long lastByte = Math.min(bytePosition + partSizeBytes - 1, objectSize - 1);
                String copySourceRange = "bytes=%d-%d".formatted(bytePosition, lastByte);

                int partNumber = uploadHelper.getNextPartNumber();

                UploadPartCopyRequest.Builder partBuilder = UploadPartCopyRequest.builder()
                    .sourceBucket(sourceStore.getBucketName())
                    .sourceKey(sourceKey)
                    .destinationBucket(targetStore.getBucketName())
                    .destinationKey(targetKey)
                    .uploadId(uploadHelper.getUploadId())
                    .partNumber(partNumber)
                    .copySourceRange(copySourceRange);
                // See performSimpleCopy() for why the source ETag precondition is conditional.
                if (conditionalWrites) {
                    partBuilder.copySourceIfMatch(sourceETag);
                }

                UploadPartCopyResponse uploadPartCopyResponse = s3Client.uploadPartCopy(partBuilder.build());

                uploadHelper.addCompletedPart(uploadPartCopyResponse.copyPartResult().eTag());

                this.logger.debug("Copied part [{}] (bytes [{}]-[{}])", partNumber, bytePosition, lastByte);

                bytePosition += partSizeBytes;
            }

            // Step 3: Complete multipart upload
            uploadHelper.complete();

            this.logger.debug("Completed multipart copy for key: [{}]", targetKey);

            success = true;
        } catch (Exception e) {
            throw translateCopyFailure(e, targetPath, "Failed to perform multipart copy");
        } finally {
            // Abort the multipart upload on any kind of failure
            if (!success && uploadHelper != null) {
                uploadHelper.abort();
            }
        }
    }
}
