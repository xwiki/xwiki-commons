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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStoreException;

import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Error;

/**
 * Handles delete operations for S3 blob store.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component(roles = S3DeleteOperations.class)
@Singleton
public class S3DeleteOperations
{
    private static final int BATCH_DELETE_SIZE = 1000;

    private static final String NULL = "null";

    @Inject
    private S3ClientManager s3ClientManager;

    /**
     * Delete a single blob.
     *
     * @param path the blob path
     * @param store the S3 blob store
     * @throws BlobStoreException if the delete operation fails
     */
    public void deleteBlob(S3BlobStore store, BlobPath path) throws BlobStoreException
    {
        String s3Key = store.getKeyMapper().buildS3Key(path);

        try {
            this.s3ClientManager.getS3Client().deleteObject(DeleteObjectRequest.builder()
                .bucket(store.getBucketName())
                .key(s3Key)
                .build());
        } catch (Exception e) {
            throw new BlobStoreException("Failed to delete blob: " + path, e);
        }
    }

    /**
     * Delete all blobs under a given path.
     *
     * @param blobs the stream of blobs to delete
     * @param store the S3 blob store
     * @throws BlobStoreException if the delete operation fails
     */
    public void deleteBlobs(S3BlobStore store, Stream<Blob> blobs) throws BlobStoreException
    {
        List<String> keysToDelete = new ArrayList<>();
        List<S3Error> partialErrors = new ArrayList<>();

        for (Blob blob : (Iterable<Blob>) blobs::iterator) {
            keysToDelete.add(store.getKeyMapper().buildS3Key(blob.getPath()));
            if (keysToDelete.size() >= BATCH_DELETE_SIZE) {
                partialErrors.addAll(batchDeleteKeys(store.getBucketName(), keysToDelete));
                keysToDelete.clear();
            }
        }
        if (!keysToDelete.isEmpty()) {
            partialErrors.addAll(batchDeleteKeys(store.getBucketName(), keysToDelete));
        }

        if (!partialErrors.isEmpty()) {
            String errorsDescription = partialErrors.stream()
                .map(error -> String.format("key='%s', code='%s', message='%s'",
                    Objects.toString(error.key(), NULL),
                    Objects.toString(error.code(), NULL),
                    Objects.toString(error.message(), NULL)))
                .collect(Collectors.joining("; "));
            throw new BlobStoreException("Failed to delete some blobs: " + errorsDescription);
        }
    }

    /**
     * Batch delete a list of S3 keys.
     *
     * @param bucketName the S3 bucket name
     * @param s3Keys the list of S3 keys to delete (max 1000)
     * @return the list of partial errors returned by S3 (empty if none)
     * @throws BlobStoreException if the batch delete operation fails completely
     */
    private List<S3Error> batchDeleteKeys(String bucketName, List<String> s3Keys) throws BlobStoreException
    {
        if (s3Keys.size() > BATCH_DELETE_SIZE) {
            throw new IllegalArgumentException("Can only delete up to 1000 keys at a time");
        }

        try {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(b -> b.objects(s3Keys.stream().map(key ->
                    ObjectIdentifier.builder().key(key).build()
                ).toList()))
                .build();

            DeleteObjectsResponse deleteObjectsResponse =
                this.s3ClientManager.getS3Client().deleteObjects(deleteRequest);
            return deleteObjectsResponse.errors();
        } catch (Exception e) {
            throw new BlobStoreException("Failed to batch delete blobs", e);
        }
    }
}
