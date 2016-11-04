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
package org.xwiki.tool.checkstyle;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

/**
 * Simple front end to the Checkstyle plugin so that we can bypass the
 * <a href="http://jira.xwiki.org/browse/XCOMMONS-1105">limitations of the Maven Execution feature</a>.
 *
 * @version $Id$
 * @since 9.0RC1
 */
@Mojo(
    name = "check",
    defaultPhase = LifecyclePhase.VERIFY,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true
)
public class CheckMojo extends AbstractMojo
{
    @Parameter( property = "xwikicheckstyle.config.location", defaultValue = "checkstyle-blocker.xml" )
    private String configLocation;

    @Parameter
    private String propertyExpansion;

    @Parameter( property = "xwikicheckstyle.skip", defaultValue = "false" )
    private boolean skip;

    /**
     * The maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Find the checkstyle plugin
        Plugin checkstylePlugin = this.project.getPluginManagement().getPluginsAsMap().get(
            "org.apache.maven.plugins:maven-checkstyle-plugin");

        if (checkstylePlugin == null) {
            getLog().info(String.format("No Checkstyle Plugin found in <pluginManagement>, skipping checks..."));
            return;
        }

        executeMojo(
            checkstylePlugin,
            goal("check"),
            configuration(
                element(name("configLocation"), this.configLocation),
                element(name("propertyExpansion"), this.propertyExpansion),
                element(name("failOnViolation"), "true"),
                element(name("consoleOutput"), "true"),
                element(name("skip"), Boolean.toString(this.skip))
            ),
            executionEnvironment(
                this.project,
                this.mavenSession,
                this.pluginManager
            )
        );
    }
}
