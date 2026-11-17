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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

/**
 * Verifies that the CycloneDX SBOM attached to the artifact contains some components and doesn't contain others, so
 * that a regression in its generation is caught (e.g. the npm packages bundled in a webjar missing from the SBOM of a
 * distribution). It must run after the goals generating the SBOM ({@code makeBom} and {@code enrich}). It fails when
 * no SBOM is attached to the artifact, since that's a regression too (e.g. the SBOM generation skipped by mistake).
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CheckMojo extends AbstractSBOMMojo
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PURL = "purl";

    /**
     * The package URLs of the components that the SBOM must contain, usually without version so that dependency
     * upgrades don't break the check (e.g. {@code pkg:npm/vue-i18n}). The qualifiers are ignored.
     */
    @Parameter
    private List<String> expectedComponents = List.of();

    /**
     * The package URLs of the components that the SBOM must not contain, usually without version (e.g.
     * {@code pkg:maven/jakarta.servlet/jakarta.servlet-api}). The qualifiers are ignored.
     */
    @Parameter
    private List<String> forbiddenComponents = List.of();

    @Override
    public void execute() throws MojoExecutionException
    {
        Optional<File> sbomFile = getAttachedSBOM();
        if (sbomFile.isEmpty()) {
            throw new MojoExecutionException("No CycloneDX SBOM in JSON format is attached to the artifact.");
        }

        List<PackageURL> components;
        try {
            components = readComponents(sbomFile.get());
        } catch (IOException | MalformedPackageURLException e) {
            throw new MojoExecutionException(String.format("Failed to read the SBOM [%s]", sbomFile.get()), e);
        }

        List<String> errors = new ArrayList<>();
        List<String> missing = filter(this.expectedComponents, components, false);
        if (!missing.isEmpty()) {
            errors.add(String.format("it doesn't contain the expected components %s", missing));
        }
        List<String> present = filter(this.forbiddenComponents, components, true);
        if (!present.isEmpty()) {
            errors.add(String.format("it contains the forbidden components %s", present));
        }
        if (!errors.isEmpty()) {
            throw new MojoExecutionException(
                String.format("The SBOM [%s] is invalid: %s.", sbomFile.get(), String.join(", and ", errors)));
        }
    }

    private List<PackageURL> readComponents(File sbomFile) throws IOException, MalformedPackageURLException
    {
        List<PackageURL> components = new ArrayList<>();
        for (JsonNode component : MAPPER.readTree(sbomFile).path("components")) {
            if (component.hasNonNull(PURL)) {
                components.add(new PackageURL(component.get(PURL).asText()));
            }
        }

        return components;
    }

    private List<String> filter(List<String> patterns, List<PackageURL> components, boolean contained)
        throws MojoExecutionException
    {
        List<String> result = new ArrayList<>();
        for (String pattern : patterns) {
            PackageURL patternPURL;
            try {
                patternPURL = new PackageURL(pattern);
            } catch (MalformedPackageURLException e) {
                throw new MojoExecutionException(String.format("Invalid package URL [%s]", pattern), e);
            }
            if (components.stream().anyMatch(component -> matches(patternPURL, component)) == contained) {
                result.add(pattern);
            }
        }

        return result;
    }

    private boolean matches(PackageURL pattern, PackageURL component)
    {
        boolean sameVersion = pattern.getVersion() == null || pattern.getVersion().equals(component.getVersion());

        return sameVersion && pattern.getType().equals(component.getType())
            && Objects.equals(pattern.getNamespace(), component.getNamespace())
            && pattern.getName().equals(component.getName());
    }
}
