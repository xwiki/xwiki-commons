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
package org.xwiki.netflux.internal;

import jakarta.websocket.Session;

import org.xwiki.component.annotation.Role;

/**
 * Configuration options for the Netflux WebSocket end-point.
 *
 * @version $Id$
 * @since 18.9.0RC1
 */
@Role
public interface NetfluxConfiguration
{
    /**
     * @return the number of milliseconds after which an inactive WebSocket session is closed; it should be well above
     *         {@link #getPingInterval()}, and when server pings are disabled it should be at least 90 seconds, the
     *         client side disconnect threshold, because the client's own PING messages are throttled in background
     *         tabs to one per minute
     * @see Session#getMaxIdleTimeout()
     */
    long getMaxIdleTimeout();

    /**
     * @return the number of milliseconds between two WebSocket ping messages sent by the server to each client; zero
     *         or a negative value disables the server pings
     */
    long getPingInterval();

    /**
     * @return the number of consecutive ping messages a client can leave unanswered before the server closes its
     *         WebSocket session
     */
    int getPingMaxMissedPongs();
}
