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

import java.util.Arrays;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the Verify Mojo.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class VerifyMojoTest extends AbstractMojoTest
{
    @Test
    void executeWithWrongAuthor() throws Exception
    {
        verifyExecution("/wrongAuthor", "Verifying [Space/WebHome.xml]... errors",
            "- Effective author must be [xwiki:XWiki.Admin] but was [wrongAuthor]", "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithWrongAttachmentAuthorsAndMimetypes() throws Exception
    {
        verifyExecution("/wrongAttachmentAuthorsAndMimetypes", "Verifying [Space/WebHome.xml]... errors",
            "- Attachment author must be [xwiki:XWiki.Admin] but was []",
            "- Attachment author must be [xwiki:XWiki.Admin] but was [wrong author]",
            "- Missing mimetype for attachment [applications.png]",
            "- Missing mimetype for attachment [configuration.png]");
    }

    @Test
    void executeWithWrongContentAuthor() throws Exception
    {
        verifyExecution("/wrongContentAuthor", "Verifying [Space/WebHome.xml]... errors",
            "- Content Author must be [xwiki:XWiki.Admin] but was [wrongContentAuthor]",
            "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithWrongCreator() throws Exception
    {
        verifyExecution("/wrongCreator", "Verifying [Space/WebHome.xml]... errors",
            "- Creator must be [xwiki:XWiki.Admin] but was [wrongCreator]", "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithEmptyParent() throws Exception
    {
        verifyExecution("/emptyParent", "Verifying [Space/WebHome.xml]... errors", "- Parent must not be empty",
            "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithWrongVersion() throws Exception
    {
        verifyExecution("/wrongVersion", "Verifying [Space/WebHome.xml]... errors",
            "- Version must be [1.1] but was [1.2]", "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithNotEmptyComment() throws Exception
    {
        verifyExecution("/notEmptyComment", "Verifying [Space/WebHome.xml]... errors",
            "- Comment must be empty but was [notempty]", "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithWrongMinorEdit() throws Exception
    {
        verifyExecution("/wrongMinorEdit", "Verifying [Space/WebHome.xml]... errors",
            "- Minor edit must always be [false] but was [true]", "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithVisibleTechnicalPages() throws Exception
    {
        Verifier verifier = createVerifier("/visibleTechnicalPages");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    void executeWithNotEmptyDefaultLanguage() throws Exception
    {
        verifyExecution("/notEmptyDefaultLanguage", "Verifying [Space/WebHome.xml]... errors",
            "- Default Language should have been [] but was [en]", "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithWrongEncoding() throws Exception
    {
        verifyExecution("/wrongEncoding", "Verifying [Space/WebHome.xml]... errors",
            "- Encoding must be [UTF-8] but was [ISO-8859-1]", "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithWrongPageTitle() throws Exception
    {
        verifyExecution("/wrongPageTitle", "Verifying [Space/WebPreferences.xml]... errors",
            "- [WebPreferences.xml] ([Space.WebPreferences]) page must have a title matching regex "
                + "[\\$services\\.localization\\.render\\('admin.preferences.title'\\)]",
            "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithMissingLicenseHeader() throws Exception
    {
        Verifier verifier = createVerifier("/missingLicense");
        verifier.addCliOption("-Dxar.formatLicense=true");
        verifier.addCliOption("-Dxar.commons.version=" + System.getProperty("commons.version"));
        verifyExecution(verifier, "Missing header in");
    }

    @Test
    void executeWithTranslationPages() throws Exception
    {
        Verifier verifier = createVerifier("/translations");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    void executeWithTranslationOverrides() throws Exception
    {
        Verifier verifier = createVerifier("/translationOverrides");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    void executeWithWrongTranslationPages() throws Exception
    {
        // @formatter:off
        verifyExecution("/wrongTranslations",
            "Verifying [Main/SomeTranslations.xml]... errors",
            "- Technical documents must be hidden",
            "Verifying [Main/Translations.xml]... errors",
            "- Default Language should have been [en] but was []");
        // @formatter:on
    }

    @Test
    void executeWhenContentPages() throws Exception
    {
        Verifier verifier = createVerifier("/contentPages");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    void executeNestedSpaces() throws Exception
    {
        Verifier verifier = createVerifier("/nestedSpaces");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    void executeWithWrongSyntaxForTranslations() throws Exception
    {
        verifyExecution("/wrongSyntaxForTranslations", "Verifying [Space/WatchListTranslations.xml]... errors",
            "- [WatchListTranslations.xml] ([Space.WatchListTranslations]) page must use a [plain/1.0] syntax",
            "There are errors in the XAR XML files!");
    }

    @Test
    void executeWithWrongVisibilityForTranslations() throws Exception
    {
        verifyExecution("/wrongVisibilityForTranslations", "Verifying [Space/UserTranslations.xml]... errors",
            "- [UserTranslations.xml] ([Space.UserTranslations]) page contains a translation using a wrong "
                + "visibility [USER]. Consider using a [WIKI] visibility.",
            "Verifying [Space/GlobalTranslations.xml]... errors",
            "- [GlobalTranslations.xml] ([Space.GlobalTranslations]) page contains a translation using a wrong "
                + "visibility [GLOBAL]. Consider using a [WIKI] visibility.",
            "There are errors in the XAR XML files!");
    }

    private void verifyExecution(Verifier verifier, String... messages)
    {
        Throwable exception = assertThrows(VerificationException.class, () -> {
            verifier.executeGoal("install");
            verifier.verifyErrorFreeLog();
        });
        for (String message : messages) {
            assertThat(exception.getMessage(), CoreMatchers.containsString(message));
        }
    }

    private void verifyExecution(String testDirectory, String... messages) throws Exception
    {
        verifyExecution(createVerifier(testDirectory), messages);
    }

    @Test
    void executeWithDatesPresent() throws Exception
    {
        // @formatter:off
        verifyExecution("/datesPresent",
            "Verifying [Space/WebHome.xml]... errors",
            "- 'date' field is present",
            "- 'contentUpdateDate' field is present",
            "- 'creationDate' field is present",
            "Verifying [Space/Test.xml]... errors",
            "- 'date' field is present",
            "- 'contentUpdateDate' field is present",
            "- 'creationDate' field is present",
            "There are errors in the XAR XML files!");
        // @formatter:on
    }

    @Test
    void executeWithSkippedDatesCheck() throws Exception
    {
        Verifier verifier = createVerifier("/datesPresent");
        verifier.addCliOption("-Dxar.dates.skip=true");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    void executeWithSkippedDatesCheckDocument() throws Exception
    {
        Verifier verifier = createVerifier("/datesPresent");
        verifier.addCliOption("-Dxar.dates.skip.documentList=Space.WebHome,Space.Test");
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();
    }

    @Test
    void invalidXml() throws Exception
    {
        Verifier verifier = createVerifier("/invalidContent");
        assertThrows(VerificationException.class, () -> {
            verifier.executeGoals(Arrays.asList("clean", "package"));
        });
        verifier.verifyTextInLog("Unexpected non-text content found in element [content]");
    }
}
