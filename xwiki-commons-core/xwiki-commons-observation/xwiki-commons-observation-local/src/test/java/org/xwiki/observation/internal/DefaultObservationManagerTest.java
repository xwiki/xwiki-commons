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

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ActionExecutionEvent;
import org.xwiki.observation.event.AllEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultObservationManager}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultObservationManagerTest
{
    @InjectMockComponents
    private DefaultObservationManager manager;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @Test
    void notifyWhenMatching()
    {
        EventListener listener = mock(EventListener.class);
        Event event = mock(Event.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(event));
        when(event.matches(event)).thenReturn(true);

        this.manager.addListener(listener);
        assertSame(listener, this.manager.getListener("mylistener"));
        this.manager.notify(event, "some source", "some data");
        verify(listener).onEvent(event, "some source", "some data");
    }

    @Test
    void removeListener()
    {
        EventListener listener = mock(EventListener.class);
        Event event = mock(Event.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(event));

        this.manager.addListener(listener);
        this.manager.removeListener("mylistener");
        this.manager.notify(event, null);
        verify(listener, never()).onEvent(any(Event.class), any(), any());
    }

    @Test
    void addEvent()
    {
        EventListener listener = mock(EventListener.class);
        Event initialEvent = mock(Event.class, "initial");
        Event afterEvent = mock(Event.class, "after");
        Event notifyEvent = mock(Event.class, "notify");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(initialEvent));

        // Since the observation returns the first matching event, return false from initialEvent so that
        // afterEvent is called.
        when(initialEvent.matches(notifyEvent)).thenReturn(false);
        when(afterEvent.matches(notifyEvent)).thenReturn(true);

        this.manager.addListener(listener);
        this.manager.addEvent("mylistener", afterEvent);
        this.manager.notify(notifyEvent, null);
        verify(listener).onEvent(notifyEvent, null, null);
    }

    @Test
    void addEventWithNoInitialEvent()
    {
        EventListener listener = mock(EventListener.class);
        Event afterEvent = mock(Event.class, "after");
        Event notifyEvent = mock(Event.class, "notify");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Collections.<Event>emptyList());

        when(afterEvent.matches(notifyEvent)).thenReturn(true);

        this.manager.addListener(listener);
        this.manager.addEvent("mylistener", afterEvent);
        this.manager.notify(notifyEvent, null);
        verify(listener).onEvent(notifyEvent, null, null);
    }

    @Test
    void removeEvent()
    {
        EventListener listener = mock(EventListener.class);
        Event initialEvent = mock(Event.class, "initial");
        Event afterEvent = mock(Event.class, "after");
        Event notifyEvent = mock(Event.class, "notify");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(initialEvent));

        // Since the observation returns the first matching event, return false from initialEvent so that
        // the second event can be called (if there's a second event - in our case it'll be removed but
        // we still want the test to fail if that doesn't work).
        when(initialEvent.matches(same(notifyEvent))).thenReturn(false);

        this.manager.addListener(listener);
        this.manager.addEvent("mylistener", afterEvent);
        this.manager.removeEvent("mylistener", afterEvent);
        this.manager.notify(notifyEvent, null);
        // Ensure that the afterEvent is never called since we're adding it and removing it
        verify(afterEvent, never()).matches(same(notifyEvent));
        verify(listener, never()).onEvent(afterEvent, null, null);
    }

    /**
     * Verify that we can register two listeners on the same event and they'll both receive the event.
     */
    @Test
    void registerSeveralListenersForSameEvent()
    {
        EventListener listener1 = mock(EventListener.class, "listener1");
        EventListener listener2 = mock(EventListener.class, "listener2");
        Event event = mock(Event.class, "event");
        Event notifyEvent = mock(Event.class, "notify");

        when(listener1.getName()).thenReturn("listener 1");
        when(listener2.getName()).thenReturn("listener 2");
        when(listener1.getEvents()).thenReturn(Arrays.asList(event));
        when(listener2.getEvents()).thenReturn(Arrays.asList(event));

        when(event.matches(notifyEvent)).thenReturn(true);

        this.manager.addListener(listener1);
        this.manager.addListener(listener2);
        this.manager.notify(notifyEvent, null);
        verify(listener1).onEvent(notifyEvent, null, null);
        verify(listener2).onEvent(notifyEvent, null, null);
    }

    /**
     * Verify that we can register a listener to receive any kind of event using AllEvent event type.
     */
    @Test
    void registerListenerForAllEvents()
    {
        EventListener listener = mock(EventListener.class);
        Event event = mock(Event.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(AllEvent.ALLEVENT));

        this.manager.addListener(listener);
        assertSame(listener, this.manager.getListener("mylistener"));
        this.manager.notify(event, "some source", "some data");
        verify(listener).onEvent(event, "some source", "some data");
    }

    /**
     * Verify that a warning is logged is we try to register a listener with the same name.
     */
    @Test
    void registerSameListenerSeveralTimes()
    {
        EventListener listener = mock(EventListener.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(AllEvent.ALLEVENT));
        // The check is performed here, we verify that a warning is correctly logged

        this.manager.addListener(listener);
        // Will raise log warning on the next line
        this.manager.addListener(listener);

        assertTrue(this.logCapture.getMessage(0).matches("The \\[.*\\] listener is overwriting a previously registered "
            + "listener \\[.*\\] since they both are registered under the same id \\[mylistener\\]\\. In the future "
            + "consider removing a Listener first if you really want to register it again\\."));

        // Verify that no log is logged if we remove the listener before re-registering it
        this.manager.removeListener("mylistener");
        // Next line will not log any warning
        this.manager.addListener(listener);
    }

    /**
     * Verify that we can register a listener with two events of the same type but with not matching the same thing and
     * it will receive both events.
     */
    @Test
    void registerListenerForTwoEventsOfSameType()
    {
        EventListener listener = mock(EventListener.class);
        Event eventMatcher1 = new ActionExecutionEvent("action1");
        Event eventMatcher2 = new ActionExecutionEvent("action2");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(eventMatcher1, eventMatcher2));

        this.manager.addListener(listener);
        this.manager.notify(eventMatcher1, "some source", "some data");
        this.manager.notify(eventMatcher2, "some source", "some data");
        verify(listener).onEvent(eventMatcher1, "some source", "some data");
        verify(listener).onEvent(eventMatcher2, "some source", "some data");
    }

    @Test
    void onComponentEventWhenRuntimeExceptionInListenerGetName(MockitoComponentManager componentManager)
        throws Exception
    {
        ComponentDescriptorAddedEvent cdae = new ComponentDescriptorAddedEvent(EventListener.class,
            "onComponentEventWhenRuntimeExceptionInListener");

        // Important: initialize listener cache first so that we can reach the call to onComponentEvent().
        this.manager.notify((Object) -> false, componentManager, null);

        EventListener eventListener = componentManager.registerMockComponent(EventListener.class,
            "onComponentEventWhenRuntimeExceptionInListener");
        when(eventListener.getName()).thenThrow(new RuntimeException("error"));
        DefaultComponentDescriptor<EventListener> cd = new DefaultComponentDescriptor<>();
        cd.setImplementation(EventListener.class);
        cd.setRoleType(EventListener.class);

        this.manager.notify(cdae, componentManager, cd);

        assertEquals("Failed to notify some event listeners about component ["
            + "role = [interface org.xwiki.observation.EventListener], "
            + "hint = [default], "
            + "implementation = [org.xwiki.observation.EventListener], "
            + "instantiation = [SINGLETON]"
            + "] being added or removed. Root cause: [RuntimeException: error]", logCapture.getMessage(0));
    }

    @Test
    void addListenerWhenRuntimeExceptionInListenerGetName(MockitoComponentManager componentManager)
        throws Exception
    {
        EventListener eventListener = componentManager.registerMockComponent(EventListener.class,
            "onComponentEventWhenRuntimeExceptionInListener");
        when(eventListener.getName()).thenThrow(new RuntimeException("error"));

        this.manager.notify((Object) -> false, componentManager, null);

        assertEquals("Failed to add listener. Root cause: [RuntimeException: error]", logCapture.getMessage(0));
    }
}
