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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.event.LogQueueListener;
import org.xwiki.logging.internal.tail.XStreamFileLoggerTail;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.test.XWikiTempDirUtil;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.xstream.internal.SafeXStream;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultLoggerManager}.
 *
 * @version $Id$
 * @since 3.2M3
 */
@ComponentList({ DefaultLoggerManager.class, DefaultObservationManager.class, LogbackEventGenerator.class,
    XStreamFileLoggerTail.class })
public class DefaultLoggerManagerTest
{
    @Rule
    public final MockitoComponentMockingRule<DefaultLoggerManager> mocker =
        new MockitoComponentMockingRule<DefaultLoggerManager>(DefaultLoggerManager.class);

    private DefaultLoggerManager loggerManager;

    private Logger logger;

    private ListAppender<ILoggingEvent> listAppender;

    private LogbackUtils utils = new LogbackUtils();

    @Before
    public void setUp() throws Exception
    {
        ch.qos.logback.classic.Logger rootLogger = this.utils.getRootLogger();

        // Disable all appenders to avoid unnecessary log
        Filter<ILoggingEvent> filter = new Filter<ILoggingEvent>()
        {
            @Override
            public FilterReply decide(ILoggingEvent event)
            {
                if (event.getMessage() != null && event.getMessage().startsWith("[test]")) {
                    return FilterReply.DENY;
                }

                return FilterReply.NEUTRAL;
            }
        };
        Iterator<Appender<ILoggingEvent>> appendersIterator = rootLogger.iteratorForAppenders();
        while (appendersIterator.hasNext()) {
            Appender<ILoggingEvent> appender = appendersIterator.next();
            appender.addFilter(filter);
        }

        // Add appender
        this.listAppender = new ListAppender<ILoggingEvent>();
        this.listAppender.start();
        rootLogger.addAppender(this.listAppender);

        this.logger = LoggerFactory.getLogger(getClass());
        this.loggerManager = this.mocker.getComponentUnderTest();
    }

    @Test
    public void testPushPopLogListener() throws InterruptedException
    {
        this.logger.error("[test] before push");

        // Make sure the log has been sent to the logback appender
        Assert.assertEquals("[test] before push", this.listAppender.list.get(0).getMessage());

        LogQueue queue = new LogQueue();

        this.loggerManager.pushLogListener(new LogQueueListener("loglistenerid", queue));

        this.logger.error("[test] after push");

        // Make sure the log has been added to the queue
        Assert.assertEquals("[test] after push", queue.poll().getMessage());

        // Make sure the log has not been sent to the logback appender
        Assert.assertEquals(1, this.listAppender.list.size());

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DefaultLoggerManagerTest.this.logger.error("[test] other thread");
            }
        });
        thread.start();
        thread.join();

        // Make sure the log has been sent to the logback appender
        Assert.assertEquals("[test] other thread", this.listAppender.list.get(1).getMessage());

        this.logger.error(org.xwiki.logging.Logger.ROOT_MARKER, "[test] root log");

        // Make sure the log has been added to the queue
        Assert.assertEquals("[test] root log", queue.poll().getMessage());
        // Make sure the log also been sent to the logback appender
        Assert.assertEquals("[test] root log", this.listAppender.list.get(2).getMessage());

        this.loggerManager.popLogListener();

        this.logger.error("[test] after pop");

        Assert.assertTrue(queue.isEmpty());
        Assert.assertEquals("[test] after pop", this.listAppender.list.get(3).getMessage());
    }

    @Test
    public void testStackedListeners()
    {
        this.logger.error("[test] before push");

        // Make sure the log has been sent to the logback appender
        Assert.assertEquals("[test] before push", this.listAppender.list.get(0).getMessage());

        LogQueue queue1 = new LogQueue();

        this.loggerManager.pushLogListener(new LogQueueListener("loglistenerid1", queue1));

        LogQueue queue2 = new LogQueue();

        this.loggerManager.pushLogListener(new LogQueueListener("loglistenerid2", queue2));

        this.logger.error("[test] log queue2");

        // Make sure the log has not been sent to the stacked listener
        Assert.assertTrue(queue1.isEmpty());

        // Make sure the log has been sent to the current listener
        Assert.assertEquals("[test] log queue2", queue2.poll().getMessage());

        this.loggerManager.popLogListener();

        this.logger.error("[test] log queue1");

        // Make sure the log has been sent to the current listener
        Assert.assertEquals("[test] log queue1", queue1.poll().getMessage());

        this.loggerManager.popLogListener();
    }

    @Test
    public void testNullListeners()
    {
        this.logger.error("[test] before push");

        // Make sure the log has been sent to the logback appender
        Assert.assertEquals("[test] before push", this.listAppender.list.get(0).getMessage());

        this.loggerManager.pushLogListener(null);

        this.logger.error("[test] log to null");

        // Make sure the log has not been sent to the logback appender
        Assert.assertEquals(1, this.listAppender.list.size());

        this.loggerManager.popLogListener();

        this.logger.error("[test] after pop");

        // Make sure the log has been sent to the logback appender
        Assert.assertEquals("[test] after pop", this.listAppender.list.get(1).getMessage());
    }

    @Test
    public void testGetSetLoggerLevel()
    {
        assertNull(this.loggerManager.getLoggerLevel(getClass().getName()));

        LogQueue queue = new LogQueue();

        this.loggerManager.pushLogListener(new LogQueueListener("loglistenerid", queue));

        this.loggerManager.setLoggerLevel(getClass().getName(), LogLevel.WARN);
        assertSame(LogLevel.WARN, this.loggerManager.getLoggerLevel(getClass().getName()));

        this.logger.debug("[test] debug message 1");
        // Provide information when the Assert fails
        if (queue.size() > 0) {
            Assert.fail("Should have contained no message but got [" + queue.peek().getFormattedMessage()
                + "] instead (last message, there might be more)");
        }
        assertEquals(0, queue.size());

        this.loggerManager.setLoggerLevel(getClass().getName(), LogLevel.DEBUG);
        assertSame(LogLevel.DEBUG, this.loggerManager.getLoggerLevel(getClass().getName()));

        this.logger.debug("[test] debug message 2");
        assertEquals(1, queue.size());

        this.loggerManager.setLoggerLevel(getClass().getName(), null);
        assertNull(this.loggerManager.getLoggerLevel(getClass().getName()));
    }

    @Test
    public void testGetLoggers()
    {
        assertNotNull(this.loggerManager.getLoggers());
    }

    @Test
    public void initializeWhenNoLogback() throws Exception
    {
        // Simulate that the Logging implementation is not Logback
        DefaultLoggerManager spyLoggerManager = spy(this.loggerManager);
        when(spyLoggerManager.getRootLogger()).thenReturn(null);

        spyLoggerManager.initialize();

        verify(this.mocker.getMockedLogger())
            .warn("Could not find any Logback root logger. All logging module advanced features will be disabled.");
    }

    @Test
    public void getLoggerLevelWhenNoLogback() throws Exception
    {
        // Simulate that the Logging implementation is not Logback
        DefaultLoggerManager spyLoggerManager = spy(this.loggerManager);
        when(spyLoggerManager.getLoggerContext()).thenReturn(null);

        Assert.assertNull(spyLoggerManager.getLoggerLevel("whatever"));
    }

    @Test
    public void createLoggerTail() throws Exception
    {
        this.mocker.registerMockComponent(SafeXStream.class);

        File logFile = new File(XWikiTempDirUtil.createTemporaryDirectory(), "log");

        Assert.assertTrue(this.loggerManager.createLoggerTail(logFile.toPath(), true) instanceof XStreamFileLoggerTail);
    }
}
