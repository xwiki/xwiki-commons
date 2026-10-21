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
package org.xwiki.tool.xar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.xwiki.tool.xar.internal.XWikiDocument;

import static org.xwiki.tool.xar.internal.XMLUtils.getSAXReader;

/**
 * Pretty prints and set valid authors and version to XAR XML files.
 *
 * @version $Id$
 */
@Mojo(name = "format", threadSafe = true)
public class FormatMojo extends AbstractVerifyMojo
{
    private static final String XPATH_ORIGINAL_METADATA_AUTHOR = "xwikidoc/originalMetadataAuthor";

    /**
     * If false then don't pretty print the XML.
     */
    @Parameter(property = "xar.pretty", readonly = true)
    private boolean pretty = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Only format XAR modules or when forced
        if (getProject().getPackaging().equals("xar") || this.force) {
            // Note: it's important that we run the license addition before we formatting below since the pretty print
            // would add a new line between the XML declaration and the license for example. Otherwise we'd get the
            // new line added only on the second run of this mojo!
            if (this.formatLicense) {
                getLog().info("Adding missing XAR XML license headers...");
                executeLicenseGoal("format");
            }
            getLog().info("Formatting XAR XML files...");
            initializePatterns();
            Collection<File> xmlFiles = getXARXMLFiles();
            for (File file : xmlFiles) {
                try {
                    format(file, guessDefaultLocale(file, xmlFiles));
                } catch (Exception e) {
                    throw new MojoExecutionException(String.format("Failed to format file [%s]", file), e);
                }
            }
        } else {
            getLog().info("Not a XAR module, skipping reformatting...");
        }
    }

    private void format(File file, Locale defaultLocale) throws IOException, DocumentException
    {
        SAXReader reader = getSAXReader();
        Document domdoc = reader.read(file);
        format(file.getPath(), domdoc, defaultLocale);

        XWikiXMLWriter writer;
        if (this.pretty) {
            OutputFormat format = new OutputFormat("  ", true, "UTF-8");
            format.setExpandEmptyElements(false);
            writer = new XWikiXMLWriter(new FileOutputStream(file), format);
        } else {
            writer = new XWikiXMLWriter(new FileOutputStream(file));
        }
        try {
            writer.setVersion("1.1");
            writer.write(domdoc);
        } finally {
            writer.close();
        }

        String parentName = file.getParentFile().getName();
        getLog().info(String.format("  Formatting [%s/%s]... ok", parentName, file.getName()));
    }

    void format(String filePath, Document domdoc, Locale defaultLocale) throws DocumentException
    {
        resetEditMetadata(domdoc);
        formatLocales(domdoc, defaultLocale);
        clearContent("xwikidoc/comment", domdoc);
        hideTechnicalPage(filePath, domdoc);
        removeVolatileMetadata(domdoc);
    }

    /**
     * Reset the authors, version and minor edit flag set by the wiki, so that the XML files don't depend on who last
     * edited or exported the page.
     *
     * @param domdoc the DOM document
     */
    private void resetEditMetadata(Document domdoc)
    {
        setText("xwikidoc/author", AUTHOR, domdoc);
        setText(XPATH_ORIGINAL_METADATA_AUTHOR, AUTHOR, domdoc);
        setText("xwikidoc/contentAuthor", AUTHOR, domdoc);
        setText("xwikidoc/creator", AUTHOR, domdoc);
        setText("xwikidoc/version", VERSION, domdoc);
        setText("xwikidoc/minorEdit", "false", domdoc);

        // Also update the attachment authors
        for (Node attachmentAuthorNode : domdoc.selectNodes("xwikidoc/attachment/author")) {
            attachmentAuthorNode.setText(AUTHOR);
        }
    }

    /**
     * Set the text of the node found with the xpath expression, if it exists.
     *
     * @param xpathExpression the xpath expression of the node
     * @param text the text to set
     * @param domdoc the DOM document
     */
    private void setText(String xpathExpression, String text, Document domdoc)
    {
        Node node = domdoc.selectSingleNode(xpathExpression);
        if (node != null) {
            node.setText(text);
        }
    }

    /**
     * Remove the content of the element found with the xpath expression, if it exists.
     *
     * @param xpathExpression the xpath expression of the element
     * @param domdoc the DOM document
     */
    private void clearContent(String xpathExpression, Document domdoc)
    {
        Element element = (Element) domdoc.selectSingleNode(xpathExpression);
        if (element != null) {
            removeContent(element);
        }
    }

    private void formatLocales(Document domdoc, Locale defaultLocale) throws DocumentException
    {
        // Re-format the locale from the root element
        Attribute localeAttribute = domdoc.getRootElement().attribute("locale");
        if (localeAttribute != null) {
            Locale locale = XWikiDocument.toLocale(localeAttribute.getValue(), false);
            if (locale != null) {
                localeAttribute.setValue(locale.toString());
            }
        }
        // Re-format the locale from <locale>
        Element element = (Element) domdoc.selectSingleNode("xwikidoc/language");
        if (element != null) {
            setLocale(element, XWikiDocument.toLocale(element.getText(), false));
        }

        // Set the default locale
        element = (Element) domdoc.selectSingleNode("xwikidoc/defaultLanguage");
        if (element != null) {
            setLocale(element, defaultLocale);
        }
    }

    private void setLocale(Element element, Locale locale) throws DocumentException
    {
        // Writing an empty element would silently turn the page into a root locale page
        if (locale == null) {
            throw new DocumentException(String.format("No locale to set in the <%s> element", element.getName()));
        }

        if (Locale.ROOT.equals(locale)) {
            removeContent(element);
        } else {
            element.setText(locale.toString());
        }
    }

    /**
     * Make sure the page is hidden if it's a technical page that is not a visible technical page.
     *
     * @param filePath the path of the XML file, used to know whether the page is a technical page
     * @param domdoc the DOM document
     * @throws DocumentException if the technical page has no {@code <hidden>} element to set
     */
    private void hideTechnicalPage(String filePath, Document domdoc) throws DocumentException
    {
        if (!isContentPage(filePath) && !isVisibleTechnicalPage(filePath)) {
            Node hiddenNode = domdoc.selectSingleNode("xwikidoc/hidden");
            if (hiddenNode == null) {
                throw new DocumentException(
                    String.format("The technical page [%s] has no <hidden> element to set", filePath));
            }
            hiddenNode.setText("true");
        }
    }

    /**
     * Remove the dates and the original metadata author, which change on every save in the wiki, unless they're
     * configured to be kept.
     *
     * @param domdoc the DOM document
     */
    private void removeVolatileMetadata(Document domdoc)
    {
        String documentName = "";
        try {
            documentName = XWikiDocument.readDocumentReference(domdoc);
        } catch (DocumentException e) {
            getLog().error("Failed to get the document reference", e);
        }
        if (!this.skipDates && !this.skipDatesDocumentList.contains(documentName)) {
            removeNodes("xwikidoc/creationDate", domdoc);
            removeNodes("xwikidoc/date", domdoc);
            removeNodes("xwikidoc/contentUpdateDate", domdoc);
            removeNodes("xwikidoc//attachment/date", domdoc);
        }
        if (!this.skipAuthors && !this.skipAuthorsDocumentList.contains(documentName)) {
            removeNodes(XPATH_ORIGINAL_METADATA_AUTHOR, domdoc);
        }
    }

    private void removeContent(Element element)
    {
        if (element.hasContent()) {
            element.clearContent();
        }
    }

    /**
     * Remove the nodes found with the xpath expression.
     *
     * @param xpathExpression the xpath expression of the nodes
     * @param domdoc The DOM document
     */
    private void removeNodes(String xpathExpression, Document domdoc)
    {
        List<Node> nodes = domdoc.selectNodes(xpathExpression);
        for (Node node : nodes) {
            node.detach();
        }
    }
}
