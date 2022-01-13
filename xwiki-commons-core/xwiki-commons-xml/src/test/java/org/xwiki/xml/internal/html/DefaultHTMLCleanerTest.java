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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;
import org.xwiki.xml.html.filter.HTMLFilter;
import org.xwiki.xml.internal.html.filter.AttributeFilter;
import org.xwiki.xml.internal.html.filter.BodyFilter;
import org.xwiki.xml.internal.html.filter.ControlCharactersFilter;
import org.xwiki.xml.internal.html.filter.FontFilter;
import org.xwiki.xml.internal.html.filter.LinkFilter;
import org.xwiki.xml.internal.html.filter.ListFilter;
import org.xwiki.xml.internal.html.filter.ListItemFilter;
import org.xwiki.xml.internal.html.filter.UniqueIdFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DefaultHTMLCleaner}.
 *
 * @version $Id$
 * @since 1.6M1
 */
@ComponentTest
// @formatter:off
@ComponentList({
    ListFilter.class,
    ListItemFilter.class,
    FontFilter.class,
    BodyFilter.class,
    AttributeFilter.class,
    UniqueIdFilter.class,
    DefaultHTMLCleaner.class,
    LinkFilter.class,
    ControlCharactersFilter.class
})
// @formatter:on
public class DefaultHTMLCleanerTest
{
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";

    private static final String HEADER_FULL = HEADER + "<html><head></head><body>";

    private static final String FOOTER = "</body></html>\n";

    @InjectMockComponents
    protected DefaultHTMLCleaner cleaner;

    protected HTMLCleanerConfiguration cleanerConfiguration;

    /**
     * @return The expected XHTML 1.0 header.
     */
    public String getHeader()
    {
        return HEADER;
    }

    /**
     * @return The expected full XHTML 1.0 header up to &lt;body&gt;.
     */
    public String getHeaderFull()
    {
        return HEADER_FULL;
    }

    /**
     * Cleans using the cleaner configuration {@link DefaultHTMLCleanerTest#cleanerConfiguration}.
     *
     * Ensures that always the correct configuration is used and allows executing the same tests for HTML 4 and HTML 5.
     *
     * @param originalHtmlContent The content to clean as string.
     * @return The cleaned document.
     */
    protected Document clean(String originalHtmlContent)
    {
        return this.cleaner.clean(new StringReader(originalHtmlContent), cleanerConfiguration);
    }

    @BeforeEach
    void setUpCleaner()
    {
        this.cleanerConfiguration = this.cleaner.getDefaultConfiguration();
    }

    @Test
    void elementExpansion()
    {
        assertHTML("<p><textarea></textarea></p>", "<textarea/>");

        // Verify exceptions (by default elements are expanded).
        assertHTML("<p><br /></p>", "<p><br></p>");
        assertHTML("<hr />", "<hr>");
    }

    @Test
    void specialCharacters()
    {
        // The blank space is not a standard space, but a non-breaking space.
        assertHTML("<p>\"&amp;**notbold**&lt;notag&gt; </p>",
            "<p>&quot;&amp;**notbold**&lt;notag&gt;&nbsp;</p>");
        assertHTML("<p>\"&amp;</p>", "<p>\"&</p>");
        assertHTML("<p><img src=\"http://host.com/a.gif?a=foo&amp;b=bar\" /></p>",
            "<img src=\"http://host.com/a.gif?a=foo&amp;b=bar\" />");
        assertHTML("<p>&#xA;</p>", "<p>&#xA;</p>");

        // Verify that double quotes are escaped in attribute values
        assertHTML("<p value=\"script:&quot;&quot;\"></p>", "<p value='script:\"\"'");
    }

    @Test
    void closeUnbalancedTags()
    {
        assertHTML("<hr /><p>hello</p>", "<hr><p>hello");
    }

