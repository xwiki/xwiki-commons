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
package org.xwiki.logging.test;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Assert;
import org.slf4j.MarkerFactory;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.Logger;
import org.xwiki.logging.event.LogEvent;

public class Utils
{
    public static void populateLogger(Logger logger)
    {
        int index = 0;

        logger.trace(LogEvent.MARKER_BEGIN, "TRACE");
        logger.trace("msg" + index++);
        logger.trace("msg{}", index++);
        logger.trace("{}{}", "msg", index++);
        logger.trace("{}{}{}{}", "m", "s", "g", index++);
        logger.trace("msg" + index, new Exception("msg" + index++));
        logger.trace(MarkerFactory.getMarker("marker" + index), "msg" + index++);
        logger.trace(MarkerFactory.getMarker("marker" + index), "msg{}", index++);
        logger.trace(MarkerFactory.getMarker("marker" + index), "{}{}", "msg", index++);
        logger.trace(MarkerFactory.getMarker("marker" + index), "{}{}{}{}", "m", "s", "g", index++);
        logger.trace(MarkerFactory.getMarker("marker" + index), "msg" + index, new Exception("msg" + index++));
        logger.trace(LogEvent.MARKER_END, "TRACE");

        logger.debug(LogEvent.MARKER_BEGIN, "DEBUG");
        logger.debug("msg" + index++);
        logger.debug("msg{}", index++);
        logger.debug("{}{}", "msg", index++);
        logger.debug("{}{}{}{}", "m", "s", "g", index++);
        logger.debug("msg" + index, new Exception("msg" + index++));
        logger.debug(MarkerFactory.getMarker("marker" + index), "msg" + index++);
        logger.debug(MarkerFactory.getMarker("marker" + index), "msg{}", index++);
        logger.debug(MarkerFactory.getMarker("marker" + index), "{}{}", "msg", index++);
        logger.debug(MarkerFactory.getMarker("marker" + index), "{}{}{}{}", "m", "s", "g", index++);
        logger.debug(MarkerFactory.getMarker("marker" + index), "msg" + index, new Exception("msg" + index++));
        logger.debug(LogEvent.MARKER_END, "DEBUG");

        logger.info(LogEvent.MARKER_BEGIN, "INFO");
        logger.info("msg" + index++);
        logger.info("msg{}", index++);
        logger.info("{}{}", "msg", index++);
        logger.info("{}{}{}{}", "m", "s", "g", index++);
        logger.info("msg" + index, new Exception("msg" + index++));
        logger.info(MarkerFactory.getMarker("marker" + index), "msg" + index++);
        logger.info(MarkerFactory.getMarker("marker" + index), "msg{}", index++);
        logger.info(MarkerFactory.getMarker("marker" + index), "{}{}", "msg", index++);
        logger.info(MarkerFactory.getMarker("marker" + index), "{}{}{}{}", "m", "s", "g", index++);
        logger.info(MarkerFactory.getMarker("marker" + index), "msg" + index, new Exception("msg" + index++));
        logger.info(LogEvent.MARKER_END, "INFO");

        logger.warn(LogEvent.MARKER_BEGIN, "WARN");
        logger.warn("msg" + index++);
        logger.warn("msg{}", index++);
        logger.warn("{}{}", "msg", index++);
        logger.warn("{}{}{}{}", "m", "s", "g", index++);
        logger.warn("msg" + index, new Exception("msg" + index++));
        logger.warn(MarkerFactory.getMarker("marker" + index), "msg" + index++);
        logger.warn(MarkerFactory.getMarker("marker" + index), "msg{}", index++);
        logger.warn(MarkerFactory.getMarker("marker" + index), "{}{}", "msg", index++);
        logger.warn(MarkerFactory.getMarker("marker" + index), "{}{}{}{}", "m", "s", "g", index++);
        logger.warn(MarkerFactory.getMarker("marker" + index), "msg" + index, new Exception("msg" + index++));
        logger.warn(LogEvent.MARKER_END, "WARN");

        logger.error(LogEvent.MARKER_BEGIN, "ERROR");
        logger.error("msg" + index++);
        logger.error("msg{}", index++);
        logger.error("{}{}", "msg", index++);
        logger.error("{}{}{}{}", "m", "s", "g", index++);
        logger.error("msg" + index, new Exception("msg" + index++));
        logger.error(MarkerFactory.getMarker("marker" + index), "msg" + index++);
        logger.error(MarkerFactory.getMarker("marker" + index), "msg{}", index++);
        logger.error(MarkerFactory.getMarker("marker" + index), "{}{}", "msg", index++);
        logger.error(MarkerFactory.getMarker("marker" + index), "{}{}{}{}", "m", "s", "g", index++);
        logger.error(MarkerFactory.getMarker("marker" + index), "msg" + index, new Exception("msg" + index++));
        logger.error(LogEvent.MARKER_END, "ERROR");
    }

