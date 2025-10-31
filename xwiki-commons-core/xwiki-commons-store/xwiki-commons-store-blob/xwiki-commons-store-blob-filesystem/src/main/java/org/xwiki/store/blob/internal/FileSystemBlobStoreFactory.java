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

import java.nio.file.Path;
import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.environment.Environment;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreFactory;
import org.xwiki.store.blob.BlobStoreProperties;
import org.xwiki.store.blob.BlobStorePropertiesBuilder;
import org.xwiki.store.blob.FileSystemBlobStoreProperties;

/**
 * Factory for filesystem BlobStore backed by {@link FileSystemBlobStore}.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component
@Singleton
@Named("filesystem")
public class FileSystemBlobStoreFactory implements BlobStoreFactory
{
    @Inject
    private Environment environment;

    @Override
    public String getHint()
    {
        return "filesystem";
    }

    @Override
    public Class<? extends BlobStoreProperties> getPropertiesClass()
    {
        return FileSystemBlobStoreProperties.class;
    }

    @Override
    public BlobStorePropertiesBuilder newPropertiesBuilder(String name) throws BlobStoreException
    {
        Objects.requireNonNull(name, "Blob store name cannot be null");
        BlobStorePropertiesBuilder builder = new BlobStorePropertiesBuilder(name, getHint());

        // Default base path: $permanentDir/<name>
        Path basePath = this.environment.getPermanentDirectory().toPath().resolve(name);
        builder.set(FileSystemBlobStoreProperties.ROOT_DIRECTORY, basePath);

        return builder;
    }

    @Override
    public BlobStore create(String name, BlobStoreProperties properties) throws BlobStoreException
    {
        if (!(properties instanceof FileSystemBlobStoreProperties fileSystemProperties)) {
            throw new BlobStoreException("Invalid properties type for filesystem blob store factory: "
                + properties.getClass().getName());
        }

        return new FileSystemBlobStore(name, fileSystemProperties);
    }
}
