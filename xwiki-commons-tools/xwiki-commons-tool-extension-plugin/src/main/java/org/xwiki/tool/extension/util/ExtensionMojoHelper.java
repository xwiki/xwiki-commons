package org.xwiki.tool.extension.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.MutableExtension;
import org.xwiki.extension.internal.ExtensionUtils;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.internal.DependenciesJob;
import org.xwiki.extension.job.internal.InstallJob;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.maven.ArtifactModel;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepositoryException;
import org.xwiki.extension.repository.internal.ExtensionSerializer;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.job.Job;
import org.xwiki.properties.converter.Converter;
import org.xwiki.tool.extension.ComponentRepresentation;
import org.xwiki.tool.extension.ExtensionOverride;
import org.xwiki.tool.extension.internal.ExtensionMojoCoreExtensionRepository;
import org.xwiki.tool.extension.internal.MavenBuildConfigurationSource;
import org.xwiki.tool.extension.internal.MavenBuildExtensionRepository;

@Component(roles = ExtensionMojoHelper.class)
@Singleton
public class ExtensionMojoHelper implements AutoCloseable
{
    private MavenProject project;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private ExtensionSerializer extensionSerializer;

    @Inject
    private CoreExtensionRepository coreExtensionRepository;

    @Inject
    private ExtensionRepositoryManager repositories;

    @Inject
    private Converter<ArtifactModel> extensionConverter;

    @Inject
    @Named(DependenciesJob.JOBTYPE)
    private Provider<Job> dependeciesJobProvider;

    @Inject
    @Named(InstallJob.JOBTYPE)
    private Provider<Job> installJobProvider;

    @Inject
    private Execution execution;

    @Inject
    private ConfigurationSource configurationSource;

    // Not injected so that it's not initialized if not needed
    private InstalledExtensionRepository installedExtensionRepository;

    // Not injected so that it's not initialized if not needed
    private LocalExtensionRepository localExtensionRepository;

    private MavenSession session;

    private ArtifactRepository localRepository;

    private PlexusContainer plexusContainer;

    private ProjectBuilder projectBuilder;

    private RepositorySystem repositorySystem;

    // Maven components

    private List<ExtensionOverride> extensionOverrides;

    private MavenBuildExtensionRepository extensionRepository;

    private File permanentDirectory;

    public static ExtensionMojoHelper create(MavenProject project, File permanentDirectory)
        throws MojoExecutionException
    {
        if (permanentDirectory == null) {
            permanentDirectory = new File(project.getBuild().getDirectory(), "data/");
        }

        // Create and initialize a Component Manager
        EmbeddableComponentManager embeddableComponentManager =
            (EmbeddableComponentManager) org.xwiki.environment.System.initialize(permanentDirectory);

        // Disable default repositories
        try {
            MavenBuildConfigurationSource configuration =
                embeddableComponentManager.getInstance(ConfigurationSource.class);
            configuration.setProperty("extension.repositories", Collections.singletonList(""));
        } catch (ComponentLookupException e) {
            throw new MojoExecutionException("Failed to lookup configuration component", e);
        }

        // Initialize Execution Context
        try {
            ExecutionContextManager ecim = embeddableComponentManager.getInstance(ExecutionContextManager.class);
            ecim.initialize(new ExecutionContext());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to initialize Execution Context Manager.", e);
        }

        // Lookup ExtensionMojoHelper
        ExtensionMojoHelper extensionMojoHelper;
        try {
            extensionMojoHelper = embeddableComponentManager.getInstance(ExtensionMojoHelper.class);
        } catch (ComponentLookupException e) {
            throw new MojoExecutionException("Failed to get ExtensionMojoHelper component", e);
        }
        extensionMojoHelper.project = project;
        extensionMojoHelper.permanentDirectory = permanentDirectory;

        return extensionMojoHelper;
    }

    /**
     * Public for technical reason, {@link #create(MavenProject, File)} should be used instead.
     */
    public ExtensionMojoHelper()
    {

    }

