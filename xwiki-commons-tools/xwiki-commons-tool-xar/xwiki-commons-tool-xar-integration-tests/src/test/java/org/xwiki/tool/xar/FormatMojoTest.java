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
 * @version $Id$
 * @since 4.0M2
 */
public class FormatMojoTest
{
    @Test
    public void formatWhenNoStyle() throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/format");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.addCliOption("-Dforce=true");
        verifier.addCliOption("-Dpretty=false");
        verifier.executeGoal("xar:format");
        verifier.verifyErrorFreeLog();

        String content = FileUtils.fileRead(new File(testDir, "src/main/resources/NoStyle/Page1.xml"));
        String expected = FileUtils.fileRead(new File(testDir, "ExpectedNoStylePage1.xml"));
        Assert.assertEquals(expected, content);

        // Test with a XML file having a license header
        content = FileUtils.fileRead(new File(testDir, "src/main/resources/NoStyle/Page2.xml"));
        expected = FileUtils.fileRead(new File(testDir, "ExpectedNoStylePage2.xml"));
        Assert.assertEquals(expected, content);
    }

    @Test
    public void formatWhenPrettyPrinting() throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/format");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.addCliOption("-Dforce=true");
        verifier.executeGoal("xar:format");
        verifier.verifyErrorFreeLog();

        String content = FileUtils.fileRead(new File(testDir, "src/main/resources/Pretty/Page1.xml"));
        String expected = FileUtils.fileRead(new File(testDir, "ExpectedPrettyPage1.xml"));
        Assert.assertEquals(expected, content);

        // Test with a XML file having a license header
        content = FileUtils.fileRead(new File(testDir, "src/main/resources/Pretty/Page2.xml"));
        expected = FileUtils.fileRead(new File(testDir, "ExpectedPrettyPage2.xml"));
        Assert.assertEquals(expected, content);
    }
}
