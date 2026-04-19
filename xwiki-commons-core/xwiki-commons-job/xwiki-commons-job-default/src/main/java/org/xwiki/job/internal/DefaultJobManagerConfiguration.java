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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.job.JobManagerConfiguration;

/**
 * Default implementation of {@link JobManagerConfiguration}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultJobManagerConfiguration implements JobManagerConfiguration, Initializable
{
    static final String PROPERTY_JOB_LOG_DIRECTORY = "xwiki.job.log.dir";

    private static final String PROPERTY_JOB_LOG_FOLDER = "job.logFolder";

    /**
     * Used to get permanent directory.
     */
    @Inject
    private Environment environment;

    /**
     * The configuration.
     */
    @Inject
    @Named("restricted")
    private Provider<ConfigurationSource> configuration;

    // Cache

    /**
     * @see DefaultJobManagerConfiguration#getStorage()
     */
    private File store;

    @Override
    public void initialize() throws InitializationException
    {
        initializeJobLogDirectory();
    }

    /**
     * @return job manager home folder
     */
    private File getHome()
    {
        return new File(this.environment.getPermanentDirectory(), "jobs/");
    }

    @Override
    public File getStorage()
    {
        if (this.store == null) {
            String localRepositoryPath = this.configuration.get().getProperty("job.statusFolder");

            if (localRepositoryPath == null) {
                this.store = new File(getHome(), "status/");
            } else {
                this.store = new File(localRepositoryPath);
            }
        }

        return this.store;
    }

    @Override
    public int getJobStatusCacheSize()
    {
        return this.configuration.get().getProperty("job.statusCacheSize", 50);
    }

    @Override
    public int getGroupedJobInitializerCacheSize()
    {
        return this.configuration.get().getProperty("job.groupedJobInitializerCacheSize", 100);
    }

    @Override
    public long getSingleJobThreadKeepAliveTime()
    {
        return this.configuration.get().getProperty("job.singleJobThreadKeepAliveTime", 60000L);
    }

    @Override
    public long getGroupedJobThreadKeepAliveTime()
    {
        return this.configuration.get().getProperty("job.groupedJobThreadKeepAliveTime", 60000L);
    }

    private void initializeJobLogDirectory()
    {
        String configuredLogDirectory = System.getProperty(PROPERTY_JOB_LOG_DIRECTORY);

        if (configuredLogDirectory != null && !configuredLogDirectory.isBlank()) {
            System.setProperty(PROPERTY_JOB_LOG_DIRECTORY, new File(configuredLogDirectory).getAbsolutePath());
            return;
        }

        configuredLogDirectory = this.configuration.get().getProperty(PROPERTY_JOB_LOG_FOLDER);
        if (configuredLogDirectory != null && !configuredLogDirectory.isBlank()) {
            System.setProperty(PROPERTY_JOB_LOG_DIRECTORY, new File(configuredLogDirectory).getAbsolutePath());
            return;
        }

        File permanentDirectory = this.environment.getPermanentDirectory();
        if (permanentDirectory != null) {
            System.setProperty(PROPERTY_JOB_LOG_DIRECTORY,
                new File(permanentDirectory, "logs/jobs/").getAbsolutePath());
        }
    }
}
