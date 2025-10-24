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

import jakarta.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreManager;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3BlobStoreManager}.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList(S3BlobStoreManager.class)
class S3BlobStoreManagerTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private S3BlobStoreConfiguration configuration;

    @MockComponent
    private S3ClientManager clientManager;

    @MockComponent
    private Provider<S3BlobStore> blobStoreProvider;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    private S3Client s3Client;

    @BeforeEach
    void setUp()
    {
        this.s3Client = mock();
        when(this.clientManager.getS3Client()).thenReturn(this.s3Client);
    }

    @Test
    void initializeWithValidBucket() throws Exception
    {
        when(this.configuration.getS3BucketName()).thenReturn("test-bucket");
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenReturn(HeadBucketResponse.builder().build());

        BlobStoreManager manager = this.componentManager.getInstance(BlobStoreManager.class, "s3");

        assertNotNull(manager);
        verify(this.s3Client).headBucket(any(HeadBucketRequest.class));
        assertEquals("S3 blob store manager initialized for bucket: test-bucket", 
            this.logCapture.getMessage(0));
    }

    @Test
    void initializeWithMissingBucketName()
    {
        when(this.configuration.getS3BucketName()).thenReturn("");

        Exception exception = assertThrows(Exception.class,
            () -> this.componentManager.getInstance(BlobStoreManager.class, "s3"));
        
        assertInstanceOf(InitializationException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("S3 bucket name is required but not configured"));
        assertTrue(exception.getCause().getMessage().contains("store.s3.bucketName"));
    }

    @Test
    void initializeWithNullBucketName()
    {
        when(this.configuration.getS3BucketName()).thenReturn(null);

        Exception exception = assertThrows(Exception.class,
            () -> this.componentManager.getInstance(BlobStoreManager.class, "s3"));
        
        assertInstanceOf(InitializationException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("S3 bucket name is required but not configured"));
    }

    @Test
    void initializeWithNonExistentBucket()
    {
        when(this.configuration.getS3BucketName()).thenReturn("non-existent-bucket");
        NoSuchBucketException noSuchBucketException = NoSuchBucketException.builder()
            .message("Bucket does not exist")
            .build();
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(noSuchBucketException);

        Exception exception = assertThrows(Exception.class,
            () -> this.componentManager.getInstance(BlobStoreManager.class, "s3"));
        
        assertInstanceOf(InitializationException.class, exception.getCause());
        assertEquals("Failed to validate S3 bucket access", exception.getCause().getMessage());
        assertInstanceOf(BlobStoreException.class, exception.getCause().getCause());
        assertTrue(exception.getCause().getCause().getMessage()
            .contains("S3 bucket does not exist: non-existent-bucket"));
    }

    @Test
    void initializeWithAccessDenied()
    {
        when(this.configuration.getS3BucketName()).thenReturn("forbidden-bucket");
        AwsServiceException s3Exception = S3Exception.builder()
            .statusCode(403)
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode("AccessDenied")
                .errorMessage("Access Denied")
                .build())
            .build();
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(s3Exception);

        Exception exception = assertThrows(Exception.class,
            () -> this.componentManager.getInstance(BlobStoreManager.class, "s3"));
        
        assertInstanceOf(InitializationException.class, exception.getCause());
        assertEquals("Failed to validate S3 bucket access", exception.getCause().getMessage());
        assertInstanceOf(BlobStoreException.class, exception.getCause().getCause());
        assertTrue(exception.getCause().getCause().getMessage()
            .contains("Access denied to S3 bucket: forbidden-bucket"));
        assertTrue(exception.getCause().getCause().getMessage()
            .contains("Please check credentials and bucket permissions"));
    }

    @Test
    void initializeWithOtherS3Exception()
    {
        when(this.configuration.getS3BucketName()).thenReturn("error-bucket");
        AwsServiceException s3Exception = S3Exception.builder()
            .statusCode(500)
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode("InternalError")
                .errorMessage("Internal Server Error")
                .build())
            .build();
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(s3Exception);

        Exception exception = assertThrows(Exception.class,
            () -> this.componentManager.getInstance(BlobStoreManager.class, "s3"));
        
        assertInstanceOf(InitializationException.class, exception.getCause());
        assertEquals("Failed to validate S3 bucket access", exception.getCause().getMessage());
        assertInstanceOf(BlobStoreException.class, exception.getCause().getCause());
        assertTrue(exception.getCause().getCause().getMessage()
            .contains("Failed to access S3 bucket: error-bucket"));
    }

    @Test
    void initializeWithUnexpectedException()
    {
        when(this.configuration.getS3BucketName()).thenReturn("test-bucket");
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        Exception exception = assertThrows(Exception.class,
            () -> this.componentManager.getInstance(BlobStoreManager.class, "s3"));
        
        assertInstanceOf(InitializationException.class, exception.getCause());
        assertEquals("Failed to validate S3 bucket access", exception.getCause().getMessage());
        assertInstanceOf(BlobStoreException.class, exception.getCause().getCause());
        assertTrue(exception.getCause().getCause().getMessage()
            .contains("Unexpected error while validating S3 bucket access"));
    }

    @ParameterizedTest
    @CsvSource({
        "'', '', ''",
        "'', 'mystore', 'mystore'",
        "'global/prefix', '', 'global/prefix'",
        "'global/prefix', 'mystore', 'global/prefix/mystore'",
        "'prefix',, 'prefix'"
    })
    void getBlobStoreWithPrefixAndName(String prefix, String name, String keyPrefix) throws Exception
    {
        BlobStoreManager manager = initializeManager();
        when(this.configuration.getS3KeyPrefix()).thenReturn(prefix);

        S3BlobStore store = mock();
        when(this.blobStoreProvider.get()).thenReturn(store);

        BlobStore blobStore = manager.getBlobStore(name);

        assertSame(store, blobStore);
        verify(store).initialize(name, "test-bucket", keyPrefix);
    }

    private BlobStoreManager initializeManager() throws Exception
    {
        when(this.configuration.getS3BucketName()).thenReturn("test-bucket");
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenReturn(HeadBucketResponse.builder().build());
        BlobStoreManager result = this.componentManager.getInstance(BlobStoreManager.class, "s3");
        assertEquals("S3 blob store manager initialized for bucket: test-bucket",
            this.logCapture.getMessage(0));
        return result;
    }
}
