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
package org.xwiki.test.junit5;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.xwiki.test.LogLevel;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.read.ListAppender;

/**
 * Captures any logging done by code being tested in order for the logging to not leak in the console and to be able
 * to assert the content of the logging statements.
 * <p/>
 * Usage:
 * <ul>
 *   <li>Must be used with {@code @RegisterExtension} and ujsing a {@code static} variable</li>
 *   <li>If you want to remove the {@code static} keyword, you'll need to annotate the test class with
 *       {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}</li>
 * </ul>
 * Example:
 * <pre>
 * {@code
 * &#64;RegisterExtension
 * static LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);
 * }
 * </pre>
 *
 * @version $Id$
 * @since 10.4RC1
 */
public class LogCaptureExtension implements BeforeEachCallback, AfterEachCallback
{
    /**
     * The log output is captured in a Logback ListAppender.
     */
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    private final Set<Integer> assertedMessages = new HashSet<>();

    private LogLevel level;

    /**
     * @param level the logging level from which to start capturing logs (for example if {@link LogLevel#INFO then
     *              INFO, WARN, ERROR, etc are captured too).
     */
    public LogCaptureExtension(LogLevel level)
    {
        this.level = level;
    }

    /**
     * Captures all logs from severity {@link LogLevel#INFO} and higher (WARN, ERROR, etc).
     */
    public LogCaptureExtension()
    {
        this.level = LogLevel.INFO;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception
    {
        initializeLoggers();
        this.listAppender.start();

    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception
    {
        this.listAppender.stop();
        uninitializeLogger();
    }

    /**
     * @param position the message number in the list of captured logs
     * @return the logging event corresponding to the message, allowing to get information such as the level, the
     *         marker, the formatted string, etc
     */
    public ILoggingEvent getLogEvent(int position)
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
     * Voluntarily ignore all messages to signify they should not need to be asserted.
     */
    public void ignoreAllMessages()
    {
        for (int i = 0; i < size(); i++) {
            getLogEvent(i);
        }
    }

    /**
     * Voluntarily ignore a message to signify it should not need to be asserted.
     *
     * @param position the message number in the list of captured logs
     */
    public void ignoreMessage(int position)
    {
        getLogEvent(position);
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

    private void uninitializeLogger() throws Exception
    {
        // Reinitialize Logback (by reading its config from the logback-test.xml file in the classpath)
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        ContextInitializer initializer = new ContextInitializer(context);
        initializer.autoConfig();

        // Verify that all appender list messages have been asserted.
        if (this.listAppender.list.size() != this.assertedMessages.size()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < this.listAppender.list.size(); i++) {
                // Has the message been asserted already?
                if (!this.assertedMessages.contains(i)) {
                    builder.append(getMessage(i)).append('\n');
                }
            }
            throw new AssertionError(String.format("Following messages must be asserted: [%s]", builder.toString()));
        }
    }
}
