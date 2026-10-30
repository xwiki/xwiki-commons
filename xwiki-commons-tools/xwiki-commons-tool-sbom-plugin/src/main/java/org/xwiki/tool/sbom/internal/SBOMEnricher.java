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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

/**
 * Completes a CycloneDX SBOM generated from the Maven dependencies with the npm packages they contain:
 * <ul>
 * <li>the packages bundled in a JAR (e.g. by Vite in a webjar), listed by the CycloneDX SBOMs found in its
 * {@code META-INF/sbom/} directory;</li>
 * <li>the package repackaged by a third-party webjar.</li>
 * </ul>
 * Each npm package is added as a top-level component, which the JAR containing it depends on, rather than as a
 * component nested in the JAR one: some vulnerability scanners (e.g. Trivy) ignore nested components. Its
 * {@code bom-ref} is the {@code bom-ref} of the JAR, followed by {@code !/} and the {@code bom-ref} the package had in
 * the embedded SBOM (its package URL), so that it's unique (the same package can be bundled in several JARs) and
 * stable from one release to the next.
 *
 * @version $Id$
 */
public class SBOMEnricher
{
    private static final String SBOM_DIRECTORY = "META-INF/sbom/";

    private static final String CONTAINED_SEPARATOR = "!/";

    private static final String METADATA = "metadata";

    private static final String COMPONENT = "component";

    private static final String COMPONENTS = "components";

    private static final String DEPENDENCIES = "dependencies";

    private static final String DEPENDS_ON = "dependsOn";

    private static final String REF = "ref";

    private static final String BOM_REF = "bom-ref";

    private static final String PURL = "purl";

    private static final String NAME = "name";

    private static final String VERSION = "version";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectNode bom;

    private final WebjarNpmResolver webjarResolver;

    private final Log log;

    private final ArrayNode bomComponents;

    private final ArrayNode bomDependencies;

    private final Set<String> componentRefs = new HashSet<>();

    private final Map<String, ObjectNode> dependenciesByRef = new LinkedHashMap<>();

    /**
     * @param bom the SBOM to complete
     * @param webjarResolver finds the npm package repackaged by the third-party webjars
     * @param log where to warn about what can't be added to the SBOM
     */
    public SBOMEnricher(ObjectNode bom, WebjarNpmResolver webjarResolver, Log log)
    {
        this.bom = bom;
        this.webjarResolver = webjarResolver;
        this.log = log;
        this.bomComponents = bom.withArrayProperty(COMPONENTS);
        this.bomDependencies = bom.withArrayProperty(DEPENDENCIES);
        this.bomComponents.forEach(component -> this.componentRefs.add(component.path(BOM_REF).asText()));
        this.bomDependencies.forEach(dependency -> this.dependenciesByRef.put(dependency.path(REF).asText(),
            (ObjectNode) dependency));
    }

    /**
     * Set the organization supplying and manufacturing the components described by the SBOM, unless the SBOM already
     * names them.
     *
     * @param name the name of the organization
     * @param url the URL of the organization, {@code null} if unknown
     */
    public void setSupplier(String name, String url)
    {
        ObjectNode organization = MAPPER.createObjectNode();
        organization.put(NAME, name);
        if (url != null) {
            organization.putArray("url").add(url);
        }
        ObjectNode metadata = this.bom.withObjectProperty(METADATA);
        metadata.putIfAbsent("supplier", organization);
        metadata.putIfAbsent("manufacturer", organization.deepCopy());
    }

    /**
     * Add the npm packages contained in the Maven components of the SBOM, including the component it describes.
     *
     * @param jarResolver gives the JAR file of a Maven component from its package URL, {@code null} when it's not
     *     available (e.g. it's not a JAR)
     * @throws IOException when failing to read a JAR
     * @throws MalformedPackageURLException when a package URL is invalid
     */
    public void enrich(Function<PackageURL, Path> jarResolver) throws IOException, MalformedPackageURLException
    {
        List<JsonNode> containers = new ArrayList<>();
        JsonNode described = this.bom.path(METADATA).get(COMPONENT);
        if (described != null) {
            containers.add(described);
        }
        // Copy the list since the npm packages found are added to it.
        this.bomComponents.forEach(containers::add);

        for (JsonNode container : containers) {
            enrich(container, jarResolver);
        }
    }

