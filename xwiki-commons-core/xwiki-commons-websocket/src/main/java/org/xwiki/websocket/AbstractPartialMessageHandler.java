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

import jakarta.websocket.MessageHandler;

/**
 * Base class for partial message handlers that want to handle messages when all their parts have been received.
 * 
 * @param <T> the type of message to handle
 * @version $Id$
 * @since 13.10.5
 * @since 14.3RC1
 */
public abstract class AbstractPartialMessageHandler<T> implements MessageHandler.Partial<T>
{
    /**
     * The list of message parts received so far.
     */
    private final List<T> parts = new LinkedList<>();

    /**
     * The maximum allowed size for a message.
     */
    private final int maxMessageSize;

    /**
     * The current message size, i.e. the aggregated size of the partial messages received so far.
     */
    private int messageSize;

    /**
     * Create a new partial message handler with no limit on the message size.
     */
    protected AbstractPartialMessageHandler()
    {
        this(0);
    }

    /**
     * Create a new partial message handler with the specified maximum message size.
     *
     * @param maxMessageSize the maximum allowed size for a message; only positive values are enforced
     * @since 18.3.0RC1
     */
    protected AbstractPartialMessageHandler(int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
    }

    @Override
    public void onMessage(T partialMessage, boolean last)
    {
        this.messageSize += getLength(partialMessage);
        if (this.maxMessageSize > 0 && this.messageSize > this.maxMessageSize) {
            throw new IllegalStateException(
                "Message size exceeds the configured limit of [%s].".formatted(this.maxMessageSize));
        }

        this.parts.add(partialMessage);
        if (last) {
            onMessage(Collections.unmodifiableList(this.parts));

            // Reset the message size and the message parts for the next message.
            this.messageSize = 0;
            this.parts.clear();
        }
    }

    /**
     * Get the length of the given partial message. This is used to compute the total message size and check it against
     * the configured maximum message size.
     * <p>
     * NOTE: the default implementation supports only {@link String} and {@code byte[]} messages. For other types of
     * messages you'll have to overwrite this method to return the appropriate length if you want to enforce the maximum
     * message size limit.
     *
     * @param partialMessage the partial message to get the length of
     * @return the length of the given partial message
     * @since 18.3.0RC1
     */
    protected int getLength(T partialMessage)
    {
        return switch (partialMessage) {
            case null -> 0;
            case String stringPartialMessage -> stringPartialMessage.length();
            case byte[] byteArrayPartialMessage -> byteArrayPartialMessage.length;
            default -> 0;
        };
    }

    /**
     * @return the current message size, i.e. the aggregated size of the partial messages received so far
     * @since 18.3.0RC1
     */
    protected int getMessageSize()
    {
        return this.messageSize;
    }

    /**
     * Called to handle the message when all its parts have been received.
     * 
     * @param parts the message parts
     */
    public abstract void onMessage(List<T> parts);
}