    /**
     * Allow to unregister the components given in the list.
     *
     * @param componentList the list of components to unregister.
     * @throws MojoExecutionException in case of error when deserializing the component types.
     * @since 12.2
     */
    public void disableComponents(List<ComponentRepresentation> componentList) throws MojoExecutionException
    {
        if (componentList != null) {
            for (ComponentRepresentation componentRepresentation : componentList) {
                try {
                    Type type =
                        ReflectionUtils.unserializeType(componentRepresentation.getType(), getClass().getClassLoader());

                    this.componentManager.unregisterComponent(type, componentRepresentation.getRole());
                } catch (ClassNotFoundException e) {
                    throw new MojoExecutionException(
                        String.format("Cannot unserialize type [%s].", componentRepresentation.getType()), e);
                }
            }
        }
    }

    public void initalize(MavenSession session, ArtifactRepository localRepository, PlexusContainer plexusContainer)
        throws MojoExecutionException
    {
        this.session = session;
        this.localRepository = localRepository;

        this.plexusContainer = plexusContainer;

        try {
            initializeComponents();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to initialize components", e);
        }
    }

    private void initializeComponents() throws Exception
    {
        this.projectBuilder = this.plexusContainer.lookup(ProjectBuilder.class);
        this.repositorySystem = this.plexusContainer.lookup(RepositorySystem.class);

        this.extensionRepository =
            new MavenBuildExtensionRepository(session, localRepository, plexusContainer, this.componentManager);
        this.repositories.addRepository(this.extensionRepository);
    }

    public void setExtensionOverrides(List<ExtensionOverride> extensionOverrides)
    {
        this.extensionOverrides = extensionOverrides;
    }

    private void disposeComponents()
    {
        org.xwiki.environment.System.dispose(this.componentManager);
    }

    public ComponentManager getComponentManager()
    {
        return this.componentManager;
    }

    @Override
    public void close()
    {
        this.execution.removeContext();

        disposeComponents();
    }

    public File getPermanentDirectory()
    {
        return this.permanentDirectory;
    }

    public LocalExtensionRepository getLocalExtensionRepository() throws MojoExecutionException
    {
        if (this.localExtensionRepository == null) {
            try {
                this.localExtensionRepository = this.componentManager.getInstance(LocalExtensionRepository.class);
            } catch (ComponentLookupException e) {
                throw new MojoExecutionException("Failed to get LocalExtensionRepository component", e);
            }

        }

        return this.localExtensionRepository;
    }

    public InstalledExtensionRepository getInstalledExtensionRepository() throws MojoExecutionException
    {
        if (this.installedExtensionRepository == null) {
            try {
                this.installedExtensionRepository =
                    this.componentManager.getInstance(InstalledExtensionRepository.class);
            } catch (ComponentLookupException e) {
                throw new MojoExecutionException("Failed to get InstalledExtensionRepository component", e);
            }
        }

        return this.installedExtensionRepository;
    }

    public ExtensionMojoCoreExtensionRepository getExtensionMojoCoreExtensionRepository()
    {
        return (ExtensionMojoCoreExtensionRepository) this.coreExtensionRepository;
    }

    public MavenProject getMavenProject(Artifact artifact) throws MojoExecutionException
    {
        try {
            ProjectBuildingRequest request = new DefaultProjectBuildingRequest(this.session.getProjectBuildingRequest())
                // We don't want to execute any plugin here
                .setProcessPlugins(false)
                // The local repository
                .setLocalRepository(this.localRepository)
                // It's not this plugin job to validate this pom.xml
                .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
                // Use the repositories configured for the built project instead of the default Maven ones
                .setRemoteRepositories(this.session.getCurrentProject().getRemoteArtifactRepositories());
            // Note: build() will automatically get the POM artifact corresponding to the passed artifact.
            ProjectBuildingResult result = this.projectBuilder.build(artifact, request);
            return result.getProject();
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException(String.format("Failed to build project for [%s]", artifact), e);
        }
    }

