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

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.netflux.internal.Channel;
import org.xwiki.netflux.internal.ChannelStore;
import org.xwiki.netflux.internal.MessageBuilder;
import org.xwiki.netflux.internal.Netflux;
import org.xwiki.netflux.internal.User;
import org.xwiki.netflux.internal.user.local.LocalUser;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

/**
 * Dispatch messages to local users.
 * 
 * @version $Id$
 * @since 17.10.0RC1
 */
public class ChannelMessageListener extends AbstractEventListener
{
    /**
     * The unique identifier of the listener.
     */
    public static final String NAME = "org.xwiki.internal.document.DocumentOverrideListener";

    @Inject
    private ChannelStore channelStore;

    @Inject
    private Provider<Netflux> netfluxProvider;

    @Inject
    private Logger logger;

    /**
     * The default constructor.
     */
    public ChannelMessageListener()
    {
        super(NAME, new SendChannelMessageEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof SendChannelMessageEvent messageEvent) {
            Channel channel = this.channelStore.get(messageEvent.getChannel());

            if (channel != null) {
                User sender = (User) source;
                String message = (String) data;

                // Broadcast the message to all the bots connected to the channel.
                channel.getBots().values()
                    .forEach(bot -> bot.onChannelMessage(channel, sender, messageEvent.getCommand(), message));

                // Broadcast the message to all the users connected to the channel.
                channel.getUsers().values().stream()
                    .filter(user -> user instanceof LocalUser
                        && !(MessageBuilder.COMMAND_MSG.equals(messageEvent.getCommand()) && user != sender))
                    .forEach(user -> this.netfluxProvider.get().sendMessage((LocalUser) user, message));
            } else {
                this.logger.warn(
                    "Failed to find a netflux channel with identifier [{}]. The command [{}] will be ignored.",
                    messageEvent.getChannel(), messageEvent.getCommand());
            }
        }
    }
}
