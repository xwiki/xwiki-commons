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
package org.xwiki.filter.test.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TestDataParser}.
 *
 * @version $Id$
 */
class TestDataParserTest
{
    @Test
    void interpret()
    {
        System.setProperty("key", "value");

        assertEquals("test", TestDataParser.interpret("test"));
        assertEquals("value", TestDataParser.interpret("${{{key}}}"));
        assertEquals("testvaluetest", TestDataParser.interpret("test${{{key}}}test"));
        assertEquals("test${{{key}}}", TestDataParser.interpret("test\\${{{key}}}"));
    }

    private Map<String, String> toMap(String... elements)
    {
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < elements.length; i += 2) {
            map.put(elements[i], elements[i + 1]);
        }

        return map;
    }

    @Test
    void parse() throws IOException
    {
        TestDataParser parser = new TestDataParser();

        TestResourceData data = parser.parse(IOUtils.toInputStream("", StandardCharsets.UTF_8), "resourceName");
        assertEquals("resourceName", data.resourceName);
        assertTrue(data.configuration.isEmpty());

        assertEquals(toMap("key", "value"),
            parser.parse(IOUtils.toInputStream(".configuration.key=value", StandardCharsets.UTF_8),
                "resourceName").configuration);
        assertEquals(toMap("key", "key=value"),
            parser.parse(IOUtils.toInputStream(".configuration.key=key=value", StandardCharsets.UTF_8),
                "resourceName").configuration);
    }
}
