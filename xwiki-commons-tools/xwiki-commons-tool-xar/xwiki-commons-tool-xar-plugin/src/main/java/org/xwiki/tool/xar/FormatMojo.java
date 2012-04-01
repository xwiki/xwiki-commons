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
import java.io.FileWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Pretty prints and set valid authors and version to XAR XML files.
 *
 * @version $Id$
 * @goal format
 * @requiresProject
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class FormatMojo extends AbstractVerifyMojo
{
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Formatting XAR XML files...");
        for (File file : getXARXMLFiles()) {
            try {
                format(file);
            } catch (Exception e) {
                throw new MojoExecutionException(String.format("Failed to format file [%s]", file));
            }
        }
    }

    private void format(File file) throws Exception
    {
        SAXReader reader = new SAXReader();
        Document domdoc = reader.read(file);

        Node node = domdoc.selectSingleNode("xwikidoc/author");
        node.setText(AUTHOR);
        node = domdoc.selectSingleNode("xwikidoc/contentAuthor");
        node.setText(AUTHOR);
        node = domdoc.selectSingleNode("xwikidoc/creator");
        node.setText(AUTHOR);
        node = domdoc.selectSingleNode("xwikidoc/version");
        node.setText(VERSION);

        OutputFormat format = new OutputFormat(" ", true, "UTF-8");
        format.setTrimText(false);
        format.setExpandEmptyElements(true);

        XMLWriter writer = new XMLWriter(new FileWriter(file), format);
        writer.write(domdoc);
        writer.close();

        String parentName = file.getParentFile().getName();
        getLog().info(String.format("  Formatting [%s/%s]... ok", parentName, file.getName()));
    }
}
