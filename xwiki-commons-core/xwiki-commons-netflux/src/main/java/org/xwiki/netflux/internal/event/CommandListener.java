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
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.netflux.internal.Channel;
import org.xwiki.netflux.internal.ChannelStore;
import org.xwiki.netflux.internal.LocalUser;
import org.xwiki.netflux.internal.Netflux;
import org.xwiki.netflux.internal.User;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

/**
 * Dispatch messages to local users.
 * 
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component
@Named(CommandListener.NAME)
@Singleton
public class CommandListener extends AbstractEventListener
{
    /**
     * The unique identifier of the listener.
     */
    public static final String NAME = "org.xwiki.internal.document.DocumentOverrideListener";

    @Inject
    protected ChannelStore channelStore;

    @Inject
    protected Netflux netflux;

    /**
     * The default constructor.
     */
    public CommandListener()
    {
        super(NAME, new NetfluxUserLeftEvent(), new NetfluxUserJoinedEvent(), new NetfluxPingUserEvent(),
            new NetfluxMessageUserEvent(), new NetfluxCommandChannelEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof NetfluxUserLeftEvent leaveCommand) {
            userLeft(leaveCommand);
        } else if (event instanceof NetfluxUserJoinedEvent joinCommand) {
            userJoined(joinCommand, (User) source);
        } else if (event instanceof NetfluxPingUserEvent pingEvent) {
            userPing(pingEvent);
        } else if (event instanceof NetfluxMessageUserEvent messageEvent) {
            userMessage(messageEvent);
        } else if (event instanceof NetfluxCommandChannelEvent messageEvent) {
            channelMessage(messageEvent);
        }
    }

    protected void channelMessage(NetfluxCommandChannelEvent event)
    {
        Channel channel = this.channelStore.get(event.getChannel());

        if (channel != null) {
            for (User user : channel.getUsers().values()) {
                if (user instanceof LocalUser localUser && !user.getName().equals(event.getSender())) {
                    this.netflux.sendMessage(localUser, event.getMessage());
                }
            }
        }
    }

    protected void userMessage(NetfluxMessageUserEvent event)
    {
        User user = this.netflux.getUser(event.getUser());

        if (user instanceof LocalUser localUser) {
            this.netflux.sendMessage(localUser, event.getMessage());
        }
    }

    protected void userPing(NetfluxPingUserEvent event)
    {
        User user = this.netflux.getUser(event.getUser());

        if (user instanceof LocalUser localUser) {
            this.netflux.pingUser(localUser, event.getSequence());
        }
    }

    protected void userJoined(NetfluxUserJoinedEvent event, User user)
    {
        // The channel might not exist yet (for example in the case of a cluster), so we make sure it's created when it
        // does not exist yet
        Channel channel = this.channelStore.get(event.getChannel(), true);

        this.netflux.userJoinedChannel(user, channel);
    }

    protected void userLeft(NetfluxUserLeftEvent event)
    {
        User user = this.netflux.getUser(event.getUser());
        Channel channel = this.channelStore.get(event.getChannel());

        if (user != null && channel != null) {
            this.netflux.userLeftChannel(user, channel, event.getReason());
        }
    }
}
