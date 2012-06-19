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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

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
     * @see #getConfigurationSource()
     *
     * Note that we use a Provider instead of directly injecting a ConfigurationSource so that we always get a valid
     * Configuration Source even if no "restricted" Configuration Source implementation is provided (in this case it'll
     * default to using a Memory Configuration Source or a Void Configuration Source if no Memory one is found).
     */
    @Inject
    @Named("restricted")
    private Provider<ConfigurationSource> configurationSourceProvider;

    /**
     * @return the configuration source from where to get configuration data from
     */
    protected ConfigurationSource getConfigurationSource()
    {
        return this.configurationSourceProvider.get();
    }

    @Override
    public String getPermanentDirectoryPath()
    {
        return getConfigurationSource().getProperty(PROPERTY_PERMANENTDIRECTORY, String.class);
    }
}
