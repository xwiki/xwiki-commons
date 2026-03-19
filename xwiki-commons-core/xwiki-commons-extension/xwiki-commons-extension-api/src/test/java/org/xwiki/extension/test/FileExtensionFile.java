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
package org.xwiki.extension.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xwiki.extension.ExtensionFile;
import org.xwiki.extension.LocalExtensionFile;

/**
 * A filesystem based implementation of {@link LocalExtensionFile}.
 *
 * @version $Id$
 * @since 18.2.0RC1
 */
public class FileExtensionFile implements ExtensionFile
{
    /**
     * The filesystem file of the local extension.
     */
    private File file;

    /**
     * @param file the filesystem file of the local extension
     */
    public FileExtensionFile(File file)
    {
        this.file = file;
    }

    /**
     * @return the real file
     */
    public File getFile()
    {
        return this.file;
    }

    // ExtensionFile

    @Override
    public long getLength()
    {
        return getFile().length();
    }

    @Override
    public InputStream openStream() throws IOException
    {
        return new FileInputStream(getFile());
    }
}
