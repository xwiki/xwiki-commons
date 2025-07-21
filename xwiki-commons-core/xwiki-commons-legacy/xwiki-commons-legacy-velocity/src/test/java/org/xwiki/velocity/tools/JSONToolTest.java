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
package org.xwiki.velocity.tools;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for legacy methods of {@link JSONTool}.
 *
 * @version $Id$
 * @since 17.6.0RC1
 */
class JSONToolTest
{
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    /**
     * The object being tested.
     */
    private JSONTool tool = new JSONTool();

    @Test
    void parseArray()
    {
        List<?> json = (List<?>) this.tool.parse("[1,2,3]");
        assertEquals(3, json.size());
    }

    @Test
    void parseEmptyArray()
    {
        List<?> json = (List<?>) this.tool.parse("[]");
        assertTrue(json.isEmpty());
    }

    @Test
    void parseMap()
    {
        Map<?, ?> json = (Map<?, ?>) this.tool.parse("{\"a\" : 1, \"b\": [1], \"c\": true}");
        assertEquals(3, json.size());
        assertTrue((boolean) json.get("c"));
    }

    @Test
    void parseEmptyMap()
    {
        Map<?, ?> json = (Map<?, ?>) this.tool.parse("{}");
        assertTrue(json.isEmpty());
    }

    @Test
    void parseNull()
    {
        assertNull(this.tool.parse(null));
    }

    @Test
    void parseEmptyString()
    {
        assertNull(this.tool.parse(""));
    }

    @Test
    void parseInvalidJSON()
    {
        assertNull(this.tool.parse("This is not the JSON you are looking for..."));

        assertTrue(this.logCapture.getMessage(0)
            .startsWith("Failed to parse JSON [This is not the JSON you are ...]: "
                + "JsonParseException: Unrecognized token 'This': was expecting (JSON String, Number, Array, Object or "
                + "token 'null', 'true' or 'false')"));
    }
}