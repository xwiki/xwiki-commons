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

import jakarta.websocket.CloseReason;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.PongMessage;
import jakarta.websocket.RemoteEndpoint.Basic;
import jakarta.websocket.Session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static jakarta.websocket.CloseReason.CloseCodes.GOING_AWAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NetfluxPingPongManager}.
 *
 * @version $Id$
 */
@ComponentTest
class NetfluxPingPongManagerTest
{
    @InjectMockComponents
    private NetfluxPingPongManager pingPongManager;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @MockComponent
    private NetfluxConfiguration configuration;

    @Captor
    private ArgumentCaptor<MessageHandler.Whole<PongMessage>> pongHandlerCaptor;

    @Captor
    private ArgumentCaptor<CloseReason> closeReasonCaptor;

    @BeforeComponent
    void beforeComponent()
    {
        // Use a large ping interval to avoid automatic pings during the test. We trigger the pings manually.
        when(this.configuration.getPingInterval()).thenReturn(60_000L);
    }

    @Test
    void startPingStop() throws Exception
    {
        when(this.configuration.getPingMaxMissedPongs()).thenReturn(1);

        LocalUser alice = mockUser("alice");
        Session aliceSession = alice.getSession();
        Basic aliceBasicRemote = aliceSession.getBasicRemote();

        LocalUser bob = mockUser("bob");
        Session bobSession = bob.getSession();
        Basic bobBasicRemote = bobSession.getBasicRemote();

        //
        // Ping when no user is tracked.
        //

        this.pingPongManager.pingAllUsers();

        verify(aliceBasicRemote, never()).sendPing(any());
        verify(bobBasicRemote, never()).sendPing(any());

        //
        // Start tracking Alice and ping.
        //

        this.pingPongManager.startPinging(alice);
        verify(aliceSession).addMessageHandler(eq(PongMessage.class), this.pongHandlerCaptor.capture());
        MessageHandler.Whole<PongMessage> alicePongHandler = this.pongHandlerCaptor.getValue();

        // Starting to ping the same user again has no effect.
        this.pingPongManager.startPinging(alice);
        verify(aliceSession).addMessageHandler(eq(PongMessage.class), any(MessageHandler.Whole.class));

        this.pingPongManager.pingAllUsers();

        verify(aliceBasicRemote).sendPing(any());
        verify(bobBasicRemote, never()).sendPing(any());

        // Alice answers, which resets her missed pongs count.
        alicePongHandler.onMessage(mock(PongMessage.class));

        //
        // Start tracking Bob and ping.
        //

        this.pingPongManager.startPinging(bob);
        verify(bobSession).addMessageHandler(eq(PongMessage.class), this.pongHandlerCaptor.capture());
        MessageHandler.Whole<PongMessage> bobPongHandler = this.pongHandlerCaptor.getAllValues().get(1);

        this.pingPongManager.pingAllUsers();
        verify(aliceBasicRemote, times(2)).sendPing(any());
        verify(bobBasicRemote).sendPing(any());

        alicePongHandler.onMessage(mock(PongMessage.class));

        // Bob didn't answer so his session is closed because he reached the maximum number of missed pongs.
        this.pingPongManager.pingAllUsers();
        verify(aliceBasicRemote, times(3)).sendPing(any());
        verify(bobBasicRemote).sendPing(any());
        verify(bobSession).removeMessageHandler(bobPongHandler);
        verify(bobSession).close(this.closeReasonCaptor.capture());
        assertEquals(GOING_AWAY, this.closeReasonCaptor.getValue().getCloseCode());
        assertEquals("Client did not respond to server ping.", this.closeReasonCaptor.getValue().getReasonPhrase());

        alicePongHandler.onMessage(mock(PongMessage.class));

        // A late pong from Bob is ignored.
        bobPongHandler.onMessage(mock(PongMessage.class));

        this.pingPongManager.pingAllUsers();
        verify(aliceBasicRemote, times(4)).sendPing(any());
        verify(bobBasicRemote).sendPing(any());

        alicePongHandler.onMessage(mock(PongMessage.class));

        //
        // Bob joins back.
        //

        this.pingPongManager.startPinging(bob);
        verify(bobSession, times(2)).addMessageHandler(eq(PongMessage.class), this.pongHandlerCaptor.capture());
        bobPongHandler = this.pongHandlerCaptor.getAllValues().get(3);

        // Failing to send the ping to Alice closes her session.
        doThrow(new IOException("Ping failed")).when(aliceBasicRemote).sendPing(any());
        this.pingPongManager.pingAllUsers();
        verify(bobBasicRemote, times(2)).sendPing(any());

        assertEquals(1, this.logCapture.size());
        assertEquals("Failed to send ping message to session [alice-session]. Cause: [IOException: Ping failed]",
            this.logCapture.getMessage(0));
        verify(aliceSession).removeMessageHandler(alicePongHandler);
        verify(aliceSession).close(this.closeReasonCaptor.capture());
        assertEquals(GOING_AWAY, this.closeReasonCaptor.getValue().getCloseCode());
        assertEquals("Failed to send ping message to the client.", this.closeReasonCaptor.getValue().getReasonPhrase());

        bobPongHandler.onMessage(mock(PongMessage.class));

        //
        // Bob's session gets closed.
        //

        when(bobSession.isOpen()).thenReturn(false);
        this.pingPongManager.pingAllUsers();
        verify(aliceBasicRemote, times(5)).sendPing(any());
        verify(bobBasicRemote, times(2)).sendPing(any());
        verify(bobSession).removeMessageHandler(bobPongHandler);
        verify(bobSession).close(any());

        //
        // Alice joins back and leaves.
        //

        this.pingPongManager.startPinging(alice);
        verify(aliceSession, times(2)).addMessageHandler(eq(PongMessage.class), this.pongHandlerCaptor.capture());
        alicePongHandler = this.pongHandlerCaptor.getAllValues().get(4);

        this.pingPongManager.stopPinging(alice);
        verify(aliceSession).removeMessageHandler(alicePongHandler);
        verify(aliceSession).close(any());

        // Stopping to ping a user that is not tracked has no effect.
        this.pingPongManager.stopPinging(alice);
        verify(aliceSession, times(2)).removeMessageHandler(any());

        //
        // No user is tracked, ping does nothing.
        //

        this.pingPongManager.pingAllUsers();
        verify(aliceBasicRemote, times(5)).sendPing(any());
        verify(bobBasicRemote, times(2)).sendPing(any());
    }

