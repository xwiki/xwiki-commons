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
package org.xwiki.extension.repository;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.stability.Unstable;

/**
 * Default implementation of {@link ExtensionRepositoryDescriptor}.
 *
 * @version $Id$
 * @since 4.3M1
 */
public class DefaultExtensionRepositoryDescriptor implements ExtensionRepositoryDescriptor
{
    /**
     * @see #getId()
     */
    private final String id;

    /**
     * @see #getType()
     */
    private final String type;

    /**
     * @see #getURI()
     */
    private final URI uri;

    /**
     * @see #getProperties()
     */
    private Map<String, String> properties = new HashMap<>();

    private transient int hashCode;

    /**
     * @param descriptor the identifier to clone
     */
    public DefaultExtensionRepositoryDescriptor(ExtensionRepositoryDescriptor descriptor)
    {
        this(descriptor.getId(), descriptor.getType(), descriptor.getURI());

        setProperties(descriptor.getProperties());
    }

    /**
     * Constructor to use only for "virtual" repositories having no storage like the ExtensionRepositoryManager.
     * 
     * @param id the unique identifier
     * @since 8.3RC1
     */
    public DefaultExtensionRepositoryDescriptor(String id)
    {
        this(id, null, null);
    }

    /**
     * @param id the unique identifier
     * @param type the repository type (maven, xwiki, etc.)
     * @param uri the repository address
     */
    public DefaultExtensionRepositoryDescriptor(String id, String type, URI uri)
    {
        this.id = id;
        this.type = type;
        this.uri = uri;
    }

    /**
     * @param id the unique identifier
     * @param type the repository type (maven, xwiki, etc.)
     * @param uri the repository address
     * @param properties the properties
     * @since 12.2
     */
    @Unstable
    public DefaultExtensionRepositoryDescriptor(String id, String type, URI uri, Map<String, String> properties)
    {
        this.id = id;
        this.type = type;
        this.uri = uri;

        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getType()
    {
        return this.type;
    }

    @Override
    public URI getURI()
    {
        return this.uri;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public String getProperty(String key)
    {
        return this.properties.get(key);
    }

    /**
     * Set a property.
     *
     * @param key the property key
     * @param value the property value
     * @see #getProperty(String)
     */
    public void putProperty(String key, String value)
    {
        this.properties.put(key, value);
    }

    /**
     * Replace existing properties with provided properties.
     *
     * @param properties the properties
     */
    public void setProperties(Map<String, String> properties)
    {
        this.properties.clear();

        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ExtensionRepositoryDescriptor) {
            ExtensionRepositoryDescriptor repository = (ExtensionRepositoryDescriptor) obj;

            return Objects.equals(getId(), repository.getId()) && Objects.equals(getType(), repository.getType())
                && Objects.equals(getURI(), repository.getURI())
                && Objects.equals(getProperties(), repository.getProperties());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        if (this.hashCode == 0) {
            HashCodeBuilder builder = new HashCodeBuilder();

            builder.append(getId());
            builder.append(getType());
            builder.append(getURI());
            builder.append(getProperties());

            this.hashCode = builder.toHashCode();
        }

        return this.hashCode;
    }

    @Override
    public String toString()
    {
        return getId() + ':' + getType() + ':' + getURI();
    }
}
