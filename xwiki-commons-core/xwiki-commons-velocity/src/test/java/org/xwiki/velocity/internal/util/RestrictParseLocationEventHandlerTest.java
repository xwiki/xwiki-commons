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
package org.xwiki.velocity.internal.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link RestrictParseLocationEventHandler}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class RestrictParseLocationEventHandlerTest
{
    private RestrictParseLocationEventHandler handler = new RestrictParseLocationEventHandler();

    /**
     * Capture logs with WARN or higher severity to assert them.
     */
    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @Test
    void includeEventWhenAllowedPath()
    {
        assertEquals("/templates/xwikivars.vm",
            this.handler.includeEvent(null, "xwikivars.vm", "xwiki:Main.WebHome", "parse"), "Wrong template returned");
    }

    @Test
    void includeEventWhenIllegalPath()
    {
        assertNull(this.handler.includeEvent(null, "../WEB-INF/xwiki.cfg", "xwiki:Main.WebHome", "parse"),
            "Template shouldn't have been returned");

        assertEquals(1, this.logCapture.size());
        assertEquals("Direct access to template file [/WEB-INF/xwiki.cfg] refused. Possible break-in attempt!",
            this.logCapture.getMessage(0));
    }
}
