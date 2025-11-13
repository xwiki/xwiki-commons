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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Configuration for the Blob Store.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component(roles = BlobStoreConfiguration.class)
@Singleton
public class BlobStoreConfiguration
{
    private static final String FILESYSTEM_STORE_TYPE = "filesystem";

    @Inject
    @Named("restricted")
    private Provider<ConfigurationSource> configurationSourceProvider;

    /**
     * @return the type of the blob store type to use, e.g., "filesystem" or "s3". This is used to determine which blob
     *     store factory to use when creating a new blob store.
     */
    public String getStoreType()
    {
        return this.configurationSourceProvider.get().getProperty("store.blobStoreType", FILESYSTEM_STORE_TYPE);
    }

    /**
     * @return the type of the blob store from which data should be migrated when there is no data in the current blob
     *    store. This can be used, e.g., to migrate from the filesystem blob store to an S3 blob store.
     */
    public String getMigrationStoreType()
    {
        return this.configurationSourceProvider.get()
            .getProperty("store.blobMigrationStoreType", FILESYSTEM_STORE_TYPE);
    }
}
