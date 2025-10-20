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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3BlobIterator}.
 *
 * @version $Id$
 */
@ExtendWith(MockitoExtension.class)
class S3BlobIteratorTest
{
    private static final String BUCKET_NAME = "test-bucket";

    private static final String PREFIX = "test/prefix/";

    private static final int PAGE_SIZE = 10;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3BlobStore store;

    private S3BlobIterator iterator;

    @BeforeEach
    void setUp()
    {
        this.iterator = new S3BlobIterator(PREFIX, BUCKET_NAME, PAGE_SIZE, this.s3Client, this.store);
    }

    @Test
    void iterateWithSinglePage()
    {
        // Create mock S3 objects
        S3Object obj1 = createS3Object("test/prefix/file1.txt");
        S3Object obj2 = createS3Object("test/prefix/file2.txt");

        // Mock the response
        ListObjectsV2Response response = ListObjectsV2Response.builder()
            .contents(obj1, obj2)
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // Mock blob path conversion
        BlobPath path1 = mock();
        BlobPath path2 = mock();
        when(this.store.s3KeyToBlobPath("test/prefix/file1.txt")).thenReturn(path1);
        when(this.store.s3KeyToBlobPath("test/prefix/file2.txt")).thenReturn(path2);

        // Verify iteration
        assertTrue(this.iterator.hasNext());
        Blob blob1 = this.iterator.next();
        assertNotNull(blob1);
        assertEquals(path1, blob1.getPath());

        assertTrue(this.iterator.hasNext());
        Blob blob2 = this.iterator.next();
        assertNotNull(blob2);
        assertEquals(path2, blob2.getPath());

        assertFalse(this.iterator.hasNext());

        // Verify S3 client was called once
        verify(this.s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void iterateWithMultiplePages()
    {
        // First page
        S3Object obj1 = createS3Object("test/prefix/file1.txt");
        ListObjectsV2Response response1 = ListObjectsV2Response.builder()
            .contents(obj1)
            .isTruncated(true)
            .nextContinuationToken("token1")
            .build();

        // Second page
        S3Object obj2 = createS3Object("test/prefix/file2.txt");
        ListObjectsV2Response response2 = ListObjectsV2Response.builder()
            .contents(obj2)
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(response1)
            .thenReturn(response2);

        // Mock blob path conversion
        BlobPath path1 = mock();
        BlobPath path2 = mock();
        when(this.store.s3KeyToBlobPath("test/prefix/file1.txt")).thenReturn(path1);
        when(this.store.s3KeyToBlobPath("test/prefix/file2.txt")).thenReturn(path2);

        // Verify iteration
        assertTrue(this.iterator.hasNext());
        Blob blob1 = this.iterator.next();
        assertNotNull(blob1);

        assertTrue(this.iterator.hasNext());
        Blob blob2 = this.iterator.next();
        assertNotNull(blob2);

        assertFalse(this.iterator.hasNext());

        // Verify S3 client was called twice
        ArgumentCaptor<ListObjectsV2Request> captor = ArgumentCaptor.captor();
        verify(this.s3Client, times(2)).listObjectsV2(captor.capture());

        List<ListObjectsV2Request> requests = captor.getAllValues();
        assertEquals(2, requests.size());
        assertEquals(PREFIX, requests.get(0).prefix());
        assertEquals(BUCKET_NAME, requests.get(0).bucket());
        assertEquals(PAGE_SIZE, requests.get(0).maxKeys());
        assertEquals("token1", requests.get(1).continuationToken());
    }

    @Test
    void skipDirectories()
    {
        // Create mock S3 objects including a directory
        S3Object obj1 = createS3Object("test/prefix/file1.txt");
        S3Object directory = createS3Object("test/prefix/subdir/");
        S3Object obj2 = createS3Object("test/prefix/file2.txt");

        ListObjectsV2Response response = ListObjectsV2Response.builder()
            .contents(obj1, directory, obj2)
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // Mock blob path conversion
        BlobPath path1 = mock();
        BlobPath path2 = mock();
        when(this.store.s3KeyToBlobPath("test/prefix/file1.txt")).thenReturn(path1);
        when(this.store.s3KeyToBlobPath("test/prefix/file2.txt")).thenReturn(path2);

        // Verify iteration - directory should be skipped
        List<Blob> blobs = new ArrayList<>();
        while (this.iterator.hasNext()) {
            blobs.add(this.iterator.next());
        }

        assertEquals(2, blobs.size());
        assertEquals(path1, blobs.get(0).getPath());
        assertEquals(path2, blobs.get(1).getPath());
    }

    @Test
    void skipInvalidBlobPaths()
    {
        // Create mock S3 objects
        S3Object obj1 = createS3Object("test/prefix/file1.txt");
        S3Object obj2 = createS3Object("test/prefix/invalid.txt");
        S3Object obj3 = createS3Object("test/prefix/file3.txt");

        ListObjectsV2Response response = ListObjectsV2Response.builder()
            .contents(obj1, obj2, obj3)
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // Mock blob path conversion - obj2 returns null (invalid path)
        BlobPath path1 = mock();
        BlobPath path3 = mock();
        when(this.store.s3KeyToBlobPath("test/prefix/file1.txt")).thenReturn(path1);
        when(this.store.s3KeyToBlobPath("test/prefix/invalid.txt")).thenReturn(null);
        when(this.store.s3KeyToBlobPath("test/prefix/file3.txt")).thenReturn(path3);

        // Verify iteration - invalid path should be skipped
        List<Blob> blobs = new ArrayList<>();
        while (this.iterator.hasNext()) {
            blobs.add(this.iterator.next());
        }

        assertEquals(2, blobs.size());
        assertEquals(path1, blobs.get(0).getPath());
        assertEquals(path3, blobs.get(1).getPath());
    }

    @Test
    void emptyResults()
    {
        ListObjectsV2Response response = ListObjectsV2Response.builder()
            .contents(List.of())
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        assertFalse(this.iterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> this.iterator.next());
    }

    @Test
    void nextWithoutHasNext()
    {
        S3Object obj1 = createS3Object("test/prefix/file1.txt");
        ListObjectsV2Response response = ListObjectsV2Response.builder()
            .contents(obj1)
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        BlobPath path1 = mock();
        when(this.store.s3KeyToBlobPath("test/prefix/file1.txt")).thenReturn(path1);

        // Call next() without hasNext()
        Blob blob = this.iterator.next();
        assertNotNull(blob);
        assertEquals(path1, blob.getPath());

        assertThrows(NoSuchElementException.class, () -> this.iterator.next());
    }

    @Test
    void multipleHasNextCalls()
    {
        S3Object obj1 = createS3Object("test/prefix/file1.txt");
        ListObjectsV2Response response = ListObjectsV2Response.builder()
            .contents(obj1)
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        BlobPath path1 = mock();
        when(this.store.s3KeyToBlobPath("test/prefix/file1.txt")).thenReturn(path1);

        // Multiple hasNext() calls should not advance the iterator
        assertTrue(this.iterator.hasNext());
        assertTrue(this.iterator.hasNext());
        assertTrue(this.iterator.hasNext());

        Blob blob = this.iterator.next();
        assertNotNull(blob);

        assertFalse(this.iterator.hasNext());
        assertFalse(this.iterator.hasNext());

        // Verify S3 client was called only once
        verify(this.s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void s3ExceptionDuringFetch()
    {
        AwsServiceException exception = S3Exception.builder()
            .message("Access denied")
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(exception);

        assertThrows(S3Exception.class, () -> this.iterator.hasNext());
    }

    @Test
    void emptyPageFollowedByNonEmptyPage()
    {
        // First page is empty but truncated
        ListObjectsV2Response response1 = ListObjectsV2Response.builder()
            .contents(List.of())
            .isTruncated(true)
            .nextContinuationToken("token1")
            .build();

        // Second page has content
        S3Object obj1 = createS3Object("test/prefix/file1.txt");
        ListObjectsV2Response response2 = ListObjectsV2Response.builder()
            .contents(obj1)
            .isTruncated(false)
            .build();

        when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(response1)
            .thenReturn(response2);

        BlobPath path1 = mock();
        when(this.store.s3KeyToBlobPath("test/prefix/file1.txt")).thenReturn(path1);

        assertTrue(this.iterator.hasNext());
        Blob blob = this.iterator.next();
        assertNotNull(blob);
        assertEquals(path1, blob.getPath());

        assertFalse(this.iterator.hasNext());

        // Verify both pages were fetched
        verify(this.s3Client, times(2)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    private S3Object createS3Object(String key)
    {
        return S3Object.builder()
            .key(key)
            .build();
    }
}
