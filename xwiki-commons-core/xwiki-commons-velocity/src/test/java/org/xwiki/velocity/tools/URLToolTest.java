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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link URLTool}.
 * 
 * @version $Id$
 * @since 6.3M1
 */
public class URLToolTest
{
    private URLTool tool = new URLTool();

    @Test
    public void testSimpleUrlParse()
    {
        Map<String, ?> query = tool.parseQuery("a=b%26c");
        assertEquals(Collections.singleton("a"), query.keySet(), "should have one parameter");
        assertEquals(Collections.singletonList("b&c"), query.get("a"), "should have one parameter value");
    }

    @Test
    public void testUrlParseMultipleValues()
    {
        Map<String, ?> query = tool.parseQuery("a=b+c&a=b%3Dc&ab=a+%26+b");
        assertEquals(2, query.size(), "should have two parameter");
        assertEquals(Collections.singletonList("a & b"), query.get("ab"), "should have one parameter value for ab");
        assertEquals(Arrays.asList(new String[] {"b c", "b=c"}), query.get("a"),
            "should have two parameter values for a");
    }

    @Test
    public void testInvalidUrlParse()
    {
        Map<String, ?> query = tool.parseQuery("a=b ' onclick='foo");
        assertEquals(Collections.singleton("a"), query.keySet(), "should have one parameter");
        assertEquals(Collections.singletonList("b ' onclick='foo"), query.get("a"), "should have one parameter");
    }

    @Test
    public void testHandleNull()
    {
        Map<String, ?> query = tool.parseQuery(null);
        assertNotNull(query, "null query results in empty map");
        assertTrue(query.isEmpty(), "null query results in empty map");
    }

    @Test
    public void preserveParameterOrder()
    {
        EscapeTool escapeTool = new EscapeTool();
        String queryString = "x=5&x=4&r=3&a=2";
        assertEquals(queryString, escapeTool.url(tool.parseQuery(queryString)));
    }
    
    @Test
    public void addParameterToEmptyQueryString() {
        assertEquals("a=b", tool.add("", "a", "b"));
    }
    
    @Test
    public void addParameterToQueryString() {
        assertEquals("a=b&c=d", tool.add("a=b", "c", "d"));
    }
    
    @Test
    public void addNullValueToQueryString() {
        assertEquals("a=b&c=", tool.add("a=b", "c", null));
    }
    
    @Test
    public void encodeParameter() {
        assertEquals("a=b&c=%25d", tool.add("a=b", "c", "%d"));
    }
    
    @Test
    public void convertMapToQueryString() {
        Map<String, String> params = new HashMap<>();
        params.put("a", "b");
        params.put("c", "%d");
        assertEquals("a=b&c=%25d", tool.getQueryString(params));
    }
}
