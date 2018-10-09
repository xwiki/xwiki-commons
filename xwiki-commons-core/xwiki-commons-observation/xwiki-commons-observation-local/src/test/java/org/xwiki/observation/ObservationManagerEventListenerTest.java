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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ObservationManager}.
 *
 * @version $Id$
 */
@ComponentTest
public class ObservationManagerEventListenerTest
{
    @InjectMockComponents
    private DefaultObservationManager manager;

    @InjectComponentManager
    private ComponentManager componentManager;

    private EventListener eventListenerMock;

    private Event eventMock;

    private DefaultComponentDescriptor<EventListener> componentDescriptor;

    @BeforeEach
    public void setUp()
    {
        StackingComponentEventManager componentEventManager = new StackingComponentEventManager();
        componentEventManager.shouldStack(false);
        componentEventManager.setObservationManager(this.manager);
        this.componentManager.setComponentEventManager(componentEventManager);

        this.eventListenerMock = mock(EventListener.class);
        this.eventMock = mock(Event.class);

        this.componentDescriptor = new DefaultComponentDescriptor<>();
        this.componentDescriptor.setImplementation(this.eventListenerMock.getClass());
        this.componentDescriptor.setRoleType(EventListener.class);
        this.componentDescriptor.setRoleHint("mylistener");

        when(this.eventMock.matches(this.eventMock)).thenReturn(true);
        when(this.eventListenerMock.getName()).thenReturn("mylistener");
        when(this.eventListenerMock.getEvents()).thenReturn(Arrays.asList(this.eventMock));
    }

    @Test
    public void newListenerComponent() throws ComponentRepositoryException
    {
        this.componentManager.registerComponent(this.componentDescriptor, this.eventListenerMock);

        assertSame(this.eventListenerMock, this.manager.getListener("mylistener"));
    }

    @Test
    public void removedListenerComponent() throws Exception
    {
        this.componentManager.registerComponent(this.componentDescriptor, this.eventListenerMock);
        this.componentManager.unregisterComponent(this.componentDescriptor.getRoleType(),
            this.componentDescriptor.getRoleHint());

        assertNull(this.manager.getListener("mylistener"));
    }
}
