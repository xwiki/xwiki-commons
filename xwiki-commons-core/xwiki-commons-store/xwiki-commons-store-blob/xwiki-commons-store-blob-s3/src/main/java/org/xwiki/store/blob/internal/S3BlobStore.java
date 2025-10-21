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

import jakarta.inject.Inject;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.store.blob.AbstractBlobStore;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

/**
 * S3-based blob store implementation.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component(roles = S3BlobStore.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class S3BlobStore extends AbstractBlobStore
{
    private String bucketName;

    @Inject
    private S3ClientManager clientManager;

    private S3KeyMapper keyMapper;

    @Inject
    private S3CopyOperations copyOperations;

    @Inject
    private S3DeleteOperations deleteOperations;

    /**
     * Default constructor for component manager.
     */
    public S3BlobStore()
    {
        super(null);
    }

    /**
     * Initialize this blob store, must be called before performing any other operations.
     *
     * @param name the name of this blob store
     * @param bucketName the S3 bucket name
     * @param keyPrefix the key prefix for all objects in this store
     */
    public void initialize(String name, String bucketName, String keyPrefix)
    {
        this.name = name;
        this.bucketName = bucketName;
        this.keyMapper = new S3KeyMapper(keyPrefix);
    }

    @Override
    public Blob getBlob(BlobPath path) throws BlobStoreException
    {
        String s3Key = this.keyMapper.buildS3Key(path);
        return new S3Blob(path, this.bucketName, s3Key, this, this.clientManager.getS3Client());
    }

    @Override
    public Stream<Blob> listBlobs(BlobPath path)
    {
        String prefix = this.keyMapper.getS3KeyPrefix(path);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
            new S3BlobIterator(prefix, this.bucketName, 1000, this.clientManager.getS3Client(), this),
            Spliterator.ORDERED), false);
    }

    @Override
    public Blob copyBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        return this.copyOperations.copyBlob(this, sourcePath, this, targetPath);
    }

    @Override
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        return this.copyOperations.copyBlob(sourceStore, sourcePath, this, targetPath);
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
            // Fetch with a page size of 1 as we only ever request the first element
            return !new S3BlobIterator(this.keyMapper.getS3KeyPrefix(path), this.bucketName, 1,
                this.clientManager.getS3Client(), this).hasNext();
        } catch (Exception e) {
            // The code doesn't throw any checked exceptions as the iterator cannot throw them, but we catch any
            // runtime exceptions to make them nicer to handle for the caller
            throw new BlobStoreException("Failed to check if directory is empty: " + path, e);
        }
    }

    @Override
    public void deleteBlob(BlobPath path) throws BlobStoreException
    {
        this.deleteOperations.deleteBlob(this, path);
    }

    @Override
    public void deleteBlobs(BlobPath path) throws BlobStoreException
    {
        try (Stream<Blob> blobs = listBlobs(path)) {
            this.deleteOperations.deleteBlobs(this, blobs);
        }
    }

    /**
     * Get the bucket name.
     *
     * @return the bucket name
     */
    String getBucketName()
    {
        return this.bucketName;
    }

    /**
     * Get the key mapper.
     *
     * @return the key mapper
     */
    S3KeyMapper getKeyMapper()
    {
        return this.keyMapper;
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
            .append(this.keyMapper, that.keyMapper)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(this.bucketName).append(this.keyMapper).toHashCode();
    }
}
