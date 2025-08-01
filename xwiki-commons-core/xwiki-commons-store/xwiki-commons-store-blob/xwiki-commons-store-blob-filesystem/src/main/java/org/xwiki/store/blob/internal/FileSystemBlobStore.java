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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

public class FileSystemBlobStore implements BlobStore
{
    private final String name;

    private final Path basePath;

    FileSystemBlobStore(String name, Path basePath)
    {
        this.name = name;
        this.basePath = basePath;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Blob getBlob(Path path) throws BlobStoreException
    {
        Path blobPath = getBlobPath(path);
        return new FileSystemBlob(path, blobPath, this);
    }

    private Path getBlobPath(Path path)
    {
        // Remove the root from the given path.
        Path relativePath;
        if (path.isAbsolute()) {
            relativePath = path.subpath(0, path.getNameCount());
        } else {
            relativePath = path;
        }
        return this.basePath.resolve(relativePath).normalize();
    }

    @Override
    public Stream<Blob> listBlobs(Path path) throws BlobStoreException
    {
        Path absolutePath = getBlobPath(path);
        if (!Files.exists(absolutePath)) {
            return Stream.empty();
        } else if (!Files.isDirectory(absolutePath)) {
            throw new BlobStoreException("Not a directory: " + absolutePath);
        } else {
            // List files recursively, ignoring directories.
            try {
                // TODO: ensure that the returned stream is used with try-with-resource.
                return Files.walk(absolutePath)
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        Path relativePath = this.basePath.relativize(p);
                        return new FileSystemBlob(relativePath, p, this);
                    });
            } catch (IOException e) {
                throw new BlobStoreException("Failed to list blobs in directory: " + absolutePath, e);
            }
        }
    }

    @Override
    public Blob copyBlob(Path sourcePath, Path targetPath) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException("source and target paths are the same");
        } else {
            Path absoluteSourcePath = getBlobPath(sourcePath);
            Path absoluteTargetPath = getBlobPath(targetPath);
            try {
                Files.copy(absoluteSourcePath, absoluteTargetPath);
            } catch (IOException e) {
                throw new BlobStoreException("copy blob failed", e);
            }
        }

        return getBlob(targetPath);
    }

    @Override
    public Blob copyBlob(BlobStore sourceStore, Path sourcePath, Path targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            Path absoluteSourcePath = fileSystemBlobStore.getBlobPath(sourcePath);
            Path absoluteTargetPath = getBlobPath(targetPath);
            try {
                Files.copy(absoluteSourcePath, absoluteTargetPath);
            } catch (IOException e) {
                throw new BlobStoreException("copy blob failed", e);
            }
        } else {
            Blob sourceBlob = sourceStore.getBlob(sourcePath);
            Blob targetBlob = getBlob(targetPath);
            try {
                targetBlob.writeFromStream(sourceBlob.getStream());
            } catch (Exception e) {
                throw new BlobStoreException("Reading source blob failed", e);
            }
        }

        return getBlob(targetPath);
    }

    @Override
    public void deleteBlob(Path path) throws BlobStoreException
    {
        try {
            Files.deleteIfExists(getBlobPath(path));
        } catch (IOException e) {
            throw new BlobStoreException("delete blob failed", e);
        }
    }
}
