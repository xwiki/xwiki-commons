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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

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

    /**
     * If true then don't check if the packaging is XAR before running mojos.
     *
     * @parameter expression="${force}"
     * @readonly
     */
    protected boolean force;

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
                    FileFilterUtils.suffixFileFilter(".xml"),
                    FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(PACKAGE_XML))),
                TrueFileFilter.INSTANCE);
        }

        return files;
    }
}
