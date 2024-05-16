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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest.RepositoryMerging;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.ConfigurationProperties;
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
import org.eclipse.aether.spi.connector.ArtifactDownload;
import org.eclipse.aether.spi.connector.RepositoryConnector;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.eclipse.aether.transfer.ArtifactTransferException;
import org.eclipse.aether.transfer.NoRepositoryConnectorException;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionContext;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ExtensionSession;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.maven.internal.DefaultMavenExtensionDependency;
import org.xwiki.extension.maven.internal.MavenExtensionDependency;
import org.xwiki.extension.maven.internal.MavenUtils;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.maven.internal.converter.ExtensionTypeConverter;
import org.xwiki.extension.repository.maven.internal.converter.ModelConverter;
import org.xwiki.extension.repository.maven.internal.handler.MavenArtifactHandler;
import org.xwiki.extension.repository.maven.internal.handler.MavenArtifactHandlerManager;
import org.xwiki.extension.repository.maven.internal.handler.MavenArtifactHandlers;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.extension.version.VersionRange;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.extension.version.internal.VersionUtils;
import org.xwiki.properties.converter.Converter;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class AetherExtensionRepository extends AbstractExtensionRepository
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(AetherExtensionRepository.class);

    protected static class AetherExtensionFileInputStream extends FileInputStream
    {
        private final File file;

        private final boolean delete;

        public AetherExtensionFileInputStream(File file, boolean delete) throws FileNotFoundException
        {
            super(file);

            this.file = file;
            this.delete = delete;
        }

        @Override
        public void close() throws IOException
        {
            super.close();

            if (this.delete && this.file.exists()) {
                Files.delete(this.file.toPath());
            }
        }
    }

    /**
     * Used to parse the version.
     */
    protected static final GenericVersionScheme AETHERVERSIONSCHEME = new GenericVersionScheme();

    private static final String ECONTEXT_SESSION = "maven.systeSession";

    private static final ArtifactType JAR_ARTIFACT_TYPE =
        new DefaultArtifactType(MavenUtils.JAR_EXTENSION, MavenUtils.JAR_EXTENSION, "", "java");

    protected final PlexusContainer plexusContainer;

    protected final ComponentManager componentManager;

    protected RemoteRepository remoteRepository;

    protected ArtifactDescriptorReader mavenDescriptorReader;

    protected VersionRangeResolver versionRangeResolver;

    protected VersionResolver versionResolver;

    protected ModelBuilder modelBuilder;

    protected ArtifactResolver artifactResolver;

    protected RepositorySystem repositorySystem;

    protected RepositoryConnectorProvider repositoryConnectorProvider;

    protected RemoteRepositoryManager remoteRepositoryManager;

    protected Converter<Model> extensionConverter;

    protected AetherExtensionRepositoryFactory repositoryFactory;

    protected ExtensionFactory factory;

    protected ExtensionContext extensionContext;

    protected MavenArtifactHandlerManager artifactHandlerManager;

    protected ExtensionTypeConverter extensionTypeConverter;

    public AetherExtensionRepository(ExtensionRepositoryDescriptor repositoryDescriptor,
        AetherExtensionRepositoryFactory repositoryFactory, RemoteRepository aetherRepository,
        PlexusContainer plexusContainer, ComponentManager componentManager) throws Exception
    {
        super(repositoryDescriptor);

        this.remoteRepository = aetherRepository;

        this.repositoryFactory = repositoryFactory;
        this.plexusContainer = plexusContainer;
        this.componentManager = componentManager;

        this.extensionConverter = componentManager.getInstance(ModelConverter.ROLE);
        this.factory = componentManager.getInstance(ExtensionFactory.class);
        this.extensionContext = componentManager.getInstance(ExtensionContext.class);
        this.artifactHandlerManager = componentManager.getInstance(MavenArtifactHandlerManager.class);
        this.extensionTypeConverter = componentManager.getInstance(ExtensionTypeConverter.class);

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

    protected XWikiRepositorySystemSession pushSession() throws ResolveException
    {
        ExtensionSession extensionSession = this.extensionContext.pushSession();

        XWikiRepositorySystemSession session = extensionSession.get(ECONTEXT_SESSION);

        if (session == null) {
            session = createRepositorySystemSession();
            extensionSession.set(ECONTEXT_SESSION, session);
        }

        return session;
    }

    protected void popSession()
    {
        this.extensionContext.popSession();
    }

    protected XWikiRepositorySystemSession createRepositorySystemSession() throws ResolveException
    {
        XWikiRepositorySystemSession session;
        try {
            session = this.repositoryFactory.createRepositorySystemSession();
        } catch (Exception e) {
            throw new ResolveException("Failed to create the repository system session", e);
        }

        // Add HTTP headers separately from the rest of the configuration of the session as they have to be
        // stored as a map in the session configuration properties.
        Map<String, String> sessionHttpHeaders = new HashMap<>();
        for (Map.Entry<String, String> property : getDescriptor().getProperties().entrySet()) {
            if (StringUtils.startsWith(property.getKey(), "http.headers.")) {
                String headerName = StringUtils.split(property.getKey(), ".", 3)[2];
                sessionHttpHeaders.put(headerName, property.getValue());
            }
        }
        session.addConfigurationProperties(
            Collections.singletonMap(ConfigurationProperties.HTTP_HEADERS, sessionHttpHeaders));

        session.addConfigurationProperties(getDescriptor().getProperties());

        return session;
    }

    protected File createTemporaryFile(String prefix, String suffix) throws IOException
    {
        return this.repositoryFactory.createTemporaryFile(prefix, suffix);
    }

    protected InputStream openStream(Artifact artifact) throws IOException
    {
        XWikiRepositorySystemSession session;
        try {
            session = pushSession();
        } catch (ResolveException e) {
            throw new IOException("Failed to create the repository system session", e);
        }

        File file;
        try {
            file = getFile(artifact, session);
        } finally {
            popSession();
        }

        return new AetherExtensionFileInputStream(file, true);
    }

    private File getFile(Artifact artifact, XWikiRepositorySystemSession session) throws IOException
    {
        List<RemoteRepository> repositories = newResolutionRepositories(session, true);

        File file = createTemporaryFile(artifact.getArtifactId(), artifact.getExtension());

        ArtifactTransferException exception = null;
        for (RemoteRepository repository : repositories) {
            ArtifactDownload download = new ArtifactDownload();
            download.setArtifact(artifact);
            download.setRepositories(repositories);
            download.setFile(file);
            download.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);

            try (RepositoryConnector connector =
                getRepositoryConnectorProvider().newRepositoryConnector(session, repository)) {
                connector.get(Arrays.asList(download), null);
            } catch (NoRepositoryConnectorException e) {
                throw new IOException("Failed to download artifact [" + artifact + "]", e);
            }

            // Check if the download succeeded
            if (download.getException() == null) {
                return file;
            }

            // Remember the first exception (the from where the descriptor was found)
            if (exception == null) {
                exception = download.getException();
            }

            LOGGER.debug("Failed to download file for artifact [{}] from repository [{}]", artifact, repository,
                download.getException());
        }

        throw new IOException("Failed to download file for artifact [" + artifact + "]", exception);
    }

    @Override
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        try {
            if (getDescriptor().getType().equals("maven") && this.mavenDescriptorReader != null) {
                return resolveMaven(extensionId);
            } else {
                // FIXME: impossible to resolve extension type as well as most of the information with pure Aether API
                throw new ResolveException("Unsupported");
            }
        } catch (InvalidExtensionIdException e) {
            // In case the id is invalid behave as if the extension simply did not exist (which is true anyway)
            throw new ExtensionNotFoundException("Invalid extension id", e);
        }
    }

    @Override
    public Extension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        try {
            if (getDescriptor().getType().equals("maven") && this.mavenDescriptorReader != null) {
                return resolveMaven(extensionDependency);
            } else {
                // FIXME: impossible to resolve extension type as well as most of the information with pure Aether API
                throw new ResolveException("Unsupported");
            }
        } catch (InvalidExtensionIdException e) {
            // In case the id is invalid behave as if the extension simply did not exist (which is true anyway)
            throw new ExtensionNotFoundException("Invalid extension id", e);
        }
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        Artifact artifact;
        try {
            artifact = AetherUtils.createArtifact(id, "(,)");
        } catch (InvalidExtensionIdException e) {
            // In case the id is invalid behave as if the extension simply did not exist (which is true anyway)
            throw new ExtensionNotFoundException("Invalid extension id", e);
        }

        List<org.eclipse.aether.version.Version> versions;
        XWikiRepositorySystemSession session = pushSession();
        try {
            versions = resolveVersions(artifact, session);
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve versions for id [" + id + "]", e);
        } finally {
            popSession();
        }

        if (versions.isEmpty()) {
            throw new ExtensionNotFoundException("No versions available for id [" + id + "]");
        }

        if (nb == 0 || offset >= versions.size()) {
            return new CollectionIterableResult<>(versions.size(), offset, Collections.<Version>emptyList());
        }

        int fromId = offset < 0 ? 0 : offset;
        int toId = offset + nb > versions.size() || nb < 0 ? versions.size() : offset + nb;

        List<Version> result = new ArrayList<>(toId - fromId);
        for (int i = fromId; i < toId; ++i) {
            result.add(new DefaultVersion(versions.get(i).toString()));
        }

        return new CollectionIterableResult<>(versions.size(), offset, result);
    }

    private org.eclipse.aether.version.Version resolveVersionConstraint(String id, VersionConstraint versionConstraint,
        RepositorySystemSession session) throws ResolveException
    {
        // Single version
        if (versionConstraint.getVersion() != null) {
            try {
                return AETHERVERSIONSCHEME.parseVersion(versionConstraint.getVersion().getValue());
            } catch (InvalidVersionSpecificationException e) {
                throw new ResolveException("Invalid version [" + versionConstraint.getVersion() + "]", e);
            }
        }

        // Strict version
        Version strictVersion = VersionUtils.getStrictVersion(versionConstraint.getRanges());
        if (strictVersion != null) {
            try {
                return AETHERVERSIONSCHEME.parseVersion(strictVersion.getValue());
            } catch (InvalidVersionSpecificationException e) {
                throw new ResolveException("Invalid version [" + versionConstraint.getVersion() + "]", e);
            }
        }

        // Ranges
        List<org.eclipse.aether.version.Version> commonVersions = null;

        for (VersionRange range : versionConstraint.getRanges()) {
            List<org.eclipse.aether.version.Version> versions = resolveVersionRange(id, range, session);

            if (commonVersions == null) {
                commonVersions = versionConstraint.getRanges().size() > 1 ? new ArrayList<>(versions) : versions;
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
            throw new ExtensionNotFoundException(
                "No versions available for id [" + id + "] and version constraint [" + versionConstraint + "]");
        }

        return commonVersions.get(commonVersions.size() - 1);
    }

    private List<org.eclipse.aether.version.Version> resolveVersionRange(String id, VersionRange versionRange,
        RepositorySystemSession session) throws ResolveException
    {
        Artifact artifact = AetherUtils.createArtifact(id, versionRange.getValue());

        List<org.eclipse.aether.version.Version> versions;
        try {
            versions = resolveVersions(artifact, session);
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve version range", e);
        }

        if (versions.isEmpty()) {
            throw new ExtensionNotFoundException(
                "No versions available for id [" + id + "] and version range [" + versionRange + "]");
        }

        return versions;
    }

    private org.eclipse.aether.version.Version resolveVersionConstraint(Artifact artifact,
        RepositorySystemSession session) throws ResolveException
    {
        try {
            List<org.eclipse.aether.version.Version> versions = resolveVersions(artifact, session);

            if (versions.isEmpty()) {
                throw new ExtensionNotFoundException("No versions available for artifact [" + artifact + "]");
            }

            return versions.get(versions.size() - 1);
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
        String targetMavenType;

        XWikiRepositorySystemSession session = pushSession();
        try {
            if (extensionDependency instanceof AetherExtensionDependency) {
                artifact = ((AetherExtensionDependency) extensionDependency).getAetherDependency().getArtifact();
                targetMavenType = ((AetherExtensionDependency) extensionDependency).getAetherDependency().getArtifact()
                    .getExtension();

                // Find the right version
                if (!extensionDependency.getVersionConstraint().getRanges().isEmpty()) {
                    artifact = artifact.setVersion(resolveVersionConstraint(artifact, session).toString());
                }
            } else {
                artifact = AetherUtils.createArtifact(extensionDependency.getId(),
                    extensionDependency.getVersionConstraint().getValue());
                targetMavenType = DefaultMavenExtensionDependency.getType(extensionDependency);

                // Find the right version
                if (!extensionDependency.getVersionConstraint().getRanges().isEmpty()) {
                    artifact = artifact.setVersion(resolveVersionConstraint(extensionDependency.getId(),
                        extensionDependency.getVersionConstraint(), session).toString());
                }
            }
        } finally {
            popSession();
        }

        return resolveMaven(artifact, targetMavenType);
    }

    private AetherExtension resolveMaven(ExtensionId extensionId) throws ResolveException
    {
        Artifact artifact = AetherUtils.createArtifact(extensionId.getId(), extensionId.getVersion().getValue());

        return resolveMaven(artifact, null);
    }

    private AetherExtension resolveMaven(Artifact artifact, String targetMavenType) throws ResolveException
    {
        XWikiRepositorySystemSession session = pushSession();
        try {
            return resolveMaven(artifact, targetMavenType, session);
        } finally {
            popSession();
        }
    }

    private AetherExtension resolveMaven(Artifact artifact, String targetMavenType, RepositorySystemSession session)
        throws ResolveException
    {
        // Get Maven descriptor

        Artifact pomArtifact;
        try {
            pomArtifact = downloadPom(artifact, session);
        } catch (ArtifactResolutionException e1) {
            if (e1.getResult() != null && !e1.getResult().getExceptions().isEmpty()
                && e1.getResult().getExceptions().get(0) instanceof ArtifactNotFoundException) {
                throw new ExtensionNotFoundException("Could not find artifact [" + artifact + "] descriptor", e1);
            } else {
                throw new ResolveException("Failed to resolve artifact [" + artifact + "] descriptor", e1);
            }
        } catch (Exception e2) {
            throw new ResolveException("Failed to resolve artifact [" + artifact + "] descriptor", e2);
        }

        Model model;
        try {
            model = createModel(pomArtifact.getFile(), session);
        } catch (ModelBuildingException e) {
            throw new ResolveException("Failed to create Maven model", e);
        }

        if (model == null) {
            throw new ResolveException("Failed to resolve artifact [" + artifact + "] descriptor");
        }

        // Relocation
        DistributionManagement distributionManagement = model.getDistributionManagement();
        if (distributionManagement != null) {
            Relocation relocation = distributionManagement.getRelocation();
            if (relocation != null) {
                return resolveMaven(
                    new DefaultArtifact(
                        relocation.getGroupId() != null ? relocation.getGroupId() : artifact.getGroupId(),
                        relocation.getArtifactId() != null ? relocation.getArtifactId() : artifact.getArtifactId(),
                        artifact.getClassifier(), artifact.getExtension(),
                        relocation.getVersion() != null ? relocation.getVersion() : artifact.getVersion()),
                    targetMavenType, session);
            }
        }

        // Get the Maven artifact handler
        MavenArtifactHandler artifactHandler = getMavenArtifactHandler(model, session);

        // Get the Maven artifact type
        ArtifactType artifactType = getArtifactType(targetMavenType, artifactHandler, session);

        // Resolve the classifier
        String artifactClassifier = artifact.getClassifier();
        if (StringUtils.isEmpty(artifactClassifier)) {
            artifactClassifier = artifactType.getClassifier();
        }

        // Resolve the Maven file extension
        String artifactExtension = artifactType.getExtension();
        Artifact fileArtifact = new DefaultArtifact(pomArtifact.getGroupId(), pomArtifact.getArtifactId(),
            artifact.getClassifier(), artifactExtension, pomArtifact.getVersion());

        // When the file extension is different from the default one in needs to be included in the extension id
        boolean includeExtension = false;

        // Resolve the extension type
        String extensionType;
        try {
            // Is the requested type equals to the artifact type or extension
            if (isTargetType(targetMavenType, artifactHandler)) {
                // When the requested type is the default one, try to find a ArtifactTypeHandler associated with the
                // packaging
                extensionType = this.extensionTypeConverter.toExtensionType(artifactHandler);
            } else {
                // Otherwise relies on the target type extension
                extensionType = this.extensionTypeConverter.mavenExtensionToExtensionType(artifactExtension);

                // Since it's not the default, we also need to include the type in the extension
                includeExtension = true;
            }
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve the extension type", e);
        }

        // Convert the pom Model into an Extension
        Extension mavenExtension = this.extensionConverter.convert(Extension.class, model);

        // Resolve the extension id
        // TODO: put back special extension id generation when finishing to implement
        // https://jira.xwiki.org/browse/XCOMMONS-3028, currently it's causing more harm than good
        // String extensionIdString = MavenUtils.toXWikiExtensionIdentifier(artifact.getGroupId(),
        // artifact.getArtifactId(), artifactClassifier, includeExtension ? artifact.getExtension() : null);
        String extensionIdString =
            MavenUtils.toXWikiExtensionIdentifier(artifact.getGroupId(), artifact.getArtifactId(), "", null);
        ExtensionId extensionId =
            new ExtensionId(extensionIdString, this.factory.getVersion(artifact.getBaseVersion()));

        // Create the final extension
        AetherExtension extension = new AetherExtension(extensionId, extensionType, mavenExtension, fileArtifact, this);

        // Convert Maven dependencies to Aether dependencies
        extension.setDependencies(toAetherDependencies(mavenExtension.getDependencies(), session));

        // Convert Managed Maven dependencies to Aether dependencies
        extension.setManagedDependencies(toAetherDependencies(mavenExtension.getManagedDependencies(), session));

        return extension;
    }

    private boolean isTargetType(String targetMavenType, MavenArtifactHandler artifactHandler)
    {
        return targetMavenType == null || artifactHandler.getType().equals(targetMavenType)
            || artifactHandler.getExtension().equals(targetMavenType);
    }

    private MavenArtifactHandler getMavenArtifactHandler(Model model, RepositorySystemSession session)
    {
        MavenArtifactHandlers handlers = XWikiRepositorySystemSession.getArtifactHandlers(session);

        MavenArtifactHandler handler = handlers.getByPackaging(model.getPackaging());

        if (handler != null) {
            return handler;
        }

        // TODO: No mapping found for the provided packaging, search extensions
        // See https://jira.xwiki.org/browse/XCOMMONS-3027

        // Still no mapping found, use a default handler where every is equals to the packaging
        return new MavenArtifactHandler(model.getPackaging(), new DefaultArtifactHandler(model.getPackaging()));
    }
    
    private ArtifactType getArtifactType(String targetMavenType, MavenArtifactHandler artifactHandler,
        RepositorySystemSession session)
    {
        if (targetMavenType != null) {
            // Search the type registry
            ArtifactType artifactType = session.getArtifactTypeRegistry().get(targetMavenType);
            if (artifactType != null) {
                return artifactType;
            }

            // Return a base type
            return new DefaultArtifactType(targetMavenType);
        }

        // Fallback on the artifact handler
        return artifactHandler.getArtifactType();
    }

    private List<ExtensionDependency> toAetherDependencies(Collection<ExtensionDependency> mavenDependencies,
        RepositorySystemSession session) throws ResolveException
    {
        List<ExtensionDependency> dependencies = new ArrayList<>(mavenDependencies.size());

        try {
            ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();

            for (ExtensionDependency mavenDependency : mavenDependencies) {
                dependencies.add(new AetherExtensionDependency(mavenDependency,
                    convertToAether(((MavenExtensionDependency) mavenDependency).getMavenDependency(), stereotypes),
                    this.getDescriptor()));
            }
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve dependencies", e);
        }

        return dependencies;
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

        Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
            dependency.getClassifier(), null, dependency.getVersion(), props, stereotype);

        List<Exclusion> exclusions = new ArrayList<>(dependency.getExclusions().size());
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

    private Artifact downloadPom(Artifact artifact, RepositorySystemSession session)
        throws VersionResolutionException, ArtifactResolutionException
    {
        List<RemoteRepository> repositories = newResolutionRepositories(session);

        Artifact pomArtifact = resolveVersion(artifact, repositories, session);

        // Download pom file

        ArtifactRequest resolveRequest = new ArtifactRequest(pomArtifact, repositories, "");
        ArtifactResult resolveResult = this.artifactResolver.resolveArtifact(session, resolveRequest);
        pomArtifact = resolveResult.getArtifact();

        return pomArtifact;
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
        modelRequest.setModelResolver(new ProjectModelResolver(session, null, this.repositorySystem,
            this.remoteRepositoryManager, repositories, RepositoryMerging.POM_DOMINANT, null));
        modelRequest.setPomFile(pomFile);
        modelRequest.setActiveProfileIds(Arrays.asList("legacy"));

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

    protected List<RemoteRepository> newResolutionRepositories(RepositorySystemSession session)
    {
        return newResolutionRepositories(session, false);
    }

    protected List<RemoteRepository> newResolutionRepositories(RepositorySystemSession session, boolean all)
    {
        List<RemoteRepository> repositories;

        if (all) {
            // Get all maven repositories
            repositories = getAllMavenRepositories();
        } else {
            repositories = Arrays.asList(this.remoteRepository);
        }

        return this.repositorySystem.newResolutionRepositories(session, repositories);
    }

    protected List<RemoteRepository> getAllMavenRepositories()
    {
        Collection<ExtensionRepository> extensionRepositories;
        try {
            extensionRepositories = this.componentManager
                .<ExtensionRepositoryManager>getInstance(ExtensionRepositoryManager.class).getRepositories();
        } catch (ComponentLookupException e) {
            return Collections.singletonList(this.remoteRepository);
        }

        List<RemoteRepository> reposirories = new ArrayList<>(extensionRepositories.size());

        // Put first repository
        reposirories.add(this.remoteRepository);

        // Add other repositories (and filter first one)
        for (ExtensionRepository extensionRepository : extensionRepositories) {
            if (extensionRepository instanceof AetherExtensionRepository) {
                RemoteRepository repository = ((AetherExtensionRepository) extensionRepository).getRemoteRepository();

                if (this.remoteRepository != repository) {
                    reposirories.add(repository);
                }
            }
        }

        return reposirories;
    }

    protected RemoteRepository newResolutionRepository(RepositorySystemSession session)
    {
        return newResolutionRepositories(session).get(0);
    }
}