    public Extension getExtension(Artifact artifact) throws MojoExecutionException
    {
        MavenProject project = getMavenProject(artifact);

        return toExtension(toArtifactModel(artifact, project.getModel()));
    }

    public Extension toExtension(ArtifactModel model)
    {
        Extension extension = this.extensionConverter.convert(Extension.class, model);

        MutableExtension mutableExtension;

        if (!(extension instanceof MutableExtension)) {
            mutableExtension = new DefaultLocalExtension(null, extension);
        } else {
            mutableExtension = (MutableExtension) extension;
        }

        // Apply overrides
        override(mutableExtension);

        return extension;
    }

    public LocalExtension storeExtension(Artifact artifact) throws MojoExecutionException
    {
        Extension extension = getExtension(artifact);

        return storeExtension(extension);
    }

    public LocalExtension storeExtension(Extension extension) throws MojoExecutionException
    {
        LocalExtension localExtension = getLocalExtensionRepository().getLocalExtension(extension.getId());

        if (localExtension == null) {
            try {
                localExtension = getLocalExtensionRepository().storeExtension(extension);
            } catch (LocalExtensionRepositoryException e) {
                throw new MojoExecutionException("Failed to stored extension", e);
            }
        }

        return localExtension;
    }

    public void storeExtensionDependencies() throws MojoExecutionException
    {
        storeExtensionDependencies(false);
    }

    public void storeExtensionDependencies(boolean isolate) throws MojoExecutionException
    {
        // Resolve dependencies
        List<ExtensionPlan> plans = resolveDependencies(this.project, isolate);

        // Store dependencies
        for (ExtensionPlan plan : plans) {
            for (ExtensionPlanAction action : plan.getActions()) {
                if (action.getAction() == Action.INSTALL) {
                    storeExtension(action.getExtension());
                } else if (action.getAction() != Action.NONE) {
                    throw new MojoExecutionException("Unexpected action [" + action
                        + "] when storing dependencies of project [" + this.project + "]");
                }
            }
        }
    }

    private ExtensionPlan resolve(InstallRequest installRequest)
    {
        // Minimum job log
        installRequest.setVerbose(false);

        Job installPlanJob = this.dependeciesJobProvider.get();

        installPlanJob.initialize(installRequest);
        installPlanJob.run();

        return (ExtensionPlan) installPlanJob.getStatus();
    }

    public List<ExtensionPlan> resolveDependencies(MavenProject project, boolean isolate) throws MojoExecutionException
    {
        if (isolate) {
            List<ExtensionPlan> plans = new ArrayList<>();

            for (org.apache.maven.model.Dependency dependency : project.getDependencies()) {
                InstallRequest installRequest = new InstallRequest();

                // TODO: Add support for version range or ExtensionDependency in InstallRequest
                installRequest.addExtension(new ExtensionId(dependency.getGroupId() + ':' + dependency.getArtifactId(),
                    dependency.getVersion()));

                ExtensionPlan status = resolve(installRequest);
                // Deal with errors
                if (status.getError() != null) {
                    throw new MojoExecutionException(
                        "Failed to resolve dependencies for project [" + project + "] dependencies", status.getError());
                }

                plans.add(status);
            }

            return plans;
        } else {
            return Collections.singletonList(resolveDependencies(project));
        }
    }

    public ExtensionPlan resolveDependencies(MavenProject project) throws MojoExecutionException
    {
        InstallRequest installRequest = new InstallRequest();

        for (org.apache.maven.model.Dependency dependency : project.getDependencies()) {
            // TODO: Add support for version range or ExtensionDependency in InstallRequest
            installRequest.addExtension(
                new ExtensionId(dependency.getGroupId() + ':' + dependency.getArtifactId(), dependency.getVersion()));
        }

        ExtensionPlan status = resolve(installRequest);
        // Deal with errors
        if (status.getError() != null) {
            throw new MojoExecutionException(
                "Failed to resolve dependencies for project [" + project + "] dependencies", status.getError());
        }

        return status;
    }

