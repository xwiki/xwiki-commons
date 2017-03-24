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

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CleanerTransformations;
import org.htmlcleaner.DoctypeToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagTransformation;
import org.htmlcleaner.XWikiDOMSerializer;
import org.w3c.dom.Document;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLConstants;
import org.xwiki.xml.html.filter.HTMLFilter;

/**
 * Default implementation for {@link org.xwiki.xml.html.HTMLCleaner} using the <a href="HTML Cleaner
 * framework>http://htmlcleaner.sourceforge.net/</a>.
 *
 * @version $Id$
 * @since 1.6M1
 */
@Component
@Singleton
public class DefaultHTMLCleaner implements HTMLCleaner, Initializable
{
    /**
     * {@link HTMLFilter} for filtering html lists.
     */
    @Inject
    @Named("list")
    private HTMLFilter listFilter;

    /**
     * {@link HTMLFilter} for filtering html lists.
     */
    @Inject
    @Named("listitem")
    private HTMLFilter listItemFilter;

    /**
     * {@link HTMLFilter} for filtering HTML font elements.
     */
    @Inject
    @Named("font")
    private HTMLFilter fontFilter;

    /**
     * {@link HTMLFilter} for wrapping invalid body elements with paragraphs.
     */
    @Inject
    @Named("body")
    private HTMLFilter bodyFilter;

    /**
     * {@link HTMLFilter} for filtering HTML attributes that are used by many different elements and for which we cannot
     * write simple transformations like in {@link #getDefaultCleanerTransformations(HTMLCleanerConfiguration)}.
     */
    @Inject
    @Named("attribute")
    private HTMLFilter attributeFilter;

    /**
     * {@link HTMLFilter} for filtering HTML links.
     */
    @Inject
    @Named("link")
    private HTMLFilter linkFilter;

    @Inject
    private Execution execution;

    @Override
    public void initialize() throws InitializationException
    {
        // The clean method below is thread safe. However it seems that DOMOutputter.output() is not fully thread safe
        // since it causes the following exception on the first time it's called from different threads:
        //  Caused by: org.jdom.JDOMException: Reflection failed while creating new JAXP document:
        //  duplicate class definition: org/apache/xerces/jaxp/DocumentBuilderFactoryImpl
        //  at org.jdom.adapters.JAXPDOMAdapter.createDocument(JAXPDOMAdapter.java:191)
        //  at org.jdom.adapters.AbstractDOMAdapter.createDocument(AbstractDOMAdapter.java:133)
        //  at org.jdom.output.DOMOutputter.createDOMDocument(DOMOutputter.java:208)
        //  at org.jdom.output.DOMOutputter.output(DOMOutputter.java:127)
        // Since this only happens once, we call it first here at initialization time (since there's no thread
        // contention at that time). Note: This email thread seems to say it's thread safe but that's not what we see
        // here: http:osdir.com/ml/text.xml.xforms.chiba.devel/2006-09/msg00025.html
        clean(new StringReader(""));
    }

    @Override
    public Document clean(Reader originalHtmlContent)
    {
        return clean(originalHtmlContent, getDefaultConfiguration());
    }

