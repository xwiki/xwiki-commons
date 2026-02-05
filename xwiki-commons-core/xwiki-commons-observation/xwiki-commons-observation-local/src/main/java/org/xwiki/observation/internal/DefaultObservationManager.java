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
package org.xwiki.observation.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.collection.internal.PriorityEntries;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.AllEvent;
import org.xwiki.observation.event.Event;

/**
 * Default implementation of the {@link ObservationManager}.
 * <p>
 * This component use synchronized for concurrent protection instead of having
 * {@link java.util.concurrent.ConcurrentHashMap} everywhere because it's more efficient since most of methods access to
 * several maps and generally do enumerations.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultObservationManager implements ObservationManager
{
    /**
     * @see #getListenersByEvent()
     */
    private volatile Map<Class<? extends Event>, PriorityEntries<RegisteredListener>> listenersByEvent;

    /**
     * @see #getListenersByName()
     */
    private volatile Map<String, EventListener> listenersByName;

    /**
     * Used to find all components implementing {@link EventListener} to register them automatically.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Helper class to store the list of events of a given type associated with a given listener. We need this for
     * performance reasons and also in order to be able to add events after a listener has been registered.
     */
    private static class RegisteredListener implements Comparable<RegisteredListener>
    {
        /**
         * Events of a given type associated with a given listener.
         */
        private List<Event> events = new ArrayList<>();

        /**
         * Listener associated with the events.
         */
        private EventListener listener;

        private int priority;

        /**
         * @param listener the listener associated with the events.
         * @param event the first event to associate with the passed listener. More events are added by calling
         *            {@link #addEvent(Event)}
         * @param priority the priority of the listener
         */
        RegisteredListener(EventListener listener, Event event, int priority)
        {
            addEvent(event);

            this.listener = listener;
            this.priority = priority;
        }

        /**
         * @param event the event to add
         */
        void addEvent(Event event)
        {
            this.events.add(event);
        }

        /**
         * @param event the event to remove
         */
        void removeEvent(Event event)
        {
            this.events.remove(event);
        }

        @Override
        public int compareTo(RegisteredListener other)
        {
            return this.priority - other.priority;
        }
    }

    /**
     * @return the registered listeners indexed on Event classes so that it's fast to find all the listeners registered
     *         for a given event, so that {@link #notify} calls execute fast and in a fixed amount a time.
     */
    private Map<Class<? extends Event>, PriorityEntries<RegisteredListener>> getListenersByEvent()
    {
        if (this.listenersByEvent == null) {
            initializeListeners();
        }

        return this.listenersByEvent;
    }

    /**
     * @return the registered listeners index by listener name. It makes it fast to perform operations on already
     *         registered listeners.
     */
    private Map<String, EventListener> getListenersByName()
    {
        if (this.listenersByName == null) {
            initializeListeners();
        }

        return this.listenersByName;
    }

    /**
     * Lazily initialized to allow @Inject {@link ObservationManager} in a listener.
     *
     * @todo Should we allow event inheritance ?
     */
    private synchronized void initializeListeners()
    {
        if (this.listenersByName == null) {
            this.listenersByEvent = new ConcurrentHashMap<>();
            this.listenersByName = new ConcurrentHashMap<>();

            // Can be null in unit tests
            if (this.componentManager != null) {
                for (ComponentDescriptor<EventListener> descriptor : this.componentManager
                    .<EventListener>getComponentDescriptorList(EventListener.class)) {
                    try {
                        addListener(this.componentManager.getInstance(EventListener.class, descriptor.getRoleHint()),
                            descriptor.getRoleTypePriority());
                    } catch (ComponentLookupException e) {
                        this.logger.error("Failed to lookup listener with role hint [{}]", descriptor.getRoleHint(), e);
                    }
                }
            }
        }
    }

    @Override
    public void addListener(EventListener eventListener)
    {
        addListener(eventListener, ComponentDescriptor.DEFAULT_PRIORITY);
    }

    @Override
    public void addListener(EventListener eventListener, int priority)
    {
        try {
            addListenerInternal(eventListener, priority);
        } catch (Exception e) {
            // Protect against bad listeners which have their getName() methods throw some runtime exception.
            this.logger.warn("Failed to add listener. Root cause: [{}]", ExceptionUtils.getRootCauseMessage(e));
        }
    }

    private void addListenerInternal(EventListener eventListener, int priority)
    {
        this.logger.debug("Adding listener [{}] with name [{}] and priority [{}]", eventListener.getClass().getName(),
            eventListener.getName(), priority);

        Map<String, EventListener> listeners = getListenersByName();

        // Remove previous listener if any
        EventListener previousListener = listeners.get(eventListener.getName());
        if (previousListener != null) {
            removeListener(eventListener.getName());

            this.logger.warn(
                "The [{}] listener is overwriting a previously "
                    + "registered listener [{}] since they both are registered under the same id [{}]. "
                    + "In the future consider removing a Listener first if you really want to register it again.",
                eventListener.getClass().getName(), previousListener.getClass().getName(), eventListener.getName());
        }

        // Register the listener by name. If already registered, override it.
        listeners.put(eventListener.getName(), eventListener);

        // when lot of threads are involved there might be a concurrent access when inserting a new listener
        // this needs to be managed with a lock to avoid an event to be "lost", e.g. not consumed by the appropriate
        // listener
        synchronized (this.listenersByEvent) {
            // For each event defined for this listener, add it to the Event Map.
            for (Event event : eventListener.getEvents()) {
                // Check if this is a new Event type not already registered
                PriorityEntries<RegisteredListener> eventListeners = this.listenersByEvent.get(event.getClass());
                if (eventListeners == null) {
                    // No listener registered for this event yet. Create a map to store listeners for this event.
                    eventListeners = new PriorityEntries<RegisteredListener>();
                    this.listenersByEvent.put(event.getClass(), eventListeners);
                    // There is no RegisteredListener yet, create one
                    eventListeners.put(eventListener.getName(), new RegisteredListener(eventListener, event, priority));
                } else {
                    // Add an event to existing RegisteredListener object
                    RegisteredListener registeredListener = eventListeners.get(eventListener.getName());
                    if (registeredListener == null) {
                        eventListeners.put(eventListener.getName(),
                            new RegisteredListener(eventListener, event, priority));
                    } else {
                        registeredListener.addEvent(event);
                    }
                }
            }
        }
    }

    @Override
    public void removeListener(String listenerName)
    {
        this.logger.debug("Removing listener with name [{}]", listenerName);

        getListenersByName().remove(listenerName);
        for (Map.Entry<Class<? extends Event>, PriorityEntries<RegisteredListener>> entry : this.listenersByEvent
            .entrySet()) {
            entry.getValue().remove(listenerName);
            if (entry.getValue().isEmpty()) {
                this.listenersByEvent.remove(entry.getKey());
            }
        }
    }

    @Override
    public void addEvent(String listenerName, Event event)
    {
        PriorityEntries<RegisteredListener> listeners =
            getListenersByEvent().computeIfAbsent(event.getClass(), k -> new PriorityEntries<RegisteredListener>());
        RegisteredListener listener = listeners.get(listenerName);
        if (listener != null) {
            listener.addEvent(event);
        } else {
            listeners.put(listenerName,
                new RegisteredListener(this.getListener(listenerName), event, ComponentDescriptor.DEFAULT_PRIORITY));
        }
    }

    @Override
    public void removeEvent(String listenerName, Event event)
    {
        PriorityEntries<RegisteredListener> listeners = getListenersByEvent().get(event.getClass());
        if (listeners != null) {
            RegisteredListener listener = listeners.get(listenerName);
            if (listener != null) {
                listener.removeEvent(event);
            }
        }
    }

    @Override
    public EventListener getListener(String listenerName)
    {
        return getListenersByName().get(listenerName);
    }

    @Override
    public void notify(Event event, Object source, Object data)
    {
        // Find all listeners for this event
        PriorityEntries<RegisteredListener> regListeners = getListenersByEvent().get(event.getClass());
        if (regListeners != null) {
            notify(regListeners.getSorted(), event, source, data);
        }

        // Find listener listening all events
        PriorityEntries<RegisteredListener> allEventRegListeners = this.listenersByEvent.get(AllEvent.class);
        if (allEventRegListeners != null) {
            notify(allEventRegListeners.getSorted(), event, source, data);
        }

        // We want this Observation Manager to be able to handle new Event Listener components being added or removed
        // at runtime. Thus ideally we should make this Manager an Event Listener itself. However in order to avoid
        // circular dependencies issues and in order to be more performant we simply handle ComponentDescriptorEvents
        // here to add/remove Event Listeners.
        if (event instanceof ComponentDescriptorEvent) {
            onComponentEvent((ComponentDescriptorEvent) event, (ComponentManager) source,
                (ComponentDescriptor<EventListener>) data);
        }
    }

    /**
     * Call the provided listeners matching the passed Event. The definition of <em>source</em> and <em>data</em> is
     * purely up to the communicating classes.
     *
     * @param listeners the listeners to notify
     * @param event the event to pass to the registered listeners
     * @param source the source of the event (or <code>null</code>)
     * @param data the additional data related to the event (or <code>null</code>)
     */
    private void notify(Collection<RegisteredListener> listeners, Event event, Object source, Object data)
    {
        for (RegisteredListener listener : listeners) {
            // Verify that one of the events matches and send the first matching event
            for (Event listenerEvent : listener.events) {
                if (listenerEvent.matches(event)) {
                    try {
                        listener.listener.onEvent(event, source, data);
                    } catch (LinkageError | Exception e) {
                        // protect from bad listeners
                        this.logger.error("Failed to send event [{}] to listener [{}]", event, listener.listener, e);
                    }

                    // Only send the first matching event since the listener should only be called once per event.
                    break;
                }
            }
        }
    }

    @Override
    public void notify(Event event, Object source)
    {
        notify(event, source, null);
    }

    /**
     * A Component has been modified (added or removed) and we update our cache of Event Listeners if that Component is
     * an Event Listener.
     *
     * @param componentEvent the event about the Component being added or removed
     * @param componentManager the {@link ComponentManager} where the descriptor is registered
     * @param descriptor the descriptor of the modified component
     */
    private void onComponentEvent(ComponentDescriptorEvent componentEvent, ComponentManager componentManager,
        ComponentDescriptor<EventListener> descriptor)
    {
        try {
            if (componentEvent.getRoleType() == EventListener.class) {
                if (componentEvent instanceof ComponentDescriptorAddedEvent) {
                    onEventListenerComponentAdded((ComponentDescriptorAddedEvent) componentEvent, componentManager,
                        descriptor);
                } else if (componentEvent instanceof ComponentDescriptorRemovedEvent) {
                    onEventListenerComponentRemoved((ComponentDescriptorRemovedEvent) componentEvent, componentManager,
                        descriptor);
                } else {
                    this.logger.warn("Ignoring unknown Component event [{}]", componentEvent.getClass().getName());
                }
            }
        } catch (Exception e) {
            // Protect against bad listeners which have their getName() or getEvents() methods throw some runtime
            // exception. For example we don't want to fail so that Component registration or unregistration won't fail
            // in this case.
            this.logger.warn("Failed to notify some event listeners about component [{}] being added or removed. "
                + "Root cause: [{}]", descriptor, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * An Event Listener Component has been dynamically registered in the system, add it to our cache.
     *
     * @param event event object containing the new component descriptor
     * @param componentManager the {@link ComponentManager} where the descriptor is registered
     * @param descriptor the component descriptor removed from component manager
     */
    private void onEventListenerComponentAdded(ComponentDescriptorAddedEvent event, ComponentManager componentManager,
        ComponentDescriptor<EventListener> descriptor)
    {
        try {
            EventListener eventListener = componentManager.getInstance(EventListener.class, event.getRoleHint());

            EventListener existingListener = getListener(eventListener.getName());

            // No reason to re-add the exact same listener.
            // No reason to complain about this (for example root installed extensions are both available when
            // initializing DefaultObservationManager to not miss anything and produce ComponentDescriptorAddedEvent
            // events which can be used for other needs).
            if (existingListener != eventListener) {
                addListener(eventListener, descriptor.getRoleTypePriority());
            }
        } catch (ComponentLookupException e) {
            this.logger.error(
                "Failed to lookup the Event Listener [{}] corresponding to the Component registration "
                    + "event for [{}]. Ignoring the event",
                event.getRoleHint(), descriptor.getImplementation().getName(), e);
        }
    }

    /**
     * An Event Listener Component has been dynamically unregistered in the system, remove it from our cache.
     *
     * @param event the event object containing the removed component descriptor
     * @param componentManager the {@link ComponentManager} where the descriptor is registered
     * @param descriptor the component descriptor removed from the component manager
     */
    private void onEventListenerComponentRemoved(ComponentDescriptorRemovedEvent event,
        ComponentManager componentManager, ComponentDescriptor<?> descriptor)
    {
        EventListener removedEventListener = null;
        for (EventListener eventListener : getListenersByName().values()) {
            if (eventListener.getClass() == descriptor.getImplementation()) {
                removedEventListener = eventListener;
            }
        }

        if (removedEventListener != null) {
            removeListener(removedEventListener.getName());
        }
    }
}
