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

import java.util.Collections;

import javax.inject.Named;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.observation.ObservationContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.websocket.EndpointComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Unit tests for {@link XWikiEndpointListener}.
 * 
 * @version $Id$
 */
@ComponentTest
class XWikiEndpointListenerTest
{
    @InjectMockComponents
    private XWikiEndpointListener listener;

    @InjectMockComponents
    private XWikiEndpointDispatcher dispatcher;

    @MockComponent
    private ObservationContext observationContext;

    @MockComponent
    private Execution execution;

    @MockComponent
    @Named("context")
    private ComponentManager contextComponentManager;

    @Mock
    private Session session;

    @Captor
    ArgumentCaptor<CloseReason> closeReasonCaptor;

    @BeforeEach
    void setup() throws Exception
    {
        ExecutionContext context = new ExecutionContext();
        when(this.execution.getContext()).thenReturn(context);

        // Open a WebSocket session.
        when(this.session.getId()).thenReturn("1");
        when(this.session.getPathParameters()).thenReturn(Collections.singletonMap("roleHint", "test"));

        Endpoint endPoint = mock(Endpoint.class, withSettings().extraInterfaces(EndpointComponent.class));
        when(this.contextComponentManager.getInstance(EndpointComponent.class, "test")).thenReturn(endPoint);

        EndpointConfig endPointConfig = mock(EndpointConfig.class);
        this.dispatcher.onOpen(this.session, endPointConfig);
    }

    @Test
    void onComponentDescriptorAdded() throws Exception
    {
        this.listener.onEvent(new ComponentDescriptorAddedEvent(), null, null);

        verifySessionClose();
    }

    @Test
    void onInstall() throws Exception
    {
        when(this.observationContext.isIn(XWikiEndpointListener.PARENT)).thenReturn(true);

        this.listener.onEvent(new ComponentDescriptorAddedEvent(), null, null);
        this.listener.onEvent(new JobFinishedEvent(), null, null);

        verifySessionClose();
    }

    @Test
    void onInstallWithoutComponentsAdded() throws Exception
    {
        this.listener.onEvent(new JobFinishedEvent(), null, null);

        verify(this.session, never()).close(any(CloseReason.class));
    }

    void verifySessionClose() throws Exception
    {
        verify(this.session).close(this.closeReasonCaptor.capture());
        CloseReason reason = this.closeReasonCaptor.getValue();
        assertEquals(CloseReason.CloseCodes.SERVICE_RESTART, reason.getCloseCode());
        assertEquals("The server end-point is being updated. Please reconnect.", reason.getReasonPhrase());
    }
}
