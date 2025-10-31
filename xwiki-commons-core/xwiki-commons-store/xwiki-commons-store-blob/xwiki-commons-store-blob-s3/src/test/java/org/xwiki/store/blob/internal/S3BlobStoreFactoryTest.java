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

import javax.inject.Named;

import jakarta.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.component.manager.ComponentManager;

import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreProperties;
import org.xwiki.store.blob.BlobStorePropertiesBuilder;
import org.xwiki.store.blob.S3BlobStoreProperties;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3BlobStoreFactory}.
 *
 * @version $Id$
 */
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:MultipleStringLiterals"})
@ComponentTest
class S3BlobStoreFactoryTest
{
    private static final String TYPE = "s3";
    private static final String NAME = "mystore";
    private static final String BUCKET = "test-bucket";
    private static final String PREFIX = "test-prefix";

    // Valid multipart sizes (5MB minimum as per S3 requirements)
    // 5MB
    private static final long VALID_MULTIPART_UPLOAD_SIZE = 5 * 1024 * 1024L;
    // 10MB
    private static final long VALID_MULTIPART_COPY_SIZE = 10 * 1024 * 1024L;

    @InjectMockComponents
    private S3BlobStoreFactory factory;

    @MockComponent
    private S3BlobStoreConfiguration configuration;

    @MockComponent
    private S3ClientManager clientManager;

    @MockComponent
    private Provider<S3BlobStore> blobStoreProvider;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    private S3Client s3Client;

    @BeforeEach
    void setUp(MockitoComponentManager componentManager)
    {
        when(this.componentManagerProvider.get()).thenReturn(componentManager);
        this.s3Client = mock();
        when(this.clientManager.getS3Client()).thenReturn(this.s3Client);
    }

    @Test
    void getPropertiesClass()
    {
        assertEquals(S3BlobStoreProperties.class, this.factory.getPropertiesClass());
    }

    @Test
    void getHint()
    {
        assertEquals(TYPE, this.factory.getHint());
    }

    @Test
    void newPropertiesBuilderWithConfiguration()
    {
        when(this.configuration.getS3BucketName()).thenReturn(BUCKET);
        when(this.configuration.getS3KeyPrefix()).thenReturn(PREFIX);
        when(this.configuration.getS3MultipartPartUploadSizeBytes()).thenReturn(VALID_MULTIPART_UPLOAD_SIZE);
        when(this.configuration.getS3MultipartCopySizeBytes()).thenReturn(VALID_MULTIPART_COPY_SIZE);

        BlobStorePropertiesBuilder builder = this.factory.newPropertiesBuilder(NAME);

        assertNotNull(builder);
        assertEquals(NAME, builder.getName());
        assertEquals(TYPE, builder.getHint());
        assertEquals(BUCKET, builder.get(S3BlobStoreProperties.BUCKET).orElse(null));
        assertEquals(PREFIX + "/" + NAME, builder.get(S3BlobStoreProperties.KEY_PREFIX).orElse(null));
        assertEquals(VALID_MULTIPART_UPLOAD_SIZE,
            builder.get(S3BlobStoreProperties.MULTIPART_UPLOAD_PART_SIZE).orElse(null));
        assertEquals(VALID_MULTIPART_COPY_SIZE,
            builder.get(S3BlobStoreProperties.MULTIPART_COPY_PART_SIZE).orElse(null));
    }

    @ParameterizedTest
    @CsvSource({
        "'', '', ''",
        "'', 'mystore', 'mystore'",
        "'global/prefix', '', 'global/prefix'",
        "'global/prefix', 'mystore', 'global/prefix/mystore'"
    })
    void newPropertiesBuilderWithPrefixAndName(String prefix, String name, String expectedKeyPrefix)
    {
        when(this.configuration.getS3KeyPrefix()).thenReturn(prefix);

        BlobStorePropertiesBuilder builder = this.factory.newPropertiesBuilder(name);

        assertEquals(expectedKeyPrefix, builder.get(S3BlobStoreProperties.KEY_PREFIX).orElse(null));
    }

