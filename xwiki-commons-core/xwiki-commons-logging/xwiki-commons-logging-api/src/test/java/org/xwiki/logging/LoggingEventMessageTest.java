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

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

/**
 * Test {@link LoggingEvent}.
 *
 * @version $Id$
 */
public class LoggingEventMessageTest
{
    @Test
    public void testGetMessageElements()
    {
        LoggingEventMessage logEvent = new LoggingEventMessage(null, Level.ERROR, "", null, null);

        Assert.assertEquals(logEvent.getMessageElements(), Arrays.asList(""));

        logEvent = new LoggingEventMessage(null, Level.ERROR, "", ArrayUtils.EMPTY_OBJECT_ARRAY, null);

        Assert.assertEquals(logEvent.getMessageElements(), Arrays.asList(""));

        logEvent = new LoggingEventMessage(null, Level.ERROR, "message", ArrayUtils.EMPTY_OBJECT_ARRAY, null);

        Assert.assertEquals(logEvent.getMessageElements(), Arrays.asList("message"));

        logEvent = new LoggingEventMessage(null, Level.ERROR, "message {}", new Object[] { "" }, null);

        Assert.assertEquals(logEvent.getMessageElements(), Arrays.asList("message ", ""));
    }
}
