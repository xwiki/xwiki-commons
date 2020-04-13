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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Allow capturing Logs output in the Class under test during unit testing. This is useful for two reasons:
 * <ul>
 *   <li>it allows not outputting log messages in the console which is a bad practice. When a test run it should not
 *       output anything and if it needs to assert something, it has to be done in the test itself.</li>
 *   <li>it allows to assert the output log messages</li>
 * </ul>
 * This code was inspired by code written by Christian Baranowski in a
 * <a href="http://tux2323.blogspot.fr/2011/06/test-logging-via-junit-rule.html">blog</a> post.
 * <p>
 * Example usage:
 * <pre>{@code
 *  &#64;Rule public LogRule logRule = new LogRule() {{
 *      record(LogLevel.WARN);
 *      recordLoggingForType(RestrictParseLocationEventHandler.class);
 *  }};
 * }</pre>
 *
 * @version $Id$
 * @since 4.2RC1
 * @deprecated starting with 7.0M1 you should use {@link AllLogRule} instead since we want tests to not output anything
 *             to the console
 */
@Deprecated
public class LogRule implements TestRule
{
    /**
     * The log output is captured in a Logback ListAppender.
     */
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    /**
     * The log level below which we do the capture. By default we capture everything.
     */
    private LogLevel level = LogLevel.TRACE;

    /**
     * Saved logging levels for existing Loggers. We save them so that we can restore them at the end of the
     * test. This is important so that changes are not carried over from one unit test to another...
     */
    private Map<Class<?>, Level> savedLevels = new HashMap<>();

    /**
     * Saved logger's additivities so that we can restore them at the end of the test.
     */
    private Map<Class<?>, Boolean> savedAdditivities = new HashMap<>();

    /**
     * The Logger classes for which to capture logs.
     */
    private final List<Class> loggingSources = new ArrayList<>();

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
        LogLevel(Level level)
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

            try {
                // Run the test
                this.statement.evaluate();
            } finally {
                // Remove Logback setup
                after();
            }
        }

        /**
         * Setup Logback capturing.
         */
        private void before()
        {
            listAppender.start();
        }

        /**
         * Stop Logback capturing.
         */
        private void after()
        {
            listAppender.stop();
            for (Class logSource : loggingSources) {
                uninitializeLogger(logSource);
            }
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
        initializeLogger(type);
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

    private ILoggingEvent getLogEvent(int position)
    {
        List<ILoggingEvent> list = this.listAppender.list;
        if (list.size() <= position) {
            throw new RuntimeException(String.format("There are only %s messages in the captured logs", list.size()));
        }

        return list.get(position);
    }

    /**
     * @param position the message number in the list of captured logs
     * @return the message at the specified position
     */
    public String getMessage(int position)
    {
        return getLogEvent(position).getFormattedMessage();
    }

    /**
     * @param position the message number in the list of captured logs
     * @return the marker at the specified position
     * @since 7.0M2
     */
    public Marker getMarker(int position)
    {
        return getLogEvent(position).getMarker();
    }

    /**
     * @return the number of log messages that have been captured
     */
    public int size()
    {
        return listAppender.list.size();
    }

    /**
     * @param type the logging class to add to the capturing list appender and for which to set the asked logging level
     */
    private void initializeLogger(Class<?> type)
    {
        Logger logger = (Logger) LoggerFactory.getLogger(type);
        logger.addAppender(this.listAppender);
        this.savedLevels.put(type, logger.getLevel());
        this.savedAdditivities.put(type, logger.isAdditive());
        logger.setLevel(this.level.getLevel());
        // Make sure only our new appender is used (and parent's appenders are not used) so that we don't generate logs
        // elsewhere (console, file, etc).
        logger.setAdditive(false);
    }

    /**
     * @param type the logging class from which to remove the capturing list appender and for which to put back the
     *        logging level as before
     */
    private void uninitializeLogger(Class<?> type)
    {
        Logger logger = (Logger) LoggerFactory.getLogger(type);
        logger.detachAppender(this.listAppender);
        logger.setLevel(this.savedLevels.get(type));
        logger.setAdditive(this.savedAdditivities.get(type));
    }
}
