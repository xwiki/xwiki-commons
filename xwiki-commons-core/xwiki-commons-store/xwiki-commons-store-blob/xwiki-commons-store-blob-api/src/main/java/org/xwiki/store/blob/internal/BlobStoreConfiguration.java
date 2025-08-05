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

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Configuration for the Blob Store.
 *
 * @version $Id$
 */
@Component
public class BlobStoreConfiguration
{
    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configurationSource;

    /**
     * @return the hint for the blob store type to use, e.g., "filesystem" or "s3". This is used to determine which blob
     *     store manager to use when creating a new blob store.
     */
    public String getStoreHint()
    {
        return this.configurationSource.getProperty("store.blobStoreHint", "filesystem");
    }

    // S3 Configuration properties

    /**
     * @return the name of the S3 bucket where blobs will be stored
     */
    public String getS3BucketName()
    {
        return this.configurationSource.getProperty("store.s3.bucketName");
    }

    /**
     * @return the AWS region where the S3 bucket is located (defaults to "us-east-1")
     */
    public String getS3Region()
    {
        return this.configurationSource.getProperty("store.s3.region", "us-east-1");
    }

    /**
     * @return the AWS access key for authenticating with S3
     */
    public String getS3AccessKey()
    {
        return this.configurationSource.getProperty("store.s3.accessKey");
    }

    /**
     * @return the AWS secret key for authenticating with S3
     */
    public String getS3SecretKey()
    {
        return this.configurationSource.getProperty("store.s3.secretKey");
    }

    /**
     * @return the S3 endpoint URL when using a custom S3-compatible service
     */
    public String getS3Endpoint()
    {
        return this.configurationSource.getProperty("store.s3.endpoint");
    }

    /**
     * @return whether to use path-style access for S3 URLs (defaults to false)
     */
    public boolean isS3PathStyleAccess()
    {
        return this.configurationSource.getProperty("store.s3.pathStyleAccess", false);
    }

    /**
     * @return the maximum number of concurrent connections to S3 (defaults to 50)
     */
    public int getS3MaxConnections()
    {
        return this.configurationSource.getProperty("store.s3.maxConnections", 50);
    }

    /**
     * @return the connection timeout in milliseconds for S3 requests (defaults to 10000)
     */
    public int getS3ConnectionTimeout()
    {
        return this.configurationSource.getProperty("store.s3.connectionTimeout", 10000);
    }

    /**
     * @return the socket timeout in milliseconds for S3 requests (defaults to 50000)
     */
    public int getS3SocketTimeout()
    {
        return this.configurationSource.getProperty("store.s3.socketTimeout", 50000);
    }

    /**
     * @return the request timeout in milliseconds for S3 requests (defaults to 300000)
     */
    public int getS3RequestTimeout()
    {
        return this.configurationSource.getProperty("store.s3.requestTimeout", 300000);
    }

    /**
     * @return the maximum number of retries for failed S3 requests (defaults to 3)
     */
    public int getS3MaxRetries()
    {
        return this.configurationSource.getProperty("store.s3.maxRetries", 3);
    }

    /**
     * @return the key prefix to prepend to all S3 object keys (defaults to "")
     */
    public String getS3KeyPrefix()
    {
        return this.configurationSource.getProperty("store.s3.keyPrefix", "");
    }
}
