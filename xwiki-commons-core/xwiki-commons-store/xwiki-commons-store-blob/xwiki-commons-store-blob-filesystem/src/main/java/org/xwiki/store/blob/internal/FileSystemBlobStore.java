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
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.store.blob.AbstractBlobStore;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobOption;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobWriteMode;
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

    private static final Set<Class<? extends BlobOption>> SUPPORTED_COPY_MOVE_OPTIONS = Set.of(BlobWriteMode.class);

    private enum TransferKind
    {
        COPY, MOVE
    }

    private final Path basePath;

    /**
     * Creates a new FileSystemBlobStore with the given name and properties.
     *
     * @param name the name of this blob store
     * @param properties the properties for this blob store
     */
    public FileSystemBlobStore(String name, FileSystemBlobStoreProperties properties)
    {
        initialize(name, "filesystem", properties);
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
        for (String segment : blobPath.getNames()) {
            currentPath = currentPath.resolve(segment);
        }

        return currentPath;
    }

    @Override
    public Stream<Blob> listDescendants(BlobPath path) throws BlobStoreException
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
                    .map(p -> toBlobFromAbsolute(p.normalize(), normalizedAbsolutePath, path));
            } catch (IOException e) {
                throw new BlobStoreException("Failed to list blobs in directory: " + absolutePath, e);
            }
        }
    }

    private Blob toBlobFromAbsolute(Path normalizedPath, Path normalizedAbsolutePath, BlobPath basePath)
    {
        if (!normalizedPath.startsWith(normalizedAbsolutePath)) {
            throw new IllegalStateException("Found a file outside the expected directory: " + normalizedPath);
        }

        Path relativePath = normalizedAbsolutePath.relativize(normalizedPath);

        List<String> segments = new ArrayList<>(basePath.getNames());
        for (Path segment : relativePath) {
            segments.add(segment.toString());
        }

        return new FileSystemBlob(BlobPath.absolute(segments), normalizedPath, this);
    }

    @Override
    public Blob copyBlob(BlobPath sourcePath, BlobPath targetPath, BlobOption... options) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException(SOURCE_AND_TARGET_SAME_ERROR);
        }

        BlobOptionSupport.validateSupportedOptions(SUPPORTED_COPY_MOVE_OPTIONS, options);
        BlobWriteMode writeMode = BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW, options);
        Path absoluteSourcePath = getBlobFilePath(sourcePath);
        transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, TransferKind.COPY, writeMode);

        return getBlob(targetPath);
    }

    @Override
    public Blob copyBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath, BlobOption... options)
        throws BlobStoreException
    {
        BlobOptionSupport.validateSupportedOptions(SUPPORTED_COPY_MOVE_OPTIONS, options);
        BlobWriteMode writeMode = BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW, options);

        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            Path absoluteSourcePath = fileSystemBlobStore.getBlobFilePath(sourcePath);
            transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, TransferKind.COPY, writeMode);
        } else {
            // For cross-store copies, use streaming approach.
            try (var inputStream = sourceStore.getBlob(sourcePath).getStream()) {
                Blob targetBlob = getBlob(targetPath);
                targetBlob.writeFromStream(inputStream, writeMode);
            } catch (BlobStoreException e) {
                throw e;
            } catch (Exception e) {
                throw new BlobStoreException("Reading source blob failed", e);
            }
        }

        return getBlob(targetPath);
    }

    @Override
    public Blob moveBlob(BlobPath sourcePath, BlobPath targetPath, BlobOption... options) throws BlobStoreException
    {
        if (sourcePath.equals(targetPath)) {
            throw new BlobStoreException(SOURCE_AND_TARGET_SAME_ERROR);
        }

        BlobOptionSupport.validateSupportedOptions(SUPPORTED_COPY_MOVE_OPTIONS, options);
        BlobWriteMode writeMode = BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW, options);
        Path absoluteSourcePath = getBlobFilePath(sourcePath);
        transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, TransferKind.MOVE, writeMode);

        return getBlob(targetPath);
    }

    @Override
    public Blob moveBlob(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath, BlobOption... options)
        throws BlobStoreException
    {
        if (sourceStore instanceof FileSystemBlobStore fileSystemBlobStore) {
            BlobOptionSupport.validateSupportedOptions(SUPPORTED_COPY_MOVE_OPTIONS, options);
            Path absoluteSourcePath = fileSystemBlobStore.getBlobFilePath(sourcePath);
            BlobWriteMode writeMode = BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW, options);
            transferBlobInternal(sourcePath, targetPath, absoluteSourcePath, TransferKind.MOVE, writeMode);
            return getBlob(targetPath);
        } else {
            // For cross-store moves, use copy + delete approach
            Blob targetBlob = copyBlob(sourceStore, sourcePath, targetPath, options);
            sourceStore.deleteBlob(sourcePath);
            return targetBlob;
        }
    }

    @Override
    public boolean hasDescendants(BlobPath path) throws BlobStoreException
    {
        try (Stream<Blob> stream = listDescendants(path)) {
            return stream.findFirst().isPresent();
        }
    }

    private void transferBlobInternal(BlobPath sourcePath, BlobPath targetPath, Path absoluteSourcePath,
        TransferKind kind, BlobWriteMode writeMode) throws BlobStoreException
    {
        Path absoluteTargetPath = getBlobFilePath(targetPath);
        String operationName = kind.toString().toLowerCase(Locale.ROOT);

        NoSuchFileException lastNoSuchFileException = null;

        for (int attempt = 0; attempt < NUM_ATTEMPTS; ++attempt) {
            try {
                // Ensure parent directory exists
                createParents(absoluteTargetPath);

                CopyOption[] copyOptions = resolveCopyOptions(writeMode);

                // Use atomic operation - this fails if the target file already exists and the write mode
                // is not REPLACE_EXISTING. If the write mode is REPLACE_EXISTING, it will replace the existing file.
                if (kind == TransferKind.MOVE) {
                    Files.move(absoluteSourcePath, absoluteTargetPath, copyOptions);
                    cleanUpParents(absoluteSourcePath);
                } else {
                    Files.copy(absoluteSourcePath, absoluteTargetPath, copyOptions);
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
                throw new BlobStoreException(operationName + " blob failed", e);
            }
        }

        cleanUpParents(absoluteTargetPath);
        throw new BlobStoreException("%s blob failed after %d attempts".formatted(operationName, NUM_ATTEMPTS),
            lastNoSuchFileException);
    }

    private static CopyOption[] resolveCopyOptions(BlobWriteMode writeMode)
    {
        return writeMode == BlobWriteMode.REPLACE_EXISTING
            ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING }
            : new CopyOption[0];
    }

    void createParents(Path absoluteTargetPath) throws IOException
    {
        Path targetParent = absoluteTargetPath.getParent();
        if (targetParent != null) {
            Files.createDirectories(targetParent);
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
    public void deleteDescendants(BlobPath path) throws BlobStoreException
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
