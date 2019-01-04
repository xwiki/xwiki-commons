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
package org.xwiki.tool.xar;

import java.io.File;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.ArchiverException;

/**
 * Expand a XAR file.
 * 
 * @version $Id$
 */
@Mojo(
    name = "unxar",
    requiresDependencyResolution = ResolutionScope.COMPILE,
    threadSafe = true
)
public class UnXARMojo extends AbstractXARMojo
{
    /**
     * The groupId of the XAR dependency to expand.
     */
    @Parameter(required = true)
    private String groupId;

    /**
     * The artifactId of the XAR dependency to expand.
     */
    @Parameter(required = true)
    private String artifactId;

    /**
     * The location where to put the expanded XAR.
     */
    @Parameter(required = true)
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        this.outputDirectory.mkdirs();

        try {
            performUnArchive();
        } catch (Exception e) {
            throw new MojoExecutionException(
                String.format("Error while expanding the XAR file [%s:%s]", this.groupId, this.artifactId), e);
        }
    }

    /**
     * @return the Maven artifact.
     * @throws MojoExecutionException error when searching for the Maven artifact.
     */
    private Artifact findArtifact() throws MojoExecutionException
    {
        Artifact resolvedArtifact = null;

        getLog().debug(
            String.format("Searching for an artifact that matches [%s:%s]...", this.groupId, this.artifactId));

        for (Artifact artifact : this.project.getArtifacts()) {
            getLog().debug(String.format("Checking artifact [%s:%s:%s]...",
                artifact.getGroupId(), artifact.getArtifactId(), artifact.getType()));

            if (artifact.getGroupId().equals(this.groupId) && artifact.getArtifactId().equals(this.artifactId)) {
                resolvedArtifact = artifact;
                break;
            }
        }

        if (resolvedArtifact == null) {
            throw new MojoExecutionException(String.format("Artifact [%s:%s] is not a dependency of the project.",
                this.groupId, this.artifactId));
        }

        return resolvedArtifact;
    }

    /**
     * Unzip xar artifact and its dependencies.
     * 
     * @throws ArchiverException error when unzip package.O
     * @throws MojoExecutionException error when unzip package.
     */
    private void performUnArchive() throws MojoExecutionException
    {
        Artifact artifact = findArtifact();

        getLog().debug(String.format("Source XAR = [%s]", artifact.getFile()));
        unpack(artifact.getFile(), this.outputDirectory, "XAR Plugin", true, getIncludes(), getExcludes());
        unpackDependentXars(artifact);
    }

    /**
     * Unpack xar dependencies of the provided artifact.
     * 
     * @throws MojoExecutionException error when unpack dependencies.
     */
    protected void unpackDependentXars(Artifact artifact) throws MojoExecutionException
    {
        try {
            Set<Artifact> dependencies = resolveArtifactDependencies(artifact);
            for (Artifact dependency : dependencies) {
                unpack(dependency.getFile(), this.outputDirectory, "XAR Plugin", false, getIncludes(), getExcludes());
            }
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Failed to unpack artifact [%s] dependencies", artifact), e);
        }
    }
}
