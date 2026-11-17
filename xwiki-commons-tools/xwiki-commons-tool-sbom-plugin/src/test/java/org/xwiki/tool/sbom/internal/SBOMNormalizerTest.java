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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for {@link SBOMNormalizer}.
 *
 * @version $Id$
 */
class SBOMNormalizerTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SBOM = """
        {
          "$schema": "http://cyclonedx.org/schema/bom-1.6.schema.json",
          "bomFormat": "CycloneDX",
          "specVersion": "1.6",
          "serialNumber": "urn:uuid:%s",
          "version": 1,
          "metadata": {
            "timestamp": "%s",
            "component": {"name": "livedata", "bom-ref": "pkg:npm/livedata@0.0.0"}
          },
          "components": [
            {"name": "vue", "bom-ref": "pkg:npm/vue@3.5.0"},
            {"name": "lodash", "bom-ref": "pkg:npm/lodash@4.17.15"}
          ],
          "dependencies": [
            {"ref": "pkg:npm/livedata@0.0.0", "dependsOn": ["pkg:npm/vue@3.5.0"]}
          ]
        }
        """;

    private final SBOMNormalizer normalizer = new SBOMNormalizer();

    @Test
    void normalize() throws Exception
    {
        ObjectNode bom = parse(SBOM.formatted("11111111-1111-1111-1111-111111111111", "2026-10-30T10:00:00Z"));
        JsonNode source = bom.deepCopy();

        ObjectNode normalized = this.normalizer.normalize(bom);

        assertEquals(source, bom);
        List<String> fieldNames = new ArrayList<>();
        normalized.fieldNames().forEachRemaining(fieldNames::add);
        assertEquals(List.of("$schema", "bomFormat", "specVersion", "serialNumber", "version", "metadata",
            "components", "dependencies"), fieldNames);
        assertFalse(normalized.get("metadata").has("timestamp"));
        assertEquals("pkg:npm/lodash@4.17.15", normalized.at("/components/0/bom-ref").asText());
        assertEquals("pkg:npm/vue@3.5.0", normalized.at("/components/1/bom-ref").asText());
        assertEquals(parse("""
            {"dependencies": [
              {"ref": "pkg:npm/livedata@0.0.0", "dependsOn": ["pkg:npm/lodash@4.17.15", "pkg:npm/vue@3.5.0"]}
            ]}
            """).get("dependencies"), normalized.get("dependencies"));
    }

    @Test
    void normalizeIsReproducible() throws Exception
    {
        ObjectNode first = parse(SBOM.formatted("11111111-1111-1111-1111-111111111111", "2026-10-30T10:00:00Z"));
        // Same packages, but another serial number, timestamp and component order.
        ObjectNode second = parse(SBOM.formatted("22222222-2222-2222-2222-222222222222", "2026-10-31T10:00:00Z"));
        second.withArrayProperty("components").insert(0, second.withArrayProperty("components").remove(1));

        String serialNumber = this.normalizer.normalize(first).get("serialNumber").asText();

        assertEquals(this.normalizer.normalize(first), this.normalizer.normalize(second));
        assertNotEquals("urn:uuid:11111111-1111-1111-1111-111111111111", serialNumber);

        first.withArrayProperty("components").remove(0);
        assertNotEquals(serialNumber, this.normalizer.normalize(first).get("serialNumber").asText());
    }

    @Test
    void normalizeWithoutDescribedComponent() throws Exception
    {
        ObjectNode bom = parse("""
            {"components": [{"bom-ref": "b"}, {"bom-ref": "a"}], "dependencies": [{"ref": "b"}]}
            """);

        ObjectNode normalized = this.normalizer.normalize(bom);

        List<String> fieldNames = new ArrayList<>();
        normalized.fieldNames().forEachRemaining(fieldNames::add);
        assertEquals(List.of("serialNumber", "components"), fieldNames);
        assertEquals("a", normalized.at("/components/0/bom-ref").asText());
    }

    private ObjectNode parse(String json) throws Exception
    {
        return (ObjectNode) MAPPER.readTree(json);
    }
}
