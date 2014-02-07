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
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

/**
 * Common code for the Verify and Format Mojos.
 *
 * @version $Id$
 * @since 4.0M2
 */
public abstract class AbstractVerifyMojo extends AbstractXARMojo
{
    /**
     * Admin author for creator/contentAuthor and author.
     */
    protected static final String AUTHOR = "xwiki:XWiki.Admin";

    /**
     * First version.
     */
    protected static final String VERSION = "1.1";

    private static final String EXTENSION = ".xml";

    private static final Pattern TRANSLATION_PATTERN = Pattern.compile("(.*)\\..*\\.xml");

    /**
     * If true then don't check if the packaging is XAR before running mojos.
     *
     * @parameter expression="${force}"
     * @readonly
     */
    protected boolean force;

    /**
     * The language in which non-translated documents are written in.
     *
     * @parameter expression="${defaultLanguage}" default-value="en"
     */
    protected String defaultLanguage;

    /**
     * @return the list of XAR XML files in this project
     */
    protected Collection<File> getXARXMLFiles()
    {
        // Find all files in the resources dir
        File resourcesDir = getResourcesDirectory();

        // Filter package.xml and files not ending with .xml
        Collection<File> files = Collections.emptyList();
        if (resourcesDir.exists()) {
            files = FileUtils.listFiles(resourcesDir,
                FileFilterUtils.and(
                    FileFilterUtils.suffixFileFilter(EXTENSION),
                    FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(PACKAGE_XML))),
                TrueFileFilter.INSTANCE);
        }

        return files;
    }

    /**
     * Guess the {@code &lt;defaultLanguage&gt;} value to use for the passed file using the following algorithm:
     * <ul>
     *     <li>If there's no other translation of the file then consider default language to be empty to signify that
     *         it's a technical document. </li>
     *     <li>If there are other translations ("(prefix).(language).xml" format) then the default language should be
     *         {@link #defaultLanguage}</li>
     * </ul>
     * @since 5.4.1
     */
    protected String guessDefaultLanguage(File file, Collection<File> xwikiXmlFiles)
    {
        String language = "";

        // Check if the doc is a translation
        Matcher matcher = TRANSLATION_PATTERN.matcher(file.getName());
        if (matcher.matches()) {
            // We're in a translation, use the default language
            language = this.defaultLanguage;
        } else {
            // We're not in a translation, check if there are translations. First get the doc name before the extension
            String prefix = StringUtils.substringBeforeLast(file.getName(), EXTENSION);
            // Check for a translation now
            Pattern translationPattern = Pattern.compile(String.format("%s\\..*\\.xml", Pattern.quote(prefix)));
            for (File xwikiXmlFile : xwikiXmlFiles) {
                Matcher translationMatcher = translationPattern.matcher(xwikiXmlFile.getName());
                if (translationMatcher.matches()) {
                    // Found a translation, use the default language
                    language = this.defaultLanguage;
                    break;
                }
            }
        }
        return language;
    }
}
