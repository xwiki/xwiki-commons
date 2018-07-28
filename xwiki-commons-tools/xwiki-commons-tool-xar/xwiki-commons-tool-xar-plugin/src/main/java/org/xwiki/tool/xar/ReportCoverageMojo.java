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

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compute a report for Velocity Coverage.
 *
 * @version $Id$
 */
@Mojo(
        name = "reportCoverage",
        defaultPhase = LifecyclePhase.VERIFY,
        threadSafe = true
)
public class ReportCoverageMojo extends AbstractVerifyMojo
{
    /**
     * This constant is used to specify when a velocity statement is covered in the execution logs.
     */
    public static final String LOG_TYPE_COV = "COV";

    /**
     * This constant is used to specify the number of lines encountered for this velocity script.
     */
    public static final String LOG_TYPE_COUNTER = "Counter Velocity";

    private static final String LOG_DIR_PATH = "target/xwiki/data/logs/";

    private static final String PATTERN_LOG_FILENAME = "output";

    private static final String LOGGER_NAME = "XARVelocityCoverage";

    private static final String REPORT_TARGET_DIR = "target/report";

    private static final String REPORT_FILENAME = "velocityCoverage.txt";

    @Parameter(name = "targetLog", defaultValue = LOG_DIR_PATH)
    private String targetLog;

    final class Coverage
    {
        private int totalLines;

        private int coveredLines;

        private Set<Integer> inspectedLines = new HashSet<>();

        public int getTotalLines()
        {
            return totalLines;
        }

        public void setTotalLines(int totalLines)
        {
            this.totalLines = totalLines;
        }

        public int getCoveredLines()
        {
            return coveredLines;
        }

        public void increaseCoveredLines(int coveredLines)
        {
            this.coveredLines += coveredLines;
        }

        public Set<Integer> getInspectedLines()
        {
            return inspectedLines;
        }
    }

    private Map<String, Coverage> coverageMap = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try {
            File loggerFile = getLoggerFile();
            if (loggerFile != null) {
                this.parseLogs(loggerFile);
                this.outputReport();
            } else {
                throw new MojoExecutionException("Logger file has not been found.");
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void outputReport() throws IOException
    {

        final String valueSeparator = "\t";
        final List<String> lines = new ArrayList<>();

        for (Map.Entry<String, Coverage> coverageEntry : this.coverageMap.entrySet()) {
            int totalLines = coverageEntry.getValue().totalLines;
            int coveredLines = coverageEntry.getValue().coveredLines;

            DecimalFormat df = new DecimalFormat("#.##");
            double tpc = Double.valueOf(df.format(((double) coveredLines / (double) totalLines) * 100));

            StringBuilder lineContent = new StringBuilder();
            lineContent.append(coverageEntry.getKey()).append(valueSeparator);
            lineContent.append(tpc).append("%").append(valueSeparator);
            lineContent.append(coveredLines).append("/").append(totalLines).append(valueSeparator);

            lines.add(lineContent.toString());
        }

        File targetDir = new File(REPORT_TARGET_DIR);
        targetDir.mkdirs();

        File targetFile = new File(targetDir, REPORT_FILENAME);
        IOUtils.writeLines(lines, System.lineSeparator(), new FileOutputStream(targetFile), "UTF-8");
    }

    private void parseLogs(File loggerFile) throws IOException
    {
        List<String> lines = Files.readAllLines(loggerFile.toPath());

        Coverage coverage = null;
        for (String line : lines) {
            if (line.contains(LOGGER_NAME)) {
                if (line.contains(LOG_TYPE_COV)) {
                    String fileName = this.parseLineToGetFilename(line);

                    if (this.coverageMap.containsKey(fileName)) {
                        coverage = this.coverageMap.get(fileName);
                    } else {
                        coverage = new Coverage();
                        this.coverageMap.put(fileName, coverage);
                    }

                    int lineNumber = this.parseLineToGetNumbers(line);

                    if (!coverage.getInspectedLines().contains(lineNumber)) {
                        coverage.increaseCoveredLines(1);
                        coverage.getInspectedLines().add(lineNumber);
                    }
                } else if (line.contains(LOG_TYPE_COUNTER)) {
                    String fileName = this.parseLineToGetFilename(line);

                    if (this.coverageMap.containsKey(fileName)) {
                        coverage = this.coverageMap.get(fileName);
                    } else {
                        coverage = new Coverage();
                        this.coverageMap.put(fileName, coverage);
                    }

                    coverage.setTotalLines(this.parseLineToGetNumbers(line));
                }
            }
        }
    }

    private String parseLineToGetFilename(String line)
    {
        return line.substring(line.lastIndexOf("[") + 1, line.lastIndexOf("]"));
    }

    private int parseLineToGetNumbers(String line) {
        String number = line.substring(line.lastIndexOf("(") + 1, line.lastIndexOf(")"));
        return Integer.parseInt(number);
    }

    private File getLoggerFile() throws IOException
    {
        File logDir = new File(this.targetLog);

        if (logDir.exists()) {
            for (Path path : Files.list(logDir.toPath()).collect(Collectors.toList())) {
                File file = path.toFile();
                if (file.getName().contains(PATTERN_LOG_FILENAME)) {
                    return file;
                }
            }
        }
        return null;
    }
}
