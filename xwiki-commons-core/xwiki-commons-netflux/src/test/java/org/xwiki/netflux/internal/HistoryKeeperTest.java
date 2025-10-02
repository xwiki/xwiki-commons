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

import java.util.List;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HistoryKeeper}.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList(IdGenerator.class)
class HistoryKeeperTest
{
    @InjectMockComponents
    private HistoryKeeper historyKeeper;

    @MockComponent
    private ChannelStore channels;

    @MockComponent
    private MessageBuilder messageBuilder;

    @Mock
    Session session;

    @Mock
    Basic basicRemote;

    private User user;

    private Channel channel = new Channel("test");

    @BeforeEach
    void beforeEach()
    {
        when(this.session.getBasicRemote()).thenReturn(this.basicRemote);
        this.user = new User(this.session, "alice");
        when(this.channels.get("test")).thenReturn(this.channel);
    }

    @Test
    void getId()
    {
        assertEquals(16, this.historyKeeper.getId().length());
    }

    @Test
    void onUserMessage() throws Exception
    {
        // Message size too small.
        this.historyKeeper.onUserMessage(this.user, List.of(0, 1, 2));

        // Invalid message.
        when(this.messageBuilder.decode("invalid")).thenThrow(new RuntimeException());
        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "invalid"));

        // Unknown message type.
        when(this.messageBuilder.decode("valid")).thenReturn(List.of("GET_TIME", "test"));
        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "valid"));

        // Get history without specifying the channel.
        when(this.messageBuilder.decode("valid")).thenReturn(List.of("GET_HISTORY"));
        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "valid"));

        // Get history for missing channel.
        when(this.messageBuilder.decode("valid")).thenReturn(List.of("GET_HISTORY", "missing"));
        when(this.messageBuilder.buildMessage(0, this.historyKeeper.getId(), this.user.getName(),
            "{\"state\":1, \"channel\":\"missing\"}")).thenReturn("end missing");
        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "valid"));
        verify(this.basicRemote).sendText("end missing");

        // Get history message.
        when(this.messageBuilder.decode("valid")).thenReturn(List.of("GET_HISTORY", "test"));
        this.channel.getMessages().addAll(List.of("first", "second"));
        when(this.messageBuilder.buildMessage(0, this.historyKeeper.getId(), this.user.getName(),
            "{\"state\":1, \"channel\":\"test\"}")).thenReturn("end");

        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "valid"));

        verify(this.basicRemote).sendText("first");
        verify(this.basicRemote).sendText("second");
        verify(this.basicRemote).sendText("end");
    }

    @Test
    void onChannelMessage()
    {
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "Hi!");
        // Only "MSG" messages are kept in the channel history. So this message should be ignored.
        this.historyKeeper.onChannelMessage(this.channel, this.user, "JOIN", "Bob joined");
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "Anyone here?");

        assertEquals(List.of("Hi!", "Anyone here?"), this.channel.getMessages());

        // Simulate a checkpoint message.
        when(this.messageBuilder.decode("checkpoint")).thenReturn(List.of(1, "cp|[4,[test"));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "checkpoint");

        // Add another message that looks like a checkpoint message but it's not.
        when(this.messageBuilder.decode("fake checkpoint")).thenReturn(List.of("cp|[4,[test", 1));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "fake checkpoint");

        assertEquals(List.of("Hi!", "Anyone here?", "checkpoint", "fake checkpoint"), this.channel.getMessages());

        // Simulate another checkpoint message to test message pruning.
        when(this.messageBuilder.decode("another checkpoint")).thenReturn(List.of("cp|[4,[end"));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "another checkpoint");

        assertEquals(List.of("checkpoint", "fake checkpoint", "another checkpoint"), this.channel.getMessages());

        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "I'm alone");
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "Bye!");

        // Simulate a final checkpoint message to test message pruning.
        when(this.messageBuilder.decode("final checkpoint")).thenReturn(List.of(1, "foo", "cp|[4,[final"));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "final checkpoint");

        assertEquals(List.of("another checkpoint", "I'm alone", "Bye!", "final checkpoint"),
            this.channel.getMessages());
    }
}
