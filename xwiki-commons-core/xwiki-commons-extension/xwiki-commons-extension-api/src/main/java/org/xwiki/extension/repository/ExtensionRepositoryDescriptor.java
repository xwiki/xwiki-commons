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
import java.util.Map;

/**
 * Describe a repository.
 * 
 * @version $Id$
 * @since 4.3M1
 */
public interface ExtensionRepositoryDescriptor
{
    /**
     * @return the unique identifier
     */
    String getId();

    /**
     * @return the repository type (maven, xwiki, etc.)
     */
    String getType();

    /**
     * @return the repository address
     */
    URI getURI();

    /**
     * @param key the property key
     * @return the property value
     */
    String getProperty(String key);

    /**
     * Extends {@link ExtensionRepositoryDescriptor} standard properties.
     * 
     * @return the properties
     */
    Map<String, String> getProperties();
}
