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
package org.xwiki.store.blob;

import java.nio.file.Path;

import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.stability.Unstable;

/**
 * Properties bean for the filesystem blob store.
 * This class defines the properties required for configuring a filesystem-based blob store.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public class FileSystemBlobStoreProperties implements BlobStoreProperties
{
    /**
     * The property ID for the root directory.
     */
    public static final String ROOT_DIRECTORY = "filesystem.rootDirectory";

    /**
     * The root directory for the filesystem blob store.
     */
    private Path rootDirectory;

    /**
     * Gets the root directory for the filesystem blob store.
     *
     * @return the root directory
     */
    public Path getRootDirectory()
    {
        return this.rootDirectory;
    }

    /**
     * Sets the root directory for the filesystem blob store.
     *
     * @param rootDirectory the root directory to set
     */
    @PropertyId(ROOT_DIRECTORY)
    @PropertyMandatory
    public void setRootDirectory(Path rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }
}
