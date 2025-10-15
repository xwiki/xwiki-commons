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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.environment.Environment;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreManager;

/**
 * Blob store manager for the file-system-based blob store.
 *
 * @version $Id$
 * @since 17.9.0RC1
 */
@Component
@Singleton
@Named("filesystem")
public class FileSystemBlobStoreManager implements BlobStoreManager
{
    @Inject
    private Environment environment;

    @Override
    public BlobStore getBlobStore(String name) throws BlobStoreException
    {
        if (StringUtils.isBlank(name)) {
            throw new BlobStoreException("The blob store name must not be null or empty");
        }

        Path basePath = this.environment.getPermanentDirectory().toPath().resolve(name);
        return new FileSystemBlobStore(name, basePath);
    }
}
