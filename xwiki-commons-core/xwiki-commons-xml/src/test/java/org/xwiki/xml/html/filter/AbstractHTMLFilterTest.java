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
package org.xwiki.xml.html.filter;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.internal.DefaultExecution;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.internal.html.DefaultHTMLCleaner;
import org.xwiki.xml.internal.html.filter.AttributeFilter;
import org.xwiki.xml.internal.html.filter.BodyFilter;
import org.xwiki.xml.internal.html.filter.ControlCharactersFilter;
import org.xwiki.xml.internal.html.filter.FontFilter;
import org.xwiki.xml.internal.html.filter.LinkFilter;
import org.xwiki.xml.internal.html.filter.ListFilter;
import org.xwiki.xml.internal.html.filter.ListItemFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AbstractHTMLFilter}.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList({
    LinkFilter.class,
    ListFilter.class,
    ListItemFilter.class,
    FontFilter.class,
    BodyFilter.class,
    AttributeFilter.class,
    DefaultHTMLCleaner.class,
    DefaultExecution.class,
    ControlCharactersFilter.class
})
public class AbstractHTMLFilterTest
{
    private AbstractHTMLFilter htmlFilter = new AbstractHTMLFilter()
    {
        @Override
        public void filter(Document document, Map<String, String> cleaningParameters)
        {
            // do nothing
        }
    };

    private HTMLCleaner cleaner;

    @BeforeEach
    public void setUp(ComponentManager componentManager) throws Exception
    {
        this.cleaner = componentManager.getInstance(HTMLCleaner.class);
    }

    @Test
    void filterChildren()
    {
        Document document =
            this.cleaner.clean(new StringReader("<html><head><body><p>test1</p><p>test2</p></body></html>"));
        List<Element> body = htmlFilter.filterChildren(document.getDocumentElement(), "body");
        assertEquals(1, body.size());
        List<Element> paragraphs = htmlFilter.filterChildren(body.get(0), "p");
        assertEquals(2, paragraphs.size());
        assertEquals("test1", paragraphs.get(0).getTextContent());
        assertEquals("test2", paragraphs.get(1).getTextContent());
    }

    @Test
    void filterDescendants()
    {
        Document document =
            this.cleaner.clean(new StringReader("<html><head><body><p>test1</p><p>test2</p></body></html>"));
        List<Element> paragraphs = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "p" });
        assertEquals(2, paragraphs.size());
        assertEquals("test1", paragraphs.get(0).getTextContent());
        assertEquals("test2", paragraphs.get(1).getTextContent());
    }

    @Test
    void filterDescendantsWithElementSelector()
    {
        Document document =
            this.cleaner.clean(new StringReader("<html><head><body><p>test1</p><p class=\"myClass\">test2</p></body>"
                + "</html>"));
        List<Element> paragraphs = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "p" },
            element -> element.hasAttribute("class") && element.getAttribute("class").equals("myClass"));
        assertEquals(1, paragraphs.size());
        assertEquals("test2", paragraphs.get(0).getTextContent());
    }

    @Test
    void hasAttribute()
    {
        Document document =
            this.cleaner.clean(new StringReader("<html><head><body><p>test1</p><p class=\"myClass\">test2</p></body>"
                + "</html>"));
        List<Element> paragraphs = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "p" });
        assertEquals(2, paragraphs.size());
        assertFalse(htmlFilter.hasAttribute(paragraphs, "class", false));

        document =
            this.cleaner.clean(new StringReader("<html><head><body><p class=\"anotherClass\">test1</p>"
                + "<p class=\"myClass\">test2</p></body></html>"));
        paragraphs = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "p" });
        assertEquals(2, paragraphs.size());
        assertTrue(htmlFilter.hasAttribute(paragraphs, "class", false));
        assertFalse(htmlFilter.hasAttribute(paragraphs, "class", true));

        document =
            this.cleaner.clean(new StringReader("<html><head><body><p class=\"myClass\">test1</p>"
                + "<p class=\"myClass\">test2</p></body></html>"));
        paragraphs = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "p" });
        assertEquals(2, paragraphs.size());
        assertTrue(htmlFilter.hasAttribute(paragraphs, "class", true));
    }

    @Test
    void replaceWithChildren()
    {
        Document document =
            this.cleaner.clean(new StringReader("<html><head><body><div><p>test1</p><p>test2</p></div></body></html>"));

        List<Element> div = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "div" });
        assertEquals(1, div.size());

        htmlFilter.replaceWithChildren(div.get(0));

        div = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "div" });
        assertTrue(div.isEmpty());

        List<Element> paragraphs = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "p" });
        assertEquals(2, paragraphs.size());
    }

    @Test
    void moveChildren()
    {
        Document document =
            this.cleaner.clean(new StringReader("<html><body><div><p>test1</p><p>test2</p></div>"
                + "<span>bla</span></body></html>"));

        List<Element> divs = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "div" });
        assertEquals(1, divs.size());
        assertEquals(2, htmlFilter.filterChildren(divs.get(0), "p").size());

        List<Element> spans = htmlFilter.filterDescendants(document.getDocumentElement(), new String[]{ "span" });
        assertEquals(1, spans.size());
        assertTrue(htmlFilter.filterChildren(spans.get(0), "p").isEmpty());

        htmlFilter.moveChildren(divs.get(0), spans.get(0));

        assertEquals(2, htmlFilter.filterChildren(spans.get(0), "p").size());
        assertTrue(htmlFilter.filterChildren(divs.get(0), "p").isEmpty());
    }
}
