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

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.xwiki.logging.event.LogEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link LogQueue}.
 *
 * @version $Id$
 */
public class LogQueueTest
{
    @Test
    public void error()
    {
        LogQueue queue = new LogQueue();
        LogEvent logEvent;

        queue.error("");
        logEvent = queue.poll();
        assertEquals(Arrays.asList(""), logEvent.getMessageElements());
        assertEquals("", logEvent.getFormattedMessage());

        queue.error("message");
        assertEquals("message", queue.poll().getFormattedMessage());

        queue.error("message {}", "param");
        assertEquals("message param", queue.poll().getFormattedMessage());

        queue.error("message {} {}", "param1", "param2");
        logEvent = queue.poll();
        assertEquals("message param1 param2", logEvent.getFormattedMessage());
        assertEquals(Arrays.asList("message ", " ", ""), logEvent.getMessageElements());
        assertEquals(Arrays.asList("param1", "param2"), Arrays.asList(logEvent.getArgumentArray()));

        queue.error("message {}", "param1", new Exception());
        logEvent = queue.poll();
        assertEquals("message param1", logEvent.getFormattedMessage());
        assertEquals(Arrays.asList("message ", ""), logEvent.getMessageElements());
        assertEquals(Arrays.asList("param1"), Arrays.asList(logEvent.getArgumentArray()));
        assertNotNull(logEvent.getThrowable());
        assertNull(logEvent.getTranslationKey());

        queue.error("message {}", new Object[] { "param1", new Exception() });
        logEvent = queue.poll();
        assertEquals("message param1", logEvent.getFormattedMessage());
        assertEquals(Arrays.asList("message ", ""), logEvent.getMessageElements());
        assertEquals(Arrays.asList("param1"), Arrays.asList(logEvent.getArgumentArray()));
        assertNotNull(logEvent.getThrowable());
        assertNull(logEvent.getTranslationKey());

        queue.error(new TranslationMarker("translation.key"), "message");
        logEvent = queue.poll();
        assertEquals("translation.key", logEvent.getTranslationKey());
        assertEquals(Arrays.asList("message"), logEvent.getMessageElements());
        assertEquals(Arrays.asList(), Arrays.asList(logEvent.getArgumentArray()));
    }

    @Test
    public void containLogsFrom()
    {
        LogQueue queue = new LogQueue();

        assertFalse(queue.containLogsFrom(LogLevel.TRACE));
        assertFalse(queue.containLogsFrom(LogLevel.DEBUG));
        assertFalse(queue.containLogsFrom(LogLevel.INFO));
        assertFalse(queue.containLogsFrom(LogLevel.WARN));
        assertFalse(queue.containLogsFrom(LogLevel.ERROR));

        queue.warn("");

        assertTrue(queue.containLogsFrom(LogLevel.TRACE));
        assertTrue(queue.containLogsFrom(LogLevel.DEBUG));
        assertTrue(queue.containLogsFrom(LogLevel.INFO));
        assertTrue(queue.containLogsFrom(LogLevel.WARN));
        assertFalse(queue.containLogsFrom(LogLevel.ERROR));
    }

    @Test
    public void getLogsFrom()
    {
        LogQueue queue = new LogQueue();

        assertTrue(queue.getLogsFrom(LogLevel.TRACE).isEmpty());
        assertTrue(queue.getLogsFrom(LogLevel.DEBUG).isEmpty());
        assertTrue(queue.getLogsFrom(LogLevel.INFO).isEmpty());
        assertTrue(queue.getLogsFrom(LogLevel.WARN).isEmpty());
        assertTrue(queue.getLogsFrom(LogLevel.ERROR).isEmpty());

        queue.warn("");

        assertFalse(queue.getLogsFrom(LogLevel.TRACE).isEmpty());
        assertTrue(queue.hasLogLevel(LogLevel.TRACE));
        assertFalse(queue.getLogsFrom(LogLevel.DEBUG).isEmpty());
        assertTrue(queue.hasLogLevel(LogLevel.DEBUG));
        assertFalse(queue.getLogsFrom(LogLevel.INFO).isEmpty());
        assertTrue(queue.hasLogLevel(LogLevel.INFO));
        assertFalse(queue.getLogsFrom(LogLevel.WARN).isEmpty());
        assertTrue(queue.hasLogLevel(LogLevel.WARN));
        assertTrue(queue.getLogsFrom(LogLevel.ERROR).isEmpty());
        assertFalse(queue.hasLogLevel(LogLevel.ERROR));
    }

    @Test
    public void getLogEvent()
    {
        LogQueue queue = new LogQueue();

        assertNull(queue.getLogEvent(0));
        assertNull(queue.getLogEvent(1));

        queue.warn("message");

        assertEquals("message", queue.getLogEvent(0).getMessage());
        assertNull(queue.getLogEvent(1));
    }

    @Test
    public void copyLog()
    {
        LogQueue queue1 = new LogQueue();
        LogQueue queue2 = new LogQueue();

        queue1.log(queue2);

        assertTrue(queue2.isEmpty());

        queue1.warn("message");

        queue1.log(queue2);

        assertEquals(1, queue2.size());
        assertSame(LogLevel.WARN, queue2.getLogEvent(0).getLevel());
    }

    @Test
    public void getLogEvents() throws IOException
    {
        LogQueue queue = new LogQueue();

        assertEquals(0, queue.getLogEvents(LogLevel.TRACE).stream().count());
        assertEquals(0, queue.getLogEvents(LogLevel.DEBUG).stream().count());
        assertEquals(0, queue.getLogEvents(LogLevel.INFO).stream().count());
        assertEquals(0, queue.getLogEvents(LogLevel.WARN).stream().count());
        assertEquals(0, queue.getLogEvents(LogLevel.ERROR).stream().count());

        queue.warn("");

        assertEquals(1, queue.getLogEvents(LogLevel.TRACE).stream().count());
        assertTrue(queue.hasLogLevel(LogLevel.TRACE));
        assertEquals(1, queue.getLogEvents(LogLevel.DEBUG).stream().count());
        assertTrue(queue.hasLogLevel(LogLevel.DEBUG));
        assertEquals(1, queue.getLogEvents(LogLevel.INFO).stream().count());
        assertTrue(queue.hasLogLevel(LogLevel.INFO));
        assertEquals(1, queue.getLogEvents(LogLevel.WARN).stream().count());
        assertTrue(queue.hasLogLevel(LogLevel.WARN));
        assertEquals(0, queue.getLogEvents(LogLevel.ERROR).stream().count());
        assertFalse(queue.hasLogLevel(LogLevel.ERROR));

        assertEquals(1, queue.getLogEvents(null).stream().count());
    }

    @Test
    public void getFirstEvent() throws IOException
    {
        LogQueue queue = new LogQueue();

        assertNull(queue.getFirstLogEvent());

        queue.info("info");
        queue.warn("warn");
        queue.error("error");

        assertEquals("info", queue.getFirstLogEvent().getMessage());
        assertEquals("info", queue.getFirstLogEvent(LogLevel.INFO).getMessage());
        assertEquals("warn", queue.getFirstLogEvent(LogLevel.WARN).getMessage());
        assertEquals("error", queue.getFirstLogEvent(LogLevel.ERROR).getMessage());
    }
}
