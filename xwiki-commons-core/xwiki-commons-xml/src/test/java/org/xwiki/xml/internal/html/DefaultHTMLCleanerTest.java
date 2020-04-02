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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link org.xwiki.xml.internal.html.DefaultHTMLCleaner}.
 *
 * @version $Id$
 * @since 1.6M1
 */
@ComponentTest
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
public class DefaultHTMLCleanerTest
{
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";

    private static final String HEADER_FULL = HEADER + "<html><head></head><body>";

    private static final String FOOTER = "</body></html>\n";

    @InjectMockComponents
    private DefaultHTMLCleaner cleaner;

    @Test
    public void elementExpansion()
    {
        assertHTML("<p><textarea></textarea></p>", "<textarea/>");

        // Verify exceptions (by default elements are expanded).
        assertHTML("<p><br /></p>", "<p><br></p>");
        assertHTML("<hr />", "<hr>");
    }

    @Test
    public void specialCharacters()
    {
        assertHTML("<p>&quot;&amp;**notbold**&lt;notag&gt;&nbsp;</p>",
            "<p>&quot;&amp;**notbold**&lt;notag&gt;&nbsp;</p>");
        assertHTML("<p>\"&amp;</p>", "<p>\"&</p>");
        assertHTML("<p><img src=\"http://host.com/a.gif?a=foo&amp;b=bar\" /></p>",
            "<img src=\"http://host.com/a.gif?a=foo&amp;b=bar\" />");
        assertHTML("<p>&#xA;</p>", "<p>&#xA;</p>");

        // Verify that double quotes are escaped in attribute values
        assertHTML("<p value=\"script:&quot;&quot;\"></p>", "<p value='script:\"\"'");
    }

    @Test
    public void closeUnbalancedTags()
    {
        assertHTML("<hr /><p>hello</p>", "<hr><p>hello");
    }

    @Test
    public void conversionsFromHTML()
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
    public void convertImageAlignment()
    {
        assertHTML("<p><img style=\"float:left\" /></p>", "<img align=\"left\"/>");
        assertHTML("<p><img style=\"float:right\" /></p>", "<img align=\"right\"/>");
        assertHTML("<p><img style=\"vertical-align:top\" /></p>", "<img align=\"top\"/>");
        assertHTML("<p><img style=\"vertical-align:middle\" /></p>", "<img align=\"middle\"/>");
        assertHTML("<p><img style=\"vertical-align:bottom\" /></p>", "<img align=\"bottom\"/>");
    }

    @Test
    public void convertImplicitParagraphs()
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
    public void cleanNonXHTMLLists()
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
    public void scriptAndCData()
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

