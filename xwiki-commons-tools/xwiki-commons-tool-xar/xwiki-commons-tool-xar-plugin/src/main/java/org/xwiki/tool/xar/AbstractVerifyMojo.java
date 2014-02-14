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
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResourceCollection;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

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
     *
     * @parameter expression="${force}"
     * @readonly
     */
    protected boolean force;

    /**
     * The language in which non-translated documents are written in.
     *
     * @parameter expression="${defaultLanguage}" default-value="en"
     */
    protected String defaultLanguage;

    /**
     * If true then add license header to XML files.
     *
     * @parameter expression="${formatLicense}"
     * @readonly
     */
    protected boolean formatLicense;

    /**
     * The Commons version to be used by this mojo.
     *
     * @parameter expression="${commons.version}" default-value="${commons.version}"
     */
    protected String commonsVersion;

    /**
     * The current Maven session.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession mavenSession;

    /**
     * The Maven BuildPluginManager component.
     *
     * @component
     * @required
     */
    private BuildPluginManager pluginManager;

    /**
     * @return the list of XAR XML files in this project
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
     *     <li>If there's no other translation of the file then consider default language to be empty to signify that
     *         it's a technical document. </li>
     *     <li>If there are other translations ("(prefix).(language).xml" format) then the default language should be
     *         {@link #defaultLanguage}</li>
     * </ul>
     * @since 5.4.1
     */
    protected String guessDefaultLanguage(File file, Collection<File> xwikiXmlFiles)
    {
        String language = "";

        // Check if the doc is a translation
        Matcher matcher = TRANSLATION_PATTERN.matcher(file.getName());
        if (matcher.matches()) {
            // We're in a translation, use the default language
            language = this.defaultLanguage;
        } else {
            // We're not in a translation, check if there are translations. First get the doc name before the extension
            String prefix = StringUtils.substringBeforeLast(file.getName(), EXTENSION);
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
     * Executes a mojo of the Maven License plugin (used for adding or checking for license headers.
     */
    protected void executeLicenseGoal(String goal) throws MojoExecutionException
    {
        Dependency dep = new Dependency();
        dep.setGroupId("org.xwiki.commons");
        dep.setArtifactId("xwiki-commons-tool-verification-resources");
        dep.setVersion(getXWikiCommonsVersion());

        Plugin licensePlugin = plugin(
            groupId("com.mycila"),
            artifactId("license-maven-plugin"),
            version("2.6"));
        licensePlugin.setDependencies(Collections.singletonList(dep));

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
