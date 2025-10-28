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

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.store.blob.AbstractBlobStore;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.FileSystemBlobStoreProperties;

/**
 * A {@link BlobStore} implementation that stores blobs in the file system.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class FileSystemBlobStore extends AbstractBlobStore<FileSystemBlobStoreProperties>
{
    /**
     * Number of attempts to make when trying to create parent directories for a blob operation.
     */
    static final int NUM_ATTEMPTS = 5;

    private static final String SOURCE_AND_TARGET_SAME_ERROR = "source and target paths are the same";

    private final Path basePath;

    /**
     * Creates a new FileSystemBlobStore with the given properties.
     *
     * @param properties the properties for this blob store
     */
    public FileSystemBlobStore(FileSystemBlobStoreProperties properties)
    {
        super(properties);
        this.basePath = properties.getRootDirectory();
    }

    @Override
    public Blob getBlob(BlobPath path) throws BlobStoreException
    {
        Path blobFsPath = getBlobFilePath(path);
        return new FileSystemBlob(path, blobFsPath, this);
    }

    /**
     * Get the absolute file system path for the given BlobPath. This is meant for internal use only.
     *
     * @param blobPath the BlobPath to get the file system path for
     * @return the absolute file system path for the given BlobPath
     */
    public Path getBlobFilePath(BlobPath blobPath)
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
        Path absolutePath = getBlobFilePath(path);
        if (!Files.exists(absolutePath) || !Files.isDirectory(absolutePath)) {
            return Stream.empty();
        } else {
            // List files recursively, ignoring directories.
            try {
                Path normalizedAbsolutePath = absolutePath.normalize();
                // The caller is responsible for closing the stream.
                //noinspection resource
                return Files.walk(normalizedAbsolutePath)
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        // Compute relative path by subtracting the base path
                        Path normalizedPath = p.normalize();
                        if (!normalizedPath.startsWith(normalizedAbsolutePath)) {
                            // This should never happen, but just in case...
                            throw new IllegalStateException(
                                "Found a file outside the expected directory: " + normalizedPath);
                        }
                        Path relativePath = normalizedAbsolutePath.relativize(normalizedPath);

                        // Convert relative path segments to list
                        List<String> segments = new ArrayList<>(path.getSegments());
                        for (Path segment : relativePath) {
                            segments.add(segment.toString());
                        }

                        return new FileSystemBlob(BlobPath.of(segments), p, this);
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
            throw new BlobStoreException(SOURCE_AND_TARGET_SAME_ERROR);
        }

        Path absoluteSourcePath = getBlobFilePath(sourcePath);
        transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, false);

        return getBlob(targetPath);
    }

    @Override
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            Path absoluteSourcePath = fileSystemBlobStore.getBlobFilePath(sourcePath);
            transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, false);
        } else {
            // For cross-store copies, use streaming approach.
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

    @Override
    public void moveDirectory(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException(SOURCE_AND_TARGET_SAME_ERROR);
        }

        Path absoluteSourcePath = getBlobFilePath(sourcePath);
        transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, true);
    }

    @Override
    public void moveDirectory(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            Path absoluteSourcePath = fileSystemBlobStore.getBlobFilePath(sourcePath);
            transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, true);
        } else {
            super.moveDirectory(sourceStore, sourcePath, targetPath);
        }
    }

    @Override
    public boolean isEmptyDirectory(BlobPath path) throws BlobStoreException
    {
        try (Stream<Blob> stream = listBlobs(path)) {
            return stream.findFirst().isEmpty();
        }
    }

    private void transferBlobInternal(BlobPath sourcePath, BlobPath targetPath, Path absoluteSourcePath, boolean move)
        throws BlobStoreException
    {
        Path absoluteTargetPath = getBlobFilePath(targetPath);

        String operation = move ? "move" : "copy";

        NoSuchFileException lastNoSuchFileException = null;

        for (int attempt = 0; attempt < NUM_ATTEMPTS; ++attempt) {
            try {
                // Ensure parent directory exists
                createParents(absoluteTargetPath);

                // Use atomic operation - this will fail if source doesn't exist or target exists
                if (move) {
                    Files.move(absoluteSourcePath, absoluteTargetPath);
                    cleanUpParents(absoluteSourcePath);
                } else {
                    Files.copy(absoluteSourcePath, absoluteTargetPath);
                }

                return;
            } catch (NoSuchFileException e) {
                // If the source file doesn't exist, we can't continue. If the parent directory of the target
                // file doesn't exist, loop around and try again.
                if (!Files.exists(absoluteSourcePath)) {
                    cleanUpParents(absoluteTargetPath);
                    throw new BlobNotFoundException(sourcePath, e);
                }
                lastNoSuchFileException = e;
            } catch (FileAlreadyExistsException e) {
                throw new BlobAlreadyExistsException(targetPath, e);
            } catch (IOException e) {
                cleanUpParents(absoluteTargetPath);
                throw new BlobStoreException(operation + " blob failed", e);
            }
        }

        cleanUpParents(absoluteTargetPath);
        throw new BlobStoreException("%s blob failed after %d attempts".formatted(operation, NUM_ATTEMPTS),
            lastNoSuchFileException);
    }

    void createParents(Path absoluteTargetPath) throws IOException
    {
        Path targetParent = absoluteTargetPath.getParent();
        if (targetParent != null) {
            Files.createDirectories(targetParent);
        }
    }

    @Override
    public Blob moveBlob(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException(SOURCE_AND_TARGET_SAME_ERROR);
        }

        Path absoluteSourcePath = getBlobFilePath(sourcePath);
        transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, true);

        return getBlob(targetPath);
    }

    @Override
    public Blob moveBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            Path absoluteSourcePath = fileSystemBlobStore.getBlobFilePath(sourcePath);
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
            Path fileSystemPath = getBlobFilePath(path);
            Files.deleteIfExists(fileSystemPath);
            // Delete parent directories up to basePath.
            cleanUpParents(fileSystemPath);
        } catch (IOException e) {
            throw new BlobStoreException("delete blob failed", e);
        }
    }

    void cleanUpParents(Path fileSystemPath)
    {
        try {
            for (Path parentPath = fileSystemPath.getParent(); parentPath != null && !this.basePath.equals(parentPath)
                && Files.isDirectory(parentPath) && PathUtils.isEmptyDirectory(parentPath);
                parentPath = parentPath.getParent()) {
                Files.deleteIfExists(parentPath);
            }
        } catch (IOException e) {
            // Ignore errors when cleaning up parent directories.
        }
    }

    @Override
    public void deleteBlobs(BlobPath path) throws BlobStoreException
    {
        try {
            Path absolutePath = getBlobFilePath(path);
            if (Files.exists(absolutePath) && Files.isDirectory(absolutePath)) {
                PathUtils.deleteDirectory(absolutePath);
                cleanUpParents(absolutePath);
            }
        } catch (IOException e) {
            throw new BlobStoreException("delete blobs failed", e);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FileSystemBlobStore blobStore)) {
            return false;
        }

        return new EqualsBuilder().append(this.basePath, blobStore.basePath).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(this.basePath).toHashCode();
    }
}
