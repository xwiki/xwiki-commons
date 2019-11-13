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
package org.xwiki.extension.internal.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.component.namespace.Namespace;
import org.xwiki.extension.Extension;
import org.xwiki.extension.tree.ExtensionNode;

/**
 * Default implementation of {@link ExtensionNode}.
 * 
 * @param <E> the type of extension (installed, core, etc.) the node contains
 * @version $Id$
 * @since 11.10RC1
 */
public class DefaultExtensionNode<E extends Extension> implements ExtensionNode<E>
{
    private final Namespace namespace;

    private final E extension;

    private final List<ExtensionNode<E>> children;

    /**
     * @param namespace the namespace associated with node
     * @param extension the extension associated with node
     */
    public DefaultExtensionNode(Namespace namespace, E extension)
    {
        this.namespace = namespace;
        this.extension = extension;
        this.children = Collections.emptyList();
    }

    /**
     * @param namespace the namespace associated with node
     * @param extension the extension associated with node
     * @param children the children of the node
     */
    public DefaultExtensionNode(Namespace namespace, E extension, List<ExtensionNode<E>> children)
    {
        this.namespace = namespace;
        this.extension = extension;
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
    }

    @Override
    public Namespace getNamespace()
    {
        return this.namespace;
    }

    @Override
    public E getExtension()
    {
        return this.extension;
    }

    @Override
    public List<ExtensionNode<E>> getChildren()
    {
        return this.children;
    }
}
