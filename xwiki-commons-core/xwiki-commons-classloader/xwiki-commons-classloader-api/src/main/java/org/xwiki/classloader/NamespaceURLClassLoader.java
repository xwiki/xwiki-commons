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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.xwiki.stability.Unstable;

/**
 * An {@link ExtendedURLClassLoader} associated with a namespace. The namespace can be anything.
 * For example it's used by the Extension Manager to have one classloader per wiki using a
 * namespace of the type {@code wiki:wikiname}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class NamespaceURLClassLoader extends ExtendedURLClassLoader
{
    /**
     * @see #getNamespace()
     */
    private String namespace;

    /**
     * @param uris the search path
     * @param parent the parent class loader
     * @param namespace see {@link #getNamespace()}
     * @deprecated since 12.5RC1 prefer using
     *      {@link NamespaceURLClassLoader#NamespaceURLClassLoader(URL[], ClassLoader, String)}.
     */
    @Deprecated
    public NamespaceURLClassLoader(URI[] uris, ClassLoader parent, String namespace)
    {
        this(Arrays.stream(uris).map(uri -> {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()).toArray(new URL[0]), parent, namespace);
    }

    /**
     * @param urls the search path.
     * @param parent the parent class loader
     * @param namespace see {@link #getNamespace()}
     * @since 12.5RC1
     */
    @Unstable
    public NamespaceURLClassLoader(URL[] urls, ClassLoader parent, String namespace)
    {
        super(urls, parent);
        this.namespace = namespace;
    }

    /**
     * @param parent the parent class loader
     * @param namespace see {@link #getNamespace()}
     * @since 12.5RC1
     */
    @Unstable
    public NamespaceURLClassLoader(ClassLoader parent, String namespace)
    {
        this(new URL[]{}, parent, namespace);
    }

    /**
     * @return the namespace associated with the classloader
     */
    public String getNamespace()
    {
        return this.namespace;
    }
}
