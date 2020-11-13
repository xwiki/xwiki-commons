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
package org.xwiki.xml.html;

import java.io.StringReader;

import org.apache.xerces.dom.DocumentImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.internal.DefaultExecution;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.xml.XMLUtils;
import org.xwiki.xml.internal.html.DefaultHTMLCleaner;
import org.xwiki.xml.internal.html.DefaultHTMLCleanerTest;
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
 * Unit tests for {@link org.xwiki.xml.html.HTMLUtils}.
 * 
 * @version $Id$
 * @since 1.8.3
 */
@ComponentTest
@ComponentList({
    LinkFilter.class,
    ListFilter.class,
    ListItemFilter.class,
    FontFilter.class,
    BodyFilter.class,
    AttributeFilter.class,
    ControlCharactersFilter.class,
    DefaultHTMLCleaner.class,
    DefaultExecution.class
})
class HTMLUtilsTest
{
    private HTMLCleaner cleaner;

    @BeforeEach
    void setUp(ComponentManager componentManager) throws Exception
    {
        this.cleaner = componentManager.getInstance(HTMLCleaner.class);
    }

    @Test
    void stripHTMLEnvelope()
    {
        Document document =
            this.cleaner.clean(new StringReader("<html><head><body><p>test1</p><p>test2</p></body></html>"));
        HTMLUtils.stripHTMLEnvelope(document);
        assertEquals(DefaultHTMLCleanerTest.HEADER + "<html><p>test1</p><p>test2</p></html>\n",
            HTMLUtils.toString(document));
    }

    @Test
    void stripTopLevelParagraph()
    {
        Document document = this.cleaner.clean(new StringReader("<html><head /><body><p>test</p></body></html>"));
        HTMLUtils.stripFirstElementInside(document, "body", "p");
        assertEquals(DefaultHTMLCleanerTest.HEADER + "<html><head></head><body>test</body></html>\n",
            HTMLUtils.toString(document));
    }

    @Test
    void testCornerCases()
    {
        // ensures that the empty element does not lead to error
        Document document = this.cleaner.clean(new StringReader("<html><head /><body foo=\"\"></body></html>"));
        assertEquals(DefaultHTMLCleanerTest.HEADER + "<html><head></head><body foo=\"\"></body></html>\n",
            HTMLUtils.toString(document));
    }

    @Test
    void containsElementSyntax()
    {
        assertTrue(HTMLUtils.containsElementSyntax("a < a"));
        assertTrue(HTMLUtils.containsElementSyntax("a & a"));

        assertFalse(HTMLUtils.containsElementSyntax("a >'\" a"));
    }
}
