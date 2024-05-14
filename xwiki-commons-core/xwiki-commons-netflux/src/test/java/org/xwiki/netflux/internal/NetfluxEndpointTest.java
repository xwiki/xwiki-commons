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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.websocket.AbstractPartialStringMessageHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NetfluxEndpoint}.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
@ComponentTest
@ComponentList(ChannelStore.class)
class NetfluxEndpointTest
{
    @InjectMockComponents
    private NetfluxEndpoint endPoint;

    @MockComponent
    private HistoryKeeper historyKeeper;

    private final JsonConverter jsonConverter = new JsonConverter();

    @Captor
    private ArgumentCaptor<AbstractPartialStringMessageHandler> messageHandlerCaptor;

    @Test
    void joinMessagePingLeave(MockitoComponentManager componentManager) throws Exception
    {
        when(this.historyKeeper.getKey()).thenReturn("historyKeeper");

        // Alice opens a new session.
        Session aliceSession = mockSession("alice");
        this.endPoint.onOpen(aliceSession, null);

        verify(aliceSession).addMessageHandler(this.messageHandlerCaptor.capture());
        AbstractPartialStringMessageHandler aliceMessageHandler = this.messageHandlerCaptor.getValue();

        // Bob opens a new session.
        Session bobSession = mockSession("bob");
        this.endPoint.onOpen(bobSession, null);

        verify(bobSession).addMessageHandler(this.messageHandlerCaptor.capture());
        AbstractPartialStringMessageHandler bobMessageHandler = this.messageHandlerCaptor.getValue();

        // Create the first channel.
        ChannelStore channelStore = componentManager.getInstance(ChannelStore.class);
        Channel firstChannel = channelStore.create();

        // Alice joins the first channel.
        aliceMessageHandler.onMessage(this.jsonConverter.encode(Arrays.asList(1, "JOIN", firstChannel.getKey())));

        assertEquals(1, firstChannel.getConnectedUsers().size());
        String aliceId = firstChannel.getConnectedUsers().get(0).getName();

        // Bob joins the first channel.
        bobMessageHandler.onMessage(this.jsonConverter.encode(Arrays.asList(1, "JOIN", firstChannel.getKey())));

        assertEquals(2, firstChannel.getConnectedUsers().size());
        String bobId = firstChannel.getConnectedUsers().get(1).getName();

        // Alice sends a message to the channel.
        aliceMessageHandler
            .onMessage(this.jsonConverter.encode(Arrays.asList(2, "MSG", firstChannel.getKey(), "Hi channel!")));

        // Bob sends a message to the user.
        bobMessageHandler.onMessage(this.jsonConverter.encode(Arrays.asList(2, "MSG", aliceId, "Hi Alice!")));

        // Alice sends a ping message.
        aliceMessageHandler.onMessage(this.jsonConverter.encode(Arrays.asList(3, "PING")));

        // Create the second channel.
        Channel secondChannel = channelStore.create();

        // Both users join the second channel.
        bobMessageHandler.onMessage(this.jsonConverter.encode(Arrays.asList(3, "JOIN", secondChannel.getKey())));
        aliceMessageHandler.onMessage(this.jsonConverter.encode(Arrays.asList(4, "JOIN", secondChannel.getKey())));
        assertEquals(2, secondChannel.getConnectedUsers().size());

        User alice = (User) aliceSession.getUserProperties().get("netflux.user");
        assertEquals(Set.of(firstChannel.getKey(), secondChannel.getKey()),
            alice.getChannels().stream().map(Channel::getKey).collect(Collectors.toSet()));

        User bob = (User) bobSession.getUserProperties().get("netflux.user");
        assertEquals(Set.of(firstChannel.getKey(), secondChannel.getKey()),
            bob.getChannels().stream().map(Channel::getKey).collect(Collectors.toSet()));

        // Bob leaves the first channel.
        bobMessageHandler.onMessage(this.jsonConverter.encode(Arrays.asList(4, "LEAVE", firstChannel.getKey())));
        assertEquals(1, firstChannel.getConnectedUsers().size());
        assertEquals(2, secondChannel.getConnectedUsers().size());

        // Close both sessions.
        this.endPoint.onClose(bobSession, null);
        this.endPoint.onError(aliceSession, null);

        assertEquals(0, firstChannel.getConnectedUsers().size());
        assertEquals(0, secondChannel.getConnectedUsers().size());

        assertEquals(0, alice.getChannels().size());
        assertEquals(0, bob.getChannels().size());

        //
        // Verify the messages received by each user.
        //

        ArgumentCaptor<String> aliceMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(aliceSession.getBasicRemote(), times(13)).sendText(aliceMessageCaptor.capture());
        assertEquals(Arrays.asList(
            // Alice receives an identity.
            "[0,\"\",\"IDENT\",\"" + aliceId + "\"]",
            // Join acknowledgement on the first channel.
            "[1,\"JACK\",\"" + firstChannel.getKey() + "\"]",
            // History keeper is in the first channel.
            "[0,\"" + this.historyKeeper.getKey() + "\",\"JOIN\",\"" + firstChannel.getKey() + "\"]",
            // Alice joined the first channel.
            "[0,\"" + aliceId + "\",\"JOIN\",\"" + firstChannel.getKey() + "\"]",
            // Bob joined the channel.
            "[0,\"" + bobId + "\",\"JOIN\",\"" + firstChannel.getKey() + "\"]",
            // Channel message acknowledgement.
            "[2,\"ACK\"]",
            // Private message received from Bob.
            "[0,\"" + bobId + "\",\"MSG\",\"" + aliceId + "\",\"Hi Alice!\"]",
            // Ping acknowledgement.
            "[3,\"ACK\"]",
            // Join acknowledgement on the second channel.
            "[4,\"JACK\",\"" + secondChannel.getKey() + "\"]",
            // History keeper is in the second channel.
            "[0,\"" + this.historyKeeper.getKey() + "\",\"JOIN\",\"" + secondChannel.getKey() + "\"]",
            // Bob joined the second channel.
            "[0,\"" + bobId + "\",\"JOIN\",\"" + secondChannel.getKey() + "\"]",
            // Alice joined the second channel.
            "[0,\"" + aliceId + "\",\"JOIN\",\"" + secondChannel.getKey() + "\"]",
            // Bob left the first channel.
            "[0,\"" + bobId + "\",\"LEAVE\",\"" + firstChannel.getKey() + "\",\"\"]"), aliceMessageCaptor.getAllValues());

        ArgumentCaptor<String> bobMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(bobSession.getBasicRemote(), times(12)).sendText(bobMessageCaptor.capture());
        assertEquals(Arrays.asList(
            // Bob receives an identity.
            "[0,\"\",\"IDENT\",\"" + bobId + "\"]",
            // Join acknowledgement on the first channel.
            "[1,\"JACK\",\"" + firstChannel.getKey() + "\"]",
            // History keeper is in the first channel.
            "[0,\"" + this.historyKeeper.getKey() + "\",\"JOIN\",\"" + firstChannel.getKey() + "\"]",
            // Alice joined the first channel.
            "[0,\"" + aliceId + "\",\"JOIN\",\"" + firstChannel.getKey() + "\"]",
            // Bob joined the first channel.
            "[0,\"" + bobId + "\",\"JOIN\",\"" + firstChannel.getKey() + "\"]",
            // Received channel message from Alice.
            "[0,\"" + aliceId + "\",\"MSG\",\"" + firstChannel.getKey() + "\",\"Hi channel!\"]",
            // Private message acknowledgement.
            "[2,\"ACK\"]",
            // Join acknowledgement on the second channel.
            "[3,\"JACK\",\"" + secondChannel.getKey() + "\"]",
            // History keeper is in the second channel.
            "[0,\"" + this.historyKeeper.getKey() + "\",\"JOIN\",\"" + secondChannel.getKey() + "\"]",
            // Bob joined the second channel.
            "[0,\"" + bobId + "\",\"JOIN\",\"" + secondChannel.getKey() + "\"]",
            // Alice joined the second channel.
            "[0,\"" + aliceId + "\",\"JOIN\",\"" + secondChannel.getKey() + "\"]",
            // Leave acknowledgement.
            "[4,\"ACK\"]"), bobMessageCaptor.getAllValues());
    }

    private Session mockSession(String name)
    {
        Session session = mock(Session.class, name);
        when(session.getUserProperties()).thenReturn(new HashMap<>());
        Basic basicRemote = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basicRemote);
        return session;
    }
}
