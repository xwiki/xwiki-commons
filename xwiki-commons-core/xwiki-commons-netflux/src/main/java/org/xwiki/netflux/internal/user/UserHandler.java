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
package org.xwiki.netflux.internal.user;

import org.xwiki.component.annotation.Role;
import org.xwiki.netflux.internal.NetfluxException;
import org.xwiki.netflux.internal.User;

/**
 * Communicate with various types of users.
 * 
 * @param <U> the type of User
 * @version $Id$
 * @since 17.10.0RC1
 */
@Role
public interface UserHandler<U extends User>
{
    /**
     * Send a text message. The message may or may not be sent asynchronously, depending on the implementation.
     *
     * @param user the user to send the message to
     * @param text the message to be sent
     * @throws IllegalArgumentException if the text is {@code null}
     * @throws NetfluxException when failing to send the message
     */
    void sendText(U user, String text) throws NetfluxException;
}
