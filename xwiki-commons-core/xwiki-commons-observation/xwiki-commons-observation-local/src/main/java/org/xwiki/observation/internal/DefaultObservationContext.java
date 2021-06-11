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

import java.util.Deque;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.BeginEvent;

/**
 * Default implementation of {@link ObservationContext}.
 *
 * @version $Id$
 * @since 3.2M1
 */
@Component
@Singleton
public class DefaultObservationContext implements ObservationContext
{
    /**
     * The name of the property storing current events.
     */
    static final String KEY_EVENTS = "observation.currentevents";

    /**
     * The execution.
     */
    @Inject
    private Execution execution;

    /**
     * @return the events stacked in the execution context
     */
    private Deque<BeginEvent> getCurrentEvents()
    {
        Deque<BeginEvent> events = null;

        ExecutionContext context = this.execution.getContext();
        if (context != null) {
            events = (Deque<BeginEvent>) context.getProperty(KEY_EVENTS);
        }

        return events;
    }

    @Override
    public boolean isIn(BeginEvent event)
    {
        Deque<BeginEvent> events = getCurrentEvents();

        if (events != null) {
            for (BeginEvent currentEvent : events) {
                if (event.matches(currentEvent)) {
                    return true;
                }
            }
        }
        return false;
    }
}
