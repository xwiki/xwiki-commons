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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.read.ListAppender;

/**
 * Allow capturing Logs output during the execution of the unit test. This is useful for two reasons:
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
 *  &#64;Rule public AllLogRule logRule = new AllLogRule();
 * }</pre>
 *
 * @version $Id$
 * @since 7.0M1
 */
public class AllLogRule implements TestRule
{
    /**
     * The log output is captured in a Logback ListAppender.
     */
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    private final Set<Integer> assertedMessages = new HashSet<>();

    private LogLevel level;

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

            boolean validate = true;
            try {
                // Run the test
                this.statement.evaluate();
            } catch (Throwable t) {
                // Don't verify anything if the test did not pass in the first place
                validate = false;

                throw t;
            } finally {
                // Remove Logback setup
                after(validate);
            }
        }

        /**
         * Setup Logback capturing.
         */
        private void before() throws Throwable
        {
            initializeLoggers();
            listAppender.start();
        }

        /**
         * Stop Logback capturing.
         */
        private void after(boolean verify) throws Throwable
        {
            listAppender.stop();
            uninitializeLogger(verify);
        }
    }

    /**
     * Capture INFO log.
     */
    public AllLogRule()
    {
        this(LogLevel.INFO);
    }

    /**
     * Caputure passed log level.
     * 
     * @param level the level of log to capture
     */
    public AllLogRule(LogLevel level)
    {
        this.level = level;
    }
    
    @Override
    public Statement apply(Statement statement, Description description)
    {
        return new LogStatement(statement);
    }

    private ILoggingEvent getLogEvent(int position)
    {
        List<ILoggingEvent> list = this.listAppender.list;
        if (list.size() <= position) {
            throw new RuntimeException(String.format("There are only %s messages in the captured logs", list.size()));
        }

        this.assertedMessages.add(position);

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

    private void initializeLoggers()
    {
        // Reinitialize completely Logback
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        // Configure the root logger to use our list appender and to log at the level asked.
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(this.listAppender);
        logger.setLevel(this.level.getLevel());
    }

    private void uninitializeLogger(boolean verify) throws Exception
    {
        // Reinitialize Logback (by reading its config from the logback-test.xml file in the classpath)
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        ContextInitializer initializer = new ContextInitializer(context);
        initializer.autoConfig();

        // Verify that all appender list messages have been asserted.
        if (verify && this.listAppender.list.size() != this.assertedMessages.size()) {
            throw new AssertionError("All messages must be asserted!");
        }
    }
}
