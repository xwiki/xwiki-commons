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
package org.xwiki.extension.job;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.job.Request;

/**
 * Request used in {@link org.xwiki.extension.job.internal.InstallJob}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class InstallRequest extends AbstractExtensionRequest
{
    /**
     * @see #getExtensionProperties()
     */
    public static final String PROPERTY_EXTENSION_PROPERTIES = "extension.properties";

    /**
     * @see #isFailOnExist()
     */
    public static final String PROPERTY_EXTENSION_FAIL_ON_EXIST = "extension.failOnExist";

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public InstallRequest()
    {
    }

    /**
     * @param request the request to copy
     */
    public InstallRequest(Request request)
    {
        super(request);
    }

    /**
     * @return whether or not the wiki creation should fail if the database already exists
     * @since 14.5RC1
     */
    public boolean isFailOnExist()
    {
        return getProperty(PROPERTY_EXTENSION_FAIL_ON_EXIST, true);
    }

    /**
     * @param failOnExist whether or not the wiki creation should fail if the database already exists
     * @since 14.5RC1
     */
    public void setFailOnExist(boolean failOnExist)
    {
        setProperty(PROPERTY_EXTENSION_FAIL_ON_EXIST, failOnExist);
    }

    /**
     * @return the custom extension properties to be set on each of the extensions that are going to be installed from
     *         this request
     * @since 7.0M2
     */
    public Map<String, Object> getExtensionProperties()
    {
        Map<String, Object> extensionProperties = getProperty(PROPERTY_EXTENSION_PROPERTIES);
        if (extensionProperties == null) {
            extensionProperties = new HashMap<>();
            setProperty(PROPERTY_EXTENSION_PROPERTIES, extensionProperties);
        }
        return extensionProperties;
    }

    /**
     * Sets a custom extension property to be set on each of the extensions that are going to be installed from this
     * request.
     * 
     * @param key the property name
     * @param value the new property value
     * @return the previous property value
     * @since 7.0M2
     */
    public Object setExtensionProperty(String key, Object value)
    {
        return getExtensionProperties().put(key, value);
    }

    /**
     * Sets custom extension properties to be set on each of the extensions that are going to be installed from this
     * request.
     * 
     * @param properties the properties to add to custom extension properties
     * @see #setExtensionProperty(String, Object)
     * @since 9.5RC1
     */
    public void addExtensionProperties(Map<String, Object> properties)
    {
        getExtensionProperties().putAll(properties);
    }
}
