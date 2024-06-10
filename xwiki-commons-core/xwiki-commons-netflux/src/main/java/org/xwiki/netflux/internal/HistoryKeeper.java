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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;

/**
 * Holds the key of the history keeper fake user that is added to all Netflux channels.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
@Component
@Singleton
@Named("HistoryKeeper")
public class HistoryKeeper extends AbstractBot
{
    @Inject
    private Logger logger;

    @Inject
    private ChannelStore channels;

    @Inject
    private MessageDispatcher dispatcher;

    @Override
    public String getId()
    {
        // The client side identifies the history keeper by the id length, expecting 16 characters, so we have to reduce
        // the id length compared to other bots.
        return super.getId().substring(0, 16);
    }

    @Override
    public void onUserMessage(User sender, List<Object> message)
    {
        if (message.size() <= 3) {
            // Unsupported message format.
            return;
        }

        List<Object> messageBody;
        try {
            messageBody = this.dispatcher.decode(message.get(3).toString());
        } catch (Exception e) {
            this.logger.debug("Failed to parse message body.", e);
            return;
        }

        if (messageBody.size() < 2 || !"GET_HISTORY".equals(messageBody.get(0))) {
            // Unsupported message format.
            return;
        }

        String channelKey = (String) messageBody.get(1);
        Channel channel = this.channels.get(channelKey);
        if (channel != null) {
            channel.getMessages().forEach(msgStr -> this.dispatcher.addMessage(sender, msgStr));
        }
        String endHistoryBody = "{\"state\":1, \"channel\":\"" + channelKey + "\"}";
        String endHistoryMessage = this.dispatcher.buildMessage(0, getId(), sender.getName(), endHistoryBody);
        this.dispatcher.addMessage(sender, endHistoryMessage);
    }

    @Override
    public void onChannelMessage(Channel channel, User sender, String messageType, String message)
    {
        // We keep only command messages in the channel history. Note that the channel history is replayed when a user
        // joins a channel, so we don't want to replay messages like JOIN or LEAVE (an user can join and leave a channel
        // multiple times).
        if (MessageDispatcher.COMMAND_MSG.equals(messageType)) {
            this.logger.debug("Added in history: [{}]", message);
            if (isCheckpoint(message)) {
                // Prune old messages from memory.
                this.logger.debug("Pruning old messages.");
                // Keep only the messages that were sent after the last checkpoint.
                LinkedList<String> recentMessages = new LinkedList<>();
                for (Iterator<String> iterator = channel.getMessages().descendingIterator(); iterator.hasNext();) {
                    String recentMessage = iterator.next();
                    recentMessages.addFirst(recentMessage);
                    if (isCheckpoint(recentMessage)) {
                        break;
                    }
                }
                channel.getMessages().clear();
                channel.getMessages().addAll(recentMessages);
            }
            channel.getMessages().add(message);
        }
    }

    private boolean isCheckpoint(String message)
    {
        List<Object> msg = this.dispatcher.decode(message);
        if (!msg.isEmpty()) {
            Object lastItem = msg.get(msg.size() - 1);
            if (lastItem instanceof String) {
                return ((String) lastItem).startsWith("cp|[4,[");
            }
        }
        return false;
    }
}
