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

import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobOption;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobRangeOption;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobWriteMode;
import org.xwiki.store.blob.S3BlobStoreProperties;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for S3Blob.
 *
 * @version $Id$
 */
@ExtendWith(MockitoExtension.class)
class S3BlobTest
{
    private static final String BUCKET = "bucket";

    private static final String KEY = "key";

    private static final BlobPath BLOB_PATH = BlobPath.absolute("my", "blob.txt");

    @Mock
    private S3Client s3Client;

    @Mock
    private S3BlobStore store;

    private S3Blob blob;

    @BeforeEach
    void setUp()
    {
        this.blob = new S3Blob(BLOB_PATH, BUCKET, KEY, this.store, this.s3Client);
    }

    @Test
    void existsWhenObjectExists() throws BlobStoreException
    {
        HeadObjectResponse response = HeadObjectResponse.builder().build();
        when(this.s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(response);

        boolean exists = this.blob.exists();

        assertTrue(exists);
        ArgumentCaptor<HeadObjectRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).headObject(captor.capture());
        HeadObjectRequest request = captor.getValue();
        assertEquals(BUCKET, request.bucket());
        assertEquals(KEY, request.key());
    }

    @Test
    void existsWhenObjectMissing() throws BlobStoreException
    {
        when(this.s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("missing").build());

        boolean exists = this.blob.exists();

        assertFalse(exists);
    }

    @Test
    void existsWhenS3Exception()
    {
        when(this.s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(S3Exception.builder().message("error").statusCode(500).build());

        assertThrows(BlobStoreException.class, () -> this.blob.exists());
    }

    @Test
    void getSizeWhenPresent() throws BlobStoreException
    {
        HeadObjectResponse response = HeadObjectResponse.builder().contentLength(42L).build();
        when(this.s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(response);

        long size = this.blob.getSize();

        assertEquals(42L, size);
    }

    @Test
    void getSizeWhenMissing() throws BlobStoreException
    {
        when(this.s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("missing").build());

        long size = this.blob.getSize();

        assertEquals(-1L, size);
    }

    @Test
    void getSizeWhenS3Exception()
    {
        when(this.s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(S3Exception.builder().message("error").statusCode(500).build());

        assertThrows(BlobStoreException.class, () -> this.blob.getSize());
    }

    @Test
    void getOutputStreamReturnsS3BlobOutputStream() throws BlobStoreException
    {
        S3BlobStoreProperties properties = new S3BlobStoreProperties();
        properties.setMultipartUploadPartSize(5 * 1024 * 1024);
        when(this.store.getProperties()).thenReturn(properties);

        try (MockedConstruction<S3BlobOutputStream> mockedOutputStream = mockConstruction(S3BlobOutputStream.class,
            (mock, context) -> {
                assertEquals(BUCKET, context.arguments().get(0));
                assertEquals(KEY, context.arguments().get(1));
                assertSame(this.s3Client, context.arguments().get(2));
                assertEquals(BLOB_PATH, context.arguments().get(3));
                assertEquals(5 * 1024 * 1024L, context.arguments().get(4));
                BlobOption[] options = (BlobOption[]) context.arguments().get(5);
                assertEquals(1, options.length);
                assertInstanceOf(BlobWriteMode.class, options[0]);
            })) {

            OutputStream outputStream = this.blob.getOutputStream(BlobWriteMode.CREATE_NEW);

            assertThat(outputStream, instanceOf(S3BlobOutputStream.class));
            assertEquals(1, mockedOutputStream.constructed().size());
        }
    }

    @Test
    void getStreamWhenPresent() throws BlobStoreException
    {
        ResponseInputStream<GetObjectResponse> responseStream = mock();
        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        InputStream actual = this.blob.getStream();

        assertSame(responseStream, actual);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).getObject(captor.capture());
        GetObjectRequest request = captor.getValue();
        assertEquals(BUCKET, request.bucket());
        assertEquals(KEY, request.key());
        assertNull(request.range());
    }

    @Test
    void getStreamWhenMissing()
    {
        when(this.s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("missing").build());

        assertThrows(BlobNotFoundException.class, () -> this.blob.getStream());
    }

    @Test
    void getStreamWithRangeFromOffset() throws BlobStoreException
    {
        ResponseInputStream<GetObjectResponse> responseStream = mock();
        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        InputStream actual = this.blob.getStream(BlobRangeOption.from(128));

        assertSame(responseStream, actual);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).getObject(captor.capture());
        GetObjectRequest request = captor.getValue();
        assertEquals("bytes=128-", request.range());
    }

    @Test
    void getStreamWithRangeAndLength() throws BlobStoreException
    {
        ResponseInputStream<GetObjectResponse> responseStream = mock();
        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        InputStream actual = this.blob.getStream(BlobRangeOption.withLength(256, 512));

        assertSame(responseStream, actual);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.captor();
        verify(this.s3Client).getObject(captor.capture());
        GetObjectRequest request = captor.getValue();
        assertEquals("bytes=256-767", request.range());
    }

    @Test
    void getStreamWhenS3Exception()
    {
        AwsServiceException exception = S3Exception.builder().message("error").statusCode(500).build();
        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenThrow(exception);

        assertThrows(BlobStoreException.class, () -> this.blob.getStream());
    }

    @Test
    void equalsAndHashCode()
    {
        S3Blob sameBlob = new S3Blob(BLOB_PATH, BUCKET, KEY, this.store, this.s3Client);
        S3Blob onlySameBucketKey = new S3Blob(BlobPath.absolute("other", "blob.txt"), BUCKET, KEY, mock(),
            mock());
        S3Blob differentBucketBlob = new S3Blob(BLOB_PATH, "other-bucket", KEY, this.store, this.s3Client);
        S3Blob differentKeyBlob = new S3Blob(BLOB_PATH, BUCKET, "other-key", this.store, this.s3Client);

        assertEquals(this.blob, sameBlob);
        assertEquals(this.blob.hashCode(), sameBlob.hashCode());

        assertEquals(this.blob, onlySameBucketKey);
        assertEquals(this.blob.hashCode(), onlySameBucketKey.hashCode());

        assertNotEquals(this.blob, differentBucketBlob);
        assertNotEquals(this.blob.hashCode(), differentBucketBlob.hashCode());

        assertNotEquals(this.blob, differentKeyBlob);
        assertNotEquals(this.blob.hashCode(), differentKeyBlob.hashCode());
    }
}
