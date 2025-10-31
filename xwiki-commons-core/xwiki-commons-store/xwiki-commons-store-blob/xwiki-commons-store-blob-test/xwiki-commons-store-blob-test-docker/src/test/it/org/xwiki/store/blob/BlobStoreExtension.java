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
package org.xwiki.store.blob;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.store.blob.internal.S3BlobStoreConfiguration;
import org.xwiki.test.junit5.mockito.MockitoComponentManagerExtension;
import org.xwiki.test.mockito.MockitoComponentManager;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import static org.mockito.Mockito.when;

/**
 * JUnit 5 extension that manages MinIO container lifecycle for blob store integration tests.
 * Can use either a MinIO container or an external S3-compatible endpoint configured via system properties.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class BlobStoreExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver, BeforeEachCallback
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreExtension.class);

    private static final String MINIO_IMAGE = "minio/minio:latest";

    private static final String DEFAULT_BUCKET = "test-bucket";

    private static final String PROP_ENDPOINT = "xwiki.test.blobstore.s3.endpoint";

    private static final String PROP_BUCKET = "xwiki.test.blobstore.s3.bucket";

    private static final String PROP_ACCESS_KEY = "xwiki.test.blobstore.s3.accessKey";

    private static final String PROP_SECRET_KEY = "xwiki.test.blobstore.s3.secretKey";

    private static final String PROP_REGION = "xwiki.test.blobstore.s3.region";

    private static final String PROP_PATH_STYLE = "xwiki.test.blobstore.s3.pathStyleAccess";

    private static final ExtensionContext.Namespace NAMESPACE =
        ExtensionContext.Namespace.create(BlobStoreExtension.class);

    @Override
    public void beforeAll(ExtensionContext extensionContext)
    {
        if (isInNestedTest(extensionContext)) {
            return;
        }

        BlobStoreContainer container;
        String externalEndpoint = System.getProperty(PROP_ENDPOINT);

        if (externalEndpoint != null) {
            LOGGER.info("(*) Using external S3 endpoint: {}", externalEndpoint);
            container = createExternalContainer(externalEndpoint);
        } else {
            LOGGER.info("(*) Starting MinIO container...");
            container = createMinIOContainer(extensionContext);
        }

        // Create the test bucket
        createBucket(container);

        saveContainer(extensionContext, container);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext)
    {
        // Inject the container instance into the @InjectBlobStoreContainer fields from the test class
        Optional<TestInstances> testInstances = extensionContext.getTestInstances();
        if (testInstances.isPresent()) {
            // Initialize all test classes, including nested ones.
            for (Object testInstance : testInstances.get().getAllInstances()) {
                for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
                    if (field.isAnnotationPresent(InjectBlobStoreContainer.class)) {
                        ReflectionUtils.setFieldValue(testInstance, field.getName(), loadContainer(extensionContext));
                    }
                }
            }
        }

        // Register mock S3BlobStoreConfiguration with the component manager
        setupMockConfiguration(extensionContext);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext)
    {
        if (isInNestedTest(extensionContext)) {
            return;
        }

        MinIOContainer minioContainer = loadMinIOContainer(extensionContext);
        if (minioContainer != null) {
            LOGGER.info("(*) Stopping MinIO container...");
            minioContainer.stop();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        Class<?> type = parameterContext.getParameter().getType();
        return BlobStoreContainer.class.isAssignableFrom(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return loadContainer(extensionContext);
    }

    private BlobStoreContainer createExternalContainer(String endpoint)
    {
        String bucket = System.getProperty(PROP_BUCKET, DEFAULT_BUCKET);
        String accessKey = System.getProperty(PROP_ACCESS_KEY, "minioadmin");
        String secretKey = System.getProperty(PROP_SECRET_KEY, "minioadmin");
        String region = System.getProperty(PROP_REGION, "us-east-1");
        boolean pathStyleAccess = Boolean.parseBoolean(System.getProperty(PROP_PATH_STYLE, "true"));

        return new BlobStoreContainer(endpoint, bucket, accessKey, secretKey, region, pathStyleAccess);
    }

    private BlobStoreContainer createMinIOContainer(ExtensionContext context)
    {
        DockerImageName imageName = DockerImageName.parse(MINIO_IMAGE);
        MinIOContainer minioContainer = new MinIOContainer(imageName);
        minioContainer.start();

        // Save the MinIO container separately so we can stop it later
        getStore(context).put(MinIOContainer.class, minioContainer);

        return new BlobStoreContainer(
            minioContainer.getS3URL(),
            DEFAULT_BUCKET,
            minioContainer.getUserName(),
            minioContainer.getPassword(),
            "us-east-1",
            true
        );
    }

    private void createBucket(BlobStoreContainer container)
    {
        try (S3Client s3Client = S3Client.builder()
            .endpointOverride(URI.create(container.endpoint()))
            .region(Region.of(container.region()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(container.accessKey(), container.secretKey())))
            .forcePathStyle(container.pathStyleAccess())
            .build())
        {
            // Check if bucket exists
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(container.bucketName()).build());
                LOGGER.info("(*) Bucket '{}' already exists", container.bucketName());
            } catch (Exception e) {
                // Bucket doesn't exist, create it
                s3Client.createBucket(CreateBucketRequest.builder().bucket(container.bucketName()).build());
                LOGGER.info("(*) Created bucket '{}'", container.bucketName());
            }
        }
    }

    private static ExtensionContext.Store getStore(ExtensionContext context)
    {
        return context.getRoot().getStore(NAMESPACE);
    }

    private void saveContainer(ExtensionContext context, BlobStoreContainer container)
    {
        getStore(context).put(BlobStoreContainer.class, container);
    }

    private BlobStoreContainer loadContainer(ExtensionContext context)
    {
        return getStore(context).get(BlobStoreContainer.class, BlobStoreContainer.class);
    }

    private MinIOContainer loadMinIOContainer(ExtensionContext context)
    {
        return getStore(context).get(MinIOContainer.class, MinIOContainer.class);
    }

    private boolean isInNestedTest(ExtensionContext context)
    {
        // This method is going to be called for the top level test class but also for nested test classes. We want
        // to start MinIO only once, and thus we only start it for the top level context.
        // Note: the top level context is the JUnitJupiterExtensionContext one, and it doesn't contain any test, and
        // thus we skip it.
        return context.getParent().get().getParent().isPresent();
    }

    private void setupMockConfiguration(ExtensionContext context)
    {
        try {
            MockitoComponentManager componentManager = MockitoComponentManagerExtension.loadComponentManager(context);
            if (componentManager != null) {
                BlobStoreContainer container = loadContainer(context);

                S3BlobStoreConfiguration configuration =
                    componentManager.registerMockComponent(S3BlobStoreConfiguration.class);

                // Configure the mock with container settings
                when(configuration.getS3Endpoint()).thenReturn(container.endpoint());
                when(configuration.getS3AccessKey()).thenReturn(container.accessKey());
                when(configuration.getS3SecretKey()).thenReturn(container.secretKey());
                when(configuration.getS3Region()).thenReturn(container.region());
                when(configuration.isS3PathStyleAccess()).thenReturn(container.pathStyleAccess());

                // Set multipart sizes to 5MB
                when(configuration.getS3MultipartPartUploadSizeBytes()).thenReturn(5L * 1024 * 1024);
                when(configuration.getS3MultipartCopySizeBytes()).thenReturn(5L * 1024 * 1024);

                // Connection settings
                when(configuration.getS3MaxConnections()).thenReturn(50);
                when(configuration.getS3ConnectionTimeout()).thenReturn(10000);
                when(configuration.getS3SocketTimeout()).thenReturn(50000);
                when(configuration.getS3RequestTimeout()).thenReturn(300000);
                when(configuration.getS3MaxRetries()).thenReturn(3);
                when(configuration.getS3KeyPrefix()).thenReturn("");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup mock S3BlobStoreConfiguration", e);
        }
    }
}
