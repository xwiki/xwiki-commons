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

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.packageurl.PackageURL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link WebjarNpmResolver}.
 *
 * @version $Id$
 */
class WebjarNpmResolverTest
{
    @TempDir
    private Path tmpDir;

    private Log log;

    private WebjarNpmResolver resolver;

    @BeforeEach
    void setUp() throws Exception
    {
        this.log = mock(Log.class);
        this.resolver = new WebjarNpmResolver(this.log);
    }

    @Test
    void resolveNpmWebjarFromPackageJSON() throws Exception
    {
        Path jar = this.tmpDir.resolve("core-base.jar");
        try (OutputStream stream = Files.newOutputStream(jar); ZipOutputStream zip = new ZipOutputStream(stream)) {
            zip.putNextEntry(new ZipEntry("META-INF/resources/webjars/intlify__core-base/11.4.4-1/package.json"));
            zip.write("{\"name\": \"@intlify/core-base\", \"version\": \"11.4.4\"}".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        assertEquals("pkg:npm/%40intlify/core-base@11.4.4",
            resolve("pkg:maven/org.webjars.npm/intlify__core-base@11.4.4-1?type=jar", jar));
    }

    @Test
    void resolveNpmWebjarWithoutPackageJSON() throws Exception
    {
        assertEquals("pkg:npm/%40vue/compiler-sfc@3.5.0",
            resolve("pkg:maven/org.webjars.npm/vue__compiler-sfc@3.5.0?type=jar", null));
        assertEquals("pkg:npm/dompurify@3.4.9", resolve("pkg:maven/org.webjars.npm/dompurify@3.4.9?type=jar", null));
    }

    @Test
    void resolveClassicWebjar() throws Exception
    {
        assertEquals("pkg:npm/moment@2.30.1", resolve("pkg:maven/org.webjars/momentjs@2.30.1?type=jar", null));
        assertEquals("pkg:npm/jquery-ui-touch-punch@0.2.3",
            resolve("pkg:maven/org.webjars/jquery-ui-touch-punch@0.2.3-2?type=jar", null));
        assertEquals("pkg:npm/%40fortawesome/fontawesome-free@7.3.0",
            resolve("pkg:maven/org.webjars/font-awesome@7.3.0?type=jar", null));
        assertNull(resolve("pkg:maven/org.webjars/webjars-locator-core@0.59?type=jar", null));
        verify(this.log, never()).warn(anyString());
    }

    @Test
    void resolveUnknownWebjars() throws Exception
    {
        assertNull(resolve("pkg:maven/org.webjars/unknown@1.0.0?type=jar", null));
        assertNull(resolve("pkg:maven/org.webjars.bower/other@1.0.0?type=jar", null));

        verify(this.log).warn("No npm package is known for the webjar [pkg:maven/org.webjars/unknown@1.0.0?type=jar],"
            + " so vulnerability scanners won't match it against the npm advisories. Add it to webjars-npm.properties"
            + " in xwiki-commons-tool-sbom-plugin.");
        verify(this.log).warn("No npm package is known for the webjar "
            + "[pkg:maven/org.webjars.bower/other@1.0.0?type=jar], so vulnerability scanners won't match it against the"
            + " npm advisories. Add it to webjars-npm.properties in xwiki-commons-tool-sbom-plugin.");
    }

    @Test
    void resolveNotWebjar() throws Exception
    {
        assertNull(resolve("pkg:maven/org.xwiki.commons/xwiki-commons-text@18.10.0?type=jar", null));
        verify(this.log, never()).warn(anyString());
    }

    private String resolve(String mavenPURL, Path jar) throws Exception
    {
        PackageURL npmPURL = this.resolver.resolve(new PackageURL(mavenPURL), jar);

        return npmPURL != null ? npmPURL.canonicalize() : null;
    }
}
