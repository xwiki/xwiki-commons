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
package org.xwiki.store.blob;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Factory for creating BlobStore instances.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Role
@Unstable
public interface BlobStoreFactory
{
    /**
     * @return the hint this factory supports (e.g., "filesystem", "s3")
     */
    String getHint();

    /**
     * Create a new properties builder pre-initialized with defaults for the given store name.
     * Implementations should set any implementation-specific defaults.
     *
     * @param name the store name
     * @return a property builder initialized with defaults
     * @throws BlobStoreException if the given name is invalid
     */
    BlobStorePropertiesBuilder newPropertiesBuilder(String name) throws BlobStoreException;

    /**
     * Create a BlobStore from the given name and properties.
     *
     * @param name the unique name for this blob store
     * @param properties the populated properties bean to use
     * @return a BlobStore instance
     * @throws BlobStoreException if the properties are invalid or creation fails
     */
    BlobStore create(String name, BlobStoreProperties properties) throws BlobStoreException;

    /**
     * @return the properties class used by this factory
     */
    Class<? extends BlobStoreProperties> getPropertiesClass();
}
