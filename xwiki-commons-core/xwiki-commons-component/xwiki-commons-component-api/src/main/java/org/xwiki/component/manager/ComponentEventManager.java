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

import org.xwiki.component.descriptor.ComponentDescriptor;

/**
 * Manages Component Events (when a component instance is created for example). It's recommended that implementations
 * use the Observation module to send the events. We're introducing this level of indirection in order to be able to
 * perform some processing before the events are fired. For example one implementation may want to stack the events
 * before sending them.
 *
 * @version $Id$
 * @since 2.0M1
 */
public interface ComponentEventManager
{
    /**
     * Notify all listeners that a component with the passed descriptor has been registered.
     *
     * @param descriptor the descriptor for the instantiated component
     * @since 2.0M2
     * @deprecated since 3.3 use {@link #notifyComponentRegistered(ComponentDescriptor, ComponentManager)} instead
     */
    @Deprecated
    void notifyComponentRegistered(ComponentDescriptor<?> descriptor);

    /**
     * Notify all listeners that a component with the passed descriptor has been registered.
     *
     * @param descriptor the descriptor for the instantiated component
     * @param componentManager the ComponentManager where the component has been registered
     * @since 2.0M2
     */
    void notifyComponentRegistered(ComponentDescriptor<?> descriptor, ComponentManager componentManager);

    /**
     * Notify all listeners that a component with the passed descriptor has been unregistered.
     *
     * @param descriptor the descriptor for the instantiated component
     * @since 2.0M2
     * @deprecated since 3.3 use {@link #notifyComponentUnregistered(ComponentDescriptor, ComponentManager)} instead
     */
    @Deprecated
    void notifyComponentUnregistered(ComponentDescriptor<?> descriptor);

    /**
     * Notify all listeners that a component with the passed descriptor has been unregistered.
     *
     * @param descriptor the descriptor for the instantiated component
     * @param componentManager the ComponentManager from where the component has been unregistered
     * @since 3.3
     */
    void notifyComponentUnregistered(ComponentDescriptor<?> descriptor, ComponentManager componentManager);
}
