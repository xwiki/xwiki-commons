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

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LogbackEventGenerator}.
 *
 * @version $Id$
 * @since 3.2M3
 */
@ComponentTest
@ComponentList({DefaultObservationManager.class, LogbackEventGenerator.class})
public class LogbackEventGeneratorTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(org.xwiki.test.LogLevel.INFO);

    private Logger logger;

    private ObservationManager observationManager;

    @BeforeEach
    public void beforeEach() throws Exception
    {
        this.observationManager = this.componentManager.getInstance(ObservationManager.class);

        this.logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Verify that logging an error will generate a Log Event.
     */
    @Test
    public void verifyThatLoggingGeneratesALogEvent()
    {
        Event event = new LogEvent();

        EventListener listener = mock(EventListener.class);
        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(event));

        this.observationManager.addListener(listener);

        this.logger.error("error message");

        Event expected = new LogEvent(null, LogLevel.ERROR, "error message", null, null);
        verify(listener).onEvent(eq(expected), eq(getClass().getName()), eq(null));
        assertEquals("error message", this.logCapture.getMessage(0));
    }

    @Test
    public void initializeWhenNoLogback() throws Exception
    {
        // Simulate that the Logging implementation is not Logback
        LogbackEventGenerator generator =
            (LogbackEventGenerator) this.componentManager.getInstance(EventListener.class, "LogbackEventGenerator");
        LogbackEventGenerator spyGenerator = spy(generator);
        when(spyGenerator.getRootLogger()).thenReturn(null);

        spyGenerator.initialize();

        assertEquals(1, this.logCapture.size());
        assertEquals("Could not find any Logback root logger. The logging module won't be able to catch " + "logs.",
            this.logCapture.getMessage(0));
    }

    /**
     * Verify that LogbackEventGenerator can produce two events on two different threads at the same time.
     */
    @Test
    public void unsynchronized() throws ComponentLookupException, InterruptedException
    {
        EventListener listener = mock(EventListener.class);
        when(listener.getName()).thenReturn("mylistener");
        when(listener.getEvents()).thenReturn(Arrays.asList(new LogEvent()));

        this.observationManager.addListener(listener);

        // Make the listener wait
        Lock lock = new ReentrantLock();
        lock.lock();
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                lock.lock();
                lock.unlock();

                return null;
            }
        }).when(listener).onEvent(any(), any(), any());

        CompletableFuture.runAsync(() -> this.logger.error("thread1 error message"));
        CompletableFuture.runAsync(() -> this.logger.error("thread2 error message"));

        // Make threads have enough time to send the events
        Thread.sleep(100);

        // Make sure both thread send the log
        verify(listener, times(2)).onEvent(any(), any(), any());

        // Release the threads
        lock.unlock();

        // We don't really care about the actual log
        this.logCapture.ignoreAllMessages();
    }
}
