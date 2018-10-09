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
package org.xwiki.component.embed;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentLookupException;

/**
 * Add a backward compatibility layer to the {@link EmbeddableComponentManager} class.
 *
 * @version $Id$
 * @since 4.1M1
 */
public privileged aspect EmbeddableComponentManagerCompatibilityAspect
{
    @Deprecated
    public <T> T EmbeddableComponentManager.lookup(Class<T> role) throws ComponentLookupException
    {
        return getInstance(role);
    }

    @Deprecated
    public <T> T EmbeddableComponentManager.lookup(Class<T> role, String hint) throws ComponentLookupException
    {
        return getInstance(role, hint);
    }

    @Deprecated
    public <T> List<T> EmbeddableComponentManager.lookupList(Class<T> role) throws ComponentLookupException
    {
        return getInstanceList((Type) role);
    }

    @Deprecated
    public <T> Map<String, T> EmbeddableComponentManager.lookupMap(Class<T> role) throws ComponentLookupException
    {
        return getInstanceMap((Type) role);
    }

    @Deprecated
    public <T> boolean EmbeddableComponentManager.hasComponent(Class<T> role, String hint)
    {
        return hasComponent((Type) role, hint);
    }

    @Deprecated
    public <T> boolean EmbeddableComponentManager.hasComponent(Class<T> role)
    {
        return hasComponent((Type) role);
    }

    @Deprecated
    public <T> ComponentDescriptor<T> EmbeddableComponentManager.getComponentDescriptor(Class<T> role, String hint)
    {
        return getComponentDescriptor((Type) role, hint);
    }

    @Deprecated
    public <T> void EmbeddableComponentManager.unregisterComponent(Class<T> role, String hint)
    {
        unregisterComponent((Type) role, hint);
    }
}
