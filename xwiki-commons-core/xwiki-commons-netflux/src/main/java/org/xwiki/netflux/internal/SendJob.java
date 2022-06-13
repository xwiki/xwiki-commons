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

/**
 * Encapsulates the messages that are pending to be sent.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
public class SendJob
{
    private final User user;

    private final List<String> messages;

    /**
     * Creates a new instance.
     * 
     * @param user the user to send the messages to
     * @param messages the message to send to the specified user
     */
    public SendJob(User user, List<String> messages)
    {
        this.user = user;
        this.messages = messages;
    }

    /**
     * @return the messages to send
     */
    public List<String> getMessages()
    {
        return messages;
    }

    /**
     * @return the user to send the messages to
     */
    public User getUser()
    {
        return user;
    }
}
