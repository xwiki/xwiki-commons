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

import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.maven.internal.MavenUtils;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.aether.internal.AetherExtensionRepository;
import org.xwiki.extension.repository.aether.internal.XWikiRepositorySystemSession;
import org.xwiki.extension.version.internal.DefaultVersion;

/**
 * @version $Id$
 * @since 9.4RC1
 */
public class MavenBuildExtensionRepository extends AetherExtensionRepository
{
    private MavenSession mavenSession;

    public MavenBuildExtensionRepository(MavenSession session, ArtifactRepository localRepository,
        PlexusContainer plexusContainer, ComponentManager componentManager) throws Exception
    {
        super(new DefaultExtensionRepositoryDescriptor("maven-build", "maven", null), null, null, plexusContainer,
            componentManager);

        this.mavenSession = session;
    }

    public static ExtensionId createExtensionId(Artifact artifact)
    {
        return createExtensionId(artifact, null);
    }

    public static ExtensionId createExtensionId(Artifact artifact, ExtensionFactory factory)
    {
        String extensionId =
            MavenUtils.toExtensionId(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier());

        return new ExtensionId(extensionId, factory != null ? factory.getVersion(artifact.getBaseVersion())
            : new DefaultVersion(artifact.getBaseVersion()));
    }

    @Override
    protected XWikiRepositorySystemSession createRepositorySystemSession()
    {
        XWikiRepositorySystemSession session =
            new XWikiRepositorySystemSession(this.mavenSession.getRepositorySession());

        return session;
    }

    @Override
    protected List<RemoteRepository> newResolutionRepositories(RepositorySystemSession session, boolean all)
    {
        return RepositoryUtils.toRepos(this.mavenSession.getCurrentProject().getRemoteArtifactRepositories());
    }
}
