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

import javax.inject.Named;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.websocket.EndpointComponent;
import org.xwiki.websocket.WebSocketContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XWikiEndpointConfigurator}.
 * 
 * @version $Id$
 */
@ComponentTest
class XWikiEndpointConfiguratorTest
{
    @InjectMockComponents
    private XWikiEndpointConfigurator configurator;

    @MockComponent
    @Named("context")
    private ComponentManager contextComponentManager;

    @MockComponent
    private WebSocketContext context;

    @Test
    void getExistingEndpointInstance() throws Exception
    {
        String instance = "test";

        when(this.contextComponentManager.getInstance(EndpointComponent.class, String.class.getName()))
            .thenReturn(instance);
        assertSame(instance, this.configurator.getEndpointInstance(String.class));
    }

    @Test
    void getMissingEndpointInstance() throws Exception
    {
        when(this.contextComponentManager.getInstance(EndpointComponent.class, String.class.getName()))
            .thenThrow(new ComponentLookupException("test"));

        try {
            this.configurator.getEndpointInstance(String.class);
            fail();
        } catch (InstantiationException e) {
            assertEquals("ComponentLookupException: test", e.getMessage());
        }
    }

    @Test
    void modifyHandshake()
    {
        ServerEndpointConfig config = mock(ServerEndpointConfig.class);
        HandshakeRequest request = mock(HandshakeRequest.class);
        HandshakeResponse response = mock(HandshakeResponse.class);
        this.configurator.modifyHandshake(config, request, response);
        verify(this.context).initialize(config, request, response);
    }
}