    private DocumentBuilder getAvailableDocumentBuilder() throws ParserConfigurationException
    {
        ExecutionContext econtext = this.execution.getContext();

        if (econtext != null) {
            DocumentBuilder documentBuilder = (DocumentBuilder) econtext.getProperty(DocumentBuilder.class.getName());

            if (documentBuilder == null) {
                documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                econtext.setProperty(DocumentBuilder.class.getName(), documentBuilder);
            }

            return documentBuilder;
        }

        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Override
    public Document clean(Reader originalHtmlContent, HTMLCleanerConfiguration configuration)
    {
        Document result;

        // Note: Instantiation of an HtmlCleaner object is cheap so there's no need to cache an instance of it,
        // especially since this makes it extra safe with regards to multithreading (even though HTML Cleaner is
        // already supposed to be thread safe).
        CleanerProperties cleanerProperties = getDefaultCleanerProperties(configuration);
        HtmlCleaner cleaner = new HtmlCleaner(cleanerProperties);

        TagNode cleanedNode;
        try {
            cleanedNode = cleaner.clean(originalHtmlContent);
        } catch (Exception e) {
            // This shouldn't happen since we're not doing any IO... I consider this a flaw in the design of HTML
            // Cleaner.
            throw new RuntimeException("Unhandled error when cleaning HTML", e);
        }

        try {
            // Ideally we would use SF's HTMLCleaner DomSerializer but there are outstanding issues with it, so we're
            // using a custom XWikiDOMSerializer (see its javadoc for more details).
            // Replace by the following when fixed:
            //   result = new DomSerializer(cleanerProperties, false).createDOM(cleanedNode);

            cleanedNode.setDocType(new DoctypeToken("html", "PUBLIC", "-//W3C//DTD XHTML 1.0 Strict//EN",
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"));
            result =
                new XWikiDOMSerializer(cleanerProperties, false).createDOM(getAvailableDocumentBuilder(), cleanedNode);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException("Error while serializing TagNode into w3c dom.", ex);
        }

        // Finally apply filters.
        for (HTMLFilter filter : configuration.getFilters()) {
            filter.filter(result, configuration.getParameters());
        }

        return result;
    }

    @Override
    public HTMLCleanerConfiguration getDefaultConfiguration()
    {
        HTMLCleanerConfiguration configuration = new DefaultHTMLCleanerConfiguration();
        configuration.setFilters(Arrays.asList(
            this.bodyFilter,
            this.listItemFilter,
            this.listFilter,
            this.fontFilter,
            this.attributeFilter,
            this.linkFilter));
        return configuration;
    }

    /**
     * @param configuration the configuration to use for the cleaning
     * @return the default {@link CleanerProperties} to be used for cleaning.
     */
    private CleanerProperties getDefaultCleanerProperties(HTMLCleanerConfiguration configuration)
    {
        CleanerProperties defaultProperties = new CleanerProperties();
        defaultProperties.setOmitUnknownTags(true);

        // HTML Cleaner uses the compact notation by default but we don't want that since:
        // - it's more work and not required since not compact notation is valid XHTML
        // - expanded elements can also be rendered fine in browsers that only support HTML.
        defaultProperties.setUseEmptyElementTags(false);

        // Wrap script and style content in CDATA blocks
        defaultProperties.setUseCdataForScriptAndStyle(true);

        // We need this for example to ignore CDATA sections not inside script or style elements.
        defaultProperties.setIgnoreQuestAndExclam(true);

        // Remove CDATA outside of script and style since according to the spec it has no effect there.
        defaultProperties.setOmitCdataOutsideScriptAndStyle(true);

        // If the caller has defined NAMESPACE_AWARE configuration property then use it, otherwise use our default.
        String param = configuration.getParameters().get(HTMLCleanerConfiguration.NAMESPACES_AWARE);
        boolean namespacesAware = (param != null) ? Boolean.parseBoolean(param) : true;
        defaultProperties.setNamespacesAware(namespacesAware);

        // Set Cleaner transformations
        defaultProperties.setCleanerTransformations(getDefaultCleanerTransformations(configuration));
        
        // By default, we are cleaning XHTML 1.0 code, not HTML 5.
        // Note: Tests are broken if we don't set the version 4, meaning that supporting HTML5 requires some work.
        // TODO: handle HTML5 correctly (see: https://jira.xwiki.org/browse/XCOMMONS-901)
        defaultProperties.setHtmlVersion(4);

        return defaultProperties;
    }

    /**
     * @param configuration The cleaner configuration.
     * @return the default cleaning transformations to perform on tags, in addition to the base transformations done by
     *         HTML Cleaner
     */
    private CleanerTransformations getDefaultCleanerTransformations(HTMLCleanerConfiguration configuration)
    {
        CleanerTransformations defaultTransformations = new CleanerTransformations();

        TagTransformation tt = new TagTransformation(HTMLConstants.TAG_B, HTMLConstants.TAG_STRONG, false);
        defaultTransformations.addTransformation(tt);

        tt = new TagTransformation(HTMLConstants.TAG_I, HTMLConstants.TAG_EM, false);
        defaultTransformations.addTransformation(tt);

        tt = new TagTransformation(HTMLConstants.TAG_U, HTMLConstants.TAG_INS, false);
        defaultTransformations.addTransformation(tt);

        tt = new TagTransformation(HTMLConstants.TAG_S, HTMLConstants.TAG_DEL, false);
        defaultTransformations.addTransformation(tt);

        tt = new TagTransformation(HTMLConstants.TAG_STRIKE, HTMLConstants.TAG_DEL, false);
        defaultTransformations.addTransformation(tt);

        tt = new TagTransformation(HTMLConstants.TAG_CENTER, HTMLConstants.TAG_P, false);
        tt.addAttributeTransformation(HTMLConstants.ATTRIBUTE_STYLE, "text-align:center");
        defaultTransformations.addTransformation(tt);

        String restricted = configuration.getParameters().get(HTMLCleanerConfiguration.RESTRICTED);
        if ("true".equalsIgnoreCase(restricted)) {

            tt = new TagTransformation(HTMLConstants.TAG_SCRIPT, HTMLConstants.TAG_PRE, false);
            defaultTransformations.addTransformation(tt);

            tt = new TagTransformation(HTMLConstants.TAG_STYLE, HTMLConstants.TAG_PRE, false);
            defaultTransformations.addTransformation(tt);
        }

        return defaultTransformations;
    }
}
