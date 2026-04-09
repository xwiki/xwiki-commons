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
import java.io.OutputStream;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InvalidExtensionException;
import org.xwiki.extension.internal.PathUtils;
import org.xwiki.extension.repository.internal.ExtensionSerializer;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;

/**
 * Manipulate the extension filesystem repository storage.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class LocalExtensionStorage
{
    /**
     * Logging tool.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalExtensionStorage.class);

    /**
     * The extension of the descriptor files.
     */
    private static final String DESCRIPTOR_EXT = "xed";

    /**
     * The repository.
     */
    private DefaultLocalExtensionRepository repository;

    /**
     * Used to read/write in the repository storage itself.
     */
    private ExtensionSerializer extensionSerializer;

    /**
     * @see #getBlobStore()
     */
    private BlobStore store;

    /**
     * @param repository the repository
     * @param store the blob store where the extension files are located
     * @param componentManager used to lookup needed components
     * @throws ComponentLookupException can't find ExtensionSerializer
     * @since 18.2.0RC1
     */
    public LocalExtensionStorage(DefaultLocalExtensionRepository repository, BlobStore store,
        ComponentManager componentManager) throws ComponentLookupException
    {
        this.repository = repository;
        this.store = store;

        this.extensionSerializer = componentManager.getInstance(ExtensionSerializer.class);
    }

    /**
     * @return the repository folder
     * @since 18.2.0RC1
     */
    public BlobStore getBlobStore()
    {
        return this.store;
    }

    /**
     * Load extension from repository storage.
     *
     * @throws IOException when failing to load extensions
     */
    void loadExtensions() throws IOException
    {
        // Load local extension from repository

        Stream<Blob> blobStream;
        try {
            blobStream = this.store.listDescendants(BlobPath.root());
        } catch (BlobStoreException e) {
            throw new IOException("Failed to list blobs in local repository", e);
        }

        blobStream.forEach(blob -> {
            if (blob.getPath().getFileName().endsWith(DESCRIPTOR_EXT)) {
                try {
                    DefaultLocalExtension localExtension = loadDescriptor(blob);

                    this.repository.addLocalExtension(localExtension);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load extension from blob [" + blob.getPath() + "] in local repository", e);
                }
            }
        });
    }

    /**
     * Local extension descriptor from a file.
     *
     * @param descriptor the descriptor file
     * @return the extension descriptor
     * @throws InvalidExtensionException error when trying to load extension descriptor
     */
    private DefaultLocalExtension loadDescriptor(Blob descriptor) throws InvalidExtensionException
    {
        InputStream fis;
        try {
            fis = descriptor.getStream();
        } catch (BlobStoreException e) {
            throw new InvalidExtensionException("Failed to open descriptor for reading", e);
        }

        try {
            DefaultLocalExtension localExtension =
                this.extensionSerializer.loadLocalExtensionDescriptor(this.repository, fis);

            localExtension.setDescriptorBlob(descriptor);

            BlobPath extensionPath = getBlobPath(descriptor.getPath(), DESCRIPTOR_EXT, localExtension.getType());
            if (extensionPath != null) {
                localExtension.setBlob(this.store.getBlob(extensionPath));

                if (!localExtension.getFile().getBlob().exists()) {
                    throw new InvalidExtensionException("Failed to load local extension [" + descriptor + "]: ["
                        + localExtension.getFile() + "] file does not exist");
                }
            }

            return localExtension;
        } catch (BlobStoreException e) {
            throw new InvalidExtensionException("Failed to open access the extension blob", e);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close stream for file [" + descriptor + "]", e);
            }
        }
    }

    /***
     * Update the extension descriptor in the filesystem repository.
     *
     * @param extension the local extension descriptor to save
     * @throws ParserConfigurationException error when trying to save the descriptor
     * @throws TransformerException error when trying to save the descriptor
     * @throws IOException error when trying to save the descriptor
     * @throws BlobStoreException error when trying to save the descriptor
     */
    public void saveDescriptor(DefaultLocalExtension extension)
        throws ParserConfigurationException, TransformerException, IOException, BlobStoreException
    {
        Blob blob = extension.getDescriptorBlob();

        if (blob == null) {
            blob = getNewDescriptorBlob(extension.getId());
            extension.setDescriptorBlob(blob);
        }

        try (OutputStream os = blob.getOutputStream()) {
            this.extensionSerializer.saveExtensionDescriptor(extension, os);
        }
    }

    Blob getNewExtensionBlob(ExtensionId id, String type) throws BlobStoreException
    {
        return this.store.getBlob(getBlobPath(id, type));
    }

    private Blob getNewDescriptorBlob(ExtensionId id) throws BlobStoreException
    {
        return this.store.getBlob(getBlobPath(id, DESCRIPTOR_EXT));
    }

    /**
     * @param basePath the path to the extension file
     * @param baseType the type of the extension
     * @param type the type of the file to get
     * @return the path to the extension descriptor
     */
    private BlobPath getBlobPath(BlobPath basePath, String baseType, String type)
    {
        if (StringUtils.isEmpty(type)) {
            return null;
        }

        String baseName = getBaseName(basePath.getFileName().toString(), baseType);

        return basePath.getParent().resolve(baseName + '.' + PathUtils.encode(type));
    }

    /**
     * @param fileName the name of the file of the provided type
     * @param type the type of the file
     * @return the base name which is the name without the typed extension
     */
    private String getBaseName(String fileName, String type)
    {
        return fileName.substring(0, fileName.length() - PathUtils.encode(type).length() - 1);
    }

    /**
     * Get blob path in the local extension repository.
     *
     * @param id the extension id
     * @param fileExtension the file extension
     * @return the encoded file path
     */
    private BlobPath getBlobPath(ExtensionId id, String fileExtension)
    {
        String encodedId = PathUtils.encode(id.getId());
        String encodedVersion = PathUtils.encode(id.getVersion().toString());
        String encodedType = PathUtils.encode(fileExtension);

        return BlobPath.absolute(encodedId, encodedVersion, encodedId + '-' + encodedVersion + '.' + encodedType);
    }

    /**
     * Remove extension from storage.
     *
     * @param extension extension to remove
     * @throws IOException error when deleting the extension
     */
    public void removeExtension(DefaultLocalExtension extension) throws IOException
    {
        Blob descriptorBlob = extension.getDescriptorBlob();

        if (descriptorBlob == null) {
            throw new IOException(
                String.format("Extension [%s] does not exist: descriptor file is null", extension.getId().getId()));
        }

        // Get extension file descriptor path
        BlobPath extensionDescriptorFilePath = descriptorBlob.getPath();

        // Delete the extension descriptor file
        try {
            this.store.deleteBlob(extensionDescriptorFilePath);
        } catch (BlobStoreException e) {
            LOGGER.warn(
                "Couldn't delete the extension descriptor file [{}] when removing extension [{}]. Root error: [{}]",
                extensionDescriptorFilePath.toString(), extension.getId().getId(),
                ExceptionUtils.getRootCauseMessage(e));
        }

        DefaultLocalExtensionFile extensionFile = extension.getFile();

        if (extensionFile != null) {
            BlobPath extensionFilePath = extensionFile.getBlob().getPath();

            // Delete the extension file
            try {
                this.store.deleteBlob(extensionFilePath);
            } catch (BlobStoreException e) {
                LOGGER.warn("Could not deleted the extension blob [{}] for extension [{}]. Root error: [{}]",
                    extensionFilePath.toString(), extension.getId().getId(), ExceptionUtils.getRootCauseMessage(e));
            }
        }
    }
}
