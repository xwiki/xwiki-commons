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
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.api.RetryStrategy;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3ClientManager}.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList(S3ClientManager.class)
class S3ClientManagerTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private S3BlobStoreConfiguration configuration;

    @RegisterExtension
    private final LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    /**
     * Helper method to configure the mock with standard test values.
     */
    private void configureFullConfiguration()
    {
        when(this.configuration.getS3Region()).thenReturn("us-west-2");
        when(this.configuration.getS3AccessKey()).thenReturn("accesskey");
        when(this.configuration.getS3SecretKey()).thenReturn("secretkey");
        when(this.configuration.getS3Endpoint()).thenReturn("https://s3.amazonaws.com");
        when(this.configuration.isS3PathStyleAccess()).thenReturn(true);
        when(this.configuration.getS3MaxConnections()).thenReturn(50);
        when(this.configuration.getS3ConnectionTimeout()).thenReturn(5000);
        when(this.configuration.getS3SocketTimeout()).thenReturn(10000);
        when(this.configuration.getS3RequestTimeout()).thenReturn(15000);
        when(this.configuration.getS3MaxRetries()).thenReturn(3);
    }

    /**
     * Helper method to configure the mock with minimal values.
     * Uses us-east-1 as a valid default region to avoid SDK auto-detection failures.
     */
    private void configureMinimalConfiguration()
    {
        when(this.configuration.getS3Region()).thenReturn("us-east-1");
        when(this.configuration.getS3AccessKey()).thenReturn("");
        when(this.configuration.getS3SecretKey()).thenReturn("");
        when(this.configuration.getS3Endpoint()).thenReturn("");
        when(this.configuration.isS3PathStyleAccess()).thenReturn(false);
        when(this.configuration.getS3MaxConnections()).thenReturn(10);
        when(this.configuration.getS3ConnectionTimeout()).thenReturn(3000);
        when(this.configuration.getS3SocketTimeout()).thenReturn(5000);
        when(this.configuration.getS3RequestTimeout()).thenReturn(10000);
        when(this.configuration.getS3MaxRetries()).thenReturn(2);
    }

    /**
     * Helper method to verify path-style access by examining generated URLs.
     * Path-style: https://endpoint/bucket/key
     * Virtual-hosted-style: https://bucket.endpoint/key
     */
    private void assertPathStyleAccess(S3Client s3Client, boolean expectedPathStyle)
    {
        GetUrlRequest request = GetUrlRequest.builder()
            .bucket("test-bucket")
            .key("test-key")
            .build();

        URL url = s3Client.utilities().getUrl(request);
        String urlString = url.toString();

        if (expectedPathStyle) {
            // Path-style: bucket name should be in the path
            assertTrue(urlString.contains("/test-bucket/test-key"),
                "Expected path-style URL containing '/test-bucket/test-key', got: " + urlString);
        } else {
            // Virtual-hosted-style: bucket name should be in the hostname
            assertTrue(url.getHost().startsWith("test-bucket."),
                "Expected virtual-hosted-style URL with 'test-bucket.' in hostname, got: " + urlString);
        }
    }

    @Test
    void initializeWithFullConfigurationAndVerifyClientConfiguration() throws Exception
    {
        configureFullConfiguration();

        S3ClientManager s3ClientManager = this.componentManager.getInstance(S3ClientManager.class);
        S3Client s3Client = s3ClientManager.getS3Client();

        // Verify the client is created and cached
        assertNotNull(s3Client);
        assertSame(s3Client, s3ClientManager.getS3Client());

        // Verify actual client configuration
        var clientConfig = s3Client.serviceClientConfiguration();

        // Verify region
        assertEquals(Region.US_WEST_2, clientConfig.region());

        // Verify endpoint override
        assertTrue(clientConfig.endpointOverride().isPresent());
        assertEquals(URI.create("https://s3.amazonaws.com"), clientConfig.endpointOverride().get());

        // Verify path style access through URL generation
        assertPathStyleAccess(s3Client, true);

        // Verify retry strategy
        assertTrue(clientConfig.overrideConfiguration().retryStrategy().isPresent());
        RetryStrategy retryStrategy = clientConfig.overrideConfiguration().retryStrategy().get();
        assertEquals(3, retryStrategy.maxAttempts());

        // Verify request timeout
        assertTrue(clientConfig.overrideConfiguration().apiCallTimeout().isPresent());
        assertEquals(15000, clientConfig.overrideConfiguration().apiCallTimeout().get().toMillis());

        assertEquals("S3 client initialized successfully", this.logCapture.getMessage(0));

        s3ClientManager.dispose();
        assertEquals("S3 client disposed", this.logCapture.getMessage(1));
    }

    @Test
    void initializeWithMinimalConfigurationAndVerifyDefaults() throws Exception
    {
        configureMinimalConfiguration();

        S3ClientManager s3ClientManager = this.componentManager.getInstance(S3ClientManager.class);
        S3Client s3Client = s3ClientManager.getS3Client();

        assertNotNull(s3Client);

        var clientConfig = s3Client.serviceClientConfiguration();

        // Verify region is set to us-east-1
        assertEquals(Region.US_EAST_1, clientConfig.region());

        // Verify no endpoint override
        assertTrue(clientConfig.endpointOverride().isEmpty());

        // Verify path style access is disabled (virtual-hosted-style)
        assertPathStyleAccess(s3Client, false);

        // Verify retry strategy with minimal config
        assertTrue(clientConfig.overrideConfiguration().retryStrategy().isPresent());
        RetryStrategy retryStrategy = clientConfig.overrideConfiguration().retryStrategy().get();
        assertEquals(2, retryStrategy.maxAttempts());

        // Verify request timeout
        assertTrue(clientConfig.overrideConfiguration().apiCallTimeout().isPresent());
        assertEquals(10000, clientConfig.overrideConfiguration().apiCallTimeout().get().toMillis());

        assertEquals("Using default AWS credentials provider chain", this.logCapture.getMessage(0));
        assertEquals("S3 client initialized successfully", this.logCapture.getMessage(1));

        s3ClientManager.dispose();
        assertEquals("S3 client disposed", this.logCapture.getMessage(2));
    }

    @Test
    void clientIsCachedAndReused() throws Exception
    {
        configureMinimalConfiguration();

        S3ClientManager s3ClientManager = this.componentManager.getInstance(S3ClientManager.class);

        S3Client firstCall = s3ClientManager.getS3Client();
        S3Client secondCall = s3ClientManager.getS3Client();
        S3Client thirdCall = s3ClientManager.getS3Client();

        assertNotNull(firstCall);
        assertSame(firstCall, secondCall);
        assertSame(firstCall, thirdCall);

        // Only one initialization log message
        assertEquals(2, this.logCapture.size());
        assertEquals("Using default AWS credentials provider chain", this.logCapture.getMessage(0));
        assertEquals("S3 client initialized successfully", this.logCapture.getMessage(1));

        s3ClientManager.dispose();
        assertEquals("S3 client disposed", this.logCapture.getMessage(2));
    }
}
