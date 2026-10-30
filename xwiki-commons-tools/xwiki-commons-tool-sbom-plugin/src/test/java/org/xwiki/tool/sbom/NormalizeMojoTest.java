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

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link NormalizeMojo}.
 *
 * @version $Id$
 */
class NormalizeMojoTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    private Path tmpDir;

    private Path sourceFile;

    private Path targetFile;

    private NormalizeMojo mojo;

    @BeforeEach
    void setUp() throws Exception
    {
        this.sourceFile = this.tmpDir.resolve("npm-sbom/bom.json");
        this.targetFile = this.tmpDir.resolve("classes/META-INF/sbom/npm.cdx.json");
        this.mojo = new NormalizeMojo();
        set("sourceFile", this.sourceFile.toFile());
        set("targetFile", this.targetFile.toFile());
    }

    @Test
    void execute() throws Exception
    {
        Files.createDirectories(this.sourceFile.getParent());
        Files.writeString(this.sourceFile, """
            {"bomFormat": "CycloneDX", "metadata": {"timestamp": "2026-10-30T10:00:00Z"},
              "components": [{"bom-ref": "pkg:npm/vue@3.5.0"}, {"bom-ref": "pkg:npm/lodash@4.17.15"}]}
            """);

        this.mojo.execute();

        JsonNode bom = MAPPER.readTree(this.targetFile.toFile());
        assertFalse(bom.get("metadata").has("timestamp"));
        assertEquals("pkg:npm/lodash@4.17.15", bom.at("/components/0/bom-ref").asText());
    }

    @Test
    void executeWithoutSBOM() throws Exception
    {
        Log log = mock(Log.class);
        this.mojo.setLog(log);

        this.mojo.execute();

        verify(log).info("No SBOM found at [%s], nothing to normalize.".formatted(this.sourceFile));
        assertFalse(Files.exists(this.targetFile));
    }

    @Test
    void executeWithInvalidSBOM() throws Exception
    {
        Files.createDirectories(this.sourceFile.getParent());
        Files.writeString(this.sourceFile, "[]");

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("Failed to normalize the SBOM [%s]: it's not a JSON object.".formatted(this.sourceFile),
            exception.getMessage());

        Files.writeString(this.sourceFile, "{");

        exception = assertThrows(MojoExecutionException.class, this.mojo::execute);

        assertEquals("Failed to normalize the SBOM [%s]".formatted(this.sourceFile), exception.getMessage());
    }

    private void set(String fieldName, Object value) throws Exception
    {
        Field field = NormalizeMojo.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(this.mojo, value);
    }
}
