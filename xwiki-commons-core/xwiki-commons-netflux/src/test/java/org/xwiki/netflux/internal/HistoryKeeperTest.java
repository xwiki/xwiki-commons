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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

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
    private MessageDispatcher dispatcher;

    private User user = new User(null, "alice");

    private Channel channel = new Channel("test");

    @BeforeEach
    void beforeEach()
    {
        when(this.channels.get("test")).thenReturn(this.channel);
    }

    @Test
    void getId()
    {
        assertEquals(16, this.historyKeeper.getId().length());
    }

    @Test
    void onUserMessage()
    {
        // Message size too small.
        this.historyKeeper.onUserMessage(this.user, List.of(0, 1, 2));

        // Invalid message.
        when(this.dispatcher.decode("invalid")).thenThrow(new RuntimeException());
        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "invalid"));

        // Unknown message type.
        when(this.dispatcher.decode("valid")).thenReturn(List.of("GET_TIME", "test"));
        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "valid"));

        // Get history message.
        when(this.dispatcher.decode("valid")).thenReturn(List.of("GET_HISTORY", "test"));
        this.channel.getMessages().addAll(List.of("first", "second"));
        when(this.dispatcher.buildMessage(0, this.historyKeeper.getId(), this.user.getName(),
            "{\"state\":1, \"channel\":\"test\"}")).thenReturn("end");

        this.historyKeeper.onUserMessage(this.user, List.of(0, "MSG", this.historyKeeper.getId(), "valid"));

        verify(this.dispatcher).addMessage(this.user, "first");
        verify(this.dispatcher).addMessage(this.user, "second");
        verify(this.dispatcher).addMessage(this.user, "end");
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
        when(this.dispatcher.decode("checkpoint")).thenReturn(List.of(1, "cp|[4,[test"));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "checkpoint");

        // Add another message that looks like a checkpoint message but it's not.
        when(this.dispatcher.decode("fake checkpoint")).thenReturn(List.of("cp|[4,[test", 1));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "fake checkpoint");

        assertEquals(List.of("Hi!", "Anyone here?", "checkpoint", "fake checkpoint"), this.channel.getMessages());

        // Simulate another checkpoint message to test message pruning.
        when(this.dispatcher.decode("another checkpoint")).thenReturn(List.of("cp|[4,[end"));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "another checkpoint");

        assertEquals(List.of("checkpoint", "fake checkpoint", "another checkpoint"), this.channel.getMessages());

        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "I'm alone");
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "Bye!");

        // Simulate a final checkpoint message to test message pruning.
        when(this.dispatcher.decode("final checkpoint")).thenReturn(List.of(1, "foo", "cp|[4,[final"));
        this.historyKeeper.onChannelMessage(this.channel, this.user, "MSG", "final checkpoint");

        assertEquals(List.of("another checkpoint", "I'm alone", "Bye!", "final checkpoint"),
            this.channel.getMessages());
    }
}