    public InstalledExtension registerInstalledExtension(Artifact artifact, String namespace, boolean dependency,
        Map<String, Object> properties) throws MojoExecutionException
    {
        LocalExtension localExtension = storeExtension(artifact);

        try {
            return getInstalledExtensionRepository().installExtension(localExtension, namespace, dependency,
                properties);
        } catch (InstallException e) {
            throw new MojoExecutionException("Failed to install extension", e);
        }
    }

    public Job install(Collection<ExtensionArtifact> artifacts, String namespace, Map<String, Object> properties)
        throws MojoExecutionException
    {
        InstallRequest installRequest = new InstallRequest();

        return install(artifacts, installRequest, namespace, properties);
    }

    public Job install(Collection<ExtensionArtifact> artifacts, InstallRequest installRequest, String namespace,
        Map<String, Object> properties) throws MojoExecutionException
    {
        for (ExtensionArtifact artifact : artifacts) {
            installRequest.addExtension(
                new ExtensionId(artifact.getGroupId() + ':' + artifact.getArtifactId(), artifact.getVersion()));
        }

        return install(installRequest, namespace, properties);
    }

    public Job install(Artifact artifact, String namespace, Map<String, Object> properties)
        throws MojoExecutionException
    {
        InstallRequest installRequest = new InstallRequest();

        installRequest.addExtension(
            new ExtensionId(artifact.getGroupId() + ':' + artifact.getArtifactId(), artifact.getVersion()));

        return install(installRequest, namespace, properties);
    }

    public Job install(InstallRequest installRequest, String namespace, Map<String, Object> properties)
        throws MojoExecutionException
    {
        installRequest.addNamespace(namespace);
        if (properties != null) {
            installRequest.addExtensionProperties(properties);
        }

        // Minimum job log
        installRequest.setVerbose(false);

        Job installJob = this.installJobProvider.get();
        installJob.initialize(installRequest);
        installJob.run();

        // Error report
        if (installJob.getStatus().getError() != null) {
            throw new MojoExecutionException("Failed to install extension(s)", installJob.getStatus().getError());
        }

        return installJob;
    }

    public void serializeExtension(File path, Artifact artifact)
        throws MojoExecutionException, IOException, ParserConfigurationException, TransformerException
    {
        // Get MavenProject instance
        MavenProject mavenProject = getMavenProject(artifact);

        serializeExtension(path, artifact, mavenProject.getModel());
    }

    public void serializeExtension(File path, Artifact artifact, Model model)
        throws IOException, ParserConfigurationException, TransformerException
    {
        serializeExtension(path, toArtifactModel(artifact, model));
    }

    public ArtifactModel toArtifactModel(Artifact artifact, Model model)
    {
        ArtifactModel artifactModel = new ArtifactModel(model);
        artifactModel.setClassifier(artifact.getClassifier());
        artifactModel.setType(artifact.getType());

        return artifactModel;
    }

    public void serializeExtension(File path, ArtifactModel artifactModel)
        throws IOException, ParserConfigurationException, TransformerException
    {
        // Get Extension instance
        Extension mavenExtension = toExtension(artifactModel);

        if (!path.exists()) {
            // Save the Extension descriptor
            try (FileOutputStream stream = new FileOutputStream(path)) {
                this.extensionSerializer.saveExtensionDescriptor(mavenExtension, stream);
            }
        }
    }

    public void override(MutableExtension extension)
    {
        if (this.extensionOverrides != null) {
            for (ExtensionOverride extensionOverride : this.extensionOverrides) {
                String id = extensionOverride.get(Extension.FIELD_ID);
                if (extension.getId().getId().equals(id)) {
                    String version = extensionOverride.get(Extension.FIELD_VERSION);
                    if (version == null || extension.getId().getVersion().equals(new DefaultVersion(id))) {
                        // Override features
                        String featuresString = extensionOverride.get(Extension.FIELD_FEATURES);
                        if (featuresString != null) {
                            Collection<String> features = ExtensionUtils.importPropertyStringList(featuresString, true);
                            extension.setExtensionFeatures(
                                ExtensionIdConverter.toExtensionIdList(features, extension.getId().getVersion()));
                        }
                        // Override properties
                        String propertiesString = extensionOverride.get(Extension.FIELD_PROPERTIES);
                        if (propertiesString != null) {
                            Properties properties = new Properties();
                            try {
                                properties.load(new StringReader(propertiesString));
                            } catch (IOException e) {
                                // Does not make sense with a StringReader
                            }
                            properties.forEach((key, value) -> extension.putProperty((String) key, value));
                        }
                    }
                }
            }
        }
    }

