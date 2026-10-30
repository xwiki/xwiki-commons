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
package org.xwiki.tool.sbom.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.packageurl.PackageURL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link SBOMEnricher}.
 *
 * @version $Id$
 */
class SBOMEnricherTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String WEBJAR_REF = "pkg:maven/org.xwiki.platform/livedata-webjar@1.0?type=jar";

    private static final String OTHER_WEBJAR_REF = "pkg:maven/org.xwiki.platform/other-webjar@1.0?type=jar";

    private static final String WAR_REF = "pkg:maven/org.xwiki.platform/war@1.0?type=war";

    private static final String JQUERY_REF = "pkg:maven/org.webjars/jquery@3.4.1-1?type=jar";

    private static final String EMBEDDED_SBOM = """
        {
          "bomFormat": "CycloneDX",
          "specVersion": "1.6",
          "metadata": {"component": {"name": "livedata", "bom-ref": "pkg:npm/livedata@0.0.0"}},
          "components": [
            {"type": "library", "name": "vue", "version": "3.5.0", "purl": "pkg:npm/vue@3.5.0",
              "bom-ref": "pkg:npm/vue@3.5.0",
              "components": [{"type": "library", "name": "inner", "version": "1.0", "purl": "pkg:npm/inner@1.0"}]},
            {"type": "library", "name": "lodash", "version": "4.17.15", "purl": "pkg:npm/lodash@4.17.15",
              "bom-ref": "pkg:npm/lodash@4.17.15"}
          ],
          "dependencies": [
            {"ref": "pkg:npm/livedata@0.0.0", "dependsOn": ["pkg:npm/vue@3.5.0"]},
            {"ref": "pkg:npm/vue@3.5.0", "dependsOn": ["pkg:npm/lodash@4.17.15"]}
          ]
        }
        """;

    @TempDir
    private Path tmpDir;

    private Log log;

    private WebjarNpmResolver webjarResolver;

    @BeforeEach
    void setUp() throws Exception
    {
        this.log = mock(Log.class);
        this.webjarResolver = new WebjarNpmResolver(this.log);
    }

    @Test
    void setSupplier() throws Exception
    {
        ObjectNode bom = (ObjectNode) MAPPER.readTree("{\"metadata\": {}}");

        new SBOMEnricher(bom, this.webjarResolver, this.log).setSupplier("XWiki", "https://xwiki.org/");

        JsonNode expected = MAPPER.readTree("{\"name\": \"XWiki\", \"url\": [\"https://xwiki.org/\"]}");
        assertEquals(expected, bom.at("/metadata/supplier"));
        assertEquals(expected, bom.at("/metadata/manufacturer"));
    }

    @Test
    void setSupplierWhenAlreadySet() throws Exception
    {
        ObjectNode bom = (ObjectNode) MAPPER.readTree("{\"metadata\": {\"supplier\": {\"name\": \"Other\"}}}");

        new SBOMEnricher(bom, this.webjarResolver, this.log).setSupplier("XWiki", null);

        assertEquals("Other", bom.at("/metadata/supplier/name").asText());
        assertEquals(MAPPER.readTree("{\"name\": \"XWiki\"}"), bom.at("/metadata/manufacturer"));
    }

    @Test
    void enrichWithEmbeddedSBOMs() throws Exception
    {
        Path webjar = createJAR("livedata.jar", Map.of("META-INF/sbom/livedata.cdx.json", EMBEDDED_SBOM,
            "META-INF/sbom/notes.json", "{\"some\": \"json\"}",
            "META-INF/sbom/sub/other.cdx.json", EMBEDDED_SBOM));
        ObjectNode bom = createBOM(WAR_REF, WEBJAR_REF);

        enrich(bom, Map.of(WEBJAR_REF, webjar));

        String vueRef = WEBJAR_REF + "!/pkg:npm/vue@3.5.0";
        String innerRef = WEBJAR_REF + "!/pkg:npm/inner@1.0";
        String lodashRef = WEBJAR_REF + "!/pkg:npm/lodash@4.17.15";
        assertEquals(List.of(WEBJAR_REF, vueRef, innerRef, lodashRef), getRefs(bom));
        assertEquals("pkg:npm/vue@3.5.0", bom.at("/components/1/purl").asText());
        assertEquals(MAPPER.readTree(String.format("""
            [
              {"ref": "%s", "dependsOn": ["%s"]},
              {"ref": "%s", "dependsOn": ["%s", "%s", "%s"]},
              {"ref": "%s", "dependsOn": ["%s"]}
            ]
            """, WAR_REF, WEBJAR_REF, WEBJAR_REF, vueRef, innerRef, lodashRef, vueRef, lodashRef)),
            bom.get("dependencies"));
        verify(this.log).warn(String.format(
            "Ignoring [META-INF/sbom/notes.json] in [%s] since it's not a CycloneDX SBOM.", webjar));
    }

    @Test
    void enrichWithEmbeddedSBOMOfTheDescribedComponent() throws Exception
    {
        Path webjar = createJAR("livedata.jar", Map.of("META-INF/sbom/livedata.cdx.json", EMBEDDED_SBOM));
        ObjectNode bom = createBOM(WEBJAR_REF);

        enrich(bom, Map.of(WEBJAR_REF, webjar));

        assertEquals(List.of(WEBJAR_REF + "!/pkg:npm/vue@3.5.0", WEBJAR_REF + "!/pkg:npm/inner@1.0",
            WEBJAR_REF + "!/pkg:npm/lodash@4.17.15"), getRefs(bom));
    }

    @Test
    void enrichWithSamePackageBundledInSeveralJARs() throws Exception
    {
        Map<String, String> entries = Map.of("META-INF/sbom/livedata.cdx.json", EMBEDDED_SBOM);
        ObjectNode bom = createBOM(WAR_REF, WEBJAR_REF, OTHER_WEBJAR_REF);

        enrich(bom, Map.of(WEBJAR_REF, createJAR("livedata.jar", entries), OTHER_WEBJAR_REF,
            createJAR("other.jar", entries)));

        assertEquals(8, bom.get("components").size());
        assertEquals(OTHER_WEBJAR_REF + "!/pkg:npm/vue@3.5.0", bom.at("/components/5/bom-ref").asText());
    }

    @Test
    void enrichWithThirdPartyWebjar() throws Exception
    {
        ObjectNode bom = createBOM(WAR_REF, JQUERY_REF);

        enrich(bom, Map.of());

        String jqueryNpmRef = JQUERY_REF + "!/pkg:npm/jquery@3.4.1";
        assertEquals(List.of(JQUERY_REF, jqueryNpmRef), getRefs(bom));
        assertEquals(MAPPER.readTree(String.format("""
            {"type": "library", "name": "jquery", "version": "3.4.1", "purl": "pkg:npm/jquery@3.4.1",
              "bom-ref": "%s"}
            """, jqueryNpmRef)), bom.at("/components/1"));
        assertEquals(jqueryNpmRef, bom.at("/dependencies/1/dependsOn/0").asText());
    }

    @Test
    void enrichTwice() throws Exception
    {
        Path webjar = createJAR("livedata.jar", Map.of("META-INF/sbom/livedata.cdx.json", EMBEDDED_SBOM));
        ObjectNode bom = createBOM(WAR_REF, WEBJAR_REF, JQUERY_REF);
        enrich(bom, Map.of(WEBJAR_REF, webjar));
        JsonNode enriched = bom.deepCopy();

        enrich(bom, Map.of(WEBJAR_REF, webjar));

        assertEquals(enriched, bom);
    }

    @Test
    void enrichWithoutMavenComponents() throws Exception
    {
        ObjectNode bom = (ObjectNode) MAPPER.readTree("""
            {"components": [{"name": "noref"}, {"bom-ref": "npm", "purl": "pkg:npm/jquery@3.4.1"}]}
            """);
        JsonNode original = bom.deepCopy();

        enrich(bom, Map.of());

        assertEquals(original.get("components"), bom.get("components"));
        verify(this.log, never()).warn(anyString());
    }

    private void enrich(ObjectNode bom, Map<String, Path> jars) throws Exception
    {
        Function<PackageURL, Path> jarResolver = purl -> jars.get(purl.canonicalize());
        new SBOMEnricher(bom, this.webjarResolver, this.log).enrich(jarResolver);
    }

    private ObjectNode createBOM(String describedRef, String... componentRefs)
    {
        ObjectNode bom = MAPPER.createObjectNode();
        bom.putObject("metadata").set("component", createComponent(describedRef));
        for (String componentRef : componentRefs) {
            bom.withArrayProperty("components").add(createComponent(componentRef));
        }
        ObjectNode dependency = bom.withArrayProperty("dependencies").addObject();
        dependency.put("ref", describedRef);
        for (String componentRef : componentRefs) {
            dependency.withArrayProperty("dependsOn").add(componentRef);
        }

        return bom;
    }

    private ObjectNode createComponent(String ref)
    {
        ObjectNode component = MAPPER.createObjectNode();
        component.put("type", "library");
        component.put("bom-ref", ref);
        component.put("purl", ref);

        return component;
    }

    private List<String> getRefs(JsonNode bom)
    {
        List<String> refs = new ArrayList<>();
        bom.path("components").forEach(component -> refs.add(component.path("bom-ref").asText()));

        return refs;
    }

    private Path createJAR(String name, Map<String, String> entries) throws IOException
    {
        Path jar = this.tmpDir.resolve(name);
        try (OutputStream stream = Files.newOutputStream(jar); ZipOutputStream zip = new ZipOutputStream(stream)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }

        return jar;
    }
}
