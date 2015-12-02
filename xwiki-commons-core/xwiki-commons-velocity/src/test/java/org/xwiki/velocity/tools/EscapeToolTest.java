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

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link EscapeTool}.
 *
 * @version $Id$
 * @since 2.7RC1
 */
public class EscapeToolTest
{
    @Test
    public void testEscapeSimpleXML()
    {
        EscapeTool tool = new EscapeTool();
        String escapedText = tool.xml("a < a' && a' < a\" => a < a\"");

        Assert.assertFalse("Failed to escape <", escapedText.contains("<"));
        Assert.assertFalse("Failed to escape >", escapedText.contains(">"));
        Assert.assertFalse("Failed to escape '", escapedText.contains("'"));
        Assert.assertFalse("Failed to escape \"", escapedText.contains("\""));
        Assert.assertFalse("Failed to escape &", escapedText.contains("&&"));
    }

    @Test
    public void testEscapeXMLApos()
    {
        EscapeTool tool = new EscapeTool();

        Assert.assertFalse("' wrongly escaped to non-HTML &apos;", tool.xml("'").equals("&apos;"));
    }

    @Test
    public void testEscapeXMLWithNull()
    {
        EscapeTool tool = new EscapeTool();

        Assert.assertNull("null should be null", tool.xml(null));
    }

    @Test
    public void testEscapeXMLNonAscii()
    {
        EscapeTool tool = new EscapeTool();

        Assert.assertTrue("Non-ASCII characters shouldn't be escaped", tool.xml("\u0123").equals("\u0123"));
    }

    @Test
    public void testEscapeJSON()
    {
        EscapeTool tool = new EscapeTool();
        String escapedText = tool.json("\"'\\/\b\f\n\r\t\u1234 plain  text");

        Assert.assertTrue("Failed to escape [\"]", escapedText.contains("\\\""));
        Assert.assertTrue("Wrongly escaped [']", escapedText.contains("'"));
        Assert.assertTrue("Failed to escape [\\]", escapedText.contains("\\\\"));
        Assert.assertTrue("Failed to escape [/]", escapedText.contains("\\/"));
        Assert.assertTrue("Failed to escape [\\b]", escapedText.contains("\\b"));
        Assert.assertTrue("Failed to escape [\\f]", escapedText.contains("\\f"));
        Assert.assertTrue("Failed to escape [\\n]", escapedText.contains("\\n"));
        Assert.assertTrue("Failed to escape [\\r]", escapedText.contains("\\r"));
        Assert.assertTrue("Failed to escape [\\t]", escapedText.contains("\\t"));
        Assert.assertTrue("Failed to escape [\\u1234]", escapedText.contains("\\u1234"));
        Assert.assertTrue("Wrongly escaped plain text", escapedText.contains(" plain  text"));
    }

    @Test
    public void testEscapeJSONWithNullInput()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertNull("Unexpected non-null output for null input", tool.json(null));
    }

    @Test
    public void testEscapeJSONWithNonStringInput()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("true", tool.json(true));
        Assert.assertEquals("42", tool.json(42));
        Assert.assertEquals(tool.toString(), tool.json(tool));
    }

    @Test
    public void testQuotedPrintableWithSimpleText()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("Hello World", tool.quotedPrintable("Hello World"));
    }

    @Test
    public void testQuotedPrintableWithSpecialChars()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("a=3Db=0A", tool.quotedPrintable("a=b\n"));
    }

    @Test
    public void testQuotedPrintableWithNonAsciiChars()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("=C4=A3", tool.quotedPrintable("\u0123"));
    }

    @Test
    public void testQuotedPrintableWithNull()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertNull(tool.quotedPrintable(null));
    }

    @Test
    public void testQWithSimpleText()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("=?UTF-8?Q?Hello_World?=", tool.q("Hello World"));
    }

    @Test
    public void testQWithSpecialChars()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("=?UTF-8?Q?a=3Db=3F=5F=0A?=", tool.q("a=b?_\n"));
    }

    @Test
    public void testQWithNonAsciiChars()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("=?UTF-8?Q?=C4=A3?=", tool.q("\u0123"));
    }

    @Test
    public void testQWithNull()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertNull(tool.q(null));
    }

    @Test
    public void testBWithSimpleText()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("=?UTF-8?B?SGVsbG8gV29ybGQ=?=", tool.b("Hello World"));
    }

    @Test
    public void testBWithSpecialChars()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("=?UTF-8?B?YT1iPwo=?=", tool.b("a=b?\n"));
    }

    @Test
    public void testBWithNonAsciiChars()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals("=?UTF-8?B?xKM=?=", tool.b("\u0123"));
    }

    @Test
    public void testBWithNull()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertNull(tool.b(null));
    }

    @Test
    public void testURL()
    {
        EscapeTool tool = new EscapeTool();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("hello", "world");
        map.put(null, "value");
        map.put("B&B", "yes");
        map.put("empty", null);
        Assert.assertEquals("hello=world&B%26B=yes&empty=", tool.url(map));
    }

    @Test
    public void testURLWithDouble()
    {
        EscapeTool tool = new EscapeTool();
        HashMap<String, Double> map = new HashMap<String, Double>();
        map.put("A&A", 1.5);
        map.put("B&B", 1.2);
        Assert.assertEquals("B%26B=1.2&A%26A=1.5", tool.url(map));
    }

    @Test
    public void testURLWithArray()
    {
        EscapeTool tool = new EscapeTool();
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        String[] array = { "M&M", null, "Astronomy&Astrophysics" };
        map.put("couple", array);
        Assert.assertEquals("couple=M%26M&couple=&couple=Astronomy%26Astrophysics", tool.url(map));
    }

    @Test
    public void testURLWithCollection()
    {
        EscapeTool tool = new EscapeTool();
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        ArrayList<String> collection1 = new ArrayList<String>();
        collection1.add("test");
        map.put("alice", collection1);
        ArrayList<String> collection2 = new ArrayList<String>();
        collection2.add(null);
        collection2.add("t&t");
        collection2.add("R&D");
        map.put("bob", collection2);
        Assert.assertEquals("bob=&bob=t%26t&bob=R%26D&alice=test", tool.url(map));
    }

    /**
     * @see https://drafts.csswg.org/cssom/#serialize-an-identifier
     */
    @Test
    public void css()
    {
        EscapeTool tool = new EscapeTool();
        Assert.assertEquals(tool.css("a#b.c d[e=f]g{h:i;j}k"), "a\\#b\\.c\\ d\\[e\\=f\\]g\\{h\\:i\\;j\\}k");

        // Invalid character U+0000 (the exception must be caught)
        Assert.assertNull(tool.css("a\u0000b"));
    }
}
