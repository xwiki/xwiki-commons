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
package org.xwiki.tool.extension.internal;

import org.apache.maven.artifact.Artifact;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;

/**
 * @version $Id$
 * @since 9.4RC1
 */
public class ExtensionMojoCoreExtensionRepository extends DefaultCoreExtensionRepository
{
    @Override
    public void initialize() throws InitializationException
    {
        // Cancel standard DefaultCoreExtensionRepository#initialize() since we don't care about what's in Maven
        // classloader
    }

    public void addExtension(Extension extension)
    {
        DefaultCoreExtension coreExtension = new DefaultCoreExtension(this, null, extension);

        addExtension(coreExtension);
    }

    public void addExtension(Artifact artifact)
    {
        DefaultCoreExtension coreExtension = new DefaultCoreExtension(this, null,
            new ExtensionId(artifact.getGroupId() + ':' + artifact.getArtifactId()), artifact.getType());

        addExtension(coreExtension);
    }
}
