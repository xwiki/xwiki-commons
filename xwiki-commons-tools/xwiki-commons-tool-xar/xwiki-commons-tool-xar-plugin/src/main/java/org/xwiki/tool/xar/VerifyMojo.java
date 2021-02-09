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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.xwiki.tool.xar.internal.XWikiDocument;

/**
 * Perform various verifications of the XAR files in this project. Namely:
 * <ul>
 *   <li>ensure that the encoding is UTF8</li>
 *   <li>(optional) ensure that all pages have a parent (except for {@code Main.WebHome})</li>
 *   <li>ensure that the author/contentAuthor/creator/attachment authors are {@code xwiki:XWiki.Admin}</li>
 *   <li>ensure that the version is {@code 1.1}</li>
 *   <li>ensure that comment is empty</li>
 *   <li>ensure that minor edit is false</li>
 *   <li>ensure that technical pages are hidden. We consider by default that all pages are technical unless specified
 *     otherwise</li>
 *   <li>ensure that the default language is set properly</li>
 *   <li>ensure titles follow any defined rules (when defined by the user)</li>
 *   <li>ensure that Translations pages are using the plain/1.0 syntax</li>
 *   <li>ensure that Translations pages don't have a GLOBAL or USER visibility (USER makes no sense and GLOBAL would
 *       require Programming Rights, which is an issue in farm-based use cases)</li>
 *   <li>ensure that translated pages don't contain any attachment</li>
 *   <li>ensure that translated pages don't contain any object</li>
 *   <li>ensure that attachments have a mimetype set. If the mimetype is missing then the attachment won't be
 *       filterable in the attachment view in Page Index.</li>
 * </ul>
 *
 * @version $Id$
 */
@Mojo(
    name = "verify",
    defaultPhase = LifecyclePhase.VERIFY,
    threadSafe = true
)
public class VerifyMojo extends AbstractVerifyMojo
{
    private static final String SYNTAX_PLAIN = "plain/1.0";

    /**
     * Disables the plugin execution.
     *
     * @since 4.3M1
     */
    @Parameter(property = "xar.verify.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Disables the translations visibility check.
     *
     * @since 4.3M1
     */
    @Parameter(property = "xar.verify.translationVisibility.skip", defaultValue = "false")
    private boolean translationVisibilitySkip;

    /**
     * Disables the empty parent check. This check is turned off by default since the parent concept has been
     * replaced by the Nested Spaces one.
     *
     * @since 11.10.3
     * @since 12.0RC1
     */
    @Parameter(property = "xar.verify.emptyParent.skip", defaultValue = "true")
    private boolean emptyParentSkip;

    /**
     * Defines expectations for the Title field of pages.
     *
     * @since 7.3RC1
     */
    @Parameter(property = "xar.verify.titles")
    private Properties titles;

    private Map<Pattern, Pattern> titlePatterns;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (this.skip) {
            return;
        }

        // Only format XAR modules or when forced
        if (!getProject().getPackaging().equals("xar") && !this.force) {
            getLog().info("Not a XAR module, skipping validity check...");
            return;
        }

        getLog().info("Checking validity of XAR XML files...");

        initializePatterns();

        boolean hasErrors = false;
        Collection<File> xmlFiles = getXARXMLFiles();
        for (File file : xmlFiles) {
            String parentName = file.getParentFile().getName();
            XWikiDocument xdoc = getDocFromXML(file);
            List<String> errors = new ArrayList<>();

            // Verification 1: Verify Encoding is UTF8
            if (!xdoc.getEncoding().equals("UTF-8")) {
                errors.add(String.format("Encoding must be [UTF-8] but was [%s]", xdoc.getEncoding()));
            }

            // Verification 2: Verify authors
            verifyAuthor(errors, xdoc.getAuthor(), String.format("Author must be [%s] but was [%s]",
                AUTHOR, xdoc.getAuthor()));
            verifyAuthor(errors, xdoc.getContentAuthor(),
                String.format("Content Author must be [%s] but was [%s]",
                    AUTHOR, xdoc.getContentAuthor()));
            verifyAuthor(errors, xdoc.getCreator(), String.format("Creator must be [%s] but was [%s]",
                AUTHOR, xdoc.getCreator()));
            verifyAttachmentAuthors(errors, xdoc.getAttachmentData());

            // Verification 3: Check for orphans, except for Main.WebHome since it's the topmost document
            if (!this.emptyParentSkip && StringUtils.isEmpty(xdoc.getParent())
                && !xdoc.getReference().equals("Main.WebHome"))
            {
                errors.add("Parent must not be empty");
            }

            // Verification 4: Check for version
            if (!xdoc.getVersion().equals(VERSION)) {
                errors.add(String.format("Version must be [%s] but was [%s]", VERSION, xdoc.getVersion()));
            }

            // Verification 5: Check for empty comment
            if (!StringUtils.isEmpty(xdoc.getComment())) {
                errors.add(String.format("Comment must be empty but was [%s]", xdoc.getComment()));
            }

            // Verification 6: Check for minor edit is always "false"
            if (!xdoc.getMinorEdit().equals("false")) {
                errors.add(String.format("Minor edit must always be [false] but was [%s]", xdoc.getMinorEdit()));
            }

            // Verification 7: Check the default language value
            String expectedDefaultLanguage = guessDefaultLanguage(file, xmlFiles);
            if (!xdoc.getDefaultLanguage().equals(expectedDefaultLanguage)) {
                errors.add(String.format("Default Language should have been [%s] but was [%s]", expectedDefaultLanguage,
                    xdoc.getDefaultLanguage()));
            }

            // Verification 8: Verify that all technical pages are hidden (except for visible technical pages).
            if (!isContentPage(file.getPath()) && !isVisibleTechnicalPage(file.getPath()) && !xdoc.isHidden()) {
                errors.add("Technical documents must be hidden");
            }

            // Verification 9: Verify that the current document has a matching title (if a rule is defined for it)
            if (!isTitlesMatching(xdoc.getReference(), xdoc.getTitle())) {
                errors.add(String.format("[%s] ([%s]) page must have a title matching regex [%s]",
                    file.getName(), xdoc.getReference(), getTitlePatternRuleforPage(xdoc.getReference())));
            }

            // Verification 10: Verify that Translations documents are using the plain/1.0 syntax
            if (xdoc.containsTranslations() && !xdoc.getSyntaxId().equals(SYNTAX_PLAIN)) {
                errors.add(String.format("[%s] ([%s]) page must use a [%s] syntax", file.getName(),
                    xdoc.getReference(), SYNTAX_PLAIN));
            }

            // Verification 11: Verify that Translations documents don't use GLOBAL or USER visibility
            if (!translationVisibilitySkip && xdoc.containsTranslations()) {
                for (String visibility : xdoc.getTranslationVisibilities()) {
                    if (visibility.equals("USER") || visibility.equals("GLOBAL")) {
                        errors.add(String.format("[%s] ([%s]) page contains a translation using a wrong visibility "
                            + "[%s]. Consider using a [WIKI] visibility.", file.getName(), xdoc.getReference(),
                            visibility));
                    }
                }
            }

            // Verification 12: Translated pages don't contain any attachment
            if (StringUtils.isNotEmpty(xdoc.getLocale()) && xdoc.isAttachmentPresent()) {
                errors.add(String.format("[%s] ([%s]) translated page contains attachment(s)", file.getName(),
                    xdoc.getReference()));
            }

            // Verification 13: Translated pages don't contain any object
            if (StringUtils.isNotEmpty(xdoc.getLocale()) && xdoc.isObjectPresent()) {
                errors.add(String.format("[%s] ([%s]) translated page contains object(s)", file.getName(),
                    xdoc.getReference()));
            }

            // Verification 14: Verify that  attachments have a mimetype set.
            verifyAttachmentMimetypes(errors, xdoc.getAttachmentData());

            // Verification 15: Verify that date fields are not set.
            if (!skipDates) {
                verifyDateFields(errors, xdoc);
            }

            // Display errors
            if (errors.isEmpty()) {
                getLog().info(String.format("  Verifying [%s/%s]... ok", parentName, file.getName()));
            } else {
                getLog().info(String.format("  Verifying [%s/%s]... errors", parentName, file.getName()));
                for (String error : errors) {
                    getLog().info(String.format("  - %s", error));
                }
                hasErrors = true;
            }
        }

        if (hasErrors) {
            throw new MojoFailureException("There are errors in the XAR XML files!");
        }

        // Check license headers
        if (this.formatLicense) {
            getLog().info("Checking for missing XAR XML license headers...");
            executeLicenseGoal("check");
        }
    }

