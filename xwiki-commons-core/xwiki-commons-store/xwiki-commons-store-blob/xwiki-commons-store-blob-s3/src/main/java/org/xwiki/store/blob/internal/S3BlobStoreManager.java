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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreManager;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Blob store manager for the S3-based blob store.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component
@Named("s3")
@Singleton
public class S3BlobStoreManager implements BlobStoreManager, Initializable
{
    @Inject
    private Logger logger;

    @Inject
    private S3BlobStoreConfiguration configuration;

    @Inject
    private S3ClientManager clientManager;

    @Inject
    private Provider<S3BlobStore> blobStoreProvider;

    private String bucketName;

    @Override
    public void initialize() throws InitializationException
    {
        this.bucketName = this.configuration.getS3BucketName();
        if (StringUtils.isBlank(this.bucketName)) {
            throw new InitializationException("S3 bucket name is required but not configured. "
                + "Please set the 'store.s3.bucketName' property.");
        }

        // Verify bucket access
        try {
            validateBucketAccess();
            this.logger.info("S3 blob store manager initialized for bucket: {}", this.bucketName);
        } catch (Exception e) {
            throw new InitializationException("Failed to validate S3 bucket access", e);
        }
    }

    @Override
    public BlobStore getBlobStore(String name) throws BlobStoreException
    {
        String keyPrefix = this.configuration.getS3KeyPrefix();
        if (StringUtils.isNotBlank(keyPrefix) && StringUtils.isNotBlank(name)) {
            // Use store name as additional prefix if not default
            keyPrefix = keyPrefix + "/" + name;
        } else if (!StringUtils.isBlank(name)) {
            // Use store name as prefix if no global prefix configured
            keyPrefix = name;
        }

        S3BlobStore blobStore = this.blobStoreProvider.get();
        blobStore.initialize(name, this.bucketName, keyPrefix);
        return blobStore;
    }

    private void validateBucketAccess() throws BlobStoreException
    {
        try {
            S3Client s3Client = this.clientManager.getS3Client();
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(this.bucketName)
                .build();

            s3Client.headBucket(headBucketRequest);
            this.logger.debug("Successfully validated access to S3 bucket: {}", this.bucketName);
        } catch (NoSuchBucketException e) {
            throw new BlobStoreException("S3 bucket does not exist: " + this.bucketName, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new BlobStoreException("Access denied to S3 bucket: " + this.bucketName
                    + ". Please check credentials and bucket permissions.", e);
            } else {
                throw new BlobStoreException("Failed to access S3 bucket: " + this.bucketName, e);
            }
        } catch (Exception e) {
            throw new BlobStoreException("Unexpected error while validating S3 bucket access", e);
        }
    }
}
