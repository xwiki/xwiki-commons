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
 * An event triggered when a user is leaving a channel.
 * 
 * @version $Id$
 * @since 17.10.1
 * @since 18.0.0RC1
 */
public class NetfluxUserLeaveEvent implements Event, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String user;

    private final String channel;

    private final String reason;

    /**
     * @param userId the identifier of the user
     * @param channelKey the identifier of the channel
     * @param reason the reason why the user left
     */
    public NetfluxUserLeaveEvent(String userId, String channelKey, String reason)
    {
        this.user = userId;
        this.channel = channelKey;
        this.reason = reason;
    }

    /**
     * Listen to all events.
     */
    public NetfluxUserLeaveEvent()
    {
        this(null, null, null);
    }

    /**
     * @return the identifier of the user
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * @return the identifier of the channel
     */
    public String getChannel()
    {
        return this.channel;
    }

    /**
     * @return the reason why the user left
     */
    public String getReason()
    {
        return this.reason;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof NetfluxUserLeaveEvent;
    }
}
