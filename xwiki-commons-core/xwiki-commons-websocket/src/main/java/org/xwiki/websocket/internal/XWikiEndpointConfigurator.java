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
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.websocket.EndpointComponent;
import org.xwiki.websocket.WebSocketContext;

/**
 * Used to instantiate WebSocket end-points as XWiki components.
 * 
 * @version $Id$
 * @since 13.7RC1
 */
@Component(roles = XWikiEndpointConfigurator.class)
@Singleton
public class XWikiEndpointConfigurator extends ServerEndpointConfig.Configurator
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private WebSocketContext context;

    @Override
    public <T> T getEndpointInstance(Class<T> endPointClass) throws InstantiationException
    {
        try {
            return this.componentManagerProvider.get().getInstance(EndpointComponent.class, endPointClass.getName());
        } catch (ComponentLookupException e) {
            throw new InstantiationException(ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response)
    {
        super.modifyHandshake(config, request, response);
        this.context.initialize(config, request, response);
    }
}
