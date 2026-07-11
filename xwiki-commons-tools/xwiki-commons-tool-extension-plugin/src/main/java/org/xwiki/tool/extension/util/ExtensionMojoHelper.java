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
import org.xwiki.extension.InstallOnRootNamespaceExtensionRewriter;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.MutableExtension;
import org.xwiki.extension.internal.ExtensionUtils;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;
import org.xwiki.extension.jar.internal.handler.JarExtensionHandler;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.internal.DependenciesJob;
import org.xwiki.extension.job.internal.InstallJob;
import org.xwiki.extension.job.internal.InstallPlanJob;
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
import org.xwiki.job.Job;
import org.xwiki.properties.converter.Converter;
import org.xwiki.tool.extension.ComponentRepresentation;
import org.xwiki.tool.extension.ExtensionOverride;
import org.xwiki.tool.extension.internal.ExtensionMojoCoreExtensionRepository;
import org.xwiki.tool.extension.internal.MavenBuildConfigurationSource;
import org.xwiki.tool.extension.internal.MavenBuildExtensionRepository;

/**
 * Various helpers used by the XWiki Maven plugins that need to manipulate XWiki extensions: resolving them from a
 * Maven project or artifact, converting them to the extension model, storing them in the local extension repository,
 * computing install plans, installing them and serializing their descriptors.
 *
 * @version $Id$
 */
