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
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Pretty prints and set valid authors and version to XAR XML files.
 *
 * @version $Id$
 * @goal format
 * @requiresProject
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class FormatMojo extends AbstractVerifyMojo
{
    /**
     * The project currently being build.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;

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
     * If false then don't pretty print the XML.
     *
     * @parameter expression="${pretty}"
     * @readonly
     */
    private boolean pretty = true;

    /**
     * If true then add license header to XML files.
     *
     * @parameter expression="${formatLicense}"
     * @readonly
     */
    private boolean formatLicense;

    /**
     * The Commons version to be used by this mojo.
     *
     * @parameter expression="${commons.version}" default-value="${commons.version}"
     */
    private String commonsVersion;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Only format XAR modules or when forced
        if (getProject().getPackaging().equals("xar") || this.force) {
            getLog().info("Formatting XAR XML files...");
            Collection<File> xmlFiles = getXARXMLFiles();
            for (File file : xmlFiles) {
                try {
                    format(file, guessDefaultLanguage(file, xmlFiles));
                } catch (Exception e) {
                    throw new MojoExecutionException(String.format("Failed to format file [%s]", file), e);
                }
            }
            if (this.formatLicense) {
                getLog().info("Adding missing XAR XML license headers...");
                formatLicense();
            }
        } else {
            getLog().info("Not a XAR module, skipping reformatting...");
        }
    }

    /**
     * Add license headers to all XML files present in the {@code src/main/resources} directory where this plugin
     * executes.
     */
    private void formatLicense() throws MojoExecutionException
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
            goal("format"),
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

    private void format(File file, String defaultLanguage) throws Exception
    {
        SAXReader reader = new SAXReader();
        Document domdoc = reader.read(file);
        format(domdoc, defaultLanguage);

        XMLWriter writer;
        if (this.pretty) {
            OutputFormat format = new OutputFormat("  ", true, "UTF-8");
            format.setExpandEmptyElements(false);
            writer = new XWikiXMLWriter(new FileOutputStream(file), format);
        } else {
            writer = new XWikiXMLWriter(new FileOutputStream(file));
        }
        writer.write(domdoc);
        writer.close();

        String parentName = file.getParentFile().getName();
        getLog().info(String.format("  Formatting [%s/%s]... ok", parentName, file.getName()));
    }

    private void format(Document domdoc, String defaultLanguage) throws Exception
    {
        Node node = domdoc.selectSingleNode("xwikidoc/author");
        if (node != null) {
            node.setText(AUTHOR);
        }
        node = domdoc.selectSingleNode("xwikidoc/contentAuthor");
        if (node != null) {
            node.setText(AUTHOR);
        }
        node = domdoc.selectSingleNode("xwikidoc/creator");
        if (node != null) {
            node.setText(AUTHOR);
        }
        node = domdoc.selectSingleNode("xwikidoc/version");
        if (node != null) {
            node.setText(VERSION);
        }
        node = domdoc.selectSingleNode("xwikidoc/minorEdit");
        if (node != null) {
            node.setText("false");
        }

        // Set the default language
        Element element = (Element) domdoc.selectSingleNode("xwikidoc/defaultLanguage");
        if (element != null) {
            if (StringUtils.isEmpty(defaultLanguage)) {
                removeContent(element);
            } else {
                element.setText(defaultLanguage);
            }
        }

        // Remove any content of the <comment> element
        element = (Element) domdoc.selectSingleNode("xwikidoc/comment");
        if (element != null) {
            removeContent(element);
        }
    }

    private void removeContent(Element element)
    {
        if (element.hasContent()) {
            ((Node) element.content().get(0)).detach();
        }
    }

    /**
     * @return the version of the XWiki Commons project, either configured in the project using this plugin or taken
     *         from the {@code commons.version} property if defined, defaulting to the current project version if not
     *         defined
     */
    private String getXWikiCommonsVersion()
    {
        String version = this.commonsVersion;
        if (version == null) {
            version = this.project.getVersion();
        }
        return version;
    }
}
