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
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Perform various verifications of the XAR files in this project. Namely:
 * <ul>
 *   <li>ensure that pages all have a parent (except for Main.WebHome)</li>
 *   <li>ensure that the author/contentAuthor/creator is {@code xwiki:XWiki.Admin}</li>
 *   <li>ensure that the version is {@code 1.1}</li>
 * </ul>
 *
 * @version $Id$
 * @goal verify
 * @phase verify
 * @requiresProject
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class VerifyMojo extends AbstractXARMojo
{
    private static final String AUTHOR = "xwiki:XWiki.Admin";

    private static final String VERSION = "1.1";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Find all files in the resources dir
        File resourcesDir = getResourcesDirectory();

        getLog().info("Checking validity of XAR XML files...");

        Collection<File> files = FileUtils.listFiles(resourcesDir, FileFilterUtils.and(
            FileFilterUtils.suffixFileFilter(".xml"),
            FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(PACKAGE_XML))), TrueFileFilter.INSTANCE);
        for (File file : files) {
            XWikiDocument xdoc = getDocFromXML(file);
            // Verification 1: Verify authors
            verifyAuthor(xdoc.getAuthor(), String.format("[%s]: Author must be [%s] but was [%s]",
                file.getName(), AUTHOR, xdoc.getAuthor()));
            verifyAuthor(xdoc.getContentAuthor(), String.format("[%s]: Content Author must be [%s] but was [%s]",
                file.getName(), AUTHOR, xdoc.getContentAuthor()));
            verifyAuthor(xdoc.getCreator(), String.format("[%s]: Creator must be [%s] but was [%s]",
                file.getName(), AUTHOR, xdoc.getCreator()));
            // Verification 2: Check for orphans
            if (StringUtils.isEmpty(xdoc.getParent())) {
                throw new MojoFailureException(String.format("[%s]: Parent must not be empty", file.getName()));
            }
            // Verification 3: Check for version
            if (!xdoc.getVersion().equals(VERSION)) {
                throw new MojoFailureException(String.format("[%s]: Version must be [%s] but was [%s]",
                    file.getName(), VERSION, xdoc.getVersion()));
            }
            getLog().info(String.format("  Verifying [%s]... ok", file.getName()));
        }
    }

    private void verifyAuthor(String author, String message) throws MojoFailureException
    {
        if (!author.equals(AUTHOR)) {
            throw new MojoFailureException(message);
        }
    }
}
