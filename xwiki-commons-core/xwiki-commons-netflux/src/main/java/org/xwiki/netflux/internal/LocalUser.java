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

import jakarta.websocket.Session;

/**
 * A user accessing the current instance.
 * 
 * @version $Id$
 * @since 17.10.1
 * @since 18.0.0RC1
 */
public class LocalUser extends User
{
    private final Session session;

    /**
     * Creates a new user with the specified name, using the given WebSocket session.
     * 
     * @param session the WebSocket session used to communicate with the user
     * @param name the identifier of the user
     */
    public LocalUser(Session session, String name)
    {
        super(name);

        this.session = session;
    }

    /**
     * @return the WebSocket session
     */
    public Session getSession()
    {
        return this.session;
    }
}
