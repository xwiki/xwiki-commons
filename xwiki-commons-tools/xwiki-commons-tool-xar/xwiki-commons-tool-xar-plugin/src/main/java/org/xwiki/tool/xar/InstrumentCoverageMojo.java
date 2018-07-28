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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Instrument velocity scripts in order to compute their coverage when running tests.
 *
 * @version $Id$
 */
@Mojo(
        name = "instrument",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true
)
public class InstrumentCoverageMojo extends AbstractVerifyMojo
{
    private static final String DEFAULT_LOGGER_NAME = "org.xwiki.XARVelocityCoverage";

    private static final String PATTERN_VELOCITY_BEGIN = "{{velocity";

    private static final String PATTERN_VELOCITY_END = "{{/velocity}}";

    private static final List<String> IGNORE_VELOCITY_LINES = Arrays.asList("##", "#end", "# ");

    private static final String DEFAULT_TARGET_OUTPUT = "target/velocity-instrumented";

    private static final String SOURCE_PREFIX = "src/main/resources";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        Collection<File> xarxmlFiles = getXARXMLFiles();
        getLog().info("Starting coverage instrumentation");
        try {
            for (File xarxmlFile : xarxmlFiles) {
                this.instrumentXML(xarxmlFile);
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        List<Resource> resourceRoots = this.project.getResources();

        Resource oldResource = null;
        for (Resource resourceRoot : resourceRoots) {
            if (resourceRoot.getDirectory().endsWith(SOURCE_PREFIX)) {
                oldResource = resourceRoot;
                break;
            }
        }

        if (oldResource != null) {
            resourceRoots.remove(oldResource);
        }

        Resource newResource = new Resource();
        newResource.setDirectory(project.getBasedir() + DEFAULT_TARGET_OUTPUT);
        resourceRoots.add(newResource);
    }

    private String createLoggerCall(String callType, String filename, int numbers) {
        return String.format("$services.logging.getLogger('%s').info('%s [%s] (%s)')",
                DEFAULT_LOGGER_NAME,
                callType,
                filename,
                String.valueOf(numbers));
    }

    private void instrumentXML(File xmlFile) throws IOException
    {
        List<String> lines = IOUtils.readLines(new FileInputStream(xmlFile), Charset.defaultCharset());
        List<String> outputLines = new ArrayList<>();
        boolean isInVelocityBlock = false;
        int counterVelocityInterestingLines = 0;
        int lineNumber = 0;
        for (String line : lines) {
            String trimmedLine = line.trim();

            if (!isInVelocityBlock) {
                outputLines.add(line);
                if (trimmedLine.contains(PATTERN_VELOCITY_BEGIN)) {
                    isInVelocityBlock = true;
                }
            } else {
                // we are in a velocity block

                if (trimmedLine.contains(PATTERN_VELOCITY_END)) {
                    isInVelocityBlock = false;
                    String logOutputCounter = this.createLoggerCall(ReportCoverageMojo.LOG_TYPE_COUNTER,
                            xmlFile.getName(),
                            counterVelocityInterestingLines);
                    outputLines.add(logOutputCounter);
                    outputLines.add(line);
                } else {

                    boolean isInterestingVelocityStatement = (trimmedLine.startsWith("#") && !StringUtils
                            .startsWithAny(trimmedLine, IGNORE_VELOCITY_LINES.toArray(new String[0])));
                    if (isInterestingVelocityStatement) {
                        counterVelocityInterestingLines++;
                        String log = this.createLoggerCall(ReportCoverageMojo.LOG_TYPE_COV,
                                xmlFile.getName(),
                                lineNumber);
                        outputLines.add(log);
                        outputLines.add(line);
                    } else {
                        outputLines.add(line);
                    }
                }
            }
            lineNumber++;
        }

        String pathFile = xmlFile.getPath();
        String newPath = String.format("%s/%s/",
                DEFAULT_TARGET_OUTPUT,
                StringUtils.substringAfter(pathFile, SOURCE_PREFIX));

        File newPathFile = new File(newPath);
        newPathFile.getParentFile().mkdirs();
        getLog().info(String.format("File will be written in [%s]", newPath));

        IOUtils.writeLines(outputLines, "\n", new FileOutputStream(newPathFile), Charset.defaultCharset());
    }
}
