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

import java.nio.ByteBuffer;
import java.util.List;

import org.xwiki.stability.Unstable;

/**
 * Base class for partial binary message handlers.
 *
 * @version $Id$
 * @since 18.3.0RC1
 */
@Unstable
public abstract class AbstractPartialBinaryMessageHandler extends AbstractPartialMessageHandler<byte[]>
{
    /**
     * Create a new partial binary message handler with the specified maximum message size.
     *
     * @param maxMessageSize the maximum allowed size for a message; only positive values are enforced
     */
    protected AbstractPartialBinaryMessageHandler(int maxMessageSize)
    {
        super(maxMessageSize);
    }

    @Override
    public void onMessage(List<byte[]> parts)
    {
        this.onMessage(this.getMessage(parts));
    }

    private byte[] getMessage(List<byte[]> parts)
    {
        ByteBuffer buffer = ByteBuffer.allocate(getMessageSize());
        for (byte[] part : parts) {
            buffer.put(part);
        }

        return buffer.array();
    }

    /**
     * Called to handle the whole binary message when all its parts have been received.
     * 
     * @param message the complete binary message
     */
    public abstract void onMessage(byte[] message);
}
