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

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Integration tests for the Format Mojo.
 *
 * @version $$
 * @since 4.0M2
 */
public class FormatMojoTest
{
    @Test
    public void format() throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/format");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.executeGoal("xar:format");
        verifier.verifyErrorFreeLog();

        String content = FileUtils.fileRead(new File(testDir, "src/main/resources/Space/WebHome.xml"));
        String expected = FileUtils.fileRead(new File(testDir, "ExpectedWebHome.xml"));
        Assert.assertEquals(expected, content);
    }
}
