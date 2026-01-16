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
package org.xwiki.classloader.internal;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

/**
 * Various {@link ClassLoader} tools to make easier to safely access resources.
 * 
 * @version $Id$
 * @since 17.4.0RC1
 * @since 16.10.7
 */
public final class ClassLoaderUtils
{
    private ClassLoaderUtils()
    {

    }

    private static String resolveResourceName(String prefixPath, String resourcePath)
    {
        String fullPath;
        if (StringUtils.isEmpty(prefixPath)) {
            fullPath = resourcePath;

            // Prevent access to resources from other directories
            // TODO: find or implement something closer to Servlet ClassLoader behavior to be as accurate as possible
            // and be able to reuse the normalized result. Not so easy since the various applications servers can use
            // different logics.

            // On Tomcat, all leading / have no effect, contrary to Paths#normalize()
            int index = 0;
            while (index < fullPath.length() && fullPath.charAt(index) == '/') {
                ++index;
            }
            String normalizedPath = fullPath.substring(index);

            Path normalizedResource = Paths.get(normalizedPath).normalize();
            if (normalizedResource.startsWith("../")) {
                throw new IllegalArgumentException(String.format(
                    "The provided resource name [%s] is trying to navigate out of the mandatory root location",
                    fullPath));
            }
        } else {
            fullPath = prefixPath + resourcePath;

            // Prevent access to resources from other directories
            // TODO: find or implement something closed to Servlet ClassLoader behavior to be as accurate as possible
            // and be able to reuse the normalized result. Not so easy since the various applications servers can use
            // different logics.
            Path normalizedResource = Paths.get(fullPath).normalize();
            if (!normalizedResource.startsWith(prefixPath)) {
                throw new IllegalArgumentException(String.format(
                    "The provided resource name [%s] is trying to navigate out of the mandatory prefix [%s]",
                    resourcePath, prefixPath));
            }
        }

        // We cannot sent back the normalized version as it might produce a result which is not compatible with the
        // ClassLoader (for example, on Windows Path#normalize() is replacing all "/" by "\", which is not a path
        // separator in ClassLoader)

        return fullPath;
    }

    /**
     * Returns an input stream for reading the specified resource.
     *
     * @param classloader the class loader in which to search for the resource
     * @param prefixPath the prefix of the path in which the resource should be found
     * @param resourcePath the path of the resource below the prefix
     * @return An input stream for reading the resource; {@code null} if the resource could not be found, the resource
     *         is in a package that is not opened unconditionally, or access to the resource is denied by the security
     *         manager.
     * @see ClassLoader#getResourceAsStream(String)
     */
    public static InputStream getResourceAsStream(ClassLoader classloader, String prefixPath, String resourcePath)
    {
        return classloader.getResourceAsStream(resolveResourceName(prefixPath, resourcePath));
    }

    /**
     * Returns an input stream for reading the specified resource.
     *
     * @param classloader the class loader in which to search for the resource
     * @param resourcePath the path of the resource below the prefix
     * @return An input stream for reading the resource; {@code null} if the resource could not be found, the resource
     *         is in a package that is not opened unconditionally, or access to the resource is denied by the security
     *         manager.
     * @see ClassLoader#getResourceAsStream(String)
     */
    public static InputStream getResourceAsStream(ClassLoader classloader, String resourcePath)
    {
        return getResourceAsStream(classloader, null, resourcePath);
    }

    /**
     * Finds the resource with the given name to which the given prefix is added.
     * <p>
     * This method is also making sure that the resource will remain below the given prefix (the given resource name
     * contains path traversal syntax).
     * 
     * @param classloader the class loader in which to search for the resource
     * @param prefixPath the prefix of the path in which the resource should be found
     * @param resourcePath the path of the resource below the prefix
     * @return {@code URL} object for reading the resource; {@code null} if the resource could not be found, a
     *         {@code URL} could not be constructed to locate the resource, the resource is in a package that is not
     *         opened unconditionally, or access to the resource is denied by the security manager.
     * @see ClassLoader#getResource(String)
     */
    public static URL getResource(ClassLoader classloader, String prefixPath, String resourcePath)
    {
        return classloader.getResource(resolveResourceName(prefixPath, resourcePath));
    }

    /**
     * Finds the resource with the given name to which the given prefix is added.
     * <p>
     * This method is also making sure that the resource will remain below the given prefix (the given resource name
     * contains path traversal syntax).
     * 
     * @param classloader the class loader in which to search for the resource
     * @param resourcePath the path of the resource below the prefix
     * @return {@code URL} object for reading the resource; {@code null} if the resource could not be found, a
     *         {@code URL} could not be constructed to locate the resource, the resource is in a package that is not
     *         opened unconditionally, or access to the resource is denied by the security manager.
     * @see ClassLoader#getResource(String)
     */
    public static URL getResource(ClassLoader classloader, String resourcePath)
    {
        return getResource(classloader, null, resourcePath);
    }
}
