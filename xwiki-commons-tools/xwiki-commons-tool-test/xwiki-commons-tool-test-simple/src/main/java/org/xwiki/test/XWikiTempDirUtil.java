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
package org.xwiki.test;

import java.io.File;
import java.util.Date;

/**
 * Utility method to create a unique temporary directory located under Maven's {@code target} directory.
 *
 * @version $Id$
 * @since 11.7RC1
 */
public final class XWikiTempDirUtil
{
    private XWikiTempDirUtil()
    {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * @return the unique temporary directory created underMaven's {@code target} directory.
     */
    public static File createTemporaryDirectory()
    {
        File tmpDir = new File("target/test-" + new Date().getTime()).getAbsoluteFile();
        tmpDir.mkdirs();
        return tmpDir;
    }
}
