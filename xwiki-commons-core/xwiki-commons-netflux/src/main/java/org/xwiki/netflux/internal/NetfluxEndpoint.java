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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.websocket.AbstractPartialStringMessageHandler;
import org.xwiki.websocket.EndpointComponent;

/**
 * The Netflux WebSocket end-point.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
@Component
@Singleton
@Named("netflux")
public class NetfluxEndpoint extends Endpoint implements EndpointComponent
{
    // The client side keeps the connection alive by sending a PING message from time to time, using a timer
    // (setTimeout). The browsers are slowing down timers used by inactive tabs / windows (that don't have
    // the user focus). This is called timer throttling and can go up to 1 minute, which means inactive browser tabs
    // won't be able to send PING messages more often than every minute. For this reason, we set the session idle
    // timeout a little bit higher than the timer throttling value to make sure the WebSocket connection is not closed
    // in background tabs.
    // See https://developer.chrome.com/blog/timer-throttling-in-chrome-88/
    private static final long TIMEOUT_MILLISECONDS = 65000;

    private static final String NETFLUX_USER = "netflux.user";

    private static final String COMMAND_LEAVE = "LEAVE";

    private static final String COMMAND_JOIN = "JOIN";

    private static final String ERROR_INVALID = "EINVAL";

    private static final String ERROR_NO_ENTITY = "ENOENT";

    private final Object bigLock = new Object();

    private final Map<String, User> users = new HashMap<>();

    @Inject
    private Logger logger;

    @Inject
    private IdGenerator idGenerator;

    @Inject
    private ChannelStore channels;

    @Inject
    private MessageBuilder messageBuilder;

    @Override
    public void onOpen(Session session, EndpointConfig config)
    {
        synchronized (this.bigLock) {
            // Close the session if we don't receive any message from the user in TIMEOUT_MILLISECONDS.
            session.setMaxIdleTimeout(TIMEOUT_MILLISECONDS);

            User user = getOrRegisterUser(session);

            // Send the IDENT message.
            String identMessage = this.messageBuilder.buildDefault("", "IDENT", user.getName(), null);
            if (!sendMessage(user, identMessage)) {
                return;
            }

            session.addMessageHandler(new AbstractPartialStringMessageHandler()
            {
                @Override
                public void onMessage(String message)
                {
                    synchronized (NetfluxEndpoint.this.bigLock) {
                        NetfluxEndpoint.this.onMessage(session, message);
                    }
                }
            });
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason)
    {
        synchronized (this.bigLock) {
            User user = getOrRegisterUser(session);

            this.logger.debug("Last message from [{}] received [{}ms] ago. Session idle timeout is [{}].",
                user.getName(), System.currentTimeMillis() - user.getTimeOfLastMessage(), session.getMaxIdleTimeout());
            this.logger.debug("Disconnecting [{}] because [{}] ([{}])", user.getName(), closeReason.getReasonPhrase(),
                closeReason.getCloseCode());
            this.users.remove(user.getName());
            user.setConnected(false);

            // We copy the set of channels because we're modifying it while iterating over it.
            new LinkedList<Channel>(user.getChannels()).forEach(channel -> leaveChannel(user, channel, "Disconnected"));
        }
    }

    @Override
    public void onError(Session session, Throwable e)
    {
        this.logger.debug("Session closed with error.", e);
        onClose(session,
            new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, ExceptionUtils.getRootCauseMessage(e)));
    }

    private User getOrRegisterUser(Session session)
    {
        User user = (User) session.getUserProperties().get(NETFLUX_USER);
        if (user == null) {
            // Register the user.
            String userId = this.idGenerator.generateUserId();
            user = new User(session, userId);
            this.users.put(userId, user);
            session.getUserProperties().put(NETFLUX_USER, user);
            this.logger.debug("Registered [{}]", userId);
        }
        return user;
    }

    private void onMessage(Session session, String message)
    {
        List<Object> msg = this.messageBuilder.decode(message);
        if (msg == null) {
            return;
        }

        User user = getOrRegisterUser(session);
        // The time of the last message received from a user was initially used to close expired sessions (i.e. sessions
        // in which we haven't received any message in the past TIMEOUT_MILLISECONDS). This is now done by setting the
        // max idle timeout of the session to TIMEOUT_MILLISECONDS. We still keep track of the time of the last message
        // mostly for debugging purposes.
        user.setTimeOfLastMessage(System.currentTimeMillis());

        Integer seq = (Integer) msg.get(0);
        String cmd = msg.get(1).toString();
        String obj = "";
        if (msg.size() >= 3) {
            obj = Objects.toString(msg.get(2), null);
        }

        if (COMMAND_JOIN.equals(cmd)) {
            /*
             * JOIN request: - Send a JACK - Join or create the channel - Send a JOIN message to the selected channel
             */
            onCommandJoin(user, seq, obj);
        } else if (COMMAND_LEAVE.equals(cmd)) {
            /*
             * LEAVE request: - Check if the request is correct - Send an ACK - Leave the channel - Send a LEAVE message
             * to the selected channel
             */
            onCommandLeave(user, seq, obj);
        } else if (cmd.equals("PING")) {
            /*
             * PING: - Send an ACK
             */
            onCommandPing(user, seq);
        } else if (MessageBuilder.COMMAND_MSG.equals(cmd)) {
            /*
             * MSG (patch): - Send an ACK - Check if the history of the channel is requested - Yes : send the history -
             * No : transfer the message to the recipient
             */
            onCommandMessage(user, seq, obj, msg);
        }
    }

    private void onCommandJoin(User user, Integer seq, String channelKey)
    {
        // Expected channel key length is 48, see IdGenerator.generateChannelId().
        if (!StringUtils.isEmpty(channelKey) && channelKey.length() != 48) {
            String errorMsg = this.messageBuilder.buildError(seq, ERROR_INVALID, "Invalid channel key");
            sendMessage(user, errorMsg);
            return;
        }
        Channel channel = (channelKey == null) ? null : this.channels.get(channelKey);
        // No key provided: create a new channel.
        if (channel == null && StringUtils.isEmpty(channelKey)) {
            channel = this.channels.create();
        } else if (channel == null) {
            String errorMsg = this.messageBuilder.buildError(seq, ERROR_NO_ENTITY,
                String.format("Channel [%s] not found", channelKey));
            sendMessage(user, errorMsg);
            return;
        }
        String jackMsg = this.messageBuilder.buildJoinAck(seq, channel.getKey());
        sendMessage(user, jackMsg);
        user.getChannels().add(channel);
        // The user that just joined the channel has to know what other users and bots are in the channel (for instance
        // to find out the history keeper) so we send them a JOIN command for each member of the channel.
        Set<String> botsAndUsers = new LinkedHashSet<>(channel.getBots().keySet());
        botsAndUsers.addAll(channel.getUsers().keySet());
        for (String userOrBotId : botsAndUsers) {
            String inChannelMsg = this.messageBuilder.buildDefault(userOrBotId, COMMAND_JOIN, channel.getKey(), null);
            sendMessage(user, inChannelMsg);
        }
        channel.getUsers().put(user.getName(), user);
        this.channels.prune();
        String joinMsg = this.messageBuilder.buildDefault(user.getName(), COMMAND_JOIN, channel.getKey(), null);
        sendChannelMessage(COMMAND_JOIN, user, channel, joinMsg);
    }

    private void onCommandLeave(User user, Integer seq, String channelKey)
    {
        String errorMsg = null;
        if (StringUtils.isEmpty(channelKey)) {
            errorMsg = this.messageBuilder.buildError(seq, ERROR_INVALID, "Channel key is not specified");
        } else if (this.channels.get(channelKey) == null) {
            errorMsg = this.messageBuilder.buildError(seq, ERROR_NO_ENTITY, channelKey);
        } else if (!this.channels.get(channelKey).getUsers().containsKey(user.getName())) {
            errorMsg = this.messageBuilder.buildError(seq, "NOT_IN_CHAN", channelKey);
        }
        if (errorMsg != null) {
            sendMessage(user, errorMsg);
            return;
        }
        String ackMsg = this.messageBuilder.buildAck(seq);
        sendMessage(user, ackMsg);
        Channel channel = this.channels.get(channelKey);
        leaveChannel(user, channel, "");
    }

    private void leaveChannel(User user, Channel channel, String reason)
    {
        channel.getUsers().remove(user.getName());
        user.getChannels().remove(channel);

        String leaveMessage = this.messageBuilder.buildDefault(user.getName(), COMMAND_LEAVE, channel.getKey(), reason);
        sendChannelMessage(COMMAND_LEAVE, user, channel, leaveMessage);

        // Remove the channel when there is no user anymore (the history keeper doesn't count).
        if (channel.getConnectedUsers().isEmpty()) {
            this.channels.remove(channel);
        }
    }

    private void onCommandPing(User user, Integer seq)
    {
        String ackMsg = this.messageBuilder.buildAck(seq);
        sendMessage(user, ackMsg);
    }

    private void onCommandMessage(User user, Integer seq, String channelKeyOrUserName, List<Object> msg)
    {
        String ackMsg = this.messageBuilder.buildAck(seq);
        sendMessage(user, ackMsg);
        Optional<Bot> bot = getBot(user, channelKeyOrUserName);
        if (bot.isPresent()) {
            // Send message to the specified bot.
            bot.get().onUserMessage(user, msg);
        } else if (this.channels.get(channelKeyOrUserName) != null) {
            // Send message to the specified channel.
            String msgMsg = this.messageBuilder.buildMessage(0, user.getName(), channelKeyOrUserName, msg.get(3));
            Channel chan = this.channels.get(channelKeyOrUserName);
            sendChannelMessage(MessageBuilder.COMMAND_MSG, user, chan, msgMsg);
        } else if (this.users.containsKey(channelKeyOrUserName)) {
            // Send message to the specified user.
            String msgMsg = this.messageBuilder.buildMessage(0, user.getName(), channelKeyOrUserName, msg.get(3));
            sendMessage(this.users.get(channelKeyOrUserName), msgMsg);
        } else if (!channelKeyOrUserName.isEmpty()) {
            // Unknown channel / user / bot.
            String errorMsg = this.messageBuilder.buildError(seq, ERROR_NO_ENTITY, channelKeyOrUserName);
            sendMessage(user, errorMsg);
        }
    }

    private Optional<Bot> getBot(User user, String id)
    {
        return user.getChannels().stream().map(channel -> channel.getBots().get(id)).filter(Objects::nonNull)
            .findFirst();
    }

    private boolean sendMessage(User user, String message)
    {
        try {
            this.logger.debug("Sending to [{}] : [{}]", user.getName(), message);
            user.getSession().getBasicRemote().sendText(message);
            return true;
        } catch (IOException e) {
            this.logger.debug("Sending failed.", e);
            onClose(user.getSession(),
                new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, ExceptionUtils.getRootCauseMessage(e)));
            return false;
        }
    }

    /**
     * Broadcast a message to a channel.
     * 
     * @param cmd the message type/command
     * @param me the sender
     * @param channel the channel where the message is sent
     * @param message the message
     */
    private void sendChannelMessage(String cmd, User me, Channel channel, String message)
    {
        // Broadcast the message to all the bots connected to the channel.
        channel.getBots().values().forEach(bot -> bot.onChannelMessage(channel, me, cmd, message));

        // Broadcast the message to all the users connected to the channel.
        channel.getUsers().values().stream()
            .filter(user -> !(MessageBuilder.COMMAND_MSG.equals(cmd) && user.equals(me)))
            .forEach(user -> sendMessage(user, message));
    }
}
