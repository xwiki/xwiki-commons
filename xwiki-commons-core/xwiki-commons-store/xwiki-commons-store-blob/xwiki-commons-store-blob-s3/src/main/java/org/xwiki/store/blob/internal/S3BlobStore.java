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
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
    public Stream<Blob> listBlobs(BlobPath path) throws BlobStoreException
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

        String sourceKey = buildS3Key(sourcePath);
        return copyBlobInternal(sourcePath, targetPath, sourceKey);
    }

    private Blob copyBlobInternal(BlobPath sourcePath, BlobPath targetPath, String sourceKey) throws BlobStoreException
    {
        String targetKey = buildS3Key(targetPath);

        try {
            // Use S3 copyObject with conditional copy to ensure target doesn't exist
            // TODO: according to the documentation, this only works for objects smaller than 5GB.
            // See https://docs.aws.amazon.com/AmazonS3/latest/userguide/CopyingObjectsMPUapi.html
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(this.bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(this.bucketName)
                .destinationKey(targetKey)
                .metadataDirective("COPY")
                .build();

            this.s3Client.copyObject(copyRequest);

            return getBlob(targetPath);
        } catch (NoSuchKeyException e) {
            // This means the source key doesn't exist
            throw new BlobNotFoundException(sourcePath, e);
        } catch (S3Exception e) {
            // Check if this is because target already exists by trying to get target metadata
            try {
                HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(targetKey)
                    .build();
                this.s3Client.headObject(headRequest);
                // Target exists, this might be why copy failed
                throw new BlobAlreadyExistsException(targetPath);
            } catch (NoSuchKeyException targetNotFound) {
                // Target doesn't exist, so the error was something else
                throw new BlobStoreException("Failed to copy blob from " + sourcePath + " to " + targetPath, e);
            }
        }
    }

    @Override
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof S3BlobStore s3SourceStore
            && s3SourceStore.bucketName.equals(this.bucketName)) {
            String sourceKey = s3SourceStore.buildS3Key(sourcePath);
            // Optimize for same-bucket copies using S3 server-side copy
            return copyBlobInternal(sourcePath, targetPath, sourceKey);
        } else {
            // Fall back to stream-based copy for different stores
            try (var inputStream = sourceStore.getBlob(sourcePath).getStream()) {
                Blob targetBlob = getBlob(targetPath);

                // Check if target already exists before writing
                if (targetBlob.exists()) {
                    throw new BlobAlreadyExistsException(targetPath);
                }

                targetBlob.writeFromStream(inputStream);
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
        // Fetch with a page size of 1 as we only ever request the first element.
        return !new S3BlobIterator(getS3KeyPrefix(path), this.bucketName, 1, this.s3Client, this).hasNext();
    }

    @Override
    public void deleteBlob(BlobPath path) throws BlobStoreException
    {
        String s3Key = buildS3Key(path);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(this.bucketName)
                .key(s3Key)
                .build();

            this.s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            throw new BlobStoreException("Failed to delete blob: " + path, e);
        }
    }

    @Override
    public void deleteBlobs(BlobPath path) throws BlobStoreException
    {
        try (Stream<Blob> blobs = listBlobs(path)) {
            for (Blob blob : (Iterable<Blob>) blobs::iterator) {
                // TODO: check with other client libraries if they have something
                // TODO: clarify the behavior if some deletions fail
                deleteBlob(blob.getPath());
            }
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
