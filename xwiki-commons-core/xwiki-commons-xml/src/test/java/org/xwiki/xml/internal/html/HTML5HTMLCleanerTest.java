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
package org.xwiki.xml.internal.html;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xwiki.xml.html.HTMLCleanerConfiguration;

/**
 * Unit tests for {@link DefaultHTMLCleaner} in the HTML5 configuration.
 *
 * @version $Id$
 * @since 14.0RC1
 */
public class HTML5HTMLCleanerTest extends DefaultHTMLCleanerTest
{
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE html>\n";

    private static final String HEADER_FULL = HEADER + "<html><head></head><body>";

    /**
     * @return The expected XHTML 5 header.
     */
    @Override
    public String getHeader()
    {
        return HEADER;
    }

    /**
     * @return The expected full XHTML 5 header up to &lt;body&gt;.
     */
    @Override
    public String getHeaderFull()
    {
        return HEADER_FULL;
    }

    @BeforeEach
    @Override
    void setUpCleaner()
    {
        super.setUpCleaner();
        HashMap<String, String> parameters = new HashMap<>(this.cleanerConfiguration.getParameters());
        parameters.put(HTMLCleanerConfiguration.HTML_VERSION, "5");
        this.cleanerConfiguration.setParameters(parameters);
    }

    /**
     * Disable SVG test until https://sourceforge.net/p/htmlcleaner/bugs/228/ is fixed.
     *
     * This test should be removed again once it has been fixed to re-enable the parent test.
     */
    @Test
    @Override
    @Disabled("See https://sourceforge.net/p/htmlcleaner/bugs/228/")
    void cleanSVGTags() throws Exception
    {
        String input =
            "<p>before</p>\n" + "<p><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n"
                + "<circle cx=\"100\" cy=\"50\" fill=\"red\" r=\"40\" stroke=\"black\" stroke-width=\"2\"></circle>\n"
                + "</svg></p>\n" + "<p>after</p>\n";
        assertHTML(input, getHeaderFull() + input + FOOTER);
    }

    /**
     * Disable style test until https://sourceforge.net/p/htmlcleaner/bugs/229/ is fixed.
     *
     * This test should be removed again once it has been fixed to re-enable the parent test.
     */
    @Test
    @Override
    @Disabled("See https://sourceforge.net/p/htmlcleaner/bugs/229/")
    void styleAndCData()
    {
        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>",
            "<style type=\"text/css\"><![CDATA[\na { color: red; }\n]]></style>");

        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>",
            "<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>");

        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na>span { color: blue;}\n/*]]>*/</style>",
            "<style type=\"text/css\">a&gt;span { color: blue;}</style>");

        assertHTMLWithHeadContent("<style>/*<![CDATA[*/\n<>\n/*]]>*/</style>", "<style>&lt;&gt;</style>");
        assertHTMLWithHeadContent("<style>/*<![CDATA[*/\n<>\n/*]]>*/</style>", "<style><></style>");
    }

    /**
     * This tests various invalid list usages. With HTML5, the lists are cleaned by HTMLCleaner sometimes and thus the
     * list-style-type is not set to none as in the custom filter in XWiki. Further, HTML comments are not moved
     * around as it was the case with HTML 4.
     */
    @Test
    void cleanNonXHTMLLists()
    {
        // Fixing invalid list item.
        assertHTML("<ul><li>item</li></ul>", "<li>item</li>");

        assertHTML("<ul><li>item1<ul><li>item2</li></ul></li></ul>", "<ul><li>item1</li><ul><li>item2</li></ul></ul>");
        assertHTML("<ul><li>item1<ul><li>item2<ul><li>item3</li></ul></li></ul></li></ul>",
            "<ul><li>item1</li><ul><li>item2</li><ul><li>item3</li></ul></ul></ul>");
        assertHTML("<ul><li style=\"list-style-type: none\"><ul><li>item</li></ul></li></ul>",
            "<ul><ul><li>item</li></ul></ul>");
        assertHTML("<ul> <li style=\"list-style-type: none\"><ul><li>item</li></ul></li></ul>",
            "<ul> <ul><li>item</li></ul></ul>");
        assertHTML("<ul><li>item1<ol><li>item2</li></ol></li></ul>", "<ul><li>item1</li><ol><li>item2</li></ol></ul>");
        assertHTML("<ol><li>item1<ol><li>item2<ol><li>item3</li></ol></li></ol></li></ol>",
            "<ol><li>item1</li><ol><li>item2</li><ol><li>item3</li></ol></ol></ol>");
        assertHTML("<ol><li style=\"list-style-type: none\"><ol><li>item</li></ol></li></ol>",
            "<ol><ol><li>item</li></ol></ol>");
        assertHTML("<ul><li>item1<ul><li style=\"list-style-type: none\"><ul><li>item2</li></ul></li>"
            + "<li>item3</li></ul></li></ul>", "<ul><li>item1</li><ul><ul><li>item2</li></ul><li>item3</li></ul></ul>");

        assertHTML("<ul>\n\n<li><p>text</p></li></ul>", "<ul>\n\n<p>text</p></ul>");
        assertHTML("<ul><li>item</li><!--x-->  <li><p>text</p></li></ul>", "<ul><li>item</li><!--x-->  "
            + "<p>text</p></ul>");
        assertHTML("<ul> \n<li><em>1</em><!--x-->2<ins>3</ins></li><li>item</li></ul>",
            "<ul> \n<em>1</em><!--x-->2<ins>3</ins><li>item</li></ul>");
    }

    /**
     * Test that the &lt;tt&gt;-tag is properly converted to a span in HTML5.
     */
    @Test
    @Override
    void ttElement()
    {
        assertHTML("<p><span class=\"monospace\">Monospace Text</span></p>", "<tt>Monospace Text</tt>");
    }
}
