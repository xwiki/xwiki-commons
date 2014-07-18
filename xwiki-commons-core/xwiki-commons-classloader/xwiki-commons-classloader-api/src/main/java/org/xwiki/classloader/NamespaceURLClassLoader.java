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

import java.net.URI;

/**
 * A {@link URIClassLoader} associated with a namespace. The namespace can be anything. For example it's used by the
 * Extension Manager to have one classloader per wiki using a namrspace of the type {@code wiki:wikiname}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class NamespaceURLClassLoader extends URIClassLoader
{
    /**
     * @see #getNamespace()
     */
    private String namespace;

    /**
     * @param uris the search path
     * @param parent the parent class loader
     * @param namespace see {@link #getNamespace()}
     */
    public NamespaceURLClassLoader(URI[] uris, ClassLoader parent, String namespace)
    {
        super(uris, parent);

        this.namespace = namespace;
    }

    /**
     * @return the namespace associated with the classloader
     */
    public String getNamespace()
    {
        return this.namespace;
    }
}
