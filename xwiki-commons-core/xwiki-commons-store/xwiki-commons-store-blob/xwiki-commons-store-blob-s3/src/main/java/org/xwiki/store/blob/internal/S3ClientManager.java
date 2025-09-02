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

import java.net.URI;
import java.time.Duration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Component responsible for managing S3 client connections with proper pooling and configuration.
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
@Component(roles = S3ClientManager.class)
@Singleton
public class S3ClientManager implements Initializable, Disposable
{
    @Inject
    private Logger logger;

    @Inject
    private S3BlobStoreConfiguration configuration;

    private S3Client s3Client;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.s3Client = createS3Client();
            this.logger.info("S3 client initialized successfully");
        } catch (Exception e) {
            throw new InitializationException("Failed to initialize S3 client", e);
        }
    }

    @Override
    public void dispose()
    {
        if (this.s3Client != null) {
            this.s3Client.close();
            this.logger.info("S3 client disposed");
        }
    }

    /**
     * Get the configured S3 client.
     *
     * @return the S3 client instance
     */
    public S3Client getS3Client()
    {
        return this.s3Client;
    }

    private S3Client createS3Client()
    {
        S3ClientBuilder builder = S3Client.builder();

        // Configure region
        String region = this.configuration.getS3Region();
        if (StringUtils.isNotBlank(region)) {
            builder.region(Region.of(region));
        }

        // Configure credentials
        AwsCredentialsProvider credentialsProvider = createCredentialsProvider();
        builder.credentialsProvider(credentialsProvider);

        // Configure endpoint (for S3-compatible services like MinIO)
        String endpoint = this.configuration.getS3Endpoint();
        if (StringUtils.isNotBlank(endpoint)) {
            builder.endpointOverride(URI.create(endpoint));
        }

        // Configure S3-specific settings
        S3Configuration s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(this.configuration.isS3PathStyleAccess())
            .build();
        builder.serviceConfiguration(s3Config);

        // Configure HTTP client with connection pooling
        SdkHttpClient httpClient = ApacheHttpClient.builder()
            .maxConnections(this.configuration.getS3MaxConnections())
            .connectionTimeout(Duration.ofMillis(this.configuration.getS3ConnectionTimeout()))
            .socketTimeout(Duration.ofMillis(this.configuration.getS3SocketTimeout()))
            .build();
        builder.httpClient(httpClient);

        // Configure timeouts and retry strategy
        builder.overrideConfiguration(c -> c
            .apiCallTimeout(Duration.ofMillis(this.configuration.getS3RequestTimeout()))
            .retryStrategy(retryStrategy -> retryStrategy.maxAttempts(this.configuration.getS3MaxRetries())));

        return builder.build();
    }

    private AwsCredentialsProvider createCredentialsProvider()
    {
        String accessKey = this.configuration.getS3AccessKey();
        String secretKey = this.configuration.getS3SecretKey();

        if (StringUtils.isNotBlank(accessKey) && StringUtils.isNotBlank(secretKey)) {
            // Use explicit credentials from configuration
            AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            return StaticCredentialsProvider.create(credentials);
        } else {
            // Use default credential provider chain (environment variables, instance profile, etc.)
            this.logger.info("Using default AWS credentials provider chain");
            return DefaultCredentialsProvider.builder().build();
        }
    }
}
