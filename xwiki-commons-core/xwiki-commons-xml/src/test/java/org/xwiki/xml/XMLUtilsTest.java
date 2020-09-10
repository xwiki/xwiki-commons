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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link XMLUtils}.
 *
 * @version $Id$
 * @since 1.6M1
 */
public class XMLUtilsTest
{
    @Test
    void escapeXMLComment()
    {
        assertEquals("-\\- ", XMLUtils.escapeXMLComment("-- "));
        assertEquals("\\\\", XMLUtils.escapeXMLComment("\\"));
        assertEquals("-\\", XMLUtils.escapeXMLComment("-"));
        assertEquals("-\\-\\-\\", XMLUtils.escapeXMLComment("---"));
        assertEquals("- ", XMLUtils.escapeXMLComment("- "));
    }

    @Test
    void unescapeXMLComment()
    {
        assertEquals("", XMLUtils.unescapeXMLComment("\\"));
        assertEquals("\\", XMLUtils.unescapeXMLComment("\\\\"));
        assertEquals("--", XMLUtils.unescapeXMLComment("\\-\\-"));
        assertEquals("--", XMLUtils.unescapeXMLComment("\\-\\-\\"));
    }

    @Test
    void escape()
    {
        String escapedText = XMLUtils.escape("a < a' && a' < a\" => a < a\"");

        assertFalse(escapedText.contains("<"), "Failed to escape <");
        assertFalse(escapedText.contains(">"), "Failed to escape >");
        assertFalse(escapedText.contains("'"), "Failed to escape '");
        assertFalse(escapedText.contains("\""), "Failed to escape \"");
        assertFalse(escapedText.contains("&&"), "Failed to escape &");
    }

    @Test
    void escapeObjectContent()
    {
        String content = "a < a' && a' < a\" => a < a\"";
        StringBuilder sb = new StringBuilder();
        sb.append(content);

        String expected = XMLUtils.escape(content);
        String actual = XMLUtils.escape(sb);

        assertEquals(expected, actual);
    }

    @Test
    void escapeElementText()
    {
        String actual = XMLUtils.escapeElementText("a < a' && a' < a\" => a < a\"");
        assertEquals("a &#60; a' &#38;&#38; a' &#60; a\" => a &#60; a\"", actual);
    }

    @Test
    void escapeElementTextNull()
    {
        assertNull(XMLUtils.escapeElementText(null));
    }

    @Test
    void escapeApos()
    {
        assertNotEquals("&apos;", XMLUtils.escape("'"), "' wrongly escaped to non-HTML &apos;");
    }

    @Test
    void escapeEmptyString()
    {
        assertEquals("", XMLUtils.escape(""), "\"\" should be \"\"");
    }

    @Test
    void escapeWithNull()
    {
        assertNull(XMLUtils.escape(null), "null should be null");
    }

    @Test
    void escapeNonAscii()
    {
        assertEquals("\u0123", XMLUtils.escape("\u0123"), "Non-ASCII characters were escaped");
    }

    @Test
    void escapeAttributeValue()
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
    void escapeAttributeValueContentNull()
    {
        StringBuilder sb = null;
        assertNull(XMLUtils.escapeAttributeValue(sb));
    }

    @Test
    void escapeAttributeValueApos()
    {
        assertNotEquals("&apos;", XMLUtils.escapeAttributeValue("'"), "' wrongly escaped to non-HTML &apos;");
    }

    @Test
    void escapeFAttributeValueEmptyString()
    {
        assertEquals("", XMLUtils.escapeAttributeValue(""), "\"\" should be \"\"");
    }

    @Test
    void escapeFAttributeValueWithNull()
    {
        assertNull(XMLUtils.escapeAttributeValue(null), "null should be null");
    }

    @Test
    void escapeAttributeValueNonAscii()
    {
        assertEquals("\u0123", XMLUtils.escapeAttributeValue("\u0123"), "Non-ASCII characters were escaped");
    }

    @Test
    void escapeElementContent()
    {
        String actual = XMLUtils.escapeElementContent("a < a' && a' < a\" => a < a\"");
        assertEquals("a &#60; a' &#38;&#38; a' &#60; a\" => a &#60; a\"", actual);
    }

    @Test
    void escapeElementContentObjectContent()
    {
        String expected = XMLUtils.escapeElementContent("a < a' && a' < a\" => a < a\"");
        StringBuilder content = new StringBuilder();
        content.append("a < a' && a' < a\" => a < a\"");
        String actual = XMLUtils.escapeElementContent(content);
        assertEquals(expected, actual);
    }

    @Test
    void escapeElementContentContentNull()
    {
        StringBuilder content = null;
        assertNull(XMLUtils.escapeElementContent(content));
    }

    @Test
    void escapeElementContentEmptyString()
    {
        assertEquals("", XMLUtils.escapeElementContent(""), "\"\" should be \"\"");
    }

    @Test
    void escapeElementContentWithNull()
    {
        assertNull(XMLUtils.escapeElementContent(null), "null should be null");
    }

    @Test
    void escapeElementContentNonAscii()
    {
        assertEquals("\u0123", XMLUtils.escapeElementContent("\u0123"), "Non-ASCII characters were escaped");
    }

    @Test
    void unescape()
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
    void unescapeObjectContent()
    {
        StringBuilder content = new StringBuilder();
        content.append("&amp;&apos;&quot;&lt;&gt;");
        assertEquals("&'\"<>", XMLUtils.unescape(content));
    }

    @Test
    void unescapeNullContent()
    {
        StringBuilder content = null;
        assertNull(XMLUtils.unescape(content));
    }

    @Test
    void unescapeEmptyString()
    {
        assertEquals("", XMLUtils.unescape(""), "\"\" should be \"\"");
    }

    @Test
    void unescapeWithNull()
    {
        assertNull(XMLUtils.unescape(null), "null should be null");
    }

    @Test
    void unescapeOtherEscapes()
    {
        assertEquals("&deg;", XMLUtils.unescape("&deg;"), "Extra named entities were unescaped");
        assertEquals("&#65;", XMLUtils.unescape("&#65;"), "Extra decimal entities were unescaped");
        assertEquals("&#x5;", XMLUtils.unescape("&#x5;"), "Extra hexadecimal entities were unescaped");
    }

    @Test
    void createDomDocument()
    {
        // Nothing much that we can test here...
        assertNotNull(XMLUtils.createDOMDocument());
    }

    @Test
    void serializeNode()
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