    @Test
    void pingAllUsersWhenPingThrowsUnexpectedException() throws Exception
    {
        when(this.configuration.getPingMaxMissedPongs()).thenReturn(3);

        LocalUser alice = mockUser("alice");
        Basic aliceBasicRemote = alice.getSession().getBasicRemote();
        doThrow(new IllegalStateException("Concurrent write")).when(aliceBasicRemote).sendPing(any());
        LocalUser bob = mockUser("bob");

        this.pingPongManager.startPinging(alice);
        this.pingPongManager.startPinging(bob);

        this.pingPongManager.pingAllUsers();

        // The unexpected exception is logged and doesn't prevent pinging the other users.
        verify(bob.getSession().getBasicRemote()).sendPing(any());
        verify(alice.getSession(), never()).close(any());
        assertEquals(1, this.logCapture.size());
        assertEquals("Failed to ping user [alice]. Cause: [IllegalStateException: Concurrent write]",
            this.logCapture.getMessage(0));
    }

    @Test
    void closeSessionFails() throws Exception
    {
        when(this.configuration.getPingMaxMissedPongs()).thenReturn(0);

        LocalUser alice = mockUser("alice");
        Session aliceSession = alice.getSession();
        doThrow(new IOException("Close failed")).when(aliceSession).close(any());

        this.pingPongManager.startPinging(alice);
        this.pingPongManager.pingAllUsers();

        verify(alice.getSession().getBasicRemote(), never()).sendPing(any());
        verify(alice.getSession()).close(any());

        // The user is not tracked anymore.
        this.pingPongManager.pingAllUsers();
        verify(alice.getSession()).close(any());
    }

    @Test
    void pingsDisabled() throws Exception
    {
        when(this.configuration.getPingInterval()).thenReturn(0L);
        this.pingPongManager.dispose();
        this.pingPongManager.initialize();

        LocalUser alice = mockUser("alice");
        this.pingPongManager.startPinging(alice);
        this.pingPongManager.pingAllUsers();
        this.pingPongManager.stopPinging(alice);

        verify(alice.getSession(), never()).addMessageHandler(eq(PongMessage.class), any(MessageHandler.Whole.class));
        verify(alice.getSession().getBasicRemote(), never()).sendPing(any());
        verify(alice.getSession(), never()).removeMessageHandler(any());
    }

    @Test
    void dispose()
    {
        this.pingPongManager.dispose();
        // Disposing twice has no effect.
        this.pingPongManager.dispose();
    }

    private LocalUser mockUser(String name)
    {
        Session session = mock(Session.class, name + "-session");
        when(session.getId()).thenReturn(name + "-session");
        when(session.isOpen()).thenReturn(true);
        Basic basicRemote = mock(Basic.class, name + "-basic-remote");
        when(session.getBasicRemote()).thenReturn(basicRemote);
        return new LocalUser(session, name);
    }
}
