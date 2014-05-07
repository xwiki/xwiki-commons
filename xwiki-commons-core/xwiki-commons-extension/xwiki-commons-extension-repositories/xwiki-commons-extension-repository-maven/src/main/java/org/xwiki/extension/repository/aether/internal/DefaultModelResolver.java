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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

/**
 * A model resolver to assist building of dependency POMs. This resolver gives priority to those repositories that have
 * been initially specified and repositories discovered in dependency POMs are recessively merged into the search chain.
 * 
 * @version $Id$
 */
public class DefaultModelResolver implements ModelResolver
{
    private final RepositorySystemSession session;

    private final String context;

    private List<RemoteRepository> repositories;

    private final ArtifactResolver resolver;

    private final RemoteRepositoryManager remoteRepositoryManager;

    private final Set<String> repositoryIds;

    /**
     * @param session the session
     * @param context the context
     * @param resolver the resolver
     * @param remoteRepositoryManager the repositories manager
     * @param repositories the initial repositories to search in
     */
    public DefaultModelResolver(RepositorySystemSession session, String context, ArtifactResolver resolver,
        RemoteRepositoryManager remoteRepositoryManager, List<RemoteRepository> repositories)
    {
        this.session = session;
        this.context = context;
        this.resolver = resolver;
        this.remoteRepositoryManager = remoteRepositoryManager;
        this.repositories = repositories;
        this.repositoryIds = new HashSet<String>();
    }

    private DefaultModelResolver(DefaultModelResolver original)
    {
        this.session = original.session;
        this.context = original.context;
        this.resolver = original.resolver;
        this.remoteRepositoryManager = original.remoteRepositoryManager;
        this.repositories = original.repositories;
        this.repositoryIds = new HashSet<String>(original.repositoryIds);
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException
    {
        if (this.session.isIgnoreArtifactDescriptorRepositories() || !this.repositoryIds.add(repository.getId())) {
            return;
        }

        List<RemoteRepository> newRepositories =
            Collections.singletonList(ArtifactDescriptorUtils.toRemoteRepository(repository));

        this.repositories =
            this.remoteRepositoryManager.aggregateRepositories(this.session, this.repositories, newRepositories, true);
    }

    @Override
    public ModelResolver newCopy()
    {
        return new DefaultModelResolver(this);
    }

    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version)
        throws UnresolvableModelException
    {
        Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version);

        try {
            ArtifactRequest request = new ArtifactRequest(pomArtifact, this.repositories, this.context);
            pomArtifact = this.resolver.resolveArtifact(this.session, request).getArtifact();
        } catch (ArtifactResolutionException e) {
            throw new UnresolvableModelException(e.getMessage(), groupId, artifactId, version, e);
        }

        File pomFile = pomArtifact.getFile();

        return new FileModelSource(pomFile);
    }

}
