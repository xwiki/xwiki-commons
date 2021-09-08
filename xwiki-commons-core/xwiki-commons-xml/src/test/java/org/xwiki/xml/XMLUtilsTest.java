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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.html.dom.HTMLDocumentImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.SAXException;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link XMLUtils}.
 *
 * @version $Id$
 * @since 1.6M1
 */
public class XMLUtilsTest
{
    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.DEBUG);

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
        String escapedText = XMLUtils.escapeElementContent("a < a' && a' < a\" => a < a\"");

        assertFalse(escapedText.contains("<"), "Failed to escape <");
        assertFalse(escapedText.contains(">"), "Failed to escape >");
        assertTrue(escapedText.contains("'"), "Wrongfully escaped '");
        assertTrue(escapedText.contains("\""), "Wrongfully escaped \"");
        assertFalse(escapedText.contains("&&"), "Failed to escape &");
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

    @Test
<<<<<<< HEAD
    void disableExternalEntities()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException, IOException
    {
        File tempFile = File.createTempFile("file", ".txt");

        FileUtils.write(tempFile, "external", StandardCharsets.UTF_8);

        DOMImplementationLS ls =
            (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS 3.0");
        LSInput input = ls.createLSInput();
        input.setStringData("<?xml version='1.0' encoding='UTF-8'?>" + "<!DOCTYPE root[<!ENTITY xxe SYSTEM 'file://"
            + tempFile.getAbsolutePath() + "' >]><root>&xxe;</root>");

        Document result = XMLUtils.parse(input);
        assertNotEquals("external", result.getDocumentElement().getTextContent());
    }

    @Test
    void extractXML() throws Exception
    {
        LSInput input = inputForExtractXML("<root>hello world</root>");
        Document document = XMLUtils.parse(input);

        assertEquals("<root>llo world</root>", XMLUtils.extractXML(document, 2, 40));
    }

    @Test
    void extractXMLWithIllegalStart() throws Exception
    {
        LSInput input = inputForExtractXML("<root>whatever</root>");
        Document document = XMLUtils.parse(input);

        Throwable exception = assertThrows(RuntimeException.class, () -> {
            XMLUtils.extractXML(document, -2, 40);
        });
        assertEquals("Failed to extract XML", exception.getMessage());
        assertEquals(SAXException.class, exception.getCause().getClass());
        assertEquals("Start must be greater than or equal to 0", exception.getCause().getMessage());
    }

    @Test
    void extractXMLWithIllegalLength() throws Exception
    {
        LSInput input = inputForExtractXML("<root>whatever</root>");
        Document document = XMLUtils.parse(input);

        Throwable exception = assertThrows(RuntimeException.class, () -> {
            XMLUtils.extractXML(document, 0, 0);
        });
        assertEquals("Failed to extract XML", exception.getMessage());
        assertEquals(SAXException.class, exception.getCause().getClass());
        assertEquals("Length must be greater than 0", exception.getCause().getMessage());
    }

    @Test
    void extractXMLWithTooLongTextNode() throws Exception
    {
        LSInput input = inputForExtractXML("<root>hello world</root>");
        Document document = XMLUtils.parse(input);

        assertEquals("<root>llo</root>", XMLUtils.extractXML(document, 2, 4));
        assertEquals(Level.DEBUG, logCapture.getLogEvent(0).getLevel());
        assertEquals(String.format("Error [%s] from xml transformer", "Length limit reached"),
            logCapture.getMessage(0));
    }

    @Test
    @Disabled("because of unidentified location where code writes directly to the console")
    void extractXMLWithBrokenInput() throws Exception
    {
        LSInput input = inputForExtractXML("<root>hello <b>world</a></root><garbage/>");
        Document document = XMLUtils.parse(input);

        assertNull(document, "we should not parse broken XML sucessfully ");
        assertEquals("", XMLUtils.extractXML(document, 2, 40));

        // XXX: we expect the corresponding error message to go through our error listener
        // but it does not.
        // instead our code logs as warning:
        assertEquals(Level.WARN, logCapture.getLogEvent(0).getLevel());
        assertEquals(String.format("Cannot parse XML document: [%s]", "null"),
            logCapture.getMessage(0));
        // but then another part of the code writes to the console; something like
        //  [[Fatal Error] :1:23: The element type "b" must be terminated by the matching end-tag "</b>"
    }

    private LSInput inputForExtractXML(String xmlContent) throws Exception
    {
        DOMImplementationLS lsImpl =
            (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS 3.0");
        LSInput input = lsImpl.createLSInput();
        input.setStringData(xmlContent);
        return input;
    }
}
