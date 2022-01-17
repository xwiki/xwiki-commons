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
class HTML5HTMLCleanerTest extends DefaultHTMLCleanerTest
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
     * In HTML5, some elements that were deprecated/removed in XHTML 1.0 are not deprecated anymore. This overrides
     * the test to ensure they are not removed.
     */
    @Override
    @Test
    void conversionsFromHTML()
    {
        assertHTML("<p>this <b>is</b> highlighted but not important</p>",
            "this <b>is</b> highlighted but not important");
        assertHTML("<p><i>alternate voice</i></p>", "<i>alternate voice</i>");
        assertHTML("<del>strike</del>", "<strike>strike</strike>");
        assertHTML("<p><s>no longer relevant</s></p>", "<s>no longer relevant</s>");
        assertHTML("<p><u>misspell</u></p>", "<u>misspell</u>");
        assertHTML("<p style=\"text-align:center\">center</p>", "<center>center</center>");
        assertHTML("<p><span style=\"color:red;font-family:Arial;font-size:1.0em;\">This is some text!</span></p>",
            "<font face=\"Arial\" size=\"3\" color=\"red\">This is some text!</font>");
        assertHTML("<p><span style=\"font-size:1.6em;\">This is some text!</span></p>",
            "<font size=\"+3\">This is some text!</font>");
        assertHTML("<table><tbody><tr><td style=\"text-align:right;background-color:red;vertical-align:top\">"
            + "x</td></tr></tbody></table>", "<table><tr><td align=right valign=top bgcolor=red>x</td></tr></table>");
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
