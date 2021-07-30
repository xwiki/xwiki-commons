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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.websocket.EndpointComponent;

/**
 * The Netflux WebSocket end-point.
 * 
 * @version $Id$
 * @since 13.7RC1
 */
@Component
@Singleton
@Named("netflux")
public class NetfluxEndpoint extends Endpoint implements EndpointComponent
{
    private static final long TIMEOUT_MILLISECONDS = 30000;

    private static final boolean USE_HISTORY_KEEPER = true;

    private static final String NETFLUX_USER = "netflux.user";

    private static final String COMMAND_LEAVE = "LEAVE";

    private static final String COMMAND_JOIN = "JOIN";

    private static final String COMMAND_MSG = "MSG";

    private static final String ERROR_INVALID = "EINVAL";

    private static final String ERROR_NO_ENTITY = "ENOENT";

    private final String historyKeeper = Utils.getRandomHexString(16);

    private final Object bigLock = new Object();

    private final Map<String, User> users = new HashMap<>();

    private final JsonConverter converter = new JsonConverter();

    @Inject
    private Logger logger;

    @Inject
    private ChannelStore channels;

    @Override
    public void onOpen(Session session, EndpointConfig config)
    {
        synchronized (this.bigLock) {
            User user = getOrRegisterUser(session);

            // Send the IDENT message.
            String identMessage = display(buildDefault("", "IDENT", user.getName(), null));
            if (!sendMessage(user, identMessage)) {
                return;
            }

            session.addMessageHandler(new MessageHandler.Whole<String>()
            {
                @Override
                public void onMessage(String message)
                {
                    handleMessage(session, message);
                }
            });
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason)
    {
        synchronized (this.bigLock) {
            wsDisconnect(session);
        }
    }

    @Override
    public void onError(Session session, Throwable e)
    {
        this.logger.debug("Session closed with error.", e);
    }

    private void handleMessage(Session session, String message)
    {
        SendJob sendJob;
        synchronized (this.bigLock) {
            onMessage(session, message);
            sendJob = getSendJob();
        }
        while (sendJob != null) {
            for (String msg : sendJob.getMessages()) {
                if (!sendJob.getUser().isConnected()) {
                    break;
                }
                if (!sendMessage(sendJob.getUser(), msg)) {
                    return;
                }
            }
            sendJob = getSendJob();
        }
    }

    private void wsDisconnect(Session session)
    {
        synchronized (this.bigLock) {
            User user = getOrRegisterUser(session);

            this.logger.debug("Disconnect " + user.getName());
            this.users.remove(user.getName());
            user.setConnected(false);

            for (Channel channel : user.getChannels()) {
                channel.getUsers().remove(user.getName());
                List<Object> leaveMessage =
                    buildDefault(user.getName(), COMMAND_LEAVE, channel.getKey(), "Quit: [ wsDisconnect() ]");
                String msgStr = display(leaveMessage);
                sendChannelMessage(COMMAND_LEAVE, user, channel, msgStr);
                // Remove the channel when there is no user anymore (the history keeper doesn't count).
                if (channel.getConnectedUsers().isEmpty()) {
                    this.channels.remove(channel);
                }
            }
        }
    }

    private User getOrRegisterUser(Session session)
    {
        User user = (User) session.getUserProperties().get(NETFLUX_USER);
        if (user == null) {
            // Register the user.
            String userName = Utils.getRandomHexString(32);
            user = new User(session, userName);
            this.users.put(userName, user);
            session.getUserProperties().put(NETFLUX_USER, user);
            this.logger.debug("Registered " + userName);
        }
        return user;
    }

    private void onMessage(Session session, String message)
    {
        List<Object> msg;
        try {
            msg = this.converter.decode(message);
        } catch (DecodeException e) {
            throw new RuntimeException(e);
        }
        if (msg == null) {
            return;
        }

        User user = getOrRegisterUser(session);

        long now = System.currentTimeMillis();
        user.setTimeOfLastMessage(now);

        // Disconnect anyone who hasn't written to the WebSocket in more than TIMEOUT_MILLISECONDS.
        List<Session> sessions = this.users.values().stream().map(User::getSession).collect(Collectors.toList());
        // Filter "expired" sessions and close them.
        sessions.stream().filter(s -> now - getOrRegisterUser(s).getTimeOfLastMessage() > TIMEOUT_MILLISECONDS)
            .forEach(this::wsDisconnect);

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
        } else if (COMMAND_MSG.equals(cmd)) {
            /*
             * MSG (patch): - Send an ACK - Check if the history of the channel is requested - Yes : send the history -
             * No : transfer the message to the recipient
             */
            onCommandMessage(user, seq, obj, msg);
        }
    }

