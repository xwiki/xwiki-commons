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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.xwiki.stability.Unstable;

/**
 * Mutable properties map used to build a typed properties bean for creating a BlobStore.
 * Holds the store name, type, and implementation-specific options keyed by property id.
 * The name and type are stored separately from the options map and are immutable after construction.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public final class BlobStorePropertiesBuilder
{
    private final String name;

    private final String type;

    private final Map<String, Object> options = new HashMap<>();

    /**
     * Create a new builder with the specified name and type.
     *
     * @param name the store name
     * @param type the type of the blob store (e.g., "filesystem", "s3")
     */
    public BlobStorePropertiesBuilder(String name, String type)
    {
        this.name = name;
        this.type = type;
    }

    /**
     * @return the name of the store
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the store type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * Get an option value by its property id.
     *
     * @param propertyId the property id
     * @return an optional value
     */
    public Optional<Object> get(String propertyId)
    {
        return Optional.ofNullable(this.options.get(propertyId));
    }

    /**
     * Set an option value using a property id.
     *
     * @param propertyId the property id
     * @param value the value
     * @return this builder
     */
    public BlobStorePropertiesBuilder set(String propertyId, Object value)
    {
        this.options.put(propertyId, value);
        return this;
    }

    /**
     * Remove an option by property id.
     *
     * @param propertyId the property id
     * @return this builder
     */
    public BlobStorePropertiesBuilder remove(String propertyId)
    {
        this.options.remove(propertyId);
        return this;
    }

    /**
     * @return a view of all properties keyed by property id (excludes name and type)
     */
    public Map<String, Object> getAllProperties()
    {
        return this.options;
    }
}
