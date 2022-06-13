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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResourceCollection;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

/**
 * Common code for the Verify and Format Mojos.
 *
 * @version $Id$
 * @since 4.0M2
 */
public abstract class AbstractVerifyMojo extends AbstractXARMojo
{
    /**
     * Admin author for creator/contentAuthor and author.
     */
    protected static final String AUTHOR = "xwiki:XWiki.Admin";

    /**
     * First version.
     */
    protected static final String VERSION = "1.1";

    private static final String EXTENSION = ".xml";

    private static final Pattern TRANSLATION_PATTERN = Pattern.compile("(.*)\\..*\\.xml");

    /**
     * If true then don't check if the packaging is XAR before running mojos.
     */
    @Parameter(property = "xar.force", readonly = true)
    protected boolean force;

    /**
     * The language in which non-translated documents are written in.
     */
    @Parameter(property = "xar.defaultLanguage", defaultValue = "en")
    protected String defaultLanguage;

    /**
     * If true then check for cliense and add a license header to XML files.
     */
    @Parameter(property = "xar.formatLicense", readonly = true)
    protected boolean formatLicense;

    /**
     * The Commons version to be used by this mojo.
     */
    @Parameter(property = "xar.commons.version")
    protected String commonsVersion;

    /**
     * Disables the check for the existence of the date fields.
     *
     * @since 10.8RC1
     */
    @Parameter(property = "xar.dates.skip", defaultValue = "false")
    protected boolean skipDates;

    /**
     * Disables the check for the existence of the date fields.
     *
     * @since 10.8RC1
     */
    @Parameter(property = "xar.dates.skip.documentList")
    protected Set<String> skipDatesDocumentList;

    /**
     * Disables the check for the existence of the author fields.
     *
     * @since 14.5RC1
     */
    @Parameter(property = "xar.authors.skip", defaultValue = "false")
    protected boolean skipAuthors;

    /**
     * Disables the check for the existence of the author fields.
     *
     * @since 14.5RC1
     */
    @Parameter(property = "xar.authors.skip.documentList")
    protected Set<String> skipAuthorsDocumentList;

    /**
     * Explicitly define a list of pages (it's a regex) that should be considered as content pages (rather than
     * technical pages which is the default). Note that content pages must have a non-empty default language specified.
     *
     * @since 7.1M1
     */
    @Parameter(property = "xar.contentPages")
    protected List<String> contentPages;

    /**
     * Explicitly define a list of pages (it's a regex) that can have translations and thus which must have a default
     * language not empty. If not defined then defaults to considering that {@code *Translations.xml} pages are
     * translated pages.
     *
     * @since 12.3RC1
     */
    @Parameter(property = "xar.translatablePages", defaultValue = ".*/.*Translations\\.xml")
    protected List<String> translatablePages;

    /**
     * Explicitly define a list of pages (it's a regex) that are technical pages but that should be visible (not
     * hidden). For example, home pages of applications. These pages must have their default langue set to empty so
     * that a search in a given language doesn't return them as they're not content.
     *
     * @since 12.3RC1
     */
    @Parameter(property = "xar.visibleTechnicalPages")
    protected List<String> visibleTechnicalPages;

    /**
     * The current Maven session.
     */
    @Parameter(property = "session", required = true, readonly = true)
    private MavenSession mavenSession;

    /**
     * The Maven BuildPluginManager component.
     */
    @Component
    private BuildPluginManager pluginManager;

    private List<Pattern> contentPagePatterns;

    private List<Pattern> translatablePagePatterns;

    private List<Pattern> visibleTechnicalPagePatterns;

    /**
     * Initialize regex Patterns for performance reasons.
     */
    protected void initializePatterns()
    {
        this.contentPagePatterns = initializationPagePatterns(this.contentPages);
        this.translatablePagePatterns = initializationPagePatterns(this.translatablePages);
        this.visibleTechnicalPagePatterns = initializationPagePatterns(this.visibleTechnicalPages);
    }

    private List<Pattern> initializationPagePatterns(List<String> pageRegexes)
    {
        List<Pattern> patterns = new ArrayList<>();
        if (pageRegexes != null) {
            for (String pageRegex : pageRegexes) {
                patterns.add(Pattern.compile(pageRegex));
            }
        }
        return patterns;
    }

