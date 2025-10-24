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

import jakarta.inject.Named;
import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3BlobStoreConfiguration}.
 *
 * @version $Id$
 */
@ComponentTest
class S3BlobStoreConfigurationTest
{
    private static final String MULTIPART_PROP = "store.s3.multipartPartUploadSizeMB";

    private static final String MULTIPART_COPY_PROP = "store.s3.multipartCopySizeMB";

    @InjectMockComponents
    private S3BlobStoreConfiguration configuration;

    @MockComponent
    @Named("xwikiproperties")
    private ConfigurationSource configurationSource;

    @Test
    void getS3MultipartPartUploadSizeBytesDefault()
    {
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(5);

        assertEquals(S3MultipartUploadHelper.MIN_PART_SIZE, this.configuration.getS3MultipartPartUploadSizeBytes());
    }

    @Test
    void getS3MultipartPartUploadSizeBytesInRange()
    {
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(10);

        assertEquals(10L * 1024L * 1024L, this.configuration.getS3MultipartPartUploadSizeBytes());
    }

    @Test
    void getS3MultipartPartUploadSizeBytesBelowMin()
    {
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(1);

        assertEquals(S3MultipartUploadHelper.MIN_PART_SIZE, this.configuration.getS3MultipartPartUploadSizeBytes());
    }

    @Test
    void getS3MultipartPartUploadSizeBytesAboveMax()
    {
        // 6000 MB is larger than the 5120 MB corresponding to the 5 GB MAX_PART_SIZE
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(6000);

        assertEquals(S3MultipartUploadHelper.MAX_PART_SIZE, this.configuration.getS3MultipartPartUploadSizeBytes());
    }

    @Test
    void getS3MultipartCopySizeBytesDefault()
    {
        when(this.configurationSource.getProperty(MULTIPART_COPY_PROP, 512)).thenReturn(512);
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(5);

        assertEquals(512L * 1024L * 1024L, this.configuration.getS3MultipartCopySizeBytes());
    }

    @Test
    void getS3MultipartCopySizeBytesInRange()
    {
        when(this.configurationSource.getProperty(MULTIPART_COPY_PROP, 512)).thenReturn(256);
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(5);

        assertEquals(256L * 1024L * 1024L, this.configuration.getS3MultipartCopySizeBytes());
    }

    @Test
    void getS3MultipartCopySizeBytesBelowMin()
    {
        when(this.configurationSource.getProperty(MULTIPART_COPY_PROP, 512)).thenReturn(1);
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(5);

        assertEquals(S3MultipartUploadHelper.MIN_PART_SIZE, this.configuration.getS3MultipartCopySizeBytes());
    }

    @Test
    void getS3MultipartCopySizeBytesAboveMax()
    {
        // 6000 MB is larger than the 5120 MB corresponding to the 5 GB MAX_PART_SIZE
        when(this.configurationSource.getProperty(MULTIPART_COPY_PROP, 512)).thenReturn(6000);
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(5);

        assertEquals(S3MultipartUploadHelper.MAX_PART_SIZE, this.configuration.getS3MultipartCopySizeBytes());
    }

    @Test
    void getS3MultipartCopySizeBytesAtLeastUploadPartSize()
    {
        // Configure the copy size to a small value (which would be clamped to MIN_PART_SIZE),
        // but configure the upload part size to a larger value; the result should be the upload part size.
        when(this.configurationSource.getProperty(MULTIPART_COPY_PROP, 512)).thenReturn(1);
        when(this.configurationSource.getProperty(MULTIPART_PROP, 5)).thenReturn(10);

        long expectedUploadPartSize = 10L * 1024L * 1024L;
        assertEquals(expectedUploadPartSize, this.configuration.getS3MultipartCopySizeBytes());
    }
}