    private void onCommandJoin(User user, Integer seq, String channelKey)
    {
        // Key Length == 32 ==> Cryptpad key
        // Key Length == 48 ==> RTFrontend key
        if (!StringUtils.isEmpty(channelKey) && channelKey.length() != 32 && channelKey.length() != 48) {
            ArrayList<Object> errorMsg = buildError(seq, ERROR_INVALID, "");
            addMessage(user, display(errorMsg));
            return;
        }
        Channel channel = (channelKey == null) ? null : this.channels.get(channelKey);
        // No key provided: create a new channel.
        if (channel == null && StringUtils.isEmpty(channelKey)) {
            channel = createChannel();
        } else if (channel == null) {
            ArrayList<Object> errorMsg = buildError(seq, ERROR_NO_ENTITY, "");
            addMessage(user, display(errorMsg));
            return;
        }
        ArrayList<Object> jackMsg = buildJack(seq, channel.getKey());
        addMessage(user, display(jackMsg));
        user.getChannels().add(channel);
        for (String userId : channel.getUsers().keySet()) {
            ArrayList<Object> inChannelMsg = buildDefault(userId, COMMAND_JOIN, channel.getKey(), null);
            addMessage(user, display(inChannelMsg));
        }
        channel.getUsers().put(user.getName(), user);
        this.channels.prune();
        ArrayList<Object> joinMsg = buildDefault(user.getName(), COMMAND_JOIN, channel.getKey(), null);
        sendChannelMessage(COMMAND_JOIN, user, channel, display(joinMsg));
        return;
    }

    private void onCommandLeave(User user, Integer seq, String channelKey)
    {
        ArrayList<Object> errorMsg = null;
        if (StringUtils.isEmpty(channelKey)) {
            errorMsg = buildError(seq, ERROR_INVALID, "undefined");
        }
        if (errorMsg != null && this.channels.get(channelKey) == null) {
            errorMsg = buildError(seq, ERROR_NO_ENTITY, channelKey);
        }
        if (errorMsg != null && !this.channels.get(channelKey).getUsers().containsKey(user.getName())) {
            errorMsg = buildError(seq, "NOT_IN_CHAN", channelKey);
        }
        if (errorMsg != null) {
            addMessage(user, display(errorMsg));
            return;
        }
        ArrayList<Object> ackMsg = buildAck(seq);
        addMessage(user, display(ackMsg));
        Channel channel = this.channels.get(channelKey);
        channel.getUsers().remove(user.getName());
        user.getChannels().remove(channel);
        ArrayList<Object> leaveMsg = buildDefault(user.getName(), COMMAND_LEAVE, channelKey, "");
        sendChannelMessage(COMMAND_LEAVE, user, channel, display(leaveMsg));
    }

    private void onCommandPing(User user, Integer seq)
    {
        ArrayList<Object> ackMsg = buildAck(seq);
        addMessage(user, display(ackMsg));
    }

    private void onCommandMessage(User user, Integer seq, String channelKeyOrUserName, List<Object> msg)
    {
        ArrayList<Object> ackMsg = buildAck(seq);
        addMessage(user, display(ackMsg));
        if (USE_HISTORY_KEEPER && channelKeyOrUserName.equals(this.historyKeeper)) {
            List<Object> msgHistory;
            try {
                msgHistory = this.converter.decode(msg.get(3).toString());
            } catch (DecodeException e) {
                msgHistory = null;
                this.logger.debug("Failed to parse message history.", e);
            }
            String text = (msgHistory == null) ? "" : (String) msgHistory.get(0);
            if ("GET_HISTORY".equals(text)) {
                String channelKey = (String) msgHistory.get(1);
                Channel channel = this.channels.get(channelKey);
                if (channel != null) {
                    channel.getMessages().forEach(msgStr -> addMessage(user, msgStr));
                }
                String endHistoryMsg = "{\"state\":1, \"channel\":\"" + channelKey + "\"}";
                ArrayList<Object> msgEndHistory = buildMessage(0, this.historyKeeper, user.getName(), endHistoryMsg);
                addMessage(user, display(msgEndHistory));
            }
        } else if (this.channels.get(channelKeyOrUserName) != null) {
            // Send message to the specified channel.
            ArrayList<Object> msgMsg = buildMessage(0, user.getName(), channelKeyOrUserName, msg.get(3));
            Channel chan = this.channels.get(channelKeyOrUserName);
            sendChannelMessage(COMMAND_MSG, user, chan, display(msgMsg));
        } else if (this.users.containsKey(channelKeyOrUserName)) {
            // Send message to the specified user.
            ArrayList<Object> msgMsg = buildMessage(0, user.getName(), channelKeyOrUserName, msg.get(3));
            addMessage(this.users.get(channelKeyOrUserName), display(msgMsg));
        } else if (!channelKeyOrUserName.isEmpty()) {
            // Unknown channel / user.
            ArrayList<Object> errorMsg = buildError(seq, ERROR_NO_ENTITY, channelKeyOrUserName);
            addMessage(user, display(errorMsg));
        }
    }

