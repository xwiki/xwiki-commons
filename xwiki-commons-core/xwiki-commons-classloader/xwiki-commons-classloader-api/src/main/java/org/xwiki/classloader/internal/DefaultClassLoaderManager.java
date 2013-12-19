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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.classloader.NamespaceURLClassLoader;
import org.xwiki.component.annotation.Component;

/**
 * Default implementation of {@link ClassLoaderManager}.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultClassLoaderManager implements ClassLoaderManager
{
    /**
     * The class loader corresponding to null namespace.
     */
    private NamespaceURLClassLoader rootClassLoader;

    /**
     * The classloaders stored by namespace.
     */
    private Map<String, NamespaceURLClassLoader> wikiClassLoaderMap =
        new ConcurrentHashMap<String, NamespaceURLClassLoader>();

    /**
     * Allow overriding the system classloader during tests.
     * 
     * @return a ClassLoader to be used as the system parent
     */
    protected ClassLoader getSystemClassLoader()
    {
        return getClass().getClassLoader();
    }

    @Override
    public NamespaceURLClassLoader getURLClassLoader(String namespace, boolean create)
    {
        if (this.rootClassLoader == null) {
            this.rootClassLoader = new NamespaceURLClassLoader(new URI[] {}, getSystemClassLoader(), null);
        }

        NamespaceURLClassLoader wikiClassLoader = this.rootClassLoader;

        if (namespace != null) {
            wikiClassLoader = this.wikiClassLoaderMap.get(namespace);

            if (wikiClassLoader == null) {
                if (create) {
                    wikiClassLoader = new NamespaceURLClassLoader(new URI[] {}, this.rootClassLoader, namespace);
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
            for (String namespace : wikiClassLoaderMap.keySet()) {
                dropURLClassLoader(namespace);
            }

            this.rootClassLoader = null;
        }
    }

    @Override
    public void dropURLClassLoader(String namespace)
    {
        if (this.rootClassLoader != null && namespace != null) {
            this.wikiClassLoaderMap.remove(namespace);
        }
    }
}