    public void serializeExtension(Artifact artifact, File directory) throws MojoExecutionException
    {
        // Get path
        // WAR plugin use based version for the name of the actual file stored in the package
        StringBuilder builder = new StringBuilder();
        builder.append(artifact.getArtifactId());
        builder.append('-');
        builder.append(artifact.getBaseVersion());
        if (artifact.getClassifier() != null) {
            builder.append('-');
            builder.append(artifact.getClassifier());
        }
        builder.append(".xed");
        File path = new File(directory, builder.toString());

        try {
            serializeExtension(path, artifact);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to write descriptor for artifact [" + artifact + "]", e);
        }
    }

    public void serializeExtensions(Collection<Artifact> artifacts, File directory, String type)
        throws MojoExecutionException
    {
        // Register dependencies
        for (Artifact artifact : artifacts) {
            if (!artifact.isOptional()) {
                if (type == null || type.equals(artifact.getType())) {
                    serializeExtension(artifact, directory);
                }
            }
        }
    }

    public MavenBuildConfigurationSource getMavenBuildConfigurationSource()
    {
        return (MavenBuildConfigurationSource) this.configurationSource;
    }

    public Set<Artifact> collectMavenArtifacts(List<ExtensionArtifact> input) throws MojoExecutionException
    {
        if (input != null) {
            CollectRequest request = new CollectRequest();

            request.setRepositories(this.project.getRemoteProjectRepositories());

            RepositorySystemSession repositorySession = this.session.getRepositorySession();
            ArtifactTypeRegistry typeRegistry = repositorySession.getArtifactTypeRegistry();

            DependencyManagement dependencyManagement = this.project.getDependencyManagement();
            if (dependencyManagement != null) {
                for (org.apache.maven.model.Dependency dependency : dependencyManagement.getDependencies()) {
                    request.addManagedDependency(RepositoryUtils.toDependency(dependency, typeRegistry));
                }
            }

            for (ExtensionArtifact extensionArtifact : input) {
                request.addDependency(new Dependency(new DefaultArtifact(extensionArtifact.getGroupId(),
                    extensionArtifact.getArtifactId(), extensionArtifact.getType(), extensionArtifact.getVersion()),
                    null));
            }

            CollectResult collectResult;
            try {
                collectResult = this.repositorySystem.collectDependencies(repositorySession, request);
            } catch (DependencyCollectionException e) {
                throw new MojoExecutionException("Failed to resolve artifacts", e);
            }

            if (!collectResult.getExceptions().isEmpty()) {
                throw new MojoExecutionException(String.format("Failed to resolve artifacts [%s]", input),
                    collectResult.getExceptions().get(0));
            }

            Set<Artifact> artifacts = new HashSet<>();

            addNodes(collectResult.getRoot().getChildren(), artifacts);

            return artifacts;
        }

        return null;
    }

    private void addNode(DependencyNode node, Collection<Artifact> artifacts)
    {
        // TODO: find out why we end up with "system" scope dependency (seems to be specific to jdk.tools:jdk.tools)
        if (!node.getDependency().getScope().equals("system")) {
            artifacts.add(RepositoryUtils.toArtifact(node.getArtifact()));

            addNodes(node.getChildren(), artifacts);
        }
    }

    private void addNodes(List<DependencyNode> nodes, Collection<Artifact> artifacts)
    {
        nodes.forEach(c -> addNode(c, artifacts));
    }
}
