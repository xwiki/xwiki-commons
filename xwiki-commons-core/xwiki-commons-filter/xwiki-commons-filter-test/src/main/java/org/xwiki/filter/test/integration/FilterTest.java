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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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
import org.xwiki.filter.test.internal.FileAssert;
import org.xwiki.filter.utils.FilterStreamConstants;
import org.xwiki.test.internal.MockConfigurationSource;

/**
 * A generic JUnit Test used by {@link FilterTestSuite} to parse some passed content and verify it matches some passed
 * expectation. The format of the input/expectation is specified in {@link TestDataParser}.
 * 
 * @version $Id$
 * @since 6.2M1
 */
public class FilterTest
{
    private TestConfiguration configuration;

    private ComponentManager componentManager;

    public FilterTest(TestConfiguration configuration, ComponentManager componentManager)
    {
        this.configuration = configuration;
        this.componentManager = componentManager;
    }

    @Test
    public void execute() throws Throwable
    {
        TimeZone currentTimeZone = TimeZone.getDefault();

        // Make sure to have a stable timezone during tests
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Map<String, String> originalConfiguration = new HashMap<String, String>();
        if (this.configuration.configuration != null) {
            ConfigurationSource configurationSource = getComponentManager().getInstance(ConfigurationSource.class);

            if (configurationSource instanceof MockConfigurationSource) {
                MockConfigurationSource mockConfigurationSource = (MockConfigurationSource) configurationSource;

                for (Map.Entry<String, String> entry : this.configuration.configuration.entrySet()) {
                    originalConfiguration.put(entry.getKey(),
                        mockConfigurationSource.<String>getProperty(entry.getKey()));
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

                if (configurationSource instanceof MockConfigurationSource) {
                    MockConfigurationSource mockConfigurationSource = (MockConfigurationSource) configurationSource;

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

        if (sourceString.startsWith("file:")) {
            sourceString = sourceString.substring("file:".length());
        }

        File file = new File(sourceString);

        if (file.exists()) {
            // It's a file

            source = new DefaultFileInputSource(file);
        } else {
            // If not a file it's probably a resource

            if (!sourceString.startsWith("/")) {
                sourceString =
                    StringUtils.substringBeforeLast(testConfiguration.resourceName, "/") + '/' + sourceString;
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

    private Map<String, Object> toInputConfiguration(InputFilterStreamFactory inputFactory,
        TestConfiguration testConfiguration, InputTestConfiguration inputTestConfiguration) throws FilterException
    {
        Map<String, Object> inputConfiguration = new HashMap<>();
        for (Map.Entry<String, String> entry : inputTestConfiguration.entrySet()) {
            FilterStreamPropertyDescriptor<?> propertyDescriptor =
                inputFactory.getDescriptor().getPropertyDescriptor(entry.getKey());

            if (propertyDescriptor != null && propertyDescriptor.getType() == InputSource.class
                && entry.getValue() != null
                && (entry.getKey().startsWith("file:") || entry.getKey().indexOf(':') < 0)) {
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

    private Map<String, Object> toOutputConfiguration(TestConfiguration testConfiguration,
        ExpectTestConfiguration expectTestConfiguration, InputSource expect)
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
            return new StringInputSource(expectConfiguration.buffer.toString());
        } else {
            return getInputSource(testConfiguration, expectPath);
        }
    }

    private void runTestInternal() throws Throwable
    {
        // Expect

        InputSource expect = getExpectInputSource(this.configuration, this.configuration.expectConfiguration);

        // Input

        InputFilterStreamFactory inputFactory = getComponentManager().getInstance(InputFilterStreamFactory.class,
            this.configuration.inputConfiguration.typeId);
        InputFilterStream inputFilter = inputFactory.createInputFilterStream(
            toInputConfiguration(inputFactory, this.configuration, this.configuration.inputConfiguration));

        // Output

        Map<String, Object> outputConfiguration =
            toOutputConfiguration(this.configuration, this.configuration.expectConfiguration, expect);
        OutputFilterStreamFactory outputFactory = getComponentManager().getInstance(OutputFilterStreamFactory.class,
            this.configuration.expectConfiguration.typeId);
        OutputFilterStream outputFilter = outputFactory.createOutputFilterStream(outputConfiguration);

        // Convert

        inputFilter.read(outputFilter.getFilter());

        inputFilter.close();
        outputFilter.close();

        // Verify the expected result against the result we got.

        assertExpectedResult(this.configuration.expectConfiguration.typeId, expect,
            (OutputTarget) outputConfiguration.get(FilterStreamConstants.PROPERTY_TARGET));
    }

    private void assertExpectedResult(String typeId, InputSource expected, OutputTarget actual) throws IOException
    {
        if (actual instanceof StringWriterOutputTarget) {
            Assertions.assertEquals(expected.toString(), actual.toString());
        } else if (actual instanceof ByteArrayOutputTarget) {
            byte[] actualBytes = ((ByteArrayOutputTarget) actual).toByteArray();

            if (expected instanceof FileInputSource) {
                FileAssert.assertEquals(((FileInputSource) expected).getFile(), actualBytes);
            } else {
                byte[] expectedBytes = IOUtils.toByteArray(((InputStreamInputSource) expected).getInputStream());
                expected.close();

                Assertions.assertArrayEquals(expectedBytes, actualBytes);
            }
        } else {
            // No idea how to compare that
            Assertions.fail("Output target type [" + actual.getClass() + "] is not supported");
        }
    }

    public ComponentManager getComponentManager() throws Exception
    {
        return this.componentManager;
    }
}
