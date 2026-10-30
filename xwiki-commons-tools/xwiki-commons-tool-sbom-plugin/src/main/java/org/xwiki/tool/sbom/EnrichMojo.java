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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.xwiki.tool.sbom.internal.SBOMEnricher;
import org.xwiki.tool.sbom.internal.WebjarNpmResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

/**
 * Completes the CycloneDX SBOM attached to the artifact (by the {@code makeBom} goal of the
 * {@code cyclonedx-maven-plugin}, which must run before) with what Maven doesn't know about:
 * <ul>
 * <li>the npm packages bundled in the artifact and in its dependencies, listed by the CycloneDX SBOMs found in the
 * {@code META-INF/sbom/} directory of their JAR;</li>
 * <li>the npm package repackaged by each third-party webjar;</li>
 * <li>the organization supplying and manufacturing the artifact, taken from the POM {@code <organization>}.</li>
 * </ul>
 * It fails when the artifact, or one of its dependencies, is a webjar built from npm packages without an embedded
 * SBOM of these packages (see {@code failOnMissingEmbeddedSBOM}). It does nothing when no SBOM is attached to the
 * artifact.
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
@Mojo(name = "enrich", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true)
public class EnrichMojo extends AbstractMojo
{
    private static final String SBOM_CLASSIFIER = "cyclonedx";

    private static final String WEBJAR_NODE_TYPE = "webjar-node";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The name of the organization supplying and manufacturing the artifact.
     */
    @Parameter(defaultValue = "${project.organization.name}")
    private String supplierName;

    /**
     * The URL of the organization supplying and manufacturing the artifact.
     */
    @Parameter(defaultValue = "${project.organization.url}")
    private String supplierURL;

    /**
     * Whether to fail when the artifact, or one of its dependencies, is a webjar built from npm packages (i.e. of type
     * {@code webjar-node}) without an SBOM of these packages embedded in its {@code META-INF/sbom/} directory: they
     * would be missing from the SBOM. Only a warning is logged when {@code false}.
     */
    @Parameter(property = "xwiki.sbom.failOnMissingEmbeddedSBOM", defaultValue = "true")
    private boolean failOnMissingEmbeddedSBOM;

    @Override
    public void execute() throws MojoExecutionException
    {
        Optional<File> sbomFile = this.project.getAttachedArtifacts().stream()
            .filter(artifact -> SBOM_CLASSIFIER.equals(artifact.getClassifier()) && "json".equals(artifact.getType()))
            .map(Artifact::getFile).filter(Objects::nonNull).findFirst();
        if (sbomFile.isEmpty()) {
            getLog().debug("No CycloneDX SBOM in JSON format is attached to the artifact, nothing to enrich.");
            return;
        }

        try {
            checkEmbeddedSBOMs();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to look for the embedded SBOMs", e);
        }

        try {
            ObjectNode bom = (ObjectNode) MAPPER.readTree(sbomFile.get());
            SBOMEnricher enricher = new SBOMEnricher(bom, new WebjarNpmResolver(getLog()), getLog());
            if (this.supplierName != null) {
                enricher.setSupplier(this.supplierName, this.supplierURL);
            }
            enricher.enrich(this::getJAR);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(sbomFile.get(), bom);
        } catch (IOException | MalformedPackageURLException e) {
            throw new MojoExecutionException(String.format("Failed to enrich the SBOM [%s]", sbomFile.get()), e);
        }
    }

    private void checkEmbeddedSBOMs() throws IOException, MojoExecutionException
    {
        List<String> missing = new ArrayList<>();
        for (Artifact artifact : getArtifacts()) {
            File file = artifact.getFile();
            if (WEBJAR_NODE_TYPE.equals(artifact.getType()) && file != null && file.isFile()
                && !SBOMEnricher.hasEmbeddedSBOM(file.toPath())) {
                missing.add(artifact.getId());
            }
        }
        if (!missing.isEmpty()) {
            String message = String.format("The webjars %s don't embed the SBOM of the npm packages they bundle, so "
                + "these packages are missing from the SBOM. Build them with pnpm sbom generating it in "
                + "META-INF/sbom/.", missing);
            if (this.failOnMissingEmbeddedSBOM) {
                throw new MojoExecutionException(message);
            }
            getLog().warn(message);
        }
    }

    private List<Artifact> getArtifacts()
    {
        return Stream.concat(Stream.of(this.project.getArtifact()), this.project.getArtifacts().stream()).toList();
    }

    private Path getJAR(PackageURL purl)
    {
        // The type isn't compared: the webjars have their own types (e.g. "webjar-node") but are JAR files.
        Map<String, String> qualifiers = purl.getQualifiers();
        String classifier = qualifiers != null ? qualifiers.get("classifier") : null;

        return getArtifacts().stream()
            .filter(artifact -> Objects.equals(purl.getNamespace(), artifact.getGroupId())
                && Objects.equals(purl.getName(), artifact.getArtifactId()) && hasVersion(artifact, purl.getVersion())
                && Objects.equals(classifier, StringUtils.defaultIfEmpty(artifact.getClassifier(), null)))
            .map(Artifact::getFile).filter(file -> file != null && file.isFile() && file.getName().endsWith(".jar"))
            .map(File::toPath).findFirst().orElse(null);
    }

    private boolean hasVersion(Artifact artifact, String version)
    {
        // The version of a snapshot dependency is the timestamped one, while its base version ends with "-SNAPSHOT".
        return version.equals(artifact.getVersion()) || version.equals(artifact.getBaseVersion());
    }
}