    @Test
    void conversionsFromHTML()
    {
        assertHTML("<p>this <strong>is</strong> bold</p>", "this <b>is</b> bold");
        assertHTML("<p><em>italic</em></p>", "<i>italic</i>");
        assertHTML("<del>strike</del>", "<strike>strike</strike>");
        assertHTML("<del>strike</del>", "<s>strike</s>");
        assertHTML("<ins>strike</ins>", "<u>strike</u>");
        assertHTML("<p style=\"text-align:center\">center</p>", "<center>center</center>");
        assertHTML("<p><span style=\"color:red;font-family:Arial;font-size:1.0em;\">This is some text!</span></p>",
            "<font face=\"Arial\" size=\"3\" color=\"red\">This is some text!</font>");
        assertHTML("<p><span style=\"font-size:1.6em;\">This is some text!</span></p>",
            "<font size=\"+3\">This is some text!</font>");
        assertHTML("<table><tbody><tr><td style=\"text-align:right;background-color:red;vertical-align:top\">"
            + "x</td></tr></tbody></table>", "<table><tr><td align=right valign=top bgcolor=red>x</td></tr></table>");
    }

    @Test
    void convertImageAlignment()
    {
        assertHTML("<p><img style=\"float:left\" /></p>", "<img align=\"left\"/>");
        assertHTML("<p><img style=\"float:right\" /></p>", "<img align=\"right\"/>");
        assertHTML("<p><img style=\"vertical-align:top\" /></p>", "<img align=\"top\"/>");
        assertHTML("<p><img style=\"vertical-align:middle\" /></p>", "<img align=\"middle\"/>");
        assertHTML("<p><img style=\"vertical-align:bottom\" /></p>", "<img align=\"bottom\"/>");
    }

    @Test
    void convertImplicitParagraphs()
    {
        assertHTML("<p>word1</p><p>word2</p><p>word3</p><hr /><p>word4</p>", "word1<p>word2</p>word3<hr />word4");

        // Don't convert when there are only spaces or new lines
        assertHTML("<p>word1</p>  \n  <p>word2</p>", "<p>word1</p>  \n  <p>word2</p>");

        // Ensure that whitespaces at the end works.
        assertHTML("\n ", "\n ");

        // Ensure that comments are not wrapped
        assertHTML("<!-- comment1 -->\n<p>hello</p>\n<!-- comment2 -->",
            "<!-- comment1 -->\n<p>hello</p>\n<!-- comment2 -->");

        // Ensure that comments don't prevent other elements to be wrapped with paragraphs.
        assertHTML("<!-- comment --><p><span>hello</span><!-- comment --></p><p>world</p>",
            "<!-- comment --><span>hello</span><!-- comment --><p>world</p>");
    }

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

