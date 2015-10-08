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
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.*;

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
        verifyExecution("/wrongAuthor", "Verifying [Space/WebHome.xml]... errors",
            "- Author must be [xwiki:XWiki.Admin] but was [wrongAuthor]", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithWrongAttachmentAuthors() throws Exception
    {
        verifyExecution("/wrongAttachmentAuthors", "Verifying [Space/WebHome.xml]... errors",
            "- Attachment author must [xwiki:XWiki.Admin] but was []",
            "- Attachment author must [xwiki:XWiki.Admin] but was [wrong author]");
    }

    @Test
    public void executeWithWrongContentAuthor() throws Exception
    {
        verifyExecution("/wrongContentAuthor", "Verifying [Space/WebHome.xml]... errors",
            "- Content Author must be [xwiki:XWiki.Admin] but was [wrongContentAuthor]",
            "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithWrongCreator() throws Exception
    {
        verifyExecution("/wrongCreator", "Verifying [Space/WebHome.xml]... errors",
            "- Creator must be [xwiki:XWiki.Admin] but was [wrongCreator]", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithEmptyParent() throws Exception
    {
        verifyExecution("/emptyParent", "Verifying [Space/WebHome.xml]... errors", "- Parent must not be empty",
            "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithWrongVersion() throws Exception
    {
        verifyExecution("/wrongVersion", "Verifying [Space/WebHome.xml]... errors",
            "- Version must be [1.1] but was [1.2]", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithNotEmptyComment() throws Exception
    {
        verifyExecution("/notEmptyComment", "Verifying [Space/WebHome.xml]... errors",
            "- Comment must be empty but was [notempty]", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithWrongMinorEdit() throws Exception
    {
        verifyExecution("/wrongMinorEdit", "Verifying [Space/WebHome.xml]... errors",
            "- Minor edit must always be [false] but was [true]", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithNotEmptyDefaultLanguage() throws Exception
    {
        verifyExecution("/notEmptyDefaultLanguage", "Verifying [Space/WebHome.xml]... errors",
            "- Default Language should have been [] but was [en]", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithWrongEncoding() throws Exception
    {
        verifyExecution("/wrongEncoding", "Verifying [Space/WebHome.xml]... errors",
            "- Encoding must be [UTF-8] but was [ISO-8859-1]", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithNotHiddenTranslation() throws Exception
    {
        verifyExecution("/notHiddenTranslation", "Verifying [Space/Translations.xml]... errors",
            "- Translations documents should be hidden", "There are errors in the XAR XML files!");
    }

    @Test
    public void executeWithMissingLicenseHeader() throws Exception
    {
        Verifier verifier = createVerifier("/missingLicense");
        verifier.addCliOption("-DformatLicense=true");
        verifier.addCliOption("-Dcommons.version=" + System.getProperty("commons.version"));
        verifyExecution(verifier, "Missing header in");
    }

    @Test
    public void executeWithSpecifiedPages() throws Exception
    {
        File testDir = FixedResourceExtractor.simpleExtractResources(getClass(), "/specifiedPages");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.addCliOption("-Dforce=true");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    public void executeOk() throws Exception
    {
        File testDir = FixedResourceExtractor.simpleExtractResources(getClass(), "/allOk");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.addCliOption("-Dforce=true");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    public void executeNestedSpaces() throws Exception
    {
        File testDir = FixedResourceExtractor.simpleExtractResources(getClass(), "/nestedSpaces");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");
        verifier.addCliOption("-Dforce=true");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    private void verifyExecution(Verifier verifier, String... messages) throws Exception
    {
        verifier.deleteArtifact("org.xwiki.commons", "xwiki-commons-tool-xar-plugin-test", "1.0", "pom");

        try {
            verifier.addCliOption("-Dforce=true");
            verifier.executeGoal("install");
            verifier.verifyErrorFreeLog();
            fail("An error should have been thrown in the build");
        } catch (VerificationException expected) {
            for (String message : messages) {
                assertThat(expected.getMessage(), CoreMatchers.containsString(message));
            }
        }
    }

    private void verifyExecution(String testDirectory, String... messages) throws Exception
    {
        verifyExecution(createVerifier(testDirectory), messages);
    }

    private Verifier createVerifier(String testDirectory) throws Exception
    {
        File testDir = FixedResourceExtractor.simpleExtractResources(getClass(), testDirectory);
        return new Verifier(testDir.getAbsolutePath());
    }
}
