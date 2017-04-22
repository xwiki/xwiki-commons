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
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link EscapeTool}.
 *
 * @version $Id$
 * @since 2.7RC1
 */
public class EscapeToolTest
{
    /**
     * The tested tool.
     */
    private EscapeTool tool;

    /**
     * Initialize the tested tool.
     */
    @Before
    public void setUp()
    {
        this.tool = new EscapeTool();
    }

    @Test
    public void escapeSimpleXML()
    {
        String escapedText = this.tool.xml("a < a' && a' < a\" => a < a\"");

        Assert.assertFalse("Failed to escape <", escapedText.contains("<"));
        Assert.assertFalse("Failed to escape >", escapedText.contains(">"));
        Assert.assertFalse("Failed to escape '", escapedText.contains("'"));
        Assert.assertFalse("Failed to escape \"", escapedText.contains("\""));
        Assert.assertFalse("Failed to escape &", escapedText.contains("&&"));
    }

    @Test
    public void escapeXMLApos()
    {
        Assert.assertFalse("' wrongly escaped to non-HTML &apos;", this.tool.xml("'").equals("&apos;"));
    }

    @Test
    public void escapeXMLWithNull()
    {
        Assert.assertNull("null should be null", this.tool.xml(null));
    }

    @Test
    public void escapeXMLNonAscii()
    {
        Assert.assertEquals("Non-ASCII characters shouldn't be escaped", "\u0123", this.tool.xml("\u0123"));
    }

    @Test
    public void escapeJSON()
    {
        String escapedText = this.tool.json("\"'\\/\b\f\n\r\t\u1234 plain  text");

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
    public void escapeJSONWithNullInput()
    {
        Assert.assertNull("Unexpected non-null output for null input", this.tool.json(null));
    }

    @Test
    public void escapeJSONWithNonStringInput()
    {
        Assert.assertEquals("true", this.tool.json(true));
        Assert.assertEquals("42", this.tool.json(42));
        Assert.assertEquals(this.tool.toString(), this.tool.json(this.tool));
    }

    @Test
    public void quotedPrintableWithSimpleText()
    {
        Assert.assertEquals("Hello World", this.tool.quotedPrintable("Hello World"));
    }

    @Test
    public void quotedPrintableWithSpecialChars()
    {
        Assert.assertEquals("a=3Db=0A", this.tool.quotedPrintable("a=b\n"));
    }

    @Test
    public void quotedPrintableWithNonAsciiChars()
    {
        Assert.assertEquals("=C4=A3", this.tool.quotedPrintable("\u0123"));
    }

    @Test
    public void quotedPrintableWithNull()
    {
        Assert.assertNull(this.tool.quotedPrintable(null));
    }

    @Test
    public void qWithSimpleText()
    {
        Assert.assertEquals("=?UTF-8?Q?Hello_World?=", this.tool.q("Hello World"));
    }

    @Test
    public void qWithSpecialChars()
    {
        Assert.assertEquals("=?UTF-8?Q?a=3Db=3F=5F=0A?=", this.tool.q("a=b?_\n"));
    }

    @Test
    public void qWithNonAsciiChars()
    {
        Assert.assertEquals("=?UTF-8?Q?=C4=A3?=", this.tool.q("\u0123"));
    }

    @Test
    public void qWithNull()
    {
        Assert.assertNull(this.tool.q(null));
    }

    @Test
    public void bWithSimpleText()
    {
        Assert.assertEquals("=?UTF-8?B?SGVsbG8gV29ybGQ=?=", this.tool.b("Hello World"));
    }

    @Test
    public void bWithSpecialChars()
    {
        Assert.assertEquals("=?UTF-8?B?YT1iPwo=?=", this.tool.b("a=b?\n"));
    }

    @Test
    public void bWithNonAsciiChars()
    {
        Assert.assertEquals("=?UTF-8?B?xKM=?=", this.tool.b("\u0123"));
    }

    @Test
    public void bWithNull()
    {
        Assert.assertNull(this.tool.b(null));
    }

    @Test
    public void url()
    {
        HashMap<String, String> map = new LinkedHashMap<>();
        map.put("hello", "world");
        map.put(null, "value");
        map.put("B& B", "yes");
        map.put("empty", null);
        Assert.assertEquals("hello=world&B%26%20B=yes&empty=", this.tool.url(map));
    }

    @Test
    public void urlWithDouble()
    {
        HashMap<String, Double> map = new LinkedHashMap<>();
        map.put("A&A", 1.5);
        map.put("B&B", 1.2);
        Assert.assertEquals("A%26A=1.5&B%26B=1.2", this.tool.url(map));
    }

    @Test
    public void urlWithArray()
    {
        HashMap<String, String[]> map = new HashMap<>();
        String[] array = {"M&M", null, "Astronomy&Astrophysics"};
        map.put("couple", array);
        Assert.assertEquals("couple=M%26M&couple=&couple=Astronomy%26Astrophysics", this.tool.url(map));
    }

    @Test
    public void urlWithCollection()
    {
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        ArrayList<String> collection1 = new ArrayList<>();
        collection1.add("test");
        map.put("alice", collection1);
        ArrayList<String> collection2 = new ArrayList<>();
        collection2.add(null);
        collection2.add("t&t");
        collection2.add("R&D");
        map.put("bob", collection2);
        Assert.assertEquals("bob=&bob=t%26t&bob=R%26D&alice=test", this.tool.url(map));
    }

    /**
     * @see <a href="https://drafts.csswg.org/cssom/#serialize-an-identifier">serialize-an-identifier</a>
     */
    @Test
    public void css()
    {
        Assert.assertEquals(this.tool.css("a#b.c d[e=f]g{h:i;j}k"), "a\\#b\\.c\\ d\\[e\\=f\\]g\\{h\\:i\\;j\\}k");

        // Invalid character U+0000 (the exception must be caught)
        Assert.assertNull(this.tool.css("a\u0000b"));
    }
}
