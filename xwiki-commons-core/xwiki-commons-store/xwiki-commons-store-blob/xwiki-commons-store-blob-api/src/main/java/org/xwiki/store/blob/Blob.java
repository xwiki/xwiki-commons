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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.xwiki.stability.Unstable;
import org.xwiki.store.StreamProvider;

/**
 * A Blob is a piece of data stored in a BlobStore.
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
@Unstable
public interface Blob extends StreamProvider
{
    /**
     * @return the store where this blob is stored
     */
    BlobStore getStore();

    /**
     * @return the path of this blob inside its store
     */
    Path getPath();

    /**
     * @return true if the blob exists, false otherwise
     */
    boolean exists();

    /**
     * Get the size of this blob.
     *
     * @return the size of this blob in bytes, or -1 if the blob doesn't exist
     * @throws BlobStoreException when the size cannot be determined
     */
    long getSize() throws BlobStoreException;

    /**
     * @return an OutputStream to write data to this blob
     * @throws BlobStoreException if the blob cannot be written, for example because its name is invalid. There is no
     *     guarantee that in such a case an exception will be thrown, the exception could also only be thrown when data
     *     is written to the stream, or when the stream is closed.
     */
    OutputStream getOutputStream() throws BlobStoreException;

    /**
     * Write the content of the given InputStream to this blob.
     *
     * @param inputStream the InputStream to read data from
     * @throws BlobStoreException if the InputStream cannot be read or the blob cannot be written, for example because
     * its name is invalid.
     */
    void writeFromStream(InputStream inputStream) throws BlobStoreException;

    /**
     * Copy this blob to another path within the same store.
     *
     * @param targetPath the target path for the copy
     * @return the new blob at the target path
     * @throws BlobStoreException if the copy operation fails, for example because this blob doesn't exist
     */
    default Blob copyTo(Path targetPath) throws BlobStoreException
    {
        return getStore().copyBlob(getPath(), targetPath);
    }

    /**
     * Delete this blob from the store if it exists.
     *
     * @throws BlobStoreException if the deletion fails
     */
    default void delete() throws BlobStoreException
    {
        getStore().deleteBlob(getPath());
    }
}
