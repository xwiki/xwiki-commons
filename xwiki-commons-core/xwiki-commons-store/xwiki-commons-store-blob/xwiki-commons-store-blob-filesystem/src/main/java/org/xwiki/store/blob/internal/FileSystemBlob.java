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
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.OptionalLong;
import java.util.Set;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobOption;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobRangeOption;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobWriteMode;

/**
 * A {@link Blob} implementation that represents a file in the file system.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class FileSystemBlob extends AbstractBlob<FileSystemBlobStore>
{
    private static final Set<Class<? extends BlobOption>> SUPPORTED_OUTPUT_OPTIONS = Set.of(
        BlobWriteMode.class
    );

    private static final Set<Class<? extends BlobOption>> SUPPORTED_INPUT_OPTIONS = Set.of(
        BlobRangeOption.class
    );

    private final Path absolutePath;

    /**
     * Creates a new FileSystemBlob.
     *
     * @param blobPath the path of the blob inside the store
     * @param absolutePath the absolute file system path to the blob
     * @param store the blob store where this blob is stored
     */
    public FileSystemBlob(BlobPath blobPath, Path absolutePath, FileSystemBlobStore store)
    {
        super(store, blobPath);
        this.absolutePath = absolutePath;
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
    public OutputStream getOutputStream(BlobOption... options) throws BlobStoreException
    {
        BlobOptionSupport.validateSupportedOptions(SUPPORTED_OUTPUT_OPTIONS, options);
        BlobWriteMode writeMode = BlobOptionSupport.findSingleOption(BlobWriteMode.class, options);

        NoSuchFileException lastNoSuchFileException = null;
        for (int attempt = 0; attempt < FileSystemBlobStore.NUM_ATTEMPTS; ++attempt) {
            try {
                // Ensure the parent directory exists before creating the output stream.
                this.blobStore.createParents(this.absolutePath);

                if (writeMode == BlobWriteMode.CREATE_NEW) {
                    // Use CREATE_NEW to ensure atomic create-only behavior
                    return Files.newOutputStream(this.absolutePath,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                } else {
                    // Default behavior - create or overwrite
                    return Files.newOutputStream(this.absolutePath);
                }
            } catch (FileAlreadyExistsException e) {
                throw new BlobAlreadyExistsException(this.blobPath, e);
            } catch (NoSuchFileException e) {
                // This can happen if the parent directory was deleted between our check and the attempt to create the
                // output stream. Loop around and try again. Remember the exception in case we fail multiple times.
                lastNoSuchFileException = e;
            } catch (IOException e) {
                // Something went wrong creating the output stream. Attempt to clean up any parent directories we may
                // have created.
                this.blobStore.cleanUpParents(this.absolutePath);
                throw new BlobStoreException("Error getting output stream.", e);
            }
        }

        // We failed multiple times due to missing parent directories. Give up.
        throw new BlobStoreException("Error creating parent directories for output stream in %s attempts."
            .formatted(FileSystemBlobStore.NUM_ATTEMPTS), lastNoSuchFileException);
    }

    @Override
    public InputStream getStream(BlobOption... options) throws BlobStoreException
    {
        BlobOptionSupport.validateSupportedOptions(SUPPORTED_INPUT_OPTIONS, options);
        BlobRangeOption rangeOption = BlobOptionSupport.findSingleOption(BlobRangeOption.class, options);
        try {
            if (rangeOption == null) {
                return Files.newInputStream(this.absolutePath, LinkOption.NOFOLLOW_LINKS);
            }
            return openRangeStream(rangeOption);
        } catch (NoSuchFileException e) {
            throw new BlobNotFoundException(this.blobPath, e);
        } catch (IOException e) {
            throw new BlobStoreException("Error getting input stream.", e);
        }
    }

    private InputStream openRangeStream(BlobRangeOption rangeOption) throws IOException
    {
        SeekableByteChannel channel = Files.newByteChannel(this.absolutePath, StandardOpenOption.READ);
        try {
            channel.position(rangeOption.getStartOffset());
            InputStream baseStream = Channels.newInputStream(channel);

            OptionalLong length = rangeOption.getLength();
            if (length.isEmpty()) {
                return baseStream;
            }

            return BoundedInputStream.builder()
                .setInputStream(baseStream)
                .setMaxCount(length.getAsLong())
                .get();
        } catch (IOException e) {
            channel.close();
            throw e;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FileSystemBlob that)) {
            return false;
        }

        return new EqualsBuilder().append(this.absolutePath, that.absolutePath).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(this.absolutePath).toHashCode();
    }
}
