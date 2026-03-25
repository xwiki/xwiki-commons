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
package org.xwiki.environment;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Abstraction that represents an Environment (Java SE, Servlet, Portlet, etc) and provides API to access
 * environment-related data.
 *
 * @version $Id$
 * @since 3.5M1
 */
@Role
@NullMarked
public interface Environment
{
    /**
     * Gets the directory for storing temporary data. The content of this directory may be deleted across restarts and
     * thus is not a safe place to store permanent/important data.
     *
     * @return a {@link File} object pointing to a directory that the application can use for storing temporary files
     */
    @Nullable
    File getTemporaryDirectory();

    /**
     * Gets the root directory of a location for storing persisting data. Contrary to the Temporary Directory the
     * content of this directory is guaranteed to persist across time.
     *
     * @return a {@link File} object pointing to the root folder of the permanent directory
     */
    @Nullable
    File getPermanentDirectory();

    /**
     * @param resourcePath the full path of the resource to access (eg "/somefile.properties")
     * @return the resource location as a {@link URL} or null if not found
     */
    @Nullable
    URL getResource(String resourcePath);

    /**
     * @param resourcePath the full path of the resource to access (eg "/somefile.properties")
     * @return the resource location as an {@link InputStream} or <code>null</code> if no resource exists at the
     *         specified name
     */
    @Nullable
    InputStream getResourceAsStream(String resourcePath);

    /**
     * @param prefixPath the resource folder from where to search for the resource
     * @param resourcePath the path of the resource to access, relative to the prefix
     * @return the resource location as a {@link URL}, or null if no resource with the provided path could be found (or
     *         if the resource path is trying to access a resource outside of the specified prefix)
     * @since 17.10.5
     * @since 18.2.0
     */
    @Unstable
    @Nullable
    default URL getResource(String prefixPath, String resourcePath)
    {
        return getResource(prefixPath + resourcePath);
    }

    /**
     * @param prefixPath the resource folder from where to search for the resource
     * @param resourcePath the path of the resource to access, relative to the prefix
     * @return the resource content as an {@link InputStream}, or null if no resource with the provided path could be
     *         found (or if the resource path is trying to access a resource outside of the specified prefix)
     * @since 17.10.5
     * @since 18.2.0
     */
    @Unstable
    @Nullable
    default InputStream getResourceAsStream(String prefixPath, String resourcePath)
    {
        return getResourceAsStream(prefixPath + resourcePath);
    }
}
