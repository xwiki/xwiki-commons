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

import org.apache.html.dom.HTMLDocumentImpl;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLElement;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("\\\\", XMLUtils.escapeXMLComment("\\"));
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

        assertFalse(escapedText.contains("<"), "Failed to escape <");
        assertFalse(escapedText.contains(">"), "Failed to escape >");
        assertFalse(escapedText.contains("'"), "Failed to escape '");
        assertFalse(escapedText.contains("\""), "Failed to escape \"");
        assertFalse(escapedText.contains("&&"), "Failed to escape &");
    }

    @Test
    public void escapeApos()
    {
        assertFalse(XMLUtils.escape("'").equals("&apos;"), "' wrongly escaped to non-HTML &apos;");
    }

    @Test
    public void escapeEmptyString()
    {
        assertEquals("", XMLUtils.escape(""), "\"\" should be \"\"");
    }

    @Test
    public void escapeWithNull()
    {
        assertNull(XMLUtils.escape(null), "null should be null");
    }

    @Test
    public void escapeNonAscii()
    {
        assertEquals("\u0123", XMLUtils.escape("\u0123"), "Non-ASCII characters were escaped");
    }

    @Test
    public void escapeAttributeValue()
    {
        String escapedText = XMLUtils.escapeAttributeValue("a < a' && a' < a\" => a < a\" {");

        assertFalse(escapedText.contains("<"), "Failed to escape <");
        assertFalse(escapedText.contains(">"), "Failed to escape >");
        assertFalse(escapedText.contains("'"), "Failed to escape '");
        assertFalse(escapedText.contains("\""), "Failed to escape \"");
        assertFalse(escapedText.contains("&&"), "Failed to escape &");
        assertFalse(escapedText.contains("{"), "Failed to escape {");
    }

    @Test
    public void escapeAttributeValueApos()
    {
        assertFalse(XMLUtils.escapeAttributeValue("'").equals("&apos;"), "' wrongly escaped to non-HTML &apos;");
    }

    @Test
    public void escapeFAttributeValueEmptyString()
    {
        assertEquals("", XMLUtils.escapeAttributeValue(""), "\"\" should be \"\"");
    }

    @Test
    public void escapeFAttributeValueWithNull()
    {
        assertNull(XMLUtils.escapeAttributeValue(null), "null should be null");
    }

    @Test
    public void escapeAttributeValueNonAscii()
    {
        assertEquals("\u0123", XMLUtils.escapeAttributeValue("\u0123"), "Non-ASCII characters were escaped");
    }

    @Test
    public void escapeElementContent()
    {
        String escapedText = XMLUtils.escapeElementContent("a < a' && a' < a\" => a < a\"");

        assertFalse(escapedText.contains("<"), "Failed to escape <");
        assertFalse(escapedText.contains(">"), "Failed to escape >");
        assertTrue(escapedText.contains("'"), "Wrongfully escaped '");
        assertTrue(escapedText.contains("\""), "Wrongfully escaped \"");
        assertFalse(escapedText.contains("&&"), "Failed to escape &");
    }

    @Test
    public void escapeElementContentEmptyString()
    {
        assertEquals("", XMLUtils.escapeElementContent(""), "\"\" should be \"\"");
    }

    @Test
    public void escapeElementContentWithNull()
    {
        assertNull(XMLUtils.escapeElementContent(null), "null should be null");
    }

    @Test
    public void escapeElementContentNonAscii()
    {
        assertEquals("\u0123", XMLUtils.escapeElementContent("\u0123"), "Non-ASCII characters were escaped");
    }

    @Test
    public void unescape()
    {
        assertEquals("&'\"<>", XMLUtils.unescape("&amp;&apos;&quot;&lt;&gt;"), "Failed to unescaped named entities");
        assertEquals("&'\"<>", XMLUtils.unescape("&#38;&#39;&#34;&#60;&#62;"), "Failed to unescaped decimal entities");
        assertEquals("&'\"<>", XMLUtils.unescape("&#038;&#0039;&#00034;&#000060;&#0000062;"),
            "Failed to unescaped decimal entities with leading zeros");
        assertEquals("&'\"<<>>", XMLUtils.unescape("&#x26;&#x27;&#x22;&#x3c;&#x3C;&#x3e;&#x3E;"),
            "Failed to unescaped hexadecimal entities");
        assertEquals("&'\"<<>>", XMLUtils.unescape("&#x026;&#x0027;&#x00022;&#x00003c;&#x0003C;&#x003e;&#x03E;"),
            "Failed to unescaped hexadecimal entities with leading zeros");
    }

    @Test
    public void unescapeEmptyString()
    {
        assertEquals("", XMLUtils.unescape(""), "\"\" should be \"\"");
    }

    @Test
    public void unescapeWithNull()
    {
        assertNull(XMLUtils.unescape(null), "null should be null");
    }

    @Test
    public void unescapeOtherEscapes()
    {
        assertEquals("&deg;", XMLUtils.unescape("&deg;"), "Extra named entities were unescaped");
        assertEquals("&#65;", XMLUtils.unescape("&#65;"), "Extra decimal entities were unescaped");
        assertEquals("&#x5;", XMLUtils.unescape("&#x5;"), "Extra hexadecimal entities were unescaped");
    }

    @Test
    public void createDomDocument()
    {
        // Nothing much that we can test here...
        assertNotNull(XMLUtils.createDOMDocument());
    }

    @Test
    public void serializeNode()
    {
        HTMLDocumentImpl node = new HTMLDocumentImpl();
        String serialize = XMLUtils.serialize(node, false);
        assertEquals("", serialize);

        Element body = node.createElement("body");
        node.setBody((HTMLElement) body);
        body.setAttribute("class", "toto");
        serialize = XMLUtils.serialize(node, false);
        assertEquals("<HTML><HEAD/><BODY class=\"toto\"/></HTML>", serialize);
    }
}
