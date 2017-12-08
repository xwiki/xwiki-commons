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
package org.xwiki.xml;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link org.xwiki.xml.XMLUtils}.
 *
 * @version $Id$
 * @since 1.6M1
 */
public class XMLUtilsTest
{
    @Test
    public void escapeXMLComment()
    {
        assertEquals("-\\- ", XMLUtils.escapeXMLComment("-- "));
        assertEquals("-\\", XMLUtils.escapeXMLComment("-"));
        assertEquals("-\\-\\-\\", XMLUtils.escapeXMLComment("---"));
        assertEquals("- ", XMLUtils.escapeXMLComment("- "));
    }

    @Test
    public void unescapeXMLComment()
    {
        assertEquals("", XMLUtils.unescapeXMLComment("\\"));
        assertEquals("\\", XMLUtils.unescapeXMLComment("\\\\"));
        assertEquals("--", XMLUtils.unescapeXMLComment("\\-\\-"));
        assertEquals("--", XMLUtils.unescapeXMLComment("\\-\\-\\"));
    }

    @Test
    public void escape()
    {
        String escapedText = XMLUtils.escape("a < a' && a' < a\" => a < a\"");

        assertFalse("Failed to escape <", escapedText.contains("<"));
        assertFalse("Failed to escape >", escapedText.contains(">"));
        assertFalse("Failed to escape '", escapedText.contains("'"));
        assertFalse("Failed to escape \"", escapedText.contains("\""));
        assertFalse("Failed to escape &", escapedText.contains("&&"));
    }

    @Test
    public void escapeApos()
    {
        assertFalse("' wrongly escaped to non-HTML &apos;", XMLUtils.escape("'").equals("&apos;"));
    }

    @Test
    public void escapeEmptyString()
    {
        assertEquals("\"\" should be \"\"", "", XMLUtils.escape(""));
    }

    @Test
    public void escapeWithNull()
    {
        assertNull("null should be null", XMLUtils.escape(null));
    }

    @Test
    public void escapeNonAscii()
    {
        assertEquals("Non-ASCII characters were escaped", "\u0123", XMLUtils.escape("\u0123"));
    }

    @Test
    public void escapeAttributeValue()
    {

        String escapedText = XMLUtils.escapeAttributeValue("a < a' && a' < a\" => a < a\" {");

        assertFalse("Failed to escape <", escapedText.contains("<"));
        assertFalse("Failed to escape >", escapedText.contains(">"));
        assertFalse("Failed to escape '", escapedText.contains("'"));
        assertFalse("Failed to escape \"", escapedText.contains("\""));
        assertFalse("Failed to escape &", escapedText.contains("&&"));
        assertFalse("Failed to escape {", escapedText.contains("{"));
    }

    @Test
    public void escapeAttributeValueApos()
    {
        assertFalse("' wrongly escaped to non-HTML &apos;", XMLUtils.escapeAttributeValue("'")
            .equals("&apos;"));
    }

    @Test
    public void escapeFAttributeValueEmptyString()
    {
        assertEquals("\"\" should be \"\"", "", XMLUtils.escapeAttributeValue(""));
    }

    @Test
    public void escapeFAttributeValueWithNull()
    {
        assertNull("null should be null", XMLUtils.escapeAttributeValue(null));
    }

    @Test
    public void escapeAttributeValueNonAscii()
    {
        assertEquals("Non-ASCII characters were escaped", "\u0123", XMLUtils.escapeAttributeValue("\u0123"));
    }

    @Test
    public void escapeElementContent()
    {

        String escapedText = XMLUtils.escapeElementContent("a < a' && a' < a\" => a < a\"");

        assertFalse("Failed to escape <", escapedText.contains("<"));
        assertFalse("Failed to escape >", escapedText.contains(">"));
        assertTrue("Wrongfully escaped '", escapedText.contains("'"));
        assertTrue("Wrongfully escaped \"", escapedText.contains("\""));
        assertFalse("Failed to escape &", escapedText.contains("&&"));
    }

    @Test
    public void escapeElementContentEmptyString()
    {
        assertEquals("\"\" should be \"\"", "", XMLUtils.escapeElementContent(""));
    }

    @Test
    public void escapeElementContentWithNull()
    {
        assertNull("null should be null", XMLUtils.escapeElementContent(null));
    }

    @Test
    public void escapeElementContentNonAscii()
    {
        assertEquals("Non-ASCII characters were escaped", "\u0123", XMLUtils.escapeElementContent("\u0123"));
    }

    @Test
    public void unescape()
    {
        assertEquals("Failed to unescaped named entities", "&'\"<>",
            XMLUtils.unescape("&amp;&apos;&quot;&lt;&gt;"));
        assertEquals("Failed to unescaped decimal entities", "&'\"<>",
            XMLUtils.unescape("&#38;&#39;&#34;&#60;&#62;"));
        assertEquals("Failed to unescaped decimal entities with leading zeros", "&'\"<>",
            XMLUtils.unescape("&#038;&#0039;&#00034;&#000060;&#0000062;"));
        assertEquals("Failed to unescaped hexadecimal entities", "&'\"<<>>",
            XMLUtils.unescape("&#x26;&#x27;&#x22;&#x3c;&#x3C;&#x3e;&#x3E;"));
        assertEquals("Failed to unescaped hexadecimal entities with leading zeros", "&'\"<<>>",
            XMLUtils.unescape("&#x026;&#x0027;&#x00022;&#x00003c;&#x0003C;&#x003e;&#x03E;"));
    }

    @Test
    public void unescapeEmptyString()
    {
        assertEquals("\"\" should be \"\"", "", XMLUtils.unescape(""));
    }

    @Test
    public void unescapeWithNull()
    {
        assertNull("null should be null", XMLUtils.unescape(null));
    }

    @Test
    public void unescapeOtherEscapes()
    {
        assertEquals("Extra named entities were unescaped", "&deg;", XMLUtils.unescape("&deg;"));
        assertEquals("Extra decimal entities were unescaped", "&#65;", XMLUtils.unescape("&#65;"));
        assertEquals("Extra hexadecimal entities were unescaped", "&#x5;", XMLUtils.unescape("&#x5;"));
    }

    @Test
    public void createDomDocument()
    {
        // Nothing much that we can test here...
        assertNotNull(XMLUtils.createDOMDocument());
    }
}
