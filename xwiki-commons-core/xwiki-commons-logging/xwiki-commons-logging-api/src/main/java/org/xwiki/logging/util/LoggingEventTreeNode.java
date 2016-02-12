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
package org.xwiki.logging.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.xwiki.logging.LoggingEventMessage;

/**
 * Logs organized as a tree.
 *
 * @version $Id$
 * @since 8.0M2
 */
public class LoggingEventTreeNode extends LoggingEventMessage implements Iterable<LoggingEvent>, Serializable
{
    private static class LogTreeNodeIterator implements Iterator<LoggingEvent>
    {
        private final Iterator<LoggingEvent> rootIterator;

        private Iterator<LoggingEvent> currentIterator;

        LogTreeNodeIterator(Iterator<LoggingEvent> rootIterator)
        {
            this.rootIterator = rootIterator;
            this.currentIterator = this.rootIterator;
        }

        @Override
        public boolean hasNext()
        {
            return this.currentIterator.hasNext() || this.rootIterator.hasNext();
        }

        @Override
        public LoggingEvent next()
        {
            if (!this.currentIterator.hasNext()) {
                this.currentIterator = this.rootIterator;
            }

            LoggingEvent logEvent = this.currentIterator.next();

            if (this.currentIterator == this.rootIterator && logEvent instanceof LoggingEventTreeNode) {
                this.currentIterator = ((LoggingEventTreeNode) logEvent).iterator(true);
            }

            return logEvent;
        }

        @Override
        public void remove()
        {
            this.currentIterator.remove();
        }
    }

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The children of this log event.
     */
    protected ConcurrentLinkedQueue<LoggingEvent> children;

    // Iterable

    LoggingEventTreeNode()
    {

    }

    /**
     * @param logEvent the log event to copy
     */
    public LoggingEventTreeNode(LoggingEvent logEvent)
    {
        super(logEvent);
    }

    @Override
    public Iterator<LoggingEvent> iterator()
    {
        return this.children != null ? this.children.iterator() : Collections.<LoggingEvent>emptyList().iterator();
    }

    /**
     * @param recurse if true navigate through the whole tree, otherwise only the first level
     * @return an iterator over a tree of logs
     */
    public Iterator<LoggingEvent> iterator(boolean recurse)
    {
        if (!recurse) {
            return iterator();
        }

        return new LogTreeNodeIterator(iterator());
    }

    /**
     * The number of logs.
     *
     * @param recurse if true navigate through the whole tree, otherwise only the first level
     * @return the number of log events
     */
    public int size(boolean recurse)
    {
        if (!recurse) {
            return this.children != null ? this.children.size() : 0;
        }

        int size = 0;

        for (LoggingEvent logEvent : this) {
            ++size;

            if (logEvent instanceof LoggingEventTreeNode) {
                size += ((LoggingEventTreeNode) logEvent).size(true);
            }
        }

        return size;
    }

    /**
     * @param logEvent the log event to store
     */
    void add(LoggingEvent logEvent)
    {
        if (this.children == null) {
            this.children = new ConcurrentLinkedQueue<LoggingEvent>();
        }

        this.children.add(logEvent);
    }

    /**
     * Filter logs of a specific level.
     *
     * @param level the level of the logs to return
     * @param recurse if one of the {@link LoggingEvent} is a node look at its children too etc.
     * @return the filtered logs
     */
    public List<LoggingEvent> getLogs(Level level, boolean recurse)
    {
        List<LoggingEvent> levelLogs = new LinkedList<LoggingEvent>();

        for (LoggingEvent log : this) {
            if (log.getLevel() == level) {
                levelLogs.add(log);
            }

            if (recurse && log instanceof LoggingEventTreeNode) {
                levelLogs.addAll(((LoggingEventTreeNode) log).getLogs(level, true));
            }
        }

        return levelLogs;
    }

    /**
     * Filter logs of a specific level.
     *
     * @param level the level of the logs to return
     * @param recurse if one of the {@link LoggingEvent} is a node look at its children too etc.
     * @return the filtered logs
     */
    public List<LoggingEvent> getLogsFrom(Level level, boolean recurse)
    {
        List<LoggingEvent> levelLogs = new LinkedList<LoggingEvent>();

        for (LoggingEvent log : this) {
            if (log.getLevel().compareTo(level) <= 0) {
                levelLogs.add(log);
            }

            if (recurse && log instanceof LoggingEventTreeNode) {
                levelLogs.addAll(((LoggingEventTreeNode) log).getLogsFrom(level, true));
            }
        }

        return levelLogs;
    }
}
