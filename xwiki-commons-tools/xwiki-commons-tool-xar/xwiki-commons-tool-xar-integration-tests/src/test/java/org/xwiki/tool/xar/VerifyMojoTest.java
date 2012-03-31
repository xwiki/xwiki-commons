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

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Integration tests for the Verify Mojo.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class VerifyMojoTest
{
    @Test
    public void executeWithWrongAuthor() throws Exception
    {
        verifyExecution("/wrongAuthor",
            "[WebHome.xml]: Author must be [xwiki:XWiki.Admin] but was [wrongAuthor]");
    }

    @Test
    public void executeWithWrongContentAuthor() throws Exception
    {
        verifyExecution("/wrongContentAuthor",
            "[WebHome.xml]: Content Author must be [xwiki:XWiki.Admin] but was [wrongContentAuthor]");
    }

    @Test
    public void executeWithWrongCreator() throws Exception
    {
        verifyExecution("/wrongCreator",
            "[WebHome.xml]: Creator must be [xwiki:XWiki.Admin] but was [wrongCreator]");
    }

    @Test
    public void executeWithEmptyParent() throws Exception
    {
        verifyExecution("/emptyParent",
            "[WebHome.xml]: Parent must not be empty");
    }

    @Test
    public void executeWithWrongVersion() throws Exception
    {
        verifyExecution("/wrongVersion",
            "[WebHome.xml]: Version must be [1.1] but was [1.2]");
    }

    @Test
    public void executeOk() throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/allOk");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    private void verifyExecution(String testDirectory, String message) throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), testDirectory);

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");

        try {
            verifier.executeGoal("install");
            verifier.verifyErrorFreeLog();
            Assert.fail("An error should have been thrown in the build");
        } catch (VerificationException expected) {
            Assert.assertTrue(expected.getMessage().contains(message));
        }
    }
}
