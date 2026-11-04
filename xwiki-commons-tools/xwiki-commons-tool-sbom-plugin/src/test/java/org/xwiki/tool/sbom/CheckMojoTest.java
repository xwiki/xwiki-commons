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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CheckMojo}.
 *
 * @version $Id$
 */
class CheckMojoTest
{
    private static final String SBOM = """
        {
          "bomFormat": "CycloneDX",
          "components": [
            {"bom-ref": "webjar", "purl": "pkg:maven/org.xwiki.platform/livedata-webjar@1.0?type=jar"},
            {"bom-ref": "webjar!/pkg:npm/vue-i18n@11.4.12", "purl": "pkg:npm/vue-i18n@11.4.12"},
            {"bom-ref": "webjar!/pkg:npm/%40intlify/core-base@11.4.12", "purl": "pkg:npm/%40intlify/core-base@11.4.12"},
            {"bom-ref": "no-purl"}
          ]
        }
        """;

    @TempDir
    private Path tmpDir;

    private Path sbomFile;

    private MavenProject project;

    private CheckMojo mojo;

    @BeforeEach
    void setUp() throws Exception
    {
        this.project = new MavenProject();
        this.mojo = new CheckMojo();
        set("project", this.project);
        this.sbomFile = this.tmpDir.resolve("bom.json");
    }

    @Test
    void execute() throws Exception
    {
        attachSBOM(SBOM);
        set("expectedComponents", List.of("pkg:npm/vue-i18n", "pkg:npm/%40intlify/core-base@11.4.12",
            "pkg:maven/org.xwiki.platform/livedata-webjar?type=war"));
        set("forbiddenComponents", List.of("pkg:maven/jakarta.servlet/jakarta.servlet-api", "pkg:npm/vue-i18n@11.4.11",
            "pkg:npm/core-base"));

        this.mojo.execute();
    }

    @Test
    void executeWithInvalidSBOM() throws Exception
    {
        attachSBOM(SBOM);
        set("expectedComponents",
            List.of("pkg:npm/vue-i18n", "pkg:npm/jquery", "pkg:npm/%40intlify/core-base@11.4.11"));
        set("forbiddenComponents", List.of("pkg:maven/org.xwiki.platform/livedata-webjar", "pkg:npm/lodash"));

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("The SBOM [" + this.sbomFile + "] is invalid: it doesn't contain the expected components "
            + "[pkg:npm/jquery, pkg:npm/%40intlify/core-base@11.4.11], and it contains the forbidden components "
            + "[pkg:maven/org.xwiki.platform/livedata-webjar].", exception.getMessage());
    }

    @Test
    void executeWithOnlyForbiddenComponents() throws Exception
    {
        attachSBOM(SBOM);
        set("forbiddenComponents", List.of("pkg:npm/vue-i18n"));

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("The SBOM [%s] is invalid: it contains the forbidden components [pkg:npm/vue-i18n]."
            .formatted(this.sbomFile), exception.getMessage());
    }

    @Test
    void executeWithInvalidPattern() throws Exception
    {
        attachSBOM(SBOM);
        set("expectedComponents", List.of("vue-i18n"));

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("Invalid package URL [vue-i18n]", exception.getMessage());
    }

    @Test
    void executeWithUnreadableSBOM() throws Exception
    {
        attachSBOM("{\"components\": [{\"purl\": \"invalid\"}]}");

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("Failed to read the SBOM [%s]".formatted(this.sbomFile), exception.getMessage());
    }

    @Test
    void executeWithoutSBOM() throws Exception
    {
        Log log = mock(Log.class);
        this.mojo.setLog(log);

        this.mojo.execute();

        verify(log).debug("No CycloneDX SBOM in JSON format is attached to the artifact, nothing to check.");
    }

    private void attachSBOM(String content) throws Exception
    {
        Files.writeString(this.sbomFile, content);
        Artifact sbom = mock(Artifact.class);
        when(sbom.getClassifier()).thenReturn("cyclonedx");
        when(sbom.getType()).thenReturn("json");
        when(sbom.getFile()).thenReturn(this.sbomFile.toFile());
        this.project.addAttachedArtifact(sbom);
    }

    private void set(String fieldName, Object value) throws Exception
    {
        FieldUtils.writeField(this.mojo, fieldName, value, true);
    }
}
