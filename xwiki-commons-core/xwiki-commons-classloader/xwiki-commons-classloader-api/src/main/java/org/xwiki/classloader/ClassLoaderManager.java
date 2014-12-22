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
package org.xwiki.classloader;

import org.xwiki.component.annotation.Role;

/**
 * Store and create automatically class loaders by namespace.
 * <p>
 * All classloaders inherit from root classloader which is associated to null namespace.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Role
public interface ClassLoaderManager
{
    /**
     * Create and get classloader associated to the provided namespace.
     *
     * @param namespace the namespace
     * @param create true if the class loader should be created if it does not exists
     * @return the class loader, if none can be found and <code>create</create> is false return root classloader
     */
    NamespaceURLClassLoader getURLClassLoader(String namespace, boolean create);

    /**
     * Remove all classloaders.
     */
    void dropURLClassLoaders();

    /**
     * Remove the classloader associated to the provided namespace.
     *
     * @param namespace the namespace
     */
    void dropURLClassLoader(String namespace);
}