    @Override
    protected void initializePatterns()
    {
        super.initializePatterns();

        // Transform title expectations into Patterns
        Map<Pattern, Pattern> patterns = new HashMap<>();
        for (String key : this.titles.stringPropertyNames()) {
            patterns.put(Pattern.compile(key), Pattern.compile(this.titles.getProperty(key)));
        }
        this.titlePatterns = patterns;
    }

    private String getTitlePatternRuleforPage(String documentReference)
    {
        for (Map.Entry<Pattern, Pattern> entry : this.titlePatterns.entrySet()) {
            if (entry.getKey().matcher(documentReference).matches()) {
                return entry.getValue().pattern();
            }
        }
        return null;
    }

    private void verifyDateFields(List<String> errors, XWikiDocument xdoc)
    {
        if (!skipDatesDocumentList.contains(xdoc.getReference())) {
            if (xdoc.isDatePresent()) {
                errors.add("'date' field is present");
            }

            if (xdoc.isContentUpdateDatePresent()) {
                errors.add("'contentUpdateDate' field is present");
            }

            if (xdoc.isCreationDatePresent()) {
                errors.add("'creationDate' field is present");
            }

            if (xdoc.isAttachmentDatePresent()) {
                errors.add("'attachment/date' field is present");
            }
        }
    }

    private void verifyAuthor(List<String> errors, String author, String message)
    {
        if (!AUTHOR.equals(author)) {
            errors.add(message);
        }
    }

    private void verifyAttachmentAuthors(List<String> errors, List<Map<String, String>> attachmentData)
    {
        for (Map<String, String> data : attachmentData) {
            String author = data.get("author");
            verifyAuthor(errors, author, String.format("Attachment author must be [%s] but was [%s]", AUTHOR, author));
        }
    }

    private void verifyAttachmentMimetypes(List<String> errors, List<Map<String, String>> attachmentData)
    {
        for (Map<String, String> data : attachmentData) {
            String mimetype = data.get("mimetype");
            if (mimetype == null) {
                errors.add(String.format("Missing mimetype for attachment [%s]", data.get("filename")));
            }
        }
    }

    private boolean isTitlesMatching(String documentReference, String title)
    {
        for (Map.Entry<Pattern, Pattern> entry : this.titlePatterns.entrySet()) {
            if (entry.getKey().matcher(documentReference).matches() && !entry.getValue().matcher(title).matches()) {
                return false;
            }
        }
        return true;
    }

}
