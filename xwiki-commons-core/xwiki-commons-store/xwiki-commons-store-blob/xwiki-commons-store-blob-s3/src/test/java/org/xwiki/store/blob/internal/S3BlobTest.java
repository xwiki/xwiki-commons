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
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.store.blob.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStoreException;

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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    private static final BlobPath BLOB_PATH = BlobPath.of(List.of("my", "blob.txt"));

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
        OutputStream outputStream = this.blob.getOutputStream(BlobDoesNotExistCondition.INSTANCE);

        assertThat(outputStream, instanceOf(S3BlobOutputStream.class));
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
    }

    @Test
    void getStreamWhenMissing()
    {
        when(this.s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("missing").build());

        assertThrows(BlobNotFoundException.class, () -> this.blob.getStream());
    }

    @Test
    void getStreamWhenS3Exception()
    {
        AwsServiceException exception = S3Exception.builder().message("error").statusCode(500).build();
        when(this.s3Client.getObject(any(GetObjectRequest.class))).thenThrow(exception);

        assertThrows(BlobStoreException.class, () -> this.blob.getStream());
    }
}
