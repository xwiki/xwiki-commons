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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;
import org.xwiki.xml.html.filter.HTMLFilter;
import org.xwiki.xml.internal.html.filter.AttributeFilter;
import org.xwiki.xml.internal.html.filter.BodyFilter;
import org.xwiki.xml.internal.html.filter.FontFilter;
import org.xwiki.xml.internal.html.filter.LinkFilter;
import org.xwiki.xml.internal.html.filter.ListFilter;
import org.xwiki.xml.internal.html.filter.ListItemFilter;
import org.xwiki.xml.internal.html.filter.UniqueIdFilter;

/**
 * Unit tests for {@link org.xwiki.xml.internal.html.DefaultHTMLCleaner}.
 *
 * @version $Id$
 * @since 1.6M1
 */
@ComponentList({ ListFilter.class, ListItemFilter.class, FontFilter.class, BodyFilter.class, AttributeFilter.class,
UniqueIdFilter.class, DefaultHTMLCleaner.class, LinkFilter.class })
public class DefaultHTMLCleanerTest
{
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";

    private static final String HEADER_FULL = HEADER + "<html><head></head><body>";

    private static final String FOOTER = "</body></html>\n";

    @Rule
    public final MockitoComponentMockingRule<HTMLCleaner> mocker = new MockitoComponentMockingRule<HTMLCleaner>(
        DefaultHTMLCleaner.class);

    @Test
    public void elementExpansion() throws ComponentLookupException
    {
        assertHTML("<p><textarea></textarea></p>", "<textarea/>");

        // Verify exceptions (by default elements are expanded).
        assertHTML("<p><br /></p>", "<p><br></p>");
        assertHTML("<hr />", "<hr>");
    }

    @Test
    public void specialCharacters() throws ComponentLookupException
    {
        // TODO: We still have a problem I think in that if there are characters such as "&" or quote in the source
        // text they are not escaped. This is because we have use "false" in DefaultHTMLCleaner here:
        // Document document = new JDomSerializer(this.cleanerProperties, false).createJDom(cleanedNode);
        // See the problem described here: http://sourceforge.net/forum/forum.php?thread_id=2243880&forum_id=637246
        assertHTML("<p>&quot;&amp;**notbold**&lt;notag&gt;&nbsp;</p>",
            "<p>&quot;&amp;**notbold**&lt;notag&gt;&nbsp;</p>");
        assertHTML("<p>\"&amp;</p>", "<p>\"&</p>");
        assertHTML("<p><img src=\"http://host.com/a.gif?a=foo&amp;b=bar\" /></p>",
            "<img src=\"http://host.com/a.gif?a=foo&b=bar\" />");
        assertHTML("<p>&#xA;</p>", "<p>&#xA;</p>");

        // Verify that double quotes are escaped in attribute values
        assertHTML("<p value=\"script:&quot;&quot;\"></p>", "<p value='script:\"\"'");
    }

    @Test
    public void closeUnbalancedTags() throws ComponentLookupException
    {
        assertHTML("<hr /><p>hello</p>", "<hr><p>hello");
    }

    @Test
    public void conversionsFromHTML() throws ComponentLookupException
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
    public void convertImageAlignment() throws ComponentLookupException
    {
        assertHTML("<p><img style=\"float:left\" /></p>", "<img align=\"left\"/>");
        assertHTML("<p><img style=\"float:right\" /></p>", "<img align=\"right\"/>");
        assertHTML("<p><img style=\"vertical-align:top\" /></p>", "<img align=\"top\"/>");
        assertHTML("<p><img style=\"vertical-align:middle\" /></p>", "<img align=\"middle\"/>");
        assertHTML("<p><img style=\"vertical-align:bottom\" /></p>", "<img align=\"bottom\"/>");
    }

