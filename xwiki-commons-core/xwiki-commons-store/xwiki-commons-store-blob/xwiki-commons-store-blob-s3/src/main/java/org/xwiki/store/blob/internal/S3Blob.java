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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobOption;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * A {@link Blob} implementation that represents a blob stored in S3.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class S3Blob extends AbstractBlob<S3BlobStore>
{
    private final String bucketName;

    private final String s3Key;

    private final S3Client s3Client;

    /**
     * Constructor.
     *
     * @param path the blob path
     * @param bucketName the S3 bucket name
     * @param s3Key the S3 key for this blob
     * @param store the parent store
     * @param s3Client the S3 client
     */
    public S3Blob(BlobPath path, String bucketName, String s3Key, S3BlobStore store, S3Client s3Client)
    {
        super(store, path);
        this.bucketName = bucketName;
        this.s3Key = s3Key;
        this.s3Client = s3Client;
    }

    @Override
    public boolean exists() throws BlobStoreException
    {
        try {
            getHeadObject();
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new BlobStoreException("Error checking if the blob [%s] exists.".formatted(getPath()), e);
        }
    }

    private HeadObjectResponse getHeadObject()
    {
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
            .bucket(this.bucketName)
            .key(this.s3Key)
            .build();

        return this.s3Client.headObject(headRequest);
    }

    @Override
    public long getSize() throws BlobStoreException
    {
        try {
            return getHeadObject().contentLength();
        } catch (NoSuchKeyException e) {
            return -1;
        } catch (S3Exception e) {
            throw new BlobStoreException("Failed to get size for blob: %s".formatted(getPath()), e);
        }
    }

    @Override
    public OutputStream getOutputStream(BlobOption... options) throws BlobStoreException
    {
        long partSizeBytes = this.getStore().getProperties().getMultipartUploadPartSize();
        return new S3BlobOutputStream(this.bucketName, this.s3Key, this.s3Client,
            Arrays.asList(options), getPath(), partSizeBytes);
    }

    @Override
    public InputStream getStream() throws BlobStoreException
    {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(this.bucketName)
                .key(this.s3Key)
                .build();

            return this.s3Client.getObject(getRequest);
        } catch (NoSuchKeyException e) {
            throw new BlobNotFoundException(getPath(), e);
        } catch (S3Exception e) {
            throw new BlobStoreException("Failed to get stream for blob: %s".formatted(getPath()), e);
        }
    }
}
