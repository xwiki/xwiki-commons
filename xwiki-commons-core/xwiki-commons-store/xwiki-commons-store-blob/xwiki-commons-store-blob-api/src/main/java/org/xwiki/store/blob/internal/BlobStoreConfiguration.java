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
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Configuration for the Blob Store.
 *
 * @version $Id$
 * @since 17.9.0RC1
 */
@Component(roles = BlobStoreConfiguration.class)
@Singleton
public class BlobStoreConfiguration
{
    private static final String FILESYSTEM_STORE_HINT = "filesystem";

    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configurationSource;

    /**
     * @return the hint for the blob store type to use, e.g., "filesystem" or "s3". This is used to determine which blob
     *     store manager to use when creating a new blob store.
     */
    public String getStoreHint()
    {
        return this.configurationSource.getProperty("store.blobStoreHint", FILESYSTEM_STORE_HINT);
    }

    /**
     * @return the hint for the blob store from which data should be migrated when there is no data in the current blob
     *    store. This can be used, e.g., to migrate from the filesystem blob store to an S3 blob store.
     */
    public String getMigrationStoreHint()
    {
        return this.configurationSource.getProperty("store.blobMigrationStoreHint", FILESYSTEM_STORE_HINT);
    }
}
