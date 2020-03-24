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

import java.util.Stack;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentEventManager;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

/**
 * Allow stacking component events and flush them whenever the user of this class wants to. This is used for example at
 * application initialization time when we don't want to send events before the Application Context has been initialized
 * since components subscribing to these events may want to use the Application Context.
 *
 * @version $Id$
 * @since 2.0M1
 */
public class StackingComponentEventManager implements ComponentEventManager
{
    /**
     * The wrapped observation manager.
     */
    private ObservationManager observationManager;

    /**
     * The event stacked before been given the order to send them.
     */
    private Stack<ComponentEventEntry> events = new Stack<ComponentEventEntry>();

    /**
     * Indicate if event should be retained to directly sent.
     */
    private boolean shouldStack = true;

    @Override
    public void notifyComponentRegistered(ComponentDescriptor<?> descriptor)
    {
        notifyComponentEvent(new ComponentDescriptorAddedEvent(descriptor.getRoleType(), descriptor.getRoleHint()),
            descriptor, null);
    }

    @Override
    public void notifyComponentRegistered(ComponentDescriptor<?> descriptor, ComponentManager componentManager)
    {
        notifyComponentEvent(new ComponentDescriptorAddedEvent(descriptor.getRoleType(), descriptor.getRoleHint()),
            descriptor, componentManager);
    }

    @Override
    public void notifyComponentUnregistered(ComponentDescriptor<?> descriptor)
    {
        notifyComponentEvent(new ComponentDescriptorRemovedEvent(descriptor.getRoleType(), descriptor.getRoleHint()),
            descriptor, null);
    }

    @Override
    public void notifyComponentUnregistered(ComponentDescriptor<?> descriptor, ComponentManager componentManager)
    {
        notifyComponentEvent(new ComponentDescriptorRemovedEvent(descriptor.getRoleType(), descriptor.getRoleHint()),
            descriptor, componentManager);
    }

    /**
     * Force to send all stored events.
     */
    public synchronized void flushEvents()
    {
        while (!this.events.isEmpty()) {
            ComponentEventEntry entry = this.events.pop();
            sendEvent(entry.event, entry.descriptor, entry.componentManager);
        }
    }

    /**
     * @param shouldStack indicate if the events received should be stacked
     */
    public void shouldStack(boolean shouldStack)
    {
        this.shouldStack = shouldStack;
    }

    /**
     * @return true if the events received should be stacked
     * @since 12.2RC1
     */
    public boolean isStacked()
    {
        return this.shouldStack;
    }

    /**
     * @param observationManager the wrapped observation manager
     */
    public void setObservationManager(ObservationManager observationManager)
    {
        this.observationManager = observationManager;
    }

    /**
     * Send or stack the provided event dependening on the configuration.
     *
     * @param event the event send by the component manager
     * @param descriptor the event related component descriptor.
     * @param componentManager the event related component manager instance.
     * @see #shouldStack(boolean)
     */
    private void notifyComponentEvent(Event event, ComponentDescriptor<?> descriptor,
        ComponentManager componentManager)
    {
        if (this.shouldStack) {
            synchronized (this) {
                this.events.push(new ComponentEventEntry(event, descriptor, componentManager));
            }
        } else {
            sendEvent(event, descriptor, componentManager);
        }
    }

    /**
     * Send the event.
     *
     * @param event the event to send
     * @param descriptor the event related component descriptor.
     * @param componentManager the event related component manager instance.
     */
    private void sendEvent(Event event, ComponentDescriptor<?> descriptor, ComponentManager componentManager)
    {
        if (this.observationManager != null) {
            this.observationManager.notify(event, componentManager, descriptor);
        }
    }

    /**
     * Contains a stacked event.
     *
     * @version $Id$
     */
    static class ComponentEventEntry
    {
        /**
         * The stacked event.
         */
        public Event event;

        /**
         * The event related component descriptor.
         */
        public ComponentDescriptor<?> descriptor;

        /**
         * The event related component manager instance.
         */
        public ComponentManager componentManager;

        /**
         * @param event the stacked event.
         * @param descriptor the event related component descriptor.
         * @param componentManager the event related component manager instance.
         */
        ComponentEventEntry(Event event, ComponentDescriptor<?> descriptor, ComponentManager componentManager)
        {
            this.event = event;
            this.descriptor = descriptor;
            this.componentManager = componentManager;
        }
    }
}
