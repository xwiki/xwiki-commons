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
package org.htmlcleaner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link XWikiDOMSerializer}.
 *
 * @version $Id$
 */
class XWikiDOMSerializerTest
{
    private static final String HTML = "html";

    private final CleanerProperties properties = new CleanerProperties();

    private DocumentBuilder documentBuilder;

    @BeforeEach
    void setUp() throws Exception
    {
        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Test
    void createDOMWithoutDocType() throws Exception
    {
        TagNode rootNode = new TagNode(HTML);
        rootNode.addChild(new TagNode("body"));

        Document document = new XWikiDOMSerializer(this.properties).createDOM(this.documentBuilder, rootNode);

        assertNull(document.getDoctype());
        assertTrue(document.getStrictErrorChecking());
        Element root = document.getDocumentElement();
        assertEquals(HTML, root.getTagName());
        assertEquals("body", root.getFirstChild().getNodeName());
    }

    @Test
    void createDOMWithDocTypeWithoutQualifiedName() throws Exception
    {
        TagNode rootNode = new TagNode(HTML);
        rootNode.setDocType(new DoctypeToken(null, null, null, null));

        Document document = new XWikiDOMSerializer(this.properties).createDOM(this.documentBuilder, rootNode);

        assertEquals(HTML, document.getDoctype().getName());
        assertEquals(HTML, document.getDocumentElement().getTagName());
    }

    @Test
    void createDOMWithUpperCaseDocType() throws Exception
    {
        TagNode rootNode = new TagNode(HTML);
        rootNode.setDocType(new DoctypeToken("HTML", "PUBLIC", "-//W3C//DTD HTML 4.01//EN",
            "http://www.w3.org/TR/html4/strict.dtd"));

        Document document = new XWikiDOMSerializer(this.properties).createDOM(this.documentBuilder, rootNode);

        assertEquals("HTML", document.getDoctype().getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", document.getDoctype().getPublicId());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", document.getDoctype().getSystemId());
        // The root element is always lower case even when the DOCTYPE name isn't.
        assertEquals(HTML, document.getDocumentElement().getTagName());
    }

    @Test
    void createDOMCopiesAndSanitizesRootAttributes() throws Exception
    {
        this.properties.setInvalidXmlAttributeNamePrefix("generated-");
        TagNode rootNode = new TagNode(HTML);
        rootNode.addAttribute("id", "root");
        rootNode.addAttribute("lang", "en");
        rootNode.addAttribute("1a", "fixed");
        rootNode.addAttribute("@#", "dropped");

        Document document = new XWikiDOMSerializer(this.properties).createDOM(this.documentBuilder, rootNode);

        Element root = document.getDocumentElement();
        assertEquals(3, root.getAttributes().getLength());
        assertEquals("en", root.getAttribute("lang"));
        assertEquals("fixed", root.getAttribute("generated-1a"));
        assertFalse(root.hasAttribute("@#"));
        // The id attribute is flagged as an ID attribute.
        assertSame(root, document.getElementById("root"));
        assertFalse(root.getAttributeNode("lang").isId());
    }

    @Test
    void createDOMKeepsInvalidRootAttributesWhenAllowed() throws Exception
    {
        this.properties.setAllowInvalidAttributeNames(true);
        TagNode rootNode = new TagNode(HTML);
        rootNode.addAttribute("1a", "kept");

        Document document = new XWikiDOMSerializer(this.properties).createDOM(this.documentBuilder, rootNode);

        assertFalse(document.getStrictErrorChecking());
        assertEquals("kept", document.getDocumentElement().getAttribute("1a"));
    }
}