@Component(roles = ExtensionMojoHelper.class)
@Singleton
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class ExtensionMojoHelper implements AutoCloseable
{
    private static final String FAILED_RESOLVE_DEPENDENCIES =
        "Failed to resolve dependencies for project [%s] dependencies";

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
    @Named(InstallPlanJob.JOBTYPE)
    private Provider<Job> installPlanJobProvider;

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

    /**
     * Public for technical reasons; {@link #create(MavenProject, File)} should be used instead to obtain a properly
     * initialized instance.
     */
    public ExtensionMojoHelper()
    {
    }

    /**
     * Creates and initializes a new helper backed by a fresh embedded XWiki component manager and execution context,
     * with the default extension repositories disabled.
     *
     * @param project the Maven project for which extensions are manipulated
     * @param permanentDirectory the directory used as the permanent directory of the embedded XWiki environment (where
     *  the local and installed extension repositories are stored); when {@code null} the {@code data/} sub-directory
     *  of the project build directory is used
     * @return the initialized helper
     * @throws MojoExecutionException if the configuration source, the execution context or the helper component cannot
     *  be initialized
     */
    public static ExtensionMojoHelper create(MavenProject project, File permanentDirectory)
        throws MojoExecutionException
    {
        File directory = permanentDirectory;
        if (directory == null) {
            directory = new File(project.getBuild().getDirectory(), "data/");
        }

        // Create and initialize a Component Manager
        EmbeddableComponentManager embeddableComponentManager =
            (EmbeddableComponentManager) org.xwiki.environment.System.initialize(directory);

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
        extensionMojoHelper.permanentDirectory = directory;

        return extensionMojoHelper;
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

    /**
     * Initializes the Maven related state and the extension repository backed by the build, so that artifacts can be
     * resolved.
     *
     * @param session the current Maven session
     * @param localRepository the Maven local repository used to resolve artifacts
     * @param plexusContainer the Plexus container used to look up Maven components
     * @throws MojoExecutionException if the underlying components cannot be initialized
     */
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
            new MavenBuildExtensionRepository(this.session, this.plexusContainer, this.componentManager);
        this.repositories.addRepository(this.extensionRepository);
    }

    /**
     * Sets the overrides applied to the metadata of the extensions produced by this helper, for example to force a
     * version, features or custom properties.
     *
     * @param extensionOverrides the overrides to apply
     */
    public void setExtensionOverrides(List<ExtensionOverride> extensionOverrides)
    {
        this.extensionOverrides = extensionOverrides;
    }

    private void disposeComponents()
    {
        org.xwiki.environment.System.dispose(this.componentManager);
    }

    /**
     * {@return the component manager backing this helper}
     */
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

    /**
     * {@return the permanent directory of the embedded XWiki environment used by this helper}
     */
    public File getPermanentDirectory()
    {
        return this.permanentDirectory;
    }

    /**
     * @return the local extension repository, looked up and cached on first access
     * @throws MojoExecutionException if the local extension repository component cannot be looked up
     */
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

    /**
     * @return the installed extension repository, looked up and cached on first access
     * @throws MojoExecutionException if the installed extension repository component cannot be looked up
     */
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

    /**
     * {@return the core extension repository used to expose the build artifacts as core extensions}
     */
    public ExtensionMojoCoreExtensionRepository getExtensionMojoCoreExtensionRepository()
    {
        return (ExtensionMojoCoreExtensionRepository) this.coreExtensionRepository;
    }

    /**
     * Builds the {@link MavenProject} corresponding to the passed artifact, without executing any plugin and using the
     * remote repositories configured for the currently built project.
     *
     * @param artifact the artifact whose project (POM) is built
     * @return the resolved Maven project
     * @throws MojoExecutionException if the project cannot be built
     */
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

    /**
     * Resolves the passed artifact as an XWiki extension, with the configured overrides applied.
     *
     * @param artifact the artifact to convert into an extension
     * @return the extension corresponding to the artifact
     * @throws MojoExecutionException if the artifact's project cannot be built
     */
    public Extension getExtension(Artifact artifact) throws MojoExecutionException
    {
        MavenProject mavenProject = getMavenProject(artifact);

        return toExtension(toArtifactModel(artifact, mavenProject.getModel()));
    }

    /**
     * Converts the passed artifact and its Maven model into an XWiki extension, with the configured overrides applied.
     *
     * @param artifact the artifact providing the classifier and type of the extension
     * @param model the Maven model providing the extension metadata
     * @return the extension, with overrides applied
     * @since 17.9.0RC1
     * @since 17.4.6
     * @since 16.10.13
     */
    public Extension toExtension(Artifact artifact, Model model)
    {
        return toExtension(toArtifactModel(artifact, model));
    }

    /**
     * Converts the passed artifact model into an XWiki extension, with the configured overrides applied.
     *
     * @param model the artifact model to convert
     * @return the extension, with overrides applied
     */
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

    /**
     * Resolves the passed artifact as an extension and stores it in the local extension repository.
     *
     * @param artifact the artifact to resolve and store
     * @return the stored local extension
     * @throws MojoExecutionException if the artifact cannot be resolved or stored
     */
    public LocalExtension storeExtension(Artifact artifact) throws MojoExecutionException
    {
        Extension extension = getExtension(artifact);

        return storeExtension(extension);
    }

    /**
     * Stores the passed extension in the local extension repository, unless it is already stored there.
     *
     * @param extension the extension to store
     * @return the matching local extension, either the already stored one or the newly stored one
     * @throws MojoExecutionException if the extension cannot be stored
     */
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

    /**
     * Resolves the dependencies of the current project and stores them in the local extension repository, resolving
     * all the dependencies together.
     *
     * @throws MojoExecutionException if the dependencies cannot be resolved or stored
     */
    public void storeExtensionDependencies() throws MojoExecutionException
    {
        storeExtensionDependencies(false);
    }

    /**
     * Resolves the dependencies of the current project and stores them in the local extension repository.
     *
     * @param isolate when {@code true} each dependency is resolved independently from the others, otherwise all the
     *  dependencies are resolved together
     * @throws MojoExecutionException if the dependencies cannot be resolved or stored, or if the resulting plan
     *  contains an unexpected action
     */
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
                    throw new MojoExecutionException("Unexpected action [%s] when storing dependencies of project [%s]"
                        .formatted(action, this.project));
                }
            }
        }
    }

    private ExtensionPlan createInstallPlan(InstallRequest installRequest, boolean dependenciesOnly)
    {
        // Minimum job log
        installRequest.setVerbose(false);

        Job installPlanJob = dependenciesOnly ? this.dependeciesJobProvider.get() : this.installPlanJobProvider.get();

        installPlanJob.initialize(installRequest);
        installPlanJob.run();

        return (ExtensionPlan) installPlanJob.getStatus();
    }

    /**
     * Computes the install plan for the extension corresponding to the passed model on the given namespace.
     *
     * @param model the artifact model to compute the install plan for
     * @param namespace the namespace on which to compute the install plan (for example {@code wiki:xwiki})
     * @return the computed install plan
     * @throws MojoExecutionException if the install plan cannot be computed
     */
    public ExtensionPlan createInstallPlan(ArtifactModel model, String namespace) throws MojoExecutionException
    {
        Extension extension = toExtension(model);

        InstallRequest installRequest = new InstallRequest();

        installRequest.addNamespace(namespace);
        installRequest.addExtension(extension);

        ExtensionPlan status = createInstallPlan(installRequest, false);
        // Deal with errors
        if (status.getError() != null) {
            throw new MojoExecutionException(
                "Failed to create an install plan for Maven model [%s]".formatted(model.getModel()), status.getError());
        }

        return status;
    }

    /**
     * Computes the install plans for the dependencies of the passed project.
     *
     * @param project the project whose dependencies are resolved
     * @param isolate when {@code true} a separate plan is computed for each dependency, otherwise a single plan is
     *  computed for all of them
     * @return the list of computed install plans
     * @throws MojoExecutionException if a plan cannot be computed
     */
    public List<ExtensionPlan> resolveDependencies(MavenProject project, boolean isolate) throws MojoExecutionException
    {
        if (isolate) {
            List<ExtensionPlan> plans = new ArrayList<>();

            for (org.apache.maven.model.Dependency dependency : project.getDependencies()) {
                InstallRequest installRequest = new InstallRequest();

                // TODO: Add support for version range or ExtensionDependency in InstallRequest
                installRequest.addExtension(new ExtensionId(dependency.getGroupId() + ':' + dependency.getArtifactId(),
                    dependency.getVersion()));

                ExtensionPlan status = createInstallPlan(installRequest, true);
                // Deal with errors
                if (status.getError() != null) {
                    throw new MojoExecutionException(
                        FAILED_RESOLVE_DEPENDENCIES.formatted(project), status.getError());
                }

                plans.add(status);
            }

            return plans;
        } else {
            return Collections.singletonList(resolveDependencies(project));
        }
    }

    /**
     * Computes a single install plan for all the dependencies of the passed project.
     *
     * @param project the project whose dependencies are resolved
     * @return the computed install plan
     * @throws MojoExecutionException if the plan cannot be computed
     */
    public ExtensionPlan resolveDependencies(MavenProject project) throws MojoExecutionException
    {
        InstallRequest installRequest = new InstallRequest();

        for (org.apache.maven.model.Dependency dependency : project.getDependencies()) {
            // TODO: Add support for version range or ExtensionDependency in InstallRequest
            installRequest.addExtension(
                new ExtensionId(dependency.getGroupId() + ':' + dependency.getArtifactId(), dependency.getVersion()));
        }

        ExtensionPlan status = createInstallPlan(installRequest, true);
        // Deal with errors
        if (status.getError() != null) {
            throw new MojoExecutionException(
                FAILED_RESOLVE_DEPENDENCIES.formatted(project), status.getError());
        }

        return status;
    }

    /**
     * Resolves the passed artifact as an extension, stores it and registers it as installed on the given namespace.
     *
     * @param artifact the artifact to resolve and register
     * @param namespace the namespace on which the extension is registered as installed, or {@code null} for the root
     *  namespace
     * @param dependency {@code true} if the extension is registered as a dependency of another extension
     * @param properties the custom install properties to associate with the installed extension
     * @return the registered installed extension
     * @throws MojoExecutionException if the extension cannot be resolved, stored or registered
     */
    public InstalledExtension registerInstalledExtension(Artifact artifact, String namespace, boolean dependency,
        Map<String, Object> properties) throws MojoExecutionException
    {
        Extension extension = getExtension(artifact);

        return registerInstalledExtension(extension, namespace, dependency, properties);
    }

    /**
     * Stores the passed extension and registers it as installed on the given namespace.
     *
     * @param extension the extension to register
     * @param namespace the namespace on which the extension is registered as installed, or {@code null} for the root
     *  namespace
     * @param dependency {@code true} if the extension is registered as a dependency of another extension
     * @param properties the custom install properties to associate with the installed extension
     * @return the registered installed extension
     * @throws MojoExecutionException if the extension cannot be stored or registered
     * @since 18.0.0RC1
     * @since 17.10.3
     */
    public InstalledExtension registerInstalledExtension(Extension extension, String namespace, boolean dependency,
        Map<String, Object> properties) throws MojoExecutionException
    {
        LocalExtension localExtension = storeExtension(extension);

        try {
            return getInstalledExtensionRepository().installExtension(localExtension, namespace, dependency,
                properties);
        } catch (InstallException e) {
            throw new MojoExecutionException("Failed to install extension", e);
        }
    }

    /**
     * Registers the passed flavors and their dependencies as installed on the given namespace, installing JAR type
     * dependencies on the root namespace.
     *
     * @param flavors the flavors to register as installed
     * @param namespace the namespace on which the flavors are registered as installed (for example
     *  {@code wiki:xwiki})
     * @throws MojoExecutionException if the install plan cannot be computed or an extension cannot be registered
     * @since 18.0.0RC1
     * @since 17.10.3
     */
    public void registerInstalledFlavors(List<ExtensionDescription> flavors, String namespace)
        throws MojoExecutionException
    {
        InstallRequest installRequest = new InstallRequest();

        // Install on main wiki
        installRequest.addNamespace(namespace);

        // JAR dependencies of flavors are installed on root namespace
        InstallOnRootNamespaceExtensionRewriter rewriter = new InstallOnRootNamespaceExtensionRewriter();
        rewriter.installExtensionTypeOnRootNamespace(JarExtensionHandler.JAR);
        rewriter.installExtensionTypeOnRootNamespace(JarExtensionHandler.WEBJAR);
        rewriter.installExtensionTypeOnRootNamespace(JarExtensionHandler.WEBJAR_NODE);
        installRequest.setRewriter(rewriter);

        for (ExtensionDescription extensionDescription : flavors) {
            installRequest.addExtension(
                new ExtensionId(extensionDescription.getId(), extensionDescription.getVersion()));
        }

        ExtensionPlan plan = createInstallPlan(installRequest, false);

        // Deal with errors
        if (plan.getError() != null) {
            throw new MojoExecutionException(
                "Failed to create an install plan for extensions [%s]".formatted(flavors), plan.getError());
        }

        for (ExtensionPlanAction action : plan.getActions()) {
            if (action.getAction() == Action.INSTALL) {
                registerInstalledExtension(action.getExtension(), action.getNamespace(), false, Collections.emptyMap());
            } else if (action.getAction() != Action.NONE) {
                throw new MojoExecutionException(
                    "Unexpected action [%s] when registering installed extensions".formatted(action));
            }
        }
    }

    /**
     * Installs the passed extensions on the given namespace.
     *
     * @param artifacts the extensions to install
     * @param namespace the namespace on which to install the extensions, or {@code null} for the root namespace
     * @param properties the custom install properties to associate with the installed extensions
     * @return the executed install job
     * @throws MojoExecutionException if the installation fails
     */
    public Job install(Collection<ExtensionArtifact> artifacts, String namespace, Map<String, Object> properties)
        throws MojoExecutionException
    {
        InstallRequest installRequest = new InstallRequest();

        return install(artifacts, installRequest, namespace, properties);
    }

    /**
     * Installs the passed extensions on the given namespace, using the provided install request.
     *
     * @param artifacts the extensions to add to the install request
     * @param installRequest the install request to complete and execute
     * @param namespace the namespace on which to install the extensions, or {@code null} for the root namespace
     * @param properties the custom install properties to associate with the installed extensions
     * @return the executed install job
     * @throws MojoExecutionException if the installation fails
     */
    public Job install(Collection<ExtensionArtifact> artifacts, InstallRequest installRequest, String namespace,
        Map<String, Object> properties) throws MojoExecutionException
    {
        for (ExtensionArtifact artifact : artifacts) {
            installRequest.addExtension(
                new ExtensionId(artifact.getGroupId() + ':' + artifact.getArtifactId(), artifact.getVersion()));
        }

        return install(installRequest, namespace, properties);
    }

    /**
     * Installs the passed artifact as an extension on the given namespace.
     *
     * @param artifact the artifact to install
     * @param namespace the namespace on which to install the extension, or {@code null} for the root namespace
     * @param properties the custom install properties to associate with the installed extension
     * @return the executed install job
     * @throws MojoExecutionException if the installation fails
     */
    public Job install(Artifact artifact, String namespace, Map<String, Object> properties)
        throws MojoExecutionException
    {
        InstallRequest installRequest = new InstallRequest();

        installRequest.addExtension(
            new ExtensionId(artifact.getGroupId() + ':' + artifact.getArtifactId(), artifact.getVersion()));

        return install(installRequest, namespace, properties);
    }

    /**
     * Executes the passed install request on the given namespace.
     *
     * @param installRequest the install request to execute
     * @param namespace the namespace on which to install the extensions, or {@code null} for the root namespace
     * @param properties the custom install properties to associate with the installed extensions, or {@code null}
     * @return the executed install job
     * @throws MojoExecutionException if the installation fails
     */
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

    /**
     * Serializes the descriptor of the extension corresponding to the passed artifact to the given file.
     *
     * @param path the file to write the extension descriptor to
     * @param artifact the artifact whose extension descriptor is serialized
     * @throws MojoExecutionException if the artifact's project cannot be built
     * @throws IOException if the descriptor cannot be written
     * @throws ParserConfigurationException if the descriptor XML document cannot be created
     * @throws TransformerException if the descriptor XML cannot be serialized
     */
    public void serializeExtension(File path, Artifact artifact)
        throws MojoExecutionException, IOException, ParserConfigurationException, TransformerException
    {
        // Get MavenProject instance
        MavenProject mavenProject = getMavenProject(artifact);

        serializeExtension(path, artifact, mavenProject.getModel());
    }

    /**
     * Serializes the descriptor of the extension corresponding to the passed artifact and Maven model to the given
     * file.
     *
     * @param path the file to write the extension descriptor to
     * @param artifact the artifact providing the classifier and type of the extension
     * @param model the Maven model providing the extension metadata
     * @throws IOException if the descriptor cannot be written
     * @throws ParserConfigurationException if the descriptor XML document cannot be created
     * @throws TransformerException if the descriptor XML cannot be serialized
     */
    public void serializeExtension(File path, Artifact artifact, Model model)
        throws IOException, ParserConfigurationException, TransformerException
    {
        serializeExtension(path, toArtifactModel(artifact, model));
    }

    /**
     * Combines the passed Maven model with the classifier and type of the passed artifact into an artifact model.
     *
     * @param artifact the artifact providing the classifier and type
     * @param model the Maven model providing the metadata
     * @return the resulting artifact model
     */
    public ArtifactModel toArtifactModel(Artifact artifact, Model model)
    {
        ArtifactModel artifactModel = new ArtifactModel(model);
        artifactModel.setClassifier(artifact.getClassifier());
        artifactModel.setType(artifact.getType());

        return artifactModel;
    }

    /**
     * Serializes the descriptor of the extension corresponding to the passed artifact model to the given file.
     *
     * @param path the file to write the extension descriptor to
     * @param artifactModel the artifact model to serialize
     * @throws IOException if the descriptor cannot be written
     * @throws ParserConfigurationException if the descriptor XML document cannot be created
     * @throws TransformerException if the descriptor XML cannot be serialized
     */
    public void serializeExtension(File path, ArtifactModel artifactModel)
        throws IOException, ParserConfigurationException, TransformerException
    {
        // Get Extension instance
        Extension mavenExtension = toExtension(artifactModel);

        serializeExtension(path, mavenExtension);
    }

    /**
     * Serializes the descriptor of the passed extension to the given file, unless that file already exists.
     *
     * @param path the file to write the extension descriptor to
     * @param mavenExtension the extension whose descriptor is serialized
     * @throws IOException if the descriptor cannot be written
     * @throws ParserConfigurationException if the descriptor XML document cannot be created
     * @throws TransformerException if the descriptor XML cannot be serialized
     */
    public void serializeExtension(File path, Extension mavenExtension)
        throws IOException, ParserConfigurationException, TransformerException
    {
        if (!path.exists()) {
            // Save the Extension descriptor
            try (FileOutputStream stream = new FileOutputStream(path)) {
                this.extensionSerializer.saveExtensionDescriptor(mavenExtension, stream);
            }
        }
    }

    /**
     * Applies the configured {@link ExtensionOverride overrides} to the passed extension, updating its version,
     * features and properties when a matching override is found.
     *
     * @param extension the extension to modify in place
     */
    public void override(MutableExtension extension)
    {
        if (this.extensionOverrides != null) {
            for (ExtensionOverride extensionOverride : this.extensionOverrides) {
                ExtensionId extensionId =
                    ExtensionIdConverter.toExtensionId(extensionOverride.get(Extension.FIELD_ID), null);
                if (extension.getId().getId().equals(extensionId.getId()) && (extensionId.getVersion() == null
                    || extension.getId().getVersion().equals(extensionId.getVersion()))) {
                    // Override version
                    String versionString = extensionOverride.get(Extension.FIELD_VERSION);
                    if (versionString != null) {
                        extension.setId(new ExtensionId(extension.getId().getId(), versionString));
                    }
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

    /**
     * Serializes the descriptor of the extension corresponding to the passed artifact into the given directory, using
     * the WAR plugin naming convention ({@code artifactId-baseVersion[-classifier].xed}).
     *
     * @param artifact the artifact whose extension descriptor is serialized
     * @param directory the directory in which the {@code .xed} descriptor file is created
     * @throws MojoExecutionException if the descriptor cannot be written
     */
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
            throw new MojoExecutionException("Failed to write descriptor for artifact [%s]".formatted(artifact), e);
        }
    }

    /**
     * Serializes the descriptors of the passed artifacts into the given directory, skipping optional artifacts.
     *
     * @param artifacts the artifacts whose extension descriptors are serialized
     * @param directory the directory in which the {@code .xed} descriptor files are created
     * @param type the artifact type to restrict serialization to (for example {@code jar}), or {@code null} to
     *  serialize artifacts of any type
     * @throws MojoExecutionException if a descriptor cannot be written
     */
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

    /**
     * {@return the configuration source backed by the Maven build}
     */
    public MavenBuildConfigurationSource getMavenBuildConfigurationSource()
    {
        return (MavenBuildConfigurationSource) this.configurationSource;
    }

    /**
     * Collects the transitive Maven artifacts of the passed extension artifacts, applying the dependency management
     * of the current project and excluding {@code system} scoped dependencies.
     *
     * @param input the root extension artifacts to collect the dependencies of
     * @return the collected set of artifacts, or {@code null} if {@code input} is {@code null}
     * @throws MojoExecutionException if the artifacts cannot be resolved
     */
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
