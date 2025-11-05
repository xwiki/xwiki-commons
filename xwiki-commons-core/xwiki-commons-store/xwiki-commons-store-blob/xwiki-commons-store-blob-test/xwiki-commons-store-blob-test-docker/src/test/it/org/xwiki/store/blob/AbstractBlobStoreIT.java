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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base class for blob store integration tests. Contains all test cases that should work identically
 * on both filesystem and S3 stores.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
abstract class AbstractBlobStoreIT
{
    private final List<BlobStore> createdStores = new ArrayList<>();

    /**
     * Create a blob store instance for testing.
     *
     * @param name the name of the blob store
     * @return the blob store instance
     * @throws Exception if creation fails
     */
    protected abstract BlobStore createBlobStore(String name) throws Exception;

    /**
     * Get or create a blob store for testing, tracking it for cleanup.
     *
     * @param name the name of the blob store
     * @return the blob store instance
     * @throws Exception if creation fails
     */
    protected BlobStore getOrCreateBlobStore(String name) throws Exception
    {
        BlobStore store = createBlobStore(name);
        this.createdStores.add(store);
        return store;
    }

    @AfterEach
    void cleanup() throws Exception
    {
        for (BlobStore store : this.createdStores) {
            store.deleteBlobs(BlobPath.root());
        }
        this.createdStores.clear();
    }

    @Test
    void writeAndReadBlob() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testWriteAndReadBlob");
        BlobPath path = BlobPath.absolute("test.dat");
        byte[] data = BlobStoreTestUtils.createTestData(1024);

