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

import java.io.Serial;

import org.xwiki.stability.Unstable;

/**
 * Exception thrown when a blob cannot be found at the specified path.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public class BlobNotFoundException extends BlobStoreException
{
    /**
     * Provides an id for serialization.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String BLOB_NOT_FOUND_MESSAGE = "Blob not found at path: ";

    private final BlobPath blobPath;

    /**
     * Constructs a new exception with the specified blob path.
     *
     * @param blobPath the path of the blob that was not found
     */
    public BlobNotFoundException(BlobPath blobPath)
    {
        super(BLOB_NOT_FOUND_MESSAGE + blobPath);
        this.blobPath = blobPath;
    }

    /**
     * Constructs a new exception with the specified blob path and cause.
     *
     * @param blobPath the path of the blob that was not found
     * @param cause the cause of the exception
     */
    public BlobNotFoundException(BlobPath blobPath, Throwable cause)
    {
        super(BLOB_NOT_FOUND_MESSAGE + blobPath, cause);
        this.blobPath = blobPath;
    }

    /**
     * @return the path of the blob that was not found
     */
    public BlobPath getBlobPath()
    {
        return this.blobPath;
    }
}
