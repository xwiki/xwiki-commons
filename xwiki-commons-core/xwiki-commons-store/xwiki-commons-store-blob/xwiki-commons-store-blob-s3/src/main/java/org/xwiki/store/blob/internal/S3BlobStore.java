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
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.store.blob.AbstractBlobStore;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.UploadPartCopyRequest;
import software.amazon.awssdk.services.s3.model.UploadPartCopyResponse;

/**
 * S3-based blob store implementation.
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
public class S3BlobStore extends AbstractBlobStore
{
    private static final Logger LOGGER = LoggerFactory.getLogger(S3BlobStore.class);

    private static final String PATH_SEPARATOR = "/";

    /**
     * Maximum size for single-operation copy (5GB as per AWS documentation).
     */
    private static final long MULTIPART_COPY_THRESHOLD = 5L * 1024 * 1024 * 1024;

    /**
     * Size of each part in multipart copy (5GB, the maximum allowed part size).
     */
    private static final long MULTIPART_COPY_PART_SIZE = 5L * 1024 * 1024 * 1024;

    private final String bucketName;

    private final String keyPrefix;

    private final S3Client s3Client;

    /**
     * Constructor.
     *
     * @param name the name of this blob store
     * @param bucketName the S3 bucket name
     * @param keyPrefix the key prefix for all objects in this store
     * @param s3Client the S3 client
     */
    public S3BlobStore(String name, String bucketName, String keyPrefix, S3Client s3Client)
    {
        super(name);
        this.bucketName = bucketName;
        this.keyPrefix = keyPrefix != null ? keyPrefix : "";
        this.s3Client = s3Client;
    }

    @Override
    public Blob getBlob(BlobPath path) throws BlobStoreException
    {
        String s3Key = buildS3Key(path);
        return new S3Blob(path, this.bucketName, s3Key, this, this.s3Client);
    }

    @Override
    public Stream<Blob> listBlobs(BlobPath path)
    {
        String prefix = getS3KeyPrefix(path);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
            new S3BlobIterator(prefix, this.bucketName, 1000, this.s3Client, this), Spliterator.ORDERED), false);
    }

    private String getS3KeyPrefix(BlobPath path)
    {
        String prefix = buildS3Key(path);
        if (!prefix.endsWith(PATH_SEPARATOR)) {
            prefix += PATH_SEPARATOR;
        }
        return prefix;
    }

    @Override
    public Blob copyBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException("Source and target paths are the same");
        }

        return copyBlobInternal(this, sourcePath, targetPath);
    }

    private Blob copyBlobInternal(S3BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath)
        throws BlobStoreException
    {
        String sourceKey = sourceStore.buildS3Key(sourcePath);
        String targetKey = buildS3Key(targetPath);

        // Check if target already exists
        Blob targetBlob = getBlob(targetPath);
        if (targetBlob.exists()) {
            throw new BlobAlreadyExistsException(targetPath);
        }

        // Get source object size using the source store
        Blob sourceBlob = sourceStore.getBlob(sourcePath);
        long objectSize = sourceBlob.getSize();

        if (objectSize < 0) {
            throw new BlobNotFoundException(sourcePath);
        }

        // Choose copy strategy based on object size.
        if (objectSize < MULTIPART_COPY_THRESHOLD) {
            performSimpleCopy(sourceStore.bucketName, sourceKey, targetKey);
        } else {
            performMultipartCopy(sourceStore.bucketName, sourceKey, targetKey, objectSize, targetPath);
        }

        return getBlob(targetPath);
    }

    /**
     * Performs a simple single-operation copy for objects smaller than 5GB.
     *
     * @param sourceBucket the source S3 bucket
     * @param sourceKey the source S3 key
     * @param targetKey the target S3 key
     * @throws BlobStoreException if the copy operation fails
     */
    private void performSimpleCopy(String sourceBucket, String sourceKey, String targetKey) throws BlobStoreException
    {
        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(this.bucketName)
                .destinationKey(targetKey)
                .metadataDirective("COPY")
                .build();

            this.s3Client.copyObject(copyRequest);
        } catch (Exception e) {
            throw new BlobStoreException("Failed to perform simple copy", e);
        }
    }

    /**
     * Performs a multipart copy for objects larger than 5GB.
     *
     * @param sourceBucket the source S3 bucket
     * @param sourceKey the source S3 key
     * @param targetKey the target S3 key
     * @param objectSize the size of the object in bytes
     * @param targetPath the target blob path (for error reporting)
     * @throws BlobStoreException if the multipart copy operation fails
     */
    private void performMultipartCopy(String sourceBucket, String sourceKey, String targetKey, long objectSize,
        BlobPath targetPath) throws BlobStoreException
    {
        S3MultipartUploadHelper uploadHelper = null;
        boolean success = false;
        try {
            // Step 1: Initialize multipart upload
            uploadHelper = new S3MultipartUploadHelper(
                this.bucketName,
                targetKey,
                this.s3Client,
                targetPath,
                null
            );

            LOGGER.debug("Initiated multipart copy with upload ID: {}", uploadHelper.getUploadId());

            // Step 2: Copy parts
            long bytePosition = 0;

            while (bytePosition < objectSize) {
                long lastByte = Math.min(bytePosition + MULTIPART_COPY_PART_SIZE - 1, objectSize - 1);
                String copySourceRange = "bytes=%d-%d".formatted(bytePosition, lastByte);

                int partNumber = uploadHelper.getNextPartNumber();

                UploadPartCopyRequest uploadPartCopyRequest = UploadPartCopyRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(this.bucketName)
                    .destinationKey(targetKey)
                    .uploadId(uploadHelper.getUploadId())
                    .partNumber(partNumber)
                    .copySourceRange(copySourceRange)
                    .build();

                UploadPartCopyResponse uploadPartCopyResponse = this.s3Client.uploadPartCopy(uploadPartCopyRequest);

                uploadHelper.addCompletedPart(uploadPartCopyResponse.copyPartResult().eTag());

                LOGGER.debug("Copied part {} (bytes {}-{})", partNumber, bytePosition, lastByte);

                bytePosition += MULTIPART_COPY_PART_SIZE;
            }

            // Step 3: Complete multipart upload
            uploadHelper.complete();

            LOGGER.debug("Completed multipart copy for key: {}", targetKey);

            success = true;
        } catch (Exception e) {
            throw new BlobStoreException("Failed to perform multipart copy", e);
        } finally {
            // Abort the multipart upload on any kind of failure.
            if (!success && uploadHelper != null) {
                uploadHelper.abort();
            }
        }
    }

    @Override
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof S3BlobStore s3SourceStore
            && s3SourceStore.bucketName.equals(this.bucketName)) {
            // Optimize for same-bucket copies using S3 server-side copy
            return copyBlobInternal(s3SourceStore, sourcePath, targetPath);
        } else {
            // Fall back to stream-based copy for different stores
            try (var inputStream = sourceStore.getBlob(sourcePath).getStream()) {
                Blob targetBlob = getBlob(targetPath);

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
    }

    @Override
    public Blob moveBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        Blob movedBlob = copyBlob(sourcePath, targetPath);
        deleteBlob(sourcePath);
        return movedBlob;
    }

    @Override
    public Blob moveBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        Blob movedBlob = copyBlob(sourceStore, sourcePath, targetPath);
        sourceStore.deleteBlob(sourcePath);
        return movedBlob;
    }

    @Override
    public boolean isEmptyDirectory(BlobPath path) throws BlobStoreException
    {
        try {
            // Fetch with a page size of 1 as we only ever request the first element.
            return !new S3BlobIterator(getS3KeyPrefix(path), this.bucketName, 1, this.s3Client, this).hasNext();
        } catch (Exception e) {
            // The code doesn't throw any checked exceptions as the iterator cannot throw them, but we catch any
            // runtime exceptions to make them nicer to handle for the caller.
            throw new BlobStoreException("Failed to check if directory is empty: " + path, e);
        }
    }

    @Override
    public void deleteBlob(BlobPath path) throws BlobStoreException
    {
        String s3Key = buildS3Key(path);

        try {
            this.s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(this.bucketName)
                .key(s3Key)
                .build());
        } catch (Exception e) {
            throw new BlobStoreException("Failed to delete blob: " + path, e);
        }
    }

    @Override
    public void deleteBlobs(BlobPath path) throws BlobStoreException
    {
        try (Stream<Blob> blobs = listBlobs(path)) {
            // Collect groups of 1000 blobs to delete at a time using S3 batch delete.
            List<String> keysToDelete = new ArrayList<>();
            for (Blob blob : (Iterable<Blob>) blobs::iterator) {
                keysToDelete.add(buildS3Key(blob.getPath()));
                if (keysToDelete.size() >= 1000) {
                    batchDeleteKeys(keysToDelete);
                    keysToDelete.clear();
                }
            }
            if (!keysToDelete.isEmpty()) {
                batchDeleteKeys(keysToDelete);
            }
        }
    }

    private void batchDeleteKeys(List<String> s3Keys) throws BlobStoreException
    {
        if (s3Keys.size() > 1000) {
            throw new IllegalArgumentException("Can only delete up to 1000 keys at a time");
        }

        try {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(this.bucketName)
                .delete(b -> b.objects(s3Keys.stream().map(key ->
                    ObjectIdentifier.builder().key(key).build()
                ).toList()))
                .build();

            DeleteObjectsResponse deleteObjectsResponse = this.s3Client.deleteObjects(deleteRequest);
            if (!deleteObjectsResponse.errors().isEmpty()) {
                throw new BlobStoreException("Failed to delete some blobs: " + deleteObjectsResponse.errors());
            }
        } catch (Exception e) {
            throw new BlobStoreException("Failed to batch delete blobs", e);
        }
    }

    /**
     * Build the S3 key from a BlobPath.
     *
     * @param blobPath the blob path
     * @return the S3 key
     */
    private String buildS3Key(BlobPath blobPath)
    {
        String pathStr = blobPath.toString();
        if (StringUtils.isNotBlank(this.keyPrefix)) {
            return this.keyPrefix + PATH_SEPARATOR + pathStr;
        }
        return pathStr;
    }

    /**
     * Convert an S3 key back to a BlobPath.
     *
     * @param s3Key the S3 key
     * @return the BlobPath, or null if the key doesn't match our prefix
     */
    BlobPath s3KeyToBlobPath(String s3Key)
    {
        String pathStr = s3Key;

        if (StringUtils.isNotBlank(this.keyPrefix)) {
            String expectedPrefix = this.keyPrefix + PATH_SEPARATOR;
            if (s3Key.startsWith(expectedPrefix)) {
                pathStr = s3Key.substring(expectedPrefix.length());
            } else {
                // Key doesn't match our prefix
                return null;
            }
        }

        try {
            return BlobPath.from(pathStr);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid blob path from S3 key: {}", s3Key, e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof S3BlobStore that)) {
            return false;
        }

        return new EqualsBuilder().append(this.bucketName, that.bucketName)
            .append(this.keyPrefix, that.keyPrefix)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(this.bucketName).append(this.keyPrefix).toHashCode();
    }
}
