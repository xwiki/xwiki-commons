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
package org.xwiki.logging.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.xwiki.logging.LoggingEventMessage;
import org.xwiki.logging.util.LoggingUtils;

/**
 * Test {@link LoggingUtils}.
 *
 * @version $Id$
 */
public class LoggingUtilsTest
{
    @Test
    public void testTranslate()
    {
        LoggingEventMessage logEvent =
            new LoggingEventMessage(null, Level.ERROR, "hello {} world {}", new Object[] { "one", "two" }, null);

        LoggingEvent translatedXWikiLoggingEvent = LoggingUtils.translate(logEvent, "hello2 {} world2 {}");

        Assert.assertEquals("hello2 {} world2 {}", translatedXWikiLoggingEvent.getMessage());
        Assert.assertArrayEquals(new Object[] { "one", "two" }, translatedXWikiLoggingEvent.getArgumentArray());

        translatedXWikiLoggingEvent = LoggingUtils.translate(logEvent, "hello2 {0} world2 {1}");

        Assert.assertEquals("hello2 {} world2 {}", translatedXWikiLoggingEvent.getMessage());
        Assert.assertArrayEquals(new Object[] { "one", "two" }, translatedXWikiLoggingEvent.getArgumentArray());

        translatedXWikiLoggingEvent = LoggingUtils.translate(logEvent, "hello2 {1} world2 {0}");

        Assert.assertEquals("hello2 {} world2 {}", translatedXWikiLoggingEvent.getMessage());
        Assert.assertArrayEquals(new Object[] { "two", "one" }, translatedXWikiLoggingEvent.getArgumentArray());

        translatedXWikiLoggingEvent = LoggingUtils.translate(logEvent, "hello2 {0}");

        Assert.assertEquals("hello2 {}", translatedXWikiLoggingEvent.getMessage());
        Assert.assertArrayEquals(new Object[] { "one", "two" }, translatedXWikiLoggingEvent.getArgumentArray());

        translatedXWikiLoggingEvent = LoggingUtils.translate(logEvent, "hello2 {1}");

        Assert.assertEquals("hello2 {}", translatedXWikiLoggingEvent.getMessage());
        Assert.assertArrayEquals(new Object[] { "two", "two" }, translatedXWikiLoggingEvent.getArgumentArray());
    }
}
