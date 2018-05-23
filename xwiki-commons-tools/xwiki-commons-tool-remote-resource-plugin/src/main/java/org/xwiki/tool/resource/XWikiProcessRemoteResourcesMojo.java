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
package org.xwiki.tool.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.resources.remote.ProcessRemoteResourcesMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

@Mojo(name = "process", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class XWikiProcessRemoteResourcesMojo extends ProcessRemoteResourcesMojo
{
    /**
     * Control what is injected in the "projects" Velocity binding.
     * 
     * @version $Id$
     */
    public enum ProjectData
    {
        /**
         * "projects" is empty
         */
        NONE,

        /**
         * "projects" only contains licenses
         */
        LICENSES,

        /**
         * "projects" contains full MavenProject metadata (very expensive but standard Maven Resource plugin behavior)
         */
        FULL
    }

    @Component
    private MavenProject projectThis;

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository localRepositoryThis;

    @Component(role = MavenProjectBuilder.class)
    private MavenProjectBuilder mavenProjectBuilderThis;

    @Parameter(defaultValue = "NONE")
    protected ProjectData projectsData;

    @Override
    protected List<MavenProject> getProjects() throws MojoExecutionException
    {
        switch (this.projectsData) {
            case FULL:
                return super.getProjects();

            case LICENSES:
                return getLicenses();

            default:
                break;
        }

        return Collections.emptyList();
    }

    private List<MavenProject> getLicenses() throws MojoExecutionException
    {
        Set<Artifact> artifacts = this.projectThis.getArtifacts();

        List<MavenProject> licenses = new ArrayList<>(artifacts.size());

        for (Artifact artifact : artifacts) {
            try {
                getLog().debug("Building project for " + artifact);

                MavenProject dependencyProject = null;
                try {
                    dependencyProject = this.mavenProjectBuilderThis.buildFromRepository(artifact,
                        Collections.emptyList(), this.localRepositoryThis);
                } catch (InvalidProjectModelException e) {
                    getLog().warn(
                        "Invalid project model for artifact [" + artifact.getArtifactId() + ":" + artifact.getGroupId()
                            + ":" + artifact.getVersion() + "]. " + "It will be ignored by the remote resources Mojo.");
                    continue;
                }

                // Extract license and a summary of the project metadata
                Model miniModel = new Model();
                MavenProject miniProject = new MavenProject(miniModel);
                miniProject.setArtifact(dependencyProject.getArtifact());
                miniProject.setGroupId(dependencyProject.getGroupId());
                miniProject.setArtifactId(dependencyProject.getArtifactId());
                miniProject.setVersion(dependencyProject.getVersion());
                miniProject.setUrl(dependencyProject.getUrl());
                for (License license : dependencyProject.getLicenses()) {
                    // Get rid of XML source metadata
                    miniProject.addLicense(cloneLicense(license));
                }
                // Get rid of XML source metadata
                miniProject.setOrganization(cloneOrganization(dependencyProject.getOrganization()));

                licenses.add(miniProject);
            } catch (ProjectBuildingException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        return licenses;
    }

    public static License cloneLicense(License src)
    {
        if (src == null) {
            return null;
        }

        License result = new License();

        result.setComments(src.getComments());
        result.setDistribution(src.getDistribution());
        result.setName(src.getName());
        result.setUrl(src.getUrl());

        return result;
    }

    public static Organization cloneOrganization(Organization src)
    {
        if (src == null) {
            return null;
        }

        Organization result = new Organization();

        result.setName(src.getName());
        result.setUrl(src.getUrl());

        return result;
    }
}
