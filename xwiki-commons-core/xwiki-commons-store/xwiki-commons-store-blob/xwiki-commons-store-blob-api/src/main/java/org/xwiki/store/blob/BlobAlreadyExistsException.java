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
 * Exception thrown when trying to copy a blob to a target path that already contains a blob.
 *
 * @version $Id$
 * @since 17.9.0RC1
 */
@Unstable
public class BlobAlreadyExistsException extends BlobStoreException
{
    /**
     * Provides an id for serialization.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String BLOB_ALREADY_EXISTS_MESSAGE = "Blob already exists at target path: ";

    private final BlobPath targetPath;

    /**
     * Constructs a new exception with the specified target path.
     *
     * @param targetPath the path where a blob already exists
     */
    public BlobAlreadyExistsException(BlobPath targetPath)
    {
        super(BLOB_ALREADY_EXISTS_MESSAGE + targetPath);
        this.targetPath = targetPath;
    }

    /**
     * Constructs a new exception with the specified target path and cause.
     *
     * @param targetPath the path where a blob already exists
     * @param cause the cause of the exception
     */
    public BlobAlreadyExistsException(BlobPath targetPath, Throwable cause)
    {
        super(BLOB_ALREADY_EXISTS_MESSAGE + targetPath, cause);
        this.targetPath = targetPath;
    }

    /**
     * @return the path where a blob already exists
     */
    public BlobPath getTargetPath()
    {
        return this.targetPath;
    }
}
