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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides reusable implementation for skipping {@link }TestExecutionListener}.
 *
 * @version $Id$
 * @since 11.4RC1
 */
public abstract class AbstractTestExecutionListener implements TestExecutionListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestExecutionListener.class);

    private Boolean skip;

    /**
     * @return the System property key to skip the listener's execution
     */
    protected abstract String getSkipSystemPropertyKey();

    /**
     * {@inheritDoc}
     */
    protected abstract void executionStartedInternal(TestIdentifier testIdentifier);

    /**
     * {@inheritDoc}
     */
    protected abstract void executionFinishedInternal(TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult);

    @Override
    public void executionStarted(TestIdentifier testIdentifier)
    {
        if (shouldSkip()) {
            return;
        }
        executionStartedInternal(testIdentifier);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)
    {
        if (shouldSkip()) {
            return;
        }
        executionFinishedInternal(testIdentifier, testExecutionResult);
    }

    /**
     * @param propertyName the property to look for
     * @return the property value by first looking for it in System properties and then in the current {@code pom.xml}
     * file. Return null if not found in either.
     */
    protected String getPropertyValue(String propertyName)
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
    boolean shouldSkip()
    {
        if (this.skip == null) {
            String value = getPropertyValue(getSkipSystemPropertyKey());
            this.skip = Boolean.parseBoolean(value);
        }
        return this.skip;
    }

    private String getPropertyValueFromPOM(String propertyName)
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

    private Path getPOMPath()
    {
        return Paths.get("pom.xml");
    }
}