    @Test
    void createWithValidBucket() throws Exception
    {
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenReturn(HeadBucketResponse.builder().build());

        S3BlobStore mockStore = mock();
        when(this.blobStoreProvider.get()).thenReturn(mockStore);

        // Create a populated properties bean
        S3BlobStoreProperties properties = new S3BlobStoreProperties();
        properties.setBucket(BUCKET);
        properties.setKeyPrefix(PREFIX);
        properties.setMultipartUploadPartSize(VALID_MULTIPART_UPLOAD_SIZE);
        properties.setMultipartCopyPartSize(VALID_MULTIPART_COPY_SIZE);

        BlobStore result = this.factory.create(NAME, properties);

        assertSame(mockStore, result);
        verify(this.s3Client).headBucket(any(HeadBucketRequest.class));
        verify(mockStore).initialize(NAME, properties);
        assertEquals("Created S3 blob store [mystore] for bucket [test-bucket]",
            this.logCapture.getMessage(0));
    }

    @Test
    void createWithNonExistentBucket()
    {
        NoSuchBucketException noSuchBucketException = NoSuchBucketException.builder()
            .message("Bucket does not exist")
            .build();
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(noSuchBucketException);

        // Create a populated properties bean
        S3BlobStoreProperties properties = new S3BlobStoreProperties();
        properties.setBucket("non-existent-bucket");
        properties.setKeyPrefix(PREFIX);
        properties.setMultipartUploadPartSize(VALID_MULTIPART_UPLOAD_SIZE);
        properties.setMultipartCopyPartSize(VALID_MULTIPART_COPY_SIZE);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.factory.create(NAME, properties));

        assertThat(exception.getMessage(), containsString("S3 bucket does not exist"));
    }

    @Test
    void createWithAccessDenied()
    {
        AwsServiceException s3Exception = S3Exception.builder()
            .statusCode(403)
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode("AccessDenied")
                .errorMessage("Access Denied")
                .build())
            .build();
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(s3Exception);

        // Create a populated properties bean
        S3BlobStoreProperties properties = new S3BlobStoreProperties();
        properties.setBucket("forbidden-bucket");
        properties.setKeyPrefix(PREFIX);
        properties.setMultipartUploadPartSize(VALID_MULTIPART_UPLOAD_SIZE);
        properties.setMultipartCopyPartSize(VALID_MULTIPART_COPY_SIZE);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.factory.create(NAME, properties));

        assertThat(exception.getMessage(), containsString("Access denied to S3 bucket"));
        assertThat(exception.getMessage(), containsString("credentials and bucket permissions"));
    }

    @Test
    void createWithOtherS3Exception()
    {
        AwsServiceException s3Exception = S3Exception.builder()
            .statusCode(500)
            .awsErrorDetails(AwsErrorDetails.builder()
                .errorCode("InternalError")
                .errorMessage("Internal Server Error")
                .build())
            .build();
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(s3Exception);

        // Create a populated properties bean
        S3BlobStoreProperties properties = new S3BlobStoreProperties();
        properties.setBucket("error-bucket");
        properties.setKeyPrefix(PREFIX);
        properties.setMultipartUploadPartSize(VALID_MULTIPART_UPLOAD_SIZE);
        properties.setMultipartCopyPartSize(VALID_MULTIPART_COPY_SIZE);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.factory.create(NAME, properties));

        assertThat(exception.getMessage(), containsString("Failed to access S3 bucket"));
    }

    @Test
    void createWithUnexpectedException()
    {
        when(this.s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        // Create a populated properties bean
        S3BlobStoreProperties properties = new S3BlobStoreProperties();
        properties.setBucket(BUCKET);
        properties.setKeyPrefix(PREFIX);
        properties.setMultipartUploadPartSize(VALID_MULTIPART_UPLOAD_SIZE);
        properties.setMultipartCopyPartSize(VALID_MULTIPART_COPY_SIZE);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.factory.create(NAME, properties));

        assertThat(exception.getMessage(), containsString("Unexpected error while validating S3 bucket access"));
    }

    @Test
    void createWithWrongPropertiesType()
    {
        // Create properties of wrong type.
        BlobStoreProperties wrongProperties = mock(BlobStoreProperties.class);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.factory.create(NAME, wrongProperties));

        assertEquals("Invalid properties type for S3 blob store factory: " + wrongProperties.getClass().getName(),
            exception.getMessage());
    }
}
