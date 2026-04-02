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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Provide configuration to modify {@link ServletEnvironment}'s behavior..
 *
 * @version $Id$
 * @since 18.2.1
 * @since 17.10.6
 */
@Component(roles = ServletEnvironmentConfiguration.class)
@Singleton
public class ServletEnvironmentConfiguration
{
    private static final String PROPERTY_ALLOWEDREALPATHS = "environment.servlet.allowedRealPaths";

    private static final List<String> DEFAULT_ALLOWEDREALPATHS = List.of("/etc/xwiki/");

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

    /**
     * @return the list of allowed real paths which are allowed to contain resources
     */
    public List<String> getAllowedRealPaths()
    {
        return getConfigurationSource().getProperty(PROPERTY_ALLOWEDREALPATHS, DEFAULT_ALLOWEDREALPATHS);
    }
}
