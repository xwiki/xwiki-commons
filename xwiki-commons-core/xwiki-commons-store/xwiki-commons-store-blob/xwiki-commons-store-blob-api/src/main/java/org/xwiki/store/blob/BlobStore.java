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

import java.util.stream.Stream;

import org.xwiki.stability.Unstable;

/**
 * A storage that allows storing blob data.
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
@Unstable
public interface BlobStore
{
    /**
     * Get the name of this blob store.
     *
     * @return the name of this blob store
     */
    String getName();

    /**
     * Get the blob with the given identifier.
     *
     * @param path the path of the blob to retrieve
     * @return the blob with the given path
     * @throws BlobStoreException if there cannot be a blob with the given path (e.g., because the path is too long)
     */
    Blob getBlob(BlobPath path) throws BlobStoreException;

    /**
     * List all blobs under the given path.
     *
     * @param path the path prefix to search under
     * @return an iterator over all blobs under the given path
     * @throws BlobStoreException if the listing operation fails
     */
    Stream<Blob> listBlobs(BlobPath path) throws BlobStoreException;

    /**
     * Copy a blob from one path to another within this store.
     *
     * @param sourcePath the source path
     * @param targetPath the target path
     * @return the copied blob at the target path
     * @throws BlobStoreException if the copy operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path
     */
    Blob copyBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException;

    /**
     * Copy a blob from another store to this store.
     *
     * @param sourceStore the source blob store
     * @param sourcePath the path of the blob in the source store
     * @param targetPath the path where the blob should be copied in this store
     * @return the copied blob at the target path
     * @throws BlobStoreException if the copy operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path
     */
    Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException;

    /**
     * Move a blob from one path to another within this store.
     *
     * @param sourcePath the source path
     * @param targetPath the target path
     * @return the moved blob at the target path
     * @throws BlobStoreException if the move operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path
     */
    Blob moveBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException;

    /**
     * Move a blob from another store to this store.
     *
     * @param sourceStore the source blob store
     * @param sourcePath the path of the blob in the source store
     * @param targetPath the path where the blob should be moved in this store
     * @return the moved blob at the target path
     * @throws BlobStoreException if the move operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path
     */
    Blob moveBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException;

    /**
     * Delete the blob at the given path.
     *
     * @param path the path of the blob to delete
     * @throws BlobStoreException if the deletion fails
     */
    void deleteBlob(BlobPath path) throws BlobStoreException;
}
