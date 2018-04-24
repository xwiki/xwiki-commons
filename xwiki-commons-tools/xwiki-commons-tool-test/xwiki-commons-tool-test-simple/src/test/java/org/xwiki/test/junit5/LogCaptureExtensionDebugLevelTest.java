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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.test.LogLevel;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@link LogCaptureExtension} on a statically created SLF4J Logger.
 *
 * @version $Id$
 * @since 8.1M2
 */
public class LogCaptureExtensionDebugLevelTest
{
    public static final Logger LOGGER = LoggerFactory.getLogger(LogCaptureExtensionDebugLevelTest.class);

    @RegisterExtension
    static LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.DEBUG);

    @Test
    public void staticLogger()
    {
        LOGGER.debug("test1");
        LOGGER.trace("test2");
        assertEquals("test1", logCapture.getMessage(0));
    }
}
