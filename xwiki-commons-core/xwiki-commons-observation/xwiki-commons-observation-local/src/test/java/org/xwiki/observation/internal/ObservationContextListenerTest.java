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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Deque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.internal.ExecutionContextProperty;
import org.xwiki.observation.event.BeginEvent;
import org.xwiki.observation.event.EndEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ObservationContextListener}
 *
 * @version $Id$
 * @since 13.5RC1
 */
@ComponentTest
class ObservationContextListenerTest
{
    @InjectMockComponents
    private ObservationContextListener contextListener;

    @MockComponent
    private Execution execution;

    private ExecutionContext executionContext;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @BeforeEach
    void setup()
    {
        // We don't use a mock since it's easier to make comparison while using the property builder.
        this.executionContext = new ExecutionContext();
        when(this.execution.getContext()).thenReturn(this.executionContext);
    }

    @Test
    void onNotFoldedEvent()
    {
        Event myEvent = mock(Event.class);
        this.contextListener.onEvent(myEvent, null, null);
        verify(this.execution, never()).getContext();
    }

    /**
     * Access the properties via reflection. This method requires ReflectPermission suppressAccessChecks.
     *
     * @param key The property key
     * @return the execution context property corresponding to the given key.
     */
    @SuppressWarnings("unchecked")
    private ExecutionContextProperty fetch(String key) throws Exception
    {
        Field propertiesField = ExecutionContext.class.getDeclaredField("properties");

        propertiesField.setAccessible(true);

        Map<String, ExecutionContextProperty> properties =
            (Map<String, ExecutionContextProperty>) propertiesField.get(this.executionContext);

        return properties.get(key);
    }

    @Test
    void onBeginEvent() throws Exception
    {
        BeginEvent myBeginEvent = mock(BeginEvent.class);
        this.contextListener.onEvent(myBeginEvent, null, null);
        Deque<BeginEvent> events =
            (Deque<BeginEvent>) this.executionContext.getProperty(DefaultObservationContext.KEY_EVENTS);
        assertEquals(1, events.size());
        assertEquals(myBeginEvent, events.getFirst());

        ExecutionContextProperty executionContextProperty = this.fetch(DefaultObservationContext.KEY_EVENTS);
        assertTrue(executionContextProperty.isInherited());

        BeginEvent myOtherBeginEvent = mock(BeginEvent.class);
        this.contextListener.onEvent(myOtherBeginEvent, null, null);
        events =
            (Deque<BeginEvent>) this.executionContext.getProperty(DefaultObservationContext.KEY_EVENTS);
        assertEquals(2, events.size());
        assertEquals(myOtherBeginEvent, events.pop());
        assertEquals(myBeginEvent, events.pop());

        executionContextProperty = this.fetch(DefaultObservationContext.KEY_EVENTS);
        assertTrue(executionContextProperty.isInherited());
        assertTrue(executionContextProperty.isFinal());
    }

    @Test
    void onEndEvent()
    {
        EndEvent myEndEvent = mock(EndEvent.class);
        when(myEndEvent.toString()).thenReturn("myEndEvent");
        this.contextListener.onEvent(myEndEvent, null, null);
        assertEquals("Can't find any begin event corresponding to [myEndEvent]",
            logCapture.getMessage(0));

        BeginEvent myBeginEvent = mock(BeginEvent.class);
        this.contextListener.onEvent(myBeginEvent, null, null);
        this.contextListener.onEvent(myBeginEvent, null, null);

        this.contextListener.onEvent(myEndEvent, null, null);
        Deque<BeginEvent> events =
            (Deque<BeginEvent>) this.executionContext.getProperty(DefaultObservationContext.KEY_EVENTS);
        assertEquals(1, events.size());
        assertEquals(myBeginEvent, events.getFirst());

        this.contextListener.onEvent(myEndEvent, null, null);
        events =
            (Deque<BeginEvent>) this.executionContext.getProperty(DefaultObservationContext.KEY_EVENTS);
        assertTrue(events.isEmpty());
    }
}
