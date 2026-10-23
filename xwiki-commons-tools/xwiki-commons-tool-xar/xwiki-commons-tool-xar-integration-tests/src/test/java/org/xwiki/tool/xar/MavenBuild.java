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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.executor.ExecutorException;
import org.apache.maven.executor.ExecutorHelper;
import org.apache.maven.executor.ExecutorRequest;
import org.apache.maven.executor.ExecutorResult;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Runs a test Maven project and gives access to the produced log.
 *
 * @version $Id$
 */
public class MavenBuild
{
    private final File basedir;

    private final List<String> arguments = new ArrayList<>();

    private final Map<String, String> environmentVariables = new HashMap<>();

    private String log = "";

    /**
     * @param basedir the directory containing the pom.xml of the project to build
     */
    public MavenBuild(File basedir)
    {
        this.basedir = basedir;

        // Same as what a CI build does: no interactive prompt and no download progress in the log
        this.arguments.add("-B");
        this.arguments.add("-ntp");
    }

    /**
     * @return the directory containing the pom.xml of the project to build
     */
    public File getBasedir()
    {
        return this.basedir;
    }

    /**
     * @param argument a command line argument to pass to Maven for all the following executions
     */
    public void addArgument(String argument)
    {
        this.arguments.add(argument);
    }

    /**
     * @param name the name of the environment variable to set in the Maven process
     * @param value the value of the environment variable
     */
    public void setEnvironmentVariable(String name, String value)
    {
        this.environmentVariables.put(name, value);
    }

    /**
     * Execute the passed goals.
     *
     * @param goals the goals to execute
     * @throws ExecutorException when Maven could not be executed or when the build failed, in which case the message
     *     contains the whole log
     */
    public void execute(String... goals) throws ExecutorException
    {
        List<String> allArguments = new ArrayList<>(this.arguments);
        allArguments.addAll(Arrays.asList(goals));

        ExecutorRequest request = ExecutorRequest.mavenBuilder()
            .cwd(this.basedir.toPath())
            .arguments(allArguments)
            .environmentVariables(this.environmentVariables)
            .grabOutputAsString(true)
            .build();

        ExecutorResult result;
        try (ExecutorHelper executor = ExecutorHelper.forMavenInstallation(
            ExecutorRequest.discoverInstallationDirectory(), ExecutorHelper.Mode.AUTO)) {
            result = executor.execute(request);
        }

        // Maven logs everything to stdout but the JVM itself might write to stderr
        this.log = result.stdOutString().orElse("") + result.stdErrString().orElse("");

        if (!result.success()) {
            throw new ExecutorException(String.format("Maven build failed with exit code [%s]:%n%s",
                result.exitCode().orElse(null), this.log));
        }
    }

    /**
     * @return the log of the last execution
     */
    public String getLog()
    {
        return this.log;
    }

    /**
     * Fail if the log of the last execution contains errors.
     */
    public void verifyErrorFreeLog()
    {
        List<String> errors =
            this.log.lines().filter(line -> line.startsWith("[ERROR]")).collect(Collectors.toList());
        if (!errors.isEmpty()) {
            fail(String.format("The Maven log contains errors:%n%s%n%nFull log:%n%s", String.join("\n", errors),
                this.log));
        }
    }
}
