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
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnrichMojo}.
 *
 * @version $Id$
 */
class EnrichMojoTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String WEBJAR_PURL = "pkg:maven/org.xwiki.platform/livedata-webjar@1.0-SNAPSHOT?type=jar";

    private static final String SBOM = """
        {
          "bomFormat": "CycloneDX",
          "metadata": {"component": {"bom-ref": "war", "purl": "pkg:maven/org.xwiki.platform/war@1.0?type=war"}},
          "components": [
            {"bom-ref": "webjar", "purl": "%s"},
            {"bom-ref": "test", "purl": "pkg:maven/org.xwiki.platform/livedata-webjar@1.0-SNAPSHOT?classifier=tests"}
          ]
        }
        """.formatted(WEBJAR_PURL);

    @TempDir
    private Path tmpDir;

    private MavenProject project;

    private EnrichMojo mojo;

    @BeforeEach
    void setUp() throws Exception
    {
        this.project = new MavenProject();
        this.mojo = new EnrichMojo();
        set("project", this.project);
        set("supplierName", "XWiki");
        set("supplierURL", "https://xwiki.org/");
        set("failOnMissingEmbeddedSBOM", true);

        Artifact war = mockArtifact("war", null, "1.0", this.tmpDir.resolve("war.war"));
        Files.createFile(war.getFile().toPath());
        this.project.setArtifact(war);
    }

    @Test
    void execute() throws Exception
    {
        Path jar = this.tmpDir.resolve("livedata-webjar-1.0-20261030.120000-1.jar");
        try (OutputStream stream = Files.newOutputStream(jar); ZipOutputStream zip = new ZipOutputStream(stream)) {
            zip.putNextEntry(new ZipEntry("META-INF/sbom/livedata.cdx.json"));
            zip.write("""
                {"bomFormat": "CycloneDX", "components": [{"bom-ref": "pkg:npm/vue@3.5.0"}]}
                """.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        Artifact webjar = mockArtifact("livedata-webjar", "", "1.0-20261030.120000-1", jar);
        when(webjar.getBaseVersion()).thenReturn("1.0-SNAPSHOT");
        when(webjar.getType()).thenReturn("webjar-node");
        Artifact tests = mockArtifact("livedata-webjar", "tests", "1.0-SNAPSHOT", this.tmpDir.resolve("missing.jar"));
        this.project.setArtifacts(Set.of(webjar, tests));
        File sbomFile = attachSBOM(SBOM);

        this.mojo.execute();

        JsonNode bom = MAPPER.readTree(sbomFile);
        assertEquals("XWiki", bom.at("/metadata/supplier/name").asText());
        assertEquals("XWiki", bom.at("/metadata/manufacturer/name").asText());
        assertEquals("webjar!/pkg:npm/vue@3.5.0", bom.at("/components/2/bom-ref").asText());
        assertEquals(3, bom.get("components").size());
    }

    @Test
    void executeWithoutSBOM() throws Exception
    {
        Artifact sources = mockArtifact("war", "sources", "1.0", this.tmpDir.resolve("sources.jar"));
        when(sources.getType()).thenReturn("java-source");
        this.project.addAttachedArtifact(sources);
        Log log = mock(Log.class);
        this.mojo.setLog(log);

        this.mojo.execute();

        verify(log).debug("No CycloneDX SBOM in JSON format is attached to the artifact, nothing to enrich.");
    }

    @Test
    void executeWithInvalidSBOM() throws Exception
    {
        attachSBOM("{\"components\": [{\"bom-ref\": \"invalid\", \"purl\": \"invalid\"}]}");

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("Failed to enrich the SBOM [%s]".formatted(this.tmpDir.resolve("bom.json")),
            exception.getMessage());
    }

    @Test
    void executeWithWebjarWithoutEmbeddedSBOM() throws Exception
    {
        Path jar = this.tmpDir.resolve("other-webjar.jar");
        try (OutputStream stream = Files.newOutputStream(jar); ZipOutputStream zip = new ZipOutputStream(stream)) {
            zip.putNextEntry(new ZipEntry("META-INF/resources/webjars/other/index.js"));
            zip.closeEntry();
        }
        Artifact webjar = mockArtifact("other-webjar", null, "1.0", jar);
        when(webjar.getType()).thenReturn("webjar-node");
        when(webjar.getId()).thenReturn("org.xwiki.platform:other-webjar:webjar-node:1.0");
        this.project.setArtifacts(Set.of(webjar));
        File sbomFile = attachSBOM(SBOM);
        String message = "The webjars [org.xwiki.platform:other-webjar:webjar-node:1.0] don't embed the SBOM of the "
            + "npm packages they bundle, so these packages are missing from the SBOM. Build them with pnpm sbom "
            + "generating it in META-INF/sbom/.";

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals(message, exception.getMessage());

        set("failOnMissingEmbeddedSBOM", false);
        Log log = mock(Log.class);
        this.mojo.setLog(log);

        this.mojo.execute();

        verify(log).warn(message);
        assertEquals("XWiki", MAPPER.readTree(sbomFile).at("/metadata/supplier/name").asText());
    }

    @Test
    void executeWithInvalidWebjar() throws Exception
    {
        Path jar = this.tmpDir.resolve("invalid.jar");
        Files.writeString(jar, "not a JAR");
        Artifact webjar = mockArtifact("invalid", null, "1.0", jar);
        when(webjar.getType()).thenReturn("webjar-node");
        this.project.setArtifacts(Set.of(webjar));
        attachSBOM(SBOM);

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("Failed to look for the embedded SBOMs", exception.getMessage());
    }

    private File attachSBOM(String content) throws Exception
    {
        Path sbomFile = this.tmpDir.resolve("bom.json");
        Files.writeString(sbomFile, content);
        Artifact sbom = mockArtifact("war", "cyclonedx", "1.0", sbomFile);
        when(sbom.getType()).thenReturn("json");
        this.project.addAttachedArtifact(sbom);

        return sbomFile.toFile();
    }

    private Artifact mockArtifact(String artifactId, String classifier, String version, Path file)
    {
        Artifact artifact = mock(Artifact.class);
        when(artifact.getGroupId()).thenReturn("org.xwiki.platform");
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getClassifier()).thenReturn(classifier);
        when(artifact.getVersion()).thenReturn(version);
        when(artifact.getBaseVersion()).thenReturn(version);
        when(artifact.getFile()).thenReturn(file.toFile());

        return artifact;
    }

    private void set(String fieldName, Object value) throws Exception
    {
        Field field = EnrichMojo.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(this.mojo, value);
    }
}
