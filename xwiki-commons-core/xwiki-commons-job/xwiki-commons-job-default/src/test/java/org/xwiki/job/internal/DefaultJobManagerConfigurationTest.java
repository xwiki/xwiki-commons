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
package org.xwiki.job.internal;

import java.io.File;

import jakarta.inject.Named;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultJobManagerConfiguration}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultJobManagerConfigurationTest
{
    private static final String JOB_LOG_DIRECTORY_PROPERTY = DefaultJobManagerConfiguration.PROPERTY_JOB_LOG_DIRECTORY;

    @InjectMockComponents
    private DefaultJobManagerConfiguration configuration;

    @MockComponent
    @Named("restricted")
    private ConfigurationSource configurationSource;

    @MockComponent
    private Environment environment;

    private File permDir = new File("permDir");

    private File directory = new File("directory");

    private String previousJobLogDirectoryProperty;

    @BeforeComponent
    void beforeComponent()
    {
        this.previousJobLogDirectoryProperty = System.getProperty(JOB_LOG_DIRECTORY_PROPERTY);
        System.clearProperty(JOB_LOG_DIRECTORY_PROPERTY);
        when(this.environment.getPermanentDirectory()).thenReturn(this.permDir);
    }

    @AfterEach
    void afterEach()
    {
        if (this.previousJobLogDirectoryProperty != null) {
            System.setProperty(JOB_LOG_DIRECTORY_PROPERTY, this.previousJobLogDirectoryProperty);
        } else {
            System.clearProperty(JOB_LOG_DIRECTORY_PROPERTY);
        }
    }

    @Test
    void getStorageWithProperty()
    {
        when(this.configurationSource.getProperty("job.statusFolder")).thenReturn(this.directory.toString());

        assertEquals(this.directory.getAbsoluteFile(), this.configuration.getStorage().getAbsoluteFile());
        assertEquals(new File(this.permDir, "logs/jobs").getAbsolutePath(),
            System.getProperty(JOB_LOG_DIRECTORY_PROPERTY));
    }

    @Test
    void getStorageWithoutProperty()
    {
        assertEquals(new File(this.permDir, "jobs/status/").getAbsoluteFile(),
            this.configuration.getStorage().getAbsoluteFile());
        assertEquals(new File(this.permDir, "logs/jobs").getAbsolutePath(),
            System.getProperty(JOB_LOG_DIRECTORY_PROPERTY));
    }

    @Test
    void keepConfiguredJobLogDirectory() throws InitializationException
    {
        System.setProperty(JOB_LOG_DIRECTORY_PROPERTY, "/tmp/custom-job-logs");

        this.configuration.initialize();

        assertEquals("/tmp/custom-job-logs", System.getProperty(JOB_LOG_DIRECTORY_PROPERTY));
    }

    @Test
    void useConfiguredJobLogDirectoryWhenSystemPropertyMissing() throws InitializationException
    {
        when(this.configurationSource.getProperty("job.logFolder")).thenReturn("configured-job-logs");
        System.clearProperty(JOB_LOG_DIRECTORY_PROPERTY);

        this.configuration.initialize();

        assertEquals(new File("configured-job-logs").getAbsolutePath(),
            System.getProperty(JOB_LOG_DIRECTORY_PROPERTY));
    }

    @Test
    void preferSystemPropertyOverConfiguredJobLogDirectory() throws InitializationException
    {
        when(this.configurationSource.getProperty("job.logFolder")).thenReturn("configured-job-logs");
        System.setProperty(JOB_LOG_DIRECTORY_PROPERTY, "/tmp/custom-job-logs");

        this.configuration.initialize();

        assertEquals("/tmp/custom-job-logs", System.getProperty(JOB_LOG_DIRECTORY_PROPERTY));
    }
}
