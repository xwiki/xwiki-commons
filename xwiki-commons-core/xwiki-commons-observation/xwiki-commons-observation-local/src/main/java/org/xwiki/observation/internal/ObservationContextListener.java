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

import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.AllEvent;
import org.xwiki.observation.event.BeginEvent;
import org.xwiki.observation.event.EndEvent;
import org.xwiki.observation.event.Event;

/**
 * Listen to all events and stack {@link BeginEvent}.
 *
 * @version $Id$
 * @since 3.2M1
 */
@Component
@Singleton
@Named("ObservationContextListener")
public class ObservationContextListener extends AbstractEventListener
{
    /**
     * The execution.
     */
    @Inject
    private Execution execution;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Setup event listener.
     */
    public ObservationContextListener()
    {
        super("ObservationContextListener", AllEvent.ALLEVENT);
    }

    /**
     * @return the events stacked in the execution context
     */
    private Stack<BeginEvent> getCurrentEvents()
    {
        Stack<BeginEvent> events = null;

        ExecutionContext context = this.execution.getContext();
        if (context != null) {
            events = (Stack<BeginEvent>) context.getProperty(DefaultObservationContext.KEY_EVENTS);
        }

        return events;
    }

    /**
     * @param event the event stack
     */
    private void pushCurrentEvent(BeginEvent event)
    {
        ExecutionContext context = this.execution.getContext();
        if (context != null) {
            Stack<BeginEvent> events = (Stack<BeginEvent>) context.getProperty(DefaultObservationContext.KEY_EVENTS);

            if (events == null) {
                events = new Stack<>();
                context.setProperty(DefaultObservationContext.KEY_EVENTS, events);
            }

            events.push(event);
        }
    }

    // EventListener

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof BeginEvent) {
            pushCurrentEvent((BeginEvent) event);
        } else if (event instanceof EndEvent) {
            Stack<BeginEvent> events = getCurrentEvents();

            if (events != null && !events.isEmpty()) {
                events.pop();
            } else {
                this.logger.error("Can't find any begin event corresponding to [{}]", event);
            }
        }
    }
}
