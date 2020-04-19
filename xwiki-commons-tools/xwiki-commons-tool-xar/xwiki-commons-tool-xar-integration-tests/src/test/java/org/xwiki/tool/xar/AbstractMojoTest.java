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
package org.xwiki.tool.xar;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.it.Verifier;
import org.apache.maven.shared.utils.io.FileUtils;

import static org.apache.maven.it.util.ResourceExtractor.extractResourcePath;

/**
 * Commons Test class for Integration tests.
 *
 * @version $Id$
 * @since 7.4M1
 */
public abstract class AbstractMojoTest
{
    protected Verifier createVerifier(String projectName) throws Exception
    {
        File tempDir = new File("target/verifier");
        FileUtils.deleteDirectory(tempDir);
        File testDir = extractResourcePath(getClass(), projectName, tempDir, true);
        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");

        // Add support for Jacoco
        String jacocoDestFile = System.getProperty("xwiki.jacoco.itDestFile");
        if (!StringUtils.isEmpty(jacocoDestFile)) {
            String jacocoVersion = System.getProperty("jacoco.version");
            String jacocoAgentJarPath =
                verifier.getArtifactPath("org.jacoco", "org.jacoco.agent", jacocoVersion, "jar", "runtime");
            verifier.setEnvironmentVariable("MAVEN_OPTS",
                String.format("-javaagent:%s=destfile=%s", jacocoAgentJarPath, jacocoDestFile));
        }

        verifier.addCliOption("-Dxar.force=true");

        return verifier;
    }
}
