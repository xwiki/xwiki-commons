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
import java.io.IOException;

import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;

/**
 * Workaround bugs in {@link ResourceExtractor}.
 * 
 * @version $Id$
 * @since 5.0M2
 */
public class FixedResourceExtractor extends ResourceExtractor
{
    /**
     * Proper version of {@link ResourceExtractor}.
     */
    public static File simpleExtractResources(Class< ? > cl, String resourcePath) throws IOException
    {
        String tempDirPath = System.getProperty("maven.test.tmpdir", System.getProperty("java.io.tmpdir"));
        File tempDir = new File(tempDirPath);

        File testDir = new File(tempDir, resourcePath);

        FileUtils.deleteDirectory(testDir);

        testDir = ResourceExtractor.extractResourcePath(cl, resourcePath, tempDir, true);
        return testDir;
    }
}
