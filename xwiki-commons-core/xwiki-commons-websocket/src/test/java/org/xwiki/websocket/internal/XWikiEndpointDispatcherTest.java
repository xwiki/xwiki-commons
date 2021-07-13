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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.websocket.EndpointComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Unit tests for {@link XWikiEndpointDispatcher}.
 * 
 * @version $Id$
 */
@ComponentTest
public class XWikiEndpointDispatcherTest
{
    @InjectMockComponents
    private XWikiEndpointDispatcher dispatcher;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    @Named("context")
    private ComponentManager contextComponentManager;

    @Captor
    ArgumentCaptor<CloseReason> closeReasonCaptor;

    @Test
    void onOpenWithMissingEndPoint() throws Exception
    {
        Session session = mock(Session.class);
        when(session.getPathParameters()).thenReturn(Collections.singletonMap("roleHint", "missing"));
        when(this.contextComponentManager.getInstance(EndpointComponent.class, "missing"))
            .thenThrow(new ComponentLookupException("Missing"));

        this.dispatcher.onOpen(session, null);

        verify(session).close(this.closeReasonCaptor.capture());
        CloseReason reason = this.closeReasonCaptor.getValue();
        assertEquals(CloseReason.CloseCodes.CANNOT_ACCEPT, reason.getCloseCode());
        assertEquals("ComponentLookupException: Missing", reason.getReasonPhrase());
    }

    @Test
    void onOpenWithInvalidEndPoint() throws Exception
    {
        Session session = mock(Session.class);
        when(session.getPathParameters()).thenReturn(Collections.singletonMap("roleHint", "invalid"));

        EndpointComponent endPoint = mock(EndpointComponent.class);
        when(this.contextComponentManager.getInstance(EndpointComponent.class, "invalid")).thenReturn(endPoint);

        this.dispatcher.onOpen(session, null);

        verify(session).close(this.closeReasonCaptor.capture());
        CloseReason reason = this.closeReasonCaptor.getValue();
        assertEquals(CloseReason.CloseCodes.CANNOT_ACCEPT, reason.getCloseCode());
        assertTrue(reason.getReasonPhrase().endsWith("doesn't extend Endpoint."));
    }

    @Test
    void onOpenDispatcher() throws Exception
    {
        Session session = mock(Session.class);
        when(session.getPathParameters())
            .thenReturn(Collections.singletonMap("roleHint", XWikiEndpointDispatcher.class.getName()));

        XWikiEndpointDispatcher endPoint = mock(XWikiEndpointDispatcher.class, "dispatcher");
        when(this.contextComponentManager.getInstance(EndpointComponent.class, XWikiEndpointDispatcher.class.getName()))
            .thenReturn(endPoint);

        this.dispatcher.onOpen(session, null);

        verify(endPoint, never()).onOpen(any(Session.class), any(EndpointConfig.class));
    }

    @Test
    void closeOpenSessions() throws Exception
    {
        // Open the first session.
        Session sessionOne = mock(Session.class, "one");
        when(sessionOne.getId()).thenReturn("1");
        when(sessionOne.getPathParameters()).thenReturn(Collections.singletonMap("roleHint", "one"));

        Endpoint endPointOne =
            mock(Endpoint.class, withSettings().name("one").extraInterfaces(EndpointComponent.class));
        when(this.contextComponentManager.getInstance(EndpointComponent.class, "one")).thenReturn(endPointOne);

        EndpointConfig configOne = mock(EndpointConfig.class, "one");
        XWikiEndpointDispatcher instanceOne =
            this.componentManager.getInstance(EndpointComponent.class, XWikiEndpointDispatcher.class.getName());
        instanceOne.onOpen(sessionOne, configOne);

        verify(endPointOne).onOpen(sessionOne, configOne);

        // Open the second session.
        Session sessionTwo = mock(Session.class, "two");
        when(sessionTwo.getId()).thenReturn("2");
        when(sessionTwo.getPathParameters()).thenReturn(Collections.singletonMap("roleHint", "two"));

        Endpoint endPointTwo =
            mock(Endpoint.class, withSettings().name("two").extraInterfaces(EndpointComponent.class));
        when(this.contextComponentManager.getInstance(EndpointComponent.class, "two")).thenReturn(endPointTwo);

        EndpointConfig configTwo = mock(EndpointConfig.class, "two");
        XWikiEndpointDispatcher instanceTwo =
            this.componentManager.getInstance(EndpointComponent.class, XWikiEndpointDispatcher.class.getName());
        instanceTwo.onOpen(sessionTwo, configTwo);

        verify(endPointTwo).onOpen(sessionTwo, configTwo);

        // Open the third session.
        Session sessionThree = mock(Session.class, "three");
        when(sessionThree.getId()).thenReturn("3");
        when(sessionThree.getPathParameters()).thenReturn(Collections.singletonMap("roleHint", "three"));

        Endpoint endPointThree =
            mock(Endpoint.class, withSettings().name("three").extraInterfaces(EndpointComponent.class));
        when(this.contextComponentManager.getInstance(EndpointComponent.class, "three")).thenReturn(endPointThree);

        EndpointConfig configThree = mock(EndpointConfig.class, "three");
        XWikiEndpointDispatcher instanceThree =
            this.componentManager.getInstance(EndpointComponent.class, XWikiEndpointDispatcher.class.getName());
        instanceThree.onOpen(sessionThree, configThree);

        verify(endPointThree).onOpen(sessionThree, configThree);

        // Close the first session.
        CloseReason reason = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Navigate");
        instanceOne.onClose(sessionOne, reason);
        verify(endPointOne).onClose(sessionOne, reason);

        // Error on the third session.
        Exception error = new Exception("test");
        instanceThree.onError(sessionThree, error);
        verify(endPointThree).onError(sessionThree, error);

        // Close open sessions (only the second session should still be open).
        reason = new CloseReason(CloseReason.CloseCodes.SERVICE_RESTART, "Restart");
        XWikiEndpointDispatcher.closeOpenSessions(reason);

        verify(sessionTwo).close(reason);
        verify(sessionOne, never()).close(any());
        verify(sessionThree, never()).close(any());
    }
}
