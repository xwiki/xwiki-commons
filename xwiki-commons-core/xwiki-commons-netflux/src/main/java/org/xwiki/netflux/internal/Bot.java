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

import org.xwiki.component.annotation.Role;

/**
 * Represents a fake user that joins a {@link Channel} to perform a specific task.
 *
 * @version $Id$
 * @since 15.10.11
 * @since 16.4.1
 * @since 16.5.0RC1
 */
@Role
public interface Bot
{
    /**
     * @return the hex string that identifies this fake user
     */
    String getId();

    /**
     * Called to ask a bot if it accepts to join a channel. The bots accept to join all channels by default. Use this
     * method to filter the channels that the bot should join.
     *
     * @param channel the channel the bot is asked to join
     * @return {@code true} if the bot accepts to join the channel, {@code false} otherwise
     */
    default boolean onJoinChannel(Channel channel)
    {
        return true;
    }

    /**
     * Called when a direct message is received from a user.
     *
     * @param sender the user who sent the message
     * @param message the message that was sent
     * @since 17.10.1
     * @since 18.0.0RC1
     */
    default void onUserMessage(LocalUser sender, List<Object> message)
    {
        // Do nothing by default
    }

    /**
     * Called when a message is sent to a channel where this bot is present.
     *
     * @param channel the channel where the message was sent
     * @param sender the user who sent the message
     * @param messageType the type of message that was sent
     * @param message the actual message that was sent
     */
    default void onChannelMessage(Channel channel, User sender, String messageType, String message)
    {
        // Do nothing by default
    }

    /**
     * Called when the bot is removed from a channel. Use this method to clean up any resources that were allocated when
     * the bot joined the channel.
     *
     * @param channel the channel the bot was removed from
     */
    default void onLeaveChannel(Channel channel)
    {
        // Do nothing by default
    }
}
