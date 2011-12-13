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
    private ObservationManager observationManager;

    private Stack<ComponentEventEntry> events = new Stack<ComponentEventEntry>();

    private boolean shouldStack = true;

    @Override
    public void notifyComponentRegistered(ComponentDescriptor< ? > descriptor)
    {
        notifyComponentEvent(new ComponentDescriptorAddedEvent(descriptor.getRole(), descriptor.getRoleHint()),
            descriptor, null);
    }

    @Override
    public void notifyComponentRegistered(ComponentDescriptor< ? > descriptor, ComponentManager componentManager)
    {
        notifyComponentEvent(new ComponentDescriptorAddedEvent(descriptor.getRole(), descriptor.getRoleHint()),
            descriptor, componentManager);
    }

    @Override
    public void notifyComponentUnregistered(ComponentDescriptor< ? > descriptor)
    {
        notifyComponentEvent(new ComponentDescriptorRemovedEvent(descriptor.getRole(), descriptor.getRoleHint()),
            descriptor, null);
    }

    @Override
    public void notifyComponentUnregistered(ComponentDescriptor< ? > descriptor, ComponentManager componentManager)
    {
        notifyComponentEvent(new ComponentDescriptorRemovedEvent(descriptor.getRole(), descriptor.getRoleHint()),
            descriptor, componentManager);
    }

    public synchronized void flushEvents()
    {
        for (ComponentEventEntry entry : this.events) {
            sendEvent(entry.event, entry.descriptor, entry.componentManager);
        }
    }

    public void shouldStack(boolean shouldStack)
    {
        this.shouldStack = shouldStack;
    }

    public void setObservationManager(ObservationManager observationManager)
    {
        this.observationManager = observationManager;
    }

    private void notifyComponentEvent(Event event, ComponentDescriptor< ? > descriptor,
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

    private void sendEvent(Event event, ComponentDescriptor< ? > descriptor, ComponentManager componentManager)
    {
        if (this.observationManager != null) {
            this.observationManager.notify(event, componentManager, descriptor);
        }
    }

    static class ComponentEventEntry
    {
        public Event event;

        public ComponentDescriptor< ? > descriptor;

        public ComponentManager componentManager;

        public ComponentEventEntry(Event event, ComponentDescriptor< ? > descriptor, ComponentManager componentManager)
        {
            this.event = event;
            this.descriptor = descriptor;
            this.componentManager = componentManager;
        }
    }
}
