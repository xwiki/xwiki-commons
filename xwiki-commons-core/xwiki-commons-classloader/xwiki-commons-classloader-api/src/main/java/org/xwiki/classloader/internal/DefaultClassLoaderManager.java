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

import java.io.IOException;
import java.net.URLStreamHandlerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.classloader.NamespaceURLClassLoader;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

/**
 * Default implementation of {@link ClassLoaderManager}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultClassLoaderManager implements ClassLoaderManager, Initializable, Disposable
{
    /**
     * The readonly container loader that should be used as parent of all the namespace classloader.
     */
    protected ClassLoader containerClassLoader;

    /**
     * The class loader corresponding to null namespace.
     */
    private volatile NamespaceURLClassLoader rootClassLoader;

    /**
     * The Stream handler factory to use in the created classloader in order to be able to load our custom handlers
     * protocol.
     */
    @Inject
    private URLStreamHandlerFactory streamHandlerFactory;

    @Inject
    private Logger logger;

    /**
     * The classloaders stored by namespace.
     */
    private Map<String, NamespaceURLClassLoader> wikiClassLoaderMap = new ConcurrentHashMap<>();

    @Override
    public void initialize() throws InitializationException
    {
        this.containerClassLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * Allow overriding the system classloader during tests.
     *
     * @return a ClassLoader to be used as the system parent
     */
    protected ClassLoader getContainerClassLoader()
    {
        return this.containerClassLoader;
    }

    @Override
    public NamespaceURLClassLoader getURLClassLoader(String namespace, boolean create)
    {
        // Make sure root classloader exist
        if (this.rootClassLoader == null) {
            synchronized (this) {
                if (this.rootClassLoader == null) {
                    this.rootClassLoader =
                        new NamespaceURLClassLoader(getContainerClassLoader(), this.streamHandlerFactory, null);
                }
            }
        }

        // Return root classloader by default
        NamespaceURLClassLoader wikiClassLoader = this.rootClassLoader;

        // Find classloader corresponding to passed namespace
        if (namespace != null) {
            wikiClassLoader = this.wikiClassLoaderMap.get(namespace);

            if (wikiClassLoader == null) {
                if (create) {
                    // Create classloader
                    wikiClassLoader =
                        new NamespaceURLClassLoader(this.rootClassLoader, this.streamHandlerFactory, namespace);

                    // Store new classloader
                    this.wikiClassLoaderMap.put(namespace, wikiClassLoader);
                } else {
                    wikiClassLoader = this.rootClassLoader;
                }
            }
        }

        return wikiClassLoader;
    }

    @Override
    public void dropURLClassLoaders()
    {
        if (this.rootClassLoader != null) {
            for (String namespace : this.wikiClassLoaderMap.keySet()) {
                dropURLClassLoader(namespace);
            }

            // Reset and close root classloader
            NamespaceURLClassLoader classloader = this.rootClassLoader;
            this.rootClassLoader = null;
            try {
                classloader.close();
            } catch (IOException e) {
                this.logger.warn("Failed to close root Classloader", e);
            }
        }
    }

    @Override
    public void dropURLClassLoader(String namespace)
    {
        if (namespace == null) {
            // If root namespace drop all namespaces
            dropURLClassLoaders();
        } else {
            // Remove the classloader from the map
            NamespaceURLClassLoader classloader = this.wikiClassLoaderMap.remove(namespace);

            if (classloader != null) {
                // Close the classloader
                try {
                    classloader.close();
                } catch (IOException e) {
                    this.logger.warn("Failed to close Classloader", e);
                }
            }
        }
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        dropURLClassLoaders();
    }
}
