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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.observation.ObservationManager;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Test {@link QueueComponentEventManager}.
 *
 * @version $Id$
 */
class QueueComponentEventManagerTest
{
    private QueueComponentEventManager eventManager;

    private DefaultComponentDescriptor<CharSequence> descriptor1;

    private DefaultComponentDescriptor<Collection> descriptor2;

    private ObservationManager observationManager;

    private ComponentManager componentManager;

    @BeforeEach
    void setUp()
    {
        this.eventManager = new QueueComponentEventManager();

        this.descriptor1 = new DefaultComponentDescriptor<>();
        this.descriptor1.setImplementation(String.class);
        this.descriptor1.setRoleType(CharSequence.class);
        this.descriptor1.setRoleHint("hint1");
        this.descriptor2 = new DefaultComponentDescriptor<>();
        this.descriptor2.setImplementation(ArrayList.class);
        this.descriptor2.setRoleType(Collection.class);
        this.descriptor2.setRoleHint("hint2");

        this.observationManager = mock(ObservationManager.class);
        this.componentManager = mock(ComponentManager.class);

        this.eventManager.setObservationManager(this.observationManager);
    }

    @Test
    void flushEvents()
    {
        this.eventManager.shouldQueue(true);

        this.eventManager.notifyComponentRegistered(this.descriptor1);
        this.eventManager.notifyComponentRegistered(this.descriptor1, this.componentManager);
        this.eventManager.notifyComponentUnregistered(this.descriptor2);
        this.eventManager.notifyComponentUnregistered(this.descriptor2, this.componentManager);

        ComponentDescriptorAddedEvent addedEvent =
            new ComponentDescriptorAddedEvent(this.descriptor1.getRoleType(), this.descriptor1.getRoleHint());
        ComponentDescriptorRemovedEvent removedEvent =
            new ComponentDescriptorRemovedEvent(this.descriptor2.getRoleType(), this.descriptor2.getRoleHint());

        List<Object[]> calls = new ArrayList<>();
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            Object arg1 = invocation.getArgument(1);
            Object arg2 = invocation.getArgument(2);

            calls.add(new Object[] {arg0, arg1, arg2});

            return null;
        }).when(this.observationManager).notify(any(), any(), any());

        this.eventManager.flushEvents();

        assertEquals(4, calls.size());

        assertEquals(addedEvent, calls.get(0)[0]);
        assertNull(calls.get(0)[1]);
        assertSame(this.descriptor1, calls.get(0)[2]);

        assertEquals(addedEvent, calls.get(1)[0]);
        assertSame(this.componentManager, calls.get(1)[1]);
        assertSame(this.descriptor1, calls.get(1)[2]);

        assertEquals(removedEvent, calls.get(2)[0]);
        assertNull(calls.get(2)[1]);
        assertSame(this.descriptor2, calls.get(2)[2]);

        assertEquals(removedEvent, calls.get(3)[0]);
        assertSame(this.componentManager, calls.get(3)[1]);
        assertSame(this.descriptor2, calls.get(3)[2]);
    }
}
