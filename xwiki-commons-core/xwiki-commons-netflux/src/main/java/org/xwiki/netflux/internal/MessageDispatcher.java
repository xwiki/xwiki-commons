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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jakarta.websocket.DecodeException;
import jakarta.websocket.EncodeException;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;

/**
 * The component responsible for dispatching the Netflux messages.
 *
 * @version $Id$
 * @since 15.10.11
 * @since 16.4.1
 * @since 16.5.0RC1
 */
@Component(roles = MessageDispatcher.class)
@Singleton
public class MessageDispatcher
{
    /**
     * The standard message type.
     */
    public static final String COMMAND_MSG = "MSG";

    @Inject
    protected Logger logger;

    private final JsonConverter converter = new JsonConverter();

    /**
     * Add a message to the sending queue of a given user.
     * 
     * @param toUser the target user
     * @param message the message to add
     */
    public void addMessage(User toUser, String message)
    {
        this.logger.debug("Adding message to [{}]: [{}]", toUser.getName(), message);
        toUser.getMessagesToBeSent().add(message);
    }

    /**
     * Build a message to be sent to a user.
     *
     * @param sequence the sequence number of the message
     * @param senderId the ID of the sender (channel, user or bot)
     * @param receiverId the ID of the receiver (channel, user or bot)
     * @param messageBody the message body
     * @return the encoded message to send
     */
    public String buildMessage(Integer sequence, String senderId, String receiverId, Object messageBody)
    {
        return encode(List.of(sequence, senderId, COMMAND_MSG, receiverId, messageBody));
    }

    /**
     * Build an acknoledgement message.
     *
     * @param sequence the sequence number of the message
     * @return the encoded acknoledgement message
     */
    public String buildAck(Integer sequence)
    {
        return encode(List.of(sequence, "ACK"));
    }

    /**
     * Build a join acknoledgement message (sent to confirm that the user has joined the channel).
     *
     * @param sequence the sequence number of the message
     * @param channelKey specifies the channel that the user has joined
     * @return the encoded join acknoledgement message
     */
    public String buildJoinAck(Integer sequence, String channelKey)
    {
        return encode(List.of(sequence, "JACK", channelKey));
    }

    /**
     * Build a generic message.
     *
     * @param senderId the ID of the sender
     * @param messageType the type of message
     * @param receiverId the ID of the receiver
     * @param messageBody the message body
     * @return the encoded message
     */
    public String buildDefault(String senderId, String messageType, String receiverId, Object messageBody)
    {
        List<Object> msg = new ArrayList<>();
        msg.add(0);
        msg.add(senderId);
        msg.add(messageType);
        msg.add(receiverId);
        if (messageBody != null) {
            msg.add(messageBody);
        }
        return encode(msg);
    }

    /**
     * Build an error message.
     *
     * @param sequence the sequence number of the message
     * @param errorType the type of error
     * @param errorMessage the error message
     * @return the encoded error message
     */
    public String buildError(Integer sequence, String errorType, String errorMessage)
    {
        return encode(List.of(sequence, "ERROR", errorType, errorMessage));
    }

    /**
     * Encode a message to be sent.
     *
     * @param message the message to encode
     * @return the encoded message
     */
    public String encode(List<Object> message)
    {
        try {
            return this.converter.encode(message);
        } catch (EncodeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes a received message.
     *
     * @param message the message to decode
     * @return the decoded message
     */
    public List<Object> decode(String message)
    {
        try {
            return this.converter.decode(message);
        } catch (DecodeException e) {
            throw new RuntimeException(e);
        }
    }
}
