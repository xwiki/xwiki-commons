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
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public final class BlobStorePropertiesBuilder
{
    private static final String NAME = "name";

    private static final String TYPE = "type";

    private final Map<String, Object> options = new HashMap<>();

    /**
     * Create a new builder with the specified name and type.
     *
     * @param name the unique store name
     * @param type the store type (factory hint)
     */
    public BlobStorePropertiesBuilder(String name, String type)
    {
        this.options.put(NAME, name);
        this.options.put(TYPE, type);
    }

    /**
     * @return the unique store name
     */
    public String getName()
    {
        return this.options.get(NAME).toString();
    }

    /**
     * @return the store type (factory hint)
     */
    public String getType()
    {
        return this.options.get(TYPE).toString();
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
     * @throws IllegalArgumentException if trying to set name or type properties
     */
    public BlobStorePropertiesBuilder set(String propertyId, Object value)
    {
        if (isImmutable(propertyId)) {
            throw new IllegalArgumentException("Cannot modify immutable property: " + propertyId);
        }
        this.options.put(propertyId, value);
        return this;
    }

    /**
     * Remove an option by property id.
     *
     * @param propertyId the property id
     * @return this builder
     * @throws IllegalArgumentException if trying to remove name or type properties
     */
    public BlobStorePropertiesBuilder remove(String propertyId)
    {
        if (isImmutable(propertyId)) {
            throw new IllegalArgumentException("Cannot remove immutable property: " + propertyId);
        }
        this.options.remove(propertyId);
        return this;
    }

    /**
     * @return a view of all properties keyed by property id
     */
    public Map<String, Object> getAllProperties()
    {
        return this.options;
    }


    private static boolean isImmutable(String propertyId)
    {
        return NAME.equals(propertyId) || TYPE.equals(propertyId);
    }
}
