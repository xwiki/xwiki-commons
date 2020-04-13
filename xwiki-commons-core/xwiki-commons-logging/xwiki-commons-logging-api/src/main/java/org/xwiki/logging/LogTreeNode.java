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
package org.xwiki.logging;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.xwiki.logging.event.BeginLogEvent;
import org.xwiki.logging.event.LogEvent;

/**
 * Logs organized as a tree.
 *
 * @version $Id$
 * @since 5.4M1
 */
public class LogTreeNode extends BeginLogEvent implements Iterable<LogEvent>, Serializable
{
    private static class LogTreeNodeIterator implements Iterator<LogEvent>
    {
        private final Iterator<LogEvent> rootIterator;

        private Iterator<LogEvent> currentIterator;

        LogTreeNodeIterator(Iterator<LogEvent> rootIterator)
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
        public LogEvent next()
        {
            if (!this.currentIterator.hasNext()) {
                this.currentIterator = this.rootIterator;
            }

            LogEvent logEvent = this.currentIterator.next();

            if (this.currentIterator == this.rootIterator && logEvent instanceof LogTreeNode) {
                this.currentIterator = ((LogTreeNode) logEvent).iterator(true);
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
    protected ConcurrentLinkedQueue<LogEvent> children;

    // Iterable

    LogTreeNode()
    {

    }

    /**
     * @param logEvent the log event to copy
     */
    public LogTreeNode(LogEvent logEvent)
    {
        super(logEvent);
    }

    @Override
    public Iterator<LogEvent> iterator()
    {
        return this.children != null ? this.children.iterator() : Collections.<LogEvent>emptyList().iterator();
    }

    /**
     * @param recurse if true navigate through the whole tree, otherwise only the first level
     * @return an iterator over a tree of logs
     */
    public Iterator<LogEvent> iterator(boolean recurse)
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

        for (LogEvent logEvent : this) {
            ++size;

            if (logEvent instanceof LogTreeNode) {
                size += ((LogTreeNode) logEvent).size(true);
            }
        }

        return size;
    }

    /**
     * @param logEvent the log event to store
     */
    void add(LogEvent logEvent)
    {
        if (this.children == null) {
            this.children = new ConcurrentLinkedQueue<>();
        }

        this.children.add(logEvent);
    }

    /**
     * Filter logs of a specific level.
     *
     * @param level the level of the logs to return
     * @param recurse if one of the {@link LogEvent} is a node look at its children too etc.
     * @return the filtered logs
     */
    public List<LogEvent> getLogs(LogLevel level, boolean recurse)
    {
        List<LogEvent> levelLogs = new LinkedList<>();

        for (LogEvent log : this) {
            if (log.getLevel() == level) {
                levelLogs.add(log);
            }

            if (recurse && log instanceof LogTreeNode) {
                levelLogs.addAll(((LogTreeNode) log).getLogs(level, true));
            }
        }

        return levelLogs;
    }

    /**
     * Filter logs of a specific level.
     *
     * @param level the level of the logs to return
     * @param recurse if one of the {@link LogEvent} is a node look at its children too etc.
     * @return the filtered logs
     */
    public List<LogEvent> getLogsFrom(LogLevel level, boolean recurse)
    {
        List<LogEvent> levelLogs = new LinkedList<>();

        for (LogEvent log : this) {
            if (log.getLevel().compareTo(level) <= 0) {
                levelLogs.add(log);
            }

            if (recurse && log instanceof LogTreeNode) {
                levelLogs.addAll(((LogTreeNode) log).getLogsFrom(level, true));
            }
        }

        return levelLogs;
    }
}
