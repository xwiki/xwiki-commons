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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.observation.ObservationManager;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    private ObservationManager observationManager;

    private ComponentManager componentManager;

    @BeforeEach
    void setUp()
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

        this.observationManager = mock(ObservationManager.class);
        this.componentManager = mock(ComponentManager.class);

        this.eventManager.setObservationManager(this.observationManager);
    }

    @Test
    void flushEvents()
    {
        this.eventManager.shouldStack(true);

        this.eventManager.notifyComponentRegistered(this.descriptor1);
        this.eventManager.notifyComponentRegistered(this.descriptor1, this.componentManager);
        this.eventManager.notifyComponentUnregistered(this.descriptor2);
        this.eventManager.notifyComponentUnregistered(this.descriptor2, this.componentManager);

        ComponentDescriptorAddedEvent addedEvent =
            new ComponentDescriptorAddedEvent(this.descriptor1.getRoleType(), this.descriptor1.getRoleHint());
        ComponentDescriptorRemovedEvent removedEvent =
            new ComponentDescriptorRemovedEvent(this.descriptor2.getRoleType(), this.descriptor2.getRoleHint());

        this.eventManager.flushEvents();

        verify(this.observationManager, times(1)).notify(eq(addedEvent), same(this.componentManager),
            same(this.descriptor1));
        verify(this.observationManager, times(1)).notify(eq(addedEvent), eq(null), same(this.descriptor1));
        verify(this.observationManager, times(1)).notify(eq(removedEvent), same(this.componentManager),
            same(this.descriptor2));
        verify(this.observationManager, times(1)).notify(eq(removedEvent), eq(null), same(this.descriptor2));
    }
}
