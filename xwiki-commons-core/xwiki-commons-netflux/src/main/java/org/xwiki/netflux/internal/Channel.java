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

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a Netflux channel.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
public class Channel
{
    private final String key;

    private final long creationDate = System.currentTimeMillis();

    private final Map<String, User> users = new LinkedHashMap<>();

    private final Map<String, Bot> bots = new LinkedHashMap<>();

    private final Deque<String> messages = new LinkedList<>();

    /**
     * Creates a new channel with the given key.
     *
     * @param key the channel key
     */
    public Channel(String key)
    {
        this.key = key;
    }

    /**
     * @return the channel key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @return the channel creation date
     */
    public long getCreationDate()
    {
        return creationDate;
    }

    /**
     * @return the users that have connected to this channel
     */
    public Map<String, User> getUsers()
    {
        return users;
    }

    /**
     * @return the bots that have connected to this channel
     */
    public Map<String, Bot> getBots()
    {
        return this.bots;
    }

    /**
     * @return the list of users that are currently connected to this channel
     */
    public List<User> getConnectedUsers()
    {
        return this.users.values().stream().filter(user -> user.getSession() != null && user.isConnected())
            .collect(Collectors.toList());
    }

    /**
     * @return the channel messages
     */
    public Deque<String> getMessages()
    {
        return messages;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.key).append(this.creationDate).toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != getClass()) {
            return false;
        }
        Channel otherChannel = (Channel) object;
        return new EqualsBuilder().append(this.key, otherChannel.key)
            .append(this.creationDate, otherChannel.creationDate).isEquals();
    }
}