    private void enrich(JsonNode container, Function<PackageURL, Path> jarResolver)
        throws IOException, MalformedPackageURLException
    {
        String ref = container.path(BOM_REF).asText(null);
        String purl = container.path(PURL).asText(null);
        if (ref == null || purl == null) {
            return;
        }
        PackageURL mavenPURL = new PackageURL(purl);
        if (!PackageURL.StandardTypes.MAVEN.equals(mavenPURL.getType())) {
            return;
        }

        Path jar = jarResolver.apply(mavenPURL);
        if (jar != null) {
            addEmbeddedSBOMs(ref, jar);
        }

        PackageURL npmPURL = this.webjarResolver.resolve(mavenPURL, jar);
        if (npmPURL != null) {
            ObjectNode npmPackage = MAPPER.createObjectNode();
            npmPackage.put("type", "library");
            if (npmPURL.getNamespace() != null) {
                npmPackage.put("group", npmPURL.getNamespace());
            }
            npmPackage.put(NAME, npmPURL.getName());
            npmPackage.put(VERSION, npmPURL.getVersion());
            npmPackage.put(PURL, npmPURL.canonicalize());
            npmPackage.put(BOM_REF, ref + CONTAINED_SEPARATOR + npmPURL.canonicalize());
            addContainedComponent(ref, npmPackage);
        }
    }

    private void addEmbeddedSBOMs(String containerRef, Path jar) throws IOException
    {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            List<? extends ZipEntry> entries = zip.stream().filter(entry -> isEmbeddedSBOM(entry.getName()))
                .sorted(Comparator.comparing(ZipEntry::getName)).toList();
            for (ZipEntry entry : entries) {
                JsonNode embeddedSBOM;
                try (InputStream stream = zip.getInputStream(entry)) {
                    embeddedSBOM = MAPPER.readTree(stream);
                }
                if ("CycloneDX".equals(embeddedSBOM.path("bomFormat").asText())) {
                    addEmbeddedSBOM(containerRef, embeddedSBOM);
                } else {
                    this.log.warn(String.format("Ignoring [%s] in [%s] since it's not a CycloneDX SBOM.",
                        entry.getName(), jar));
                }
            }
        }
    }

    private boolean isEmbeddedSBOM(String entryName)
    {
        return entryName.startsWith(SBOM_DIRECTORY) && entryName.endsWith(".json")
            && entryName.indexOf('/', SBOM_DIRECTORY.length()) < 0;
    }

    private void addEmbeddedSBOM(String containerRef, JsonNode embeddedSBOM)
    {
        // The component the embedded SBOM describes is the content of the JAR, so the JAR itself.
        Map<String, String> refs = new HashMap<>();
        String describedRef = embeddedSBOM.path(METADATA).path(COMPONENT).path(BOM_REF).asText(null);
        if (describedRef != null) {
            refs.put(describedRef, containerRef);
        }

        List<JsonNode> embeddedComponents = new ArrayList<>();
        flatten(embeddedSBOM.path(COMPONENTS), embeddedComponents);
        for (JsonNode embeddedComponent : embeddedComponents) {
            String embeddedRef = getRef(embeddedComponent);
            String ref = containerRef + CONTAINED_SEPARATOR + embeddedRef;
            refs.put(embeddedRef, ref);
            ObjectNode component = embeddedComponent.deepCopy();
            component.remove(COMPONENTS);
            component.put(BOM_REF, ref);
            addContainedComponent(containerRef, component);
        }

        // Keep the dependencies between the bundled packages.
        for (JsonNode dependency : embeddedSBOM.path(DEPENDENCIES)) {
            String ref = refs.get(dependency.path(REF).asText());
            if (ref != null) {
                for (JsonNode dependsOn : dependency.path(DEPENDS_ON)) {
                    String dependsOnRef = refs.get(dependsOn.asText());
                    if (dependsOnRef != null && !dependsOnRef.equals(ref)) {
                        addDependsOn(ref, dependsOnRef);
                    }
                }
            }
        }
    }

    private void flatten(JsonNode embeddedComponents, List<JsonNode> flattened)
    {
        for (JsonNode embeddedComponent : embeddedComponents) {
            flattened.add(embeddedComponent);
            flatten(embeddedComponent.path(COMPONENTS), flattened);
        }
    }

    private String getRef(JsonNode component)
    {
        String ref = component.path(BOM_REF).asText(null);
        if (ref == null) {
            ref = component.path(PURL).asText(null);
        }
        if (ref == null) {
            ref = component.path(NAME).asText() + '@' + component.path(VERSION).asText();
        }

        return ref;
    }

    private void addContainedComponent(String containerRef, ObjectNode component)
    {
        String ref = component.get(BOM_REF).asText();
        if (this.componentRefs.add(ref)) {
            this.bomComponents.add(component);
        }
        addDependsOn(containerRef, ref);
    }

    private void addDependsOn(String ref, String dependsOnRef)
    {
        ObjectNode dependency = this.dependenciesByRef.computeIfAbsent(ref, key -> {
            ObjectNode newDependency = this.bomDependencies.addObject();
            newDependency.put(REF, key);
            return newDependency;
        });
        ArrayNode dependsOn = dependency.withArrayProperty(DEPENDS_ON);
        for (JsonNode existing : dependsOn) {
            if (existing.asText().equals(dependsOnRef)) {
                return;
            }
        }
        dependsOn.add(dependsOnRef);
    }
}
