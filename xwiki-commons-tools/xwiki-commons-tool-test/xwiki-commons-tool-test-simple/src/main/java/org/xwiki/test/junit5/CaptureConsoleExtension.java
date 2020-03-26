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
package org.xwiki.test.junit5;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Captures any content sent to stdout/stderr by JUnit5 unit tests and report a failure if the content is not empty.
 *
 * @version $Id$
 * @since 11.8RC1
 */
public class CaptureConsoleExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback,
    AfterEachCallback
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureConsoleExtension.class);

    private static final String CAPTURECONSOLESKIP_PROPERTY = "xwiki.surefire.captureconsole.skip";

    private static final ExtensionContext.Namespace NAMESPACE =
        ExtensionContext.Namespace.create(CaptureConsoleExtension.class);

    private static final String ALL = "all";

    private static final String EACH = "each";

    private static Boolean skip;

    private PrintStream savedOriginalOut;

    private PrintStream savedOriginalErr;

    private Map<String, ByteArrayOutputStream> collectingContentStream = new HashMap();

    @Override
    public void beforeAll(ExtensionContext extensionContext)
    {
        this.savedOriginalOut = System.out;
        this.savedOriginalErr = System.err;
        captureOutput(ALL);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext)
    {
        captureOutput(EACH);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext)
    {
        validateOutput(EACH);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext)
    {
        validateOutput(ALL);
        System.setOut(this.savedOriginalOut);
        System.setOut(this.savedOriginalErr);
    }

    private void captureOutput(String key)
    {
        if (shouldSkip()) {
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.collectingContentStream.put(key, stream);

        // Capture stdout but continue sending data to it at the same time
        System.setOut(new PrintStream(new TeeOutputStream(stream, this.savedOriginalOut)));

        // Capture stderr but continue sending data to it at the same time
        System.setErr(new PrintStream(new TeeOutputStream(stream, this.savedOriginalErr)));
    }

    private void validateOutput(String key)
    {
        if (shouldSkip()) {
            return;
        }

        // Only throw an exception if the captured content has not already been reported
        if (this.collectingContentStream.containsKey(key)) {
            String outputContent = filteredString(this.collectingContentStream.remove(key).toString());
            if (!outputContent.trim().isEmpty()) {
                throw new AssertionError(String.format("There should be no content output to the console by the test! "
                    + "Instead we got [%s]", outputContent));
            }
        }
    }

    /**
     * @see FailingTestDebuggingTestExecutionListener#executionFinished(TestIdentifier, TestExecutionResult)
     */
    private String filteredString(String outputContent)
    {
        StringBuilder builder = new StringBuilder();
        boolean insideDebuggingInformation = false;
        String[] lines = outputContent.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (insideDebuggingInformation) {
                if (line.contains(FailingTestDebuggingTestExecutionListener.STOP_MESSAGE)) {
                    insideDebuggingInformation = false;
                }
            } else if (line.contains(FailingTestDebuggingTestExecutionListener.START_MESSAGE)) {
                insideDebuggingInformation = true;
            } else {
                builder.append(line);
                if (i < lines.length - 1) {
                    builder.append('\n');
                }
            }
        }
        if (!outputContent.isEmpty()) {
            builder.append('\n');
        }
        return builder.toString();
    }

    /**
     * @param propertyName the property to look for
     * @return the property value by first looking for it in System properties and then in the current {@code pom.xml}
     * file. Return null if not found in either.
     */
    private static String getPropertyValue(String propertyName)
    {
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = getPropertyValueFromPOM(propertyName);
        }
        return value;
    }

    /**
     * @return true if the check should be skipped or false otherwise. The defined System property is checked first and
     * if it doesn't exist, the Maven property of the same name is read from the current {@code pom.xml}
     */
    static boolean shouldSkip()
    {
        if (skip == null) {
            String value = getPropertyValue(CAPTURECONSOLESKIP_PROPERTY);
            skip = Boolean.parseBoolean(value);
        }
        return skip;
    }

    private static String getPropertyValueFromPOM(String propertyName)
    {
        String value = null;
        // Low tech (doesn't bring any additional dependencies that could cause conflicts with tests) and fast.
        // Note: doesn't support inheritance: the property needs to be set in each pom.xml where it's used.
        if (Files.exists(getPOMPath())) {
            try {
                String content = new String(Files.readAllBytes(getPOMPath()));
                Pattern regex = Pattern.compile(String.format("<%s>(.*)</%s>", propertyName, propertyName),
                    Pattern.DOTALL);
                Matcher regexMatcher = regex.matcher(content);
                if (regexMatcher.find()) {
                    value = regexMatcher.group(1).trim();
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("Error reading file [%s]", getPOMPath()), e);
            }
        } else {
            LOGGER.warn("No [{}] file in current directory [{}]", getPOMPath(), Paths.get("").toAbsolutePath());
        }
        return value;
    }

    private static Path getPOMPath()
    {
        return Paths.get("pom.xml");
    }

    private static ExtensionContext.Store getStore(ExtensionContext context)
    {
        return context.getRoot().getStore(NAMESPACE);
    }
}
