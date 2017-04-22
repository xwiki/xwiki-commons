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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.observation.test.TestEventListener;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ObservationManager}.
 *
 * @version $Id$
 */
public class ObservationManagerEventListenerTest
{
    @Rule
    public final MockitoComponentMockingRule<ObservationManager> mocker =
        new MockitoComponentMockingRule<ObservationManager>(DefaultObservationManager.class);

    private ObservationManager manager;

    private EventListener eventListenerMock;

    private Event eventMock;

    private DefaultComponentDescriptor<EventListener> componentDescriptor;

    @Before
    public void setUp() throws Exception
    {
        this.manager = this.mocker.getComponentUnderTest();
        StackingComponentEventManager componentEventManager = new StackingComponentEventManager();
        componentEventManager.shouldStack(false);
        componentEventManager.setObservationManager(this.manager);
        this.mocker.setComponentEventManager(componentEventManager);

        this.eventListenerMock = mock(EventListener.class);
        this.eventMock = mock(Event.class);

        this.componentDescriptor = new DefaultComponentDescriptor<EventListener>();
        this.componentDescriptor.setImplementation(this.eventListenerMock.getClass());
        this.componentDescriptor.setRoleType(EventListener.class);
        this.componentDescriptor.setRoleHint("mylistener");

        when(this.eventMock.matches(this.eventMock)).thenReturn(true);
        when(this.eventListenerMock.getName()).thenReturn("mylistener");
        when(this.eventListenerMock.getEvents()).thenReturn(Arrays.asList(this.eventMock));
    }

    @Test
    public void newListenerComponent() throws Exception
    {
        this.mocker.registerComponent(this.componentDescriptor, this.eventListenerMock);

        Assert.assertSame(this.eventListenerMock, this.manager.getListener("mylistener"));
    }

    @Test
    public void removedListenerComponent() throws Exception
    {
        this.mocker.registerComponent(this.componentDescriptor, this.eventListenerMock);
        this.mocker.unregisterComponent(this.componentDescriptor.getRoleType(), this.componentDescriptor.getRoleHint());

        Assert.assertNull(this.manager.getListener("mylistener"));
    }
}
