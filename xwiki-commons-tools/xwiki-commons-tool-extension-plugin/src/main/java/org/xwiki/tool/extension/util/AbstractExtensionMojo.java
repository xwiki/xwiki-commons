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
package org.xwiki.tool.extension.util;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusContainer;
import org.xwiki.extension.Extension;
import org.xwiki.tool.extension.ExtensionOverride;
import org.xwiki.tool.extension.internal.ExtensionMojoCoreExtensionRepository;

/**
 * Base class for Maven plugins manipulating extensions.
 * 
 * @version $Id$
 * @since 8.4RC1
 */
public abstract class AbstractExtensionMojo extends AbstractMojo
{
    @Component
    protected PlexusContainer container;

    @Component
    private RepositorySystem repositorySystem;

    @Component
    private ArtifactHandlerManager artifactHandlers;

    /**
     * The current Maven session being executed.
     */
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession session;

    /**
     * The local Maven repository used to resolve required artifacts.
     */
    @Parameter(property = "localRepository")
    protected ArtifactRepository localRepository;

    /**
     * @since 9.5RC1
     */
    @Parameter
    protected List<ExtensionOverride> extensionOverrides;

    /**
     * @since 9.5RC1
     */
    @Parameter
    protected boolean skip;

    /**
     * The extensions (and their dependencies) to resolve as core extensions.
     * 
     * @since 9.5RC1
     */
    @Parameter
    private List<ExtensionArtifact> coreExtensions;

    /**
     * List of remote repositories to be used by the plugin to resolve dependencies.
     */
    @Parameter(property = "project.remoteArtifactRepositories")
    private List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    /**
     * The permanent directory.
     * 
     * @since 9.5RC1
     */
    @Parameter(defaultValue = "${project.build.directory}/data/")
    protected File permanentDirectory;

    @Parameter(defaultValue = "${xwiki.extension.recommendedVersions}")
    protected String recommendedVersions;

    protected ExtensionMojoHelper extensionHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (isSkipExecution()) {
            getLog().info("Skipping execution");

            return;
        }

        before();

        try {
            executeInternal();
        } finally {
            after();
        }
    }

    protected boolean isSkipExecution()
    {
        return this.skip;
    }

    protected void before() throws MojoExecutionException
    {
        initializeExtensionMojoHelper();

        // We need to know which JAR extension we don't want to install (usually those that are already part of the
        // WAR)
        registerCoreExtensions();

        // Set recommended versions
        this.extensionHelper.getMavenBuildConfigurationSource().setProperty("extension.recommendedVersions",
            this.recommendedVersions);
    }

    protected void after() throws MojoExecutionException
    {
        this.extensionHelper.close();
    }

    protected abstract void executeInternal() throws MojoExecutionException, MojoFailureException;

    protected void initializeExtensionMojoHelper() throws MojoExecutionException
    {
        getLog().info("Initializing extension tools...");

        System.setProperty("org.slf4j.simpleLogger.log.org", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.org.xwiki", "info");
        // Explicitly ignore warnings about the logback system, since under Maven 3.1+ the logging framework used is
        // slf4j-simple
        System.setProperty("org.slf4j.simpleLogger.log.org.xwiki.logging.logback", "error");

        this.extensionHelper = ExtensionMojoHelper.create(this.project, this.permanentDirectory);
        this.extensionHelper.initalize(this.session, this.localRepository, this.container);

        this.extensionHelper.setExtensionOverrides(this.extensionOverrides);

        getLog().info("Done initializing extension tools");
    }

    private Set<Artifact> resolveMavenArtifacts(List<ExtensionArtifact> input) throws MojoExecutionException
    {
        if (input != null) {
            Set<Artifact> artifacts = new LinkedHashSet<>(input.size());
            for (ExtensionArtifact extensionArtifact : input) {
                artifacts.add(this.repositorySystem.createArtifact(extensionArtifact.getGroupId(),
                    extensionArtifact.getArtifactId(), extensionArtifact.getVersion(), null,
                    extensionArtifact.getType()));
            }

            ArtifactResolutionRequest request = new ArtifactResolutionRequest().setArtifact(this.project.getArtifact())
                .setRemoteRepositories(this.remoteRepositories).setArtifactDependencies(artifacts)
                .setLocalRepository(this.localRepository).setManagedVersionMap(this.project.getManagedVersionMap())
                .setResolveRoot(false);
            ArtifactResolutionResult resolutionResult = this.repositorySystem.resolve(request);
            if (resolutionResult.hasExceptions()) {
                throw new MojoExecutionException(
                    String.format("Failed to resolve artifacts [%s]", input, resolutionResult.getExceptions().get(0)));
            }

            return resolutionResult.getArtifacts();
        }

        return null;
    }

    private void registerCoreExtensions() throws MojoExecutionException
    {
        if (this.coreExtensions != null) {
            getLog().info("Registering core extensions...");

            Set<Artifact> coreArtifacts = resolveMavenArtifacts(this.coreExtensions);

            if (coreArtifacts != null) {
                // Set excluded extensions as core extensions so that they don't end up in any install plan
                ExtensionMojoCoreExtensionRepository repository =
                    this.extensionHelper.getExtensionMojoCoreExtensionRepository();

                for (Artifact artifact : coreArtifacts) {
                    ArtifactHandler artifactHandler = this.artifactHandlers.getArtifactHandler(artifact.getType());

                    // Only take into account artifacts which make sense as core extensions
                    if (artifactHandler.isAddedToClasspath()) {
                        try {
                            Extension extension = this.extensionHelper.getExtension(artifact);

                            repository.addExtension(extension);
                        } catch (Exception e) {
                            getLog().warn("Failed to resolve details for artifact [" + artifact + "] ("
                                + ExceptionUtils.getRootCauseMessage(e) + "). Only considering the id.");

                            repository.addExtension(artifact);
                        }
                    }
                }
            }

            getLog().info("Done registering core extensions");
        }
    }
}
