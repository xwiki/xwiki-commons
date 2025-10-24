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
 * An event triggered to ping a user.
 * 
 * @version $Id$
 * @since 17.10.0RC1
 */
public class NetfluxPingUserEvent implements Event, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String user;

    private final Integer sequence;

    /**
     * @param user the identifier of the user
     * @param sequence the sequence number of the message
     */
    public NetfluxPingUserEvent(String user, Integer sequence)
    {
        this.user = user;
        this.sequence = sequence;
    }

    /**
     * Listen to all events.
     */
    public NetfluxPingUserEvent()
    {
        this(null, null);
    }

    /**
     * @return the identifier of the user
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * @return the sequence number of the message
     */
    public Integer getSequence()
    {
        return this.sequence;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof NetfluxPingUserEvent;
    }
}
