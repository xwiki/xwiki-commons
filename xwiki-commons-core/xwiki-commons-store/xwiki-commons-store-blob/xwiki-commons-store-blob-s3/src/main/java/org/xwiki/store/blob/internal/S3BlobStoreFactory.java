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
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreFactory;
import org.xwiki.store.blob.BlobStoreProperties;
import org.xwiki.store.blob.BlobStorePropertiesBuilder;
import org.xwiki.store.blob.S3BlobStoreProperties;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Factory for S3 BlobStore backed by {@link S3BlobStore}.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component
@Singleton
@Named("s3")
public class S3BlobStoreFactory implements BlobStoreFactory
{
    @Inject
    private Logger logger;

    @Inject
    private S3BlobStoreConfiguration configuration;

    @Inject
    private S3ClientManager clientManager;

    @Inject
    private Provider<S3BlobStore> blobStoreProvider;

    @Override
    public String getHint()
    {
        return "s3";
    }

    @Override
    public Class<? extends BlobStoreProperties> getPropertiesClass()
    {
        return S3BlobStoreProperties.class;
    }

    @Override
    public BlobStorePropertiesBuilder newPropertiesBuilder(String name)
    {
        BlobStorePropertiesBuilder builder = new BlobStorePropertiesBuilder(name, getHint());

        // Initialize from configuration.
        String bucket = this.configuration.getS3BucketName();
        if (StringUtils.isNotBlank(bucket)) {
            builder.set(S3BlobStoreProperties.BUCKET, bucket);
        }

        String keyPrefix = this.configuration.getS3KeyPrefix();
        if (StringUtils.isNotBlank(keyPrefix) && StringUtils.isNotBlank(name)) {
            keyPrefix = keyPrefix + "/" + name;
        } else if (StringUtils.isNotBlank(name)) {
            keyPrefix = name;
        }
        builder.set(S3BlobStoreProperties.KEY_PREFIX, keyPrefix);

        builder.set(S3BlobStoreProperties.MULTIPART_UPLOAD_PART_SIZE,
            this.configuration.getS3MultipartPartUploadSizeBytes());
        builder.set(S3BlobStoreProperties.MULTIPART_COPY_PART_SIZE, this.configuration.getS3MultipartCopySizeBytes());

        return builder;
    }

    @Override
    public BlobStore create(String name, BlobStoreProperties properties) throws BlobStoreException
    {
        if (!(properties instanceof S3BlobStoreProperties s3Properties)) {
            throw new BlobStoreException("Invalid properties type for S3 blob store factory: "
                + properties.getClass().getName());
        }

        // Validate bucket access before creating the store.
        validateBucketAccess(s3Properties.getBucket());

        S3BlobStore store = this.blobStoreProvider.get();
        store.initialize(name, s3Properties);

        this.logger.info("Created S3 blob store [{}] for bucket [{}]", name, s3Properties.getBucket());

        return store;
    }

    private void validateBucketAccess(String bucketName) throws BlobStoreException
    {
        try {
            S3Client s3Client = this.clientManager.getS3Client();
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

            s3Client.headBucket(headBucketRequest);
            this.logger.debug("Successfully validated access to S3 bucket: {}", bucketName);
        } catch (NoSuchBucketException e) {
            throw new BlobStoreException("S3 bucket does not exist: " + bucketName, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new BlobStoreException("Access denied to S3 bucket: " + bucketName
                    + ". Please check credentials and bucket permissions.", e);
            } else {
                throw new BlobStoreException("Failed to access S3 bucket: " + bucketName, e);
            }
        } catch (Exception e) {
            throw new BlobStoreException("Unexpected error while validating S3 bucket access", e);
        }
    }
}
