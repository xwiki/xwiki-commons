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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.observation.event.Event;

/**
 * An event triggered to ping a user.
 * 
 * @version $Id$
 * @since 17.10.1
 * @since 18.0.0RC1
 */
public class NetfluxMessageUserEvent implements Event, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String user;

    private final String message;

    /**
     * @param user the identifier of the user
     * @param message the message to send to the user
     */
    public NetfluxMessageUserEvent(String user, String message)
    {
        this.user = user;
        this.message = message;
    }

    /**
     * Listen to all events.
     */
    public NetfluxMessageUserEvent()
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
     * @return the message to send to the user
     */
    public String getMessage()
    {
        return this.message;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof NetfluxMessageUserEvent;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getUser());
        builder.append(getMessage());

        return builder.build();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof NetfluxMessageUserEvent messageEvent) {
            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getUser(), messageEvent.getUser());
            builder.append(getMessage(), messageEvent.getMessage());

            return builder.build();
        }

        return false;
    }
}
