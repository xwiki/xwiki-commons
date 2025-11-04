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
 * @since 17.10.0RC1
 */
@Unstable
public interface BlobStore
{
    /**
     * @return the unique name of this blob store
     */
    String getName();

    /**
     * @return the hint of this blob store (e.g., "filesystem", "s3")
     */
    String getHint();

    /**
     * @return the properties used to create this store
     */
    BlobStoreProperties getProperties();

    /**
     * Get the blob with the given identifier.
     *
     * @param path the path of the blob to retrieve
     * @return the blob with the given path
     * @throws BlobStoreException if there cannot be a blob with the given path (e.g., because the path is too long)
     */
    Blob getBlob(BlobPath path) throws BlobStoreException;

    /**
     * List all blobs under the given path. The caller must close the returned stream after use.
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
     * @param options optional behaviors for the copy, supports {@link BlobWriteMode}
     * @return the copied blob at the target path
     * @throws BlobStoreException if the copy operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path and
     *     {@link BlobWriteMode#CREATE_NEW} is requested
     */
    Blob copyBlob(BlobPath sourcePath, BlobPath targetPath, BlobOption... options) throws BlobStoreException;

    /**
     * Copy a blob from another store to this store.
     *
     * @param sourceStore the source blob store
     * @param sourcePath the path of the blob in the source store
     * @param targetPath the path where the blob should be copied in this store
     * @param options optional behaviors for the copy, supports {@link BlobWriteMode}
     * @return the copied blob at the target path
     * @throws BlobStoreException if the copy operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path and
     *     {@link BlobWriteMode#CREATE_NEW} is requested
     */
    Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath, BlobOption... options)
        throws BlobStoreException;

    /**
     * Move a blob from one path to another within this store.
     *
     * @param sourcePath the source path
     * @param targetPath the target path
     * @param options optional behaviors for the move, supports {@link BlobWriteMode}
     * @return the moved blob at the target path
     * @throws BlobStoreException if the move operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path and
     *     {@link BlobWriteMode#CREATE_NEW} is requested
     */
    Blob moveBlob(BlobPath sourcePath, BlobPath targetPath, BlobOption... options) throws BlobStoreException;

    /**
     * Check if a directory is empty (i.e., contains no blobs).
     *
     * <p>Only child blobs under the given path prefix are considered. If there is a blob
     * with the exact same path as the directory being checked, it is not counted
     * when determining if the directory is empty. This maintains consistency with
     * {@link #listBlobs(BlobPath)} which lists only child blobs under the path prefix.</p>
     *
     * @param path the path of the directory to check
     * @return true if the directory is empty, false otherwise
     * @throws BlobStoreException if the check operation fails
     */
    boolean isEmptyDirectory(BlobPath path) throws BlobStoreException;

    /**
     * Move a blob from another store to this store.
     *
     * @param sourceStore the source blob store
     * @param sourcePath the path of the blob in the source store
     * @param targetPath the path where the blob should be moved in this store
     * @param options optional behaviors for the move, supports {@link BlobWriteMode}
     * @return the moved blob at the target path
     * @throws BlobStoreException if the move operation fails
     * @throws BlobNotFoundException if the source blob does not exist
     * @throws BlobAlreadyExistsException if a blob already exists at the target path and
     *     {@link BlobWriteMode#CREATE_NEW} is requested
     */
    Blob moveBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath, BlobOption... options)
        throws BlobStoreException;

    /**
     * Delete the blob at the given path.
     *
     * @param path the path of the blob to delete
     * @throws BlobStoreException if the deletion fails
     */
    void deleteBlob(BlobPath path) throws BlobStoreException;

    /**
     * Delete all blobs under the given path.
     *
     * @param path the path prefix to delete under
     * @throws BlobStoreException if the deletion fails
     */
    void deleteBlobs(BlobPath path) throws BlobStoreException;
}
