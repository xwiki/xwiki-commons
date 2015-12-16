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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.internal.DefaultObservationManager;
import org.xwiki.test.AllLogRule;
import org.xwiki.test.ComponentManagerRule;
import org.xwiki.test.annotation.ComponentList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LogbackEventGenerator}.
 *
 * @version $Id$
 * @since 3.2M3
 */
@ComponentList({
    DefaultObservationManager.class,
    LogbackEventGenerator.class
})
public class LogbackEventGeneratorTest
{
    @Rule
    public final ComponentManagerRule componentManager = new ComponentManagerRule();

    @Rule
    public final AllLogRule logCapture = new AllLogRule();

    private Logger logger;

    private ObservationManager observationManager;

    @Before
    public void setUp() throws Exception
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
        Event event = new LogEvent(null, LogLevel.INFO, "dummy", null, null);

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

        Assert.assertEquals(1, this.logCapture.size());
        Assert.assertEquals("Could not find any Logback root logger. The logging module won't be able to catch "
            + "logs.", this.logCapture.getMessage(0));
    }
}
