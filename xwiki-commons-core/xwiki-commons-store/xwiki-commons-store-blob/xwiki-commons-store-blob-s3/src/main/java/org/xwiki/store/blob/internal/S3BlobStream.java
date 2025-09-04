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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * A lazy streaming implementation for listing S3 blobs that doesn't load all objects into memory.
 * Uses S3's pagination to fetch objects on-demand as the stream is consumed.
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
public class S3BlobStream
{
    private static final Logger LOGGER = LoggerFactory.getLogger(S3BlobStream.class);

    // Maximum allowed by S3
    private static final int PAGE_SIZE = 1000;

    private final String prefix;

    private final String bucketName;

    private final S3Client s3Client;

    private final S3BlobStore store;

    /**
     * Constructor.
     *
     * @param prefix the S3 key prefix to list
     * @param bucketName the S3 bucket name
     * @param s3Client the S3 client
     * @param store the parent S3 blob store
     */
    public S3BlobStream(String prefix, String bucketName, S3Client s3Client, S3BlobStore store)
    {
        this.prefix = prefix;
        this.bucketName = bucketName;
        this.s3Client = s3Client;
        this.store = store;
    }

    /**
     * Create a stream of blobs that lazily fetches from S3.
     *
     * @return a stream of blobs
     */
    public Stream<Blob> stream()
    {
        return StreamSupport.stream(new S3BlobIterable().spliterator(), false);
    }

    /**
     * Iterable that lazily fetches S3 objects page by page.
     */
    private final class S3BlobIterable implements Iterable<Blob>
    {
        @Override
        public Iterator<Blob> iterator()
        {
            return new S3BlobIterator();
        }
    }

    /**
     * Iterator that fetches S3 objects page by page and converts them to Blob objects.
     */
    private final class S3BlobIterator implements Iterator<Blob>
    {
        private String continuationToken;

        private Iterator<S3Object> currentPageIterator;

        private boolean hasMorePages = true;

        private Blob nextBlob;

        private boolean nextBlobComputed;

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
                // If we have objects in the current page, process them
                if (this.currentPageIterator != null && this.currentPageIterator.hasNext()) {
                    S3Object s3Object = this.currentPageIterator.next();
                    Blob blob = convertToBlob(s3Object);
                    if (blob != null) {
                        return blob;
                    }
                    // Continue to next object if this one was filtered out
                    continue;
                }

                // If no more pages available, we're done
                if (!this.hasMorePages) {
                    return null;
                }

                // Fetch the next page
                try {
                    fetchNextPage();
                } catch (S3Exception e) {
                    LOGGER.error("Failed to fetch next page from S3", e);
                    return null;
                }
            }
        }

        private void fetchNextPage() throws S3Exception
        {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(S3BlobStream.this.bucketName)
                .prefix(S3BlobStream.this.prefix)
                .maxKeys(PAGE_SIZE);

            if (this.continuationToken != null) {
                requestBuilder.continuationToken(this.continuationToken);
            }

            ListObjectsV2Response response = S3BlobStream.this.s3Client.listObjectsV2(requestBuilder.build());

            this.currentPageIterator = response.contents().iterator();
            this.continuationToken = response.nextContinuationToken();
            this.hasMorePages = response.isTruncated();
        }

        private Blob convertToBlob(S3Object s3Object)
        {
            String key = s3Object.key();

            // Skip directories (keys ending with /)
            if (key.endsWith("/")) {
                return null;
            }

            BlobPath blobPath = S3BlobStream.this.store.s3KeyToBlobPath(key);
            if (blobPath != null) {
                return new S3Blob(blobPath, S3BlobStream.this.bucketName, key,
                    S3BlobStream.this.store, S3BlobStream.this.s3Client);
            }

            return null;
        }
    }
}
