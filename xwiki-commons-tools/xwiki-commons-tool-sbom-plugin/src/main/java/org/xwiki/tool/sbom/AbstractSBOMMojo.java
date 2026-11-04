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
package org.xwiki.tool.sbom;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Base class of the goals working on the CycloneDX SBOM attached to the artifact (by the {@code makeBom} goal of the
 * {@code cyclonedx-maven-plugin}).
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
public abstract class AbstractSBOMMojo extends AbstractMojo
{
    private static final String SBOM_CLASSIFIER = "cyclonedx";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * @return the CycloneDX SBOM in JSON format attached to the artifact, if any
     */
    protected Optional<File> getAttachedSBOM()
    {
        return this.project.getAttachedArtifacts().stream()
            .filter(artifact -> SBOM_CLASSIFIER.equals(artifact.getClassifier()) && "json".equals(artifact.getType()))
            .map(Artifact::getFile).filter(Objects::nonNull).findFirst();
    }
}
