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

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.test.AbstractComponentTestCase;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;

public class DefaultLoggerManagerTest extends AbstractComponentTestCase
{
    private Logger logger;

    private Logger logger2;

    private LoggerManager loggerManager;

    private ListAppender<ILoggingEvent> listAppender;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        ch.qos.logback.classic.Logger rootLogger = LogbackUtils.getRootLogger();

        // Disable all appenders to havoid unnecessary log
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

        this.loggerManager = getComponentManager().lookup(LoggerManager.class);

        this.logger = LoggerFactory.getLogger(getClass());
        this.logger2 = LoggerFactory.getLogger(getClass());
    }

    @Test
    public void test() throws InterruptedException
    {
        this.logger.error("[test] before push");

        // Make sure the log has been sent to the logback appender
        Assert.assertEquals("[test] before push", this.listAppender.list.get(0).getMessage());

        LogQueue queue = new LogQueue();

        this.loggerManager.puchLogQueue(queue);

        this.logger.error("[test] after push");

        // Make sure the log has been added to the queue
        Assert.assertEquals("[test] after push", queue.poll().getMessage());

        // Make sure the log has not been sent to the logback appender
        Assert.assertTrue(this.listAppender.list.size() == 1);

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                logger2.error("[test] other thread");
            }
        });
        thread.start();
        thread.join();

        // Make sure the log has been sent to the logback appender
        Assert.assertEquals("[test] other thread", this.listAppender.list.get(1).getMessage());

        this.loggerManager.popLogListener();

        this.logger.error("[test] after pop");

        Assert.assertTrue(queue.isEmpty());
        Assert.assertEquals("[test] after pop", this.listAppender.list.get(2).getMessage());
    }
}
