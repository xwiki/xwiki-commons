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
package org.xwiki.filter.test.integration.junit5;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.descriptor.FilterStreamPropertyDescriptor;
import org.xwiki.filter.input.DefaultFileInputSource;
import org.xwiki.filter.input.DefaultURLInputSource;
import org.xwiki.filter.input.FileInputSource;
import org.xwiki.filter.input.InputFilterStream;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.input.ReaderInputSource;
import org.xwiki.filter.input.StringInputSource;
import org.xwiki.filter.output.ByteArrayOutputTarget;
import org.xwiki.filter.output.OutputFilterStream;
import org.xwiki.filter.output.OutputFilterStreamFactory;
import org.xwiki.filter.output.OutputTarget;
import org.xwiki.filter.output.StringWriterOutputTarget;
import org.xwiki.filter.test.integration.ExpectTestConfiguration;
import org.xwiki.filter.test.integration.InputTestConfiguration;
import org.xwiki.filter.test.integration.TestConfiguration;
import org.xwiki.filter.test.integration.TestDataParser;
import org.xwiki.filter.test.internal.FileAssert;
import org.xwiki.filter.utils.FilterStreamConstants;
import org.xwiki.test.internal.MockConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A generic JUnit Test used by {@link FilterTest} to parse some passed content and verify it matches some passed
 * expectation. The format of the input/expectation is specified in {@link TestDataParser}.
 * 
 * @version $Id$
 * @since 18.0.0RC1
 */
@SuppressWarnings("ClassFanOutComplexity")
public class InternalFilterTest
{
    private static final String FILE_PREFIX = "file:";

    private static final String SLASH = "/";

    private TestConfiguration configuration;

    private ComponentManager componentManager;

    /**
     * @param configuration the test configuration
     * @param componentManager the component manager to use
     */
    public InternalFilterTest(TestConfiguration configuration, ComponentManager componentManager)
    {
        this.configuration = configuration;
        this.componentManager = componentManager;
    }

