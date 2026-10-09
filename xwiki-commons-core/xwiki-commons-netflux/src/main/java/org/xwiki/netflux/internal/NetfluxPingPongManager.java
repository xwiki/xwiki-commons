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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.CloseReason;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.PongMessage;
import jakarta.websocket.Session;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

import static jakarta.websocket.CloseReason.CloseCodes.GOING_AWAY;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Keeps the WebSocket sessions of the local Netflux users alive by sending them WebSocket ping messages at regular
 * intervals, and closes the sessions of the users that stop answering them.
 * <p>
 * The client side also sends PING messages from time to time, but it does so using JavaScript timers that the browsers
 * throttle in background tabs (up to one execution per minute). The browsers answer the WebSocket ping messages without
 * involving JavaScript timers, so these pings keep the connection alive no matter how long the browser tab has been in
 * the background.
 *
 * @version $Id$
 * @since 18.9.0RC1
 */
@Component(roles = NetfluxPingPongManager.class)
@Singleton
public class NetfluxPingPongManager implements Initializable, Disposable
{
    private static final ByteBuffer EMPTY_PING_PAYLOAD = ByteBuffer.allocate(0).asReadOnlyBuffer();

    private static final class TrackedUser
    {
        private final AtomicInteger missedPongs = new AtomicInteger();

        private final MessageHandler.Whole<PongMessage> pongHandler;

        private TrackedUser(MessageHandler.Whole<PongMessage> pongHandler)
        {
            this.pongHandler = pongHandler;
        }
    }

    @Inject
    private Logger logger;

    @Inject
    private NetfluxConfiguration configuration;

    private final Map<LocalUser, TrackedUser> trackedUsers = new ConcurrentHashMap<>();

    private ScheduledExecutorService pingScheduler;

    @Override
    public void initialize() throws InitializationException
    {
        long pingInterval = this.configuration.getPingInterval();
        if (pingInterval <= 0) {
            // Server pings are disabled. Only the client's own PING messages (throttled in background tabs) and the
            // session max idle timeout keep the connection alive.
            this.logger.debug("Netflux WebSocket server pings are disabled (ping interval is [{}]).", pingInterval);
            return;
        }

        this.pingScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Netflux WebSocket ping");
            thread.setDaemon(true);
            return thread;
        });
        this.pingScheduler.scheduleAtFixedRate(this::pingAllUsers, pingInterval, pingInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose()
    {
        if (this.pingScheduler != null) {
            this.pingScheduler.shutdownNow();
            this.pingScheduler = null;
        }
    }

    /**
     * Start sending periodic ping messages to the given user. Does nothing when server pings are disabled, see
     * {@link NetfluxConfiguration#getPingInterval()}.
     *
     * @param user the local user whose WebSocket session should be kept alive
     */
    public void startPinging(LocalUser user)
    {
        if (this.pingScheduler == null) {
            return;
        }

        this.trackedUsers.computeIfAbsent(user, u -> {
            MessageHandler.Whole<PongMessage> pongHandler = message -> {
                // Reset the missed pongs count whenever we receive a pong message.
                TrackedUser trackedUser = this.trackedUsers.get(u);
                if (trackedUser != null) {
                    trackedUser.missedPongs.set(0);
                }
            };
            u.getSession().addMessageHandler(PongMessage.class, pongHandler);
            return new TrackedUser(pongHandler);
        });
    }

    /**
     * Stop sending periodic ping messages to the given user.
     *
     * @param user the local user to stop pinging
     */
    public void stopPinging(LocalUser user)
    {
        TrackedUser trackedUser = this.trackedUsers.remove(user);
        if (trackedUser != null) {
            user.getSession().removeMessageHandler(trackedUser.pongHandler);
        }
    }

    void pingAllUsers()
    {
        this.trackedUsers.forEach((user, trackedUser) -> {
            try {
                pingUser(user, trackedUser);
            } catch (Exception e) {
                // Catch everything because an exception thrown here would cancel all the future pings.
                this.logger.warn("Failed to ping user [{}]. Cause: [{}]", user.getName(), getRootCauseMessage(e));
            }
        });
    }

    private void pingUser(LocalUser user, TrackedUser trackedUser)
    {
        Session session = user.getSession();
        if (!session.isOpen()) {
            stopPinging(user);
            return;
        }

        if (trackedUser.missedPongs.get() >= this.configuration.getPingMaxMissedPongs()) {
            closeSession(user, "Client did not respond to server ping.");
            return;
        }

        try {
            trackedUser.missedPongs.incrementAndGet();
            // Netflux sends its messages while holding the user lock, see Netflux#sendMessage(LocalUser, String). We
            // take the same lock because the WebSocket API doesn't allow concurrent writes on the same session.
            synchronized (user) {
                session.getBasicRemote().sendPing(EMPTY_PING_PAYLOAD.duplicate());
            }
        } catch (IOException e) {
            this.logger.warn("Failed to send ping message to session [{}]. Cause: [{}]", session.getId(),
                getRootCauseMessage(e));
            closeSession(user, "Failed to send ping message to the client.");
        }
    }

    private void closeSession(LocalUser user, String reasonPhrase)
    {
        stopPinging(user);
        Session session = user.getSession();
        try {
            session.close(new CloseReason(GOING_AWAY, reasonPhrase));
        } catch (IOException e) {
            this.logger.debug("Failed to close session [{}]. Cause: [{}]", session.getId(), getRootCauseMessage(e));
        }
    }
}