        assertHTML("<ul>\n\n<li style=\"list-style-type: none\"><p>text</p></li></ul>", "<ul>\n\n<p>text</p></ul>");
        assertHTML("<ul><li>item<p>text</p></li><!--x-->  </ul>", "<ul><li>item</li><!--x-->  <p>text</p></ul>");
        assertHTML("<ul> \n<li style=\"list-style-type: none\"><em>1</em>2<ins>3</ins></li><!--x--><li>item</li></ul>",
            "<ul> \n<em>1</em><!--x-->2<ins>3</ins><li>item</li></ul>");
    }

    /**
     * Verify that scripts are not cleaned and that we can have a CDATA section inside. Also verify CDATA behaviors.
     */
    @Test
    void scriptAndCData()
    {
        assertHTML("<script type=\"text/javascript\">/*<![CDATA[*/\nalert(\"Hello World\")\n/*]]>*/</script>",
            "<script type=\"text/javascript\"><![CDATA[\nalert(\"Hello World\")\n]]></script>");

        assertHTML("<script type=\"text/javascript\">/*<![CDATA[*/\nalert(\"Hello World\")\n/*]]>*/</script>",
            "<script type=\"text/javascript\">//<![CDATA[\nalert(\"Hello World\")\n//]]></script>");

        assertHTML("<script type=\"text/javascript\">/*<![CDATA[*/\nalert(\"Hello World\")\n/*]]>*/</script>",
            "<script type=\"text/javascript\">/*<![CDATA[*/\nalert(\"Hello World\")\n/*]]>*/</script>");

        assertHTML("<script type=\"text/javascript\">/*<![CDATA[*/\n\n" + "function escapeForXML(origtext) {\n"
            + "   return origtext.replace(/\\&/g,'&'+'amp;').replace(/</g,'&'+'lt;')\n"
            + "       .replace(/>/g,'&'+'gt;').replace(/\'/g,'&'+'apos;').replace(/\"/g,'&'+'quot;');" + "}\n\n/*]]>*/"
            + "</script>", "<script type=\"text/javascript\">\n" + "/*<![CDATA[*/\n"
            + "function escapeForXML(origtext) {\n"
            + "   return origtext.replace(/\\&/g,'&'+'amp;').replace(/</g,'&'+'lt;')\n"
            + "       .replace(/>/g,'&'+'gt;').replace(/\'/g,'&'+'apos;').replace(/\"/g,'&'+'quot;');" + "}\n"
            + "/*]]>*/\n" + "</script>");

        assertHTML("<script>/*<![CDATA[*/\n<>\n/*]]>*/</script>", "<script>&lt;&gt;</script>");
        assertHTML("<script>/*<![CDATA[*/\n<>\n/*]]>*/</script>", "<script><></script>");

        // Verify that CDATA not inside SCRIPT or STYLE elements are considered comments in HTML and thus stripped
        // when cleaned.
        assertHTML("<p></p>", "<p><![CDATA[&]]></p>");
        assertHTML("<p>&amp;&amp;</p>", "<p>&<![CDATA[&]]>&</p>");
    }

    /**
     * Verify that inline style elements are not cleaned and that we can have a CDATA section inside.
     */
    @Test
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
     * Verify that we can control what filters are used for cleaning.
     */
    @Test
    void explicitFilterList()
    {
        this.cleanerConfiguration.setFilters(Collections.emptyList());
        String result = HTMLUtils.toString(clean("something"));
        // Note that if the default Body filter had been executed the result would have been:
        // <p>something</p>.
        assertEquals(getHeaderFull() + "something" + FOOTER, result);
    }

    /**
     * Verify that the restricted parameter works.
     */
    @Test
    void restrictedHtml()
    {
        Map<String, String> parameters = new HashMap<>(this.cleanerConfiguration.getParameters());
        parameters.put("restricted", "true");
        this.cleanerConfiguration.setParameters(parameters);
        Document document = clean("<script>alert(\"foo\")</script>");

        String textContent =
            document.getElementsByTagName("pre").item(0).getTextContent();
        assertEquals("alert(\"foo\")", textContent);

        String result = HTMLUtils.toString(document);
        assertEquals(getHeaderFull() + "<pre>alert(\"foo\")</pre>" + FOOTER, result);

        document = clean("<style>p {color:white;}</style>");

        textContent =
            document.getElementsByTagName("pre").item(0).getTextContent();
        assertEquals("p {color:white;}", textContent);

        result = HTMLUtils.toString(document);
        assertEquals(getHeaderFull() + "<pre>p {color:white;}</pre>" + FOOTER, result);
    }

    /**
     * Verify that passing a fully-formed XHTML header works fine.
     */
    @Test
    void fullXHTMLHeader()
    {
        assertHTML("<p>test</p>", getHeaderFull() + "<p>test</p>" + FOOTER);
    }

    /**
     * Test {@link UniqueIdFilter}.
     */
    @Test
    public void duplicateIds(ComponentManager componentManager) throws Exception
    {
        String actual = "<p id=\"x\">1</p><p id=\"xy\">2</p><p id=\"x\">3</p>";
        String expected = "<p id=\"x\">1</p><p id=\"xy\">2</p><p id=\"x0\">3</p>";
        List<HTMLFilter> filters = new ArrayList<>(this.cleanerConfiguration.getFilters());
        filters.add(componentManager.getInstance(HTMLFilter.class, "uniqueId"));
        this.cleanerConfiguration.setFilters(filters);
        assertEquals(getHeaderFull() + expected + FOOTER,
            HTMLUtils.toString(clean(actual)));
    }

    /**
     * Test that tags with a namespace are not considered as unknown tags by HTMLCleaner (see also <a
     * href="https://jira.xwiki.org/browse/XWIKI-9753">XWIKI-9753</a>).
     */
    @Test
    void cleanSVGTags() throws Exception
    {
        String input =
            "<p>before</p>\n" + "<p><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n"
                + "<circle cx=\"100\" cy=\"50\" fill=\"red\" r=\"40\" stroke=\"black\" stroke-width=\"2\"></circle>\n"
                + "</svg></p>\n" + "<p>after</p>\n";
        assertHTML(input, getHeaderFull() + input + FOOTER);
    }

    /**
     * Test that cleaning works when there's a TITLE element in the body (but with a namespace). The issue was that
     * HTMLCleaner would consider it a duplicate of the TITLE element in the HEAD even though it's namespaced. (see
     * also
     * <a href="https://jira.xwiki.org/browse/XWIKI-9753">XWIKI-9753</a>).
     */
    @Disabled("See https://jira.xwiki.org/browse/XWIKI-9753")
    @Test
    void cleanTitleWithNamespace()
    {
        // Test with TITLE in HEAD
        String input =
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
                + "  <head>\n"
                + "    <title>Title test</title>\n"
                + "  </head>\n"
                + "  <body>\n"
                + "    <p>before</p>\n"
                + "    <svg xmlns=\"http://www.w3.org/2000/svg\" height=\"300\" width=\"500\">\n"
                + "      <g>\n"
                + "        <title>SVG Title Demo example</title>\n"
                + "        <rect height=\"50\" style=\"fill:none; stroke:blue; stroke-width:1px\" width=\"200\" x=\"10\" "
                + "y=\"10\"></rect>\n" + "      </g>\n" + "    </svg>\n" + "    <p>after</p>\n";
        assertEquals(getHeader() + input + FOOTER,
            HTMLUtils.toString(clean(input)));
    }

    /**
     * Verify that a xmlns namespace set on the HTML element is not removed by default and it's removed if {@link
     * HTMLCleanerConfiguration#NAMESPACES_AWARE} is set to false.
     */
    @Test
    void cleanHTMLTagWithNamespace()
    {
        String input = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body>";

        // Default
        assertEquals(getHeader() + input + FOOTER,
            HTMLUtils.toString(clean(input)));

        // Configured for namespace awareness being false
        Map<String, String> parameters = new HashMap<>(this.cleanerConfiguration.getParameters());
        parameters.put(HTMLCleanerConfiguration.NAMESPACES_AWARE, "false");
        this.cleanerConfiguration.setParameters(parameters);
        assertEquals(getHeader() + "<html><head></head><body>" + FOOTER,
            HTMLUtils.toString(clean(input)));
    }

    /**
     * Test that cleaning an empty DIV works (it used to fail, see <a href="https://jira.xwiki.org/browse/XWIKI-4007">XWIKI-4007</a>).
     */
    @Test
    void cleanEmptyDIV()
    {
        String input = "<div id=\"y\"></div><div id=\"z\">something</div>";
        assertHTML(input, getHeaderFull() + input + FOOTER);
    }

    @Test
    void verifyLegendTagNotStripped()
    {
        String input = "<fieldset><legend>test</legend><div>content</div></fieldset>";
        assertHTML(input, getHeaderFull() + input + FOOTER);
    }

    @Test
    void verifySpanIsExpanded()
    {
        assertHTML("<p><span class=\"fa fa-icon\"></span></p>", "<span class=\"fa fa-icon\" />");
    }

    @Test
    void verifyExternalLinksAreSecure()
    {
        assertHTML("<p><a href=\"relativeLink\" target=\"_blank\">label</a></p>",
            "<a href=\"relativeLink\" target=\"_blank\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" rel=\" noopener noreferrer\" target=\"_blank\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"_blank\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" rel=\" noopener noreferrer\" target=\"someframe\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"someframe\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" target=\"_top\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"_top\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" target=\"_parent\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"_parent\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" target=\"_self\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"_self\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" rel=\"noopener noreferrer\" target=\"_blank\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"_blank\" rel=\"noopener\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" rel=\"noreferrer noopener\" target=\"_blank\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"_blank\" rel=\"noreferrer\">label</a>");
        assertHTML("<p><a href=\"http://xwiki.org\" rel=\"hello noopener noreferrer\" target=\"_blank\">label</a></p>",
            "<a href=\"http://xwiki.org\" target=\"_blank\" rel=\"hello\">label</a>");
    }

    @Test
    void verifyEntitiesAreNotBroken()
    {
        Document document = clean("<p>&Eacute;</p>");
        String content = document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals("É", content);
        assertHTML("<p>É</p>", "&Eacute;");

        document = clean("<p>&frac14;</p>");
        content = document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals("¼", content);
        assertHTML("<p>¼</p>", "&frac14;");

        document = clean("<p>&f!rac14;</p>");
        content = document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals("&f!rac14;", content);
        assertHTML("<p>&amp;f!rac14;</p>", "&f!rac14;");

        document = clean("<p>&frac12;</p>");
        content = document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals("½", content);
        assertHTML("<p>½</p>", "&frac12;");
    }

    @Test
    void entitiesWithTranslation()
    {
        String content = "<p>1&gt;2&amp;3&nbsp;4&frac12;5öüäăâîș</p>";
        String expectedContent = "1>2&3 4½5öüäăâîș";
        Document document = clean(content);
        String obtainedContent = document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals(expectedContent, obtainedContent);
        assertHTML("<p>1&gt;2&amp;3 4½5öüäăâîș</p>", content);

        Map<String, String> parameters = new HashMap<>(this.cleanerConfiguration.getParameters());
        parameters.put(HTMLCleanerConfiguration.TRANSLATE_SPECIAL_ENTITIES, "true");
        this.cleanerConfiguration.setParameters(parameters);
        assertHTML("<p>1&amp;gt;2&amp;amp;3 4½5öüäăâîș</p>",
            "<p>1&gt;2&amp;3&nbsp;4&frac12;5öüäăâîș</p>");
    }

    @Test
    void verifyLeadingSpacesAreKeptOnlyInInputValue()
    {
        assertHTML("<p><input type=\"hidden\" value=\"  fff\" /></p>", "<input type=\"hidden\" value=\"  fff\" />");
        assertHTML("<p><input type=\"hidden\" value=\"foo\" /></p>", "<input type=\"hidden\" value=\"foo\" />");
        assertHTML("<p><input type=\"hidden\" value=\"foo bar\" /></p>", "<input type=\"hidden\" value=\"foo bar\" />");
        assertHTML("<p><input class=\"fff\" type=\"hidden\" /></p>", "<input type=\"hidden\" class=\"  fff\" />");
        assertHTML("<p><input class=\"foo bar\" type=\"hidden\" value=\" foo bar  \" /></p>",
            "<input type=\"hidden   \" value=\" foo bar  \" class=\" foo bar  \"/>");
        assertHTML("<div class=\"foo bar\" title=\"foo bar\"></div>",
            "<div title=\" foo bar  \" class=\" foo bar  \"/>");
    }

    /**
     * @see <a href="https://jira.xwiki.org/browse/XCOMMONS-1293">XCOMMONS-1293</a>
     */
    @Test
    void verifyIFRAMECleaning() throws Exception
    {
        // TODO: these 2 lines need to be changed to the following when https://jira.xwiki.org/browse/XCOMMONS-1292 is
        // fixed:
        //          assertHTML("<iframe src=\"whatever\"></iframe>", "<iframe src=\"whatever\"/>");
        //          assertHTML("<iframe src=\"whatever\"></iframe>\r\n", "<iframe src=\"whatever\"/>\r\n");
        assertHTML("<p><iframe src=\"whatever\"></iframe></p>", "<iframe src=\"whatever\"/>");
        assertHTML("<p><iframe src=\"whatever\"></iframe>\r\n</p>", "<iframe src=\"whatever\"/>\r\n");
        assertHTML("<p>\r\n<iframe src=\"whatever\"></iframe></p>", "\r\n<iframe src=\"whatever\"/>");
        assertHTML("<p>\r\n<iframe src=\"whatever\"></iframe>\r\n</p>", "\r\n<iframe src=\"whatever\"/>\r\n");
        assertHTML("<p><iframe src=\"whatever\"></iframe><iframe src=\"whatever\"></iframe></p>",
            "<iframe src=\"whatever\"/><iframe src=\"whatever\"/>");
        assertHTML("<p><iframe src=\"whatever\"></iframe>\r\n<iframe src=\"whatever\"></iframe></p>",
            "<iframe src=\"whatever\"/>\r\n<iframe src=\"whatever\"/>");
        assertHTML("<p>\r\n<iframe src=\"whatever\"></iframe>\r\n<iframe src=\"whatever\"></iframe>\r\n</p>",
            "\r\n<iframe src=\"whatever\"/>\r\n<iframe src=\"whatever\"/>\r\n");
    }

    @Test
    void escapeHTMLCharsInAttributes() throws Exception
    {
        // Note: single quotes are not escaped since they're valid chars in attribute values that are surrounded by
        // quotes. And HTMLCleaner will convert single quoted attributes into double-quoted ones.
        String htmlInput = "<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff\">content</div>";
        Document document = clean(htmlInput);

        String textContent =
            document.getElementsByTagName("div").item(0).getAttributes().getNamedItem("foo").getTextContent();
        assertEquals("aaa\"bbb&ccc>ddd<eee'fff", textContent);

        htmlInput = "<div foo='aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff'>content</div>";
        document = clean(htmlInput);

        textContent =
            document.getElementsByTagName("div").item(0).getAttributes().getNamedItem("foo").getTextContent();
        assertEquals("aaa\"bbb&ccc>ddd<eee'fff", textContent);

        assertHTML("<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee'fff\">content</div>",
            "<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff\">content</div>");
        assertHTML("<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee'fff\">content</div>",
            "<div foo='aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff'>content</div>");
    }

    @Test
    void controlCharacters() throws Exception
    {
        String htmlInput = "<p>\u0008</p>";
        Document document = clean(htmlInput);

        String textContent =
            document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals(" ", textContent);
        assertHTML(" ", "\u0008");

        htmlInput = "<p>&#8;</p>";
        document = clean(htmlInput);

        // HtmlCleaner currently doesn't handle properly unicode characters: asking it to recognize them
        // involves that all entities will be escaped during the parsing and that's not what we want. So we
        // keep them encoded.
        // See https://sourceforge.net/p/htmlcleaner/bugs/221/
        textContent =
            document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals("&#8;", textContent);
        assertHTML("<p>&#8;</p>", "&#8;");

        htmlInput = "<p foo=\"&#8;\">content</p>";
        document = clean(htmlInput);

        // HtmlCleaner currently doesn't handle properly unicode characters: asking it to recognize them
        // involves that all entities will be escaped during the parsing and that's not what we want. So we
        // keep them encoded.
        textContent =
            document.getElementsByTagName("p").item(0).getAttributes().getNamedItem("foo").getTextContent();
        assertEquals("&#8;", textContent);
        assertHTML("<p foo=\"&#8;\">content</p>", "<p foo=\"&#8;\">content</p>");
    }

    @Test
    void ttElement()
    {
        assertHTML("<p><tt>Monospace Text</tt></p>", "<tt>Monospace Text</tt>");
    }

    protected void assertHTML(String expected, String actual)
    {
        Document documentValue = clean(actual);
        assertEquals(getHeaderFull() + expected + FOOTER, HTMLUtils.toString(documentValue));
    }

    private void assertHTMLWithHeadContent(String expected, String actual)
    {
        assertEquals(getHeader() + "<html><head>" + expected + "</head><body>" + FOOTER,
            HTMLUtils.toString(clean(actual)));
    }

    @Test
    void transformedDOMContent()
    {
        String htmlInput = "<img src=\"http://host.com/a.gif?a=foo&b=bar\" />";
        Document document = clean(htmlInput);

        String textContent =
            document.getElementsByTagName("img").item(0).getAttributes().getNamedItem("src").getTextContent();
        assertEquals("http://host.com/a.gif?a=foo&b=bar", textContent);

        htmlInput = "<img src=\"http://host.com/a.gif?a=foo&amp;b=bar\" />";
        document = clean(htmlInput);

        textContent =
            document.getElementsByTagName("img").item(0).getAttributes().getNamedItem("src").getTextContent();
        assertEquals("http://host.com/a.gif?a=foo&b=bar", textContent);
    }

    @Test
    public void preserveDoubleEscapingInAttributes() throws Exception
    {
        CleanerProperties cleanerProperties = new CleanerProperties();
        cleanerProperties.setDeserializeEntities(true);
        HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
        TagNode tagNode = cleaner.clean("<?xml version = \"1.0\"?><div foo=\"&amp;quot;\">&amp;quot;</div>");
        List<? extends TagNode> divList = tagNode.getElementListByName("div", true);
        assertEquals(1, divList.size());
        assertEquals("&quot;", divList.get(0).getText().toString());
        // This assert is failing: the attribute is deserialized to contain ".
        assertEquals("&quot;", divList.get(0).getAttributeByName("foo"));

        DomSerializer domSerializer = new DomSerializer(cleanerProperties, false);
        Document document = domSerializer.createDOM(tagNode);

        NodeList nodeList = document.getElementsByTagName("div");
        assertEquals(1, nodeList.getLength());
        assertEquals("&quot;", nodeList.item(0).getTextContent());
        assertEquals("&quot;", nodeList.item(0).getAttributes().getNamedItem("foo").getTextContent());

        document = clean("<div foo=\"&amp;quot;\">&amp;quot;</div>");
        nodeList = document.getElementsByTagName("div");
        assertEquals(1, nodeList.getLength());
        assertEquals("&quot;", nodeList.item(0).getTextContent());
        // We can never retrieve the expected value here since the encoded &amp; has been lost earlier.
        assertEquals("&quot;", nodeList.item(0).getAttributes().getNamedItem("foo").getTextContent());

        assertHTML("<div foo=\"&amp;quot;\">content</div>",
            "<div foo=\"&amp;quot;\">content</div>");
    }

    @Test
    @Disabled("See https://sourceforge.net/p/htmlcleaner/bugs/221/")
    public void parseWithUnicodeChars() throws Exception
    {
        CleanerProperties cleanerProperties = new CleanerProperties();

        cleanerProperties.setDeserializeEntities(true);
        cleanerProperties.setRecognizeUnicodeChars(true);
        cleanerProperties.setTranslateSpecialEntities(false);

        HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);
        TagNode tagNode = cleaner.clean("<?xml version = \"1.0\"?>"
            + "<div foo=\"&#169;\">&#169;</div>"
            + "<div foo=\"baz&gt;buz\">baz&gt;buz</div>"
            + "<div foo=\"baz&buz\">baz&buz</div>");
        List<? extends TagNode> divList = tagNode.getElementListByName("div", true);
        assertEquals(3, divList.size());

        assertEquals("©", divList.get(0).getText().toString());
        assertEquals("©", divList.get(0).getAttributeByName("foo"));

        assertEquals("baz>buz", divList.get(1).getText().toString());
        assertEquals("baz>buz", divList.get(1).getAttributeByName("foo"));

        assertEquals("baz&buz", divList.get(2).getText().toString());
        assertEquals("baz&buz", divList.get(2).getAttributeByName("foo"));

        DomSerializer domSerializer = new DomSerializer(cleanerProperties, false);
        Document document = domSerializer.createDOM(tagNode);

        NodeList nodeList = document.getElementsByTagName("div");
        assertEquals(3, nodeList.getLength());
        assertEquals("©", nodeList.item(0).getTextContent());
        assertEquals("©", nodeList.item(0).getAttributes().getNamedItem("foo").getTextContent());

        assertEquals("baz>buz", nodeList.item(1).getAttributes().getNamedItem("foo").getTextContent());

        assertEquals("baz&buz", nodeList.item(2).getAttributes().getNamedItem("foo").getTextContent());

        // BUG: This is failing with baz&gt;buz
        assertEquals("baz>buz", nodeList.item(1).getTextContent());
        // BUG: This is failing with baz&amp;buz
        assertEquals("baz&buz", nodeList.item(2).getTextContent());
    }

    @Test
    public void followingEncodedEntitiesAreProperlyKept()
    {
        String content = "<p><textarea>&#123;&#123;velocity}}machin&#123;&#123;/velocity}}</textarea></p>";
        Document document = clean(content);
        String textareaContent = document.getElementsByTagName("textarea").item(0).getTextContent();
        assertEquals("&#123;&#123;velocity}}machin&#123;&#123;/velocity}}", textareaContent);

        assertHTML("<p><textarea>&#123;&#123;velocity}}machin&#123;&#123;/velocity}}</textarea></p>", content);
    }
}
