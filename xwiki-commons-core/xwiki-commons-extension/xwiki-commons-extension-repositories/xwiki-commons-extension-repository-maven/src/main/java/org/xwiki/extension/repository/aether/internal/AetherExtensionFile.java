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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.RepositoryConnectorProvider;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.ArtifactDownload;
import org.eclipse.aether.spi.connector.RepositoryConnector;
import org.eclipse.aether.transfer.NoRepositoryConnectorException;
import org.xwiki.extension.ExtensionFile;
import org.xwiki.extension.ResolveException;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class AetherExtensionFile implements ExtensionFile
{
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

            if (this.file.exists()) {
                Files.delete(this.file.toPath());
            }
        }
    }

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
        XWikiRepositorySystemSession session;
        try {
            session = this.repository.createRepositorySystemSession();
        } catch (ResolveException e) {
            throw new IOException("Failed to create the repository system session", e);
        }

        List<RemoteRepository> repositories = this.repository.newResolutionRepositories(session);
        RemoteRepository repository = repositories.get(0);

        RepositoryConnector connector;
        try {
            RepositoryConnectorProvider repositoryConnectorProvider = this.repository.getRepositoryConnectorProvider();
            connector = repositoryConnectorProvider.newRepositoryConnector(session, repository);
        } catch (NoRepositoryConnectorException e) {
            throw new IOException("Failed to download artifact [" + this.artifact + "]", e);
        }

        File file = this.repository.createTemporaryFile(this.artifact.getArtifactId(), this.artifact.getExtension());

        ArtifactDownload download = new ArtifactDownload();
        download.setArtifact(this.artifact);
        download.setRepositories(repositories);
        download.setFile(file);

        try {
            connector.get(Arrays.asList(download), null);
        } finally {
            connector.close();
            session.close();
        }

        return new AetherExtensionFileInputStream(file);
    }
}
