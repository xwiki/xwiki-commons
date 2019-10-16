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
package org.xwiki.logging.logback.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.internal.tail.XStreamFileLoggerTail;
import org.xwiki.logging.tail.LoggerTail;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.WrappedThreadEventListener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * Default implementation of {@link LoggerManager}.
 *
 * @version $Id$
 * @since 3.2M3
 */
@Component
@Singleton
public class DefaultLoggerManager implements LoggerManager, Initializable
{
    /**
     * Used to register/unregister {@link org.xwiki.logging.event.LogEvent} listeners.
     */
    @Inject
    private ObservationManager observation;

    @Inject
    private Provider<XStreamFileLoggerTail> loggerTailProvider;

    /**
     * The logger.
     */
    @Inject
    private Logger logger;

    /**
     * The stack of listeners for the current thread.
     */
    private ThreadLocal<Deque<EventListener>> listeners = new ThreadLocal<>();

    /**
     * Logback utilities.
     */
    private LogbackUtils utils = new LogbackUtils();

    /**
     * Filter forbidden threads for {@link Appender}s.
     */
    private ForbiddenThreadsFilter forbiddenThreads = new ForbiddenThreadsFilter();

    @Override
    public void initialize() throws InitializationException
    {
        // Register appender
        ch.qos.logback.classic.Logger rootLogger = getRootLogger();

        if (rootLogger != null) {
            Iterator<Appender<ILoggingEvent>> iterator = rootLogger.iteratorForAppenders();

            while (iterator.hasNext()) {
                Appender<ILoggingEvent> appender = iterator.next();

                if (!(appender instanceof LogbackEventGenerator)) {
                    appender.addFilter(this.forbiddenThreads);
                }
            }
        } else {
            this.logger
                .warn("Could not find any Logback root logger. All logging module advanced features will be disabled.");
        }
    }

    @Override
    public void pushLogListener(EventListener listener)
    {
        Deque<EventListener> listenerStack = this.listeners.get();

        if (listenerStack == null) {
            listenerStack = new LinkedList<>();
            this.listeners.set(listenerStack);
        }

        if (!listenerStack.isEmpty()) {
            this.observation.removeListener(listenerStack.peek().getName());
        }

        if (listener != null) {
            this.observation.addListener(new WrappedThreadEventListener(listener));
        }
        if (listenerStack.isEmpty()) {
            grabLog(Thread.currentThread());
        }
        listenerStack.push(listener);
    }

    @Override
    public EventListener popLogListener()
    {
        Deque<EventListener> listenerStack = this.listeners.get();

        EventListener listener;
        if (listenerStack != null && !listenerStack.isEmpty()) {
            listener = listenerStack.pop();
            if (listener != null) {
                this.observation.removeListener(listener.getName());
            }
            if (listenerStack.isEmpty()) {
                ungrabLog(Thread.currentThread());
            } else {
                EventListener topListener = listenerStack.peek();
                if (topListener != null) {
                    this.observation.addListener(new WrappedThreadEventListener(topListener));
                }
            }
        } else {
            listener = null;
        }

        return listener;
    }

    /**
     * Isolate all appender from provided thread except the event generator one.
     *
     * @param thread the thread to remove from the log appender
     */
    private void grabLog(Thread thread)
    {
        this.forbiddenThreads.addThread(thread);
    }

    /**
     * Restore all appender for the current provided thread.
     *
     * @param thread the thread to restore in the log appender
     */
    private void ungrabLog(Thread thread)
    {
        this.forbiddenThreads.removeThread(thread);
    }

    @Override
    public void setLoggerLevel(String loggerName, LogLevel logLevel)
    {
        LoggerContext loggerContext = this.utils.getLoggerContext();

        if (loggerContext != null) {
            ch.qos.logback.classic.Logger askedLogger = loggerContext.getLogger(loggerName);
            askedLogger.setLevel(this.utils.toLevel(logLevel));
        }
    }

    @Override
    public LogLevel getLoggerLevel(String loggerName)
    {
        LoggerContext loggerContext = getLoggerContext();

        if (loggerContext != null) {
            ch.qos.logback.classic.Logger askedLogger = loggerContext.exists(loggerName);

            if (askedLogger != null) {
                return this.utils.toLogLevel(askedLogger.getLevel());
            }
        }

        return null;
    }

    @Override
    public Collection<Logger> getLoggers()
    {
        return (Collection) this.utils.getLoggerContext().getLoggerList();
    }

    /**
     * @return the Logback root logger or null if Logback is not available
     */
    protected ch.qos.logback.classic.Logger getRootLogger()
    {
        return this.utils.getRootLogger();
    }

    /**
     * @return the Logback Context or null if Logback is not available
     */
    protected LoggerContext getLoggerContext()
    {
        return this.utils.getLoggerContext();
    }

    @Override
    public LoggerTail createLoggerTail(Path path, boolean readonly) throws IOException
    {
        if (readonly && !XStreamFileLoggerTail.exist(path)) {
            return new LogQueue();
        } else {
            XStreamFileLoggerTail loggerTail = this.loggerTailProvider.get();

            loggerTail.initialize(path, readonly);

            return loggerTail;
        }
    }
}
