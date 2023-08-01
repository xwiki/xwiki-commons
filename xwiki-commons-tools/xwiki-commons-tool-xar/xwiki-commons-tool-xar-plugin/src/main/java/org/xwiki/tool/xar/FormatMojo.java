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

    private void format(String filePath, Document domdoc, Locale defaultLocale)
    {
        Node node = domdoc.selectSingleNode("xwikidoc/author");
        if (node != null) {
            node.setText(AUTHOR);
        }
        node = domdoc.selectSingleNode("xwikidoc/originalMetadataAuthor");
        if (node != null) {
            node.setText(AUTHOR);
        }
        node = domdoc.selectSingleNode("xwikidoc/contentAuthor");
        if (node != null) {
            node.setText(AUTHOR);
        }
        node = domdoc.selectSingleNode("xwikidoc/creator");
        if (node != null) {
            node.setText(AUTHOR);
        }
        node = domdoc.selectSingleNode("xwikidoc/version");
        if (node != null) {
            node.setText(VERSION);
        }
        node = domdoc.selectSingleNode("xwikidoc/minorEdit");
        if (node != null) {
            node.setText("false");
        }

        // Also update the attachment authors
        for (Node attachmentAuthorNode : domdoc.selectNodes("xwikidoc/attachment/author")) {
            attachmentAuthorNode.setText(AUTHOR);
        }

        // Set the default language
        Element element = (Element) domdoc.selectSingleNode("xwikidoc/defaultLanguage");
        if (element != null) {
            if (Locale.ROOT.equals(defaultLocale)) {
                removeContent(element);
            } else {
                element.setText(defaultLocale.toString());
            }
        }

        // Remove any content of the <comment> element
        element = (Element) domdoc.selectSingleNode("xwikidoc/comment");
        if (element != null) {
            removeContent(element);
        }

        // If the page is technical and not a visible technical page, make sure it's hidden
        element = (Element) domdoc.selectSingleNode("xwikidoc/hidden");
        if (!isContentPage(filePath) && !isVisibleTechnicalPage(filePath)) {
            element.setText("true");
        }

        // Remove date fields
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
            removeNodes("xwikidoc/originalMetadataAuthor", domdoc);            
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
