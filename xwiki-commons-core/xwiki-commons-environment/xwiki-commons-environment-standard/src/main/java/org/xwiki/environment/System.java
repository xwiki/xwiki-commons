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

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.environment.internal.StandardEnvironment;

/**
 * Helper class that can be used to easily initialize the full XWiki System (ie Component Manager and Environment).
 *
 * @version $Id$
 * @since 3.5M1
 */
public final class System
{
    /**
     * Helper static class, no public constructor.
     */
    private System()
    {
        // Prevent instantiation, this is a helper class.
    }

    /**
     * Initialize the full XWiki system (ie Component Manager and Environment), using the "java.io.tmpdir" System
     * property value as the temporary diretory location, without any resource directory set and without any permanent
     * directory set either.
     *
     * @param classLoader see {@link EmbeddableComponentManager#initialize(ClassLoader)}
     * @return the initialized Component Manager
     * @see #dispose(ComponentManager)
     */
    public static ComponentManager initialize(ClassLoader classLoader)
    {
        return initialize(null, null, null, classLoader);
    }

    /**
     * Initialize the full XWiki system (ie Component Manager and Environment), using the class loader that loaded
     * this class, using the "java.io.tmpdir" System property value as the temporary diretory location, without
     * any resource directory set and without any permanent directory set either.
     *
     * @return the initialized Component Manager
     * @see #dispose(ComponentManager)
     */
    public static ComponentManager initialize()
    {
        return initialize((File) null);
    }

    /**
     * Initialize the full XWiki system (ie Component Manager and Environment), using the class loader that loaded
     * this class, using the "java.io.tmpdir" System property value as the temporary diretory location and without
     * any resource directory set (the permanent directory will be used as the resource directory).
     *
     * @param permanentDirectory see {@link org.xwiki.environment.Environment#getPermanentDirectory()}
     * @return the initialized Component Manager
     * @see #dispose(ComponentManager)
     */
    public static ComponentManager initialize(File permanentDirectory)
    {
        return initialize(permanentDirectory, null);
    }

    /**
     * Initialize the full XWiki system (ie Component Manager and Environment), using the class loader that loaded
     * this class and using the "java.io.tmpdir" System property value as the temporary diretory location.
     *
     * @param permanentDirectory see {@link org.xwiki.environment.Environment#getPermanentDirectory()}
     * @param resourceDirectory see
     *            {@link org.xwiki.environment.internal.StandardEnvironment#setResourceDirectory(java.io.File)}
     * @return the initialized Component Manager
     * @see #dispose(ComponentManager)
     */
    public static ComponentManager initialize(File permanentDirectory, File resourceDirectory)
    {
        return initialize(permanentDirectory, resourceDirectory, null);
    }

    /**
     * Initialize the full XWiki system (ie Component Manager and Environment), using the class loader that loaded
     * this class.
     *
     * @param permanentDirectory see {@link org.xwiki.environment.Environment#getPermanentDirectory()}
     * @param resourceDirectory see
     *            {@link org.xwiki.environment.internal.StandardEnvironment#setResourceDirectory(java.io.File)}
     * @param temporaryDirectory see {@link org.xwiki.environment.Environment#getTemporaryDirectory()}
     * @return the initialized Component Manager
     * @see #dispose(ComponentManager)
     */
    public static ComponentManager initialize(File permanentDirectory, File resourceDirectory, File temporaryDirectory)
    {
        return initialize(permanentDirectory, resourceDirectory, temporaryDirectory, null);
    }

    /**
     * Initialize the full XWiki system (ie Component Manager and Environment).
     *
     * @param permanentDirectory see {@link org.xwiki.environment.Environment#getPermanentDirectory()}
     * @param resourceDirectory see
     *            {@link org.xwiki.environment.internal.StandardEnvironment#setResourceDirectory(java.io.File)}
     * @param temporaryDirectory see {@link org.xwiki.environment.Environment#getTemporaryDirectory()}
     * @param classLoader see {@link EmbeddableComponentManager#initialize(ClassLoader)}
     * @return the initialized Component Manager
     * @see #dispose(ComponentManager)
     */
    public static ComponentManager initialize(File permanentDirectory, File resourceDirectory, File temporaryDirectory,
        ClassLoader classLoader)
    {
        // Step 1: Initialize Component system
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.initialize(classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader);

        // Step 2: Initialize Environment
        StandardEnvironment environment;
        try {
            environment = ecm.getInstance(Environment.class);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to find Standard Environment", e);
        }
        environment.setPermanentDirectory(permanentDirectory);
        environment.setResourceDirectory(resourceDirectory);
        environment.setTemporaryDirectory(temporaryDirectory);

        return ecm;
    }

    /**
     * Free resource taken by the Component Manager create by one of the <code>initialize</code> methods.
     *
     * @param componentManager the component manager
     * @since 5.1
     */
    public static void dispose(ComponentManager componentManager)
    {
        EmbeddableComponentManager ecm = (EmbeddableComponentManager) componentManager;
        ecm.dispose();
    }
}
