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
package org.xwiki.observation;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.observation.event.ActionExecutionEvent;
import org.xwiki.observation.event.AllEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ObservationManager}.
 *
 * @version $Id$
 */
public class ObservationManagerTest
{
    @Rule
    public final MockitoComponentMockingRule<ObservationManager> mocker =
        new MockitoComponentMockingRule<ObservationManager>(DefaultObservationManager.class);

    @Test
    public void testNotifyWhenMatching() throws ComponentLookupException
    {
        final EventListener listener = mock(EventListener.class);
        final Event event = mock(Event.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(event));
        when(event.matches(event)).thenReturn(true);

        this.mocker.getComponentUnderTest().addListener(listener);
        Assert.assertSame(listener, this.mocker.getComponentUnderTest().getListener("mylistener"));
        this.mocker.getComponentUnderTest().notify(event, "some source", "some data");
        verify(listener).onEvent(event, "some source", "some data");
    }

    @Test
    public void testRemoveListener() throws ComponentLookupException
    {
        final EventListener listener = mock(EventListener.class);
        final Event event = mock(Event.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(event));

        this.mocker.getComponentUnderTest().addListener(listener);
        this.mocker.getComponentUnderTest().removeListener("mylistener");
        this.mocker.getComponentUnderTest().notify(event, null);
        verify(listener, never()).onEvent(any(Event.class), any(), any());
    }

    @Test
    public void testAddEvent() throws Exception
    {
        final EventListener listener = mock(EventListener.class);
        final Event initialEvent = mock(Event.class, "initial");
        final Event afterEvent = mock(Event.class, "after");
        final Event notifyEvent = mock(Event.class, "notify");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(initialEvent));

        // Since the observation returns the first matching event, return false from initialEvent so that
        // afterEvent is called.
        when(initialEvent.matches(notifyEvent)).thenReturn(false);
        when(afterEvent.matches(notifyEvent)).thenReturn(true);

        this.mocker.getComponentUnderTest().addListener(listener);
        this.mocker.getComponentUnderTest().addEvent("mylistener", afterEvent);
        this.mocker.getComponentUnderTest().notify(notifyEvent, null);
        verify(listener).onEvent(notifyEvent, null, null);
    }

    @Test
    public void testAddEventWithNoInitialEvent() throws Exception
    {
        final EventListener listener = mock(EventListener.class);
        final Event afterEvent = mock(Event.class, "after");
        final Event notifyEvent = mock(Event.class, "notify");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Collections.<Event>emptyList());

        when(afterEvent.matches(notifyEvent)).thenReturn(true);

        this.mocker.getComponentUnderTest().addListener(listener);
        this.mocker.getComponentUnderTest().addEvent("mylistener", afterEvent);
        this.mocker.getComponentUnderTest().notify(notifyEvent, null);
        verify(listener).onEvent(notifyEvent, null, null);
    }

    @Test
    public void testRemoveEvent() throws ComponentLookupException
    {
        final EventListener listener = mock(EventListener.class);
        final Event initialEvent = mock(Event.class, "initial");
        final Event afterEvent = mock(Event.class, "after");
        final Event notifyEvent = mock(Event.class, "notify");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(initialEvent));

        // Since the observation returns the first matching event, return false from initialEvent so that
        // the second event can be called (if there's a second event - in our case it'll be removed but
        // we still want the test to fail if that doesn't work).
        when(initialEvent.matches(same(notifyEvent))).thenReturn(false);

        this.mocker.getComponentUnderTest().addListener(listener);
        this.mocker.getComponentUnderTest().addEvent("mylistener", afterEvent);
        this.mocker.getComponentUnderTest().removeEvent("mylistener", afterEvent);
        this.mocker.getComponentUnderTest().notify(notifyEvent, null);
        // Ensure that the afterEvent is never called since we're adding it and removing it
        verify(afterEvent, never()).matches(same(notifyEvent));
        verify(listener, never()).onEvent(afterEvent, null, null);
    }

    /** Verify that we can register two listeners on the same event and they'll both receive the event. */
    @Test
    public void testRegisterSeveralListenersForSameEvent() throws ComponentLookupException
    {
        final EventListener listener1 = mock(EventListener.class, "listener1");
        final EventListener listener2 = mock(EventListener.class, "listener2");
        final Event event = mock(Event.class, "event");
        final Event notifyEvent = mock(Event.class, "notify");

        when(listener1.getName()).thenReturn("listener 1");
        when(listener2.getName()).thenReturn("listener 2");
        when(listener1.getEvents()).thenReturn(Arrays.asList(event));
        when(listener2.getEvents()).thenReturn(Arrays.asList(event));

        when(event.matches(notifyEvent)).thenReturn(true);

        this.mocker.getComponentUnderTest().addListener(listener1);
        this.mocker.getComponentUnderTest().addListener(listener2);
        this.mocker.getComponentUnderTest().notify(notifyEvent, null);
        verify(listener1).onEvent(notifyEvent, null, null);
        verify(listener2).onEvent(notifyEvent, null, null);
    }

    /** Verify that we can register a listener to receive any kind of event using AllEvent event type. */
    @Test
    public void testRegisterListenerForAllEvents() throws ComponentLookupException
    {
        final EventListener listener = mock(EventListener.class);
        final Event event = mock(Event.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(AllEvent.ALLEVENT));

        this.mocker.getComponentUnderTest().addListener(listener);
        Assert.assertSame(listener, this.mocker.getComponentUnderTest().getListener("mylistener"));
        this.mocker.getComponentUnderTest().notify(event, "some source", "some data");
        verify(listener).onEvent(event, "some source", "some data");
    }

    /** Verify that a warning is logged is we try to register a listener with the same name. */
    @Test
    public void testRegisterSameListenerSeveralTimes() throws ComponentLookupException
    {
        final EventListener listener = mock(EventListener.class);

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(AllEvent.ALLEVENT));
        // The check is performed here, we verify that a warning is correctly logged

        this.mocker.getComponentUnderTest().addListener(listener);
        // Will raise log warning on the next line
        this.mocker.getComponentUnderTest().addListener(listener);

        final Logger logger = this.mocker.getMockedLogger();
        verify(logger).warn(contains("listener is overwriting a previously registered listener"), any(), any(), any());

        // Verify that no log is logged if we remove the listener before re-registering it
        this.mocker.getComponentUnderTest().removeListener("mylistener");
        // Next line will not log any warning
        this.mocker.getComponentUnderTest().addListener(listener);
        Mockito.verifyNoMoreInteractions(logger);
    }

    /**
     * Verify that we can register a listener with two events of the same type but with not matching the same thing and
     * it will receive both events.
     */
    @Test
    public void testRegisterListenerForTwoEventsOfSameType() throws ComponentLookupException
    {
        final EventListener listener = mock(EventListener.class);
        @SuppressWarnings("deprecation")
        final Event eventMatcher1 = new ActionExecutionEvent("action1");
        @SuppressWarnings("deprecation")
        final Event eventMatcher2 = new ActionExecutionEvent("action2");

        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(eventMatcher1, eventMatcher2));

        this.mocker.getComponentUnderTest().addListener(listener);
        this.mocker.getComponentUnderTest().notify(eventMatcher1, "some source", "some data");
        this.mocker.getComponentUnderTest().notify(eventMatcher2, "some source", "some data");
        verify(listener).onEvent(eventMatcher1, "some source", "some data");
        verify(listener).onEvent(eventMatcher2, "some source", "some data");
    }
}