        assertHTML("<script>/*<![CDATA[*/\n&lt;&gt;\n/*]]>*/</script>", "<script>&lt;&gt;</script>");
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
    public void styleAndCData()
    {
        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>",
            "<style type=\"text/css\"><![CDATA[\na { color: red; }\n]]></style>");

        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>",
            "<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>");

        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na&gt;span { color: blue;}\n/*]]>*/</style>",
            "<style type=\"text/css\">a&gt;span { color: blue;}</style>");

        assertHTMLWithHeadContent("<style>/*<![CDATA[*/\n&lt;&gt;\n/*]]>*/</style>", "<style>&lt;&gt;</style>");
        assertHTMLWithHeadContent("<style>/*<![CDATA[*/\n<>\n/*]]>*/</style>", "<style><></style>");
    }

    /**
     * Verify that we can control what filters are used for cleaning.
     */
    @Test
    public void explicitFilterList()
    {
        HTMLCleanerConfiguration configuration = this.cleaner.getDefaultConfiguration();
        configuration.setFilters(Collections.emptyList());
        String result = HTMLUtils.toString(this.cleaner.clean(new StringReader("something"), configuration));
        // Note that if the default Body filter had been executed the result would have been:
        // <p>something</p>.
        assertEquals(HEADER_FULL + "something" + FOOTER, result);
    }

    /**
     * Verify that the restricted parameter works.
     */
    @Test
    public void restrictedHtml()
    {
        HTMLCleanerConfiguration configuration = this.cleaner.getDefaultConfiguration();
        Map<String, String> parameters = new HashMap<>();
        parameters.putAll(configuration.getParameters());
        parameters.put("restricted", "true");
        configuration.setParameters(parameters);
        Document document = this.cleaner.clean(new StringReader("<script>alert(\"foo\")</script>"), configuration);

        String textContent =
            document.getElementsByTagName("pre").item(0).getTextContent();
        assertEquals("alert(\"foo\")", textContent);

        String result = HTMLUtils.toString(document);
        assertEquals(HEADER_FULL + "<pre>alert(\"foo\")</pre>" + FOOTER, result);

        document = this.cleaner.clean(new StringReader("<style>p {color:white;}</style>"), configuration);

        textContent =
            document.getElementsByTagName("pre").item(0).getTextContent();
        assertEquals("p {color:white;}", textContent);

        result = HTMLUtils.toString(document);
        assertEquals(HEADER_FULL + "<pre>p {color:white;}</pre>" + FOOTER, result);
    }

    /**
     * Verify that passing a fully-formed XHTML header works fine.
     */
    @Test
    public void fullXHTMLHeader()
    {
        assertHTML("<p>test</p>", HEADER_FULL + "<p>test</p>" + FOOTER);
    }

    /**
     * Test {@link UniqueIdFilter}.
     */
    @Test
    public void duplicateIds(ComponentManager componentManager) throws Exception
    {
        String actual = "<p id=\"x\">1</p><p id=\"xy\">2</p><p id=\"x\">3</p>";
        String expected = "<p id=\"x\">1</p><p id=\"xy\">2</p><p id=\"x0\">3</p>";
        HTMLCleanerConfiguration config = this.cleaner.getDefaultConfiguration();
        List<HTMLFilter> filters = new ArrayList<>(config.getFilters());
        filters.add(componentManager.getInstance(HTMLFilter.class, "uniqueId"));
        config.setFilters(filters);
        assertEquals(HEADER_FULL + expected + FOOTER,
            HTMLUtils.toString(this.cleaner.clean(new StringReader(actual), config)));
    }

    /**
     * Test that tags with a namespace are not considered as unknown tags by HTMLCleaner (see also <a
     * href="https://jira.xwiki.org/browse/XWIKI-9753">XWIKI-9753</a>).
     */
    @Test
    public void cleanSVGTags() throws Exception
    {
        String input =
            "<p>before</p>\n" + "<p><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n"
                + "<circle cx=\"100\" cy=\"50\" fill=\"red\" r=\"40\" stroke=\"black\" stroke-width=\"2\"></circle>\n"
                + "</svg></p>\n" + "<p>after</p>\n";
        assertHTML(input, HEADER_FULL + input + FOOTER);
    }

    /**
     * Test that cleaning works when there's a TITLE element in the body (but with a namespace). The issue was that
     * HTMLCleaner would consider it a duplicate of the TITLE element in the HEAD even though it's namespaced. (see also
     * <a href="https://jira.xwiki.org/browse/XWIKI-9753">XWIKI-9753</a>).
     */
    @Test
    @Disabled("See https://jira.xwiki.org/browse/XWIKI-9753")
    public void cleanTitleWithNamespace()
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
        assertEquals(HEADER + input + FOOTER,
            HTMLUtils.toString(this.cleaner.clean(new StringReader(input))));
    }

    /**
     * Verify that a xmlns namespace set on the HTML element is not removed by default and it's removed if
     * {@link HTMLCleanerConfiguration#NAMESPACES_AWARE} is set to false.
     */
    @Test
    public void cleanHTMLTagWithNamespace()
    {
        String input = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body>";

        // Default
        assertEquals(HEADER + input + FOOTER,
            HTMLUtils.toString(this.cleaner.clean(new StringReader(input))));

        // Configured for namespace awareness being false
        HTMLCleanerConfiguration config = this.cleaner.getDefaultConfiguration();
        config.setParameters(Collections.singletonMap(HTMLCleanerConfiguration.NAMESPACES_AWARE, "false"));
        assertEquals(HEADER + "<html><head></head><body>" + FOOTER,
            HTMLUtils.toString(this.cleaner.clean(new StringReader(input), config)));
    }

    /**
     * Test that cleaning an empty DIV works (it used to fail, see <a
     * href="https://jira.xwiki.org/browse/XWIKI-4007">XWIKI-4007</a>).
     */
    @Test
    public void cleanEmptyDIV()
    {
        String input = "<div id=\"y\"></div><div id=\"z\">something</div>";
        assertHTML(input, HEADER_FULL + input + FOOTER);
    }

    @Test
    public void verifyLegendTagNotStripped()
    {
        String input = "<fieldset><legend>test</legend><div>content</div></fieldset>";
        assertHTML(input, HEADER_FULL + input + FOOTER);
    }

    @Test
    public void verifySpanIsExpanded()
    {
        assertHTML("<p><span class=\"fa fa-icon\"></span></p>", "<span class=\"fa fa-icon\" />");
    }

    @Test
    public void verifyExternalLinksAreSecure()
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
    public void verifyEntitiesAreNotBroken()
    {
        assertHTML("<p>&Eacute;</p>", "&Eacute;");
        assertHTML("<p>&frac14;</p>", "&frac14;");
        assertHTML("<p>&amp;f!rac14;</p>", "&f!rac14;");
    }

    @Test
    public void verifyLeadingSpacesAreKeptOnlyInInputValue()
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
    public void verifyIFRAMECleaning() throws Exception
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
    public void escapeHTMLCharsInAttributes() throws Exception
    {
        // Note: single quotes are not escaped since they're valid chars in attribute values that are surrounded by
        // quotes. And HTMLCleaner will convert single quoted attributes into double-quoted ones.
        String htmlInput = "<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff\">content</div>";
        Document document = this.cleaner.clean(new StringReader(htmlInput));

        String textContent =
            document.getElementsByTagName("div").item(0).getAttributes().getNamedItem("foo").getTextContent();
        assertEquals("aaa\"bbb&ccc>ddd<eee'fff", textContent);

        htmlInput = "<div foo='aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff'>content</div>";
        document = this.cleaner.clean(new StringReader(htmlInput));

        textContent =
            document.getElementsByTagName("div").item(0).getAttributes().getNamedItem("foo").getTextContent();
        assertEquals("aaa\"bbb&ccc>ddd<eee'fff", textContent);

        assertHTML("<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee'fff\">content</div>",
            "<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff\">content</div>");
        assertHTML("<div foo=\"aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee'fff\">content</div>",
            "<div foo='aaa&quot;bbb&amp;ccc&gt;ddd&lt;eee&apos;fff'>content</div>");
    }

    @Test
    public void controlCharacters() throws Exception
    {
        String htmlInput = "<p>\u0008</p>";
        Document document = this.cleaner.clean(new StringReader(htmlInput));

        String textContent =
            document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals(" ", textContent);
        assertHTML(" ", "\u0008");

        htmlInput = "<p>&#8;</p>";
        document = this.cleaner.clean(new StringReader(htmlInput));

        textContent =
            document.getElementsByTagName("p").item(0).getTextContent();
        assertEquals("&#8;", textContent);
        assertHTML("<p>&#8;</p>", "&#8;");
    }

    private void assertHTML(String expected, String actual)
    {
        assertEquals(HEADER_FULL + expected + FOOTER,
            HTMLUtils.toString(this.cleaner.clean(new StringReader(actual))));
    }

    private void assertHTMLWithHeadContent(String expected, String actual)
    {
        assertEquals(HEADER + "<html><head>" + expected + "</head><body>" + FOOTER,
            HTMLUtils.toString(this.cleaner.clean(new StringReader(actual))));
    }

    @Test
    public void transformedDOMContent()
    {
        String htmlInput = "<img src=\"http://host.com/a.gif?a=foo&b=bar\" />";
        Document document = this.cleaner.clean(new StringReader(htmlInput));

        String textContent =
            document.getElementsByTagName("img").item(0).getAttributes().getNamedItem("src").getTextContent();
        assertEquals("http://host.com/a.gif?a=foo&b=bar", textContent);

        htmlInput = "<img src=\"http://host.com/a.gif?a=foo&amp;b=bar\" />";
        document = this.cleaner.clean(new StringReader(htmlInput));

        textContent =
            document.getElementsByTagName("img").item(0).getAttributes().getNamedItem("src").getTextContent();
        assertEquals("http://host.com/a.gif?a=foo&b=bar", textContent);
    }

    @Test
    public void parse() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append("<?xml version = \"1.0\"?><img src=\"http://xwiki.org?a=&amp;b\"/>");
        ByteArrayInputStream input =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(input);
        Element root = doc.getDocumentElement();
        assertEquals("http://xwiki.org?a=&b", root.getAttribute("src"));

        OutputFormat format = new OutputFormat(doc);
        StringWriter writer = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(writer, format);
        serializer.serialize(doc);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<img src=\"http://xwiki.org?a=&amp;b\"/>", writer.toString());
    }

    @Test
    public void parse2() throws Exception
    {
        String html = "<?xml version = \"1.0\"?><img src=\"http://xwiki.org?a=&amp;b\"/>";
        final CleanerProperties cleanerProperties = new CleanerProperties();
        final TagNode tagNode = new HtmlCleaner().clean(html);
        final Document doc = new DomSerializer(cleanerProperties, true).createDOM(tagNode);
        assertEquals("http://xwiki.org?a=&amp;b",
            doc.getElementsByTagName("img").item(0).getAttributes().getNamedItem("src").getTextContent());
        cleanerProperties.setOmitHtmlEnvelope(true);
        String out = new SimpleXmlSerializer(cleanerProperties).getAsString(html);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<img src=\"http://xwiki.org?a=&amp;b\" />",
            out);
    }

}
