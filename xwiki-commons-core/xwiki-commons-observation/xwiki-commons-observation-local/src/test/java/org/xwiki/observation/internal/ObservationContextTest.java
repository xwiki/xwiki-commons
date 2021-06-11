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

import org.junit.jupiter.api.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.BeginEvent;
import org.xwiki.observation.event.EndEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.internal.DefaultObservationContext;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.observation.internal.ObservationContextListener;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultObservationContext}.
 *
 * @version $Id$
 */
@ComponentTest
public class ObservationContextTest
{
    @InjectMockComponents
    private DefaultObservationContext observationContext;

    @Test
    public void test(MockitoComponentManager componentManager) throws Exception
    {
        componentManager.registerComponent(ObservationContextListener.class);
        componentManager.registerComponent(DefaultObservationManager.class);

        ObservationManager manager = componentManager.getInstance(ObservationManager.class);
        Execution execution = componentManager.getInstance(Execution.class);

        when(execution.getContext()).thenReturn(new ExecutionContext());

        BeginEvent beginEvent1 = mock(BeginEvent.class, "begin1");
        BeginEvent beginEvent2 = mock(BeginEvent.class, "begin2");
        EndEvent endEvent1 = mock(EndEvent.class, "end1");
        EndEvent endEvent2 = mock(EndEvent.class, "end2");

        when(beginEvent1.matches(any(Event.class))).thenReturn(false);
        when(beginEvent1.matches(beginEvent1)).thenReturn(true);

        when(beginEvent2.matches(any(Event.class))).thenReturn(false);
        when(beginEvent2.matches(beginEvent2)).thenReturn(true);

        when(endEvent1.matches(any(Event.class))).thenReturn(false);
        when(endEvent1.matches(endEvent1)).thenReturn(true);

        when(endEvent2.matches(any(Event.class))).thenReturn(false);
        when(endEvent2.matches(endEvent2)).thenReturn(true);

        assertFalse(this.observationContext.isIn(beginEvent1));
        assertFalse(this.observationContext.isIn(beginEvent2));

        manager.notify(beginEvent1, null);

        assertTrue(this.observationContext.isIn(beginEvent1));

        manager.notify(beginEvent2, null);

        assertTrue(this.observationContext.isIn(beginEvent1));
        assertTrue(this.observationContext.isIn(beginEvent2));

        manager.notify(endEvent2, null);

        assertTrue(this.observationContext.isIn(beginEvent1));
        assertFalse(this.observationContext.isIn(beginEvent2));

        manager.notify(endEvent1, null);

        assertFalse(this.observationContext.isIn(beginEvent1));
        assertFalse(this.observationContext.isIn(beginEvent2));
    }
}
