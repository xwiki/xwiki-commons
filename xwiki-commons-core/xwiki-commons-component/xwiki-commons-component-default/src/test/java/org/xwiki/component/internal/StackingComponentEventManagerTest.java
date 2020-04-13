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

import java.util.ArrayList;
import java.util.Collection;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.observation.ObservationManager;

/**
 * Test {@link StackingComponentEventManager}.
 *
 * @version $Id$
 */
public class StackingComponentEventManagerTest
{
    private StackingComponentEventManager eventManager;

    private DefaultComponentDescriptor<CharSequence> descriptor1;

    private DefaultComponentDescriptor<Collection> descriptor2;

    private ObservationManager mockObservationManager;

    private ComponentManager mockComponentManager;

    private Mockery mockery;

    @Before
    public void setUp()
    {
        this.eventManager = new StackingComponentEventManager();

        this.descriptor1 = new DefaultComponentDescriptor<>();
        this.descriptor1.setImplementation(String.class);
        this.descriptor1.setRoleType(CharSequence.class);
        this.descriptor1.setRoleHint("hint1");
        this.descriptor2 = new DefaultComponentDescriptor<>();
        this.descriptor2.setImplementation(ArrayList.class);
        this.descriptor2.setRoleType(Collection.class);
        this.descriptor2.setRoleHint("hint2");

        this.mockery = new Mockery();

        this.mockObservationManager = this.mockery.mock(ObservationManager.class);
        this.mockComponentManager = this.mockery.mock(ComponentManager.class);

        this.eventManager.setObservationManager(this.mockObservationManager);
    }

    @After
    public void tearDown()
    {
        this.mockery.assertIsSatisfied();
    }

    // Tests

    @Test
    public void flushEvents()
    {
        this.eventManager.shouldStack(true);

        this.eventManager.notifyComponentRegistered(this.descriptor1);
        this.eventManager.notifyComponentRegistered(this.descriptor1, this.mockComponentManager);
        this.eventManager.notifyComponentUnregistered(this.descriptor2);
        this.eventManager.notifyComponentUnregistered(this.descriptor2, this.mockComponentManager);

        final ComponentDescriptorAddedEvent addedEvent =
            new ComponentDescriptorAddedEvent(this.descriptor1.getRoleType(), this.descriptor1.getRoleHint());
        final ComponentDescriptorRemovedEvent removedEvent =
            new ComponentDescriptorRemovedEvent(this.descriptor2.getRoleType(), this.descriptor2.getRoleHint());

        this.mockery.checking(new Expectations()
        {
            {
                oneOf(mockObservationManager).notify(with(equal(addedEvent)), with(same(mockComponentManager)),
                    with(same(descriptor1)));
                oneOf(mockObservationManager).notify(with(equal(addedEvent)), with(aNull(Object.class)),
                    with(same(descriptor1)));
                oneOf(mockObservationManager).notify(with(equal(removedEvent)), with(same(mockComponentManager)),
                    with(same(descriptor2)));
                oneOf(mockObservationManager).notify(with(equal(removedEvent)), with(aNull(Object.class)),
                    with(same(descriptor2)));
            }
        });

        this.eventManager.flushEvents();
    }
}
