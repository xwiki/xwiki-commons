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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.websocket.Session;

/**
 * Represents an user connected to a Netflux channel.
 * 
 * @version $Id$
 * @since 13.7RC1
 */
public class User
{
    private final Session session;

    private final String name;

    private final Queue<String> messagesToBeSent = new LinkedList<>();

    private final Set<Channel> channels = new LinkedHashSet<Channel>();

    private boolean connected = true;

    private long timeOfLastMessage = System.currentTimeMillis();

    /**
     * Creates a new user with the specified name, using the given WebSocket session.
     * 
     * @param session the WebSocket session used to communicate with the user
     * @param name the user name
     */
    public User(Session session, String name)
    {
        this.session = session;
        this.name = name;
    }

    /**
     * @return whether this user is connected or not
     */
    public boolean isConnected()
    {
        return connected;
    }

    /**
     * Sets whether this user is connected or not.
     * 
     * @param connected {@code true} if the user is connected, {@code false} otherwise
     */
    public void setConnected(boolean connected)
    {
        this.connected = connected;
    }

    /**
     * @return the time when this user sent their last message
     */
    public long getTimeOfLastMessage()
    {
        return timeOfLastMessage;
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
     * @return the WebSocket session
     */
    public Session getSession()
    {
        return session;
    }

    /**
     * @return the user name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the messages to be sent
     */
    public Queue<String> getMessagesToBeSent()
    {
        return messagesToBeSent;
    }

    /**
     * @return the channels this user is connected to
     */
    public Set<Channel> getChannels()
    {
        return channels;
    }
}
