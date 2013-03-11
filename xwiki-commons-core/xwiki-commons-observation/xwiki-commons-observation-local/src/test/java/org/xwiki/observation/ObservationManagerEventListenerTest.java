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

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.test.TestEventListener;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Unit tests for {@link ObservationManager}.
 * 
 * @version $Id$
 */
public class ObservationManagerEventListenerTest extends AbstractComponentTestCase
{
    private ObservationManager manager;

    private EventListener eventListenerMock;

    private Event eventMock;

    private DefaultComponentDescriptor<EventListener> componentDescriptor;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.manager = getComponentManager().getInstance(ObservationManager.class);
        StackingComponentEventManager componentEventManager = new StackingComponentEventManager();
        componentEventManager.shouldStack(false);
        componentEventManager.setObservationManager(this.manager);
        getComponentManager().setComponentEventManager(componentEventManager);

        this.eventListenerMock = getMockery().mock(EventListener.class);
        this.eventMock = getMockery().mock(Event.class);

        this.componentDescriptor = new DefaultComponentDescriptor<EventListener>();
        this.componentDescriptor.setImplementation(eventListenerMock.getClass());
        this.componentDescriptor.setRole(EventListener.class);
        this.componentDescriptor.setRoleHint("mylistener");

        getMockery().checking(new Expectations()
        {
            {
                allowing(eventMock).matches(with(same(eventMock)));
                will(returnValue(true));
                allowing(eventListenerMock).getName();
                will(returnValue("mylistener"));
                allowing(eventListenerMock).getEvents();
                will(returnValue(Arrays.asList(eventMock)));
            }
        });
    }

    @Test
    public void testNewListenerComponent() throws Exception
    {
        getComponentManager().registerComponent(this.componentDescriptor, this.eventListenerMock);

        Assert.assertSame(this.eventListenerMock, this.manager.getListener("mylistener"));
    }

    @Test
    public void testRemovedListenerComponent() throws Exception
    {
        getComponentManager().registerComponent(this.componentDescriptor, this.eventListenerMock);
        getComponentManager().unregisterComponent(this.componentDescriptor.getRole(),
            this.componentDescriptor.getRoleHint());

        Assert.assertNull(this.manager.getListener("mylistener"));
    }

    public void testInjectObservationManagerInAListener() throws ComponentLookupException, Exception
    {
        TestEventListener listener = getComponentManager().getInstance(EventListener.class, "test");

        Assert.assertNotNull(listener);
    }
}