    @Test
    public void convertImplicitParagraphs() throws ComponentLookupException
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
    public void cleanNonXHTMLLists() throws ComponentLookupException
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
    public void scriptAndCData() throws ComponentLookupException
    {
        assertHTML("<script type=\"text/javascript\">//<![CDATA[\n\nalert(\"Hello World\")\n\n//]]></script>",
            "<script type=\"text/javascript\"><![CDATA[\nalert(\"Hello World\")\n]]></script>");

        assertHTML("<script type=\"text/javascript\">//<![CDATA[\n//\nalert(\"Hello World\")\n\n//]]></script>",
            "<script type=\"text/javascript\">//<![CDATA[\nalert(\"Hello World\")\n//]]></script>");

        assertHTML("<script type=\"text/javascript\">//<![CDATA[\n\nalert(\"Hello World\")\n\n//]]></script>",
            "<script type=\"text/javascript\">/*<![CDATA[*/\nalert(\"Hello World\")\n/*]]>*/</script>");

        assertHTML("<script type=\"text/javascript\">//<![CDATA[\n\n\n" + "function escapeForXML(origtext) {\n"
            + "   return origtext.replace(/\\&/g,'&'+'amp;').replace(/</g,'&'+'lt;')\n"
            + "       .replace(/>/g,'&'+'gt;').replace(/\'/g,'&'+'apos;').replace(/\"/g,'&'+'quot;');" + "}\n\n\n//]]>"
            + "</script>", "<script type=\"text/javascript\">\n" + "/*<![CDATA[*/\n"
            + "function escapeForXML(origtext) {\n"
            + "   return origtext.replace(/\\&/g,'&'+'amp;').replace(/</g,'&'+'lt;')\n"
            + "       .replace(/>/g,'&'+'gt;').replace(/\'/g,'&'+'apos;').replace(/\"/g,'&'+'quot;');" + "}\n"
            + "/*]]>*/\n" + "</script>");

        assertHTML("<script>//<![CDATA[\n<>\n//]]></script>", "<script>&lt;&gt;</script>");
        assertHTML("<script>//<![CDATA[\n<>\n//]]></script>", "<script><></script>");

        // Verify that CDATA not inside SCRIPT or STYLE elements are considered comments in HTML and thus stripped
        // when cleaned.
        assertHTML("<p></p>", "<p><![CDATA[&]]></p>");
        assertHTML("<p>&amp;&amp;</p>", "<p>&<![CDATA[&]]>&</p>");
    }

