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
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Pretty prints and set valid authors and version to XAR XML files.
 *
 * @version $Id$
 */
@Mojo(
    name = "format",
    threadSafe = true
)
public class FormatMojo extends AbstractVerifyMojo
{
    /**
     * If false then don't pretty print the XML.
     */
    @Parameter(property = "pretty", readonly = true)
    private boolean pretty = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Only format XAR modules or when forced
        if (getProject().getPackaging().equals("xar") || this.force) {
            getLog().info("Formatting XAR XML files...");
            initializePatterns();
            Collection<File> xmlFiles = getXARXMLFiles();
            for (File file : xmlFiles) {
                try {
                    format(file, guessDefaultLanguage(file, xmlFiles));
                } catch (Exception e) {
                    throw new MojoExecutionException(String.format("Failed to format file [%s]", file), e);
                }
            }
            if (this.formatLicense) {
                getLog().info("Adding missing XAR XML license headers...");
                executeLicenseGoal("format");
            }
        } else {
            getLog().info("Not a XAR module, skipping reformatting...");
        }
    }

    private void format(File file, String defaultLanguage) throws Exception
    {
        SAXReader reader = new SAXReader();
        Document domdoc = reader.read(file);
        format(file.getName(), domdoc, defaultLanguage);

        XMLWriter writer;
        if (this.pretty) {
            OutputFormat format = new OutputFormat("  ", true, "UTF-8");
            format.setExpandEmptyElements(false);
            writer = new XWikiXMLWriter(new FileOutputStream(file), format);
        } else {
            writer = new XWikiXMLWriter(new FileOutputStream(file));
        }
        writer.write(domdoc);
        writer.close();

        String parentName = file.getParentFile().getName();
        getLog().info(String.format("  Formatting [%s/%s]... ok", parentName, file.getName()));
    }

    private void format(String fileName, Document domdoc, String defaultLanguage) throws Exception
    {
        Node node = domdoc.selectSingleNode("xwikidoc/author");
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
        for (Object attachmentAuthorNode : domdoc.selectNodes("xwikidoc/attachment/author")) {
            ((Node) attachmentAuthorNode).setText(AUTHOR);
        }

        // Set the default language
        Element element = (Element) domdoc.selectSingleNode("xwikidoc/defaultLanguage");
        if (element != null) {
            if (StringUtils.isEmpty(defaultLanguage)) {
                removeContent(element);
            } else {
                element.setText(defaultLanguage);
            }
        }

        // Remove any content of the <comment> element
        element = (Element) domdoc.selectSingleNode("xwikidoc/comment");
        if (element != null) {
            removeContent(element);
        }

        // If the page is technical, make sure it's hidedn
        element = (Element) domdoc.selectSingleNode("xwikidoc/hidden");
        if (isTechnicalPage(fileName)) {
            element.setText("true");
        }
    }

    private void removeContent(Element element)
    {
        if (element.hasContent()) {
            ((Node) element.content().get(0)).detach();
        }
    }
}
