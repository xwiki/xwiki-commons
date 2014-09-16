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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("should have one parameter", Collections.singleton("a"), query.keySet() );
        Assert.assertEquals("should have one parameter value", Collections.singletonList("b&c"), query.get("a") );
    }

    @Test
    public void testUrlParseMultipleValues()
    {
        Map<String, ?> query = tool.parseQuery("a=b+c&a=b%3Dc&ab=a+%26+b");
        Assert.assertEquals("should have two parameter", 2, query.size() );
        Assert.assertEquals("should have one parameter value for ab", Collections.singletonList("a & b"), query.get("ab") );
        Assert.assertEquals("should have two parameter values for a", Arrays.asList(new String[]{"b c", "b=c"}), query.get("a"));
    }

    @Test
    public void testInvalidUrlParse()
    {
        Map<String, ?> query = tool.parseQuery("a=b ' onclick='foo");
        Assert.assertEquals("should have one parameter", Collections.singleton("a"), query.keySet());
        Assert.assertEquals("should have one parameter", Collections.singletonList("b ' onclick='foo"), query.get("a"));
    }

    @Test
    public void testHandleNull()
    {
        Map<String, ?> query = tool.parseQuery(null);
        Assert.assertNotNull("null query results in empty map", query);
        Assert.assertTrue("null query results in empty map", query.isEmpty());
    }

}
