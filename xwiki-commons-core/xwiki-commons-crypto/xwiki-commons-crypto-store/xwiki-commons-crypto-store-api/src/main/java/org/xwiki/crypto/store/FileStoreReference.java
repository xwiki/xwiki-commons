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

package org.xwiki.crypto.store;

import java.io.File;

/**
 * Reference to a cryptographic store on the filesystem.
 *
 * @version $Id$
 * @since 6.1M2
 */
public class FileStoreReference implements StoreReference
{
    private File file;

    private boolean isMulti = true;

    /**
     * Wrap a file or folder as a store reference.
     *
     * @param file a file or folder.
     */
    public FileStoreReference(File file)
    {
        this.file = file;
        this.isMulti = file.isDirectory();
    }

    /**
     * Wrap a file or folder as a store reference.
     *
     * @param file a file or folder (that may not exists).
     * @param isMulti true to force a multi key store when the file does not exists yet.
     *                Actually, Multi key store are always directory.
     */
    public FileStoreReference(File file, boolean isMulti)
    {
        this.file = file;
        this.isMulti = file.exists() ? file.isDirectory() : isMulti;
    }

    /**
     * @return the wrapped file reference.
     */
    public File getFile()
    {
        return this.file;
    }

    /**
     * @return true if this store is a directory.
     */
    public boolean isMulti()
    {
        return this.isMulti;
    }
}
