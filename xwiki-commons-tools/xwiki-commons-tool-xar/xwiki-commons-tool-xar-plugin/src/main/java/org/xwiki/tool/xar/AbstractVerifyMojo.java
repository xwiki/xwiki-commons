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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    @Parameter(property = "force", readonly = true)
    protected boolean force;

    /**
     * The language in which non-translated documents are written in.
     */
    @Parameter(property = "defaultLanguage", defaultValue = "en")
    protected String defaultLanguage;

    /**
     * If true then add license header to XML files.
     */
    @Parameter(property = "formatLicense", readonly = true)
    protected boolean formatLicense;

    /**
     * The Commons version to be used by this mojo.
     */
    @Parameter(property = "commons.version")
    protected String commonsVersion;

    /**
     * Defines expectations for the Title field of pages.
     *
     * @since 7.3RC1
     */
    @Parameter(property = "xar.verify.titles")
    protected Properties titles;

    /**
     * Disables the check for the existence of the date fields.
     *
     * @since 10.7RC1
     */
    @Parameter(property = "xar.dates.skip", defaultValue = "false")
    protected boolean skipDates;

    /**
     * Disables the check for the existence of the date fields.
     *
     * @since 10.7RC1
     */
    @Parameter(property = "xar.dates.skip.documentList")
    protected Set<String> skipDatesDocumentList;

    /**
     * Explicitly define a list of pages (it's a regex) that should be considered as content pages (rather than
     * technical pages). Note that content pages must have a non empty default language specified and note that if a
     * page is not in this list and it doesn't have any translation then it's considered by default to be a technical
     * page for the default language check. Thus this configuration property is useful for pages such as Translations
     * pages (even though they may not have any translations at first).
     *
     * @since 7.1M1
     */
    @Parameter(property = "xar.verify.contentPages")
    private List<String> contentPages;

    /**
     * Explicitly define a list of pages (it's a regex) that should  be considered as technical pages. Any matching
     * page defined in this list has precedence over pages matching {@link #contentPages}.
     *
     * @since 7.1M1
     */
    @Parameter(property = "xar.verify.technicalPages")
    private List<String> technicalPages;

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

    private List<Pattern> technicalPagePatterns;

    private Map<Pattern, Pattern> titlePatterns;

    /**
     * Initialize regex Patterns for performance reasons.
     */
    protected void initializePatterns()
    {
        this.contentPagePatterns = initializationPagePatterns(this.contentPages);
        this.technicalPagePatterns = initializationPagePatterns(this.technicalPages);

        // Transform title expectations into Patterns
        Map<Pattern, Pattern> patterns = new HashMap<>();
        for (String key : this.titles.stringPropertyNames()) {
            patterns.put(Pattern.compile(key), Pattern.compile(this.titles.getProperty(key)));
        }
        this.titlePatterns = patterns;
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

        Collection<File> files = new ArrayList<File>();
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
     *     <li>If the page name matches one of the regexes defined by the user as content pages then check that the
     *         default language is {@link #defaultLanguage}.</li>
     *     <li>If the page name matches one of the regexes defined by the user as technial pages then check that the
     *         default language is empty. Matching technical pages have precedence over matching content pages.</li>
     *     <li>If there's no other translation of the file then consider default language to be empty to signify that
     *         it's a technical document. </li>
     *     <li>If there are other translations ("(prefix).(language).xml" format) then the default language should be
     *         {@link #defaultLanguage}</li>
     * </ul>
     * @param file the XML file for which to guess the default language that it should have
     * @param xwikiXmlFiles the list of all XML files that is used to check for translations of the passed XML file
     * @return the default language as a string (e.g. "en" for English or "" for an empty default language)
     * @since 5.4.1
     */
    protected String guessDefaultLanguage(File file, Collection<File> xwikiXmlFiles)
    {
        String fileName = file.getName();

        // Note that we need to check for content pages before technical pages because some pages are both content
        // pages (Translation pages for example) and technical pages.

        // Is it in the list of defined content pages?
        String language = guessDefaultLanguageForPatterns(fileName, this.contentPagePatterns, this.defaultLanguage);
        if (language != null) {
            return language;
        }

        // Is it in the list of defined technical pages?
        language = guessDefaultLanguageForPatterns(fileName, this.technicalPagePatterns, "");
        if (language != null) {
            return language;
        }

        language = "";

        // Check if the doc is a translation
        Matcher matcher = TRANSLATION_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            // We're in a translation, use the default language
            language = this.defaultLanguage;
        } else {
            // We're not in a translation, check if there are translations. First get the doc name before the extension
            String prefix = StringUtils.substringBeforeLast(fileName, EXTENSION);
            // Check for a translation now
            Pattern translationPattern = Pattern.compile(String.format("%s\\..*\\.xml", Pattern.quote(prefix)));
            for (File xwikiXmlFile : xwikiXmlFiles) {
                Matcher translationMatcher = translationPattern.matcher(xwikiXmlFile.getName());
                if (translationMatcher.matches()) {
                    // Found a translation, use the default language
                    language = this.defaultLanguage;
                    break;
                }
            }
        }
        return language;
    }

    private String guessDefaultLanguageForPatterns(String fileName, List<Pattern> patterns, String defaultLanguage)
    {
        String language = null;

        if (patterns != null) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(fileName).matches()) {
                    return defaultLanguage;
                }
            }
        }

        return language;
    }

    protected boolean isTechnicalPage(String fileName)
    {
        if (this.technicalPagePatterns != null) {
            for (Pattern pattern : this.technicalPagePatterns) {
                if (pattern.matcher(fileName).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isTitlesMatching(String documentReference, String title)
    {
        for (Map.Entry<Pattern, Pattern> entry : this.titlePatterns.entrySet()) {
            if (entry.getKey().matcher(documentReference).matches()) {
                if (!entry.getValue().matcher(title).matches()) {
                    return false;
                }
            }
        }
        return true;
    }

    protected String getTitlePatternRuleforPage(String documentReference)
    {
        for (Map.Entry<Pattern, Pattern> entry : this.titlePatterns.entrySet()) {
            if (entry.getKey().matcher(documentReference).matches()) {
                return entry.getValue().pattern();
            }
        }
        return null;
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
                element(name("header"), "license.txt"),
                element(name("strictCheck"), "true"),
                element(name("headerDefinitions"),
                    element(name("headerDefinition"), "license-xml-definition.xml")),
                element(name("includes"),
                    element(name("include"), "src/main/resources/**/*.xml"))
            ),
            executionEnvironment(
                this.project,
                this.mavenSession,
                this.pluginManager
            )
        );
    }
}
