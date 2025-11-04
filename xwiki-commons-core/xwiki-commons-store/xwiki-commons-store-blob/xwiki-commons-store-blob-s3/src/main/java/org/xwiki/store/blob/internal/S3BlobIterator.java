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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Iterator that fetches S3 objects page by page and converts them to Blob objects.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
final class S3BlobIterator implements Iterator<Blob>
{
    private final String prefix;

    private final String bucketName;

    private final int pageSize;

    private final S3Client s3Client;

    private final S3BlobStore store;

    private String continuationToken;

    private Iterator<S3Object> currentPageIterator;

    private boolean hasMorePages = true;

    private Blob nextBlob;

    private boolean nextBlobComputed;

    /**
     * Create a new iterator that pages through S3 objects.
     *
     * @param prefix the S3 key prefix to list
     * @param bucketName the S3 bucket name
     * @param s3Client the S3 client
     * @param pageSize the page size to use for fetching files
     * @param store the parent BlobStore
     */
    S3BlobIterator(String prefix, String bucketName, int pageSize, S3Client s3Client, S3BlobStore store)
    {
        this.prefix = prefix;
        this.bucketName = bucketName;
        this.pageSize = pageSize;
        this.s3Client = s3Client;
        this.store = store;
    }

    @Override
    public boolean hasNext()
    {
        if (!this.nextBlobComputed) {
            this.nextBlob = computeNext();
            this.nextBlobComputed = true;
        }
        return this.nextBlob != null;
    }

    @Override
    public Blob next()
    {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        Blob result = this.nextBlob;
        this.nextBlob = null;
        this.nextBlobComputed = false;
        return result;
    }

    private Blob computeNext()
    {
        while (true) {
            if (this.currentPageIterator != null && this.currentPageIterator.hasNext()) {
                S3Object s3Object = this.currentPageIterator.next();
                Blob blob = convertToBlob(s3Object);
                if (blob != null) {
                    return blob;
                }
                continue;
            }

            if (!this.hasMorePages) {
                return null;
            }

            fetchNextPage();
        }
    }

    private void fetchNextPage()
    {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
            .bucket(this.bucketName)
            .prefix(this.prefix)
            .maxKeys(this.pageSize);

        if (this.continuationToken != null) {
            requestBuilder.continuationToken(this.continuationToken);
        }

        // This could throw an exception. As there aren't any checked exceptions on Iterator methods, we let it
        // propagate as a runtime exception.
        ListObjectsV2Response response = this.s3Client.listObjectsV2(requestBuilder.build());

        this.currentPageIterator = response.contents().iterator();
        this.continuationToken = response.nextContinuationToken();
        this.hasMorePages = response.isTruncated();
    }

    private Blob convertToBlob(S3Object s3Object)
    {
        String key = s3Object.key();

        if (key.endsWith("/")) {
            return null;
        }

        BlobPath blobPath = this.store.getKeyMapper().s3KeyToBlobPath(key);
        if (blobPath != null) {
            return new S3Blob(blobPath, this.bucketName, key, this.store, this.s3Client);
        }

        return null;
    }
}
