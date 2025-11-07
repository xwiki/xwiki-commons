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
     * @return the type of this blob store (e.g., "filesystem", "s3")
     */
    String getType();

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
     * List all descendant blobs under the given path prefix. The returned stream includes blobs at every depth below
     * the provided path. The caller must close the returned stream after use.
     *
     * @param path the path prefix to search under
     * @return a stream of the blobs under the given path
     * @throws BlobStoreException if the listing operation fails
     */
    Stream<Blob> listDescendants(BlobPath path) throws BlobStoreException;

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
     * Delete all descendant blobs under the given path prefix.
     *
     * @param path the path prefix to delete under
     * @throws BlobStoreException if the deletion fails
     */
    void deleteDescendants(BlobPath path) throws BlobStoreException;

    /**
     * Check if there is at least one descendant blob under the given path prefix.
     *
     * <p>Only blobs located under the given path prefix are considered. A blob with the exact same path is not counted,
     * maintaining consistency with {@link #listDescendants(BlobPath)} which only lists blobs under the prefix.</p>
     *
     * @param path the path of the directory to check
     * @return true if at least one descendant blob exists, false otherwise
     * @throws BlobStoreException if the check operation fails
     */
    boolean hasDescendants(BlobPath path) throws BlobStoreException;
}