    /**
     * @throws Exception when failing to execute the test
     */
    public void execute() throws Exception
    {
        TimeZone currentTimeZone = TimeZone.getDefault();

        // Make sure to have a stable timezone during tests
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Map<String, String> originalConfiguration = new HashMap<>();
        if (this.configuration.configuration != null) {
            ConfigurationSource configurationSource = getComponentManager().getInstance(ConfigurationSource.class);

            if (configurationSource instanceof MockConfigurationSource mockConfigurationSource) {
                for (Map.Entry<String, String> entry : this.configuration.configuration.entrySet()) {
                    originalConfiguration.put(entry.getKey(), mockConfigurationSource.getProperty(entry.getKey()));
                    mockConfigurationSource.setProperty(entry.getKey(), TestDataParser.interpret(entry.getValue()));
                }
            }
        }

        try {
            runTestInternal();
        } finally {
            // Restore current Timezone
            TimeZone.setDefault(currentTimeZone);

            // Revert Configuration that have been set
            if (this.configuration.configuration != null) {
                ConfigurationSource configurationSource = getComponentManager().getInstance(ConfigurationSource.class);

                if (configurationSource instanceof MockConfigurationSource mockConfigurationSource) {
                    for (Map.Entry<String, String> entry : originalConfiguration.entrySet()) {
                        if (entry.getValue() == null) {
                            mockConfigurationSource.removeProperty(entry.getKey());
                        } else {
                            mockConfigurationSource.setProperty(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }
    }

    private InputSource getInputSource(TestConfiguration testConfiguration, String value) throws FilterException
    {
        InputSource source;

        String sourceString = TestDataParser.interpret(value);

        if (sourceString.startsWith(FILE_PREFIX)) {
            sourceString = sourceString.substring(FILE_PREFIX.length());
        }

        File file = new File(sourceString);

        if (file.exists()) {
            // It's a file

            source = new DefaultFileInputSource(file);
        } else {
            // If not a file it's probably a resource

            if (!sourceString.startsWith(SLASH)) {
                sourceString =
                    StringUtils.substringBeforeLast(testConfiguration.resourceName, SLASH) + SLASH + sourceString;
            }

            URL resource = getClass().getResource(sourceString);

            if (resource == null) {
                throw new FilterException("Resource [" + sourceString + "] does not exist");
            }

            if (resource.getProtocol().equals("file")) {
                // If the resource is a local file let's return it as such
                file = FileUtils.toFile(resource);
                source = new DefaultFileInputSource(file);
            } else {
                // Otherwise keep it as URL
                source = new DefaultURLInputSource(resource);
            }
        }

        return source;
    }

    private boolean isFileInputSource(FilterStreamPropertyDescriptor<?> propertyDescriptor)
    {
        return propertyDescriptor != null && propertyDescriptor.getType() == InputSource.class;
    }

    private Map<String, Object> toInputConfiguration(InputFilterStreamFactory inputFactory,
        TestConfiguration testConfiguration, InputTestConfiguration inputTestConfiguration) throws FilterException
    {
        Map<String, Object> inputConfiguration = new HashMap<>();
        for (Map.Entry<String, String> entry : inputTestConfiguration.entrySet()) {
            FilterStreamPropertyDescriptor<?> propertyDescriptor =
                inputFactory.getDescriptor().getPropertyDescriptor(entry.getKey());

            if (isFileInputSource(propertyDescriptor) && entry.getValue() != null
                && (entry.getKey().startsWith(FILE_PREFIX) || entry.getKey().indexOf(':') < 0)) {
                inputConfiguration.put(entry.getKey(), getInputSource(testConfiguration, entry.getValue()));
            } else {
                inputConfiguration.put(entry.getKey(), TestDataParser.interpret(entry.getValue()));
            }
        }

        // Generate a source f it does not exist
        if (!inputConfiguration.containsKey(FilterStreamConstants.PROPERTY_SOURCE)) {
            inputConfiguration.put(FilterStreamConstants.PROPERTY_SOURCE,
                new StringInputSource(inputTestConfiguration.buffer));
        }

        return inputConfiguration;
    }

    private Map<String, Object> toOutputConfiguration(ExpectTestConfiguration expectTestConfiguration,
        InputSource expect)
    {
        Map<String, Object> outputConfiguration = new HashMap<>();
        for (Map.Entry<String, String> entry : expectTestConfiguration.entrySet()) {
            outputConfiguration.put(entry.getKey(), TestDataParser.interpret(entry.getValue()));
        }

        // Generate a source if it does not exist
        if (!outputConfiguration.containsKey(FilterStreamConstants.PROPERTY_TARGET)) {
            if (expect instanceof ReaderInputSource) {
                outputConfiguration.put(FilterStreamConstants.PROPERTY_TARGET, new StringWriterOutputTarget());
            } else {
                outputConfiguration.put(FilterStreamConstants.PROPERTY_TARGET, new ByteArrayOutputTarget());
            }
        }

        // Format by default
        if (!outputConfiguration.containsKey(FilterStreamConstants.PROPERTY_FORMAT)) {
            outputConfiguration.put(FilterStreamConstants.PROPERTY_FORMAT, true);
        }

        // Encoding by default
        if (!outputConfiguration.containsKey(FilterStreamConstants.PROPERTY_ENCODING)) {
            outputConfiguration.put(FilterStreamConstants.PROPERTY_ENCODING, "UTF-8");
        }

        return outputConfiguration;
    }

    private InputSource getExpectInputSource(TestConfiguration testConfiguration,
        ExpectTestConfiguration expectConfiguration) throws FilterException
    {
        String expectPath = expectConfiguration.get(FilterStreamConstants.PROPERTY_SOURCE);
        if (expectPath == null) {
            return new StringInputSource(expectConfiguration.buffer);
        } else {
            return getInputSource(testConfiguration, expectPath);
        }
    }

    private void runTestInternal() throws Exception
    {
        // Expect

        InputSource expect = getExpectInputSource(this.configuration, this.configuration.expectConfiguration);

        // Input

        InputFilterStreamFactory inputFactory = getComponentManager().getInstance(InputFilterStreamFactory.class,
            this.configuration.inputConfiguration.typeId);
        Map<String, Object> outputConfiguration;
        try (InputFilterStream inputFilter = inputFactory.createInputFilterStream(
            toInputConfiguration(inputFactory, this.configuration, this.configuration.inputConfiguration))) {
            // Output

            outputConfiguration = toOutputConfiguration(this.configuration.expectConfiguration, expect);
            OutputFilterStreamFactory outputFactory = getComponentManager().getInstance(OutputFilterStreamFactory.class,
                this.configuration.expectConfiguration.typeId);
            try (OutputFilterStream outputFilter = outputFactory.createOutputFilterStream(outputConfiguration)) {
                // Convert

                inputFilter.read(outputFilter.getFilter());
            }
        }

        // Verify the expected result against the result we got.

        assertExpectedResult(expect, (OutputTarget) outputConfiguration.get(FilterStreamConstants.PROPERTY_TARGET));
    }

    private void assertExpectedResult(InputSource expected, OutputTarget actual) throws IOException
    {
        if (actual instanceof StringWriterOutputTarget) {
            assertEquals(expected.toString(), actual.toString());
        } else if (actual instanceof ByteArrayOutputTarget) {
            byte[] actualBytes = ((ByteArrayOutputTarget) actual).toByteArray();

            if (expected instanceof FileInputSource) {
                FileAssert.assertEquals(((FileInputSource) expected).getFile(), actualBytes);
            } else {
                byte[] expectedBytes = IOUtils.toByteArray(((InputStreamInputSource) expected).getInputStream());
                expected.close();

                assertArrayEquals(expectedBytes, actualBytes);
            }
        } else {
            // No idea how to compare that
            fail("Output target type [" + actual.getClass() + "] is not supported");
        }
    }

    /**
     * @return the component manager
     */
    public ComponentManager getComponentManager()
    {
        return this.componentManager;
    }
}
