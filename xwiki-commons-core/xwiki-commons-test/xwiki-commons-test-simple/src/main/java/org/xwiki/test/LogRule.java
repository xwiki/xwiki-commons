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
package org.xwiki.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Allow capturing Logs output in the Class under test during unit testing. This is useful for two reasons:
 * <ul>
 *   <li>it allows not outputting log messages in the console which is a bad practice. When a test run it should not
 *       output anything and if it needs to assert something, it has to be done in the test itself.</li>
 *   <li>it allows to assert the output log messages</li>
 * </ul>
 * <p/>
 * This code was inspired by code written by Christian Baranowski in a
 * <a href="http://tux2323.blogspot.fr/2011/06/test-logging-via-junit-rule.html">blog</a> post.
 * <p/>
 * Example usage:
 * <code><pre>
 *  &#64;Rule public LogRule logRule = new LogRule() {{
 *      record(LogLevel.WARN);
 *      recordLoggingForType(RestrictParseLocationEventHandler.class);
 *  }};
 * </pre></code>
 *
 * @version $Id$
 * @since 4.2RC1
 */
public class LogRule implements TestRule
{
    /**
     * The log output is captured in a Logback ListAppender.
     */
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();

    /**
     * The log level below which we do the capture. By default we capture everything.
     */
    private LogLevel level = LogLevel.TRACE;

    /**
     * The Logger classes for which to capture logs.
     */
    private final List<Class> loggingSources = new ArrayList<Class>();

    /**
     * Helper class to represent Logging levels to capture.
     */
    public enum LogLevel
    {
        /**
         * Trace level.
         */
        TRACE(Level.TRACE),

        /**
         * Debug level.
         */
        DEBUG(Level.DEBUG),

        /**
         * Info level.
         */
        INFO(Level.INFO),

        /**
         * Warn level.
         */
        WARN(Level.WARN),

        /**
         * Error level.
         */
        ERROR(Level.ERROR);

        /**
         * @see #LogLevel(ch.qos.logback.classic.Level)
         */
        private Level internalLevel;

        /**
         * @param level see {@link #getLevel()}
         */
        private LogLevel(Level level)
        {
            this.internalLevel = level;
        }

        /**
         * @return the log level to capture
         */
        public Level getLevel()
        {
            return this.internalLevel;
        }
    }

    /**
     * The actual code that executes our capturing logic before the test runs and removes it after it has run.
     */
    public class LogStatement extends Statement
    {
        /**
         * @see #LogStatement(org.junit.runners.model.Statement)
         */
        private final Statement statement;

        /**
         * The Logback logging context that we use to prevent any logs from being output (apart from the logs we
         * capture ourselves).
         */
        private final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        /**
         * @param statement the wrapping statement that we save so that we can execute it (the statement represents
         *        the test to execute).
         */
        public LogStatement(Statement statement)
        {
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable
        {
            // Setup Logback to catch log calls
            before();

            // Run the test
            this.statement.evaluate();

            // Remove Logback setup
            after();
        }

        /**
         * Setup Logback capturing.
         */
        private void before()
        {
            resetLoggingContext();
            for (Class logSource : loggingSources) {
                addAppenderToType(logSource);
            }
            listAppender.start();
        }

        /**
         * Stop Logback capturing.
         */
        private void after()
        {
            listAppender.stop();
            resetLoggingContext();
        }

        /**
         * Prevent normal logs to be output.
         */
        private void resetLoggingContext()
        {
            lc.reset();
        }
    }

    @Override
    public Statement apply(Statement statement, Description description)
    {
        return new LogStatement(statement);
    }

    /**
     * @param level the log level to capture
     */
    public void record(LogLevel level)
    {
        this.level = level;
    }

    /**
     * @param type the logging class type for which to capture logs
     */
    public void recordLoggingForType(Class<?> type)
    {
        this.loggingSources.add(type);
        addAppenderToType(type);
    }

    /**
     * @param logOutput the log output to match in the captured data
     * @return true if the passed text was logged or false otherwise
     */
    public boolean contains(String logOutput)
    {
        List<ILoggingEvent> list = this.listAppender.list;
        for (ILoggingEvent event : list) {
            if (event.getFormattedMessage().contains(logOutput)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param position the message number in the list of captured logs
     * @return the message at the specified position
     */
    public String getMessage(int position)
    {
        List<ILoggingEvent> list = this.listAppender.list;
        if (list.size() >= position + 1) {
            return list.get(position).getFormattedMessage();
        } else {
            throw new RuntimeException(String.format("There are only %s messages in the captured logs", list.size()));
        }
    }

    /**
     * @return the number of log messages that have been captured
     */
    public int size()
    {
        return listAppender.list.size();
    }

    /**
     * @param type the logging class to add to the capturing list
     */
    private void addAppenderToType(Class<?> type)
    {
        Logger logger = (Logger) LoggerFactory.getLogger(type);
        logger.addAppender(this.listAppender);
        logger.setLevel(this.level.getLevel());
    }
}
