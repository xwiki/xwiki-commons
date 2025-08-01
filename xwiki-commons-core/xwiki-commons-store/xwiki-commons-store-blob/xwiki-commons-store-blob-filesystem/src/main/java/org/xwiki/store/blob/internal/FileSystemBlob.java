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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

public class FileSystemBlob implements Blob
{
    private final FileSystemBlobStore blobStore;

    private final Path absolutePath;

    private final Path relativePath;

    public FileSystemBlob(Path relativePath, Path absolutePath, FileSystemBlobStore store)
    {
        this.relativePath = relativePath;
        this.blobStore = store;
        this.absolutePath = absolutePath;
    }

    @Override
    public BlobStore getStore()
    {
        return this.blobStore;
    }

    @Override
    public Path getPath()
    {
        return this.relativePath;
    }

    @Override
    public boolean exists()
    {
        return Files.exists(this.absolutePath, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public long getSize() throws BlobStoreException
    {
        try {
            return Files.exists(this.absolutePath, LinkOption.NOFOLLOW_LINKS)
                ? Files.size(this.absolutePath)
                : -1;
        } catch (IOException e) {
            throw new BlobStoreException("Error getting file size.", e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws BlobStoreException
    {
        // Ensure the parent directory exists before creating the output stream.
        if (Files.notExists(this.absolutePath.getParent())) {
            try {
                Files.createDirectories(this.absolutePath.getParent());
            } catch (IOException e) {
                throw new BlobStoreException("Error creating parent directories.", e);
            }
        }
        try {
            return Files.newOutputStream(this.absolutePath);
        } catch (IOException e) {
            throw new BlobStoreException("Error getting output stream.", e);
        }
    }

    @Override
    public void writeFromStream(InputStream inputStream) throws BlobStoreException
    {
        try (OutputStream outputStream = this.getOutputStream()) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new BlobStoreException("Error writing from InputStream to blob.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public InputStream getStream() throws Exception
    {
        if (!exists()) {
            throw new BlobStoreException("Blob does not exist at " + this.absolutePath);
        }
        return Files.newInputStream(this.absolutePath, LinkOption.NOFOLLOW_LINKS);
    }
}
