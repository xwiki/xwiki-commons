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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Store/remove/get channels in memory.
 * 
 * @version $Id$
 * @since 13.8RC1
 */
@Component(roles = ChannelStore.class)
@Singleton
public class ChannelStore
{
    private final Map<String, Channel> channelByKey = new ConcurrentHashMap<>();

    /**
     * Creates a new channel with a randomly generated key.
     * 
     * @return the created channel
     */
    public Channel create()
    {
        Channel channel = new Channel();
        this.channelByKey.put(channel.getKey(), channel);
        return channel;
    }

    /**
     * Access an existing channel by its key.
     * 
     * @param key the channel key
     * @return the corresponding channel
     */
    public Channel get(String key)
    {
        return this.channelByKey.get(key);
    }

    /**
     * Remove a channel from memory.
     * 
     * @param channel the channel to remove
     * @return {@code true} if this channel store was modified as a result, {@code false} otherwise
     */
    public boolean remove(Channel channel)
    {
        return this.channelByKey.remove(channel.getKey()) != null;
    }

    /**
     * Remove expired empty channels (that don't have any users left).
     */
    public void prune()
    {
        try {
            long currentTime = System.currentTimeMillis();
            for (Channel channel : this.channelByKey.values()) {
                if (channel.getConnectedUsers().isEmpty()
                    && (currentTime - channel.getCreationDate()) > (1000 * 60 * 60 * 2)) {
                    remove(channel);
                }
            }
        } catch (Exception e) {
            // Keep going if cleanEmpty fails...
        }
    }
}
