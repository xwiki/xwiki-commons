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
package org.xwiki.component.internal.multi;

import java.util.List;
import java.util.Map;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentLookupException;

/**
 * Add a backward compatibility layer to the {@link DelegateComponentManager} class.
 *
 * @version $Id$
 * @since 4.1M1
 */
public privileged aspect DelegateComponentManagerCompatibilityAspect
{
    @Deprecated
    public <T> T DelegateComponentManager.lookup(Class<T> role, String roleHint) throws ComponentLookupException
    {
        return getComponentManager().lookup(role, roleHint);
    }

    @Deprecated
    public <T> T DelegateComponentManager.lookup(Class<T> role) throws ComponentLookupException
    {
        return getComponentManager().lookup(role);
    }

    @Deprecated
    public <T> List<T> DelegateComponentManager.lookupList(Class<T> role) throws ComponentLookupException
    {
        return getComponentManager().lookupList(role);
    }

    @Deprecated
    public <T> Map<String, T> DelegateComponentManager.lookupMap(Class<T> role) throws ComponentLookupException
    {
        return getComponentManager().lookupMap(role);
    }

    @Deprecated
    public <T> ComponentDescriptor<T> DelegateComponentManager.getComponentDescriptor(Class<T> role, String roleHint)
    {
        return getComponentManager().getComponentDescriptor(role, roleHint);
    }

    @Deprecated
    public <T> boolean DelegateComponentManager.hasComponent(Class<T> role, String roleHint)
    {
        return getComponentManager().hasComponent(role, roleHint);
    }

    @Deprecated
    public <T> boolean DelegateComponentManager.hasComponent(Class<T> role)
    {
        return getComponentManager().hasComponent(role);
    }

    @Deprecated
    public <T> void DelegateComponentManager.unregisterComponent(Class<T> role, String roleHint)
    {
        getComponentManager().unregisterComponent(role, roleHint);
    }
}
