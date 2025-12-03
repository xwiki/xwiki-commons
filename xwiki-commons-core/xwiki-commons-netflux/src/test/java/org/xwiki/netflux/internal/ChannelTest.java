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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.websocket.Session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link Channel}.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
class ChannelTest
{
    @Test
    void getUsers()
    {
        long before = new Date().getTime();
        Channel channel = new Channel("test");
        long after = new Date().getTime();

        assertTrue(before <= channel.getCreationDate());
        assertTrue(channel.getCreationDate() <= after);

        assertTrue(channel.getMessages().isEmpty());

        assertEquals(0, channel.getUsers().size());


        Session bobSession = mock(Session.class);
        User bob = new LocalUser(bobSession, "bob");
        channel.getUsers().put(bob.getName(), bob);

        Session carolSession = mock(Session.class);
        User carol = new LocalUser(carolSession, "carol");
        channel.getUsers().put(carol.getName(), carol);

        assertEquals(2, channel.getUsers().size());
        assertEquals("bob", channel.getUsers().values().iterator().next().getName());
    }

    @Test
    void hashCodeEquals() throws Exception
    {
        Channel alice = new Channel("test");
        // Make sure the channel creation date is different.
        Thread.sleep(1);
        Channel bob = new Channel("test");

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
