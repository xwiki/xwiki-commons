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
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Utility methods for blob store integration tests.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public final class BlobStoreTestUtils
{
    private static final Random RANDOM = new Random();

    private BlobStoreTestUtils()
    {
    }

    /**
     * Create test data of a specific size.
     *
     * @param sizeInBytes the size in bytes
     * @return the test data
     */
    public static byte[] createTestData(int sizeInBytes)
    {
        byte[] data = new byte[sizeInBytes];
        RANDOM.nextBytes(data);
        return data;
    }

    /**
     * Write a blob to the store.
     *
     * @param store the blob store
     * @param path the blob path
     * @param data the data to write
     * @throws BlobStoreException if the write fails
     */
    public static void writeBlob(BlobStore store, BlobPath path, byte[] data) throws BlobStoreException
    {
        Blob blob = store.getBlob(path);
        try (OutputStream os = blob.getOutputStream()) {
            os.write(data);
        } catch (IOException e) {
            throw new BlobStoreException("Failed to write blob", e);
        }
    }

    /**
     * Assert that a blob contains the expected data.
     *
     * @param store the blob store
     * @param path the blob path
     * @param expected the expected data
     * @throws BlobStoreException if reading the blob fails
     */
    public static void assertBlobEquals(BlobStore store, BlobPath path, byte[] expected) throws BlobStoreException
    {
        Blob blob = store.getBlob(path);
        try (InputStream is = blob.getStream()) {
            byte[] actual = is.readAllBytes();
            assertArrayEquals(expected, actual, "Blob content does not match");
        } catch (IOException e) {
            throw new BlobStoreException("Failed to read blob", e);
        }
    }

    /**
     * Create many blobs with a given prefix.
     *
     * @param store the blob store
     * @param prefix the path prefix
     * @param count the number of blobs to create
     * @throws BlobStoreException if creating blobs fails
     */
    public static void createManyBlobs(BlobStore store, String prefix, int count) throws BlobStoreException
    {
        // Small blobs for bulk creation
        byte[] data = createTestData(100);
        for (int i = 0; i < count; i++) {
            BlobPath path = BlobPath.absolute(prefix, String.format("blob-%05d.dat", i));
            writeBlob(store, path, data);
        }
    }

    /**
     * Create test data with a specific pattern for verification.
     *
     * @param sizeInBytes the size in bytes
     * @param seed the seed for reproducible data
     * @return the test data
     */
    public static byte[] createTestData(int sizeInBytes, long seed)
    {
        byte[] data = new byte[sizeInBytes];
        Random random = new Random(seed);
        random.nextBytes(data);
        return data;
    }
}
