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
package org.xwiki.job.internal;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.event.AbstractComponentDescriptorEvent;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.job.GroupedJobInitializer;
import org.xwiki.job.GroupedJobInitializerManager;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Listen on {@link ComponentDescriptorAddedEvent} and {@link ComponentDescriptorRemovedEvent}, and invalidate
 * the {@link GroupedJobInitializerManager} when an event occurs.
 *
 * @version $Id$
 * @since 12.5RC1
 */
@Component
@Named("GroupedJobInitializerListener")
@Singleton
public class GroupedJobInitializerComponentListener implements EventListener
{
    @Inject
    private Provider<GroupedJobInitializerManager> groupedJobInitializerManagerProvider;

    @Override
    public String getName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public List<Event> getEvents()
    {
        return Arrays.asList(new ComponentDescriptorAddedEvent(), new ComponentDescriptorRemovedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        AbstractComponentDescriptorEvent componentDescriptorEvent = (AbstractComponentDescriptorEvent) event;
        if (componentDescriptorEvent.getRoleType() == GroupedJobInitializer.class) {
            this.groupedJobInitializerManagerProvider.get().invalidateCache();
        }
    }
}
