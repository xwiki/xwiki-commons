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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

/**
 * A {@link BlobStore} implementation that stores blobs in the file system.
 *
 * @version $Id$
 * @since 17.7.0RC1
 */
public class FileSystemBlobStore implements BlobStore
{
    private final String name;

    private final Path basePath;

    /**
     * Creates a new FileSystemBlobStore with the given name and base path.
     *
     * @param name the name of the blob store
     * @param basePath the base path in the file system where blobs are stored
     */
    public FileSystemBlobStore(String name, Path basePath)
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
    public Blob getBlob(BlobPath path) throws BlobStoreException
    {
        Path blobFsPath = getBlobFsPath(path);
        return new FileSystemBlob(path, blobFsPath, this);
    }

    private Path getBlobFsPath(BlobPath blobPath)
    {
        Path currentPath = this.basePath;

        // Append each segment of the BlobPath to the base path.
        for (String segment : blobPath.getSegments()) {
            currentPath = currentPath.resolve(segment);
        }

        return currentPath;
    }

    @Override
    public Stream<Blob> listBlobs(BlobPath path) throws BlobStoreException
    {
        Path absolutePath = getBlobFsPath(path);
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
                        // Walk the path up until we reach absolutePath and collect the segments.
                        // This is necessary to create a BlobPath that is relative to the base path.
                        List<String> segments = new ArrayList<>(path.getSegments());
                        Path currentPath = p;
                        while (!absolutePath.equals(currentPath)) {
                            segments.add(0, currentPath.getFileName().toString());
                            currentPath = currentPath.getParent();
                        }
                        // Create a BlobPath from the segments.
                        BlobPath blobPath = BlobPath.of(segments);
                        return new FileSystemBlob(blobPath, p, this);
                    });
            } catch (IOException e) {
                throw new BlobStoreException("Failed to list blobs in directory: " + absolutePath, e);
            }
        }
    }

    @Override
    public Blob copyBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException("source and target paths are the same");
        }

        Path absoluteSourcePath = getBlobFsPath(sourcePath);
        transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, false);

        return getBlob(targetPath);
    }

    @Override
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            Path absoluteSourcePath = fileSystemBlobStore.getBlobFsPath(sourcePath);
            transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, false);
        } else {
            // For cross-store copies, use streaming approach
            try (var inputStream = sourceStore.getBlob(sourcePath).getStream()) {
                Blob targetBlob = getBlob(targetPath);
                targetBlob.writeFromStream(inputStream, BlobDoesNotExistCondition.INSTANCE);
            } catch (BlobStoreException e) {
                throw e;
            } catch (Exception e) {
                throw new BlobStoreException("Reading source blob failed", e);
            }
        }

        return getBlob(targetPath);
    }

    private void transferBlobInternal(BlobPath sourcePath, BlobPath targetPath, Path absoluteSourcePath, boolean move)
        throws BlobStoreException
    {
        Path absoluteTargetPath = getBlobFsPath(targetPath);

        try {
            // Ensure parent directory exists
            Path targetParent = absoluteTargetPath.getParent();
            if (targetParent != null && !Files.exists(targetParent)) {
                Files.createDirectories(targetParent);
            }

            // Use atomic operation - this will fail if source doesn't exist or target exists
            if (move) {
                Files.move(absoluteSourcePath, absoluteTargetPath);
            } else {
                Files.copy(absoluteSourcePath, absoluteTargetPath);
            }
        } catch (NoSuchFileException e) {
            throw new BlobNotFoundException(sourcePath, e);
        } catch (FileAlreadyExistsException e) {
            throw new BlobAlreadyExistsException(targetPath, e);
        } catch (IOException e) {
            throw new BlobStoreException((move ? "move" : "copy") + " blob failed", e);
        }
    }

    @Override
    public Blob moveBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException("source and target paths are the same");
        }

        Path absoluteSourcePath = getBlobFsPath(sourcePath);
        transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, true);

        return getBlob(targetPath);
    }

    @Override
    public Blob moveBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            Path absoluteSourcePath = fileSystemBlobStore.getBlobFsPath(sourcePath);
            transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, true);
            return getBlob(targetPath);
        } else {
            // For cross-store moves, use copy + delete approach
            Blob targetBlob = copyBlob(sourceStore, sourcePath, targetPath);
            sourceStore.deleteBlob(sourcePath);
            return targetBlob;
        }
    }

    @Override
    public void deleteBlob(BlobPath path) throws BlobStoreException
    {
        try {
            Path fileSystemPath = getBlobFsPath(path);
            Files.deleteIfExists(fileSystemPath);
            // Delete parent directories up to basePath.
            // TODO: This isn't thread-safe with parent directory creation - synchronize locally as we don't really
            //  need to support cluster setups with this store.
            Path parentPath = fileSystemPath.getParent();
            while (parentPath != null && !this.basePath.equals(parentPath)
                && FileUtils.isEmptyDirectory(parentPath.toFile())) {
                Files.deleteIfExists(parentPath);
            }
        } catch (IOException e) {
            throw new BlobStoreException("delete blob failed", e);
        }
    }
}