        BlobStoreTestUtils.writeBlob(store, path, data);
        BlobStoreTestUtils.assertBlobEquals(store, path, data);
    }

    @Test
    void readBlobRange() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testReadBlobRange");
        BlobPath path = BlobPath.absolute("range.dat");
        byte[] data = BlobStoreTestUtils.createTestData(4096, 12345L);
        BlobStoreTestUtils.writeBlob(store, path, data);

        Blob blob = store.getBlob(path);

        try (InputStream input = blob.getStream(BlobRangeOption.withLength(512, 256))) {
            assertArrayEquals(Arrays.copyOfRange(data, 512, 768), input.readAllBytes());
        }

        try (InputStream input = blob.getStream(BlobRangeOption.from(2048))) {
            assertArrayEquals(Arrays.copyOfRange(data, 2048, data.length), input.readAllBytes());
        }
    }

    @Test
    void blobExists() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testBlobExists");
        BlobPath path = BlobPath.absolute("exists.dat");
        byte[] data = BlobStoreTestUtils.createTestData(100);

        Blob blob = store.getBlob(path);
        assertFalse(blob.exists());

        BlobStoreTestUtils.writeBlob(store, path, data);
        assertTrue(blob.exists());
    }

    @Test
    void listBlobsEmpty() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testListBlobsEmpty");
        BlobPath path = BlobPath.absolute("empty");

        try (Stream<Blob> blobs = store.listBlobs(path)) {
            assertEquals(0, blobs.count());
        }
    }

    @Test
    void listBlobsWithNestedPaths() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testListBlobsWithNestedPaths");
        byte[] data = BlobStoreTestUtils.createTestData(100);

        // Create nested structure
        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("dir1", "file1.dat"), data);
        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("dir1", "file2.dat"), data);
        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("dir1", "subdir", "file3.dat"), data);
        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("dir2", "file4.dat"), data);

        // List all under dir1
        try (Stream<Blob> blobs = store.listBlobs(BlobPath.absolute("dir1"))) {
            assertEquals(3, blobs.count());
        }

        // List all under root
        try (Stream<Blob> blobs = store.listBlobs(BlobPath.absolute(List.of()))) {
            assertEquals(4, blobs.count());
        }
    }

    @Test
    void copyBlobWithinStore() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testCopyBlobWithinStore");
        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
        byte[] data = BlobStoreTestUtils.createTestData(1024);

        BlobStoreTestUtils.writeBlob(store, sourcePath, data);

        Blob copiedBlob = store.copyBlob(sourcePath, targetPath);
        assertNotNull(copiedBlob);
        assertTrue(copiedBlob.exists());

        BlobStoreTestUtils.assertBlobEquals(store, targetPath, data);
        assertTrue(store.getBlob(sourcePath).exists(), "Blob should exist: " + sourcePath);
    }

    @Test
    void copyBlobTargetExists() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("copyBlobTargetExists");
        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
        byte[] data = BlobStoreTestUtils.createTestData(1024);

        BlobStoreTestUtils.writeBlob(store, sourcePath, data);
        BlobStoreTestUtils.writeBlob(store, targetPath, data);

        assertThrows(BlobAlreadyExistsException.class, () -> {
            store.copyBlob(sourcePath, targetPath);
        });
    }

    @Test
    void copyBlobWithReplaceExistingOption() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("copyBlobWithReplaceExistingOption");
        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
        byte[] sourceData = BlobStoreTestUtils.createTestData(1024, 42L);
        byte[] originalTargetData = BlobStoreTestUtils.createTestData(256, 84L);

        BlobStoreTestUtils.writeBlob(store, sourcePath, sourceData);
        BlobStoreTestUtils.writeBlob(store, targetPath, originalTargetData);

        Blob copiedBlob = store.copyBlob(sourcePath, targetPath, BlobWriteMode.REPLACE_EXISTING);

        assertNotNull(copiedBlob);
        BlobStoreTestUtils.assertBlobEquals(store, targetPath, sourceData);
        BlobStoreTestUtils.assertBlobEquals(store, sourcePath, sourceData);
    }

    @Test
    void copyBlobCreateNewStillFailsWhenTargetExists() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("copyBlobCreateNewFails");
        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
        byte[] sourceData = BlobStoreTestUtils.createTestData(64, 100L);
        byte[] targetData = BlobStoreTestUtils.createTestData(64, 200L);

        BlobStoreTestUtils.writeBlob(store, sourcePath, sourceData);
        BlobStoreTestUtils.writeBlob(store, targetPath, targetData);

        assertThrows(BlobAlreadyExistsException.class,
            () -> store.copyBlob(sourcePath, targetPath, BlobWriteMode.CREATE_NEW));

        BlobStoreTestUtils.assertBlobEquals(store, targetPath, targetData);
    }

    @Test
    void moveBlobWithinStore() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testMoveBlobWithinStore");
        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
        byte[] data = BlobStoreTestUtils.createTestData(1024);

        BlobStoreTestUtils.writeBlob(store, sourcePath, data);

        Blob movedBlob = store.moveBlob(sourcePath, targetPath);
        assertNotNull(movedBlob);
        assertTrue(movedBlob.exists());

        BlobStoreTestUtils.assertBlobEquals(store, targetPath, data);
        assertFalse(store.getBlob(sourcePath).exists());
    }

    @Test
    void moveBlobTargetExists() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("moveBlobTargetExists");
        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
        byte[] data = BlobStoreTestUtils.createTestData(1024);

        BlobStoreTestUtils.writeBlob(store, sourcePath, data);
        BlobStoreTestUtils.writeBlob(store, targetPath, data);

        assertThrows(BlobAlreadyExistsException.class, () -> {
            store.moveBlob(sourcePath, targetPath);
        });
    }

    @Test
    void moveBlobWithReplaceExistingOption() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("moveBlobWithReplaceExistingOption");
        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
        byte[] sourceData = BlobStoreTestUtils.createTestData(1024, 1234L);
        byte[] targetData = BlobStoreTestUtils.createTestData(512, 4321L);

        BlobStoreTestUtils.writeBlob(store, sourcePath, sourceData);
        BlobStoreTestUtils.writeBlob(store, targetPath, targetData);

        Blob movedBlob = store.moveBlob(sourcePath, targetPath, BlobWriteMode.REPLACE_EXISTING);

        assertNotNull(movedBlob);
        BlobStoreTestUtils.assertBlobEquals(store, targetPath, sourceData);
        assertFalse(store.getBlob(sourcePath).exists());
    }

    @Test
    void isEmptyDirectory() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testIsEmptyDirectory");
        BlobPath emptyPath = BlobPath.absolute("empty");
        BlobPath nonEmptyPath = BlobPath.absolute("nonempty");

        assertTrue(store.isEmptyDirectory(emptyPath));

        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("nonempty", "file.dat"),
            BlobStoreTestUtils.createTestData(100));

        assertFalse(store.isEmptyDirectory(nonEmptyPath));
    }

    @Test
    void deleteBlob() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testDeleteBlob");
        BlobPath path = BlobPath.absolute("delete.dat");
        byte[] data = BlobStoreTestUtils.createTestData(100);

        BlobStoreTestUtils.writeBlob(store, path, data);
        assertTrue(store.getBlob(path).exists());

        store.deleteBlob(path);
        assertFalse(store.getBlob(path).exists());
    }

    @Test
    void deleteBlobs() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testDeleteBlobs");
        byte[] data = BlobStoreTestUtils.createTestData(100);

        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("deleteDir", "file1.dat"), data);
        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("deleteDir", "file2.dat"), data);
        BlobStoreTestUtils.writeBlob(store, BlobPath.absolute("deleteDir", "subdir", "file3.dat"), data);

        BlobPath dirPath = BlobPath.absolute("deleteDir");
        store.deleteBlobs(dirPath);

        try (Stream<Blob> blobs = store.listBlobs(dirPath)) {
            assertEquals(0, blobs.count());
        }
    }

    @Test
    void blobNotFound() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testBlobNotFoundException");
        BlobPath path = BlobPath.absolute("nonexistent.dat");

        Blob blob = store.getBlob(path);
        assertFalse(blob.exists());
    }

    @Test
    void blobAlreadyExistsException() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testBlobAlreadyExistsException");
        BlobPath path = BlobPath.absolute("exists.dat");
        byte[] data = BlobStoreTestUtils.createTestData(100);

        BlobStoreTestUtils.writeBlob(store, path, data);

        Blob blob = store.getBlob(path);
        // S3 wraps BlobAlreadyExistsException in IOException, filesystem throws BlobAlreadyExistsException
        Exception exception = assertThrows(Exception.class, () -> {
            try (OutputStream os = blob.getOutputStream(BlobWriteMode.CREATE_NEW)) {
                os.write(data);
            }
        });
        // Verify it's one of the expected exceptions
        assertTrue(exception instanceof BlobAlreadyExistsException
            || (exception instanceof IOException && exception.getCause() instanceof BlobAlreadyExistsException),
            "Expected BlobAlreadyExistsException or IOException with BlobAlreadyExistsException cause");
    }

    @Test
    void nestedPaths() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testNestedPaths");
        BlobPath deepPath = BlobPath.absolute("level1", "level2", "level3", "level4", "file.dat");
        byte[] data = BlobStoreTestUtils.createTestData(100);

        BlobStoreTestUtils.writeBlob(store, deepPath, data);
        BlobStoreTestUtils.assertBlobEquals(store, deepPath, data);
    }

    @Test
    void largeBlob() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testLargeBlob");
        BlobPath path = BlobPath.absolute("large.dat");
        // 6 MB - should trigger multipart upload for S3
        byte[] data = BlobStoreTestUtils.createTestData(6 * 1024 * 1024, 12345L);

        BlobStoreTestUtils.writeBlob(store, path, data);
        BlobStoreTestUtils.assertBlobEquals(store, path, data);
    }

    @Test
    void veryLargeBlob() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testVeryLargeBlob");
        BlobPath path = BlobPath.absolute("verylarge.dat");
        // 12 MB - definitely multipart
        byte[] data = BlobStoreTestUtils.createTestData(12 * 1024 * 1024, 67890L);

        BlobStoreTestUtils.writeBlob(store, path, data);
        BlobStoreTestUtils.assertBlobEquals(store, path, data);
    }

    @Test
    void copyLargeBlob() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testCopyLargeBlob");
        BlobPath sourcePath = BlobPath.absolute("largesource.dat");
        BlobPath targetPath = BlobPath.absolute("largetarget.dat");
        // 6 MB - should trigger multipart copy for S3
        byte[] data = BlobStoreTestUtils.createTestData(6 * 1024 * 1024, 11111L);

        BlobStoreTestUtils.writeBlob(store, sourcePath, data);

        Blob copiedBlob = store.copyBlob(sourcePath, targetPath);
        assertNotNull(copiedBlob);
        assertTrue(copiedBlob.exists());

        BlobStoreTestUtils.assertBlobEquals(store, targetPath, data);
    }

    @Test
    void listAndDeleteManyBlobs() throws Exception
    {
        BlobStore store = getOrCreateBlobStore("testListAndDeleteManyBlobs");
        String prefix = "bulkdelete";
        int count = 1500;

        BlobStoreTestUtils.createManyBlobs(store, prefix, count);

        try (Stream<Blob> blobs = store.listBlobs(BlobPath.absolute(prefix))) {
            assertEquals(count, blobs.count());
        }

        BlobPath dirPath = BlobPath.absolute(prefix);
        store.deleteBlobs(dirPath);

        try (Stream<Blob> blobs = store.listBlobs(dirPath)) {
            assertEquals(0, blobs.count());
        }
    }
}
