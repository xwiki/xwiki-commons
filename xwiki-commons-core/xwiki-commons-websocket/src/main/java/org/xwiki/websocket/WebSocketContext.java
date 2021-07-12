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
package org.xwiki.websocket;

import java.util.concurrent.Callable;

import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Component used to initialize and bind a WebSocket execution context to a WebSocket session, and to run code within
 * that context when a WebSocket session is opened or when receiving messages.
 * 
 * @version $Id$
 * @since 13.6RC1
 */
@Role
@Unstable
public interface WebSocketContext
{
    /**
     * Initializes a new WebSocket context for the specified WebSocket end-point and binds it to the WebSocket
     * connection (session). The context must be created and initialized when the WebSocket handshake is performed
     * because later (during session opening and message handling) we don't have access to the HTTP request (headers,
     * cookies) and response. This method is normally called by
     * {@link ServerEndpointConfig.Configurator#modifyHandshake(ServerEndpointConfig, HandshakeRequest, HandshakeResponse)}.
     * <p>
     * Note that we cannot create the execution context inside a servlet filter because servlet filters are not
     * necessarily applied to WebSocket (upgrade) requests (it's not specified in the WebSocket JSR).
     * 
     * @param config the WebSocket end-point configuration, used to bind the initialized context to the WebSocket
     *            session
     * @param request the handshake request used to initialize the WebSocket context (e.g. authenticate the user)
     * @param response the handshake response used to initialize the WebSocket context
     */
    void initialize(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response);

    /**
     * Runs some code within the WebSocket context bound to the given WebSocket session. This method is usually called
     * when the WebSocket session is opened and when receiving messages because they happen in different threads and so
     * we cannot rely on thread-local variables. Instead, we rely on the WebSocket session to pass and share the
     * WebSocket context.
     * 
     * @param session the WebSocket session used to retrieve the WebSocket context
     * @param runnable the code to run
     */
    void run(Session session, Runnable runnable);

    /**
     * Call some code within the WebSocket context bound to the given WebSocket session. This method is usually called
     * when receiving messages because they happen in different threads and so we cannot rely on thread-local variables.
     * Instead, we rely on the WebSocket session to pass and share the WebSocket context.
     * 
     * @param <T> the type of value returned by the code to execute
     * @param session the WebSocket session used to retrieve the WebSocket context
     * @param callable the code to call
     * @return the value returned by the executed code
     * @throws Exception if the code to execute throws an exception (it simply lets the exception propagate)
     */
    <T> T call(Session session, Callable<T> callable) throws Exception;
}
