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

import jakarta.validation.constraints.NotBlank;

import org.xwiki.properties.annotation.PropertyMandatory;

/**
 * Base implementation of {@link BlobStoreProperties} that provides common functionality for blob store configurations.
 * This class defines the basic properties that all blob stores need to have.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class BaseBlobStoreProperties implements BlobStoreProperties
{
    /**
     * The name of the blob store.
     */
    private String name;

    /**
     * The type of the blob store.
     */
    @NotBlank
    private String type;

    /**
     * Gets the name of the blob store.
     *
     * @return the name of the blob store
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name of the blob store.
     *
     * @param name the name to set
     */
    @PropertyMandatory
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the type of the blob store.
     *
     * @return the type of the blob store
     */
    @Override
    public String getType()
    {
        return this.type;
    }

    /**
     * Sets the type of the blob store.
     *
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }
}
