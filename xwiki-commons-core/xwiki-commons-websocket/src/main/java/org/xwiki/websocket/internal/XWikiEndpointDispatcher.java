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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.websocket.EndpointComponent;

/**
 * A static WebSocket end-point that dispatches the requests to the dynamic end-point specified in the URL.
 * 
 * @version $Id$
 * @since 13.7RC1
 */
@Component
@Named("org.xwiki.websocket.internal.XWikiEndpointDispatcher")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XWikiEndpointDispatcher extends Endpoint implements EndpointComponent
{
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    @Inject
    private Logger logger;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * The end-point where we dispatch the WebSocket session events.
     */
    private Endpoint endPoint;

    @Override
    public void onOpen(Session session, EndpointConfig config)
    {
        CloseReason closeReason = null;

        try {
            EndpointComponent endPointComponent = this.componentManagerProvider.get()
                .getInstance(EndpointComponent.class, session.getPathParameters().get("roleHint"));
            if (endPointComponent instanceof XWikiEndpointDispatcher) {
                // Prevent infinite loop.
            } else if (endPointComponent instanceof Endpoint) {
                SESSIONS.put(session.getId(), session);
                this.endPoint = (Endpoint) endPointComponent;
                this.endPoint.onOpen(session, config);
            } else {
                closeReason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, String
                    .format("The component [%s] doesn't extend Endpoint.", endPointComponent.getClass().getName()));
            }
        } catch (ComponentLookupException e) {
            closeReason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, ExceptionUtils.getRootCauseMessage(e));
        } catch (Exception e) {
            closeReason =
                new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, ExceptionUtils.getRootCauseMessage(e));
        }

        if (closeReason != null) {
            try {
                session.close(closeReason);
            } catch (IOException e) {
                this.logger.warn("Failed to close the session.", e);
            }
        }
    }

    @Override
    public void onError(Session session, Throwable error)
    {
        this.logger.debug("WebSocket connection error.", error);
        SESSIONS.remove(session.getId());
        if (this.endPoint != null) {
            this.endPoint.onError(session, error);
        }
    }

    @Override
    public void onClose(Session session, CloseReason reason)
    {
        SESSIONS.remove(session.getId());
        if (this.endPoint != null) {
            this.endPoint.onClose(session, reason);
        }
    }

    /**
     * Closes all open sessions handled by this end-point.
     * 
     * @param reason the reason to send when closing the session
     */
    public static void closeOpenSessions(CloseReason reason)
    {
        Set<String> closedSessions = new HashSet<>();
        for (Session session : SESSIONS.values()) {
            closedSessions.add(session.getId());
            try {
                session.close(reason);
            } catch (IOException e) {
                // Ignore.
            }
        }
        closedSessions.forEach(SESSIONS::remove);
    }
}
