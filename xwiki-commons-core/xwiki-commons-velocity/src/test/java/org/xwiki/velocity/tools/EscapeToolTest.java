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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @BeforeEach
    public void setUp()
    {
        this.tool = new EscapeTool();
    }

    @Test
    public void escapeSimpleXML()
    {
        String escapedText = this.tool.xml("a < a' && a' < a\" => a < a\"");

        assertFalse(escapedText.contains("<"), "Failed to escape <");
        assertFalse(escapedText.contains(">"), "Failed to escape >");
        assertFalse(escapedText.contains("'"), "Failed to escape '");
        assertFalse(escapedText.contains("\""), "Failed to escape \"");
        assertFalse(escapedText.contains("&&"), "Failed to escape &");
    }

    @Test
    public void escapeXMLApos()
    {
        assertFalse(this.tool.xml("'").equals("&apos;"), "' wrongly escaped to non-HTML &apos;");
    }

    @Test
    public void escapeXMLWithNull()
    {
        assertNull(this.tool.xml(null), "null should be null");
    }

    @Test
    public void escapeXMLNonAscii()
    {
        assertEquals("\u0123", this.tool.xml("\u0123"), "Non-ASCII characters shouldn't be escaped");
    }

    @Test
    public void escapeJSON()
    {
        String escapedText = this.tool.json("\"'\\/\b\f\n\r\t\u1234 plain  text");

        assertTrue(escapedText.contains("\\\""), "Failed to escape [\"]");
        assertTrue(escapedText.contains("'"), "Wrongly escaped [']");
        assertTrue(escapedText.contains("\\\\"), "Failed to escape [\\]");
        assertTrue(escapedText.contains("\\/"), "Failed to escape [/]");
        assertTrue(escapedText.contains("\\b"), "Failed to escape [\\b]");
        assertTrue(escapedText.contains("\\f"), "Failed to escape [\\f]");
        assertTrue(escapedText.contains("\\n"), "Failed to escape [\\n]");
        assertTrue(escapedText.contains("\\r"), "Failed to escape [\\r]");
        assertTrue(escapedText.contains("\\t"), "Failed to escape [\\t]");
        assertTrue(escapedText.contains("\\u1234"), "Failed to escape [\\u1234]");
        assertTrue(escapedText.contains(" plain  text"), "Wrongly escaped plain text");
    }

    @Test
    public void escapeJSONWithNullInput()
    {
        assertNull(this.tool.json(null), "Unexpected non-null output for null input");
    }

    @Test
    public void escapeJSONWithNonStringInput()
    {
        assertEquals("true", this.tool.json(true));
        assertEquals("42", this.tool.json(42));
        assertEquals(this.tool.toString(), this.tool.json(this.tool));
    }

    @Test
    public void quotedPrintableWithSimpleText()
    {
        assertEquals("Hello World", this.tool.quotedPrintable("Hello World"));
    }

    @Test
    public void quotedPrintableWithSpecialChars()
    {
        assertEquals("a=3Db=0A", this.tool.quotedPrintable("a=b\n"));
    }

    @Test
    public void quotedPrintableWithNonAsciiChars()
    {
        assertEquals("=C4=A3", this.tool.quotedPrintable("\u0123"));
    }

    @Test
    public void quotedPrintableWithNull()
    {
        assertNull(this.tool.quotedPrintable(null));
    }

    @Test
    public void qWithSimpleText()
    {
        assertEquals("=?UTF-8?Q?Hello_World?=", this.tool.q("Hello World"));
    }

    @Test
    public void qWithSpecialChars()
    {
        assertEquals("=?UTF-8?Q?a=3Db=3F=5F=0A?=", this.tool.q("a=b?_\n"));
    }

    @Test
    public void qWithNonAsciiChars()
    {
        assertEquals("=?UTF-8?Q?=C4=A3?=", this.tool.q("\u0123"));
    }

    @Test
    public void qWithNull()
    {
        assertNull(this.tool.q(null));
    }

    @Test
    public void bWithSimpleText()
    {
        assertEquals("=?UTF-8?B?SGVsbG8gV29ybGQ=?=", this.tool.b("Hello World"));
    }

    @Test
    public void bWithSpecialChars()
    {
        assertEquals("=?UTF-8?B?YT1iPwo=?=", this.tool.b("a=b?\n"));
    }

    @Test
    public void bWithNonAsciiChars()
    {
        assertEquals("=?UTF-8?B?xKM=?=", this.tool.b("\u0123"));
    }

    @Test
    public void bWithNull()
    {
        assertNull(this.tool.b(null));
    }

    @Test
    public void url()
    {
        HashMap<String, String> map = new LinkedHashMap<>();
        map.put("hello", "world");
        map.put(null, "value");
        map.put("B& B", "yes");
        map.put("empty", null);
        assertEquals("hello=world&B%26%20B=yes&empty=", this.tool.url(map));
    }

    @Test
    public void urlWithDouble()
    {
        HashMap<String, Double> map = new LinkedHashMap<>();
        map.put("A&A", 1.5);
        map.put("B&B", 1.2);
        assertEquals("A%26A=1.5&B%26B=1.2", this.tool.url(map));
    }

    @Test
    public void urlWithArray()
    {
        HashMap<String, String[]> map = new HashMap<>();
        String[] array = {"M&M", null, "Astronomy&Astrophysics"};
        map.put("couple", array);
        assertEquals("couple=M%26M&couple=&couple=Astronomy%26Astrophysics", this.tool.url(map));
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
        assertEquals("bob=&bob=t%26t&bob=R%26D&alice=test", this.tool.url(map));
    }

    /**
     * @see <a href="https://drafts.csswg.org/cssom/#serialize-an-identifier">serialize-an-identifier</a>
     */
    @Test
    public void css()
    {
        assertEquals(this.tool.css("a#b.c d[e=f]g{h:i;j}k"), "a\\#b\\.c\\ d\\[e\\=f\\]g\\{h\\:i\\;j\\}k");

        // Invalid character U+0000 (the exception must be caught)
        assertNull(this.tool.css("a\u0000b"));
    }
}
