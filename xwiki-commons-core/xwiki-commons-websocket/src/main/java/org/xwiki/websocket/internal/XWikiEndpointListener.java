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
package org.xwiki.websocket.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.websocket.CloseReason;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.event.ComponentDescriptorEvent;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.Event;
import org.xwiki.websocket.EndpointComponent;

/**
 * Triggers the initialization of the WebSocket end-points and updates them whenever a new {@link EndpointComponent} is
 * installed or uninstalled.
 * 
 * @version $Id$
 * @since 13.7RC1
 */
@Component
@Named(XWikiEndpointListener.HINT)
@Singleton
public class XWikiEndpointListener extends AbstractEventListener
{
    /**
     * The role hint of the component.
     */
    public static final String HINT = "org.xwiki.websocket.internal.XWikiEndpointListener";

    static final JobStartedEvent PARENT = new JobStartedEvent();

    private static final String UPDATE = "websocket.update";

    @Inject
    private Logger logger;

    @Inject
    private ObservationContext observationContext;

    @Inject
    private Execution execution;

    /**
     * We inject it just to make sure the WebSocket (static) end-points are being registered.
     */
    @Inject
    @SuppressWarnings("unused")
    private XWikiEndpointInitializer initializer;

    /**
     * The default constructor.
     */
    public XWikiEndpointListener()
    {
        super(HINT, new ComponentDescriptorAddedEvent(EndpointComponent.class),
            new ComponentDescriptorRemovedEvent(EndpointComponent.class), new JobFinishedEvent("install"),
            new JobFinishedEvent("uninstall"));
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ComponentDescriptorEvent) {
            onComponentDescriptorEvent();
        } else {
            flush();
        }
    }

    private void onComponentDescriptorEvent()
    {
        if (this.observationContext.isIn(PARENT)) {
            // If inside a job, just remember to update when the job ends.
            updateLater();
        } else {
            // If not inside a job, update right away.
            updateNow();
        }
    }

    private void updateLater()
    {
        ExecutionContext context = this.execution.getContext();
        if (context != null) {
            context.setProperty(UPDATE, true);
        }
    }

    private void updateNow()
    {
        try {
            // Close the open sessions in order to force the WebSocket clients to reconnect in order to use the updated
            // server end-points (e.g. after a new version of a server end-point has been installed).
            XWikiEndpointDispatcher.closeOpenSessions(new CloseReason(CloseReason.CloseCodes.SERVICE_RESTART,
                "The server end-point is being updated. Please reconnect."));

            // Remove the update marker.
            ExecutionContext context = this.execution.getContext();
            if (context != null) {
                context.removeProperty(UPDATE);
            }
        } catch (Exception e) {
            this.logger.error("Failed to update the WebSocket end-points.", e);
        }
    }

    private void flush()
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null && context.hasProperty(UPDATE)) {
            updateNow();
        }
    }
}
