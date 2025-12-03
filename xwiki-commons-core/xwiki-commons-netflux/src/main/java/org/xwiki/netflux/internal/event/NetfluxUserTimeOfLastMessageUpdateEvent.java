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
 * An event triggered when the time when the last message was sent by this user is updated.
 * 
 * @version $Id$
 * @since 17.10.1
 * @since 18.0.0RC1
 */
public class NetfluxUserTimeOfLastMessageUpdateEvent implements Event, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String user;

    private final Long timeOfLastMessage;

    /**
     * @param user the identifier of the user
     * @param timeOfLastMessage the time when the last message was sent by this user
     */
    public NetfluxUserTimeOfLastMessageUpdateEvent(String user, long timeOfLastMessage)
    {
        this.user = user;
        this.timeOfLastMessage = timeOfLastMessage;
    }

    /**
     * Listen to all events.
     */
    public NetfluxUserTimeOfLastMessageUpdateEvent()
    {
        this.user = null;
        this.timeOfLastMessage = null;
    }

    /**
     * @return the identifier of the user
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * @return the time when the last message was sent by this user
     */
    public Long getTimeOfLastMessage()
    {
        return this.timeOfLastMessage;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof NetfluxUserTimeOfLastMessageUpdateEvent;
    }
}
