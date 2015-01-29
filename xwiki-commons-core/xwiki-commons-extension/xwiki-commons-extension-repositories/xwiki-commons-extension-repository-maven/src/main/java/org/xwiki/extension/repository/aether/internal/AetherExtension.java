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

import org.apache.maven.model.Model;
import org.eclipse.aether.artifact.Artifact;
import org.xwiki.extension.AbstractExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.repository.internal.MavenExtension;

/**
 * Add support for repositories supported by AETHER (only Maven for now).
 *
 * @version $Id$
 * @since 4.0M1
 */
public class AetherExtension extends AbstractExtension implements MavenExtension
{
    public static final String PKEY_AETHER_ATIFACT = "aether.Artifact";

    public static final String PKEY_MAVEN_MODEL = "maven.Model";

    public AetherExtension(Extension mavenExtension, Artifact artifact, AetherExtensionRepository repository)
    {
        super(repository, mavenExtension);

        setId(AetherUtils.createExtensionId(artifact));
        setType(artifact.getExtension());

        setFile(new AetherExtensionFile(artifact, repository));
    }

    /**
     * @return the Maven model object
     */
    public Model getMavenModel()
    {
        return (Model) getProperty(PKEY_MAVEN_MODEL);
    }

    @Override
    public String getMavenArtifactId()
    {
        return getMavenModel().getArtifactId();
    }

    @Override
    public String getMavenGroupId()
    {
        return getMavenModel().getGroupId();
    }
}
