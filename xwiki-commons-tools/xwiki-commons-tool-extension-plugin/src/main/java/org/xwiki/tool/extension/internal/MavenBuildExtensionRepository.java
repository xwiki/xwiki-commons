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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
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

    /**
     * {@inheritDoc}
     * <p>
     * Override standard {@link #openStream(org.eclipse.aether.artifact.Artifact)} to reuse running Maven session which
     * is much faster.
     * 
     * @see org.xwiki.extension.repository.aether.internal.AetherExtensionRepository#openStream(org.eclipse.aether.artifact.Artifact)
     */
    @Override
    public InputStream openStream(org.eclipse.aether.artifact.Artifact artifact) throws IOException
    {
        XWikiRepositorySystemSession session = createRepositorySystemSession();

        List<RemoteRepository> repositories = newResolutionRepositories(session);

        // /////////////////////////////////////////////////////////////////////////////:

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setRepositories(repositories);
        artifactRequest.setArtifact(artifact);

        ArtifactResult artifactResult;
        try {
            RepositorySystem repositorySystem = getRepositorySystem();
            artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);
        } catch (org.eclipse.aether.resolution.ArtifactResolutionException e) {
            throw new IOException("Failed to resolve artifact", e);
        }

        File aetherFile = artifactResult.getArtifact().getFile();

        return new AetherExtensionFileInputStream(aetherFile, false);
    }

    @Override
    protected List<RemoteRepository> newResolutionRepositories(RepositorySystemSession session, boolean all)
    {
        return RepositoryUtils.toRepos(this.mavenSession.getCurrentProject().getRemoteArtifactRepositories());
    }
}
