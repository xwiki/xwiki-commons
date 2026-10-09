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

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Default implementation of {@link NetfluxConfiguration}.
 *
 * @version $Id$
 * @since 18.9.0RC1
 */
@Component
@Singleton
public class DefaultNetfluxConfiguration implements NetfluxConfiguration
{
    private static final String PREFIX = "netflux.";

    @Inject
    private Provider<ConfigurationSource> configuration;

    @Override
    public long getMaxIdleTimeout()
    {
        // The server pings the clients regularly (see #getPingInterval()) and the browsers answer these pings without
        // involving JavaScript timers, so the idle timeout is normally never reached, even in background tabs where
        // the browsers throttle the timers used by the client side to send its own PING messages (up to once per
        // minute). The idle timeout is kept as a safety net for the WebSocket containers that don't count the ping and
        // pong messages as activity. When the server pings are disabled, this value must be raised to at least the
        // client side disconnect threshold (90 seconds) because the client PING messages are then the only activity.
        // See https://developer.chrome.com/blog/timer-throttling-in-chrome-88/
        return getProperty("maxIdleTimeout", 60000L + 15000L);
    }

    @Override
    public long getPingInterval()
    {
        // The default value allows us to send 3 ping messages before the default session idle timeout is reached, see
        // #getMaxIdleTimeout() and #getPingMaxMissedPongs().
        return getProperty("pingInterval", 25000L);
    }

    @Override
    public int getPingMaxMissedPongs()
    {
        // With the default ping interval, a client that stops answering is disconnected 100 seconds after its last
        // answer, see #getPingInterval(). This holds on the WebSocket containers that count the ping messages sent by
        // the server as activity (e.g. Tomcat and Jetty), because the session idle timeout can't be reached while we
        // keep sending pings. On a container that counts only the incoming messages, the session idle timeout (see
        // #getMaxIdleTimeout()) closes the session earlier.
        return getProperty("pingMaxMissedPongs", 3);
    }

    private <T> T getProperty(String propertyName, T defaultValue)
    {
        return this.configuration.get().getProperty(PREFIX + propertyName, defaultValue);
    }
}