    /**
     * Verify that inline style elements are not cleaned and that we can have a CDATA section inside.
     */
    @Test
    public void styleAndCData() throws ComponentLookupException
    {
        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>",
            "<style type=\"text/css\"><![CDATA[\na { color: red; }\n]]></style>");

        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>",
            "<style type=\"text/css\">/*<![CDATA[*/\na { color: red; }\n/*]]>*/</style>");

        assertHTMLWithHeadContent("<style type=\"text/css\">/*<![CDATA[*/a>span { color: blue;}\n/*]]>*/</style>",
            "<style type=\"text/css\">a&gt;span { color: blue;}</style>");

        assertHTMLWithHeadContent("<style>/*<![CDATA[*/<>\n/*]]>*/</style>", "<style>&lt;&gt;</style>");
        assertHTMLWithHeadContent("<style>/*<![CDATA[*/<>\n/*]]>*/</style>", "<style><></style>");
    }

    /**
     * Verify that we can control what filters are used for cleaning.
     */
    @Test
    public void explicitFilterList() throws ComponentLookupException
    {
        HTMLCleanerConfiguration configuration = this.mocker.getComponentUnderTest().getDefaultConfiguration();
        configuration.setFilters(Collections.<HTMLFilter>emptyList());
        String result =
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(new StringReader("something"), configuration));
        // Note that if the default Body filter had been executed the result would have been:
        // <p>something</p>.
        Assert.assertEquals(HEADER_FULL + "something" + FOOTER, result);
    }

    /**
     * Verify that the restricted parameter works.
     */
    @Test
    public void restrictedHtml() throws ComponentLookupException
    {
        HTMLCleanerConfiguration configuration = this.mocker.getComponentUnderTest().getDefaultConfiguration();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.putAll(configuration.getParameters());
        parameters.put("restricted", "true");
        configuration.setParameters(parameters);

        String result =
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(
                new StringReader("<script>alert(\"foo\")</script>"), configuration));
        Assert.assertEquals(HEADER_FULL + "<pre>alert(\"foo\")</pre>" + FOOTER, result);

        result =
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(
                new StringReader("<style>p {color:white;}</style>"), configuration));
        Assert.assertEquals(HEADER_FULL + "<pre>p {color:white;}</pre>" + FOOTER, result);

    }

    /**
     * Verify that passing a fully-formed XHTML header works fine.
     */
    @Test
    public void fullXHTMLHeader() throws ComponentLookupException
    {
        assertHTML("<p>test</p>", HEADER_FULL + "<p>test</p>" + FOOTER);
    }

    /**
     * Test {@link UniqueIdFilter}.
     */
    @Test
    public void duplicateIds() throws Exception
    {
        String actual = "<p id=\"x\">1</p><p id=\"xy\">2</p><p id=\"x\">3</p>";
        String expected = "<p id=\"x\">1</p><p id=\"xy\">2</p><p id=\"x0\">3</p>";
        HTMLCleanerConfiguration config = this.mocker.getComponentUnderTest().getDefaultConfiguration();
        List<HTMLFilter> filters = new ArrayList<HTMLFilter>(config.getFilters());
        filters.add(this.mocker.<HTMLFilter>getInstance(HTMLFilter.class, "uniqueId"));
        config.setFilters(filters);
        Assert.assertEquals(HEADER_FULL + expected + FOOTER,
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(new StringReader(actual), config)));
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
    @Ignore("See https://jira.xwiki.org/browse/XWIKI-9753")
    public void cleanTitleWithNamespace() throws Exception
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
        Assert.assertEquals(HEADER + input + FOOTER,
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(new StringReader(input))));
    }

    /**
     * Verify that a xmlns namespace set on the HTML element is not removed by default and it's removed if
     * {@link HTMLCleanerConfiguration#NAMESPACES_AWARE} is set to false.
     */
    @Test
    @Ignore("See https://sourceforge.net/p/htmlcleaner/bugs/168/")
    public void cleanHTMLTagWithNamespace() throws Exception
    {
        String input = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body>";

        // Default
        Assert.assertEquals(HEADER + input + FOOTER,
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(new StringReader(input))));

        // Configured for namespace awareness being false
        HTMLCleanerConfiguration config = this.mocker.getComponentUnderTest().getDefaultConfiguration();
        config.setParameters(Collections.singletonMap(HTMLCleanerConfiguration.NAMESPACES_AWARE, "false"));
        Assert.assertEquals(HEADER + "<html><head></head><body>" + FOOTER,
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(new StringReader(input), config)));
    }

    /**
     * Test that cleaning an empty DIV works (it used to fail, see <a
     * href="https://jira.xwiki.org/browse/XWIKI-4007">XWIKI-4007</a>).
     */
    @Test
    public void cleanEmptyDIV() throws Exception
    {
        String input = "<div id=\"y\"></div><div id=\"z\">something</div>";
        assertHTML(input, HEADER_FULL + input + FOOTER);
    }

    @Test
    public void verifyLegendTagNotStripped() throws Exception
    {
        String input = "<fieldset><legend>test</legend><div>content</div></fieldset>";
        assertHTML(input, HEADER_FULL + input + FOOTER);
    }

    @Test
    public void verifySpanIsExpanded() throws Exception
    {
        assertHTML("<p><span class=\"fa fa-icon\"></span></p>", "<span class=\"fa fa-icon\" />");
    }

    @Test
    public void verifyExternalLinksAreSecure() throws Exception
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
    public void verifyEntitiesAreNotBroken() throws Exception
    {
        assertHTML("<p>&Eacute;</p>", "&Eacute;");
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
        //          assertHTML("<iframe src=\"whatever\"></iframe>", "<iframe src=\"whatever\"/>\r\n");
        assertHTML("<p><iframe src=\"whatever\"></iframe></p>", "<iframe src=\"whatever\"/>");
        assertHTML("<p><iframe src=\"whatever\"></iframe></p>\r\n", "<iframe src=\"whatever\"/>\r\n");
    }

    private void assertHTML(String expected, String actual) throws ComponentLookupException
    {
        Assert.assertEquals(HEADER_FULL + expected + FOOTER,
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(new StringReader(actual))));
    }

    private void assertHTMLWithHeadContent(String expected, String actual) throws ComponentLookupException
    {
        Assert.assertEquals(HEADER + "<html><head>" + expected + "</head><body>" + FOOTER,
            HTMLUtils.toString(this.mocker.getComponentUnderTest().clean(new StringReader(actual))));
    }
}
