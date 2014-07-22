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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Parent;
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
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;

/**
 * A model resolver to assist building of dependency POMs. This resolver gives priority to those repositories that have
 * been initially specified and repositories discovered in dependency POMs are recessively merged into the search chain.
 * 
 * @version $Id$
 */
public class DefaultModelResolver implements ModelResolver
{
    private static final String POM = "pom";

    private final RepositorySystemSession session;

    private final String context;

    private List<RemoteRepository> repositories;

    private final List<RemoteRepository> externalRepositories;

    private final ArtifactResolver resolver;

    private final VersionRangeResolver versionRangeResolver;

    private final RemoteRepositoryManager remoteRepositoryManager;

    private final Set<String> repositoryIds;

    /**
     * @param session the session
     * @param context the context
     * @param resolver the resolver
     * @param versionRangeResolver the version range resolver
     * @param remoteRepositoryManager the repositories manager
     * @param repositories the initial repositories to search in
     */
    public DefaultModelResolver(RepositorySystemSession session, String context, ArtifactResolver resolver,
        VersionRangeResolver versionRangeResolver, RemoteRepositoryManager remoteRepositoryManager,
        List<RemoteRepository> repositories)
    {
        this.session = session;
        this.context = context;
        this.resolver = resolver;
        this.versionRangeResolver = versionRangeResolver;
        this.remoteRepositoryManager = remoteRepositoryManager;
        this.repositories = repositories;
        this.repositoryIds = new HashSet<String>();
        this.externalRepositories = Collections.unmodifiableList(new ArrayList<>(repositories));
    }

    private DefaultModelResolver(DefaultModelResolver original)
    {
        this.session = original.session;
        this.context = original.context;
        this.resolver = original.resolver;
        this.versionRangeResolver = original.versionRangeResolver;
        this.remoteRepositoryManager = original.remoteRepositoryManager;
        this.repositories = original.repositories;
        this.externalRepositories = original.externalRepositories;
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
        Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", POM, version);

        try {
            ArtifactRequest request = new ArtifactRequest(pomArtifact, this.repositories, this.context);
            pomArtifact = this.resolver.resolveArtifact(this.session, request).getArtifact();
        } catch (ArtifactResolutionException e) {
            throw new UnresolvableModelException(e.getMessage(), groupId, artifactId, version, e);
        }

        File pomFile = pomArtifact.getFile();

        return new FileModelSource(pomFile);
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException
    {
        Artifact artifact =
            new DefaultArtifact(parent.getGroupId(), parent.getArtifactId(), "", POM, parent.getVersion());

        VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact, repositories, context);

        try {
            VersionRangeResult versionRangeResult =
                this.versionRangeResolver.resolveVersionRange(session, versionRangeRequest);

            if (versionRangeResult.getHighestVersion() == null) {
                throw new UnresolvableModelException("No versions matched the requested range '" + parent.getVersion()
                    + "'", parent.getGroupId(), parent.getArtifactId(), parent.getVersion());

            }

            if (versionRangeResult.getVersionConstraint() != null
                && versionRangeResult.getVersionConstraint().getRange() != null
                && versionRangeResult.getVersionConstraint().getRange().getUpperBound() == null) {
                throw new UnresolvableModelException("The requested version range '" + parent.getVersion()
                    + "' does not specify an upper bound", parent.getGroupId(), parent.getArtifactId(),
                    parent.getVersion());

            }

            parent.setVersion(versionRangeResult.getHighestVersion().toString());
        } catch (VersionRangeResolutionException e) {
            throw new UnresolvableModelException(e.getMessage(), parent.getGroupId(), parent.getArtifactId(),
                parent.getVersion(), e);

        }

        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public void resetRepositories()
    {
        this.repositoryIds.clear();
        this.repositories.clear();
        this.repositories.addAll(externalRepositories);
    }
}