    private boolean sendMessage(User user, String message)
    {
        try {
            this.logger.debug("Sending to [{}] : [{}]", user.getName(), message);
            user.getSession().getBasicRemote().sendText(message);
            return true;
        } catch (IOException e) {
            this.logger.debug("Sending failed.", e);
            wsDisconnect(user.getSession());
            return false;
        }
    }

    private SendJob getSendJob()
    {
        synchronized (this.bigLock) {
            for (User user : this.users.values()) {
                if (user.isConnected() && !user.getMessagesToBeSent().isEmpty()) {
                    SendJob out = new SendJob(user, new ArrayList<>(user.getMessagesToBeSent()));
                    user.getMessagesToBeSent().clear();
                    return out;
                }
            }
            return null;
        }
    }

    /**
     * Create a new channel with a randomly generated key.
     * 
     * @return the created channel
     */
    public Channel createChannel()
    {
        Channel channel = this.channels.create();
        if (USE_HISTORY_KEEPER) {
            channel.getUsers().put(this.historyKeeper, null);
        }
        return channel;
    }

    private String display(List<Object> list)
    {
        try {
            return this.converter.encode(list);
        } catch (EncodeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a message to the sending queue of a user.
     * 
     * @param toUser the target user
     * @param message the message to add
     */
    private void addMessage(User toUser, String message)
    {
        this.logger.debug("Adding message to [{}]: [{}]", toUser.getName(), message);
        toUser.getMessagesToBeSent().add(message);
    }

    private boolean isCheckpoint(String message)
    {
        try {
            List<Object> msg = this.converter.decode(message);
            return ((String) msg.get(msg.size() - 1)).indexOf("cp|[4,[") == 0;
        } catch (DecodeException e) {
            throw new RuntimeException(e);
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
        channel.getUsers().values().stream().filter(Objects::nonNull)
            .filter(user -> !(COMMAND_MSG.equals(cmd) && user.equals(me))).forEach(user -> addMessage(user, message));

        if (USE_HISTORY_KEEPER && (COMMAND_MSG.equals(cmd) || COMMAND_LEAVE.equals(cmd))) {
            this.logger.debug("Added in history: [{}]", message);
            if (COMMAND_MSG.equals(cmd) && isCheckpoint(message)) {
                // Prune old messages from memory.
                this.logger.debug("Pruning old messages.");
                LinkedList<String> msgsNext = new LinkedList<String>();
                for (Iterator<String> it = channel.getMessages().descendingIterator(); it.hasNext();) {
                    String msg = it.next();
                    msgsNext.addFirst(msg);
                    if (isCheckpoint(msg)) {
                        break;
                    }
                }
                channel.getMessages().clear();
                channel.getMessages().addAll(msgsNext);
            }
            channel.getMessages().add(message);
        }
    }

    /*
     * The following function are used to build the different types of messages sent by the server : ACK, JACK
     * (Join-ACK), JOIN, LEAVE, MSG, ERROR
     */
    private ArrayList<Object> buildAck(Integer seq)
    {
        ArrayList<Object> msg = new ArrayList<>();
        msg.add(seq);
        msg.add("ACK");
        return msg;
    }

    private ArrayList<Object> buildJack(Integer seq, String obj)
    {
        ArrayList<Object> msg = new ArrayList<>();
        msg.add(seq);
        msg.add("JACK");
        msg.add(obj);
        return msg;
    }

    private ArrayList<Object> buildDefault(String userId, String cmd, String chanName, String reason)
    {
        ArrayList<Object> msg = new ArrayList<>();
        msg.add(0);
        msg.add(userId);
        msg.add(cmd);
        msg.add(chanName);
        if (reason != null) {
            msg.add(reason);
        }
        return msg;
    }

    private ArrayList<Object> buildMessage(Integer seq, String userId, String obj, Object msgStr)
    {
        ArrayList<Object> msg = new ArrayList<>();
        msg.add(0);
        msg.add(userId);
        msg.add(COMMAND_MSG);
        msg.add(obj);
        msg.add(msgStr);
        return msg;
    }

    private ArrayList<Object> buildError(Integer seq, String errorType, String errorMessage)
    {
        ArrayList<Object> msg = new ArrayList<>();
        msg.add(seq);
        msg.add("ERROR");
        msg.add(errorType);
        msg.add(errorMessage);
        return msg;
    }
}
