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
package org.xwiki.websocket;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.websocket.MessageHandler;

import org.xwiki.stability.Unstable;

/**
 * Base class for partial message handlers that want to handle messages when all their parts have been received.
 * 
 * @param <T> the type of message to handle
 * @version $Id$
 * @since 13.10.5
 * @since 14.3RC1
 */
@Unstable
public abstract class AbstractPartialMessageHandler<T> implements MessageHandler.Partial<T>
{
    /**
     * The list of message parts received so far.
     */
    private final List<T> parts = new LinkedList<>();

    @Override
    public void onMessage(T partialMessage, boolean last)
    {
        this.parts.add(partialMessage);
        if (last) {
            onMessage(Collections.unmodifiableList(this.parts));
            this.parts.clear();
        }
    }

    /**
     * Called to handle the message when all its parts have been received.
     * 
     * @param parts the message parts
     */
    public abstract void onMessage(List<T> parts);
}