    public static void validateLogger(Iterator<LogEvent> logs)
    {
        int index = 0;

        index = validateLogger(index, LogLevel.TRACE, logs);
        index = validateLogger(index, LogLevel.DEBUG, logs);
        index = validateLogger(index, LogLevel.INFO, logs);
        index = validateLogger(index, LogLevel.WARN, logs);
        index = validateLogger(index, LogLevel.ERROR, logs);
    }

    private static int validateLogger(int index, LogLevel level, Iterator<LogEvent> logs)
    {
        LogEvent event;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] {}, event.getArgumentArray());
        Assert.assertEquals(level.toString(), event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals(level.toString(), event.getMessage());
        Assert.assertEquals(Arrays.asList(level.toString()), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertEquals(LogEvent.MARKER_BEGIN, event.getMarker());

        event = logs.next();
        Assert.assertArrayEquals(new Object[] {}, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("msg" + index, event.getMessage());
        Assert.assertEquals(Arrays.asList("msg" + index), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertNull(event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] { index }, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("msg{}", event.getMessage());
        Assert.assertEquals(Arrays.asList("msg", ""), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertNull(event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] { "msg", index }, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("{}{}", event.getMessage());
        Assert.assertEquals(Arrays.asList("", "", ""), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertNull(event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] { "m", "s", "g", index }, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("{}{}{}{}", event.getMessage());
        Assert.assertEquals(Arrays.asList("", "", "", "", ""), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertNull(event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] {}, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("msg" + index, event.getMessage());
        Assert.assertEquals(Arrays.asList("msg" + index), event.getMessageElements());
        Assert.assertEquals("msg" + index, event.getThrowable().getMessage());
        Assert.assertNull(event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] {}, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("msg" + index, event.getMessage());
        Assert.assertEquals(Arrays.asList("msg" + index), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertEquals(MarkerFactory.getMarker("marker" + index), event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] { index }, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("msg{}", event.getMessage());
        Assert.assertEquals(Arrays.asList("msg", ""), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertEquals(MarkerFactory.getMarker("marker" + index), event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] { "msg", index }, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("{}{}", event.getMessage());
        Assert.assertEquals(Arrays.asList("", "", ""), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertEquals(MarkerFactory.getMarker("marker" + index), event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] { "m", "s", "g", index }, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("{}{}{}{}", event.getMessage());
        Assert.assertEquals(Arrays.asList("", "", "", "", ""), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertEquals(MarkerFactory.getMarker("marker" + index), event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] {}, event.getArgumentArray());
        Assert.assertEquals("msg" + index, event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals("msg" + index, event.getMessage());
        Assert.assertEquals(Arrays.asList("msg" + index), event.getMessageElements());
        Assert.assertEquals("msg" + index, event.getThrowable().getMessage());
        Assert.assertEquals(MarkerFactory.getMarker("marker" + index), event.getMarker());
        ++index;

        event = logs.next();
        Assert.assertArrayEquals(new Object[] {}, event.getArgumentArray());
        Assert.assertEquals(level.toString(), event.getFormattedMessage());
        Assert.assertEquals(level, event.getLevel());
        Assert.assertEquals(level.toString(), event.getMessage());
        Assert.assertEquals(Arrays.asList(level.toString()), event.getMessageElements());
        Assert.assertNull(event.getThrowable());
        Assert.assertEquals(LogEvent.MARKER_END, event.getMarker());

        return index;
    }
}
