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
package org.xwiki.component.internal;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

/**
 * Wraps the Component Manager in a component so that components requiring the component Manager can have it injected
 * automatically.
 * 
 * @version $Id$
 * @since 2.0M1
 */
@Component
@Singleton
public class DefaultComponentManager implements ComponentManager, Composable
{
    /**
     * The Component Manager instance we're wrapping.
     */
    private ComponentManager componentManager;

    @Override
    public void compose(ComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    @Override
    public <T> ComponentDescriptor<T> getComponentDescriptor(Class<T> role, String hint)
    {
        return this.componentManager.getComponentDescriptor(role, hint);
    }

    @Override
    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role)
    {
        return this.componentManager.getComponentDescriptorList(role);
    }

    @Override
    public <T> T lookup(Class<T> role, String hint) throws ComponentLookupException
    {
        return this.componentManager.lookup(role, hint);
    }

    @Override
    public <T> T lookup(Class<T> role) throws ComponentLookupException
    {
        return this.componentManager.lookup(role);
    }

    @Override
    public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException
    {
        return this.componentManager.lookupList(role);
    }

    @Override
    public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException
    {
        return this.componentManager.lookupMap(role);
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException
    {
        this.componentManager.registerComponent(componentDescriptor);
    }

    @Override
    public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
        throws ComponentRepositoryException
    {
        this.componentManager.registerComponent(componentDescriptor, componentInstance);
    }

    @Override
    public <T> void unregisterComponent(Class<T> role, String hint)
    {
        this.componentManager.unregisterComponent(role, hint);
    }

    @Override
    public <T> void release(T component) throws ComponentLifecycleException
    {
        this.componentManager.release(component);
    }

    @Override
    public <T> boolean hasComponent(Class<T> role, String hint)
    {
        return this.componentManager.hasComponent(role, hint);
    }

    @Override
    public <T> boolean hasComponent(Class<T> role)
    {
        return this.componentManager.hasComponent(role);
    }

    @Override
    public ComponentEventManager getComponentEventManager()
    {
        return this.componentManager.getComponentEventManager();
    }

    @Override
    public void setComponentEventManager(ComponentEventManager eventManager)
    {
        this.componentManager.setComponentEventManager(eventManager);
    }

    @Override
    public ComponentManager getParent()
    {
        return this.componentManager.getParent();
    }

    @Override
    public void setParent(ComponentManager parentComponentManager)
    {
        this.componentManager.setParent(parentComponentManager);
    }
}
