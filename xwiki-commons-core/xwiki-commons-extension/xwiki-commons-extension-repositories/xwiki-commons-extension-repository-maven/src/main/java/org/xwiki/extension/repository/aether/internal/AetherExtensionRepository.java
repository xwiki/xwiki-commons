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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.apache.maven.repository.internal.PublicDefaultModelResolver;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.RepositoryConnectorProvider;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.DefaultExtensionScm;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionFeature;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.internal.maven.MavenExtensionDependency;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.extension.version.VersionRange;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.properties.ConverterManager;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class AetherExtensionRepository extends AbstractExtensionRepository
{
    /**
     * Used to parse the version.
     */
    private static final GenericVersionScheme AETHERVERSIONSCHEME = new GenericVersionScheme();

    private transient PlexusContainer plexusContainer;

    private transient RemoteRepository remoteRepository;

    private transient ArtifactDescriptorReader mavenDescriptorReader;

    private transient VersionRangeResolver versionRangeResolver;

    private transient VersionResolver versionResolver;

    private transient ModelBuilder modelBuilder;

    private transient ArtifactResolver artifactResolver;

    private transient RepositorySystem repositorySystem;

    private transient RepositoryConnectorProvider repositoryConnectorProvider;

    private transient RemoteRepositoryManager remoteRepositoryManager;

    private transient ConverterManager converter;

    private transient AetherExtensionRepositoryFactory repositoryFactory;

    public AetherExtensionRepository(ExtensionRepositoryDescriptor repositoryDescriptor,
        AetherExtensionRepositoryFactory repositoryFactory, PlexusContainer plexusContainer,
        ComponentManager componentManager) throws Exception
    {
        super(repositoryDescriptor);

        this.repositoryFactory = repositoryFactory;
        this.plexusContainer = plexusContainer;

        RemoteRepository.Builder repositoryBuilder =
            new RemoteRepository.Builder(repositoryDescriptor.getId(), "default", repositoryDescriptor.getURI()
                .toString());

        // Don't use cached data
        repositoryBuilder.setPolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_ALWAYS,
            RepositoryPolicy.CHECKSUM_POLICY_WARN));

        // Authentication
        String username = getDescriptor().getProperty("auth.user");
        if (username != null) {
            AuthenticationBuilder authenticationBuilder = new AuthenticationBuilder();
            authenticationBuilder.addUsername(username);
            authenticationBuilder.addPassword(getDescriptor().getProperty("auth.password"));
            repositoryBuilder.setAuthentication(authenticationBuilder.build());
        }

        // Proxy
        Proxy proxy = XWikiRepositorySystemSession.JREPROXYSELECTOR.getProxy(repositoryBuilder.build());
        repositoryBuilder.setProxy(proxy);

        this.remoteRepository = repositoryBuilder.build();

        this.converter = componentManager.getInstance(ConverterManager.class);

        this.versionRangeResolver = this.plexusContainer.lookup(VersionRangeResolver.class);
        this.versionResolver = this.plexusContainer.lookup(VersionResolver.class);
        this.modelBuilder = this.plexusContainer.lookup(ModelBuilder.class);
        this.artifactResolver = this.plexusContainer.lookup(ArtifactResolver.class);
        this.repositorySystem = this.plexusContainer.lookup(RepositorySystem.class);
        this.mavenDescriptorReader = this.plexusContainer.lookup(ArtifactDescriptorReader.class);
        this.repositoryConnectorProvider = this.plexusContainer.lookup(RepositoryConnectorProvider.class);
        this.remoteRepositoryManager = this.plexusContainer.lookup(RemoteRepositoryManager.class);
    }

    public RemoteRepository getRemoteRepository()
    {
        return this.remoteRepository;
    }

    public RepositorySystem getRepositorySystem()
    {
        return this.repositorySystem;
    }

    public RepositoryConnectorProvider getRepositoryConnectorProvider()
    {
        return this.repositoryConnectorProvider;
    }

    protected XWikiRepositorySystemSession createRepositorySystemSession()
    {
        return this.repositoryFactory.createRepositorySystemSession();
    }

    @Override
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        if (getDescriptor().getType().equals("maven") && this.mavenDescriptorReader != null) {
            return resolveMaven(extensionId);
        } else {
            // FIXME: impossible to resolve extension type as well as most of the information with pure Aether API
            throw new ResolveException("Unsupported");
        }
    }

    @Override
    public Extension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        if (getDescriptor().getType().equals("maven") && this.mavenDescriptorReader != null) {
            return resolveMaven(extensionDependency);
        } else {
            // FIXME: impossible to resolve extension type as well as most of the information with pure Aether API
            throw new ResolveException("Unsupported");
        }
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        Artifact artifact = AetherUtils.createArtifact(id, "(,)");

        List<org.eclipse.aether.version.Version> versions;
        try (XWikiRepositorySystemSession session = createRepositorySystemSession()) {
            versions = resolveVersions(artifact, session);

            if (versions.isEmpty()) {
                throw new ResolveException("No versions available for id [" + id + "]");
            }
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve versions for id [" + id + "]", e);
        }

        if (nb == 0 || offset >= versions.size()) {
            return new CollectionIterableResult<Version>(versions.size(), offset, Collections.<Version>emptyList());
        }

        int fromId = offset < 0 ? 0 : offset;
        int toId = offset + nb > versions.size() || nb < 0 ? versions.size() : offset + nb;

        List<Version> result = new ArrayList<Version>(toId - fromId);
        for (int i = fromId; i < toId; ++i) {
            result.add(new DefaultVersion(versions.get(i).toString()));
        }

        return new CollectionIterableResult<Version>(versions.size(), offset, result);
    }

    private org.eclipse.aether.version.Version resolveVersionConstraint(String id, VersionConstraint versionConstraint,
        RepositorySystemSession session) throws ResolveException
    {
        if (versionConstraint.getVersion() != null) {
            try {
                return AETHERVERSIONSCHEME.parseVersion(versionConstraint.getVersion().getValue());
            } catch (InvalidVersionSpecificationException e) {
                throw new ResolveException("Invalid version [" + versionConstraint.getVersion() + "]", e);
            }
        }

        List<org.eclipse.aether.version.Version> commonVersions = null;

        for (VersionRange range : versionConstraint.getRanges()) {
            List<org.eclipse.aether.version.Version> versions = resolveVersionRange(id, range, session);

            if (commonVersions == null) {
                commonVersions =
                    versionConstraint.getRanges().size() > 1 ? new ArrayList<org.eclipse.aether.version.Version>(
                        versions) : versions;
            } else {
                // Find commons versions between all the ranges of the constraint
                for (Iterator<org.eclipse.aether.version.Version> it = commonVersions.iterator(); it.hasNext();) {
                    org.eclipse.aether.version.Version version = it.next();
                    if (!versions.contains(version)) {
                        it.remove();
                    }
                }
            }
        }

        if (commonVersions == null || commonVersions.isEmpty()) {
            throw new ResolveException("No versions available for id [" + id + "] and version constraint ["
                + versionConstraint + "]");
        }

        return commonVersions.get(commonVersions.size() - 1);
    }

    private List<org.eclipse.aether.version.Version> resolveVersionRange(String id, VersionRange versionRange,
        RepositorySystemSession session) throws ResolveException
    {
        Artifact artifact = AetherUtils.createArtifact(id, versionRange.getValue());

        try {
            List<org.eclipse.aether.version.Version> versions = resolveVersions(artifact, session);

            if (versions.isEmpty()) {
                throw new ResolveException("No versions available for id [" + id + "] and version range ["
                    + versionRange + "]");
            }

            return versions;
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve version range", e);
        }
    }

    List<org.eclipse.aether.version.Version> resolveVersions(Artifact artifact, RepositorySystemSession session)
        throws VersionRangeResolutionException
    {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(newResolutionRepositories(session));

        VersionRangeResult rangeResult = this.versionRangeResolver.resolveVersionRange(session, rangeRequest);

        return rangeResult.getVersions();
    }

    private AetherExtension resolveMaven(ExtensionDependency extensionDependency) throws ResolveException
    {
        Artifact artifact;
        String artifactExtension;
        try (XWikiRepositorySystemSession session = createRepositorySystemSession()) {
            if (extensionDependency instanceof AetherExtensionDependency) {
                artifact = ((AetherExtensionDependency) extensionDependency).getAetherDependency().getArtifact();
                artifactExtension =
                    ((AetherExtensionDependency) extensionDependency).getAetherDependency().getArtifact()
                        .getExtension();
            } else {
                artifact =
                    AetherUtils.createArtifact(extensionDependency.getId(), extensionDependency.getVersionConstraint()
                        .getValue());
                if (!extensionDependency.getVersionConstraint().getRanges().isEmpty()) {
                    artifact =
                        artifact.setVersion(resolveVersionConstraint(extensionDependency.getId(),
                            extensionDependency.getVersionConstraint(), session).toString());
                }
                artifactExtension = null;
            }
        }

        return resolveMaven(artifact, artifactExtension);
    }

    private AetherExtension resolveMaven(ExtensionId extensionId) throws ResolveException
    {
        Artifact artifact = AetherUtils.createArtifact(extensionId.getId(), extensionId.getVersion().getValue());

        return resolveMaven(artifact, null);
    }

    private AetherExtension resolveMaven(Artifact artifact, String artifactExtension) throws ResolveException
    {
        try (XWikiRepositorySystemSession session = createRepositorySystemSession()) {
            return resolveMaven(artifact, artifactExtension, session);
        }
    }

    private AetherExtension resolveMaven(Artifact artifact, String artifactExtension, RepositorySystemSession session)
        throws ResolveException
    {
        // Get Maven descriptor

        Model model;
        try {
            model = loadPom(artifact, session);
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve artifact [" + artifact + "] descriptor", e);
        }

        if (model == null) {
            throw new ResolveException("Failed to resolve artifact [" + artifact + "] descriptor");
        }

        // Set type

        if (artifactExtension == null) {
            // Resolve extension from the pom packaging
            ArtifactType artifactType = session.getArtifactTypeRegistry().get(model.getPackaging());
            if (artifactType != null) {
                artifactExtension = artifactType.getExtension();
            } else {
                artifactExtension = model.getPackaging();
            }
        }

        Extension mavenExtension = this.converter.convert(Extension.class, model);

        Artifact filerArtifact =
            new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                artifactExtension, artifact.getVersion());

        AetherExtension extension = new AetherExtension(mavenExtension, filerArtifact, this);

        // features
        String featuresString = getProperty(model, MPNAME_FEATURES);
        if (StringUtils.isNotBlank(featuresString)) {
            featuresString = featuresString.replaceAll("[\r\n]", "");
            extension.setExtensionFeatures(this.converter.<Collection<ExtensionFeature>>convert(
                new DefaultParameterizedType(null, List.class, ExtensionFeature.class), featuresString));
        }

        // Convert Maven dependencies to Aether dependencies
        List<ExtensionDependency> dependencies = new ArrayList<>(mavenExtension.getDependencies().size());
        try {
            ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();

            for (MavenExtensionDependency mavenDependency : (Collection<MavenExtensionDependency>) mavenExtension
                .getDependencies()) {
                dependencies.add(new AetherExtensionDependency(mavenDependency, convertToAether(
                    mavenDependency.getMavenDependency(), stereotypes), this.getDescriptor()));
            }
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve dependencies", e);
        }
        extension.setDependencies(dependencies);

        return extension;
    }

    private Dependency convertToAether(org.apache.maven.model.Dependency dependency, ArtifactTypeRegistry stereotypes)
    {
        ArtifactType stereotype = stereotypes.get(dependency.getType());
        if (stereotype == null) {
            stereotype = new DefaultArtifactType(dependency.getType());
        }

        boolean system = dependency.getSystemPath() != null && dependency.getSystemPath().length() > 0;

        Map<String, String> props = null;
        if (system) {
            props = Collections.singletonMap(ArtifactProperties.LOCAL_PATH, dependency.getSystemPath());
        }

        Artifact artifact =
            new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), null,
                dependency.getVersion(), props, stereotype);

        List<Exclusion> exclusions = new ArrayList<Exclusion>(dependency.getExclusions().size());
        for (org.apache.maven.model.Exclusion exclusion : dependency.getExclusions()) {
            exclusions.add(convert(exclusion));
        }

        Dependency result = new Dependency(artifact, dependency.getScope(), dependency.isOptional(), exclusions);

        return result;
    }

    private Exclusion convert(org.apache.maven.model.Exclusion exclusion)
    {
        return new Exclusion(exclusion.getGroupId(), exclusion.getArtifactId(), "*", "*");
    }

    private Artifact resolveVersion(Artifact artifact, List<RemoteRepository> repositories,
        RepositorySystemSession session) throws VersionResolutionException
    {
        Artifact pomArtifact = ArtifactDescriptorUtils.toPomArtifact(artifact);

        VersionRequest versionRequest = new VersionRequest(artifact, repositories, "");
        VersionResult versionResult = this.versionResolver.resolveVersion(session, versionRequest);

        return pomArtifact.setVersion(versionResult.getVersion());
    }

    private Model loadPom(Artifact artifact, RepositorySystemSession session) throws VersionResolutionException,
        ArtifactResolutionException, ModelBuildingException
    {
        List<RemoteRepository> repositories = newResolutionRepositories(session);

        Artifact pomArtifact = resolveVersion(artifact, repositories, session);

        // Download pom file

        ArtifactRequest resolveRequest = new ArtifactRequest(pomArtifact, repositories, "");
        ArtifactResult resolveResult = this.artifactResolver.resolveArtifact(session, resolveRequest);
        pomArtifact = resolveResult.getArtifact();

        // Create model

        return createModel(pomArtifact.getFile(), session);
    }

    private Model createModel(File pomFile, RepositorySystemSession session) throws ModelBuildingException
    {
        // Search for parent pom in all available Aether repositories
        List<RemoteRepository> repositories = newResolutionRepositories(session, true);

        ModelBuildingRequest modelRequest = new DefaultModelBuildingRequest();
        modelRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        modelRequest.setProcessPlugins(false);
        modelRequest.setTwoPhaseBuilding(false);
        modelRequest.setSystemProperties(toProperties(session.getUserProperties(), session.getSystemProperties()));
        modelRequest.setModelResolver(new PublicDefaultModelResolver(session, null, "", this.artifactResolver,
            this.versionRangeResolver, this.remoteRepositoryManager, repositories));
        modelRequest.setPomFile(pomFile);

        return this.modelBuilder.build(modelRequest).getEffectiveModel();
    }

    private Properties toProperties(Map<String, String> dominant, Map<String, String> recessive)
    {
        Properties props = new Properties();
        if (recessive != null) {
            props.putAll(recessive);
        }
        if (dominant != null) {
            props.putAll(dominant);
        }
        return props;
    }

    List<RemoteRepository> newResolutionRepositories(RepositorySystemSession session)
    {
        return newResolutionRepositories(session, false);
    }

    List<RemoteRepository> newResolutionRepositories(RepositorySystemSession session, boolean all)
    {
        List<RemoteRepository> repositories;

        if (all) {
            // Get all maven repositories
            repositories = this.repositoryFactory.getAllMavenRepositories(this.remoteRepository);
        } else {
            repositories = Arrays.asList(this.remoteRepository);
        }

        return this.repositorySystem.newResolutionRepositories(session, repositories);
    }

    RemoteRepository newResolutionRepository(RepositorySystemSession session)
    {
        return newResolutionRepositories(session).get(0);
    }
}
