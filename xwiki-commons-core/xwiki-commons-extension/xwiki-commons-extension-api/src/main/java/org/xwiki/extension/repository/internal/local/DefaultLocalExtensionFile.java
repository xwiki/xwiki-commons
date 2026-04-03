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
package org.xwiki.extension.repository.internal.local;

import java.io.IOException;
import java.io.InputStream;

import org.xwiki.extension.LocalExtensionFile;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobStoreException;

/**
 * Default implementation of {@link LocalExtensionFile}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultLocalExtensionFile implements LocalExtensionFile
{
    /**
     * The blob holding the local extension file.
     */
    private final Blob blob;

    /**
     * @param blob the blob holding the local extension file
     * @since 18.2.0RC1
     */
    public DefaultLocalExtensionFile(Blob blob)
    {
        this.blob = blob;
    }

    /**
     * @return the blob holding the local extension file
     * @since 18.2.0RC1
     */
    public Blob getBlob()
    {
        return this.blob;
    }

    // ExtensionFile

    @Override
    public long getLength()
    {
        try {
            return getBlob().getSize();
        } catch (BlobStoreException e) {
            throw new RuntimeException("Failed to get the size of the blob [%s]".formatted(getBlob()), e);
        }
    }

    @Override
    public InputStream openStream() throws IOException
    {
        try {
            return getBlob().getStream();
        } catch (BlobStoreException e) {
            throw new IOException("Failed to open the local extension file stream", e);
        }
    }

    // LocalExtensionFile

    @Override
    public String getAbsolutePath()
    {
        // FIXME: hardly the same meaning as before, but not sure what to do...
        // At best we can hack something by going through FileSystemBlob probably, but there is nothing we can do when
        // the blob store an anything else than filesystem
        return getBlob().getPath().absolute().toString();
    }

    @Override
    public String getName()
    {
        // FIXME: is #toString really an API here ?
        return getBlob().getPath().getFileName().toString();
    }
}
