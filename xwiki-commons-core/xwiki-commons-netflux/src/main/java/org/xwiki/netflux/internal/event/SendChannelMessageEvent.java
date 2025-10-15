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

import org.xwiki.netflux.internal.User;
import org.xwiki.observation.event.Event;

/**
 * An event triggered to indicate listeners to send a command to the indicated channel.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the {@link User} sending the command.</li>
 * <li>data: the message content, as a {@link String}</li>
 * </ul>
 * 
 * @version $Id$
 * @since 17.10.0RC1
 */
public class SendChannelMessageEvent implements Event, Serializable
{
    private static final long serialVersionUID = 1L;

    private String command;

    private String channel;

    /**
     * @param command the command name
     * @param channel the channel identifier
     */
    public SendChannelMessageEvent(String command, String channel)
    {
        this.command = command;
        this.channel = channel;
    }

    /**
     * Listen to all events.
     */
    public SendChannelMessageEvent()
    {
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

    @Override
    public boolean matches(Object otherEvent)
    {
        if (otherEvent instanceof SendChannelMessageEvent messageEvent) {
            return (this.command == null || this.command.equals(messageEvent.getCommand()))
                && (this.channel == null || this.channel.equals(messageEvent.getChannel()));
        }

        return false;
    }
}
