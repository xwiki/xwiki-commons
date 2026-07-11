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
package org.xwiki.filter.test.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses test data defined using the following syntax, shown with this example: {@code
 * .configuration <key=value>
 * .input|<type>
 * <optional input content here>
 * .expect|<type>
 * <optional expected content here>
 * }
 * <p>
 * Note that there can be several {@code .input} and {@code .expect} entries. For each {@code .input} definition, all
 * the found {@code .expect} will be executed and checked.
 * 
 * @version $Id$
 * @since 6.2M1
 */
public class TestDataParser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataParser.class);

    private static final Pattern PATTERN_VARIABLE = Pattern.compile("(\\\\)?(\\$\\{\\{\\{(.*)\\}\\}\\})");

    private static final String UTF8 = "UTF-8";

    private static final String CONFIGURATION = ".configuration";

    /**
     * Replaces every {@code ${{{name}}}} variable found in the passed source with the value of the system property
     * of that name. A variable whose system property is not set is left untouched, and a variable escaped with a
     * leading backslash (for example {@code \${{{name}}}}) is emitted verbatim with the backslash removed.
     *
     * @param source the source text possibly containing {@code ${{{name}}}} variables to interpret
     * @return the source text with its non-escaped variables replaced by the matching system property values
     */
    public static String interpret(String source)
    {
        StringBuilder result = new StringBuilder();

        Matcher matcher = PATTERN_VARIABLE.matcher(source);

        int current = 0;
        while (matcher.find()) {
            if (matcher.group(1) == null) {
                String variableName = matcher.group(3);

                String value = System.getProperty(variableName);
                if (value != null) {
                    result.append(source, current, matcher.start());
                    result.append(value);
                    current = matcher.end();
                }
            } else {
                result.append(source, current, matcher.start());
                current = matcher.start(2);
            }
        }

        if (current < source.length()) {
            result.append(source, current, source.length());
        }

        return result.toString();
    }

    /**
     * Parses the test data read from the passed stream, extracting the {@code .configuration}, {@code .input} and
     * {@code .expect} entries as described in the class documentation.
     *
     * @param source the stream to read the test data from; it is read as {@code UTF-8} regardless of the system
     *  encoding
     * @param resourceName the name of the resource being parsed, used only for reporting (for example in skip
     *  warnings)
     * @return the parsed test data, holding the collected inputs, expectations and configuration
     * @throws IOException if reading the source stream fails
     */
    public TestResourceData parse(InputStream source, String resourceName) throws IOException
    {
        TestResourceData data = new TestResourceData();

        // Resources should always be encoded as UTF-8, to reduce the dependency on the system encoding
        BufferedReader reader = new BufferedReader(new InputStreamReader(source, UTF8));

        // Read each line and look for lines starting with ".". When this happens it means we've found a separate
        // test case.
        try {
            String action = null;
            String typeId = null;
            boolean skip = false;

            data.resourceName = resourceName;

            StringBuilder buffer = new StringBuilder();
            Map<String, String> configuration = data.configuration;

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith(".")) {
                    if (line.startsWith(".#")) {
                        // Ignore comments and log it if it's a todo.
                        if (line.toLowerCase().contains("todo")) {
                            LOGGER.info(line);
                        }
                    } else if (line.startsWith(CONFIGURATION)) {
                        parseConfiguration(line, configuration);
                    } else {
                        if (!skip) {
                            saveData(data, action, typeId, buffer, configuration);
                        }

                        // Reset buffer
                        buffer.setLength(0);
                        // Reset configuration
                        configuration = new HashMap<>();

                        // Parse the directive line starting with "." and with "|" separators.
                        // For example ".input|xwiki/2.0|skip" or ".expect|xhtml"
                        StringTokenizer st = new StringTokenizer(line.substring(1), "|");
                        // First token is "input", "expect" or "inputexpect".
                        action = st.nextToken();
                        // Second token is either the input syntax id or the expectation renderer short name
                        typeId = st.nextToken();
                        // Third (optional) token is whether the test should be skipped (useful while waiting for
                        // a fix to wikimodel for example).
                        skip = false;
                        if (st.hasMoreTokens()) {
                            skip = true;
                            LOGGER.warn("Skipping test for [{}] in resource [{}] since it has been marked as skipped "
                                + "in the test. This needs to be reviewed and fixed.", typeId, resourceName);
                        }
                    }
                } else {
                    buffer.append(line).append('\n');
                }
            }

            if (!skip) {
                saveData(data, action, typeId, buffer, configuration);
            }

        } finally {
            reader.close();
        }

        return data;
    }

    private void parseConfiguration(String line, Map<String, String> configuration)
    {
        int keyIndex = CONFIGURATION.length() + 1;
        int index = line.indexOf('=', CONFIGURATION.length() + 1);
        if (index != -1) {
            configuration.put(line.substring(keyIndex, index), line.substring(index + 1));
        }
    }

    private void saveData(TestResourceData data, String action, String typeId, StringBuilder buffer,
        Map<String, String> configuration)
    {
        if (action != null) {
            // Remove the last newline since our test format forces an additional new lines
            // at the end of input texts.
            if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) == '\n') {
                buffer.setLength(buffer.length() - 1);
            }

            if ("input".equalsIgnoreCase(action)) {
                addInput(data, typeId, buffer, configuration);
            } else if ("expect".equalsIgnoreCase(action)) {
                addExpect(data, typeId, buffer, configuration);
            } else if ("inputexpect".equalsIgnoreCase(action)) {
                addExpect(data, typeId, buffer, configuration);
                addInput(data, typeId, buffer, configuration);
            }
        }
    }

    private void addInput(TestResourceData data, String typeId, StringBuilder buffer, Map<String, String> configuration)
    {
        InputTestConfiguration inputConfiguration = new InputTestConfiguration(typeId, buffer.toString());

        // Default properties
        inputConfiguration.setEncoding(UTF8);

        inputConfiguration.putAll(configuration);

        data.inputs.add(inputConfiguration);
    }

    private void addExpect(TestResourceData data, String typeId, StringBuilder buffer,
        Map<String, String> configuration)
    {
        ExpectTestConfiguration expectTestConfiguration = new ExpectTestConfiguration(typeId, buffer.toString());

        // Default properties
        expectTestConfiguration.setEncoding(UTF8);

        expectTestConfiguration.putAll(configuration);

        data.expects.add(expectTestConfiguration);
    }
}
