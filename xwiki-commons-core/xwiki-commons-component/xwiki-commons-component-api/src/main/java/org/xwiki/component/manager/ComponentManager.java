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
package org.xwiki.component.manager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.component.descriptor.ComponentDescriptor;

/**
 * Provide way to access and modify components repository.
 *
 * @version $Id$
 */
@Role
public interface ComponentManager
{
    /**
     * @param role the class (aka role) that the component implements
     * @return true if the component is registered or false otherwise
     * @since 4.0M1
     */
    boolean hasComponent(Type role);

    /**
     * @param role the class (aka role) that the component implements
     * @param hint the hint that differentiates a component implementation from another one (each component is
     *            registered with a hint; the "default" hint being the default)
     * @return true if the component is registered for the passed hint or false otherwise
     * @since 4.0M1
     */
    boolean hasComponent(Type role, String hint);

    /**
     * Find a component instance that implements that passed type. If the component has a singleton lifecycle then this
     * method always return the same instance.
     *
     * @param <T> the component role type
     * @param roleType the class (aka role) that the component implements
     * @return the component instance
     * @throws ComponentLookupException if any error happen during component search (the component does not exist, the
     *             instance fail to initialize)
     * @since 4.0RC1
     */
    <T> T getInstance(Type roleType) throws ComponentLookupException;

    /**
     * Find a component instance that implements that passed interface class. If the component has a singleton lifecycle
     * then this method always return the same instance.
     *
     * @param <T> the component role type
     * @param roleType the class (aka role) that the component implements
     * @param roleHint the hint that differentiates a component implementation from another one (each component is
     *            registered with a hint; the "default" hint being the default)
     * @return the component instance
     * @throws ComponentLookupException if any error happen during component search (the component does not exist, the
     *             instance fail to initialize)
     * @since 4.0RC1
     */
    <T> T getInstance(Type roleType, String roleHint) throws ComponentLookupException;

    /**
     * Release the provided singleton instance but don't unregister the component descriptor. This means that next time
     * the component is looked up a new instance will be created.
     *
     * @param componentInstance the component to release passed as a component instance.
     * @throws ComponentLifecycleException if the component's ending lifecycle raises an error
     * @since 4.0M1
     */
    void release(Object componentInstance) throws ComponentLifecycleException;

    /**
     * Find all the components implementing the provided role.
     *
     * @param role the type of the components role
     * @return the components
     * @param <T> the type of the components role
     * @throws ComponentLookupException if any error happen during component search
     * @since 4.0RC1
     */
    <T> List<T> getInstanceList(Type role) throws ComponentLookupException;

    /**
     * Find all the components implementing the provided role and organize then in a {@link Map} with role hint as key.
     *
     * @param role the type of the components role
     * @return the components
     * @param <T> the type of the components role
     * @throws ComponentLookupException if any error happen during component search
     * @since 4.0M1
     */
    <T> Map<String, T> getInstanceMap(Type role) throws ComponentLookupException;

    /**
     * Add a component in the component repository dynamically.
     * <p>
     * If a component with the same role and role hint already exists it will be replaced by this provided one when
     * lookup.
     *
     * @param <T> the component role type
     * @param componentDescriptor the descriptor of the component to register.
     * @throws ComponentRepositoryException error when registering component descriptor.
     * @since 1.7M1
     */
    <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException;

    /**
     * Add a component in the component repository dynamically. This method also makes possible to set the instance
     * returned by the {@link ComponentManager} instead of letting it created it from descriptor.
     * <p>
     * If a component with the same role and role hint already exists it will be replaced by this provided one when
     * lookup.
     *
     * @param <T> the component role type
     * @param componentDescriptor the descriptor of the component to register.
     * @param componentInstance the initial component instance
     * @throws ComponentRepositoryException error when registering component descriptor.
     * @since 2.0M2
     */
    <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
        throws ComponentRepositoryException;

    /**
     * Remove a component from the component repository dynamically.
     *
     * @param role the role identifying the component
     * @param hint the hint identifying the component
     * @since 4.0M1
     */
    void unregisterComponent(Type role, String hint);

    /**
     * Remove a component from the component repository dynamically.
     *
     * @param componentDescriptor the component descriptor
     * @since 4.0M1
     */
    void unregisterComponent(ComponentDescriptor<?> componentDescriptor);

    /**
     * @param <T> the component role type
     * @param role the role identifying the component
     * @param hint the hint identifying the component
     * @return the descriptor for the component matching the passed parameter or null if this component doesn't exist
     * @since 4.0M1
     */
    <T> ComponentDescriptor<T> getComponentDescriptor(Type role, String hint);

    /**
     * @param <T> the role class for which to return all component implementations
     * @param role the role class for which to return all component implementations
     * @return all component implementations for the passed role
     * @since 4.0M1
     */
    <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Type role);

    /**
     * @return the manager to use to send events when a component descriptor is registered
     * @since 2.1RC1
     */
    ComponentEventManager getComponentEventManager();

    /**
     * @param eventManager the manager to use to send events when a component descriptor is registered
     */
    void setComponentEventManager(ComponentEventManager eventManager);

    /**
     * @return the parent Component Manager of this Component Manager. When doing lookups if the component cannot be
     *         found in the current Component Manager it'll also be looked for in the parent Component Manager
     */
    ComponentManager getParent();

    /**
     * @param parentComponentManager see {@link #getParent()}
     */
    void setParent(ComponentManager parentComponentManager);

    // Deprecated

    /**
     * @param <T> the role class for which to return all component implementations
     * @param role the role class for which to return all component implementations
     * @return all component implementations for the passed role
     * @deprecated since 4.0M1 use {@link #getComponentDescriptorList(Type)} instead
     */
    @Deprecated
    <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role);
}
