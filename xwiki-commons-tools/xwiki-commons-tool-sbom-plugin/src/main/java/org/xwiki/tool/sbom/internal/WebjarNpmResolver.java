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
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

/**
 * Finds the npm package repackaged by a third-party webjar, so that vulnerability scanners can match it against the
 * npm advisories: they don't know that {@code org.webjars:jquery} is the {@code jquery} npm package.
 *
 * @version $Id$
 */
public class WebjarNpmResolver
{
    private static final String WEBJARS_GROUP = "org.webjars";

    private static final String NPM_WEBJARS_GROUP = "org.webjars.npm";

    private static final String NPM_TYPE = "npm";

    private static final String SCOPE_PREFIX = "@";

    private static final String SCOPE_SEPARATOR = "/";

    private static final String NAME = "name";

    private static final String VERSION = "version";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Properties classicWebjars = new Properties();

    private final Log log;

    /**
     * @param log where to warn about the webjars for which no npm package is known
     * @throws IOException when failing to read the mapping table of the classic webjars
     */
    public WebjarNpmResolver(Log log) throws IOException
    {
        this.log = log;
        try (InputStream stream = getClass().getResourceAsStream("/webjars-npm.properties")) {
            this.classicWebjars.load(stream);
        }
    }

    /**
     * @param mavenPURL the Maven package URL of the artifact
     * @param jar the artifact file, {@code null} when it's not available
     * @return the npm package URL of the package repackaged by the webjar, {@code null} if the artifact is not a webjar
     *         or if no npm package is known for it
     * @throws IOException when failing to read the webjar
     * @throws MalformedPackageURLException when the npm package URL can't be built
     */
    public PackageURL resolve(PackageURL mavenPURL, Path jar) throws IOException, MalformedPackageURLException
    {
        String group = mavenPURL.getNamespace();
        if (NPM_WEBJARS_GROUP.equals(group)) {
            return resolveNpmWebjar(mavenPURL, jar);
        } else if (WEBJARS_GROUP.equals(group)) {
            return resolveClassicWebjar(mavenPURL);
        } else if (group != null && group.startsWith(WEBJARS_GROUP + '.')) {
            warnUnknown(mavenPURL);
        }

        return null;
    }

    private PackageURL resolveNpmWebjar(PackageURL mavenPURL, Path jar)
        throws IOException, MalformedPackageURLException
    {
        // The webjar artifact id is the npm name, with "__" replacing the "/" after the scope, e.g. "vue__compiler-sfc"
        // for "@vue/compiler-sfc". Its package.json gives the exact name and version, when it's there.
        String name = mavenPURL.getName().replace("__", SCOPE_SEPARATOR);
        if (name.contains(SCOPE_SEPARATOR)) {
            name = SCOPE_PREFIX + name;
        }
        String version = mavenPURL.getVersion();
        if (jar != null) {
            JsonNode packageJSON = readPackageJSON(mavenPURL, jar);
            if (packageJSON != null && packageJSON.hasNonNull(NAME) && packageJSON.hasNonNull(VERSION)) {
                name = packageJSON.get(NAME).asText();
                version = packageJSON.get(VERSION).asText();
            }
        }

        return createNpmPURL(name, version);
    }

    private PackageURL resolveClassicWebjar(PackageURL mavenPURL) throws MalformedPackageURLException
    {
        String name = this.classicWebjars.getProperty(mavenPURL.getName());
        if (name == null) {
            warnUnknown(mavenPURL);

            return null;
        } else if (name.isEmpty()) {
            return null;
        }

        // A classic webjar has the version of the library it repackages, followed by "-<n>" when it had to be
        // published again (e.g. 0.2.3-2 for jquery-ui-touch-punch 0.2.3).
        String version = mavenPURL.getVersion().replaceFirst("^(\\d+\\.\\d+\\.\\d+)-\\d+$", "$1");

        return createNpmPURL(name, version);
    }

    private JsonNode readPackageJSON(PackageURL mavenPURL, Path jar) throws IOException
    {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(String.format("META-INF/resources/webjars/%s/%s/package.json",
                mavenPURL.getName(), mavenPURL.getVersion()));
            if (entry == null) {
                return null;
            }
            try (InputStream stream = zip.getInputStream(entry)) {
                return MAPPER.readTree(stream);
            }
        }
    }

    private PackageURL createNpmPURL(String name, String version) throws MalformedPackageURLException
    {
        int slash = name.indexOf(SCOPE_SEPARATOR);
        if (name.startsWith(SCOPE_PREFIX) && slash > 0) {
            return new PackageURL(NPM_TYPE, name.substring(0, slash), name.substring(slash + 1), version, null, null);
        }

        return new PackageURL(NPM_TYPE, null, name, version, null, null);
    }

    private void warnUnknown(PackageURL mavenPURL)
    {
        this.log.warn(String.format("No npm package is known for the webjar [%s], so vulnerability scanners won't match"
            + " it against the npm advisories. Add it to webjars-npm.properties in xwiki-commons-tool-sbom-plugin.",
            mavenPURL.canonicalize()));
    }
}