    /**
     * @return the list of XAR XML files in this project
     * @exception MojoFailureException when an error happens reading the XAR XML files on the file system
     */
    protected Collection<File> getXARXMLFiles() throws MojoFailureException
    {
        // Find all files in the resources dir
        File resourcesDir = getResourcesDirectory();

        Collection<File> files = new ArrayList<>();
        if (resourcesDir.exists()) {
            PlexusIoFileResourceCollection collection = new PlexusIoFileResourceCollection();
            collection.setBaseDir(resourcesDir);

            FileSelector[] selectors;

            IncludeExcludeFileSelector fs = new IncludeExcludeFileSelector();
            fs.setIncludes(getIncludes());
            fs.setExcludes(getExcludes());

            IncludeExcludeFileSelector fs2 = new IncludeExcludeFileSelector();
            fs2.setExcludes(new String[] {PACKAGE_XML});
            selectors = new FileSelector[] {fs, fs2};

            collection.setFileSelectors(selectors);

            Iterator<PlexusIoResource> resources;
            try {
                resources = collection.getResources();
            } catch (IOException e) {
                throw new MojoFailureException("Failed to get list of XAR XML files", e);
            }
            while (resources.hasNext()) {
                PlexusIoResource resource = resources.next();
                if (resource.isFile()) {
                    files.add(new File(collection.getBaseDir(), resource.getName()));
                }
            }
        }

        return files;
    }

    /**
     * Guess the {@code &lt;defaultLanguage&gt;} value to use for the passed file using the following algorithm:
     * <ul>
     *   <li>If the page name matches one of the regexes defined by the user as a translated page, then check that
     *     the default language is {@link #defaultLanguage}.</li>
     *   <li>Otherwise, if the page name matches one of the regexes defined by the user as a content page, then check
     *     that the default language is {@link #defaultLanguage}.</li>
     *   <li>Otherwise, if there's no other translation of the file then check that the default language is empty
     *     since it's a technical page.</li>
     *   <li>Otherwise, if there are other translations ("(prefix).(language).xml" format) then check that the default
     *     language should is {@link #defaultLanguage}</li>
     * </ul>
     * @param file the XML file for which to guess the default language that it should have
     * @param xwikiXmlFiles the list of all XML files that is used to check for translations of the passed XML file
     * @return the default language as a string (e.g. "en" for English or "" for an empty default language)
     * @since 5.4.1
     */
    protected String guessDefaultLanguage(File file, Collection<File> xwikiXmlFiles)
    {
        if (isTranslatablePage(file.getPath()) || isContentPage(file.getPath())) {
            return this.defaultLanguage;
        }

        String language = "";

        // Check if the doc is a translation
        Matcher matcher = TRANSLATION_PATTERN.matcher(file.getName());
        if (matcher.matches()) {
            // We're in a translation, use the default language
            language = this.defaultLanguage;
        } else {
            // We're not in a translation, check if there are translations. First get the doc name before the extension
            String prefix = StringUtils.substringBeforeLast(file.getPath(), EXTENSION);
            // Check for a translation now
            Pattern translationPattern = Pattern.compile(String.format("%s\\..*\\.xml", Pattern.quote(prefix)));
            for (File xwikiXmlFile : xwikiXmlFiles) {
                Matcher translationMatcher = translationPattern.matcher(xwikiXmlFile.getPath());
                if (translationMatcher.matches()) {
                    // Found a translation, use the default language
                    language = this.defaultLanguage;
                    break;
                }
            }
        }
        return language;
    }

    protected boolean isContentPage(String filePath)
    {
        return isMatchingPage(filePath, this.contentPagePatterns);
    }

    protected boolean isTranslatablePage(String filePath)
    {
        return isMatchingPage(filePath, this.translatablePagePatterns);
    }

    protected boolean isVisibleTechnicalPage(String filePath)
    {
        return isMatchingPage(filePath, this.visibleTechnicalPagePatterns);
    }

    private boolean isMatchingPage(String filePath, List<Pattern> patterns)
    {
        if (patterns != null) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(filePath).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the version of the XWiki Commons project, either configured in the project using this plugin or taken
     *         from the {@code commons.version} property if defined, defaulting to the current project version if not
     *         defined
     */
    protected String getXWikiCommonsVersion()
    {
        String version = this.commonsVersion;
        if (version == null) {
            version = this.project.getVersion();
        }
        return version;
    }

    /**
     * Executes a goal of the Maven License plugin (used for adding or checking for license headers.
     *
     * @param goal the goal of the Maven License plugin to call
     * @exception MojoExecutionException when the License plugins fails or if it's not found
     */
    protected void executeLicenseGoal(String goal) throws MojoExecutionException
    {
        // Find the license plugin (it's project's responsibility to make sure the License plugin is properly setup in
        // its <pluginManagement>, for most XWiki projects it just mean inherits from xwiki-commons-pom)
        Plugin licensePlugin =
            this.project.getPluginManagement().getPluginsAsMap().get("com.mycila:license-maven-plugin");

        if (licensePlugin == null) {
            throw new MojoExecutionException("License plugin could not be found in <pluginManagement>");
        }

        executeMojo(
            licensePlugin,
            goal(goal),
            configuration(
                element(name("licenseSets"),
                    element(name("licenseSet"),
                        element(name("header"), "license.txt"),
                        element(name("headerDefinitions"),
                            element(name("headerDefinition"), "license-xml-definition.xml")),
                        element(name("includes"),
                            element(name("include"), "src/main/resources/**/*.xml"))
                      )
                  )
            ),
            executionEnvironment(
                this.project,
                this.mavenSession,
                this.pluginManager
            )
        );
    }
}
