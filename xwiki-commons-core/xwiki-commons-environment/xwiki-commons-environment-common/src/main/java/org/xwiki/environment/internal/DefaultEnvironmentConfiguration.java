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
package org.xwiki.environment.internal;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.EnvironmentConfiguration;

/**
 * Default implementation which uses the default Configuration Source to look for Environment configuration data.
 *
 * @version $Id$
 * @since 3.5M1
 */
@Component
@Singleton
public class DefaultEnvironmentConfiguration implements EnvironmentConfiguration
{
    /**
     * The name of the property for configuring the permanent directory.
     */
    private static final String PROPERTY_PERMANENTDIRECTORY = "environment.permanentDirectory";

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * @see #getConfigurationSource() 
     */
    @Inject
    private Provider<ConfigurationSource> configurationSourceProvider;

    /**
     * @return the configuration source from where to get configuration data from
     */
    protected ConfigurationSource getConfigurationSource()
    {
        return this.configurationSourceProvider.get();
    }

    @Override
    public File getPermanentDirectory()
    {
        return initializeDirectory(getConfigurationSource().getProperty(PROPERTY_PERMANENTDIRECTORY, String.class));
    }

    /**
     * @param directoryName the name of the directory to initialize (ensure it exists, create the directory) or null
     * @return the initialized directory as a {@link File} or null if the directort doesn't exist, cannot be created
     *         or the passed name was null
     */
    protected File initializeDirectory(String directoryName)
    {
        File directory = null;
        if (directoryName != null) {
            directory = new File(directoryName);
            if (directory.exists()) {
                if (!directory.isDirectory()) {
                    this.logger.error("Configured permanent directory [{}] is not a directory",
                        directory.getAbsolutePath());
                    directory = null;
                } else if (!directory.canWrite()) {
                    this.logger.error("Configured permanent directory [{}] is not writable",
                        directory.getAbsolutePath());
                    directory = null;
                }
            } else {
                directory.mkdirs();
            }
        }
        return directory;
    }
}
