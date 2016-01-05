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
import org.htmlcleaner.Html4TagProvider;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.ITagInfoProvider;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagTransformation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLConstants;
import org.xwiki.xml.html.HTMLVersion;
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
     * The qualified name to be used when generating an html {@link DocumentType}.
     */
    private static final String QUALIFIED_NAME_HTML = "html";
    
    private static final XWikiHTML5TagProvider html5TagProvider = new XWikiHTML5TagProvider();

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

        HTMLVersion htmlVersion = getHTMLVersion(configuration);
        ITagInfoProvider tagInfoProvider = 
                (htmlVersion == HTMLVersion.XHTML_5_0 ? html5TagProvider : Html4TagProvider.INSTANCE);
        
        HtmlCleaner cleaner = new HtmlCleaner(tagInfoProvider, cleanerProperties);
        
        TagNode cleanedNode;
        try {
            cleanedNode = cleaner.clean(originalHtmlContent);
        } catch (Exception e) {
            // This shouldn't happen since we're not doing any IO... I consider this a flaw in the design of HTML
            // Cleaner.
            throw new RuntimeException("Unhandled error when cleaning HTML", e);
        }
        
        try {
            // Since there's a bug in SF's HTML Cleaner in that it doesn't recognize CDATA blocks we need to turn off
            // character escaping (hence the false value passed) and do the escaping in XMLUtils.toString(). Note that
            // this can cause problem for code not serializing the W3C DOM to a String since it won't have the
            // characters escaped.
            // See https://sourceforge.net/tracker/index.php?func=detail&aid=2691888&group_id=183053&atid=903696
            result =
                new XWikiDOMSerializer(cleanerProperties, false).createDOM(getAvailableDocumentBuilder(), cleanedNode);
            
            // Add the doctype if XHTML 1.0 is used.
            // Note: we cannot add the HTML5 doctype in the DOM tree because there is no HTML5 DTD.
            if (htmlVersion == HTMLVersion.XHTML_1_0) {
                // Serialize the cleanedNode TagNode into a w3c dom. Ideally following code should be enough.
                // But SF's HTML Cleaner seems to omit the DocType declaration while serializing.
                // See https://sourceforge.net/tracker/index.php?func=detail&aid=2062318&group_id=183053&atid=903696
                //      cleanedNode.setDocType(new DoctypeToken("html", "PUBLIC", "-//W3C//DTD XHTML 1.0 Strict//EN",
                //          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"));
                //      try {
                //          result = new DomSerializer(cleanerProperties, false).createDOM(cleanedNode);
                //      } catch(ParserConfigurationException ex) { }
                // As a workaround, we must serialize the cleanedNode into a temporary w3c document, create a new w3c document
                // with proper DocType declaration and move the root node from the temporary document to the new one.
                DOMImplementation domImpl = getAvailableDocumentBuilder().getDOMImplementation();
                DocumentType docType =
                    domImpl.createDocumentType(QUALIFIED_NAME_HTML, "-//W3C//DTD XHTML 1.0 Strict//EN",
                            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
                Document tempDoc = result;
                result = domImpl.createDocument(null, QUALIFIED_NAME_HTML, docType);
                result.replaceChild(result.adoptNode(tempDoc.getDocumentElement()), result.getDocumentElement());
            }
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
            this.attributeFilter));
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

        switch (getHTMLVersion(configuration)) {
            case XHTML_1_0:
                // XHTML 1.0 is marked as HTML 4 in the SF Html Cleaner
                defaultProperties.setHtmlVersion(4);
                break;
            case XHTML_5_0:
            default:
                defaultProperties.setHtmlVersion(5);
                break;
        }

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
    
    private HTMLVersion getHTMLVersion(HTMLCleanerConfiguration configuration)
    {
        return HTMLVersion.valueOf(configuration.getParameters().get(HTMLCleanerConfiguration.HTML_VERSION));
    }
}
