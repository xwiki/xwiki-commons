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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.logging.event.LogEvent;

/**
 * Test {@link LogQueue}.
 *
 * @version $Id$
 */
public class LogQueueTest
{
    @Test
    public void testError()
    {
        LogQueue queue = new LogQueue();
        LogEvent logEvent;

        queue.error("");
        logEvent = queue.poll();
        Assert.assertEquals(Arrays.asList(""), logEvent.getMessageElements());
        Assert.assertEquals("", logEvent.getFormattedMessage());

        queue.error("message");
        Assert.assertEquals("message", queue.poll().getFormattedMessage());

        queue.error("message {}", "param");
        Assert.assertEquals("message param", queue.poll().getFormattedMessage());

        queue.error("message {} {}", "param1", "param2");
        logEvent = queue.poll();
        Assert.assertEquals("message param1 param2", logEvent.getFormattedMessage());
        Assert.assertEquals(Arrays.asList("message ", " ", ""), logEvent.getMessageElements());
        Assert.assertEquals(Arrays.asList("param1", "param2"), Arrays.asList(logEvent.getArgumentArray()));

        queue.error("message {}", "param1", new Exception());
        logEvent = queue.poll();
        Assert.assertEquals("message param1", logEvent.getFormattedMessage());
        Assert.assertEquals(Arrays.asList("message ", ""), logEvent.getMessageElements());
        Assert.assertEquals(Arrays.asList("param1"), Arrays.asList(logEvent.getArgumentArray()));
        Assert.assertNotNull(logEvent.getThrowable());
        Assert.assertNull(logEvent.getTranslationKey());

        queue.error("message {}", new Object[] { "param1", new Exception() });
        logEvent = queue.poll();
        Assert.assertEquals("message param1", logEvent.getFormattedMessage());
        Assert.assertEquals(Arrays.asList("message ", ""), logEvent.getMessageElements());
        Assert.assertEquals(Arrays.asList("param1"), Arrays.asList(logEvent.getArgumentArray()));
        Assert.assertNotNull(logEvent.getThrowable());
        Assert.assertNull(logEvent.getTranslationKey());

        queue.error(new TranslationMarker("translation.key"), "message");
        logEvent = queue.poll();
        Assert.assertEquals("translation.key", logEvent.getTranslationKey());
        Assert.assertEquals(Arrays.asList("message"), logEvent.getMessageElements());
        Assert.assertEquals(Arrays.asList(), Arrays.asList(logEvent.getArgumentArray()));
    }

    @Test
    public void testContainLogsFrom()
    {
        LogQueue queue = new LogQueue();

        Assert.assertFalse(queue.containLogsFrom(LogLevel.TRACE));
        Assert.assertFalse(queue.containLogsFrom(LogLevel.DEBUG));
        Assert.assertFalse(queue.containLogsFrom(LogLevel.INFO));
        Assert.assertFalse(queue.containLogsFrom(LogLevel.WARN));
        Assert.assertFalse(queue.containLogsFrom(LogLevel.ERROR));

        queue.warn("");

        Assert.assertTrue(queue.containLogsFrom(LogLevel.TRACE));
        Assert.assertTrue(queue.containLogsFrom(LogLevel.DEBUG));
        Assert.assertTrue(queue.containLogsFrom(LogLevel.INFO));
        Assert.assertTrue(queue.containLogsFrom(LogLevel.WARN));
        Assert.assertFalse(queue.containLogsFrom(LogLevel.ERROR));
    }

    @Test
    public void testGetLogsFrom()
    {
        LogQueue queue = new LogQueue();

        Assert.assertTrue(queue.getLogsFrom(LogLevel.TRACE).isEmpty());
        Assert.assertTrue(queue.getLogsFrom(LogLevel.DEBUG).isEmpty());
        Assert.assertTrue(queue.getLogsFrom(LogLevel.INFO).isEmpty());
        Assert.assertTrue(queue.getLogsFrom(LogLevel.WARN).isEmpty());
        Assert.assertTrue(queue.getLogsFrom(LogLevel.ERROR).isEmpty());

        queue.warn("");

        Assert.assertFalse(queue.getLogsFrom(LogLevel.TRACE).isEmpty());
        Assert.assertFalse(queue.getLogsFrom(LogLevel.DEBUG).isEmpty());
        Assert.assertFalse(queue.getLogsFrom(LogLevel.INFO).isEmpty());
        Assert.assertFalse(queue.getLogsFrom(LogLevel.WARN).isEmpty());
        Assert.assertTrue(queue.getLogsFrom(LogLevel.ERROR).isEmpty());
    }
}
