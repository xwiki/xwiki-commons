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
package org.xwiki.extension.repository.internal.local;

import java.io.File;

import org.xwiki.extension.AbstractExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.LocalExtension;

/**
 * Default implementation of {@link LocalExtension}.
 * 
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultLocalExtension extends AbstractExtension implements LocalExtension
{
    /**
     * @see #getDescriptorFile()
     */
    private File descriptorFile;

    /**
     * @param repository the repository where this extension comes from
     * @param id the extension identifier
     * @param type the extension type
     */
    public DefaultLocalExtension(DefaultLocalExtensionRepository repository, ExtensionId id, String type)
    {
        super(repository, id, type);
    }

    /**
     * Create new extension descriptor by copying provided one.
     * 
     * @param repository the repository where this extension comes from
     * @param extension the extension to copy
     */
    public DefaultLocalExtension(DefaultLocalExtensionRepository repository, Extension extension)
    {
        super(repository, extension);
    }

    /**
     * @return the file containing the extension description
     */
    public File getDescriptorFile()
    {
        return this.descriptorFile;
    }

    /**
     * @param descriptorFile file containing the extension description
     */
    public void setDescriptorFile(File descriptorFile)
    {
        this.descriptorFile = descriptorFile;
    }

    /**
     * @param file the extension file in the filesystem
     * @see #getFile()
     */
    public void setFile(File file)
    {
        setFile(new DefaultLocalExtensionFile(file));
        putProperty(PKEY_FILE, file);
    }

    // LocalExtension

    @Override
    public DefaultLocalExtensionFile getFile()
    {
        return (DefaultLocalExtensionFile) super.getFile();
    }
}
