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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an user connected to a Netflux channel.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
public class User
{
    private final String name;

    private final Set<Channel> channels = new LinkedHashSet<>();

    private long timeOfLastMessage = System.currentTimeMillis();

    /**
     * Creates a new user with the specified identifier.
     * 
     * @param name the identifier of the user
     */
    public User(String name)
    {
        this.name = name;
    }

    /**
     * @return the time when this user sent their last message
     */
    public long getTimeOfLastMessage()
    {
        return this.timeOfLastMessage;
    }

    /**
     * Sets the time when this user sent their last message.
     * 
     * @param timeOfLastMessage the time when the last message was sent by this user
     */
    public void setTimeOfLastMessage(long timeOfLastMessage)
    {
        this.timeOfLastMessage = timeOfLastMessage;
    }

    /**
     * @return the user name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the channels this user is connected to
     */
    public Set<Channel> getChannels()
    {
        return this.channels;
    }
}
