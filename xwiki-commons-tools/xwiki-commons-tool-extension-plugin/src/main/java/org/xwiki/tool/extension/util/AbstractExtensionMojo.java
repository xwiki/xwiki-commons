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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.extension.Extension;
import org.xwiki.extension.MutableExtension;
import org.xwiki.extension.internal.ExtensionUtils;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;
import org.xwiki.extension.repository.internal.ExtensionSerializer;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.properties.converter.Converter;
import org.xwiki.tool.extension.ExtensionOverride;

/**
 * Base class for Maven plugins manipulating extensions.
 * 
 * @version $Id$
 * @since 8.4RC1
 */
public abstract class AbstractExtensionMojo extends AbstractMojo
{
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
     * Project builder -- builds a model from a pom.xml.
     */
    @Component
    protected ProjectBuilder projectBuilder;

    @Parameter
    protected List<ExtensionOverride> extensionOverrides;

    protected ExtensionSerializer extensionSerializer;

    protected Converter<Extension> extensionConverter;

    protected void initializeComponents() throws MojoExecutionException
    {
        // Initialize ComponentManager
        EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
        componentManager.initialize(this.getClass().getClassLoader());

        // Initialize components
        try {
            this.extensionSerializer = componentManager.getInstance(ExtensionSerializer.class);
            this.extensionConverter =
                componentManager.getInstance(new DefaultParameterizedType(null, Converter.class, Extension.class));
        } catch (ComponentLookupException e) {
            throw new MojoExecutionException("Failed to load components", e);
        }
    }

    protected MavenProject getMavenProject(Artifact artifact) throws MojoExecutionException
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

    protected Extension toExtension(Artifact artifact) throws MojoExecutionException
    {
        MavenProject mavenProject = getMavenProject(artifact);

        return toExtension(mavenProject.getModel());
    }

    protected Extension toExtension(Model model)
    {
        return this.extensionConverter.convert(Extension.class, model);
    }

    protected void saveExtension(File path, Artifact artifact)
        throws MojoExecutionException, IOException, ParserConfigurationException, TransformerException
    {
        // Get MavenProject instance
        MavenProject mavenProject = getMavenProject(artifact);

        saveExtension(path, mavenProject.getModel());
    }

    protected void saveExtension(File path, Model model)
        throws IOException, ParserConfigurationException, TransformerException
    {
        // Get Extension instance
        Extension mavenExtension = this.extensionConverter.convert(Extension.class, model);
        MutableExtension mutableExtension;
        if (mavenExtension instanceof MutableExtension) {
            mutableExtension = (MutableExtension) mavenExtension;
        } else {
            mutableExtension = new DefaultLocalExtension(null, mavenExtension);
        }

        if (!path.exists()) {
            // Apply overrides
            override(mutableExtension);

            // Save the Extension descriptor
            try (FileOutputStream stream = new FileOutputStream(path)) {
                this.extensionSerializer.saveExtensionDescriptor(mavenExtension, stream);
            }
        }
    }

    protected void override(MutableExtension extension)
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
                    }
                }
            }
        }
    }

    protected void saveExtension(Artifact artifact, File directory) throws MojoExecutionException
    {
        // Get path
        // WAR plugin use based version for the name of the actual file stored in the package
        File path = new File(directory, artifact.getArtifactId() + '-' + artifact.getBaseVersion() + ".xed");

        try {
            saveExtension(path, artifact);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to write descriptor for artifact [" + artifact + "]", e);
        }
    }

    protected void saveExtensions(Collection<Artifact> artifacts, File directory, String type)
        throws MojoExecutionException
    {
        // Register dependencies
        for (Artifact artifact : artifacts) {
            if (!artifact.isOptional()) {
                if (type == null || type.equals(artifact.getType())) {
                    saveExtension(artifact, directory);
                }
            }
        }
    }
}
