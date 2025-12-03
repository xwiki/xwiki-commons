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
package org.xwiki.netflux.internal.event;

import java.io.Serializable;

import org.xwiki.observation.event.Event;

/**
 * An event triggered to indicate listeners to send a command to the indicated channel.
 * 
 * @version $Id$
 * @since 17.10.1
 * @since 18.0.0RC1
 */
public class NetfluxCommandChannelEvent implements Event, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String command;

    private final String channel;

    private final String sender;

    private final String message;

    /**
     * @param command the command name
     * @param channel the channel identifier
     * @param user the user who sent the message
     * @param message the message content
     */
    public NetfluxCommandChannelEvent(String command, String channel, String user, String message)
    {
        this.command = command;
        this.channel = channel;
        this.sender = user;
        this.message = message;
    }

    /**
     * Listen to all events.
     */
    public NetfluxCommandChannelEvent()
    {
        this(null, null, null, null);
    }

    /**
     * @return the command name
     */
    public String getCommand()
    {
        return this.command;
    }

    /**
     * @return the channel identifier
     */
    public String getChannel()
    {
        return this.channel;
    }

    /**
     * @return the user who sent the command
     */
    public String getSender()
    {
        return this.sender;
    }

    /**
     * @return the message content
     */
    public String getMessage()
    {
        return this.message;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof NetfluxCommandChannelEvent;
    }
}
