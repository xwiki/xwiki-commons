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
package org.xwiki.xml.internal.html.filter;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xml.html.DefaultHTMLElementSanitizerComponentList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link SanitizerDetectorFilter}.
 *
 * @version $Id$
 */
@ComponentTest
@DefaultHTMLElementSanitizerComponentList
class SanitizerDetectorFilterTest
{
    private static final String DIV = "div";

    private static final String A = "a";

    private static final String HREF = "href";

    @InjectMockComponents
    private SanitizerDetectorFilter sanitizerDetectorFilter;

    @Test
    void filterWithComment() throws ParserConfigurationException
    {
        Document document = getDocument();

        Element element = document.createElement(DIV);
        document.getDocumentElement().appendChild(element);

        Comment comment = document.createComment("This is a comment");
        element.appendChild(comment);

        assertTrue(isFiltered(document));
    }

    @Test
    void filterWithValidContent() throws ParserConfigurationException
    {
        Document document = getDocument();

        Element element = document.createElement(A);
        element.setAttribute(HREF, "https://www.xwiki.org");
        document.getDocumentElement().appendChild(element);

        assertFalse(isFiltered(document));
    }

    @Test
    void filterWithForbiddenElement() throws ParserConfigurationException
    {
        Document document = getDocument();

        Element div = document.createElement(DIV);
        Element iframe = document.createElement("iframe");
        document.getDocumentElement().appendChild(div);
        div.appendChild(iframe);

        assertTrue(isFiltered(document));
    }

    @Test
    void filterWithForbiddenAttribute() throws ParserConfigurationException
    {
        Document document = getDocument();

        Element div = document.createElement(DIV);
        Element a = document.createElement(A);
        a.setAttribute(HREF, "javascript:alert()");
        document.getDocumentElement().appendChild(div);
        div.appendChild(a);

        assertTrue(isFiltered(document));
    }

    @Test
    void filterWithStyle() throws ParserConfigurationException
    {
        Document document = getDocument();

        Element div = document.createElement(DIV);
        Element style = document.createElement("style");
        document.getDocumentElement().appendChild(div);
        div.appendChild(style);

        assertTrue(isFiltered(document));
    }

    @Test
    void filterWithInvalidNamespace() throws ParserConfigurationException
    {
        Document document = getDocument();

        Element div = document.createElement(DIV);
        Element line = document.createElement("line");
        document.getDocumentElement().appendChild(div);
        div.appendChild(line);

        assertTrue(isFiltered(document));
    }

    private boolean isFiltered(Document document)
    {
        this.sanitizerDetectorFilter.filter(document, Map.of());
        return Boolean.parseBoolean(
            document.getDocumentElement().getAttribute(SanitizerDetectorFilter.ATTRIBUTE_FILTERED));
    }

    private static Document getDocument() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("html");
        document.appendChild(rootElement);
        return document;
    }
}
