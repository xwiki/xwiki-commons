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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.observation.event.BeginEvent;
import org.xwiki.observation.event.EndEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.internal.DefaultObservationContext;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.observation.internal.ObservationContextListener;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultObservationContext}.
 *
 * @version $Id$
 */
public class ObservationContextTest
{
    @Rule
    public final MockitoComponentMockingRule<ObservationContext> mocker =
        new MockitoComponentMockingRule<ObservationContext>(DefaultObservationContext.class);

    @Test
    public void test() throws Exception
    {
        this.mocker.registerComponent(ObservationContextListener.class);
        this.mocker.registerComponent(DefaultObservationManager.class);

        ObservationManager manager = this.mocker.getInstance(ObservationManager.class);
        Execution execution = this.mocker.getInstance(Execution.class);

        when(execution.getContext()).thenReturn(new ExecutionContext());

        final BeginEvent beginEvent1 = mock(BeginEvent.class, "begin1");
        final BeginEvent beginEvent2 = mock(BeginEvent.class, "begin2");
        final EndEvent endEvent1 = mock(EndEvent.class, "end1");
        final EndEvent endEvent2 = mock(EndEvent.class, "end2");

        when(beginEvent1.matches(any(Event.class))).thenReturn(false);
        when(beginEvent1.matches(beginEvent1)).thenReturn(true);

        when(beginEvent2.matches(any(Event.class))).thenReturn(false);
        when(beginEvent2.matches(beginEvent2)).thenReturn(true);

        when(endEvent1.matches(any(Event.class))).thenReturn(false);
        when(endEvent1.matches(endEvent1)).thenReturn(true);

        when(endEvent2.matches(any(Event.class))).thenReturn(false);
        when(endEvent2.matches(endEvent2)).thenReturn(true);

        Assert.assertFalse(this.mocker.getComponentUnderTest().isIn(beginEvent1));
        Assert.assertFalse(this.mocker.getComponentUnderTest().isIn(beginEvent2));

        manager.notify(beginEvent1, null);

        Assert.assertTrue(this.mocker.getComponentUnderTest().isIn(beginEvent1));

        manager.notify(beginEvent2, null);

        Assert.assertTrue(this.mocker.getComponentUnderTest().isIn(beginEvent1));
        Assert.assertTrue(this.mocker.getComponentUnderTest().isIn(beginEvent2));

        manager.notify(endEvent2, null);

        Assert.assertTrue(this.mocker.getComponentUnderTest().isIn(beginEvent1));
        Assert.assertFalse(this.mocker.getComponentUnderTest().isIn(beginEvent2));

        manager.notify(endEvent1, null);

        Assert.assertFalse(this.mocker.getComponentUnderTest().isIn(beginEvent1));
        Assert.assertFalse(this.mocker.getComponentUnderTest().isIn(beginEvent2));
    }
}
