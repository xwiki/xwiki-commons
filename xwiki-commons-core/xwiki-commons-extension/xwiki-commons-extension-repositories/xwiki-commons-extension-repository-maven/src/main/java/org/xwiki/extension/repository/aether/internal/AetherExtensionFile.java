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
package org.xwiki.extension.repository.aether.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.aether.artifact.Artifact;
import org.xwiki.extension.ExtensionFile;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class AetherExtensionFile implements ExtensionFile
{
    private Artifact artifact;

    private AetherExtensionRepository repository;

    public AetherExtensionFile(Artifact artifact, AetherExtensionRepository repository)
    {
        this.repository = repository;
        this.artifact = artifact;
    }

    @Override
    public long getLength()
    {
        // TODO
        return -1;
    }

    @Override
    public InputStream openStream() throws IOException
    {
        return this.repository.openStream(this.artifact);
    }
}
