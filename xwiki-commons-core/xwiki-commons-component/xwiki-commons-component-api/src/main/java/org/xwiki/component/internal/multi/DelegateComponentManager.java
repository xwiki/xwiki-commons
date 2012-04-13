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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

/**
 * Delegate all calls to a defined Component Manager, acting as a Proxy for it.
 * 
 * @version $Id$
 * @since 3.3M2
 */
public class DelegateComponentManager implements ComponentManager
{
    /**
     * @see #getComponentManager()
     */
    private ComponentManager componentManager;

    /**
     * @return the Component Manager to delegate to
     */
    public ComponentManager getComponentManager()
    {
        return this.componentManager;
    }

    /**
     * @param componentManager see {@link #getComponentManager()}
     */
    public void setComponentManager(ComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    @Override
    public boolean hasComponent(Type role)
    {
        return getComponentManager().hasComponent(role);
    }

    @Override
    public boolean hasComponent(Type role, String hint)
    {
        return getComponentManager().hasComponent(role, hint);
    }

    @Override
    public <T> T getInstance(Type roleType) throws ComponentLookupException
    {
        return getComponentManager().getInstance(roleType);
    }

    @Override
    public <T> T getInstance(Type roleType, String roleHint) throws ComponentLookupException
    {
        return getComponentManager().getInstance(roleType, roleHint);
    }

    @Override
    public <T> List<T> getInstanceList(Type role) throws ComponentLookupException
    {
        return getComponentManager().getInstanceList(role);
    }

    @Override
    public <T> Map<String, T> getInstanceMap(Type role) throws ComponentLookupException
    {
        return getComponentManager().getInstanceMap(role);
    }

    @Override
    public <T> ComponentDescriptor<T> getComponentDescriptor(Type role, String hint)
    {
        return getComponentManager().getComponentDescriptor(role, hint);
    }

    @Override
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Type role)
    {
        return getComponentManager().getComponentDescriptorList(role);
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
        throws ComponentRepositoryException
    {
        getComponentManager().registerComponent(componentDescriptor, componentInstance);
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        getComponentManager().registerComponent(componentDescriptor);
    }

    @Override
    public void unregisterComponent(Type role, String hint)
    {
        getComponentManager().unregisterComponent(role, hint);
    }

    @Override
    public void unregisterComponent(ComponentDescriptor< ? > classComponentDescriptor)
    {
        getComponentManager().unregisterComponent(classComponentDescriptor);
    }

    @Override
    public void release(Object component) throws ComponentLifecycleException
    {
        getComponentManager().release(component);
    }

    @Override
    public ComponentEventManager getComponentEventManager()
    {
        return getComponentManager().getComponentEventManager();
    }

    @Override
    public void setComponentEventManager(ComponentEventManager eventManager)
    {
        getComponentManager().setComponentEventManager(eventManager);
    }

    @Override
    public ComponentManager getParent()
    {
        return getComponentManager().getParent();
    }

    @Override
    public void setParent(ComponentManager parentComponentManager)
    {
        getComponentManager().setParent(parentComponentManager);
    }

    // Deprecated

    @Override
    @Deprecated
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role)
    {
        return getComponentManager().getComponentDescriptorList(role);
    }
}
