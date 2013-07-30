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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.RepositoryConnectorProvider;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.ArtifactDownload;
import org.eclipse.aether.spi.connector.RepositoryConnector;
import org.eclipse.aether.transfer.NoRepositoryConnectorException;
import org.xwiki.extension.ExtensionFile;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class AetherExtensionFile implements ExtensionFile
{
    private PlexusContainer plexusContainer;

    private Artifact artifact;

    private AetherExtensionRepository repository;

    static class AetherExtensionFileInputStream extends FileInputStream
    {
        private File file;

        public AetherExtensionFileInputStream(File file) throws FileNotFoundException
        {
            super(file);

            this.file = file;
        }

        @Override
        public void close() throws IOException
        {
            super.close();

            // Delete the file until a real stream download is done
            FileUtils.deleteQuietly(this.file);
        }
    }

    public AetherExtensionFile(Artifact artifact, AetherExtensionRepository repository, PlexusContainer plexusContainer)
    {
        this.repository = repository;
        this.plexusContainer = plexusContainer;
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
        RepositorySystem repositorySystem;
        RepositoryConnectorProvider repositoryConnectorProvider;
        try {
            repositorySystem = this.plexusContainer.lookup(RepositorySystem.class);
            repositoryConnectorProvider = this.plexusContainer.lookup(RepositoryConnectorProvider.class);
        } catch (ComponentLookupException e) {
            throw new IOException("Failed to get org.sonatype.aether.RepositorySystem component", e);
        }

        RepositorySystemSession session = this.repository.createRepositorySystemSession();

        RepositoryConnector connector;
        try {
            connector =
                repositoryConnectorProvider.newRepositoryConnector(session, this.repository.getRemoteRepository());
        } catch (NoRepositoryConnectorException e) {
            throw new IOException("Failed to download artifact [" + this.artifact + "]", e);
        }

        ArtifactDownload download = new ArtifactDownload();
        download.setArtifact(this.artifact);
        download.setRepositories(Arrays.asList(this.repository.getRemoteRepository()));

        try {
            connector.get(Arrays.asList(download), null);
        } finally {
            connector.close();
        }

        // /////////////////////////////////////////////////////////////////////////////:

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.addRepository(this.repository.getRemoteRepository());
        artifactRequest.setArtifact(this.artifact);

        ArtifactResult artifactResult;
        try {
            artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);
        } catch (ArtifactResolutionException e) {
            throw new IOException("Failed to resolve artifact", e);
        }

        File aetherFile = artifactResult.getArtifact().getFile();

        return new AetherExtensionFileInputStream(aetherFile);
    }
}
