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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.Session;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Channel}.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
class ChannelTest
{
    @Test
    void getConnectedUsers()
    {
        long before = new Date().getTime();
        Channel channel = new Channel();
        long after = new Date().getTime();

        assertTrue(before <= channel.getCreationDate());
        assertTrue(channel.getCreationDate() <= after);

        assertEquals(48, channel.getKey().length());
        assertTrue(channel.getMessages().isEmpty());

        assertEquals(0, channel.getUsers().size());
        assertEquals(0, channel.getConnectedUsers().size());

        Session session = mock(Session.class);

        channel.getUsers().put("null", null);
        channel.getUsers().put("alice", new User(null, "alice"));
        channel.getUsers().put("bob", new User(session, "bob"));

        User carol = new User(session, "carol");
        carol.setConnected(false);
        channel.getUsers().put("carol", carol);

        assertEquals(4, channel.getUsers().size());
        assertEquals(1, channel.getConnectedUsers().size());
        assertEquals("bob", channel.getConnectedUsers().iterator().next().getName());
    }

    @Test
    void hashCodeEquals()
    {
        Channel alice = new Channel();
        Channel bob = new Channel();

        assertEquals(alice, alice);
        assertNotEquals(alice, bob);

        Set<Channel> channels = new HashSet<>();
        channels.add(alice);
        channels.add(bob);
        channels.add(alice);
        channels.add(bob);
        assertEquals(2, channels.size());
    }
}
