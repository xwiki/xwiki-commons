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
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * @param descriptor the identifier to clone
     */
    public DefaultExtensionRepositoryDescriptor(ExtensionRepositoryDescriptor descriptor)
    {
        this(descriptor.getId(), descriptor.getType(), descriptor.getURI());

        setProperties(descriptor.getProperties());
    }

    /**
     * @param id the unique identifier
     * @param type the repository type (maven, xwiki, etc.)
     * @param uri the repository adress
     */
    public DefaultExtensionRepositoryDescriptor(String id, String type, URI uri)
    {
        this.id = id;
        this.type = type;
        this.uri = uri;
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
        this.properties.putAll(properties);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ExtensionRepositoryDescriptor) {
            ExtensionRepositoryDescriptor extensionId = (ExtensionRepositoryDescriptor) obj;

            return getId().equals(extensionId.getId()) && getType().equals(extensionId.getType())
                && getURI().equals(extensionId.getURI());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return getId() + ':' + getType() + ':' + getURI();
    }
}
